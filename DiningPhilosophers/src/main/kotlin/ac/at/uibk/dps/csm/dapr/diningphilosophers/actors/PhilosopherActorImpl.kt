package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import io.dapr.actors.ActorId
import io.dapr.actors.client.ActorClient
import io.dapr.actors.client.ActorProxyBuilder
import io.dapr.actors.runtime.AbstractActor
import io.dapr.actors.runtime.ActorRuntimeContext
import reactor.core.publisher.Mono
import java.time.Duration

class PhilosopherActorImpl(
  runtimeContext: ActorRuntimeContext<PhilosopherActorImpl>,
  val tablePosition: Int,
  arbitratorId: ActorId,
  client: ActorClient
): AbstractActor(runtimeContext, ActorId(tablePosition.toString())), PhilosopherActor {

  val arbitratorProxy: ArbitratorActor = ActorProxyBuilder(ArbitratorActor::class.java, client)
    .build(arbitratorId)

  override fun start(): Mono<Void> {
    println("Created Philosopher with position $tablePosition")
    val msg = PhilosopherMessage(tablePosition, this.id.toString())
    return arbitratorProxy.requestForks(msg)
  }

  override fun eat(): Mono<Void> {
    return Mono.delay(Duration.ofSeconds(1))
      .then(Mono.fromRunnable<Void> {
        println("Philosopher at position $tablePosition has eaten")
      })
      .then(arbitratorProxy.doneEating(tablePosition))
  }
}