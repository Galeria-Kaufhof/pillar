package de.kaufhof.pillar

import java.util.Date

import com.datastax.driver.core.Session
import de.kaufhof.pillar.cli.{Command, CommandExecutor, Initialize, Migrate}
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSpec}

class CommandExecutorSpec extends FunSpec with BeforeAndAfter with MockitoSugar {
  describe("#execute") {
    val session = mock[Session]
    val keyspace = "myks"
    val registry = mock[Registry]
    val reporter = mock[Reporter]
    val migrator = mock[Migrator]
    val simpleStrategy = SimpleStrategy()
    val networkTopologyStrategy = NetworkTopologyStrategyTestData.networkTopologyStrategy
    val migratorConstructor = mock[((Registry, Reporter) => Migrator)]
    stub(migratorConstructor.apply(registry, reporter)).toReturn(migrator)
    val executor = new CommandExecutor()(migratorConstructor)

    describe("an initialize action") {
      val commandSimple = Command(Initialize, session, keyspace, None, registry, simpleStrategy)

      executor.execute(commandSimple, reporter)

      it("initializes a simple strategy") {
        verify(migrator).initialize(session, keyspace, simpleStrategy)
      }

      val commandNetwork = Command(Initialize, session, keyspace, None, registry, networkTopologyStrategy)

      executor.execute(commandNetwork, reporter)

      it("initializes a network topology strategy") {
        verify(migrator).initialize(session, keyspace, networkTopologyStrategy)
      }
    }

    describe("a migrate action without date restriction") {
      val command = Command(Migrate, session, keyspace, None, registry, simpleStrategy)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(session, None)
      }
    }

    describe("a migrate action with date restriction") {
      val date = new Date()
      val command = Command(Migrate, session, keyspace, Some(date.getTime), registry, simpleStrategy)

      executor.execute(command, reporter)

      it("migrates") {
        verify(migrator).migrate(session, Some(date))
      }
    }
  }
}