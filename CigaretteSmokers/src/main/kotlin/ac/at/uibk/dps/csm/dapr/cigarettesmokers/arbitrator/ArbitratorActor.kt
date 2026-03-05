package ac.at.uibk.dps.csm.dapr.cigarettesmokers.arbitrator

import io.dapr.actors.ActorMethod
import io.dapr.actors.ActorType
import reactor.core.publisher.Mono

@ActorType(name = "Arbitrator")
interface ArbitratorActor {

  @ActorMethod(name = "start") fun start(): Mono<Void>

  @ActorMethod(name = "startedSmoking") fun startedSmoking(): Mono<Void>
}
