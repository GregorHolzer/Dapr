package ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher

import ac.at.uibk.dps.csm.dapr.diningphilosophers.arbitrator.ArbitratorSubscriber
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import java.time.Duration
import reactor.core.publisher.Mono

class PhilosopherActorImpl(
  runtimeContext: ActorRuntimeContext<PhilosopherActorImpl>,
  val tablePosition: Int,
  val eatingRounds: Int,
  val eatingDuration: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, ActorId(tablePosition.toString())), PhilosopherActor {

  var completedRounds: Int = 0

  override fun start(): Mono<Void> {
    println("Created Philosopher with position $tablePosition")
    return client.publishEvent(
      ArbitratorSubscriber.PUB_SUB_NAME,
      ArbitratorSubscriber.REQUEST_FORKS_TOPIC_NAME,
      tablePosition,
    )
  }

  override fun eat(): Mono<Void> {
    completedRounds++
    val delay =
      Mono.delay(Duration.ofMillis(eatingDuration.toLong())).flatMap {
        //
        client.publishEvent(
          ArbitratorSubscriber.PUB_SUB_NAME,
          ArbitratorSubscriber.DONE_EATING_TOPIC_NAME,
          tablePosition,
        )
      }
    if (completedRounds < eatingRounds) {
      return delay.then(
        Mono.defer {
          client.publishEvent(
            ArbitratorSubscriber.PUB_SUB_NAME,
            ArbitratorSubscriber.REQUEST_FORKS_TOPIC_NAME,
            tablePosition,
          )
        }
      )
    }
    println("Philosopher at position $tablePosition is done")
    return delay
  }
}
