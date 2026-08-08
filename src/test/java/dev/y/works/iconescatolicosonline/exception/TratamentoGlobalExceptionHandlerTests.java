package dev.y.works.iconescatolicosonline.exception;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TratamentoErroTestController.class)
@Import(TratamentoGlobalExceptionHandler.class)
@AutoConfigureMockMvc(addFilters = false)
class TratamentoGlobalExceptionHandlerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveResponderNotFoundDeFormaPadronizada() throws Exception {
        mockMvc.perform(get("/teste/erros/nao-encontrado"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.erro").value("Recurso não encontrado"))
                .andExpect(jsonPath("$.mensagem").value("Modelo de ícone não encontrado."))
                .andExpect(jsonPath("$.caminho").value("/teste/erros/nao-encontrado"))
                .andExpect(jsonPath("$.campos").isEmpty());
    }

    @Test
    void deveInformarOsCamposInvalidos() throws Exception {
        mockMvc.perform(post("/teste/erros/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.erro").value("Dados inválidos"))
                .andExpect(jsonPath("$.campos[0].campo").value("nome"))
                .andExpect(jsonPath("$.campos[0].mensagem").isNotEmpty());
    }

    @Test
    void deveTratarJsonMalformado() throws Exception {
        mockMvc.perform(post("/teste/erros/validacao")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nome\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.erro").value("Requisição inválida"))
                .andExpect(jsonPath("$.mensagem").value(
                        "O corpo da requisição está ausente ou possui formato inválido."));
    }
}
