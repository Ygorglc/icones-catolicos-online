package dev.y.works.iconescatolicosonline.exception;

import dev.y.works.iconescatolicosonline.dto.erro.ErroCampo;
import dev.y.works.iconescatolicosonline.dto.erro.ErroResposta;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class TratamentoGlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(TratamentoGlobalExceptionHandler.class);

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    public ResponseEntity<ErroResposta> tratarRecursoNaoEncontrado(
            RecursoNaoEncontradoException exception,
            HttpServletRequest request
    ) {
        return resposta(
                HttpStatus.NOT_FOUND,
                "Recurso não encontrado",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(ConflitoException.class)
    public ResponseEntity<ErroResposta> tratarConflito(
            ConflitoException exception,
            HttpServletRequest request
    ) {
        return resposta(
                HttpStatus.CONFLICT,
                "Conflito",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(RegraNegocioException.class)
    public ResponseEntity<ErroResposta> tratarRegraNegocio(
            RegraNegocioException exception,
            HttpServletRequest request
    ) {
        return resposta(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "Regra de negócio violada",
                exception.getMessage(),
                request,
                List.of()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErroResposta> tratarValidacaoDeCampos(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {
        List<ErroCampo> campos = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(erro -> new ErroCampo(erro.getField(), erro.getDefaultMessage()))
                .sorted(Comparator.comparing(ErroCampo::campo))
                .toList();

        return resposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                "Um ou mais campos possuem valores inválidos.",
                request,
                campos
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErroResposta> tratarViolacaoDeRestricao(
            ConstraintViolationException exception,
            HttpServletRequest request
    ) {
        List<ErroCampo> campos = exception.getConstraintViolations()
                .stream()
                .map(violacao -> new ErroCampo(
                        violacao.getPropertyPath().toString(),
                        violacao.getMessage()
                ))
                .sorted(Comparator.comparing(ErroCampo::campo))
                .toList();

        return resposta(
                HttpStatus.BAD_REQUEST,
                "Dados inválidos",
                "Um ou mais parâmetros possuem valores inválidos.",
                request,
                campos
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErroResposta> tratarMensagemIlegivel(HttpServletRequest request) {
        return resposta(
                HttpStatus.BAD_REQUEST,
                "Requisição inválida",
                "O corpo da requisição está ausente ou possui formato inválido.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErroResposta> tratarIntegridadeDoBanco(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {
        LOGGER.warn("Violação de integridade ao acessar {}", request.getRequestURI(), exception);
        return resposta(
                HttpStatus.CONFLICT,
                "Conflito de dados",
                "A operação viola uma restrição de integridade dos dados.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErroResposta> tratarFalhaDeAutenticacao(
            AuthenticationException exception,
            HttpServletRequest request
    ) {
        return resposta(
                HttpStatus.UNAUTHORIZED,
                "Não autorizado",
                "E-mail ou senha inválidos.",
                request,
                List.of()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErroResposta> tratarErroInesperado(
            Exception exception,
            HttpServletRequest request
    ) {
        LOGGER.error("Erro inesperado ao acessar {}", request.getRequestURI(), exception);
        return resposta(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Erro interno",
                "Não foi possível concluir a operação.",
                request,
                List.of()
        );
    }

    private ResponseEntity<ErroResposta> resposta(
            HttpStatus status,
            String erro,
            String mensagem,
            HttpServletRequest request,
            List<ErroCampo> campos
    ) {
        ErroResposta corpo = new ErroResposta(
                Instant.now(),
                status.value(),
                erro,
                mensagem,
                request.getRequestURI(),
                campos
        );
        return ResponseEntity.status(status).body(corpo);
    }
}
