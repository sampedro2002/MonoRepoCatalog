package org.example;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

// JPA scanning lives here instead of on CatalogApplication, so @WebMvcTest slices do not load it.
@Configuration
@EntityScan(basePackages = {"org.example", "org.ejemplo"})
@EnableJpaRepositories(basePackages = {"org.example", "org.ejemplo"})
public class PersistenceConfiguration {
}
