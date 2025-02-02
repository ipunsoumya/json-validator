import com.github.fge.jackson.JsonLoader
import com.github.fge.jsonschema.main.JsonSchemaFactory
import com.fasterxml.jackson.databind.{JsonNode, ObjectMapper}
import com.fasterxml.jackson.core.JsonParseException

import scala.util.{Failure, Success, Try}
import scala.io.Source
import java.nio.file.{Files, Paths}
import java.nio.charset.StandardCharsets
import org.slf4j.LoggerFactory

import scala.collection.mutable.ListBuffer
import scala.jdk.CollectionConverters.IteratorHasAsScala

object JsonValidator {
  private val logger = LoggerFactory.getLogger(getClass)
  private val mapper = new ObjectMapper()

  case class ValidationResult(
                               filePath: String,
                               isValid: Boolean,
                               errors: List[String] = List.empty
                             )

  def checkFileExists(filePath: String): Either[String, Boolean] = {
    val path = Paths.get(filePath)
    if (Files.exists(path)) {
      if (Files.isRegularFile(path)) {
        Right(true)
      } else {
        Left(s"Path exists but is not a regular file: $filePath")
      }
    } else {
      Left(s"File does not exist: $filePath")
    }
  }

  def verifyJsonFormat(content: String): Either[String, Boolean] = {
    try {
      mapper.readTree(content)
      Right(true)
    } catch {
      case e: JsonParseException =>
        Left(s"Invalid JSON format: ${e.getMessage}")
      case e: Exception =>
        Left(s"Error verifying JSON format: ${e.getMessage}")
    }
  }

  def loadFileContent(filePath: String): Either[String, String] = {
    logger.info(s"Loading file: $filePath")

    for {
      _ <- checkFileExists(filePath)
      content <- Try {
        val source = Source.fromFile(filePath)
        try {
          source.mkString
        } finally {
          source.close()
        }
      }.toEither.left.map(e => s"Error reading file $filePath: ${e.getMessage}")
      _ <- verifyJsonFormat(content)
    } yield content
  }

  def validateMultipleJsonFiles(jsonPaths: List[String], schemaPath: String): List[ValidationResult] = {
    logger.info(s"Starting validation of ${jsonPaths.size} files against schema: $schemaPath")

    loadFileContent(schemaPath) match {
      case Right(schemaContent) =>
        logger.info("Schema loaded successfully")

        jsonPaths.map { jsonPath =>
          logger.info(s"Validating file: $jsonPath")

          val result = for {
            jsonContent <- loadFileContent(jsonPath)
            validationResult <- validateJsonContent(jsonContent, schemaContent)
          } yield validationResult

          result match {
            case Right(_) =>
              logger.info(s"Validation successful for: $jsonPath")
              ValidationResult(jsonPath, true)
            case Left(error) =>
              logger.error(s"Validation failed for: $jsonPath - $error")
              ValidationResult(jsonPath, false, List(error))
          }
        }

      case Left(error) =>
        logger.error(s"Schema loading failed: $error")
        jsonPaths.map(path => ValidationResult(path, false, List(s"Schema loading failed: $error")))
    }
  }

  private def validateJsonContent(jsonContent: String, schemaContent: String): Either[String, Boolean] = {
    try {
      logger.debug("Creating JSON Schema factory")
      val factory = JsonSchemaFactory.byDefault()

      logger.debug("Loading schema")
      val jsonSchema = factory.getJsonSchema(JsonLoader.fromString(schemaContent))

      logger.debug("Loading JSON content for validation")
      val jsonToValidate = JsonLoader.fromString(jsonContent)

      logger.debug("Performing validation")
      val report = jsonSchema.validate(jsonToValidate)

      if (report.isSuccess) {
        logger.debug("Validation successful")
        Right(true)
      } else {
        val errors = report.iterator().asScala.map(_.getMessage).toList
        logger.debug(s"Validation failed with ${errors.size} errors")
        Left(errors.mkString(", "))
      }
    } catch {
      case e: Exception =>
        logger.error("Error during validation", e)
        Left(s"Error during validation: ${e.getMessage}")
    }
  }

  def main(args: Array[String]): Unit = {
    val schemaPath = "src/main/resources/schema.json"
    val jsonPaths = List(
      "src/main/resources/input1.json",
      "src/main/resources/input2.json",
      "src/main/resources/input3.json"
    )

    logger.info("Starting validation process")

    val results = validateMultipleJsonFiles(jsonPaths, schemaPath)

    println("\nValidation Results:")
    results.foreach { result =>
      println(s"\nFile: ${result.filePath}")
      println(s"Valid: ${result.isValid}")
      if (result.errors.nonEmpty) {
        println("Errors:")
        result.errors.foreach(error => println(s"  - $error"))
      }
    }
  }
}