package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import io.dapr.actors.ActorMethod
import io.dapr.actors.ActorType
import reactor.core.publisher.Mono

@ActorType(name = "ArbitratorActor")
interface ArbitratorActor {

  @ActorMethod(name= "requestForks")
  fun requestForks(position: Int): Mono<Void>

  @ActorMethod(name= "doneEating")
  fun doneEating(philosopherPosition: Int): Mono<Void>

}