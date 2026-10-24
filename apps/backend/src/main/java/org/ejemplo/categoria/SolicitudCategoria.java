package org.ejemplo.categoria;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SolicitudCategoria(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion) {
}
