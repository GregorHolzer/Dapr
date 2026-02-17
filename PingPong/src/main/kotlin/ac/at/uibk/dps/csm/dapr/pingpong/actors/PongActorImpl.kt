package ac.at.uibk.dps.csm.dapr.pingpong.actors

import io.dapr.actors.ActorId
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext

class PongActorImpl(
  runtimeContext: ActorRuntimeContext<PingActorImpl>, id: String
): AbstractActor(runtimeContext, ActorId("PongActor_$id")), PongActor {

  override fun ping(msg: PingPongMessage) {
    msg.value--
    //Call pong function
  }
}