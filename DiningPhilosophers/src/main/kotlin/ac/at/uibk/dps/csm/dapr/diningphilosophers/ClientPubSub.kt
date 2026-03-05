package ac.at.uibk.dps.csm.dapr.diningphilosophers

import ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher.PhilosopherPubSub
import io.dapr.Topic
import io.dapr.client.DaprClient
import java.time.Duration
import java.time.Instant
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@ConditionalOnProperty(name = ["IS_CLIENT"], havingValue = "true")
class ClientPubSub {
  companion object {
    const val STOP_TOPIC_NAME = "stop"
    const val PUB_SUB_NAME = "client_pubsub"

    fun stop(client: DaprClient): Mono<Void> {
      return client.publishEvent(
        PUB_SUB_NAME,
        STOP_TOPIC_NAME,
        Unit,
      )
    }
  }

  var startTime: Instant? = null

  @Topic(
    name = PhilosopherPubSub.Companion.START_TOPIC_NAME,
    pubsubName = PhilosopherPubSub.Companion.PUB_SUB_NAME,
  )
  @PostMapping("/start")
  fun startSubscriber() {
    startTime = Instant.now()
    println("Simulation started at: $startTime")
  }

  @Topic(name = STOP_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/clientStop")
  fun stopSubscriber() {
    val endTime = Instant.now()
    startTime?.let { start ->
      val duration = Duration.between(start, endTime)
      val seconds = duration.toSeconds()
      val millis = duration.toMillis() % 1000
      println("Finished! Total Duration: ${seconds}s ${millis}ms")
    } ?: println("Stop received, but start is missing!")
  }
}
