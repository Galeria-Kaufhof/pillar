package de.kaufhof.pillar

import java.util.Map.Entry

import com.typesafe.config.ConfigException.BadValue
import com.typesafe.config.{Config, ConfigException, ConfigObject, ConfigValue}

import scala.util.{Failure, Success, Try}

final case class ReplicationStrategyConfigError(msg: String) extends Exception

object ReplicationStrategyBuilder {

  /**
    * Parses replication settings from a config that looks like:
    * {{{
    *   replicationStrategy: "SimpleStrategy"
    *   replicationFactor: 3
    * }}}
    *
    * or:
    *
    * {{{
    *   replicationStrategy: "NetworkTopologyStrategy"
    *   replicationFactor: [
    *     {dc1: 3},
    *     {dc2: 3}
    *   ]
    * }}}
    *
    * @param configuration The applications Typesafe config
    * @param dataStoreName The target data store, as defined in application.conf
    * @param environment The environment, as defined in application.conf (i.e. "pillar.dataStoreName.environment {...})
    * @return ReplicationOptions with a default of Simple Strategy with a replication factor of 3.
    */
  def getReplicationStrategy(configuration: Config, dataStoreName: String, environment: String): ReplicationStrategy = try {

    val repStrategyStr = Try(configuration.getString(s"pillar.$dataStoreName.$environment.replicationStrategy"))

    repStrategyStr match {
      case Success(repStrategy) => repStrategy match {
        case "SimpleStrategy" =>
          val repFactor = configuration.getInt(s"pillar.$dataStoreName.$environment.replicationFactor")
          SimpleStrategy(repFactor)

        case "NetworkTopologyStrategy" =>
          import scala.collection.JavaConverters._
          val dcConfigBuffer = configuration
            .getObjectList(s"pillar.$dataStoreName.$environment.replicationFactor")
            .asScala

          val dcBuffer = for {
            item: ConfigObject <- dcConfigBuffer
            entry: Entry[String, ConfigValue] <- item.entrySet().asScala
            dcName = entry.getKey
            dcRepFactor = entry.getValue.unwrapped().toString.toInt
          } yield (dcName, dcRepFactor)

          val datacenters = dcBuffer
            .map(dc => CassandraDataCenter(dc._1, dc._2))
            .toList

          NetworkTopologyStrategy(datacenters)

        case _ =>
          throw new ReplicationStrategyConfigError(s"$repStrategy is not a valid replication strategy.")
      }

      case Failure(e: ConfigException.Missing) => SimpleStrategy()
      case Failure(e) => throw e
    }
  } catch {
    case e: IllegalArgumentException => throw new BadValue(s"pillar.$dataStoreName.$environment", e.getMessage)
    case e: Exception => throw e
  }
}
