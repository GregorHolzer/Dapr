package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import io.dapr.actors.ActorMethod
import io.dapr.actors.ActorType
import reactor.core.publisher.Mono

@ActorType(name = "PhilosopherActor")
interface PhilosopherActor {

  @ActorMethod(name = "eat")
  fun eat(): Mono<Void>

  @ActorMethod(name = "start")
  fun start(): Mono<Void>
}