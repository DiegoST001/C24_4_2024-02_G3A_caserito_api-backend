package com.tecsup.caserito_api.paq_web.paq_dto;

import jakarta.validation.constraints.Pattern;

public record UpdateUserRequest(
        String username,
        String password,
        @Pattern(
                regexp = "^[a-zA-Z0-9._%+-]+@tecsup\\.edu\\.pe$",
                message = "El correo debe ser de tipo @tecsup.edu.pe"
        )
        String email,
        String telefono,
        String direccion,
        String rol,
        Double latitud,
        Double longitud
) {}
