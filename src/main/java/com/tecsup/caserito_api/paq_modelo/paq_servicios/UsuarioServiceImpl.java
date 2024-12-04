package com.tecsup.caserito_api.paq_modelo.paq_servicios;

import com.tecsup.caserito_api.paq_modelo.paq_daos.UsuarioRepository;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Rol;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.RolEnum;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Usuario;
import com.tecsup.caserito_api.paq_web.paq_dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthService authService;

    @Override
    public UserResponse getAuthenticatedUser() {
        // Obtener el usuario autenticado desde el servicio de autenticación
        Usuario usuario = authService.getAuthenticatedUser();

        // Crear y devolver el record UserResponse con los datos del usuario
        return new UserResponse(
                usuario.getUsuario(),
                usuario.getEmail(),
                usuario.getDireccion(),
                usuario.getTelefono()
        );
    }




    @Override
    public Usuario registrarUsuario(Usuario usuario) {
        // Verificar si el usuario ya existe
        Optional<Usuario> existingUser = usuarioRepository.findByUsuario(usuario.getUsuario());
        if (existingUser.isPresent()) {
            throw new RuntimeException("El usuario ya existe.");
        }

        // Verificar si el email ya existe
        Optional<Usuario> existingEmail = usuarioRepository.findByEmail(usuario.getEmail());
        if (existingEmail.isPresent()) {
            throw new RuntimeException("El email ya está registrado.");
        }

        // Validar que el teléfono sea proporcionado
        if (usuario.getTelefono() == null || usuario.getTelefono().isEmpty()) {
            throw new RuntimeException("El teléfono es obligatorio.");
        }

        // Establecer valores predeterminados para campos de seguridad
        usuario.setEnabled(true); // Por defecto, habilitar la cuenta
        usuario.setAccountNonExpired(true); // Cuenta no expirada por defecto
        usuario.setAccountNonLocked(true); // Cuenta no bloqueada por defecto
        usuario.setCredentialsNonExpired(true); // Credenciales no expirada por defecto

        // Establecer la fecha de creación automáticamente
        usuario.setFecha_creacion(LocalDateTime.now());

        usuario.setFecha_modificacion(LocalDateTime.now());
        // Codificar la contraseña antes de guardarla
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

        // Asignar el rol USER automáticamente
        Rol rolUser = Rol.builder()
                .roleEnum(RolEnum.USER)
                .build();
        Set<Rol> roles = new HashSet<>();
        roles.add(rolUser);
        usuario.setRoles(roles);

        // Guardar el usuario en la base de datos
        return usuarioRepository.save(usuario);
    }

    @Override
    public Usuario obtenerUsuarioPorNombre(String nombre) {
        return usuarioRepository.findByUsuario(nombre)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
    }

}
