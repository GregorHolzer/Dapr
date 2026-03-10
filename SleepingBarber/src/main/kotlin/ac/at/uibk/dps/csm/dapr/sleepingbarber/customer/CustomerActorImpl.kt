package ac.at.uibk.dps.csm.dapr.sleepingbarber.customer

import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import io.micrometer.core.instrument.Metrics
import reactor.core.publisher.Mono

class CustomerActorImpl(
  runtimeContext: ActorRuntimeContext<CustomerActorImpl>,
  val customerId: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, ActorId(customerId.toString())), CustomerActor {

  companion object {
    const val COUNTER_NAME = "customer_rounds"
  }

  var completedRounds = 0

  var metricsCounter = Metrics.counter(COUNTER_NAME, "customer", customerId.toString())

  override fun enterWaitingRoom(): Mono<Void> {
    return WaitingRoomPubSub.newCustomer(client, customerId)
  }

  override fun waitingRoomFull(): Mono<Void> {
    return WaitingRoomPubSub.newCustomer(client, customerId)
  }

  override fun doneCutting(): Mono<Void> {
    completedRounds++
    metricsCounter.increment()
    return enterWaitingRoom()
  }
}
