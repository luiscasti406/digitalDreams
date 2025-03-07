package com.sena._2.digitalDream.services.Gestion_cuentas;

import com.sena._2.digitalDream.modelos.Usuario;
import com.sena._2.digitalDream.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        if (usuario.getRol() == null) {
            throw new UsernameNotFoundException("El usuario no tiene un rol asignado");
        }

        String roleName = "ROLE_" + usuario.getRol().getTipoRol().toUpperCase();

        return new UsuarioDetalles(usuario);
    }
}

