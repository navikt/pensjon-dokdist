import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	application
	alias(libs.plugins.kotlin.jvm)
	alias(libs.plugins.kotlin.plugin.spring)
	alias(libs.plugins.spring.boot)
	alias(libs.plugins.spring.dependency.management)
}

val javaTarget: String = providers.systemProperty("javaTarget").get()

group = "no.nav"
version = "0.0.1-SNAPSHOT"
description = "Løsning for sentral print/dokumentdistribusjon"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(javaTarget)
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

repositories {
	mavenCentral()
}

configurations.all {
	// Bruker spring-boot-starter-log4j2 som logging-implementasjon i stedet.
	exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
}

dependencies {
	implementation(libs.spring.boot.starter.web)
	implementation(libs.spring.boot.starter.oauth2.client)
	implementation(libs.spring.boot.starter.log4j2)
	implementation(libs.log4j.layout.template.json)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.springdoc.openapi.starter.webmvc.ui)

	testImplementation(libs.spring.boot.starter.test)
	testImplementation(libs.springmockk)
}

tasks.withType<KotlinCompile> {
	compilerOptions {
		freeCompilerArgs.add("-Xjsr305=strict")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks {
	build {
		dependsOn(installDist)
	}
}

tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
	archiveFileName = "pensjon-dokdist.jar"
}
