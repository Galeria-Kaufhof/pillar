package de.kaufhof.pillar

import com.datastax.driver.core.Session
import org.mockito.Mockito._
import org.scalatest.FunSpec
import org.scalatest.mock.MockitoSugar

class ReportingMigratorSpec extends FunSpec with MockitoSugar {
  val reporter = mock[Reporter]
  val wrapped = mock[Migrator]
  val migrator = new ReportingMigrator(reporter, wrapped)
  val session = mock[Session]
  val keyspace = "myks"

  describe("#initialize") {
    val replicationStrategy = SimpleStrategy()
    migrator.initialize(session, keyspace, replicationStrategy)

    it("reports the initialize action") {
      verify(reporter).initializing(session, keyspace, replicationStrategy)
    }

    it("delegates to the wrapped migrator") {
      verify(wrapped).initialize(session, keyspace, replicationStrategy)
    }
  }

  describe("#migrate") {
    migrator.migrate(session)

    it("reports the migrate action") {
      verify(reporter).migrating(session, None)
    }

    it("delegates to the wrapped migrator") {
      verify(wrapped).migrate(session, None)
    }
  }

  describe("#destroy") {
    migrator.destroy(session, keyspace)

    it("reports the destroy action") {
      verify(reporter).destroying(session, keyspace)
    }

    it("delegates to the wrapped migrator") {
      verify(wrapped).destroy(session, keyspace)
    }
  }
}
