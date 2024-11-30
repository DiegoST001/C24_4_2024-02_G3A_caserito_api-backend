package com.tecsup.caserito_api.paq_modelo.paq_servicios;

import com.tecsup.caserito_api.paq_modelo.paq_daos.UsuarioRepository;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Usuario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * Obtiene el usuario autenticado desde el contexto de seguridad.
     *
     * @return Usuario autenticado
     * @throws RuntimeException si no se encuentra el usuario
     */
    public Usuario getAuthenticatedUser() {
        // Obtener la autenticación desde el contexto
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Validar si hay un usuario autenticado
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new RuntimeException("No hay usuario autenticado");
        }

        // Obtener el nombre del usuario autenticado
        String username = authentication.getName();

        // Buscar el usuario en el repositorio
        return usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }
}

