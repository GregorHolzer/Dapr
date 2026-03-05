package ac.at.uibk.dps.csm.dapr.cigarettesmokers.smoker

import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.DaprClient
import io.dapr.client.domain.CloudEvent
import kotlin.collections.set
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class SmokerPubSub(val client: ActorClient) {

  companion object {
    const val PUB_SUB_NAME = "smoker_pub_sub"
    const val START_SMOKING_TOPIC = "startSmokingTopic"

    fun startSmoking(client: DaprClient, id: Int): Mono<Void> {
      return client.publishEvent(PUB_SUB_NAME, START_SMOKING_TOPIC, id)
    }
  }

  val smokerActors: MutableMap<Int, SmokerActor> = HashMap()

  init {
    parseEnvList("HOSTED_SMOKERS")
  }

  @Topic(name = START_SMOKING_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$START_SMOKING_TOPIC")
  fun startSmokingSubscriber(@RequestBody(required = true) event: CloudEvent<Int>) {
    smokerActors[event.data]!!.startSmoking().subscribe()
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
    list.forEach { smokerActors[it] = getSmokerActor(it) }
    return list
  }

  private fun getSmokerActor(id: Int): SmokerActor {
    return ActorProxyBuilder(SmokerActor::class.java, client).build(ActorId(id.toString()))
  }
}
