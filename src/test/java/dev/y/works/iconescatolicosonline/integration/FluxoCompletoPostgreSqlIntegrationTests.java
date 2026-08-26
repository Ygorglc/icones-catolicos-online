package dev.y.works.iconescatolicosonline.integration;

import com.jayway.jsonpath.JsonPath;
import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.CertificadoArtesanalRepository;
import dev.y.works.iconescatolicosonline.repository.pagamento.PagamentoRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("integration-test")
@Testcontainers(disabledWithoutDocker = true)
class FluxoCompletoPostgreSqlIntegrationTests {

    private static final String ADMIN_EMAIL = "admin.integracao@icones.local";
    private static final String ADMIN_SENHA = "admin12345";

    @Container
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine")
            .withDatabaseName("icones_integracao")
            .withUsername("icones_test")
            .withPassword("icones_test");

    @DynamicPropertySource
    static void configurarPostgreSql(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.docker.compose.enabled", () -> "false");
        registry.add("app.dados-demonstracao.enabled", () -> "false");
    }

    @Autowired MockMvc mockMvc;
    @Autowired UsuarioRepository usuarioRepository;
    @Autowired ClienteRepository clienteRepository;
    @Autowired AdministradorRepository administradorRepository;
    @Autowired ModeloIconeRepository modeloRepository;
    @Autowired EncomendaRepository encomendaRepository;
    @Autowired PagamentoRepository pagamentoRepository;
    @Autowired CertificadoArtesanalRepository certificadoRepository;
    @Autowired PasswordEncoder passwordEncoder;
    private Long modeloId;

    @BeforeEach
    void prepararCatalogoEAdministrador() {
        Administrador administrador = administradorRepository
                .findByUsuario_EmailIgnoreCase(ADMIN_EMAIL)
                .orElseGet(this::criarAdministrador);

        ModeloIcone modelo = new ModeloIcone();
        modelo.setNome("Sagrada Família - Integração " + System.nanoTime());
        modelo.setDescricao("Modelo criado pelo teste integrado.");
        modelo.setPrecoBase(new BigDecimal("300.00"));
        modelo.setAtivo(true);
        modeloId = modeloRepository.save(modelo).getId();
        assertThat(administrador.getId()).isNotNull();
    }

