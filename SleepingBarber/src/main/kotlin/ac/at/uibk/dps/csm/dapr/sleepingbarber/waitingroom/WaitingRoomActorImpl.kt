package ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom

import ac.at.uibk.dps.csm.dapr.sleepingbarber.ClientSubscriber
import ac.at.uibk.dps.csm.dapr.sleepingbarber.barber.BarberSubscriber
import ac.at.uibk.dps.csm.dapr.sleepingbarber.customer.CustomerSubscriber
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
  val numberOfCustomers: Int,
  val roomSlots: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, id), WaitingRoomActor {

  var barberBusy = false

  val queue: Queue<Int> = LinkedList()

  var finishedCustomers = 0

  override fun newCustomerArrives(customerId: Int): Mono<Void> {
    if (roomSlots <= queue.size) {
      println("Reject customer $customerId: Queue is Full")
      return client.publishEvent(
        CustomerSubscriber.PUB_SUB_NAME,
        CustomerSubscriber.FULL_TOPIC,
        customerId,
      )
    } else if (!queue.isEmpty() || barberBusy) {
      queue.add(customerId)
      println("Add customer $customerId to queue: Barber busy: $barberBusy, Queue state: $queue")
    } else {
      println("'Customer $customerId served by barber")
      barberBusy = true
      return client.publishEvent(
        BarberSubscriber.PUB_SUB_NAME,
        BarberSubscriber.CUTTING_TOPIC,
        customerId,
      )
    }
    return Mono.empty()
  }

  override fun barberFinished(): Mono<Void> {
    if (queue.isEmpty()) {
      println("Barber finished: Sleeping, Queue state $queue")
      barberBusy = false
      return Mono.empty()
    } else {
      val nextId = queue.poll()
      println("Barber finished: Next Customer with id $nextId, Queue state $queue")
      return client.publishEvent(
        BarberSubscriber.PUB_SUB_NAME,
        BarberSubscriber.CUTTING_TOPIC,
        nextId,
      )
    }
  }

  override fun customerFinished(customerId: Int): Mono<Void> {
    finishedCustomers++
    if (finishedCustomers < numberOfCustomers) {
      return Mono.empty()
    }
    println("All customers finished")
    return client.publishEvent(
      ClientSubscriber.PUB_SUB_NAME,
      ClientSubscriber.STOP_TOPIC_NAME,
      Unit,
    )
  }
}
