package com.tecsup.caserito_api.paq_modelo.paq_daos;

import com.tecsup.caserito_api.paq_modelo.paq_entidades.Restaurante;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;


public interface RestauranteRepository extends JpaRepository<Restaurante, Long> {
    List<Restaurante> findByUsuario(Usuario usuario);
    Optional<Restaurante> findByNombreAndUsuario(String nombre, Usuario usuario);
    Optional<Restaurante> findByNombre(String nombre);
    Optional<Restaurante> findAllByTipo(String tipo);


}
