package com.sena._2.digitalDream.controller;

import com.itextpdf.text.DocumentException;
import com.sena._2.digitalDream.dtos.UsuarioDTO;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.RolRepository;
import com.sena._2.digitalDream.services.Usuarios.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;
    @Autowired
    private RolRepository rolRepository;

    // Obtener todos los usuarios
    @GetMapping({"/","/listar"})
    public String listarUsuarios(Model model) {
        System.out.println("Entrando a listarUsuarios...");
        List<Usuario> usuarios = usuarioService.obtenerTodosLosUsuarios();
        model.addAttribute("usuarios", usuarios);
        return "usuarios/listar";
    }

    // Ver detalles de un usuario
    @GetMapping("/ver/{id}")
    public String verUsuario(@PathVariable("id") int id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id).orElse(null);
        if (usuario == null) {
            return "redirect:/usuarios/listar";
        }
        model.addAttribute("usuario", usuario);
        return "usuarios/ver";
    }

    // Crear un nuevo usuario (vista de formulario)
    @GetMapping("/crear")
    public String crearUsuarioForm(Model model) {
        model.addAttribute("usuarioDTO", new UsuarioDTO());
        model.addAttribute("roles", rolRepository.findAll());
        return "usuarios/crear";
    }

    // Crear un nuevo usuario (guardar)
    @PostMapping("/guardar")
    public String crearUsuario(@ModelAttribute UsuarioDTO usuarioDTO,
                               @RequestParam(value = "foto_usuario", required = false) MultipartFile fotoUsuario) throws IOException {
        usuarioService.crearUsuario(usuarioDTO);
        return "redirect:/usuarios/listar";
    }

    // Modificar usuario (vista de formulario)
    @GetMapping("/modificar/{id}")
    public String modificarUsuarioForm(@PathVariable("id") int id, Model model) {
        Usuario usuario = usuarioService.obtenerUsuarioPorId(id).orElse(null);
        if (usuario == null) {
            return "redirect:/usuarios/listar";
        }
        model.addAttribute("usuario", usuario);
        return "usuarios/modificar";
    }

    @PostMapping("/modificar/{id}")
    public String modificarUsuario(@PathVariable("id") int id,
                                   @ModelAttribute UsuarioDTO usuarioDTO,
                                   @RequestParam(value = "foto_usuario", required = false) MultipartFile fotoUsuario) throws IOException {

        usuarioService.actualizarUsuario(id, usuarioDTO, fotoUsuario); // <-- Ahora se pasa la imagen correctamente
        return "redirect:/usuarios/listar";
    }

    // Eliminar usuario
    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable("id") int id) {
        usuarioService.eliminarUsuario(id);
        return "redirect:/usuarios/listar";
    }

    // Exportar lista de usuarios a Excel
    @GetMapping("/exportarExcel")
    public void exportarExcel(HttpServletResponse response) throws IOException {
        usuarioService.exportarExcel(response);
    }

    // Exportar lista de usuarios a PDF
    @GetMapping("/exportarPdf")
    public void exportarPdf(HttpServletResponse response) throws IOException, DocumentException {
        usuarioService.exportarPdf(response);
    }
}
