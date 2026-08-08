package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeDetalheResponse;
import dev.y.works.iconescatolicosonline.dto.catalogo.ModeloIconeRequest;
import dev.y.works.iconescatolicosonline.exception.TratamentoGlobalExceptionHandler;
import dev.y.works.iconescatolicosonline.service.catalogo.ModeloIconeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ModeloIconeAdminController.class)
@Import(TratamentoGlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class ModeloIconeAdminControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ModeloIconeService modeloIconeService;

    @Test
    void deveCriarModelo() throws Exception {
        when(modeloIconeService.criar(any(ModeloIconeRequest.class))).thenReturn(
                new ModeloIconeDetalheResponse(
                        5L, "São Bento", "Ícone artesanal", null,
                        new BigDecimal("250.00"), true,
                        Instant.parse("2026-08-08T12:00:00Z"), null));

        mockMvc.perform(post("/api/admin/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "São Bento",
                                  "descricao": "Ícone artesanal",
                                  "precoBase": 250.00
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/admin/modelos/5"))
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.nome").value("São Bento"));
    }

    @Test
    void deveRejeitarModeloInvalido() throws Exception {
        mockMvc.perform(post("/api/admin/modelos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome": "", "descricao": "", "precoBase": -1}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campos").isArray());
    }

    @Test
    void deveDesativarModelo() throws Exception {
        mockMvc.perform(delete("/api/admin/modelos/5"))
                .andExpect(status().isNoContent());

        verify(modeloIconeService).desativar(5L);
    }
}
