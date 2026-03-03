package ac.at.uibk.dps.csm.dapr.sleepingbarber.barber

import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerSubscriber
import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomSubscriber
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
    println("Barber is cutting hair of customer $customerId")
    return Mono.delay(java.time.Duration.ofMillis(cuttingTimeMS.toLong())).flatMap {
      Mono.`when`(
        client.publishEvent(
          CustomerSubscriber.PUB_SUB_NAME,
          CustomerSubscriber.DONE_TOPIC,
          customerId,
        ),
        client.publishEvent(
          WaitingRoomSubscriber.PUB_SUB_NAME,
          WaitingRoomSubscriber.BARBER_FINISHED_TOPIC,
          Unit,
        ),
      )
    }
  }
}
