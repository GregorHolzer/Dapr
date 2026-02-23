plugins {
  id("application")
  id("com.ncorti.ktfmt.gradle") version "0.25.0"
  kotlin("jvm") version "2.2.20" apply false
  id("org.springframework.boot") version "3.2.0" apply false
  id("io.spring.dependency-management") version "1.1.4" apply false
}

allprojects {
  group = "ac.at.uibk.dps.csm.dapr"
  version = "1.0.0"

  repositories { mavenCentral() }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  java { toolchain { languageVersion = JavaLanguageVersion.of(21) } }

  dependencies {
    testImplementation(kotlin("test"))
    implementation("io.dapr:dapr-sdk:1.16.0")
    implementation("io.dapr:dapr-sdk-actors:1.16.0")
    implementation("io.dapr:dapr-sdk-springboot:1.16.0")
    implementation("commons-cli:commons-cli:1.9.0")
    implementation("org.springframework.boot:spring-boot-starter-web")
  }

  tasks.withType<Test> { useJUnitPlatform() }
}

ktfmt { googleStyle() }
