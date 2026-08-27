package dev.y.works.iconescatolicosonline.config;

import dev.y.works.iconescatolicosonline.domain.catalogo.ConteudoDevocional;
import dev.y.works.iconescatolicosonline.domain.catalogo.ModeloIcone;
import dev.y.works.iconescatolicosonline.domain.catalogo.TamanhoIcone;
import dev.y.works.iconescatolicosonline.domain.encomenda.Encomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.ItemEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.Personalizacao;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusEncomenda;
import dev.y.works.iconescatolicosonline.domain.encomenda.StatusFinanceiro;
import dev.y.works.iconescatolicosonline.domain.encomenda.TipoEntrega;
import dev.y.works.iconescatolicosonline.domain.estoque.IconePronto;
import dev.y.works.iconescatolicosonline.domain.estoque.Material;
import dev.y.works.iconescatolicosonline.domain.estoque.StatusIconePronto;
import dev.y.works.iconescatolicosonline.domain.financeiro.Gasto;
import dev.y.works.iconescatolicosonline.domain.financeiro.Venda;
import dev.y.works.iconescatolicosonline.domain.pagamento.FormaPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.OrigemPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.Pagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.StatusPagamento;
import dev.y.works.iconescatolicosonline.domain.pagamento.TipoPagamento;
import dev.y.works.iconescatolicosonline.domain.usuario.Administrador;
import dev.y.works.iconescatolicosonline.domain.usuario.Cliente;
import dev.y.works.iconescatolicosonline.domain.usuario.PerfilUsuario;
import dev.y.works.iconescatolicosonline.domain.usuario.Usuario;
import dev.y.works.iconescatolicosonline.repository.catalogo.ConteudoDevocionalRepository;
import dev.y.works.iconescatolicosonline.repository.catalogo.ModeloIconeRepository;
import dev.y.works.iconescatolicosonline.repository.encomenda.EncomendaRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.IconeProntoRepository;
import dev.y.works.iconescatolicosonline.repository.estoque.MaterialRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.GastoRepository;
import dev.y.works.iconescatolicosonline.repository.financeiro.VendaRepository;
import dev.y.works.iconescatolicosonline.repository.pagamento.PagamentoRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.AdministradorRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.ClienteRepository;
import dev.y.works.iconescatolicosonline.repository.usuario.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

@Configuration
@Profile("dev")
@Order(2)
@ConditionalOnProperty(name = "app.dados-demonstracao.enabled", havingValue = "true")
public class DadosDemonstracaoConfig implements ApplicationRunner {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final AdministradorRepository administradorRepository;
    private final ModeloIconeRepository modeloRepository;
    private final ConteudoDevocionalRepository conteudoRepository;
    private final EncomendaRepository encomendaRepository;
    private final MaterialRepository materialRepository;
    private final IconeProntoRepository iconeProntoRepository;
    private final PagamentoRepository pagamentoRepository;
    private final GastoRepository gastoRepository;
    private final VendaRepository vendaRepository;
    private final PasswordEncoder passwordEncoder;
    private final String clienteEmail;
    private final String clienteSenha;

