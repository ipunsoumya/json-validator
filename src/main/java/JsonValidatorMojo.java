import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

/**
 * Maven plugin to validate JSON files against a JSON schema
 */
@Mojo(name = "validate-json", defaultPhase = LifecyclePhase.VALIDATE)
public class JsonValidatorMojo extends AbstractMojo {

    /**
     * Directory containing JSON files to validate
     */
    @Parameter(property = "jsonValidator.jsonDirectory", 
               defaultValue = "${project.basedir}/src/main/resources/json", 
               required = true)
    private File jsonDirectory;

    /**
     * JSON schema file to validate against
     */
    @Parameter(property = "jsonValidator.schemaFile", 
               defaultValue = "${project.basedir}/src/main/resources/schema/schema.json", 
               required = true)
    private File schemaFile;

    /**
     * Whether to fail the build on validation errors
     */
    @Parameter(property = "jsonValidator.failOnError", defaultValue = "true")
    private boolean failOnError;

    /**
     * Represents the validation result for a single file
     */
    private static class ValidationResult {
        boolean isValid;
        Set<ValidationMessage> validationMessages;

        ValidationResult(boolean isValid, Set<ValidationMessage> validationMessages) {
            this.isValid = isValid;
            this.validationMessages = validationMessages;
        }
    }

    @Override
    public void execute() throws MojoExecutionException {
        // Validate input directory and schema file
        if (!jsonDirectory.exists() || !jsonDirectory.isDirectory()) {
            throw new MojoExecutionException("JSON directory does not exist: " + jsonDirectory);
        }

        if (!schemaFile.exists() || !schemaFile.isFile()) {
            throw new MojoExecutionException("Schema file does not exist: " + schemaFile);
        }

        // Collect validation results
        List<ValidationResult> validationResults = new ArrayList<>();

        try {
            // Recursively find and validate JSON files
            try (Stream<Path> paths = Files.walk(Paths.get(jsonDirectory.getPath()))) {
                paths.filter(Files::isRegularFile)
                     .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                     .forEach(path -> {
                         try {
                             ValidationResult result = validateJsonFile(path.toFile(), schemaFile);
                             validationResults.add(result);

                             // Log validation details
                             if (!result.isValid) {
                                 getLog().error("Validation failed for file: " + path);
                                 result.validationMessages.forEach(msg -> 
                                     getLog().error(" - " + msg.getMessage())
                                 );
                             } else {
                                 getLog().info("Validation passed for file: " + path);
                             }
                         } catch (Exception e) {
                             getLog().error("Error validating file: " + path, e);
                         }
                     });
            }

            // Check if validation should fail the build
            boolean hasErrors = validationResults.stream()
                .anyMatch(result -> !result.isValid);

            if (failOnError && hasErrors) {
                throw new MojoExecutionException("JSON validation failed. See log for details.");
            }

        } catch (IOException e) {
            throw new MojoExecutionException("Error walking JSON directory", e);
        }
    }

    /**
     * Validate a single JSON file against the schema
     */
    private ValidationResult validateJsonFile(File jsonFile, File schemaFile) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        
        // Read JSON file
        JsonNode jsonNode = objectMapper.readTree(jsonFile);
        
        // Read schema file
        JsonNode schemaNode = objectMapper.readTree(schemaFile);
        
        // Create schema validator
        JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
        JsonSchema schema = schemaFactory.getSchema(schemaNode);
        
        // Validate
        Set<ValidationMessage> validationMessages = schema.validate(jsonNode);
        
        return new ValidationResult(
            validationMessages.isEmpty(), 
            validationMessages
        );
    }
}