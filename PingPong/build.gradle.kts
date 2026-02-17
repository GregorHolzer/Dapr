plugins {
  kotlin("jvm") version "2.2.20"
  id("application")
  id("com.ncorti.ktfmt.gradle") version "0.25.0"
}

repositories { mavenCentral() }

dependencies {
  testImplementation(kotlin("test"))
  implementation("io.dapr:dapr-sdk:1.16.0")
  implementation("io.dapr:dapr-sdk-actors:1.16.0")
}

tasks.test { useJUnitPlatform() }

kotlin { jvmToolchain(24) }

application { mainClass.set("ac.at.uibk.dps.csm.dapr.pingpong.MainKt") }

ktfmt { googleStyle() }
