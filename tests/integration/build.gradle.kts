plugins {
    java
    alias(libs.plugins.spring.dependency.management)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:${libs.versions.springBoot.get()}")
    }
}

dependencies {
    testImplementation(project(":apps:backend"))
    testImplementation(libs.spring.boot.starter.web)
    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.core)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testRuntimeOnly(libs.postgresql)
}

tasks.withType<Test> {
    useJUnitPlatform()
}
