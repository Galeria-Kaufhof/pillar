package de.kaufhof.pillar.cli

import java.io.File

import com.datastax.driver.core.{ConsistencyLevel, QueryOptions, Cluster}
import com.typesafe.config.ConfigFactory
import de.kaufhof.pillar._
import de.kaufhof.pillar.config.ConnectionConfiguration

object App {
  def apply(reporter: Reporter = new PrintStreamReporter(System.out)): App = {
    new App(reporter)
  }

  def main(arguments: Array[String]) {
    try {
      App().run(arguments)
    } catch {
      case exception: Exception =>
        System.err.println(exception.getMessage)
        System.exit(1)
    }

    System.exit(0)
  }
}

class App(reporter: Reporter) {
  private val configuration = ConfigFactory.load()

  def run(arguments: Array[String]) {
    val commandLineConfiguration = CommandLineConfiguration.buildFromArguments(arguments)
    val registry = Registry.fromDirectory(new File(commandLineConfiguration.migrationsDirectory, commandLineConfiguration.dataStore), reporter)
    val dataStoreName = commandLineConfiguration.dataStore
    val environment = commandLineConfiguration.environment

    val cassandraConfiguration = new ConnectionConfiguration(dataStoreName, environment, configuration)

    val cluster:Cluster = createCluster(cassandraConfiguration)

    val session = commandLineConfiguration.command match {
      case Initialize => cluster.connect()
      case _ => cluster.connect(cassandraConfiguration.keyspace)
    }

    val replicationOptions = try {
      ReplicationStrategyBuilder.getReplicationStrategy(configuration, dataStoreName, environment)
    } catch {
      case e: Exception => throw e
    }

    // TODO: Command shouldn't be the sole point of entry when passing things into a migration.
    // TODO: This should be refactored at some point.
    val command = Command(
      commandLineConfiguration.command,
      session,
      cassandraConfiguration.keyspace,
      commandLineConfiguration.timeStampOption,
      registry,
      replicationOptions)

    try {
      CommandExecutor().execute(command, reporter)
    } finally {
      session.close()
    }
  }

  private def createCluster(connectionConfiguration:ConnectionConfiguration): Cluster = {
    val queryOptions = new QueryOptions()
    queryOptions.setConsistencyLevel(ConsistencyLevel.QUORUM)

    val clusterBuilder = Cluster.builder()
      .addContactPoint(connectionConfiguration.seedAddress)
      .withPort(connectionConfiguration.port)
      .withQueryOptions(queryOptions)
    connectionConfiguration.auth.foreach(clusterBuilder.withAuthProvider)

    connectionConfiguration.sslConfig.foreach(_.setAsSystemProperties())
    if (connectionConfiguration.useSsl)
      clusterBuilder.withSSL()

    clusterBuilder.build()
  }
}
