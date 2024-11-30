package com.tecsup.caserito_api.paq_config;

import com.tecsup.caserito_api.paq_modelo.paq_daos.UsuarioRepository;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Rol;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.RolEnum;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Usuario;
import com.tecsup.caserito_api.paq_modelo.paq_servicios.GeocodingService;
import com.tecsup.caserito_api.paq_util.JwtUtils;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthCreateUserRequest;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthLoginRequest;
import com.tecsup.caserito_api.paq_web.paq_dto.AuthResponse;
import com.tecsup.caserito_api.paq_web.paq_dto.UpdateUserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioRepository userRepository;

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private GeocodingService geocodingService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Buscar el usuario en el repositorio
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("El usuario " + username + " no existe"));

        // Lista para almacenar las autoridades (roles) del usuario
        List<SimpleGrantedAuthority> authorityList = usuario.getRoles()
                .stream()
                .map(rol -> new SimpleGrantedAuthority("ROLE_".concat(rol.getRoleEnum().name())))
                .collect(Collectors.toList());

        // Crear un UserDetails con el username, contraseña y lista de autoridades
        return new User(
                usuario.getUsuario(),
                usuario.getPassword(),
                usuario.isEnabled(),
                usuario.isAccountNonExpired(),
                usuario.isCredentialsNonExpired(),
                usuario.isAccountNonLocked(),
                authorityList);
    }

    public boolean existsByUsernameOrEmail(String username, String email) {
        return usuarioRepository.findByUsuario(username).isPresent() || usuarioRepository.findByEmail(email).isPresent();
    }


    public AuthResponse loginUser(AuthLoginRequest loginRequest) {
        String username = loginRequest.username();
        String password = loginRequest.password();

        // Verifica si el usuario existe
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new BadCredentialsException("El usuario " + username + " es incorrecto"));

        // Verifica si el password es correcto
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BadCredentialsException("Usuario o contraseña incorrectos");
        }

        // Autenticación del usuario
        Authentication authentication = this.authenticate(username, password);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Obtener el ID del usuario
        Long userId = usuario.getPk_Usuario();

        // Crear el token JWT
        String accessToken = jwtUtils.createToken(authentication, userId);

        // Obtener los roles del usuario autenticado
        List<String> roles = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        // Crear la respuesta AuthResponse
        return new AuthResponse(username, "Login correcto", accessToken, roles, true);
    }


    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = this.loadUserByUsername(username); // Se asume que lanza excepción si no existe
        // Verificar si la contraseña es correcta
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("La contraseña es incorrecta");
        }
        // Crear el token de autenticación sin pasar la contraseña
        return new UsernamePasswordAuthenticationToken(userDetails.getUsername(), null, userDetails.getAuthorities());
    }





    public AuthResponse createUser(AuthCreateUserRequest authCreateUserRequest) {
        // Verifica si el usuario o correo ya existen
        if (existsByUsernameOrEmail(authCreateUserRequest.username(), authCreateUserRequest.email())) {
            return new AuthResponse(
                    null, // username
                    "El usuario o correo ya existen",
                    null,
                    null,
                    false
            );
        }

        // Si no existe, crea el usuario
        String username = authCreateUserRequest.username();
        String password = authCreateUserRequest.password();
        String email = authCreateUserRequest.email();
        String telefono = authCreateUserRequest.telefono();
        String direccion = authCreateUserRequest.direccion();

        // Inicialización de coordenadas
        double latitud = 0.0;
        double longitud = 0.0;
        String geolocalizationErrorMessage = null;

        // Convierte la dirección a latitud y longitud
        if (direccion != null && !direccion.isEmpty()) {
            try {
                double[] coordinates = geocodingService.getCoordinates(direccion);
                latitud = coordinates[0];
                longitud = coordinates[1];
            } catch (Exception e) {
                // Si ocurre un error, se guarda el mensaje de error pero no se detiene la creación del usuario
                geolocalizationErrorMessage = "Hubo problemas con la ubicación.";
            }
        }

        Rol rolUser = Rol.builder()
                .roleEnum(RolEnum.USER)
                .build();
        Set<Rol> roles = new HashSet<>();
        roles.add(rolUser);

        Usuario usuario = Usuario.builder()
                .usuario(username)
                .password(passwordEncoder.encode(password))
                .isEnabled(true)
                .accountNonExpired(true)
                .accountNonLocked(true)
                .credentialsNonExpired(true)
                .email(email)
                .telefono(telefono)
                .direccion(direccion)
                .latitud(latitud)
                .longitud(longitud)
                .roles(roles)
                .fecha_creacion(LocalDateTime.now())
                .fecha_modificacion(LocalDateTime.now())
                .build();

        // Guardar el usuario
        Usuario userCreated = usuarioRepository.save(usuario);

        Long userId = userCreated.getPk_Usuario();
        List<SimpleGrantedAuthority> authorityArrayList = userCreated.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().name()))
                .collect(Collectors.toList());

        Authentication authentication = new UsernamePasswordAuthenticationToken(userCreated.getUsuario(), userCreated.getPassword(), authorityArrayList);
        String accessToken = jwtUtils.createToken(authentication, userId);

        List<String> rolesList = userCreated.getRoles().stream()
                .map(role -> role.getRoleEnum().name())
                .collect(Collectors.toList());

        // Si hubo un error en la geolocalización, se agrega al mensaje
        String finalMessage = "Usuario creado.";
        if (geolocalizationErrorMessage != null) {
            finalMessage += " Sin embargo, " + geolocalizationErrorMessage;
        }

        return new AuthResponse(
                userCreated.getUsuario(),
                finalMessage,
                accessToken,
                rolesList,
                true
        );
    }




    public AuthResponse updateUserDate(UpdateUserRequest updateUserRequest) {
        // Obtener el usuario autenticado
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // Obtener el nombre de usuario desde el contexto de seguridad

        // Buscar el usuario en la base de datos
        Usuario usuario = usuarioRepository.findByUsuario(username)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Actualizar los campos del usuario solo si los valores no son nulos
        if (updateUserRequest.username() != null) {
            usuario.setUsuario(updateUserRequest.username());
        }
        if (updateUserRequest.email() != null) {
            usuario.setEmail(updateUserRequest.email());
        }
        if (updateUserRequest.telefono() != null) {
            usuario.setTelefono(updateUserRequest.telefono());
        }
        if (updateUserRequest.direccion() != null && !updateUserRequest.direccion().isEmpty()) {
            usuario.setDireccion(updateUserRequest.direccion()); // Actualizamos la dirección

            // Convierte la dirección a latitud y longitud
            double latitud = 0.0;
            double longitud = 0.0;

            try {
                // Intentamos obtener las coordenadas a partir de la dirección
                double[] coordinates = geocodingService.getCoordinates(updateUserRequest.direccion());
                latitud = coordinates[0];
                longitud = coordinates[1];

                // Actualizamos las coordenadas del usuario
                usuario.setLatitud(latitud);
                usuario.setLongitud(longitud);
            } catch (Exception e) {
                // En caso de error al obtener las coordenadas, lanzamos una excepción con el mensaje adecuado
                throw new RuntimeException("No se pudo obtener las coordenadas para la dirección proporcionada.");
            }
        }

        // Si se proporcionan latitud y longitud, convertimos a dirección
        if (updateUserRequest.latitud() != null && updateUserRequest.longitud() != null) {
            try {
                // Convertimos las coordenadas en dirección
                String direccion = geocodingService.getAddress(updateUserRequest.latitud(), updateUserRequest.longitud());
                usuario.setDireccion(direccion);  // Actualizamos la dirección con la nueva dirección obtenida
            } catch (Exception e) {
                // En caso de error al convertir las coordenadas en dirección
                throw new RuntimeException("No se pudo obtener la dirección a partir de las coordenadas proporcionadas.");
            }

            // Actualizamos las coordenadas del usuario
            usuario.setLatitud(updateUserRequest.latitud());
            usuario.setLongitud(updateUserRequest.longitud());
        }

        // Si se proporciona una nueva contraseña, se debe encriptar antes de actualizarla
        if (updateUserRequest.password() != null) {
            usuario.setPassword(passwordEncoder.encode(updateUserRequest.password()));
        }

        // Agregar nuevos roles sin eliminar los roles existentes
        if (updateUserRequest.rol() != null) {
            Set<Rol> existingRoles = usuario.getRoles(); // Roles actuales del usuario
            String roleName = updateUserRequest.rol();   // Obtener el rol enviado

            if (roleName.equals("EMPRESA")) { // Verificar si el rol enviado es EMPRESA
                RolEnum roleEnum = RolEnum.valueOf(roleName); // Convertir el rol a enum

                // Verificar si el usuario ya tiene el rol EMPRESA
                boolean hasEmpresaRole = existingRoles.stream()
                        .anyMatch(role -> role.getRoleEnum() == RolEnum.EMPRESA);

                // Si no tiene el rol EMPRESA, agregarlo
                if (!hasEmpresaRole) {
                    Rol newRole = Rol.builder()
                            .roleEnum(roleEnum)
                            .build();
                    existingRoles.add(newRole); // Agregar el nuevo rol
                    usuario.setRoles(existingRoles); // Actualizar los roles del usuario
                }
            }
        }

        // Actualiza la fecha de modificación
        usuario.setFecha_modificacion(LocalDateTime.now());

        // Guardar el usuario con los datos actualizados
        usuarioRepository.save(usuario);

        // Crear un JWT para el usuario actualizado
        Long userId = usuario.getPk_Usuario();
        List<SimpleGrantedAuthority> authorityArrayList = usuario.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getRoleEnum().name()))
                .collect(Collectors.toList());

        Authentication updatedAuthentication = new UsernamePasswordAuthenticationToken(usuario.getUsuario(), usuario.getPassword(), authorityArrayList);
        String accessToken = jwtUtils.createToken(updatedAuthentication, userId);

        // Retornar la respuesta con los detalles actualizados
        List<String> rolesList = usuario.getRoles().stream()
                .map(role -> role.getRoleEnum().name())
                .collect(Collectors.toList());

        return new AuthResponse(
                usuario.getUsuario(),
                "Usuario actualizado correctamente",
                accessToken,
                rolesList,
                true
        );
    }





}