    @Test
    void deveExecutarFluxoCompletoComRegrasPermissoesEPersistencia() throws Exception {
        String emailCliente = "cliente." + System.nanoTime() + "@teste.local";
        String tokenCliente = cadastrarCliente(emailCliente);
        String tokenAdmin = autenticar(ADMIN_EMAIL, ADMIN_SENHA);

        mockMvc.perform(get("/api/admin/encomendas"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/admin/encomendas")
                        .header("Authorization", bearer(tokenCliente)))
                .andExpect(status().isForbidden());

        MvcResult criacao = mockMvc.perform(post("/api/encomendas")
                        .header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipoEntrega": "ENTREGA",
                                  "enderecoEntrega": "Rua do Teste, 100",
                                  "observacoes": "Fluxo integrado",
                                  "itens": [{
                                    "modeloIconeId": %d,
                                    "quantidade": 1,
                                    "personalizacao": {
                                      "tamanho": "MEDIO",
                                      "acabamento": "Envernizado",
                                      "frase": "Deus abençoe este lar",
                                      "nomeFamilia": "Família Teste"
                                    }
                                  }]
                                }
                                """.formatted(modeloId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.valorTotal").value(300.0))
                .andExpect(jsonPath("$.valorSinal").value(90.0))
                .andExpect(jsonPath("$.statusEncomenda")
                        .value("AGUARDANDO_PAGAMENTO_SINAL"))
                .andReturn();
        Long encomendaId = numero(criacao, "$.id");

        atualizarStatus(encomendaId, "CONCLUIDO", tokenAdmin, 422);

        mockMvc.perform(post("/api/admin/estoque/icones-prontos")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "modeloIconeId": %d,
                                  "tamanho": "MEDIO",
                                  "acabamento": "Envernizado",
                                  "custoProducao": 120.00,
                                  "precoSugerido": 300.00,
                                  "status": "DISPONIVEL",
                                  "localizacao": "Prateleira de integração"
                                }
                                """.formatted(modeloId)))
                .andExpect(status().isCreated());
        mockMvc.perform(post("/api/encomendas/{id}/pagamentos", encomendaId)
                        .header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "SINAL",
                                  "forma": "PIX",
                                  "origem": "SIMULADO_SISTEMA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("CONFIRMADO"))
                .andExpect(jsonPath("$.statusEncomenda").value("PRODUCAO_LIBERADA"));

        mockMvc.perform(post("/api/admin/estoque/icones-prontos/reservas")
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"encomendaId\":" + encomendaId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RESERVADO"));

        atualizarStatus(encomendaId, "EM_PRODUCAO", tokenAdmin, 200);
        atualizarStatus(encomendaId, "EM_ACABAMENTO", tokenAdmin, 200);
        atualizarStatus(encomendaId, "PRONTO_PARA_ENTREGA_RETIRADA", tokenAdmin, 200);
        atualizarStatus(encomendaId, "AGUARDANDO_PAGAMENTO_RESTANTE", tokenAdmin, 200);

        mockMvc.perform(post("/api/encomendas/{id}/pagamentos", encomendaId)
                        .header("Authorization", bearer(tokenCliente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "tipo": "RESTANTE",
                                  "forma": "CARTAO_CREDITO",
                                  "origem": "SIMULADO_SISTEMA"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.saldoPendente").value(0.0))
                .andExpect(jsonPath("$.statusFinanceiro").value("PAGO_INTEGRALMENTE"));

        atualizarStatus(encomendaId, "ENVIADO_OU_RETIRADO", tokenAdmin, 200);
        atualizarStatus(encomendaId, "CONCLUIDO", tokenAdmin, 200);

        MvcResult emissao = mockMvc.perform(post(
                                "/api/admin/certificados/encomendas/{id}", encomendaId)
                        .header("Authorization", bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nomeArtesao": "Artesão do Teste",
                                  "materialUtilizado": "Madeira de cedro e verniz"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location",
                        org.hamcrest.Matchers.startsWith("/api/publico/certificados/")))
                .andExpect(jsonPath("$.autentico").value(true))
                .andReturn();
        String codigoPublico = texto(emissao, "$.codigoPublico");

        mockMvc.perform(get("/api/publico/certificados/{codigo}", codigoPublico))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPublico").value(codigoPublico))
                .andExpect(jsonPath("$.modeloIcone").exists())
                .andExpect(jsonPath("$.nomeArtesao").value("Artesão do Teste"));

        assertThat(clienteRepository.findByUsuario_EmailIgnoreCase(emailCliente)).isPresent();
        assertThat(encomendaRepository.findById(encomendaId))
                .get().extracting(encomenda -> encomenda.getStatusEncomenda().name())
                .isEqualTo("CONCLUIDO");
        assertThat(pagamentoRepository.findByEncomenda_IdOrderByCriadoEmAsc(encomendaId))
                .hasSize(2);
        assertThat(certificadoRepository.findByCodigoPublico(codigoPublico)).isPresent();
    }

    private String cadastrarCliente(String email) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/cadastro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Cliente Integração",
                                  "email": "%s",
                                  "senha": "cliente123",
                                  "telefone": "11999998888",
                                  "cpf": "52998224725"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.mensagem").exists())
                .andReturn();
        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(email).orElseThrow();
        usuario.setEmailVerificado(true);
        usuario.setTokenConfirmacaoEmailHash(null);
        usuario.setTokenConfirmacaoEmailExpiraEm(null);
        usuarioRepository.save(usuario);
        return autenticar(email, "cliente123");
    }

    private String autenticar(String email, String senha) throws Exception {
        MvcResult resultado = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"%s","senha":"%s"}
                                """.formatted(email, senha)))
                .andExpect(status().isOk())
                .andReturn();
        return texto(resultado, "$.token");
    }

    private void atualizarStatus(
            Long encomendaId, String status, String token, int esperado) throws Exception {
        mockMvc.perform(patch("/api/admin/encomendas/{id}/status", encomendaId)
                        .header("Authorization", bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().is(esperado));
    }

    private Administrador criarAdministrador() {
        Usuario usuario = new Usuario();
        usuario.setNome("Administrador Integração");
        usuario.setEmail(ADMIN_EMAIL);
        usuario.setSenha(passwordEncoder.encode(ADMIN_SENHA));
        usuario.setPerfil(PerfilUsuario.ADMINISTRADOR);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        Administrador administrador = new Administrador();
        administrador.setUsuario(usuario);
        administrador.setCargo("Administrador de testes");
        administrador.setNivelAcesso("TOTAL");
        return administradorRepository.save(administrador);
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }

    private String texto(MvcResult resultado, String caminho) throws Exception {
        return JsonPath.read(resultado.getResponse().getContentAsString(), caminho);
    }

    private Long numero(MvcResult resultado, String caminho) throws Exception {
        Number numero = JsonPath.read(resultado.getResponse().getContentAsString(), caminho);
        return numero.longValue();
    }
}
