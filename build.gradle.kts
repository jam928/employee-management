import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.5.4"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.21"
	kotlin("plugin.spring") version "1.5.21"
	kotlin("plugin.jpa") version "1.5.21"
	idea
}

group = "com.tm"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-data-jpa:2.5.5")
	implementation("org.springframework.boot:spring-boot-starter-web:2.5.5")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	runtimeOnly("mysql:mysql-connector-java:8.0.25")
	implementation("org.apache.httpcomponents:httpclient:4.5.13")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.5")
	testImplementation("io.mockk:mockk:1.12.0")
	testImplementation("org.jetbrains.kotlin:kotlin-test:1.3.21")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit:1.3.21")
	testImplementation("com.h2database:h2:1.4.200")
	implementation ("com.fasterxml.jackson.module:jackson-module-kotlin:2.8.+")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform {
		includeEngines("junit-jupiter")
		includeEngines("junit-vintage")
	}
}
