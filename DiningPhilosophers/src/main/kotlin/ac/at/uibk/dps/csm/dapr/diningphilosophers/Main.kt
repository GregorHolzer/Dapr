package ac.at.uibk.dps.csm.dapr.diningphilosophers

import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.ArbitratorActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.PhilosopherActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.ArbitratorSub
import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.PhilosopherSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.ActorRuntime
import io.dapr.client.DaprClientBuilder
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import java.lang.Thread.sleep
import kotlin.concurrent.thread
import kotlin.system.exitProcess

@SpringBootApplication
open class DiningPhilosophers

fun main() {
  val type = System.getenv("TYPE") ?: run{
    println("Missing TYPE env variable")
    exitProcess(1)
  }
  when (type) {
    "SERVICE" -> {
      service()
    }
    "CLIENT" -> {
      client()
    }
    else -> {
      println("Invalid TYPE")
      exitProcess(1)
    }
  }
}

fun client() {
  val client = DaprClientBuilder().build()
  val app = SpringApplication(DiningPhilosophers::class.java)
  val port = System.getenv("PORT")?.toInt() ?: 3000
  app.setDefaultProperties(mapOf("IS_CLIENT" to true))
  app.run("--server.port=$port")
  sleep(5000)
  println("Starting philosophers...")
  client.publishEvent(
    PhilosopherSub.PUB_SUB_NAME,
    PhilosopherSub.START_TOPIC_NAME,
    Unit
  ).block()

}

fun service() {
  val client = DaprClientBuilder().build()
  val app = SpringApplication(DiningPhilosophers::class.java)

  val port = System.getenv("PORT")?.toInt() ?: 3000
  val hostArbitratorActor = System.getenv("HOST_ARBITRATOR_ACTOR")?.toBoolean() ?: false
  val hostPhilosopherPubSub = System.getenv("HOST_PHILOSOPHER_PUBSUB")?.toBoolean() ?: false
  val props = mutableMapOf<String, Any>()

  if (hostArbitratorActor) {
    println("Hosting arbitrator pubsub...")
    val numberOfPhilosophers = System.getenv("NUMBER_OF_PHILOSOPHERS")?.toInt() ?: 20
    props["RUN_ARBITRATOR_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(ArbitratorActorImpl::class.java) { context, _ ->
      ArbitratorActorImpl(context, ActorId(ArbitratorSub.ARBITRATOR_NAME), numberOfPhilosophers, client)
    }
  }
  if (hostPhilosopherPubSub) {
    props["RUN_PHILOSOPHER_SUB"] = "true"
  }
  ActorRuntime.getInstance().registerActor(PhilosopherActorImpl::class.java) { context, id ->
    PhilosopherActorImpl(context, id.toString().toInt(), client)
  }
  app.setDefaultProperties(props)
  app.run("--server.port=$port")
}
