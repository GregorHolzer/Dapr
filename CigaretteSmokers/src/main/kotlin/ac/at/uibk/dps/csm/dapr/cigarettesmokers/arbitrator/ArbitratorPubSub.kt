package ac.at.uibk.dps.csm.dapr.cigarettesmokers.arbitrator

import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.DaprClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
class ArbitratorPubSub(client: ActorClient) {

  companion object {
    const val PUB_SUB_NAME = "arbitrator_pub_sub"
    const val START_TOPIC = "start"
    const val STARTED_SMOKING_TOPIC = "started_smoking"
    const val ARBITRATOR_NAME = "arbitrator"

    fun start(client: DaprClient): Mono<Void> {
      return client.publishEvent(PUB_SUB_NAME, START_TOPIC, Unit)
    }

    fun startedSmoking(client: DaprClient): Mono<Void> {
      return client.publishEvent(PUB_SUB_NAME, STARTED_SMOKING_TOPIC, Unit)
    }
  }

  val arbitrator: ArbitratorActor? =
    ActorProxyBuilder(ArbitratorActor::class.java, client).build(ActorId(ARBITRATOR_NAME))

  @Topic(name = START_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$START_TOPIC")
  fun startSubscriber() {
    arbitrator!!.start().subscribe()
  }

  @Topic(name = STARTED_SMOKING_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$STARTED_SMOKING_TOPIC")
  fun startedSmokingSubscriber() {
    arbitrator!!.startedSmoking().subscribe()
  }
}
