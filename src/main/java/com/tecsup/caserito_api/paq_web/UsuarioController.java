package com.tecsup.caserito_api.paq_web;

import com.tecsup.caserito_api.paq_config.UserDetailServiceImpl;
import com.tecsup.caserito_api.paq_modelo.paq_daos.UsuarioRepository;
import com.tecsup.caserito_api.paq_modelo.paq_servicios.UsuarioService;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthResponse;
import com.tecsup.caserito_api.paq_web.paq_dto.UpdateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/caserito_api/user")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private UserDetailServiceImpl userDetailService;

    // Método auxiliar para crear una respuesta de error
    private ResponseEntity<AuthResponse> buildErrorResponse(String message, HttpStatus status) {
        return new ResponseEntity<>(new AuthResponse(
                null,
                message,
                null,
                null,
                false
        ), status);
    }

    @PostMapping("/update-user")
    public ResponseEntity<AuthResponse> updateUser(
            @Validated @RequestBody UpdateUserRequest updateUserRequest) {
        try {
            // Validar que la dirección no está vacía antes de llamar al servicio
            if (updateUserRequest.direccion() != null && updateUserRequest.direccion().isEmpty()) {
                throw new IllegalArgumentException("La dirección no puede estar vacía.");
            }

            // Llamar al método de servicio pasando el token
            AuthResponse authResponse = userDetailService.updateUserDate(updateUserRequest);

            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return buildErrorResponse("Error en los datos proporcionados: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (RuntimeException e) {
            // Excepción de geocodificación o error similar
            return buildErrorResponse("Error en la geocodificación: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception ex) {
            return buildErrorResponse("Error inesperado: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
