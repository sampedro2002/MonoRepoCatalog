// The openapi-generator plugin pulls an older jackson-core onto its classpath
// that conflicts with the one its YAML serializer needs. Pin it here.
buildscript {
    configurations.classpath {
        resolutionStrategy.force("com.fasterxml.jackson.core:jackson-core:2.17.2")
    }
}

plugins {
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

allprojects {
    group = "org.example"
    version = "1.0.0"
}
