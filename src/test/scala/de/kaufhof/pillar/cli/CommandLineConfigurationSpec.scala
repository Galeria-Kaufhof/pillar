package de.kaufhof.pillar.cli

import java.io.File

import org.scalatest.FunSpec
import org.scalatest.matchers.ShouldMatchers

class CommandLineConfigurationSpec extends FunSpec with ShouldMatchers {
  val sep = File.separator

  describe(".buildFromArguments") {
    describe("command initialize") {
      describe("data-store faker") {

        info("Required arguments are the command (migrate / initialize) and the data store (name of the data store behind migrate or initialize command")
        it("should fail when required arguments are missing"){

          intercept[CommandLineParsingException] {
            CommandLineConfiguration.buildFromArguments(Array("migrate", "-e", "test"))
          }

          intercept[CommandLineParsingException] {
            CommandLineConfiguration.buildFromArguments(Array("faker", "-e", "test"))
          }

          intercept[CommandLineParsingException] {
            CommandLineConfiguration.buildFromArguments(Array("-e", "test"))
          }

        }

        it("sets the command") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).command should be(Initialize)
          CommandLineConfiguration.buildFromArguments(Array("migrate", "faker")).command should be (Migrate)
        }

        it("sets the data store name") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).dataStore should equal("faker")
          CommandLineConfiguration.buildFromArguments(Array("migrate", "faker")).dataStore should equal ("faker")
        }

        it("sets the environment") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).environment should equal("development")
          CommandLineConfiguration.buildFromArguments(Array("migrate", "faker")).environment should equal("development")
        }

        it("sets the migrations directory") {

          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).migrationsDirectory
            .getPath should equal(s"conf${sep}pillar${sep}migrations")
          CommandLineConfiguration.buildFromArguments(Array("migrate", "faker")).migrationsDirectory
            .getPath should equal(s"conf${sep}pillar${sep}migrations")
        }

        it("sets the time stamp") {
          CommandLineConfiguration.buildFromArguments(Array("initialize", "faker")).timeStampOption should be(None)
          CommandLineConfiguration.buildFromArguments(Array("migrate", "faker")).timeStampOption should be(None)
        }

        describe("environment test") {
          it("sets the environment") {
            CommandLineConfiguration.buildFromArguments(Array("-e", "test", "initialize", "faker")).environment should equal("test")
            CommandLineConfiguration.buildFromArguments(Array("-e", "test", "migrate", "faker")).environment should equal("test")
          }
        }

        describe("migrations-directory baz") {
          it("sets the migrations directory") {
            CommandLineConfiguration.buildFromArguments(Array("-d", "src/test/resources/pillar/migrations",
              "initialize", "faker")).migrationsDirectory
              .getPath should equal(s"src${sep}test${sep}resources${sep}pillar${sep}migrations")

            CommandLineConfiguration.buildFromArguments(Array("-d", "src/test/resources/pillar/migrations",
              "migrate", "faker")).migrationsDirectory
              .getPath should equal(s"src${sep}test${sep}resources${sep}pillar${sep}migrations")
          }
        }

        describe("time-stamp 1370028262") {
          it("sets the time stamp option") {
            CommandLineConfiguration.buildFromArguments(Array("-t", "1370028262", "initialize", "faker")).timeStampOption should equal(Some(1370028262))
            CommandLineConfiguration.buildFromArguments(Array("-t", "1370028262", "migrate", "faker")).timeStampOption should equal(Some(1370028262))
          }
        }
      }
    }
  }
}