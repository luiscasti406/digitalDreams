package com.sena._2.digitalDream.services.Gestion_cuentas;

import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RecuperacionService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public String enviarCorreoRecuperacion(String email) {
        Optional<Usuario> usuarioOpt = usuarioRepository.findByEmail(email);
        if (usuarioOpt.isEmpty()) {
            return "Correo no encontrado";
        }

        Usuario usuario = usuarioOpt.get();
        String token = UUID.randomUUID().toString();
        usuario.setTokenRecuperacion(token);
        usuarioRepository.save(usuario);

        String enlace = "http://localhost:8080/recuperacion/restablecer?token=" + token;

        String mensaje = "<div style='font-family: Arial, sans-serif; padding: 20px; background-color: #f4f4f4; text-align: center;'>"
                + "<h2 style='color: #333;'>Recuperación de Contraseña</h2>"
                + "<p style='font-size: 16px; color: #555;'>Hemos recibido una solicitud para restablecer tu contraseña.</p>"
                + "<p style='font-size: 16px; color: #555;'>Si no hiciste esta solicitud, ignora este mensaje.</p>"
                + "<a href='" + enlace + "' style='display: inline-block; padding: 10px 20px; font-size: 18px; color: white; background-color: #29b5f3; text-decoration: none; border-radius: 5px;'>Restablecer Contraseña</a>"
                + "<p style='margin-top: 20px; font-size: 14px; color: #777;'>Este enlace expirará en 24 horas por razones de seguridad.</p>"
                + "<p style='font-size: 14px; color: #777;'>Si tienes algún problema, contáctanos.</p>"
                + "</div>";

        emailService.enviarCorreo(email, "Recuperación de contraseña", mensaje);

        return "Se ha enviado un correo con las instrucciones para recuperar tu contraseña.";
    }

    public String restablecerPassword(String token, String nuevaPassword) {

        Optional<Usuario> usuarioOpt = usuarioRepository.findByTokenRecuperacion(token.trim());
        if (usuarioOpt.isEmpty()) {
            return "Token inválido o expirado";
        }

        Usuario usuario = usuarioOpt.get();
        usuario.setPassword(passwordEncoder.encode(nuevaPassword));
        usuario.setTokenRecuperacion(null);
        usuarioRepository.save(usuario);

        return "Contraseña actualizada con éxito";
    }
}

