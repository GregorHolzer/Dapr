package ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber

import ac.at.uibk.dps.csm.dapr.diningphilosophers.actors.ArbitratorActor
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
import reactor.core.publisher.Mono

@RestController
@ConditionalOnProperty(name = ["RUN_ARBITRATOR_SUB"], havingValue = "true")
class ArbitratorSub {

  companion object{
    const val REQUEST_FORKS_TOPIC_NAME = "requestForks"
    const val DONE_EATING_TOPIC_NAME = "doneEating"
    const val PUB_SUB_NAME = "arbitrator_pub_sub"
    const val ARBITRATOR_NAME = "arbitrator"
  }

  val client = ActorClient()

  val arbitratorActor: ArbitratorActor? = ActorProxyBuilder(ArbitratorActor::class.java, client).build(ActorId(ARBITRATOR_NAME))

  @Topic(name = REQUEST_FORKS_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/requestForks")
  fun requestForks(@RequestBody(required = true) event: CloudEvent<Int>): Mono<ResponseEntity<Unit>> {
    return arbitratorActor!!.requestForks(event.data).thenReturn(ResponseEntity<Unit>(HttpStatus.OK))
  }

  @Topic(name = DONE_EATING_TOPIC_NAME, pubsubName = PUB_SUB_NAME)
  @PostMapping("/doneEating")
  fun doneEating(@RequestBody(required = true) event: CloudEvent<Int>): Mono<ResponseEntity<Unit>> {
    return arbitratorActor!!.doneEating(event.data).thenReturn(ResponseEntity<Unit>(HttpStatus.OK))
  }
}