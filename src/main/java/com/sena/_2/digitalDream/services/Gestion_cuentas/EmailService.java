package com.sena._2.digitalDream.services.Gestion_cuentas;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    public void enviarCorreo(String destinatario, String asunto, String contenido) {
        try {
            System.out.println("Intentando enviar correo a: " + destinatario);
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(contenido, true);
            mailSender.send(mensaje);
            System.out.println("Correo enviado a: " + destinatario);
        } catch (MessagingException e) {
            System.err.println("Error al enviar el correo: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