    public DadosDemonstracaoConfig(
            UsuarioRepository usuarioRepository,
            ClienteRepository clienteRepository,
            AdministradorRepository administradorRepository,
            ModeloIconeRepository modeloRepository,
            ConteudoDevocionalRepository conteudoRepository,
            EncomendaRepository encomendaRepository,
            MaterialRepository materialRepository,
            IconeProntoRepository iconeProntoRepository,
            PagamentoRepository pagamentoRepository,
            GastoRepository gastoRepository,
            VendaRepository vendaRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.dados-demonstracao.cliente-email}") String clienteEmail,
            @Value("${app.dados-demonstracao.cliente-senha}") String clienteSenha) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.administradorRepository = administradorRepository;
        this.modeloRepository = modeloRepository;
        this.conteudoRepository = conteudoRepository;
        this.encomendaRepository = encomendaRepository;
        this.materialRepository = materialRepository;
        this.iconeProntoRepository = iconeProntoRepository;
        this.pagamentoRepository = pagamentoRepository;
        this.gastoRepository = gastoRepository;
        this.vendaRepository = vendaRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteEmail = clienteEmail.trim().toLowerCase(Locale.ROOT);
        this.clienteSenha = clienteSenha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarioRepository.existsByEmailIgnoreCase(clienteEmail)) {
            return;
        }

        Cliente cliente = criarCliente();
        Administrador administrador = administradorRepository.findAll().stream()
                .findFirst().orElseThrow(() -> new IllegalStateException(
                        "O administrador inicial deve ser criado antes dos dados de demonstração."));

        ModeloIcone sagradaFamilia = criarModelo(
                "Sagrada Família", "Ícone artesanal da Sagrada Família em madeira.", "320.00");
        ModeloIcone nossaSenhora = criarModelo(
                "Nossa Senhora Aparecida", "Ícone da padroeira do Brasil entalhado em madeira.", "280.00");
        ModeloIcone saoBento = criarModelo(
                "São Bento", "Ícone artesanal de São Bento para ambientes de oração.", "250.00");

        criarConteudo(sagradaFamilia,
                "A Sagrada Família inspira a vida familiar cristã.",
                "Representa união, fé e cuidado no lar.",
                "Jesus, Maria e José simbolizam amor e comunhão.",
                "Sagrada Família de Nazaré, protegei nossas famílias.");
        criarConteudo(nossaSenhora,
                "A devoção surgiu após o encontro da imagem no rio Paraíba do Sul.",
                "Recorda a proteção materna de Maria ao povo brasileiro.",
                "O manto azul e a coroa representam sua dignidade de Rainha.",
                "Nossa Senhora Aparecida, rogai por nós.");
        criarConteudo(saoBento,
                "São Bento é referência de oração, trabalho e vida comunitária.",
                "Seu testemunho convida à disciplina e à busca de Deus.",
                "A medalha recorda a confiança cristã diante do mal.",
                "São Bento, intercedei por nós.");

        criarMateriais();
        criarIconeDisponivel(nossaSenhora);
        criarIconeDisponivel(saoBento);

        Encomenda aguardandoSinal = criarEncomenda(cliente, nossaSenhora,
                StatusEncomenda.AGUARDANDO_PAGAMENTO_INICIAL,
                StatusFinanceiro.AGUARDANDO_SINAL, "280.00", "84.00",
                TamanhoIcone.PEQUENO, "Cera natural", 2);
        criarPagamentoPendente(aguardandoSinal, "84.00");

        Encomenda producao = criarEncomenda(cliente, sagradaFamilia,
                StatusEncomenda.EM_PRODUCAO, StatusFinanceiro.SINAL_PAGO,
                "420.00", "126.00", TamanhoIcone.MEDIO, "Envernizado", 12);
        criarPagamento(producao, TipoPagamento.SINAL, FormaPagamento.PIX,
                OrigemPagamento.SIMULADO_SISTEMA, "126.00", administrador, 11);

        Encomenda acabamento = criarEncomenda(cliente, saoBento,
                StatusEncomenda.EM_ACABAMENTO, StatusFinanceiro.PAGAMENTO_PARCIAL,
                "390.00", "117.00", TamanhoIcone.PERSONALIZADO, "Pátina", 24);
        criarPagamento(acabamento, TipoPagamento.SINAL, FormaPagamento.DINHEIRO,
                OrigemPagamento.EXTERNO_MANUAL, "150.00", administrador, 23);
        criarGasto(acabamento, "Madeira selecionada para peça personalizada",
                "85.00", "MATÉRIA-PRIMA", 22);

        Encomenda concluida = criarEncomenda(cliente, sagradaFamilia,
                StatusEncomenda.CONCLUIDO, StatusFinanceiro.PAGO_INTEGRALMENTE,
                "520.00", "156.00", TamanhoIcone.GRANDE, "Envernizado", 45);
        criarPagamento(concluida, TipoPagamento.INTEGRAL, FormaPagamento.DEPOSITO,
                OrigemPagamento.SIMULADO_SISTEMA, "520.00", administrador, 44);
        criarIconeVendido(sagradaFamilia, concluida);
        criarGasto(concluida, "Madeira, tintas e acabamento da encomenda",
                "145.00", "PRODUÇÃO", 43);
        criarGasto(concluida, "Embalagem protetora para entrega",
                "25.00", "EMBALAGEM", 42);
        criarVenda(concluida, "520.00", "375.00", "350.00", 40);

        criarGasto(null, "Compra de pincéis para o ateliê",
                "48.00", "FERRAMENTAS", 15);
    }

    private Cliente criarCliente() {
        Usuario usuario = new Usuario();
        usuario.setNome("Cliente Demonstração");
        usuario.setEmail(clienteEmail);
        usuario.setSenha(passwordEncoder.encode(clienteSenha));
        usuario.setPerfil(PerfilUsuario.CLIENTE);
        usuario.setAtivo(true);
        usuario = usuarioRepository.save(usuario);

        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario);
        cliente.setTelefone("11999990000");
        cliente.setCpf("12345678901");
        return clienteRepository.save(cliente);
    }

    private ModeloIcone criarModelo(String nome, String descricao, String preco) {
        ModeloIcone modelo = new ModeloIcone();
        modelo.setNome(nome);
        modelo.setDescricao(descricao);
        modelo.setImagemUrl("https://placehold.co/600x600?text="
                + nome.replace(" ", "+"));
        modelo.setPrecoBase(new BigDecimal(preco));
        modelo.setAtivo(true);
        return modeloRepository.save(modelo);
    }

    private void criarConteudo(ModeloIcone modelo, String historia, String significado,
                               String simbologia, String oracao) {
        ConteudoDevocional conteudo = new ConteudoDevocional();
        conteudo.setModeloIcone(modelo);
        conteudo.setHistoria(historia);
        conteudo.setSignificado(significado);
        conteudo.setSimbologia(simbologia);
        conteudo.setOracao(oracao);
        conteudo.setOcasiaoPresente("Batizados, casamentos, aniversários e formação de um lar.");
        conteudo.setCuidados("Limpar com pano seco e evitar exposição direta à umidade.");
        conteudoRepository.save(conteudo);
    }

    private void criarMateriais() {
        criarMaterial("Madeira de cedro", "UNIDADE", "18.000", "45.00", "5.000");
        criarMaterial("Tinta acrílica", "MILILITRO", "2500.000", "0.08", "500.000");
        criarMaterial("Verniz", "MILILITRO", "1200.000", "0.06", "300.000");
        criarMaterial("Folha dourada", "UNIDADE", "8.000", "12.50", "10.000");
    }

    private void criarMaterial(String nome, String unidade, String quantidade,
                               String custo, String minimo) {
        Material material = new Material();
        material.setNome(nome);
        material.setUnidadeMedida(unidade);
        material.setQuantidade(new BigDecimal(quantidade));
        material.setCustoUnitario(new BigDecimal(custo));
        material.setEstoqueMinimo(new BigDecimal(minimo));
        materialRepository.save(material);
    }

    private Encomenda criarEncomenda(
            Cliente cliente, ModeloIcone modelo, StatusEncomenda status,
            StatusFinanceiro financeiro, String total, String sinal,
            TamanhoIcone tamanho, String acabamento, long diasAtras) {
        Encomenda encomenda = new Encomenda();
        encomenda.setCliente(cliente);
        encomenda.setDataCriacao(Instant.now().minus(diasAtras, ChronoUnit.DAYS));
        encomenda.setStatusEncomenda(status);
        encomenda.setStatusFinanceiro(financeiro);
        encomenda.setValorTotal(new BigDecimal(total));
        encomenda.setValorSinal(new BigDecimal(sinal));
        encomenda.setTipoEntrega(TipoEntrega.ENTREGA);
        encomenda.setEnderecoEntrega("Rua das Artes, 100 - Centro, Rio de Janeiro/RJ - CEP 20040002");
        encomenda.setObservacoes("Registro fictício criado para demonstração acadêmica.");

        ItemEncomenda item = new ItemEncomenda();
        item.setEncomenda(encomenda);
        item.setModeloIcone(modelo);
        item.setQuantidade(1);
        item.setValorUnitario(new BigDecimal(total));

        Personalizacao personalizacao = new Personalizacao();
        personalizacao.setItemEncomenda(item);
        personalizacao.setTamanho(tamanho);
        personalizacao.setAcabamento(acabamento);
        personalizacao.setNomeFamilia("Família Demonstração");
        personalizacao.setFrase("Deus abençoe este lar");
        item.setPersonalizacao(personalizacao);
        encomenda.getItens().add(item);
        return encomendaRepository.save(encomenda);
    }

    private void criarPagamento(
            Encomenda encomenda, TipoPagamento tipo, FormaPagamento forma,
            OrigemPagamento origem, String valor, Administrador administrador,
            long diasAtras) {
        Instant data = Instant.now().minus(diasAtras, ChronoUnit.DAYS);
        Pagamento pagamento = new Pagamento();
        pagamento.setEncomenda(encomenda);
        pagamento.setTipo(tipo);
        pagamento.setFormaPagamento(forma);
        pagamento.setOrigem(origem);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setStatus(StatusPagamento.CONFIRMADO);
        pagamento.setCriadoEm(data);
        pagamento.setDataPagamento(data);
        pagamento.setReferenciaSimulada("DEMO-" + encomenda.getId());
        pagamento.setAnalisadoPor(administrador);
        pagamento.setDataAnalise(data);
        pagamento.setObservacaoAdministrativa("Pagamento fictício confirmado para demonstração.");
        pagamentoRepository.save(pagamento);
    }

    private void criarPagamentoPendente(Encomenda encomenda, String valor) {
        Pagamento pagamento = new Pagamento();
        pagamento.setEncomenda(encomenda);
        pagamento.setTipo(TipoPagamento.SINAL);
        pagamento.setFormaPagamento(FormaPagamento.DEPOSITO);
        pagamento.setOrigem(OrigemPagamento.SIMULADO_SISTEMA);
        pagamento.setValor(new BigDecimal(valor));
        pagamento.setStatus(StatusPagamento.PENDENTE);
        pagamento.setReferenciaSimulada("DEMO-PENDENTE-" + encomenda.getId());
        pagamentoRepository.save(pagamento);
    }

    private void criarIconeDisponivel(ModeloIcone modelo) {
        IconePronto icone = novoIcone(modelo, TamanhoIcone.MEDIO, "Envernizado");
        icone.setStatus(StatusIconePronto.DISPONIVEL);
        icone.setLocalizacao("Prateleira de demonstração");
        iconeProntoRepository.save(icone);
    }

    private void criarIconeVendido(ModeloIcone modelo, Encomenda encomenda) {
        IconePronto icone = novoIcone(modelo, TamanhoIcone.GRANDE, "Envernizado");
        icone.setEncomenda(encomenda);
        icone.setStatus(StatusIconePronto.VENDIDO);
        icone.setLocalizacao("Entregue ao cliente");
        iconeProntoRepository.save(icone);
    }

    private IconePronto novoIcone(
            ModeloIcone modelo, TamanhoIcone tamanho, String acabamento) {
        IconePronto icone = new IconePronto();
        icone.setModeloIcone(modelo);
        icone.setTamanho(tamanho);
        icone.setAcabamento(acabamento);
        icone.setCustoProducao(new BigDecimal("140.00"));
        icone.setPrecoSugerido(new BigDecimal("350.00"));
        return icone;
    }

    private void criarGasto(Encomenda encomenda, String descricao, String valor,
                            String categoria, long diasAtras) {
        Gasto gasto = new Gasto();
        gasto.setEncomenda(encomenda);
        gasto.setDescricao(descricao);
        gasto.setValor(new BigDecimal(valor));
        gasto.setCategoria(categoria);
        gasto.setDataGasto(LocalDate.now().minusDays(diasAtras));
        gastoRepository.save(gasto);
    }

    private void criarVenda(Encomenda encomenda, String total, String bruto,
                            String liquido, long diasAtras) {
        Venda venda = new Venda();
        venda.setEncomenda(encomenda);
        venda.setValorTotal(new BigDecimal(total));
        venda.setDataVenda(Instant.now().minus(diasAtras, ChronoUnit.DAYS));
        venda.setLucroBruto(new BigDecimal(bruto));
        venda.setLucroLiquidoEstimado(new BigDecimal(liquido));
        vendaRepository.save(venda);
    }
}
