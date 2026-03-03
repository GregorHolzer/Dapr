package ac.at.uibk.dps.csm.dapr.sleepingbarber.barber

import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.domain.CloudEvent
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["HOST_BARBER_SUB"], havingValue = "true")
class BarberSubscriber(client: ActorClient) {

  companion object {
    const val PUB_SUB_NAME = "barber_pub_sub"
    const val CUTTING_TOPIC = "cutting"
    const val BARBER_NAME = "barber"
  }

  val barberActor: BarberActor? =
    ActorProxyBuilder(BarberActor::class.java, client).build(ActorId(BARBER_NAME))

  @Topic(name = CUTTING_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$CUTTING_TOPIC")
  fun cutting(@RequestBody event: CloudEvent<Int>) {
    barberActor!!.cuttingHair(event.data).subscribe()
  }
}
