package com.sena._2.digitalDream.controller;

import com.sena._2.digitalDream.dtos.UsuarioDTO;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.services.Gestion_cuentas.GestionCuentasService;
import com.sena._2.digitalDream.services.Gestion_cuentas.UsuarioDetalles;
import com.sena._2.digitalDream.services.Usuarios.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequiredArgsConstructor
@RequestMapping("/perfil")
public class PerfilController {

    private final GestionCuentasService gestionCuentasService;
    private final UsuarioService usuarioService;

    // Mostrar perfil del usuario
    @GetMapping({"/", "/index"})
    public String verPerfil(Model model, @AuthenticationPrincipal UsuarioDetalles usuarioDetalles) {
        if (usuarioDetalles == null) {
            return "redirect:/login";
        }

        Usuario usuario = usuarioDetalles.getUsuario();
        model.addAttribute("usuario", usuario);

        return "perfil/index";
    }


    @PostMapping("/editar")
    public String editarPerfil(@ModelAttribute("usuario") Usuario usuario,
                               @RequestParam("prim_nombre") String primNombre,
                               @RequestParam("seg_nombre") String segNombre,
                               @RequestParam("prim_apellido") String primApellido,
                               @RequestParam("seg_apellido") String segApellido,
                               @RequestParam("email") String email,
                               @RequestParam("password") String password,
                               @RequestParam(value = "foto", required = false) MultipartFile foto,
                               Model model, Authentication authentication,
                               HttpServletRequest request) throws IOException {

        if (authentication == null || !authentication.isAuthenticated()) {
            model.addAttribute("mensaje", "No estás autenticado. Por favor inicia sesión.");
            return "redirect:/login";
        }

        UsuarioDTO usuarioDTO = new UsuarioDTO();
        usuarioDTO.setPrim_nombre(primNombre);
        usuarioDTO.setSeg_nombre(segNombre);
        usuarioDTO.setPrim_apellido(primApellido);
        usuarioDTO.setSeg_apellido(segApellido);
        usuarioDTO.setEmail(email);
        usuarioDTO.setPassword(password);

        try {
            Usuario usuarioActualizado = gestionCuentasService.actualizarUsuario(usuario.getIdUsuario(), usuarioDTO, foto);

            if (!usuario.getEmail().equals(email)) {
                ((UsuarioDetalles) authentication.getPrincipal()).getUsuario().setEmail(email);
            }

            model.addAttribute("usuario", usuarioActualizado);
            model.addAttribute("mensaje", "Perfil actualizado exitosamente.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("mensaje", e.getMessage());
        } catch (IOException e) {
            model.addAttribute("mensaje", "Error al guardar la foto de perfil.");
        }

        return "redirect:/home";
    }



    // Eliminar cuenta del usuario
    @PostMapping("/eliminar")
    public String eliminarCuenta(@RequestParam("usuarioId") Integer usuarioId, Model model) {
        // Llamar al servicio para eliminar la cuenta
        String mensaje = gestionCuentasService.eliminarCuenta(usuarioId);
        model.addAttribute("mensaje", mensaje);

        return "redirect:/login";
    }

}

