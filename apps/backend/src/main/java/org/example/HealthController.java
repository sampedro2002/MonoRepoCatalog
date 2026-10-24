package org.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/api/health")
    public HealthStatus health() {
        return new HealthStatus("UP");
    }

    // Matches the HealthStatus schema in packages/shared/src/main/openapi/catalog-api.yaml
    public record HealthStatus(String status) {
    }
}
