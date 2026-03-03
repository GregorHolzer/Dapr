package ac.at.uibk.dps.csm.dapr.diningphilosophers

import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.ArbitratorActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.PhilosopherActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.ArbitratorSub
import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.PhilosopherSub
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.runtime.ActorRuntime
import io.dapr.client.DaprClientBuilder
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import java.lang.Thread.sleep
import kotlin.system.exitProcess

@SpringBootApplication
open class DiningPhilosophers{
  @Bean
  open fun actorClient(): ActorClient = ActorClient()
}

fun main() {
  val type = System.getenv("TYPE") ?: run{
    println("Missing TYPE env variable")
    exitProcess(1)
  }
  val port = System.getenv("PORT")?.toInt() ?: 3000
  when (type) {
    "SERVICE" -> {
      service(port)
    }
    "CLIENT" -> {
      daprClient(port)
    }
    else -> {
      println("Invalid TYPE")
      exitProcess(1)
    }
  }
}

fun daprClient(port: Int) {
  val daprClient = DaprClientBuilder().build()
  val app = SpringApplication(DiningPhilosophers::class.java)
  app.setDefaultProperties(mapOf("IS_CLIENT" to true))
  app.run("--server.port=$port")
  sleep(5000)
  println("Starting philosophers...")
  daprClient.publishEvent(
    PhilosopherSub.PUB_SUB_NAME,
    PhilosopherSub.START_TOPIC_NAME,
    Unit
  ).block()
}

fun service(port: Int) {
  val daprClient = DaprClientBuilder().build()
  val app = SpringApplication(DiningPhilosophers::class.java)
  val props = mutableMapOf<String, Any>()

  val hostArbitratorActor = System.getenv("HOST_ARBITRATOR_ACTOR")?.toBoolean() ?: false
  val hostPhilosopherPubSub = System.getenv("HOST_PHILOSOPHER_PUBSUB")?.toBoolean() ?: false
  val eatingRounds = System.getenv("PHILOSOPHER_EATING_ROUNDS")?.toInt() ?: 10
  val eatingDuration = System.getenv("PHILOSOPHER_EATING_TIME_MS")?.toInt() ?: 10

  if (hostArbitratorActor) {
    println("Hosting arbitrator pubsub...")
    val numberOfPhilosophers = System.getenv("NUMBER_OF_PHILOSOPHERS")?.toInt() ?: 20
    props["RUN_ARBITRATOR_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(ArbitratorActorImpl::class.java) { context, _ ->
      ArbitratorActorImpl(context, ActorId(ArbitratorSub.ARBITRATOR_NAME), numberOfPhilosophers, eatingRounds, daprClient)
    }
  }
  if (hostPhilosopherPubSub) {
    props["RUN_PHILOSOPHER_SUB"] = "true"
  }
  ActorRuntime.getInstance().registerActor(PhilosopherActorImpl::class.java) { context, id ->
    PhilosopherActorImpl(context, id.toString().toInt(), eatingRounds, eatingDuration, daprClient)
  }
  app.setDefaultProperties(props)
  app.run("--server.port=$port")
}
