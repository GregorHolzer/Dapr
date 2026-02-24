package ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber

import io.dapr.Topic
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Duration
import java.time.Instant

@RestController
@ConditionalOnProperty(name = ["IS_CLIENT"], havingValue = "true")
class ClientSub {
  companion object {
    const val STOP_TOPIC_NAME = "stop"
    const val PUB_SUB_NAME = "client_pubsub"
  }

  var startTime: Instant? = null

  @Topic(name = PhilosopherSub.START_TOPIC_NAME, pubsubName = PhilosopherSub.PUB_SUB_NAME)
  @PostMapping("/start")
  fun start() {
    startTime = Instant.now()
    println("Simulation started at: $startTime")
  }

  @Topic(name = STOP_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/clientStop")
  fun stop() {
    val endTime = Instant.now()
    startTime?.let { start ->
      val duration = Duration.between(start, endTime)
      val seconds = duration.toSeconds()
      val millis = duration.toMillis() % 1000
      println("Finished! Total Duration: ${seconds}s ${millis}ms")
    } ?: println("Stop received, but start is missing!")
  }
}