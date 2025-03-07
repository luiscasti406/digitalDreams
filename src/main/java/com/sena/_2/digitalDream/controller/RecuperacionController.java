package com.sena._2.digitalDream.controller;

import com.sena._2.digitalDream.services.Gestion_cuentas.RecuperacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/recuperacion")
@RequiredArgsConstructor
public class RecuperacionController {

    private final RecuperacionService recuperacionService;

    @PostMapping("/solicitar")
    public ResponseEntity<String> solicitarRecuperacion(@RequestParam String email) {
        String mensaje = recuperacionService.enviarCorreoRecuperacion(email);
        return ResponseEntity.ok(mensaje);
    }

    @PostMapping("/restablecer")
    public String restablecerPassword(@RequestParam String token, @RequestParam String nuevaPassword, Model model) {
        String mensaje = recuperacionService.restablecerPassword(token, nuevaPassword);

        if (mensaje.equals("Contraseña actualizada con éxito")) {
            return "redirect:/login";
        }

        model.addAttribute("message", mensaje);
        return "recuperacion/restablecer";
    }


    @GetMapping
    public String mostrarFormularioRecuperacion() {
        return "recuperacion/index";
    }

    @GetMapping("/restablecer")
    public String mostrarFormularioRestablecimiento(@RequestParam String token, Model model) {
        model.addAttribute("token", token);
        return "recuperacion/restablecer";
    }
}

