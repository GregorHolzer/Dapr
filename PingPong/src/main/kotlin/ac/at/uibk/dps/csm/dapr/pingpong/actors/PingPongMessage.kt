package ac.at.uibk.dps.csm.dapr.pingpong.actors

import io.dapr.actors.ActorId

class PingPongMessage {

  val id: String

  var value: Int

  constructor(value: Int, id: String) {
    this.value = value
    this.id = id
  }
}