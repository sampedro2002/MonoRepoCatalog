plugins {
    `java-library`
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

tasks.withType<Test> {
    useJUnitPlatform()
}
