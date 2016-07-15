package de.kaufhof.pillar

sealed trait ReplicationStrategy {
  override def toString: String
}

final case class SimpleStrategy(replicationFactor: Int = 3) extends ReplicationStrategy {
  require(replicationFactor > 0)

  override def toString: String = s"{'class' : 'SimpleStrategy', 'replication_factor' : $replicationFactor}"
}

final case class NetworkTopologyStrategy(dataCenters: Seq[CassandraDataCenter]) extends ReplicationStrategy {
  require(dataCenters.nonEmpty)

  override def toString: String = {
    val replicationFacString = dataCenters.map { dc =>
      s"'${dc.name}' : ${dc.replicationFactor} "
    }.mkString(", ")

    s"{'class' : 'NetworkTopologyStrategy', $replicationFacString }"
  }
}

final case class CassandraDataCenter(name: String, replicationFactor: Int){
  require(replicationFactor > 0 && name.nonEmpty)
}
