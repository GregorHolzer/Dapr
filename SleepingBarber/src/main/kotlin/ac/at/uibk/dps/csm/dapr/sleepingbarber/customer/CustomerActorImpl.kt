package ac.at.uibk.dps.csm.dapr.sleepingbarber.customer

import ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom.WaitingRoomSubscriber
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Metrics
import reactor.core.publisher.Mono

class CustomerActorImpl(
  runtimeContext: ActorRuntimeContext<CustomerActorImpl>,
  val customerId: Int,
  val cuttingRounds: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, ActorId(customerId.toString())), CustomerActor {

  var completedRounds = 0

  var metricsCounter = Metrics.counter("customer_rounds")

  override fun enterWaitingRoom(): Mono<Void> {
    println("Customer $customerId entering waiting room")
    return client.publishEvent(
      WaitingRoomSubscriber.PUB_SUB_NAME,
      WaitingRoomSubscriber.NEW_CUSTOMER_TOPIC,
      customerId,
    )
  }

  override fun waitingRoomFull(): Mono<Void> {
    println("Customer $customerId received full waiting room")
    return client.publishEvent(
      WaitingRoomSubscriber.PUB_SUB_NAME,
      WaitingRoomSubscriber.NEW_CUSTOMER_TOPIC,
      customerId,
    )
  }

  override fun doneCutting(): Mono<Void> {
    completedRounds++
    metricsCounter.increment()
    println("Customer $customerId completed $completedRounds rounds")
    return enterWaitingRoom()
    /*if (completedRounds < cuttingRounds) {
      return enterWaitingRoom()
    }
    return client.publishEvent(
      WaitingRoomSubscriber.PUB_SUB_NAME,
      WaitingRoomSubscriber.CUSTOMER_FINISHED_TOPIC,
      customerId,
    )*/
  }
}
