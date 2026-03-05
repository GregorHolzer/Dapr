package ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom

import ac.at.uibk.dps.csm.dapr.sleepingbarber.barber.BarberPubSub
import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import java.util.LinkedList
import java.util.Queue
import reactor.core.publisher.Mono

class WaitingRoomActorImpl(
  runtimeContext: ActorRuntimeContext<WaitingRoomActorImpl>,
  id: ActorId,
  val roomSlots: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, id), WaitingRoomActor {

  var barberBusy = false

  val queue: Queue<Int> = LinkedList()

  override fun newCustomerArrives(customerId: Int): Mono<Void> {
    if (roomSlots <= queue.size) {
      return CustomerPubSub.full(client, customerId)
    } else if (!queue.isEmpty() || barberBusy) {
      queue.add(customerId)
    } else {
      barberBusy = true
      return BarberPubSub.cutting(client, customerId)
    }
    return Mono.empty()
  }

  override fun barberFinished(): Mono<Void> {
    if (queue.isEmpty()) {
      barberBusy = false
      return Mono.empty()
    } else {
      val nextId = queue.poll()
      return BarberPubSub.cutting(client, nextId)
    }
  }
}
