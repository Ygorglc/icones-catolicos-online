package dev.y.works.iconescatolicosonline.controller;

import dev.y.works.iconescatolicosonline.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SistemaStatusController.class)
@Import(SecurityConfig.class)
class SistemaStatusControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void deveInformarQueSistemaEstaEmAtividade() throws Exception {
        mockMvc.perform(get("/api/status"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"))
                .andExpect(content().string("Sistemas em atividade"));
    }
}
