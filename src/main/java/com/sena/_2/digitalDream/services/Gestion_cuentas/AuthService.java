package com.sena._2.digitalDream.services.Gestion_cuentas;

import com.sena._2.digitalDream.dtos.UsuarioDTO;
import com.sena._2.digitalDream.modelos.Rol;
import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.RolRepository;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    @Value("${upload.dir.usuarios}")
    private String uploadDir;

    @Transactional
    public String registrarUsuario(UsuarioDTO usuarioDTO) {

        if (usuarioRepository.findByEmail(usuarioDTO.getEmail()).isPresent()) {
            return "Error: El correo ya está registrado";
        }


        Usuario usuario = new Usuario();

        // Manejo de la foto de perfil
        if (usuarioDTO.getFotoUsuario() != null && !usuarioDTO.getFotoUsuario().isEmpty()) {

            String fileName = System.currentTimeMillis() + usuarioDTO.getFotoUsuario().getOriginalFilename();
            Path path = Path.of(uploadDir, fileName);  // Ruta completa a la carpeta de imágenes de usuarios

            try {

                Files.createDirectories(path.getParent());

                String extension = fileName.substring(fileName.lastIndexOf(".") + 1);

                if (!extension.equalsIgnoreCase("jpg") && !extension.equalsIgnoreCase("png")) {
                    return "Error: Solo se permiten archivos .jpg y .png";
                }


                Files.copy(usuarioDTO.getFotoUsuario().getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);


                usuario.setFotoUsuario(fileName);
            } catch (IOException e) {
                return "Error al subir la imagen.";
            }
        }


        Rol rol = rolRepository.findById(2)
                .orElseThrow(() -> new RuntimeException("Error: Rol CLIENTE no encontrado"));


        usuario.setPrim_nombre(usuarioDTO.getPrim_nombre());
        usuario.setSeg_nombre(usuarioDTO.getSeg_nombre());
        usuario.setPrim_apellido(usuarioDTO.getPrim_apellido());
        usuario.setSeg_apellido(usuarioDTO.getSeg_apellido());
        usuario.setEmail(usuarioDTO.getEmail());
        usuario.setPassword(passwordEncoder.encode(usuarioDTO.getPassword()));
        usuario.setRol(rol);


        usuarioRepository.save(usuario);

        return "¡Usuario registrado con éxito!";
    }
}

