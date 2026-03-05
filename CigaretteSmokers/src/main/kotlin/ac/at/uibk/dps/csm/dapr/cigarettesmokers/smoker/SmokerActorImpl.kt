package ac.at.uibk.dps.csm.dapr.cigarettesmokers.smoker

import ac.at.uibk.dps.csm.dapr.cigarettesmokers.arbitrator.ArbitratorPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import java.time.Duration
import reactor.core.publisher.Mono

class SmokerActorImpl(
  runtimeContext: ActorRuntimeContext<SmokerActorImpl>,
  id: ActorId,
  val smokingTimeMs: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, id), SmokerActor {

  override fun startSmoking(): Mono<Void> {
    return Mono.delay(Duration.ofMillis(smokingTimeMs.toLong())).flatMap {
      ArbitratorPubSub.startedSmoking(client)
    }
  }
}
