package com.tecsup.caserito_api.paq_web;

import com.tecsup.caserito_api.paq_config.UserDetailServiceImpl;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthCreateUserRequest;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthLoginRequest;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.BadCredentialsException;


@RestController
@RequestMapping("/caserito_api/authentication")
public class AuthController {
    @Autowired
    private UserDetailServiceImpl userDetailService;


    @PostMapping("/sign-up")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid AuthCreateUserRequest authCreateUser, BindingResult bindingResult) {
        // Verifica si hay errores de validación en los datos recibidos
        if (bindingResult.hasErrors()) {
            StringBuilder errorMessage = new StringBuilder("Errores de validación: ");
            bindingResult.getAllErrors().forEach(error ->
                    errorMessage.append(error.getDefaultMessage()).append(" ")
            );

            return ResponseEntity.badRequest().body(new AuthResponse(
                    null,                             // username
                    errorMessage.toString(),          // Mensaje de error
                    null,                             // jwt
                    null,                             // roles
                    false                             // status
            ));
        }

        // Llama al servicio para crear el usuario
        AuthResponse authResponse = this.userDetailService.createUser(authCreateUser);

        // Si la creación del usuario falla, devolvemos la respuesta de error
        if (!authResponse.status()) {
            return ResponseEntity.badRequest().body(authResponse);
        }

        // Si el usuario se crea correctamente, devolvemos la respuesta con el JWT y los detalles
        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
    }


    @PostMapping("/log-in")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthLoginRequest userRequest) {
        try {
            // Intenta realizar el login
            AuthResponse authResponse = this.userDetailService.loginUser(userRequest);
            return new ResponseEntity<>(authResponse, HttpStatus.OK);
        } catch (BadCredentialsException ex) {
            // En caso de que el usuario o la contraseña sean incorrectos
            return new ResponseEntity<>(new AuthResponse(null, ex.getMessage(), null, null, false), HttpStatus.BAD_REQUEST);
        } catch (Exception ex) {
            // Manejo de errores genéricos
            return new ResponseEntity<>(new AuthResponse(null, "Error inesperado", null, null, false), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }



}
