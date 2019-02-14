package de.kaufhof.pillar

/**
  * Defines all possible ReplicationStrategy configurations.
  * A NetworkTopologyStrategy will require the appropriate snitch.
  */
sealed trait ReplicationStrategy {
  def cql: String
  override def toString: String = cql
}

final case class SimpleStrategy(replicationFactor: Int = 3) extends ReplicationStrategy {
  require(replicationFactor > 0)

  override def cql: String = s"{'class' : 'SimpleStrategy', 'replication_factor' : $replicationFactor}"
}

final case class NetworkTopologyStrategy(dataCenters: Seq[CassandraDataCenter]) extends ReplicationStrategy {
  require(dataCenters.nonEmpty)

  override def cql: String = {
    val replicationFacString = dataCenters.map { dc =>
      s"'${dc.name}' : ${dc.replicationFactor}"
    }.mkString(" , ")

    s"{'class' : 'NetworkTopologyStrategy', $replicationFacString}"
  }
}

final case class CassandraDataCenter(name: String, replicationFactor: Int){
  require(replicationFactor > 0 && name.nonEmpty)
}
