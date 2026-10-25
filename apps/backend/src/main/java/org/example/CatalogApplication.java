package org.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// The catalog code lives in org.example (platform code) and org.ejemplo (catalog domain),
// so component scanning covers both packages.
@SpringBootApplication(scanBasePackages = {"org.example", "org.ejemplo"})
public class CatalogApplication {

    public static void main(String[] args) {
        SpringApplication.run(CatalogApplication.class, args);
    }
}
