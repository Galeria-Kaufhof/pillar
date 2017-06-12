package de.kaufhof.pillar.cli

import java.io.File

import scala.annotation.tailrec

class CommandLineParsingException(message: String) extends RuntimeException(message)

object CommandLineConfiguration {

  sealed trait ParserResult

  case class ParserSuccess(command: Option[MigratorAction], dataStore: Option[String], environment: Option[String], migrationsDirectory: Option[File], timeStampOption: Option[Long]) extends ParserResult
  case class ParserFailure(reason: String) extends ParserResult

  val commandLineUsage: String =
    """
      |Usage: pillar [OPTIONS] command data-store
      |
      |OPTIONS
      |
      |-d directory
      |--migrations-directory directory  The directory containing migrations
      |
      |-e env
      |--environment env                 environment
      |
      |-t time
      |--time-stamp time                 The migration time stamp
      |
      |PARAMETERS
      |
      |command     migrate or initialize
      |
      |data-store  The target data store, as defined in application.conf
    """.stripMargin

  @tailrec
  private def parseArgsRecusively(remainingArgs: List[String], parserSuccess: ParserSuccess = ParserSuccess(None, None, None, None, None)): ParserResult = {

    remainingArgs match {

      case Nil =>

        if(parserSuccess.command.isDefined && parserSuccess.dataStore.isDefined) {
          parserSuccess
        } else {
          ParserFailure("Required arguments command and data store not given.")
        }

      case "initialize" :: tail if parserSuccess.command.isEmpty && tail.nonEmpty =>
        parseArgsRecusively(tail.tail, parserSuccess.copy(command = Some(Initialize), dataStore = Some(tail.head)))

      case "migrate" :: tail if parserSuccess.command.isEmpty && tail.nonEmpty =>
        parseArgsRecusively(tail.tail, parserSuccess.copy(command = Some(Migrate), dataStore = Some(tail.head)))

      case "-e" :: tail if tail.nonEmpty =>
        parseArgsRecusively(tail.tail, parserSuccess.copy(environment = Some(tail.head)))

      case "-t" :: tail if tail.nonEmpty =>

        parseArgsRecusively(tail.tail, parserSuccess.copy(timeStampOption = Some(tail.head.toLong)))

      case "-d" :: tail if tail.nonEmpty =>
        parseArgsRecusively(tail.tail, parserSuccess.copy(migrationsDirectory = Some(new File(tail.head))))

      case unknown =>
        ParserFailure(s"Failed to parse the command line - cannot handle arguments: ${unknown.mkString(" ")}")
    }
  }

  def buildFromArguments(arguments: Array[String]): CommandLineConfiguration = parseArgsRecusively(arguments.toList) match {
    case ParserSuccess(command, dataStore, environment, migrationsDirectory, timeStampOption) =>
      CommandLineConfiguration(
        command.get,
        dataStore.get,
        environment.getOrElse("development"),
        migrationsDirectory.getOrElse(new File("conf/pillar/migrations")),
        timeStampOption
      )
    case ParserFailure(reason) =>
      throw new CommandLineParsingException(s"$reason\n${CommandLineConfiguration.commandLineUsage}")
  }
}

case class CommandLineConfiguration(command: MigratorAction, dataStore: String, environment: String, migrationsDirectory: File, timeStampOption: Option[Long])

