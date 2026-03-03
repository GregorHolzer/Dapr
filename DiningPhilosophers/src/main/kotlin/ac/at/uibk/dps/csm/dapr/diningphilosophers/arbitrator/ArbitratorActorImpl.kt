package ac.at.uibk.dps.csm.dapr.diningphilosophers.arbitrator

import ac.at.uibk.dps.csm.dapr.diningphilosophers.ClientSubscriber
import ac.at.uibk.dps.csm.dapr.diningphilosophers.philosopher.PhilosopherSubscriber
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import reactor.core.publisher.Mono

class ArbitratorActorImpl(
  runtimeContext: ActorRuntimeContext<ArbitratorActorImpl>,
  id: ActorId,
  val numberOfPhilosophers: Int,
  val eatingRounds: Int,
  val client: DaprClient,
) : AbstractActor(runtimeContext, id), ArbitratorActor {

  private val forks = Array(numberOfPhilosophers) { true }
  private var waitingPhilosophers = Array(numberOfPhilosophers) { false }
  private var donePhilosophers: Int = 0

  override fun requestForks(position: Int): Mono<Void> {
    if (position in 0 until numberOfPhilosophers) {
      tryAssignForks(position)
    } else {
      println("Received invalid philosopher position: $position")
    }
    return Mono.empty()
  }

  private fun tryAssignForks(position: Int) {
    val nextForkIdx = (position + 1) % numberOfPhilosophers
    if (forks[position] && forks[nextForkIdx]) {
      forks[position] = false
      forks[nextForkIdx] = false
      waitingPhilosophers[position] = false
      client
        .publishEvent(
          PhilosopherSubscriber.PUB_SUB_NAME,
          PhilosopherSubscriber.EAT_TOPIC_NAME,
          position,
        )
        .subscribe()
    } else {
      waitingPhilosophers[position] = true
    }
  }

  override fun doneEating(philosopherPosition: Int): Mono<Void> {
    donePhilosophers++
    if (donePhilosophers >= numberOfPhilosophers * eatingRounds) {
      println("All philosophers have eaten $eatingRounds times!")
      return client.publishEvent(
        ClientSubscriber.PUB_SUB_NAME,
        ClientSubscriber.STOP_TOPIC_NAME,
        Unit,
      )
    }
    val nextPhilosopherIdx = (philosopherPosition + 1) % numberOfPhilosophers
    val prevPhilosopherIdx = (philosopherPosition - 1 + numberOfPhilosophers) % numberOfPhilosophers
    forks[philosopherPosition] = true
    forks[(philosopherPosition + 1) % numberOfPhilosophers] = true
    if (waitingPhilosophers[nextPhilosopherIdx]) {
      tryAssignForks(nextPhilosopherIdx)
    }
    if (waitingPhilosophers[prevPhilosopherIdx]) {
      tryAssignForks(prevPhilosopherIdx)
    }
    if (waitingPhilosophers[philosopherPosition]) {
      tryAssignForks(philosopherPosition)
    }
    return Mono.empty()
  }
}
