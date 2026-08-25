package dev.y.works.iconescatolicosonline.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.MailException;
import org.springframework.stereotype.Service;
import org.slf4j.Logger; import org.slf4j.LoggerFactory;
import java.nio.charset.StandardCharsets;

@Service
public class EmailRecuperacaoSenhaService {
    private static final Logger LOGGER=LoggerFactory.getLogger(EmailRecuperacaoSenhaService.class);
    private static final String NOME_REMETENTE="Oficina de Ícones São José";
    private final JavaMailSender mailSender; private final boolean enabled; private final String remetente; private final String frontendUrl;
    public EmailRecuperacaoSenhaService(JavaMailSender mailSender,
            @Value("${app.email.enabled:false}") boolean enabled,
            @Value("${app.email.remetente}") String remetente,
            @Value("${app.frontend.url}") String frontendUrl) {
        this.mailSender=mailSender;this.enabled=enabled;this.remetente=remetente;this.frontendUrl=frontendUrl;
    }
    public boolean enviarRecuperacao(String destinatario, String nome, String token, long minutos) {
        if (!enabled) return false;
        try {
            MimeMessage mensagem=mailSender.createMimeMessage(); MimeMessageHelper helper=new MimeMessageHelper(mensagem,false,StandardCharsets.UTF_8.name());
            helper.setFrom(remetente,NOME_REMETENTE);helper.setTo(destinatario);helper.setSubject("Recuperação de senha — Oficina de Ícones São José");
            String link=frontendUrl+"/redefinir-senha?token="+token;
            helper.setText("<div style='font-family:Arial,sans-serif;color:#39231d'><h2>Recuperação de senha</h2><p>Olá, "+escapar(nome)+".</p><p>Recebemos uma solicitação para redefinir sua senha.</p><p><a style='display:inline-block;padding:12px 18px;background:#a63225;color:white;text-decoration:none;border-radius:6px' href='"+link+"'>Redefinir minha senha</a></p><p>Este link expira em "+minutos+" minutos e só pode ser utilizado uma vez.</p><p>Se você não solicitou a alteração, ignore esta mensagem.</p><p>Oficina de Ícones São José</p></div>",true);
            mailSender.send(mensagem); return true;
        } catch (MessagingException | java.io.UnsupportedEncodingException | MailException exception) { LOGGER.error("Falha ao enviar e-mail de recuperação pelo Gmail SMTP.",exception); return false; }
    }
    private String escapar(String valor){return valor.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");}
}
