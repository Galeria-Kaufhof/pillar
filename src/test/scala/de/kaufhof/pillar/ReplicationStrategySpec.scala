package de.kaufhof.pillar

import com.typesafe.config.ConfigException.BadValue
import com.typesafe.config.{ConfigException, ConfigFactory}
import org.scalatest.{FlatSpec, Matchers}

class ReplicationStrategySpec extends FlatSpec with Matchers {
  private val configuration = ConfigFactory.load()
  private val datastore = "test"

  behavior of "A configuration with no replication strategy"
  it should "return a SimpleStrategy object with a replication factor of 3" in {
    val goodCase = ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "test")
    goodCase shouldBe a [SimpleStrategy]
    goodCase match {
      case s: SimpleStrategy => s.replicationFactor should equal(3)
      case _ =>
    }
  }

  behavior of "A valid SimpleStrategy configuration"
  it should "return a SimpleStrategy object" in {
    val goodCase = ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "simpleGood")
    goodCase shouldBe a [SimpleStrategy]
    goodCase match {
      case s: SimpleStrategy => s.replicationFactor should equal(1)
      case _ =>
    }
  }

  behavior of "A strategy configuration with an invalid strategy string value"
  it should "return a ReplicationStrategyConfigError exception" in {
    intercept[ReplicationStrategyConfigError] {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "simpleBadStrat")
    }
  }

  behavior of "A simple strategy configuration with a non-numeric replication factor"
  it should "return a ConfigException.WrongType exception" in {
    intercept[ConfigException.WrongType] {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "simpleBadRep")
    }
  }

  behavior of "A simple strategy configuration with no replication factor"
  it should "return a ConfigException.Missing exception" in {
    intercept[ConfigException.Missing] {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "simpleMissingRep")
    }
  }

  behavior of "A simple strategy configuration with no replication factor"
  it should "return an BadValue exception" in {
    intercept[BadValue] {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "simpleZeroRep")
    }
  }

  behavior of "A valid network topology strategy configuration"
  it should "return a NetworkTopology object with the configured values" in {
    val goodCase = ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "netGood")
    goodCase shouldBe a [NetworkTopologyStrategy]
    goodCase match {
      case n: NetworkTopologyStrategy =>
        n.dataCenters.length should equal(2)
        n.dataCenters(0).name should equal("dc1")
        n.dataCenters(0).replicationFactor should equal(2)
        n.dataCenters(1).name should equal("dc2")
        n.dataCenters(1).replicationFactor should equal(3)
      case _ =>
    }
  }
  
  behavior of "A network topology strategy configuration with an empty replication factor array"
  it should "return an BadValue exception" in {
    intercept[BadValue] {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "netEmptyRep")
    }
  }

  behavior of "A network topology strategy configuration with a datacenter with a replication factor of 0"
  it should "return an BadValue exception" in {
    intercept[BadValue] {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, datastore, "netZeroRep")
    }
  }
}
