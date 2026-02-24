package ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber

import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.PhilosopherActor
import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.domain.CloudEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@RestController
@ConditionalOnProperty(name = ["RUN_PHILOSOPHER_SUB"], havingValue = "true")
class PhilosopherSub{

  companion object{
    const val EAT_TOPIC_NAME = "eat"
    const val START_TOPIC_NAME = "start"
    const val PUB_SUB_NAME = "philosopher_pub_sub"
  }

  val client = ActorClient()

  val hostedPhilosophers = parseEnvList("HOSTED_PHILOSOPHERS")

  private fun getPhilosopherProxy(id: Int): PhilosopherActor {
    return ActorProxyBuilder(PhilosopherActor::class.java, client)
      .build(ActorId(id.toString()))
  }

  @Topic(name = EAT_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/eat")
  fun eat(@RequestBody event: CloudEvent<Int>): Mono<ResponseEntity<Unit>> {
    if (!hostedPhilosophers.contains(event.data)) {
      return Mono.just(ResponseEntity(HttpStatus.OK))
    }

    val philosopher = getPhilosopherProxy(event.data)

    return philosopher.eat().thenReturn(ResponseEntity<Unit>(HttpStatus.OK))
  }

  @Topic(name = START_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/start")
  fun start(): Mono<ResponseEntity<Unit>> {
    println("Subscriber: Starting dispatch for philosophers: $hostedPhilosophers")
    return Flux.fromIterable(hostedPhilosophers)
      .flatMap { pos ->
        getPhilosopherProxy(pos).start()
      }.then(Mono.just(ResponseEntity(HttpStatus.OK)))
  }

  fun parseEnvList(envName: String): List<Int> {
    val raw = System.getenv(envName) ?: return emptyList()

    return raw.split(",").flatMap { part ->
      if (part.contains("-")) {
        val bounds = part.split("-")
        (bounds[0].trim().toInt()..bounds[1].trim().toInt()).toList()
      } else {
        listOf(part.trim().toInt())
      }
    }
  }
}