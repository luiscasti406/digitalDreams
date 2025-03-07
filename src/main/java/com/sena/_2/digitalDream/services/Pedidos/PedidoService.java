package com.sena._2.digitalDream.services.Pedidos;

import com.sena._2.digitalDream.dtos.PedidoDTO;
import com.sena._2.digitalDream.modelos.Marca;
import com.sena._2.digitalDream.modelos.Pedido;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.MarcaRepository;
import com.sena._2.digitalDream.repository.PedidoRepository;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final JavaMailSender mailSender;
    private final MarcaRepository marcaRepository;
    private final UsuarioRepository usuarioRepository;

    public void crearPedido(PedidoDTO pedidoDTO) {
        System.out.println("🟢 Recibiendo pedido: " + pedidoDTO);

        // Validar que la fecha de llegada no sea anterior a la de envío
        if (pedidoDTO.getFechaLlegada().before(pedidoDTO.getFechaEnvio())) {
            throw new IllegalArgumentException("La fecha de llegada no puede ser anterior a la fecha de envío.");
        }

        Pedido pedido = new Pedido();
        pedido.setNomPedido(pedidoDTO.getNomPedido());
        pedido.setFechaEnvio(pedidoDTO.getFechaEnvio());
        pedido.setFechaLlegada(pedidoDTO.getFechaLlegada());

        Marca marca = marcaRepository.findById(pedidoDTO.getIdMarca())
                .orElseThrow(() -> new RuntimeException("Marca no encontrada"));
        pedido.setMarca(marca);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Usuario usuario = usuarioRepository.findByEmail(username)  // Asumiendo que tienes un método que busca por email o username
                .orElseThrow(() -> new RuntimeException("Usuario autenticado no encontrado"));

        pedido.setUsuario(usuario);

        System.out.println("🟢 Pedido a guardar: " + pedido);
        pedidoRepository.save(pedido);
        System.out.println("🔵 Pedido guardado en BD: " + pedido);
    }



    public Pedido modificarPedido(int id, Pedido pedidoDetalles) {
        Optional<Pedido> optionalPedido = pedidoRepository.findById(id);
        if (optionalPedido.isPresent()) {
            Pedido pedido = optionalPedido.get();

            if (pedidoDetalles.getFechaLlegada() != null) {
                pedido.setFechaLlegada(pedidoDetalles.getFechaLlegada());
            }
            if (pedidoDetalles.getFechaEnvio() != null) {
                pedido.setFechaEnvio(pedidoDetalles.getFechaEnvio());
            }
            if (pedidoDetalles.getNomPedido() != null && !pedidoDetalles.getNomPedido().trim().isEmpty()) {
                pedido.setNomPedido(pedidoDetalles.getNomPedido());
            }
            if (pedidoDetalles.getMarca() != null && pedidoDetalles.getMarca().getIdMarca() != null) {
                pedido.setMarca(pedidoDetalles.getMarca());
            }

            pedidoRepository.save(pedido);
            return pedido;
        }
        return null;
    }

    public boolean cancelarPedido(int id) {
        if (pedidoRepository.existsById(id)) {
            pedidoRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public List<Pedido> obtenerTodosLosPedidos() {
        return pedidoRepository.findAllWithDetails();
    }

    // Consultar un pedido por ID
    public Pedido obtenerPedidoPorId(int id) {
        return pedidoRepository.findById(id).orElse(null);
    }

    // Notificar entrega por correo
    public void enviarNotificacionPedido(String destinatario, String nombreUsuario, int idPedido, String fechaLlegada, String linkSeguimiento) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            helper.setTo(destinatario);
            helper.setSubject("📦 ¡Tu pedido está en camino!");

            String contenidoHtml = generarHtmlNotificacion(nombreUsuario, idPedido, fechaLlegada, linkSeguimiento, "📦 ¡Tu pedido está en camino!", "Tu pedido con el número");

            helper.setText(contenidoHtml, true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Scheduled(cron = "0 0 8 * * *")
    public void notificarPedidosEntregados() {
        List<Pedido> pedidos = pedidoRepository.findAll();
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd").format(new Date());

        for (Pedido pedido : pedidos) {
            String fechaLlegada = new SimpleDateFormat("yyyy-MM-dd").format(pedido.getFechaLlegada());
            if (fechaHoy.equals(fechaLlegada)) {
                // Obtener el correo del usuario que hizo el pedido
                String correoUsuario = pedido.getUsuario().getEmail();

                // Enviar notificación
                enviarNotificacionEntrega(correoUsuario, pedido.getNomPedido(), pedido.getIdPedido());
            }
        }
    }

    public void enviarNotificacionEntrega(String destinatario, String nombreUsuario, int idPedido) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true);

            helper.setTo(destinatario);
            helper.setSubject("🎉 ¡Tu pedido ha sido entregado!");

            String contenidoHtml = generarHtmlNotificacion(nombreUsuario, idPedido, "", "", "🎉 ¡Tu pedido ha sido entregado!", "Tu pedido con el número");

            helper.setText(contenidoHtml, true);
            mailSender.send(mensaje);
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    private String generarHtmlNotificacion(String nombreUsuario, int idPedido, String fecha, String link, String titulo, String mensaje) {
        return "<!DOCTYPE html>" +
                "<html><head><meta charset='UTF-8'>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; background-color: #f4f4f4; margin: 0; padding: 0; }" +
                ".container { max-width: 600px; margin: 20px auto; background: #ffffff; border-radius: 10px; overflow: hidden; box-shadow: 0px 4px 8px rgba(0, 0, 0, 0.2); }" +
                ".header { background: linear-gradient(90deg, #4a00e0, #1e90ff); padding: 20px; text-align: center; color: white; font-size: 22px; font-weight: bold; }" +
                ".content { padding: 20px; text-align: center; color: #333; }" +
                ".content p { font-size: 16px; line-height: 1.5; }" +
                ".button { display: inline-block; padding: 12px 20px; background-color: #1e90ff; color: white; text-decoration: none; border-radius: 5px; font-size: 16px; font-weight: bold; margin-top: 10px; }" +
                ".footer { background-color: #eee; padding: 10px; text-align: center; font-size: 14px; color: #666; }" +
                "</style></head><body>" +
                "<div class='container'>" +
                "<div class='header'>" + titulo + "</div>" +
                "<div class='content'>" +
                "<p>Hola <strong>" + nombreUsuario + "</strong>,</p>" +
                "<p>" + mensaje + " <strong>" + idPedido + "</strong> ha sido entregado.</p>" +
                (fecha.isEmpty() ? "" : "<p>Fecha de entrega: <strong>" + fecha + "</strong></p>") +
                (link.isEmpty() ? "" : "<a href='" + link + "' class='button'>Seguir mi pedido</a>") +
                "</div>" +
                "<div class='footer'>&copy; 2025 DigitalDream - Todos los derechos reservados</div>" +
                "</div></body></html>";
    }
}


