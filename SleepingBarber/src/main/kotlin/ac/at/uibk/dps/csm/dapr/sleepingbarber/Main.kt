package ac.at.uibk.dps.csm.dapr.sleepingbarber

import ac.at.uibk.dps.csm.dapr.sleepingbarber.barber.BarberActorImpl
import ac.at.uibk.dps.csm.dapr.sleepingbarber.barber.BarberPubSub
import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerActorImpl
import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerPubSub
import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomActorImpl
import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomPubSub
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
open class SleepingBarber {
  companion object {
    val logger: Logger = LoggerFactory.getLogger(SleepingBarber::class.java)
    val daprClient: DaprClient = DaprClientBuilder().build()
  }

  @Bean open fun actorClient(): ActorClient = ActorClient()
}

fun main() {
  val type = System.getenv("TYPE") ?: "SERVICE"
  when (type) {
    "SERVICE" -> {
      service()
    }
    "CLIENT" -> {
      sleep(5000)
      SleepingBarber.logger.info("Starting customers...")
      CustomerPubSub.enter(SleepingBarber.daprClient).block()
    }
    else -> {
      SleepingBarber.logger.error("received invalid TYPE: $type")
      exitProcess(1)
    }
  }
}

fun service() {
  val app = SpringApplication(SleepingBarber::class.java)
  val props = mutableMapOf<String, Any>()

  val hostWaitingRoomSub = System.getenv("HOST_WAITING_ROOM_SUB")?.toBoolean() ?: false
  val hostBarberSub = System.getenv("HOST_BARBER_SUB")?.toBoolean() ?: false
  val hostCustomerSub = System.getenv("HOST_CUSTOMER_PUBSUB")?.toBoolean() ?: false

  if (hostWaitingRoomSub) {
    SleepingBarber.logger.info("hosting waitingRoom Subscriber...")
    val numberOfSlots = System.getenv("NUMBER_OF_WAITING_SLOTS")?.toInt() ?: 3
    props["HOST_WAITING_ROOM_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(WaitingRoomActorImpl::class.java) { context, _ ->
      WaitingRoomActorImpl(
        context,
        ActorId(WaitingRoomPubSub.WAITING_ROOM_NAME),
        numberOfSlots,
        SleepingBarber.daprClient,
      )
    }
  }

  if (hostBarberSub) {
    SleepingBarber.logger.info("hosting Barber Subscriber...")
    val cuttingDuration = System.getenv("BARBER_CUTTING_TIME_MS")?.toInt() ?: 0
    props["HOST_BARBER_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(BarberActorImpl::class.java) { context, _ ->
      BarberActorImpl(
        context,
        ActorId(BarberPubSub.BARBER_NAME),
        cuttingDuration,
        SleepingBarber.daprClient,
      )
    }
  }
  if (hostCustomerSub) {
    SleepingBarber.logger.info("hosting Customer Subscriber...")
    props["HOST_CUSTOMER_SUB"] = "true"
  }

  ActorRuntime.getInstance().registerActor(CustomerActorImpl::class.java) { context, id ->
    CustomerActorImpl(context, id.toString().toInt(), SleepingBarber.daprClient)
  }
  app.setDefaultProperties(props)
  app.run()
}
