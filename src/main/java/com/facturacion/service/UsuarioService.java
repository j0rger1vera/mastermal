package com.facturacion.service;

import com.facturacion.dto.LoginResponseDTO;
import com.facturacion.entity.Usuario;
import com.facturacion.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {
    private final Logger LOGGER = LoggerFactory.getLogger(UsuarioService.class);

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Optional<Object[]> buscarPorUsuarioYContrasenia(String usuario, String password) {
        return this.usuarioRepository.findByUsernameAndPasswords(usuario, password);
    }

    public Optional<Integer> obtenerIntentosFallidos(String username) {
        return this.usuarioRepository.obtenerIntentosFallidos(username);
    }
    public boolean estaBloqueado(String username) {
        LOGGER.info("Verificando si el usuario {} esta bloqueado", username);
        Optional<Byte> bloqueado = this.usuarioRepository.estaBloqueado(username);
        LOGGER.info("Verificando si el usuario {} esta bloqueado", bloqueado);
        return bloqueado.orElse((byte) 0) == 1; // Si no se encuentra el usuario, se asume que no está bloqueado
    }

    public void incrementarIntentosFallidos(String username) {
        this.usuarioRepository.incrementarIntentosFallidos(username);
    }

    public void bloquearUsuario(String username) {
        this.usuarioRepository.bloquearUsuario(username);
    }

    public Optional<Usuario> autenticar(String username, String password) {
        return usuarioRepository.findUsuarioByUsernameAndPassword(username, password);
    }

    public LoginResponseDTO construirSesion(Usuario usuario) {
        String rol = usuario.getRol() != null ? usuario.getRol() : "CONSULTA";
        return new LoginResponseDTO(
                usuario.getUsername(),
                rol,
                obtenerPermisosPorRol(rol)
        );
    }

    public List<String> obtenerPermisosPorRol(String rol) {
        return switch (rol) {
            case "ADMIN" -> List.of(
                    "CLIENTE_VER",
                    "CLIENTE_CREAR",
                    "CLIENTE_EDITAR",
                    "PRODUCTO_VER",
                    "PRODUCTO_CREAR",
                    "PRODUCTO_EDITAR",
                    "FACTURA_VER",
                    "FACTURA_CREAR",
                    "FACTURA_EDITAR",
                    "FACTURA_ELIMINAR",
                    "ABONO_REGISTRAR",
                    "ABONO_VER_HISTORIAL",
                    "ABONO_REVERSAR",
                    "SALDOS_VER",
                    "POR_COBRAR_VER"
            );

            case "CAJERO" -> List.of(
                    "FACTURA_VER",
                    "ABONO_REGISTRAR",
                    "ABONO_VER_HISTORIAL",
                    "SALDOS_VER",
                    "POR_COBRAR_VER"
            );

            case "VENDEDOR" -> List.of(
                    "CLIENTE_VER",
                    "CLIENTE_CREAR",
                    "PRODUCTO_VER",
                    "FACTURA_VER",
                    "FACTURA_CREAR"
            );

            default -> List.of(
                    "FACTURA_VER",
                    "SALDOS_VER",
                    "POR_COBRAR_VER"
            );
        };
    }

}
