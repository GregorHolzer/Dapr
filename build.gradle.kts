plugins {
  id("com.ncorti.ktfmt.gradle") version "0.25.0"
  kotlin("jvm") version "2.2.20"
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
