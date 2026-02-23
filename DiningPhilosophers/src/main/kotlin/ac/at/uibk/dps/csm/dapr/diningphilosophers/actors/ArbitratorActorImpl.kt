package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import reactor.core.publisher.Mono

class ArbitratorActorImpl(
  runtimeContext: ActorRuntimeContext<ArbitratorActorImpl>,
  id: ActorId,
  val numberOfPhilosophers: Int,
  val client: ActorClient
): AbstractActor(runtimeContext, id), ArbitratorActor {

  private val forks = Array(numberOfPhilosophers) { true }
  private var waitingPhilosophers: MutableMap<Int, ActorId> = HashMap(numberOfPhilosophers)
  private var donePhilosophers: Int = 0

  private val philosopherProxy: (ActorId) -> PhilosopherActor = { id ->
    ActorProxyBuilder(PhilosopherActor::class.java, client).build(id)
  }

  override fun requestForks(msg: PhilosopherMessage): Mono<Void> {
    if (msg.position in 0 until numberOfPhilosophers) {
      return tryAssignForks(msg.position, ActorId(msg.id))
    } else {
      println("Received invalid philosopher position: ${msg.position}")
      return Mono.empty()
    }
  }

  private fun tryAssignForks(position: Int, id: ActorId): Mono<Void> {
    val nextForkIdx = (position + 1) % numberOfPhilosophers
    if (forks[position] && forks[nextForkIdx]) {
      forks[position] = false
      forks[nextForkIdx] = false
      waitingPhilosophers.remove(position)
      philosopherProxy(id).eat().subscribe()
      return Mono.empty()
    } else {
      println("Adding actor at position $position to waiting list...")
      waitingPhilosophers[position] = id
      return Mono.empty()
    }
  }

  override fun doneEating(philosopherPosition: Int): Mono<Void> {
    donePhilosophers++
    if (donePhilosophers >= numberOfPhilosophers) {
      println("All philosophers have eaten!")
      return Mono.empty()
    }
    val nextPhilosopherIdx = (philosopherPosition + 1) % numberOfPhilosophers
    val prevPhilosopherIdx = (philosopherPosition - 1 + numberOfPhilosophers) % numberOfPhilosophers
    forks[philosopherPosition] = true
    forks[(philosopherPosition + 1) % numberOfPhilosophers] = true  // free the correct fork

    val nextPhilosopher = waitingPhilosophers[nextPhilosopherIdx]?.let {
      tryAssignForks(nextPhilosopherIdx, it)
    }?: Mono.empty()

    val prevPhilosopher = waitingPhilosophers[prevPhilosopherIdx]?.let {
      tryAssignForks(prevPhilosopherIdx, it)
    }?: Mono.empty()

    return Mono.`when`(nextPhilosopher, prevPhilosopher)
  }
}