package dev.y.works.iconescatolicosonline.service.pagamento;

import dev.y.works.iconescatolicosonline.exception.RegraNegocioException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

@Service
public class ArmazenamentoComprovanteService {
    private static final long TAMANHO_MAXIMO = 10L * 1024 * 1024;
    private static final Map<String, String> EXTENSOES = Map.of(
            "application/pdf", ".pdf",
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp");
    private final Path diretorio;

    public ArmazenamentoComprovanteService(
            @Value("${app.upload.comprovantes-diretorio}") String diretorio) {
        this.diretorio = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public ArquivoComprovante armazenar(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RegraNegocioException("O comprovante enviado está vazio.");
        }
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new RegraNegocioException("O comprovante deve possuir no máximo 10 MB.");
        }
        String tipo = arquivo.getContentType();
        String extensao = EXTENSOES.get(tipo);
        if (extensao == null) {
            throw new RegraNegocioException("Formato inválido. Envie PDF, JPG, PNG ou WEBP.");
        }
        validarAssinatura(arquivo, tipo);
        String nomeArmazenado = UUID.randomUUID() + extensao;
        Path destino = diretorio.resolve(nomeArmazenado).normalize();
        if (!destino.getParent().equals(diretorio)) {
            throw new RegraNegocioException("Nome de comprovante inválido.");
        }
        try {
            Files.createDirectories(diretorio);
            Files.copy(arquivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new RegraNegocioException("Não foi possível armazenar o comprovante.");
        }
        String original = normalizarNomeOriginal(arquivo.getOriginalFilename(), extensao);
        return new ArquivoComprovante(nomeArmazenado, original, tipo);
    }

    private String normalizarNomeOriginal(String nome, String extensao) {
        if (nome == null || nome.isBlank()) return "comprovante" + extensao;
        String normalizado = nome.replace('\\', '/');
        normalizado = normalizado.substring(normalizado.lastIndexOf('/') + 1)
                .replaceAll("[\\p{Cntrl}]", "_").trim();
        if (normalizado.isBlank()) return "comprovante" + extensao;
        return normalizado.length() > 255 ? normalizado.substring(0, 255) : normalizado;
    }

    public Resource carregar(String nomeArmazenado) {
        Path arquivo = diretorio.resolve(nomeArmazenado).normalize();
        if (!arquivo.getParent().equals(diretorio) || !Files.isRegularFile(arquivo)) {
            throw new RegraNegocioException("Arquivo de comprovante não encontrado.");
        }
        return new FileSystemResource(arquivo);
    }

    private void validarAssinatura(MultipartFile arquivo, String tipo) {
        try {
            byte[] bytes = arquivo.getInputStream().readNBytes(12);
            boolean valido = switch (tipo) {
                case "application/pdf" -> iniciaCom(bytes, 0x25, 0x50, 0x44, 0x46);
                case "image/jpeg" -> iniciaCom(bytes, 0xFF, 0xD8, 0xFF);
                case "image/png" -> iniciaCom(bytes, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
                case "image/webp" -> iniciaCom(bytes, 0x52, 0x49, 0x46, 0x46)
                        && bytes.length >= 12 && bytes[8] == 0x57 && bytes[9] == 0x45
                        && bytes[10] == 0x42 && bytes[11] == 0x50;
                default -> false;
            };
            if (!valido) throw new RegraNegocioException("O conteúdo do comprovante não corresponde ao formato informado.");
        } catch (IOException exception) {
            throw new RegraNegocioException("Não foi possível validar o comprovante.");
        }
    }

    private boolean iniciaCom(byte[] bytes, int... assinatura) {
        if (bytes.length < assinatura.length) return false;
        for (int i = 0; i < assinatura.length; i++) {
            if ((bytes[i] & 0xFF) != assinatura[i]) return false;
        }
        return true;
    }

    public record ArquivoComprovante(String nomeArmazenado, String nomeOriginal, String tipoConteudo) {}
}
