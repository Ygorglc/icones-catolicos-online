package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.catalogo.ConteudoDevocionalResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeResumoResponse;
import dev.y.works.iconescatolicosonline.exception.TratamentoGlobalExceptionHandler;
import dev.y.works.iconescatolicosonline.service.catalogo.ModeloIconeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeloIconeController.class)
@Import(TratamentoGlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ModeloIconeControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModeloIconeService modeloIconeService;

    @Test
    void deveListarCatalogoPublico() throws Exception {
        when(modeloIconeService.listarModelosAtivos()).thenReturn(List.of(
                new ModeloIconeResumoResponse(
                        1L,
                        "Sagrada Família",
                        "/imagens/sagrada-familia.jpg",
                        new BigDecimal("320.00")
                )
        ));

        mockMvc.perform(get("/api/publico/modelos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].nome").value("Sagrada Família"))
                .andExpect(jsonPath("$[0].precoBase").value(320.00));
    }

    @Test
    void deveRetornarDetalhesDoModelo() throws Exception {
        when(modeloIconeService.buscarModeloAtivo(1L)).thenReturn(
                new ModeloIconeDetalheResponse(
                        1L,
                        "Sagrada Família",
                        "Ícone artesanal em madeira.",
                        "/imagens/sagrada-familia.jpg",
                        new BigDecimal("320.00"),
                        true,
                        Instant.parse("2026-08-08T12:00:00Z"),
                        new ConteudoDevocionalResponse(
                                10L,
                                null,
                                "União e cuidado da família.",
                                null,
                                null,
                                null,
                                null
                        )
                )
        );

        mockMvc.perform(get("/api/publico/modelos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ativo").value(true))
                .andExpect(jsonPath("$.conteudoDevocional.id").value(10))
                .andExpect(jsonPath("$.conteudoDevocional.significado")
                        .value("União e cuidado da família."));
    }
}
