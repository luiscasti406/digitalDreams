package com.sena._2.digitalDream.controller;

import com.sena._2.digitalDream.dtos.ProductoDTO;
import com.sena._2.digitalDream.dtos.UsuarioDTO;
import com.sena._2.digitalDream.modelos.Producto;
import com.sena._2.digitalDream.modelos.Recibo;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.ProductoRepository;
import com.sena._2.digitalDream.services.Gestion_cuentas.AuthService;
import com.sena._2.digitalDream.services.Productos.ProductoService;
import com.sena._2.digitalDream.services.Resivos.ReciboService;
import com.sena._2.digitalDream.services.Usuarios.UsuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final AuthService authService;
    private final ProductoService productoService;
    private final UsuarioService usuarioService;
    private final ReciboService reciboService;
    private final ProductoRepository productoRepository;

    @GetMapping("/home")
    public String listaProductos(Model model, HttpServletRequest request, RedirectAttributes redirectAttributes) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        boolean isAuthenticated = authentication.isAuthenticated() && !email.equals("anonymousUser");

        if (isAuthenticated) {
            Usuario usuarioActual = usuarioService.obtenerUsuarioPorEmail(email).orElse(null);
            if (usuarioActual != null && usuarioActual.getFotoUsuario() != null && !usuarioActual.getFotoUsuario().isEmpty()) {
                model.addAttribute("fotoUsuario", "/img/usuarios/" + usuarioActual.getFotoUsuario());
                model.addAttribute("usuarioId", usuarioActual.getIdUsuario());
            } else {
                model.addAttribute("fotoUsuario", "/img/usuarios/usuario_default.jpg");
            }
        } else {
            model.addAttribute("fotoUsuario", "/img/usuarios/usuario_default.jpg");
        }

        List<ProductoDTO> productosDisponibles = productoService.obtenerProductosConMarcaYFoto()
                .stream()
                .filter(p -> p.getCantidad() > 0)
                .toList();

        model.addAttribute("productos", productosDisponibles);
        model.addAttribute("isAuthenticated", isAuthenticated);

        return "home";
    }


    @PostMapping("/comprar")
    public String comprar(
            @RequestParam List<Integer> productosSeleccionados,
            @RequestParam List<Integer> cantidades,
            @RequestParam int tipoPago,
            @RequestParam String numeroTarjeta,
            RedirectAttributes redirectAttributes) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        if (!authentication.isAuthenticated() || email.equals("anonymousUser")) {
            redirectAttributes.addFlashAttribute("error", "Debes iniciar sesión para comprar.");
            return "redirect:/login";
        }

        if (productosSeleccionados == null || productosSeleccionados.isEmpty() || productosSeleccionados.contains(null)) {
            redirectAttributes.addFlashAttribute("error", "No se han seleccionado productos válidos.");
            return "redirect:/home";
        }

        Usuario usuario = usuarioService.obtenerUsuarioPorEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        for (Integer idProducto : productosSeleccionados) {
            if (!productoRepository.existsById(idProducto)) {
                redirectAttributes.addFlashAttribute("error", "Uno o más productos seleccionados no existen.");
                return "redirect:/home";
            }
        }

        Recibo recibo = reciboService.confirmarCompra(usuario.getIdUsuario(), productosSeleccionados, cantidades, tipoPago, numeroTarjeta);

        redirectAttributes.addFlashAttribute("success", "Compra realizada con éxito. Recibo #" + recibo.getIdRecibo());
        return "redirect:/home";
    }


    @GetMapping("/login")
    public String mostrarLogin() {
        return "login";
    }

    @PostMapping("/procesar_login")
    public String login(@RequestParam String email, @RequestParam String password, Model model, HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            session = request.getSession(true);
            session.setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());

            return "redirect:/home";
        } catch (BadCredentialsException e) {
            model.addAttribute("message", "Correo o contraseña incorrectos");
            return "login";
        }
    }

    @GetMapping("/registrarse")
    public String mostrarFormularioRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "registrarse";
    }

    @PostMapping("/registrarse")
    public String registrarUsuario(@ModelAttribute UsuarioDTO usuarioDTO, Model model) {
        usuarioDTO.setId_rol(2);
        String mensaje = authService.registrarUsuario(usuarioDTO);
        model.addAttribute("message", mensaje);
        return "redirect:/home";
    }

    @PostMapping("/logout")
    public String logout(Model model) {
        SecurityContextHolder.clearContext();
        model.addAttribute("message", "Cierre de sesión exitoso");
        return "login";
    }
}


