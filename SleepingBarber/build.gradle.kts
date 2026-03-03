plugins {
  id("org.springframework.boot") version "3.2.0"
  id("io.spring.dependency-management") version "1.1.4"
}

springBoot {
  mainClass.set("ac.at.uibk.dps.csm.dapr.diningphilosophers.MainKt")
}