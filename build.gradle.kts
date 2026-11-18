plugins {
	id("org.springframework.boot") version "4.1.2"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("jvm") version "2.3.21"
	kotlin("plugin.spring") version "2.3.21"
	kotlin("plugin.jpa") version "2.3.21"
	kotlin("kapt") version "2.3.21"
	idea
	jacoco
}

group = "com.tm"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion.set(JavaLanguageVersion.of(21))
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-webmvc")
	implementation("org.springframework.boot:spring-boot-starter-flyway")
	implementation("org.flywaydb:flyway-mysql")
	implementation("tools.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.mapstruct:mapstruct:1.6.3")
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:3.2.0")
	runtimeOnly("com.mysql:mysql-connector-j")
	kapt("org.mapstruct:mapstruct-processor:1.6.3")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
	testImplementation("org.springframework.boot:spring-boot-testcontainers")
	testImplementation("org.testcontainers:testcontainers-junit-jupiter")
	testImplementation("org.testcontainers:testcontainers-mysql")
	testImplementation("io.mockk:mockk:1.14.12")
	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kapt {
	arguments {
		arg("mapstruct.defaultComponentModel", "spring")
		arg("mapstruct.unmappedTargetPolicy", "ERROR")
	}
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	// MockK attaches its agent at runtime
	jvmArgs("-XX:+EnableDynamicAgentLoading")
	testLogging {
		events("passed", "skipped", "failed")
	}
	finalizedBy(tasks.jacocoTestReport)
}

jacoco {
	toolVersion = "0.8.16"
}

tasks.jacocoTestReport {
	dependsOn(tasks.test)
	reports {
		xml.required.set(true)
		html.required.set(true)
	}
	finalizedBy("jacocoCoverageSummary")
}

// Prints the overall coverage numbers from the JaCoCo XML report to the console
tasks.register("jacocoCoverageSummary") {
	dependsOn(tasks.jacocoTestReport)
	doLast {
		val report = tasks.jacocoTestReport.get().reports.xml.outputLocation.get().asFile
		if (!report.exists()) return@doLast
		val factory = javax.xml.parsers.DocumentBuilderFactory.newInstance()
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
		val root = factory.newDocumentBuilder().parse(report).documentElement
		println("\n===== Code Coverage =====")
		val counters = root.childNodes
		for (i in 0 until counters.length) {
			val node = counters.item(i) as? org.w3c.dom.Element ?: continue
			if (node.tagName != "counter") continue
			val missed = node.getAttribute("missed").toDouble()
			val covered = node.getAttribute("covered").toDouble()
			val pct = if (missed + covered == 0.0) 0.0 else covered * 100 / (missed + covered)
			println("%-12s %6.2f%%  (%d/%d)".format(node.getAttribute("type"), pct, covered.toInt(), (missed + covered).toInt()))
		}
		println("HTML report: ${tasks.jacocoTestReport.get().reports.html.outputLocation.get().asFile}/index.html\n")
	}
}
