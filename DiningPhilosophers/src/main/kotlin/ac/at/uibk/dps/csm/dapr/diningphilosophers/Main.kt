package ac.at.uibk.dps.csm.dapr.diningphilosophers

import ac.at.uibk.dps.csm.dapr.diningphilosophers.arbitrator.ArbitratorActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.arbitrator.ArbitratorPubSub
import ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher.PhilosopherActorImpl
import ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher.PhilosopherPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.runtime.ActorRuntime
import io.dapr.client.DaprClient
import io.dapr.client.DaprClientBuilder
import java.lang.Thread.sleep
import kotlin.system.exitProcess
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class DiningPhilosophers {
  companion object {
    val logger: Logger = LoggerFactory.getLogger(DiningPhilosophers::class.java)
    val daprClient: DaprClient = DaprClientBuilder().build()
  }

  @Bean open fun actorClient(): ActorClient = ActorClient()
}

fun main() {
  val type = System.getenv("TYPE") ?: "SERVICE"

  when (type) {
    "SERVICE" -> service()
    "CLIENT" -> {
      sleep(5000)
      DiningPhilosophers.logger.info("starting philosophers...")
      PhilosopherPubSub.start(DiningPhilosophers.daprClient).block()
    }
    else -> {
      DiningPhilosophers.logger.error("received invalid TYPE: $type")
      exitProcess(1)
    }
  }
}

fun service() {
  val app = SpringApplication(DiningPhilosophers::class.java)
  val props = mutableMapOf<String, Any>()

  val hostArbitratorActor = System.getenv("HOST_ARBITRATOR_ACTOR")?.toBoolean() ?: false
  val hostPhilosopherPubSub = System.getenv("HOST_PHILOSOPHER_PUBSUB")?.toBoolean() ?: false
  val eatingDuration = System.getenv("PHILOSOPHER_EATING_TIME_MS")?.toInt() ?: 0

  if (hostArbitratorActor) {
    DiningPhilosophers.logger.info("hosting arbitrator pubsub...")
    val numberOfPhilosophers = System.getenv("NUMBER_OF_PHILOSOPHERS")?.toInt() ?: 20
    props["RUN_ARBITRATOR_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(ArbitratorActorImpl::class.java) { context, _ ->
      ArbitratorActorImpl(
        context,
        ActorId(ArbitratorPubSub.ARBITRATOR_NAME),
        numberOfPhilosophers,
        DiningPhilosophers.daprClient,
      )
    }
  }
  if (hostPhilosopherPubSub) {
    DiningPhilosophers.logger.info("hosting philosopher pubsub...")
    props["RUN_PHILOSOPHER_SUB"] = "true"
  }
  ActorRuntime.getInstance().registerActor(PhilosopherActorImpl::class.java) { context, id ->
    PhilosopherActorImpl(
      context,
      id.toString().toInt(),
      eatingDuration,
      DiningPhilosophers.daprClient,
    )
  }
  app.setDefaultProperties(props)
  app.run()
}
