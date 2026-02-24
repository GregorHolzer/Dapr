package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.ClientSub
import ac.at.uibk.dps.csm.dapr.diningphilosophers.subsciber.PhilosopherSub
import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import io.dapr.client.DaprClient
import reactor.core.publisher.Mono

class ArbitratorActorImpl(
  runtimeContext: ActorRuntimeContext<ArbitratorActorImpl>,
  id: ActorId,
  val numberOfPhilosophers: Int,
  val client: DaprClient
): AbstractActor(runtimeContext, id), ArbitratorActor {

  private val forks = Array(numberOfPhilosophers) { true }
  private var waitingPhilosophers: MutableSet<Int> = HashSet()
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
      waitingPhilosophers.remove(position)
      client.publishEvent(
        PhilosopherSub.PUB_SUB_NAME,
        PhilosopherSub.EAT_TOPIC_NAME,
        position
      ).subscribe()
    } else {
      println("Adding actor at position $position to waiting list...")
      waitingPhilosophers.add(position)
    }
  }

  override fun doneEating(philosopherPosition: Int): Mono<Void> {
    donePhilosophers++
    if (donePhilosophers >= numberOfPhilosophers) {
      println("All philosophers have eaten!")
      return client.publishEvent(
        ClientSub.PUB_SUB_NAME,
        ClientSub.STOP_TOPIC_NAME,
        Unit
      )
    }
    val nextPhilosopherIdx = (philosopherPosition + 1) % numberOfPhilosophers
    val prevPhilosopherIdx = (philosopherPosition - 1 + numberOfPhilosophers) % numberOfPhilosophers
    forks[philosopherPosition] = true
    forks[(philosopherPosition + 1) % numberOfPhilosophers] = true  // free the correct fork

    if (waitingPhilosophers.contains(nextPhilosopherIdx)) {
      tryAssignForks(nextPhilosopherIdx)
    }
    if(waitingPhilosophers.contains(prevPhilosopherIdx)) {
      tryAssignForks(prevPhilosopherIdx)
    }
    return Mono.empty()
  }
}