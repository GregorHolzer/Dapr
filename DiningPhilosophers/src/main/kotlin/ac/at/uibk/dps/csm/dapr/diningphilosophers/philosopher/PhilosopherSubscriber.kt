package ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher

import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.domain.CloudEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux

@RestController
@ConditionalOnProperty(name = ["RUN_PHILOSOPHER_SUB"], havingValue = "true")
class PhilosopherSubscriber(val client: ActorClient) {

  companion object {
    const val EAT_TOPIC_NAME = "eat"
    const val START_TOPIC_NAME = "start"
    const val PUB_SUB_NAME = "philosopher_pub_sub"
  }

  val philosopherActors: MutableMap<Int, PhilosopherActor> = HashMap()

  val hostedPhilosophers = parseEnvList("HOSTED_PHILOSOPHERS")

  @Topic(name = EAT_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/eat")
  fun eat(@RequestBody event: CloudEvent<Int>) {
    philosopherActors[event.data]?.eat()?.subscribe()
  }

  @Topic(name = START_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/start")
  fun start() {
    println("Subscriber: Starting dispatch for philosophers: $hostedPhilosophers")
    Flux.fromIterable(hostedPhilosophers)
      .flatMap { pos -> philosopherActors[pos]!!.start() }
      .subscribe()
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
    list.forEach { philosopherActors[it] = getPhilosopherProxy(it) }
    return list
  }

  private fun getPhilosopherProxy(id: Int): PhilosopherActor {
    return ActorProxyBuilder(PhilosopherActor::class.java, client).build(ActorId(id.toString()))
  }
}
