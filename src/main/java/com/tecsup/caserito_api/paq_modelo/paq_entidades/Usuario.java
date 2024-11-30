package com.tecsup.caserito_api.paq_modelo.paq_entidades;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "Usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pk_Usuario;

    @Column(nullable = false, unique = true)
    @NotNull
    @Size(max = 50)
    private String usuario;

    @Column(nullable = false, unique = true)
    @NotNull
    @Email
    @Size(max = 150)
    private String email;

    @Column(nullable = false)
    @NotNull
    @Size(min = 8, max = 200)
    private String password;

    /*Datos extras de spring security*/
    @Column(name = "is_enabled")
    private boolean isEnabled;
    @Column(name = "account_Non_Expired")
    private boolean accountNonExpired;
    @Column(name = "account_Non_Locked")
    private boolean accountNonLocked;
    @Column(name = "credential_Non_Expired")
    private boolean credentialsNonExpired;

    @Column(nullable = true)
    @Size(max = 500)
    private String direccion;

    @Column(nullable = true)
    private Double latitud;

    @Column(nullable = true)
    private Double longitud;

    @Column(nullable = true)
    @Size(max = 15)
    private String telefono;

    @Column(nullable = false)
    private LocalDateTime fecha_creacion;

    // Campo para la fecha de modificación
    @Column(nullable = true)
    private LocalDateTime fecha_modificacion;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(name = "usuario_rol", joinColumns = @JoinColumn(name = "pk_usuario"), inverseJoinColumns = @JoinColumn(name = "pk_rol"))
    private Set<Rol> roles = new HashSet<>();
}
