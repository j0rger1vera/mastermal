package com.facturacion.dto;

import java.util.List;

public class LoginResponseDTO {

    private String username;
    private String rol;
    private List<String> permisos;

    public LoginResponseDTO(String username, String rol, List<String> permisos) {
        this.username = username;
        this.rol = rol;
        this.permisos = permisos;
    }

    public String getUsername() {
        return username;
    }

    public String getRol() {
        return rol;
    }

    public List<String> getPermisos() {
        return permisos;
    }
}