package de.kaufhof.pillar

object NetworkTopologyStrategyTestData {
  val dc1 = CassandraDataCenter("dc1", 3)
  val dc2 = CassandraDataCenter("dc2", 3)
  val networkTopologyStrategy = NetworkTopologyStrategy(Seq(dc1, dc2))
}
