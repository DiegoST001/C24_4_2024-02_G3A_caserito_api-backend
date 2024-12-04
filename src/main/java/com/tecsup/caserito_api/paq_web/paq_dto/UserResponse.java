package com.tecsup.caserito_api.paq_web.paq_dto;
public record UserResponse(
        String usuario,
        String email,
        String direccion,
        String telefono
) {}
