package ac.at.uibk.dps.csm.dapr.sleepingbarber

import ac.at.uibk.dps.csm.dapr.sleepingbarber.barber.BarberActorImpl
import ac.at.uibk.dps.csm.dapr.sleepingbarber.barber.BarberSubscriber
import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerActorImpl
import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerSubscriber
import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomActorImpl
import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomSubscriber
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.runtime.ActorRuntime
import io.dapr.client.DaprClientBuilder
import java.lang.Thread.sleep
import kotlin.system.exitProcess
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean

@SpringBootApplication
open class DiningPhilosophers {
  @Bean open fun actorClient(): ActorClient = ActorClient()
}

fun main() {
  val type =
    System.getenv("TYPE")
      ?: run {
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
  println("Starting customers...")
  daprClient
    .publishEvent(CustomerSubscriber.PUB_SUB_NAME, CustomerSubscriber.ENTER_TOPIC, Unit)
    .block()
}

fun service(port: Int) {
  val daprClient = DaprClientBuilder().build()
  val app = SpringApplication(DiningPhilosophers::class.java)
  val props = mutableMapOf<String, Any>()

  val hostWaitingRoomSub = System.getenv("HOST_WAITING_ROOM_SUB")?.toBoolean() ?: false
  if (hostWaitingRoomSub) {
    println("Hosting waitingRoom Subscriber...")
    val numberOfSlots = System.getenv("NUMBER_OF_WAITING_SLOTS")?.toInt() ?: 3
    val numberOfCustomers = System.getenv("NUMBER_OF_CUSTOMERS")?.toInt() ?: 10
    props["HOST_WAITING_ROOM_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(WaitingRoomActorImpl::class.java) { context, _ ->
      WaitingRoomActorImpl(
        context,
        ActorId(WaitingRoomSubscriber.WAITING_ROOM_NAME),
        numberOfCustomers,
        numberOfSlots,
        daprClient,
      )
    }
  }
  val hostBarberSub = System.getenv("HOST_BARBER_SUB")?.toBoolean() ?: false
  if (hostBarberSub) {
    println("Hosting Barber Subscriber...")
    val cuttingDuration = System.getenv("BARBER_CUTTING_TIME_MS")?.toInt() ?: 10
    props["HOST_BARBER_SUB"] = "true"
    ActorRuntime.getInstance().registerActor(BarberActorImpl::class.java) { context, _ ->
      BarberActorImpl(context, ActorId(BarberSubscriber.BARBER_NAME), cuttingDuration, daprClient)
    }
  }
  val hostCustomerSub = System.getenv("HOST_CUSTOMER_PUBSUB")?.toBoolean() ?: false
  if (hostCustomerSub) {
    props["HOST_CUSTOMER_SUB"] = "true"
  }
  val cuttingRounds = System.getenv("NUMBER_OF_CUSTOMER_ROUNDS")?.toInt() ?: 10
  val registry =
  ActorRuntime.getInstance().registerActor(CustomerActorImpl::class.java) { context, id ->
    CustomerActorImpl(context, id.toString().toInt(), cuttingRounds, daprClient)
  }
  app.setDefaultProperties(props)
  app.run("--server.port=$port")
}
