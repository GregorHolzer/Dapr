package ac.at.uibk.dps.csm.dapr.sleepingbarber.barber

import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerPubSub
import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import reactor.core.publisher.Mono

class BarberActorImpl(
  runtimeContext: ActorRuntimeContext<BarberActorImpl>,
  id: ActorId,
  val cuttingTimeMS: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, id), BarberActor {

  override fun cuttingHair(customerId: Int): Mono<Void> {
    return Mono.delay(java.time.Duration.ofMillis(cuttingTimeMS.toLong())).flatMap {
      Mono.`when`(WaitingRoomPubSub.barberFinished(client), CustomerPubSub.done(client, customerId))
    }
  }
}
