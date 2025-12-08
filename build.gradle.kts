plugins {
	java
	id("org.springframework.boot") version "4.0.0"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.kirasin"
version = "0.0.1-SNAPSHOT"
description = "Demo project for Spring Boot"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

repositories {
	mavenCentral()
}

dependencies {
    // --- REACTIVE CORE ---
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springframework.kafka:spring-kafka")
    implementation("org.apache.kafka:kafka-streams")
    implementation("io.projectreactor.kafka:reactor-kafka:1.3.25")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-r2dbc")
    testImplementation("org.springframework.security:spring-security-test")

    // --- DATABASE & DRIVERS ---
    runtimeOnly("org.postgresql:r2dbc-postgresql")   // R2DBC драйвер
    implementation("org.postgresql:postgresql")      // JDBC драйвер (для Liquibase)
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    // --- MIGRATIONS ---
    implementation("org.springframework.boot:spring-boot-starter-liquibase")

    // --- UTILS ---
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")

    runtimeOnly  ("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly  ("io.jsonwebtoken:jjwt-jackson:0.13.0")

    // --- TESTING ---
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.projectreactor:reactor-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("io.projectreactor:reactor-test")

    // Добавляем Reactive Redis
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
