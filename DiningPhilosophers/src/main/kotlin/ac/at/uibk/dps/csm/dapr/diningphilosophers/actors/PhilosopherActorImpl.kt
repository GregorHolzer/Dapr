package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.ArbitratorSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import reactor.core.publisher.Mono
import java.time.Duration

class PhilosopherActorImpl(
  runtimeContext: ActorRuntimeContext<PhilosopherActorImpl>,
  val tablePosition: Int,
  val client: DaprClient
): AbstractActor(runtimeContext, ActorId(tablePosition.toString())), PhilosopherActor {


  override fun start(): Mono<Void> {
    println("Created Philosopher with position $tablePosition")
    return client.publishEvent(
      ArbitratorSub.PUB_SUB_NAME,
      ArbitratorSub.REQUEST_FORKS_TOPIC_NAME,
      tablePosition
    )
  }

  override fun eat(): Mono<Void> {
    return Mono.delay(Duration.ofSeconds(1)).flatMap {
      println("Philosopher at position $tablePosition has eaten")
      client.publishEvent(
        ArbitratorSub.PUB_SUB_NAME,
        ArbitratorSub.DONE_EATING_TOPIC_NAME,
        tablePosition
      )
    }
  }
}