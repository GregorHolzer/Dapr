package ac.at.uibk.dps.csm.dapr.diningphilosophers.actors

import com.fasterxml.jackson.annotation.JsonProperty

class PhilosopherMessage (
  @param:JsonProperty("position") val position: Int,
  @param:JsonProperty("id") val id: String
)