plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.openapi.generator)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

val catalogApiSpec = layout.projectDirectory.file("src/main/openapi/catalog-api.yaml")
val catalogClientDir = layout.buildDirectory.dir("generated/openapi/catalog-client")

// Typed Java client generated from the OpenAPI spec. Regenerated on every compile.
openApiGenerate {
    generatorName.set("java")
    library.set("native")
    inputSpec.set(catalogApiSpec.asFile.absolutePath)
    outputDir.set(catalogClientDir.get().asFile.absolutePath)
    apiPackage.set("org.example.shared.catalog.api")
    invokerPackage.set("org.example.shared.catalog.client")
    modelPackage.set("org.example.shared.catalog.model")
    configOptions.set(
        mapOf(
            "serializationLibrary" to "jackson",
            "dateLibrary" to "java8",
            "openApiNullable" to "false",
            "useJakartaEe" to "true",
        )
    )
    generateApiTests.set(false)
    generateModelTests.set(false)
    generateApiDocumentation.set(false)
    // Remove files left over from earlier specs, so renamed tags do not leave stale classes behind
    cleanupOutput.set(true)
}

sourceSets {
    main {
        java.srcDir(catalogClientDir.map { it.dir("src/main/java") })
    }
}

tasks.named("compileJava") {
    dependsOn("openApiGenerate")
}

dependencies {
    api(libs.jackson.databind)
    api(libs.jackson.datatype.jsr310)
    api(libs.jakarta.annotation.api)

    testImplementation(libs.junit.jupiter)
}

// The OpenAPI contract ships inside the jar, so consumers can read the spec the client was built from
tasks.jar {
    from(catalogApiSpec) {
        into("openapi")
    }
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            artifactId = "catalog-shared"
            from(components["java"])
        }
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
