package com.facturacion.config;

import com.facturacion.entity.Usuario;
import com.facturacion.repository.UsuarioRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer {

    private final UsuarioRepository usuarioRepository;

    public DataInitializer(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @PostConstruct
    public void init() {
        if (!usuarioRepository.existsByUsername("admin")) {
            Usuario usuario = new Usuario();
            usuario.setUsername("admin");
            usuario.setPassword("admin");
            usuario.setIntentosFallidos(0);
            usuario.setBloqueado(false);

            usuarioRepository.save(usuario);
        }
    }

}
