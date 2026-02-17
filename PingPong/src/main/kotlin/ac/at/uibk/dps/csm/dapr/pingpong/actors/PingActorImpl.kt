package ac.at.uibk.dps.csm.dapr.pingpong.actors

import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext;

class PingActorImpl(
  runtimeContext: ActorRuntimeContext<PingActorImpl>,
  id: String,
  startValue: Int
  ): AbstractActor(runtimeContext, ActorId("PingActor_$id")), PingActor {

  val pongActor: PongActor

  init {
    val client = ActorClient()
    val msg = PingPongMessage(startValue, id)
    val builder: ActorProxyBuilder<PongActor> = ActorProxyBuilder(PongActor::class.java, client)
    pongActor = builder.build(ActorId(id))
    pongActor.ping(msg)
  }

  fun stop(){

  }

  override fun pong(msg: PingPongMessage){
    msg.value--
    if (msg.value > 0){
      pongActor.ping(msg)
    }
    else{
      stop()
    }
  }
}