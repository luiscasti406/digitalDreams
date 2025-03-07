package com.sena._2.digitalDream.controller;

import com.sena._2.digitalDream.modelos.Recibo;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.services.Resivos.ReciboService;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/recibos")
@RequiredArgsConstructor
public class ReciboController {

    private final ReciboService reciboService;
    private final UsuarioRepository usuarioRepository;

    //Consultar compras pasadas del usuario autenticado
    @GetMapping({"/", "/index"})
    public String obtenerCompras(Model model) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        String email = userDetails.getUsername();

        // Obtener el usuario desde la base de datos
        Optional<Usuario> usuario = usuarioRepository.findByEmail(email);
        if (usuario.isEmpty()) {
            return "redirect:/login";
        }

        // Consultar las compras pasadas del usuario autenticado
        List<Recibo> compras = reciboService.consultarComprasPasadas(usuario.get().getIdUsuario());
        if (compras == null) {
            compras = new ArrayList<>();
        }

        model.addAttribute("compras", compras);
        return "recibos/index";
    }

    //Cancelar compra
    @PostMapping("/cancelar/{idRecibo}")
    public String cancelarCompra(@PathVariable int idRecibo, Model model) {
        reciboService.cancelarCompra(idRecibo);
        model.addAttribute("success", "Compra cancelada exitosamente");
        return "redirect:/recibos/";
    }

    // 🔹 Descargar recibo en PDF
    @GetMapping("/pdf/{idRecibo}")
    public ResponseEntity<byte[]> descargarReciboPDF(@PathVariable int idRecibo) {
        byte[] pdfBytes = reciboService.generarReciboPDF(idRecibo);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=recibo_" + idRecibo + ".pdf")
                .body(pdfBytes);
    }
}


