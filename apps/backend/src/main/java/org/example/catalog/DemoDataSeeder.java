package org.example.catalog;

import java.math.BigDecimal;
import java.util.List;
import org.ejemplo.categoria.Categoria;
import org.ejemplo.categoria.RepositorioCategoria;
import org.ejemplo.producto.Producto;
import org.ejemplo.producto.RepositorioProducto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inserts demo categories and products when the {@code dev} profile is active.
 * Runs only on an empty database, so restarts never duplicate the data.
 */
@Component
@Profile("dev")
public class DemoDataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

    private final RepositorioCategoria categories;
    private final RepositorioProducto products;

    public DemoDataSeeder(RepositorioCategoria categories, RepositorioProducto products) {
        this.categories = categories;
        this.products = products;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (categories.count() > 0) {
            log.info("Demo data already present, skipping seed");
            return;
        }

        Categoria laptops = categories.save(new Categoria("Laptops", "Portable computers"));
        Categoria audio = categories.save(new Categoria("Audio", "Headphones and speakers"));
        Categoria accessories = categories.save(new Categoria("Accessories", "Peripherals and cables"));

        products.saveAll(List.of(
                new Producto("Aero 14 Ultrabook", "14-inch, 16 GB RAM, 512 GB SSD",
                        new BigDecimal("1199.00"), 12, null, laptops),
                new Producto("Titan 16 Workstation", "16-inch, 32 GB RAM, RTX graphics",
                        new BigDecimal("2499.00"), 4, null, laptops),
                new Producto("Studio ANC Headphones", "Over-ear, active noise cancelling",
                        new BigDecimal("249.90"), 25, null, audio),
                new Producto("Pocket Speaker", "Waterproof Bluetooth speaker",
                        new BigDecimal("59.50"), 40, null, audio),
                new Producto("USB-C Dock 8-in-1", "HDMI, Ethernet, card reader, USB-A",
                        new BigDecimal("89.00"), 18, null, accessories),
                new Producto("Braided USB-C Cable 2m", "Nylon braided, 100 W charging",
                        new BigDecimal("14.99"), 120, null, accessories)));

        log.info("Seeded {} demo categories and {} demo products", categories.count(), products.count());
    }
}
