package org.ejemplo.categoria;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController
@RequestMapping("/api/categorias")
public class ControladorCategoria {

    private final ServicioCategoria servicioCategoria;

    public ControladorCategoria(ServicioCategoria servicioCategoria) {
        this.servicioCategoria = servicioCategoria;
    }

    @GetMapping
    public List<RespuestaCategoria> listar() {
        return servicioCategoria.buscarTodas().stream()
                .map(RespuestaCategoria::desde)
                .toList();
    }

    @GetMapping("/{id}")
    public RespuestaCategoria obtener(@PathVariable Long id) {
        return RespuestaCategoria.desde(servicioCategoria.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<RespuestaCategoria> crear(@Valid @RequestBody SolicitudCategoria request) {
        Categoria creada = servicioCategoria.crear(request.nombre(), request.descripcion());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(creada.getId())
                .toUri();
        return ResponseEntity.created(location).body(RespuestaCategoria.desde(creada));
    }

    @PutMapping("/{id}")
    public RespuestaCategoria actualizar(@PathVariable Long id, @Valid @RequestBody SolicitudCategoria request) {
        return RespuestaCategoria.desde(servicioCategoria.actualizar(id, request.nombre(), request.descripcion()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        servicioCategoria.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
