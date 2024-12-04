package com.tecsup.caserito_api.paq_config;

import com.tecsup.caserito_api.paq_config.paq_filter.JwtTokenValidator;
import com.tecsup.caserito_api.paq_util.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {
    @Autowired
    private JwtUtils jwtUtils;

    @Bean
    public SecurityFilterChain configure(HttpSecurity httpSecurity) throws Exception {
        return httpSecurity
                .cors(Customizer.withDefaults()) // Habilitar CORS con configuración global
                .csrf(csrf -> csrf.disable())
                .httpBasic(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(http -> {
                    http
                            .requestMatchers(HttpMethod.POST, "/caserito_api/authentication/*").permitAll()
                            .requestMatchers(HttpMethod.GET, "/caserito_api/restaurante/prueba").hasAuthority("EMPRESA")
                            .requestMatchers(HttpMethod.POST, "/caserito_api/user/update-user").hasAnyAuthority("USER", "EMPRESA")
                            .requestMatchers(HttpMethod.GET, "/caserito_api/user/me").hasAnyAuthority("USER", "EMPRESA")
                            .requestMatchers(HttpMethod.POST, "/caserito_api/restaurante/*").hasAuthority("EMPRESA")
                            .requestMatchers(HttpMethod.GET, "/caserito_api/restaurante/mis-restaurantes").hasAuthority("EMPRESA")
                            .requestMatchers(HttpMethod.GET, "/caserito_api/restaurante/all").hasAnyAuthority("USER", "EMPRESA")
                            .requestMatchers(HttpMethod.PUT, "/caserito_api/restaurante/update/{id}").hasAuthority("EMPRESA")
                            .requestMatchers(HttpMethod.DELETE, "/caserito_api/restaurante/delete/{id}").hasAuthority("EMPRESA")
                            .anyRequest().authenticated();
                })
                .addFilterBefore(new JwtTokenValidator(jwtUtils), BasicAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public AuthenticationProvider authenticationProvider(UserDetailServiceImpl userDetailService) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**") // Permitir todas las rutas
                        .allowedOrigins("*") // Permitir cualquier origen
                        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // Métodos permitidos
                        .allowedHeaders("*") // Permitir cualquier encabezado
                        .exposedHeaders("Authorization") // Exponer encabezados al cliente
                        .allowCredentials(false); // No permitir credenciales (cookies, etc.)
            }
        };
    }
}
