package ac.at.uibk.dps.csm.dapr.sleepingbarber.customer

import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.domain.CloudEvent
import kotlin.collections.set
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@ConditionalOnProperty(name = ["HOST_CUSTOMER_SUB"], havingValue = "true")
class CustomerSubscriber(val client: ActorClient) {

  companion object {
    const val PUB_SUB_NAME = "customer_pub_sub"
    const val ENTER_TOPIC = "enter"
    const val FULL_TOPIC = "full"
    const val DONE_TOPIC = "done"
  }

  val customerActors: MutableMap<Int, CustomerActor> = HashMap()

  val hostedCustomers = parseEnvList("HOSTED_CUSTOMERS")

  @Topic(name = ENTER_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$ENTER_TOPIC")
  fun enter() {
    Flux.fromIterable(hostedCustomers)
      .flatMap { id -> customerActors[id]!!.enterWaitingRoom() }
      .subscribe()
  }

  @Topic(name = FULL_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$FULL_TOPIC")
  fun full(@RequestBody event: CloudEvent<Int>) {
    customerActors[event.data]?.waitingRoomFull()?.subscribe()
  }

  @Topic(name = DONE_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$DONE_TOPIC")
  fun done(@RequestBody event: CloudEvent<Int>) {
    customerActors[event.data]?.doneCutting()?.subscribe()
  }

  fun parseEnvList(envName: String): List<Int> {
    val raw = System.getenv(envName) ?: return emptyList()
    val list =
      raw.split(",").flatMap { part ->
        if (part.contains("-")) {
          val bounds = part.split("-")
          (bounds[0].trim().toInt()..bounds[1].trim().toInt()).toList()
        } else {
          listOf(part.trim().toInt())
        }
      }
    list.forEach { customerActors[it] = getPhilosopherProxy(it) }
    return list
  }

  private fun getPhilosopherProxy(id: Int): CustomerActor {
    return ActorProxyBuilder(CustomerActor::class.java, client).build(ActorId(id.toString()))
  }
}
