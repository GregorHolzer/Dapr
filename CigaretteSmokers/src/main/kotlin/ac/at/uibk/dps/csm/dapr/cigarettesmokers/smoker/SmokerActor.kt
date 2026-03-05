package ac.at.uibk.dps.csm.dapr.cigarettesmokers.smoker

import io.dapr.actors.ActorMethod
import io.dapr.actors.ActorType
import reactor.core.publisher.Mono

@ActorType(name = "SmokerActor")
interface SmokerActor {

  @ActorMethod(name = "startSmoking") fun startSmoking(): Mono<Void>
}
