package ac.at.uibk.dps.csm.dapr.pingpong.actors

import io.dapr.actors.ActorMethod
import io.dapr.actors.ActorType

@ActorType(name = "PongActor")
interface PongActor {

  @ActorMethod(name = "ping")
  fun ping(msg: PingPongMessage)
}