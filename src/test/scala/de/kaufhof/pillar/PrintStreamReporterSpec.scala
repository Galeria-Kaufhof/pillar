package de.kaufhof.pillar

import java.io.{ByteArrayOutputStream, PrintStream}
import java.util.Date

import com.datastax.driver.core.Session
import org.scalatest._
import org.scalatest.mock.MockitoSugar

class PrintStreamReporterSpec extends FunSpec with MockitoSugar with Matchers with OneInstancePerTest {
  val session = mock[Session]
  val migration = Migration("creates things table", new Date(1370489972546L), "up", Some("down"))
  val output = new ByteArrayOutputStream()
  val stream = new PrintStream(output)
  val reporter = new PrintStreamReporter(stream)
  val keyspace = "myks"
  val replicationStrategy = SimpleStrategy()
  val nl = System.lineSeparator()

  describe("#initializing") {
    it("should print to the stream") {
      reporter.initializing(session, keyspace, replicationStrategy)
      output.toString should equal(s"Initializing myks${nl}")
    }
  }

  describe("#migrating") {
    describe("without date restriction") {
      it("should print to the stream") {
        reporter.migrating(session, None)
        output.toString should equal(s"Migrating with date restriction None${nl}")
      }
    }
  }

  describe("#applying") {
    it("should print to the stream") {
      reporter.applying(migration)
      output.toString should equal(s"Applying 1370489972546: creates things table${nl}")
    }
  }

  describe("#reversing") {
    it("should print to the stream") {
      reporter.reversing(migration)
      output.toString should equal(s"Reversing 1370489972546: creates things table${nl}")
    }
  }

  describe("#destroying") {
    it("should print to the stream") {
      reporter.destroying(session, keyspace)
      output.toString should equal(s"Destroying myks${nl}")
    }
  }
}
