package ac.at.uibk.dps.csm.dapr.sleepingbarber.waitingroom

import io.dapr.Topic
import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.client.domain.CloudEvent
import kotlin.jvm.java
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
@ConditionalOnProperty(name = ["HOST_WAITING_ROOM_SUB"], havingValue = "true")
class WaitingRoomSubscriber(client: ActorClient) {
  companion object {
    const val PUB_SUB_NAME = "waiting_room_pub_sub"
    const val NEW_CUSTOMER_TOPIC = "new_customer_topic"
    const val BARBER_FINISHED_TOPIC = "barber_finished_topic"
    const val CUSTOMER_FINISHED_TOPIC = "customer_finished_topic"
    const val WAITING_ROOM_NAME = "waitingRoom"
  }

  val waitingRoomActor: WaitingRoomActor? =
    ActorProxyBuilder(WaitingRoomActor::class.java, client).build(ActorId(WAITING_ROOM_NAME))

  @Topic(name = NEW_CUSTOMER_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$NEW_CUSTOMER_TOPIC")
  fun newCustomer(@RequestBody(required = true) event: CloudEvent<Int>) {
    waitingRoomActor!!.newCustomerArrives(event.data).subscribe()
  }

  @Topic(name = BARBER_FINISHED_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$BARBER_FINISHED_TOPIC")
  fun barberFinished() {
    waitingRoomActor!!.barberFinished().subscribe()
  }

  @Topic(name = CUSTOMER_FINISHED_TOPIC, pubsubName = PUB_SUB_NAME)
  @PostMapping("/$CUSTOMER_FINISHED_TOPIC")
  fun customerFinished(@RequestBody(required = true) event: CloudEvent<Int>) {
    waitingRoomActor!!.customerFinished(event.data).subscribe()
  }
}
