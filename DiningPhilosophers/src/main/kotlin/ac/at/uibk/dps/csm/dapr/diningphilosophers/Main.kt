package ac.at.uibk.dps.csm.dapr.diningphilosophers

import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.ArbitratorActor
import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.ArbitratorActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.PhilosopherActor
import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.PhilosopherActorImpl
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.actors.runtime.ActorRuntime
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import kotlin.system.exitProcess

@SpringBootApplication
open class ActorService

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
  val numberOfPhilosophers = System.getenv().getOrDefault("NUMBER_OF_PHILOSOPHERS", "10").toInt()
  println("Starting Dining philosophers with $numberOfPhilosophers philosophers...")

  val client = ActorClient()
  val arbitratorBuilder: ActorProxyBuilder<ArbitratorActor> = ActorProxyBuilder(ArbitratorActor::class.java, client)
  val philosopherBuilder: ActorProxyBuilder<PhilosopherActor> = ActorProxyBuilder(PhilosopherActor::class.java, client)

  // Create one arbitratorActor
  println("Creating the arbitratorActor...")
  val arbitratorActorId = ActorId.createRandom()
  arbitratorBuilder.build(arbitratorActorId)

  for (i in 0..<numberOfPhilosophers) {
    val philosopher = philosopherBuilder.build(ActorId(i.toString()))
    // Non-blocking start
    philosopher.start().subscribe()
  }

  println("All philosophers signaled. Waiting for the feast...")

  // This mimics the 'thread.join()' from the example
  Thread.currentThread().join()
}

fun service() {

  val numberOfPhilosophers = System.getenv("NUMBER_OF_PHILOSOPHERS")?.toInt() ?: 20
  val port = System.getenv("PORT")?.toInt() ?: 3000
  val client = ActorClient()

  val arbitratorActorId = ActorId("ArbitratorActor")
  ActorRuntime.getInstance().registerActor(ArbitratorActorImpl::class.java) { context, _ ->
    ArbitratorActorImpl(context, arbitratorActorId, numberOfPhilosophers, client)
  }

  ActorRuntime.getInstance().registerActor(PhilosopherActorImpl::class.java) { context, id ->
    PhilosopherActorImpl(context, id.toString().toInt(), arbitratorActorId, client)
  }

  val app = SpringApplication(ActorService::class.java)

  app.run("--server.port=$port")
}