package ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher

import ac.at.uibk.dps.csm.dapr.diningphilosophers.arbitrator.ArbitratorPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import io.micrometer.core.instrument.Metrics
import java.time.Duration
import reactor.core.publisher.Mono

class PhilosopherActorImpl(
  runtimeContext: ActorRuntimeContext<PhilosopherActorImpl>,
  val tablePosition: Int,
  val eatingDuration: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, ActorId(tablePosition.toString())), PhilosopherActor {

  companion object {
    const val COUNTER_NAME = "total_meals"
  }

  var completedRounds: Int = 0

  var metricsCounter = Metrics.counter(COUNTER_NAME)

  override fun start(): Mono<Void> {
    return ArbitratorPubSub.requestForks(client, tablePosition)
  }

  override fun eat(): Mono<Void> {
    completedRounds++
    metricsCounter.increment()
    val delay =
      Mono.delay(Duration.ofMillis(eatingDuration.toLong())).flatMap {
        ArbitratorPubSub.doneEating(client, tablePosition)
      }
    return delay.then(ArbitratorPubSub.requestForks(client, tablePosition))
  }
}
