import org.apache.maven.plugin.AbstractMojo
import org.apache.maven.plugin.MojoExecutionException
import org.apache.maven.plugins.annotations.{Mojo, Parameter, LifecyclePhase}
import java.io.File
import scala.collection.JavaConverters._

@Mojo(name = "validate-json", defaultPhase = LifecyclePhase.VALIDATE)
class JsonValidatorMojo extends AbstractMojo {
  @Parameter(required = true)
  private var schemaPath: String = _

  @Parameter(required = true)
  private var jsonDirectory: String = _

  @Parameter(defaultValue = "*.json")
  private var jsonPattern: String = _

  @Parameter(defaultValue = "true")
  private var failOnError: Boolean = _

  override def execute(): Unit = {
    try {
      getLog.info(s"Validating JSON files in: $jsonDirectory")
      getLog.info(s"Using schema: $schemaPath")

      val directory = new File(jsonDirectory)
      if (!directory.exists() || !directory.isDirectory) {
        throw new MojoExecutionException(s"Invalid JSON directory: $jsonDirectory")
      }

      val jsonFiles = directory.listFiles((_, name) => name.matches(jsonPattern))
        .map(_.getAbsolutePath)
        .toList

      getLog.info(s"Found ${jsonFiles.size} JSON files to validate")

      val results = JsonValidator.validateMultipleJsonFiles(jsonFiles, schemaPath)

      val (validFiles, invalidFiles) = results.partition(_.isValid)

      getLog.info(s"Validation complete:")
      getLog.info(s"Valid files: ${validFiles.size}")
      getLog.info(s"Invalid files: ${invalidFiles.size}")

      invalidFiles.foreach { result =>
        getLog.error(s"Validation failed for ${result.filePath}")
        result.errors.foreach(error => getLog.error(s"  - $error"))
      }

      if (failOnError && invalidFiles.nonEmpty) {
        throw new MojoExecutionException(
          s"JSON validation failed for ${invalidFiles.size} files"
        )
      }
    } catch {
      case e: Exception =>
        getLog.error("Error during JSON validation", e)
        if (failOnError) {
          throw new MojoExecutionException("JSON validation failed", e)
        }
    }
  }
}