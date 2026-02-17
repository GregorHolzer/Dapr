package ac.at.uibk.dps.csm.dapr.pingpong.actors

import io.dapr.actors.ActorMethod
import io.dapr.actors.ActorType

@ActorType(name = "PingActor")
interface PingActor {

  @ActorMethod(name = "pong")
  fun pong(msg: PingPongMessage)
}