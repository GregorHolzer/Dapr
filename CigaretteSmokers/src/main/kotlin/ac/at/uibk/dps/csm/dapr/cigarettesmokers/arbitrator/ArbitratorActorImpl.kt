package ac.at.uibk.dps.csm.dapr.cigarettesmokers.arbitrator

import ac.at.uibk.dps.csm.dapr.cigarettesmokers.ClientPubSub
import ac.at.uibk.dps.csm.dapr.cigarettesmokers.smoker.SmokerPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import kotlin.random.Random
import reactor.core.publisher.Mono

class ArbitratorActorImpl(
  runtimeContext: ActorRuntimeContext<ArbitratorActorImpl>,
  id: ActorId,
  val numberOfSmokers: Int,
  val numberOfCigarettes: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, id), ArbitratorActor {

  var smokedCigarettes = 0

  override fun start(): Mono<Void> {
    return selectRandomSmoker()
  }

  override fun startedSmoking(): Mono<Void> {
    return selectRandomSmoker()
  }

  private fun selectRandomSmoker(): Mono<Void> {
    smokedCigarettes++
    if (smokedCigarettes < numberOfCigarettes) {
      val smokerId = Random(numberOfSmokers).nextInt()
      return SmokerPubSub.startSmoking(client, smokerId)
    }
    return ClientPubSub.stop(client)
  }
}
