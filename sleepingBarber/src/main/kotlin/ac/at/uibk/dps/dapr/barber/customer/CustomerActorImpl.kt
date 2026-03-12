package ac.at.uibk.dps.dapr.barber.customer

import ac.at.uibk.dps.dapr.barber.SleepingBarber
import ac.at.uibk.dps.dapr.barber.waitingroom.WaitingRoomPubSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.micrometer.core.instrument.Metrics
import reactor.core.publisher.Mono

class CustomerActorImpl(runtimeContext: ActorRuntimeContext<CustomerActorImpl>, id: ActorId) :
  AbstractActor(runtimeContext, id), CustomerActor {

  companion object {
    const val COUNTER_NAME = "customer_rounds"
  }

  val customerId = System.getenv("CUSTOMER_ID")?.toInt() ?: 0

  var completedRounds = 0

  var metricsCounter = Metrics.counter(COUNTER_NAME, "customer", customerId.toString())

  override fun enterWaitingRoom(): Mono<Void> {
    return WaitingRoomPubSub.newCustomer(SleepingBarber.daprClient, customerId)
  }

  override fun waitingRoomFull(): Mono<Void> {
    return WaitingRoomPubSub.newCustomer(SleepingBarber.daprClient, customerId)
  }

  override fun doneCutting(): Mono<Void> {
    completedRounds++
    metricsCounter.increment()
    return enterWaitingRoom()
  }
}
