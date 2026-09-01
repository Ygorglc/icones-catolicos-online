package dev.y.works.iconescatolicosonline.service.pagamento;

import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArmazenamentoComprovanteServiceTests {
    @TempDir Path diretorio;

    @Test
    void deveArmazenarPdfComNomeAleatorio() throws Exception {
        var service = new ArmazenamentoComprovanteService(diretorio.toString());
        var arquivo = new MockMultipartFile("arquivo", "comprovante.pdf",
                "application/pdf", "%PDF-1.7 teste".getBytes());

        var salvo = service.armazenar(arquivo);

        assertThat(salvo.nomeOriginal()).isEqualTo("comprovante.pdf");
        assertThat(salvo.nomeArmazenado()).endsWith(".pdf");
        assertThat(Files.exists(diretorio.resolve(salvo.nomeArmazenado()))).isTrue();
    }

    @Test
    void deveRejeitarArquivoComConteudoDiferenteDoTipo() {
        var service = new ArmazenamentoComprovanteService(diretorio.toString());
        var arquivo = new MockMultipartFile("arquivo", "falso.pdf",
                "application/pdf", "conteudo invalido".getBytes());

        assertThatThrownBy(() -> service.armazenar(arquivo))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("não corresponde");
    }
}
