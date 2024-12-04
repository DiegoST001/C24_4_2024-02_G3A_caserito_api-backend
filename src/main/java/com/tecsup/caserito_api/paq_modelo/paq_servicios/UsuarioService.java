package com.tecsup.caserito_api.paq_modelo.paq_servicios;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Usuario;
import com.tecsup.caserito_api.paq_web.paq_dto.UserResponse;

public interface UsuarioService {
    UserResponse getAuthenticatedUser();

    Usuario registrarUsuario(Usuario usuario);
    Usuario obtenerUsuarioPorNombre(String nombre);
    // Cambiado a String para retornar el token JWT
}
