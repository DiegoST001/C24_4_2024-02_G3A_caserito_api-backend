package com.tecsup.caserito_api.paq_web;

import com.tecsup.caserito_api.paq_exception.ErrorResponse;
import com.tecsup.caserito_api.paq_exception.RestauranteExistenteException;
import com.tecsup.caserito_api.paq_modelo.paq_entidades.Restaurante;
import com.tecsup.caserito_api.paq_modelo.paq_servicios.RestauranteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;


import java.util.List;

@RestController
@RequestMapping("/caserito_api/restaurante")
public class RestauranteController {

    @Autowired
    private RestauranteService restauranteService;

    // Endpoint para crear un restaurante
    @PostMapping("/create")
    public Restaurante crearRestaurante(@RequestBody Restaurante restaurante) {
        return restauranteService.createRestaurante(restaurante);
    }


    // Ruta GET de prueba para verificar la conectividad
    @GetMapping("/prueba")
    public String prueba() {
        return "Hola"; // Responde con un mensaje simple
    }

    @PostMapping()
    public List<Restaurante> listarRestaurantes(Double latitude, Double longitude
    ){
        return null;
    }

    @GetMapping("/mis-restaurantes")
    public List<Restaurante> obtenerRestaurantesDelUsuario() {
        return restauranteService.getRestaurantesPorUsuario();
    }

    @GetMapping("/all")
    public List<Restaurante> obtenerRestaurantes() {return  restauranteService.getAllRestaurantes();}

    @PutMapping("/update/{id}")
    public ResponseEntity<?> actualizarRestaurante(@PathVariable Long id, @RequestBody Restaurante restauranteDetalles) {
        try {
            Restaurante updatedRestaurante = restauranteService.updateRestaurante(id, restauranteDetalles);
            return ResponseEntity.ok(updatedRestaurante);
        } catch (RestauranteExistenteException e) {
            // Manejar excepción de restaurante existente
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        } catch (RuntimeException e) {
            // Manejar errores de permisos o datos faltantes
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            // Manejar cualquier otro error inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error inesperado: " + e.getMessage()));
        }
    }


    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteRestaurante(@PathVariable Long id) {
        try {
            restauranteService.deleteRestaurante(id);
            return ResponseEntity.ok().body(new ErrorResponse("Restaurante eliminado con éxito"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error al eliminar restaurante: " + e.getMessage()));
        }
    }



}
