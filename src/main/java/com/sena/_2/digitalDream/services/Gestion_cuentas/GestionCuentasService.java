package com.sena._2.digitalDream.services.Gestion_cuentas;

import com.sena._2.digitalDream.dtos.UsuarioDTO;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GestionCuentasService {

    private final UsuarioRepository usuarioRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${upload.dir.usuarios}")
    private String uploadDir;

    @Transactional
    public Usuario actualizarUsuario(int id, UsuarioDTO usuarioDTO, MultipartFile fotoUsuario) throws IOException {
        Optional<Usuario> usuarioOpt = usuarioRepository.findById(id);
        if (usuarioOpt.isPresent()) {
            Usuario usuarioExistente = usuarioOpt.get();

            usuarioExistente.setPrim_nombre(usuarioDTO.getPrim_nombre());
            usuarioExistente.setSeg_nombre(usuarioDTO.getSeg_nombre());
            usuarioExistente.setPrim_apellido(usuarioDTO.getPrim_apellido());
            usuarioExistente.setSeg_apellido(usuarioDTO.getSeg_apellido());


            if (usuarioDTO.getEmail() != null && !usuarioDTO.getEmail().isEmpty()) {
                usuarioExistente.setEmail(usuarioDTO.getEmail());
            } else {
                throw new IllegalArgumentException("El email no puede ser nulo o vacío");
            }

            if (usuarioDTO.getPassword() != null && !usuarioDTO.getPassword().isEmpty()) {
                usuarioExistente.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
            }

            if (fotoUsuario != null && !fotoUsuario.isEmpty()) {
                if (usuarioExistente.getFotoUsuario() != null) {
                    Path rutaAnterior = Path.of(uploadDir, usuarioExistente.getFotoUsuario());
                    Files.deleteIfExists(rutaAnterior);
                }


                String fileName = System.currentTimeMillis() + "_" + fotoUsuario.getOriginalFilename();
                Path path = Path.of(uploadDir, fileName);
                Files.copy(fotoUsuario.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
                usuarioExistente.setFotoUsuario(fileName);
            }

            Usuario usuarioGuardado = usuarioRepository.save(usuarioExistente);
            return usuarioGuardado;
        }
        return null;
    }

    public String eliminarCuenta(Integer usuarioId) {

        Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);

        if (usuario == null) {
            return "No se encontró el usuario con el ID proporcionado.";
        }


        String emailAdmin = "digitaldream402@gmail.com";
        String asunto = "Cuenta eliminada: " + usuario.getEmail();
        String contenido = "La cuenta del usuario con el email " + usuario.getEmail() + " ha sido eliminada.";

        emailService.enviarCorreo(emailAdmin, asunto, contenido);


        usuarioRepository.delete(usuario);

        return "Cuenta eliminada correctamente.";
    }
}
