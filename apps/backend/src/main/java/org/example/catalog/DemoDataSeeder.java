package org.example.catalog;

import java.math.BigDecimal;
import java.util.List;
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

    private final CategoryRepository categories;
    private final ProductRepository products;

    public DemoDataSeeder(CategoryRepository categories, ProductRepository products) {
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

        Category laptops = categories.save(new Category("Laptops", "Portable computers"));
        Category audio = categories.save(new Category("Audio", "Headphones and speakers"));
        Category accessories = categories.save(new Category("Accessories", "Peripherals and cables"));

        products.saveAll(List.of(
                new Product("Aero 14 Ultrabook", "LAP-AERO-14", "14-inch, 16 GB RAM, 512 GB SSD",
                        new BigDecimal("1199.00"), laptops),
                new Product("Titan 16 Workstation", "LAP-TITAN-16", "16-inch, 32 GB RAM, RTX graphics",
                        new BigDecimal("2499.00"), laptops),
                new Product("Studio ANC Headphones", "AUD-STUDIO-ANC", "Over-ear, active noise cancelling",
                        new BigDecimal("249.90"), audio),
                new Product("Pocket Speaker", "AUD-POCKET-01", "Waterproof Bluetooth speaker",
                        new BigDecimal("59.50"), audio),
                new Product("USB-C Dock 8-in-1", "ACC-DOCK-8", "HDMI, Ethernet, card reader, USB-A",
                        new BigDecimal("89.00"), accessories),
                new Product("Braided USB-C Cable 2m", "ACC-CABLE-2M", "Nylon braided, 100 W charging",
                        new BigDecimal("14.99"), accessories)));

        log.info("Seeded {} demo categories and {} demo products", categories.count(), products.count());
    }
}
