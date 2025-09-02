import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Stream;

public class JsonValidator {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Validates a single JSON file against a provided JSON schema
     *
     * @param jsonFilePath Path to the JSON file to validate
     * @param schemaFilePath Path to the JSON schema file
     * @return ValidationResult containing validation status and messages
     */
    public static ValidationResult validateJsonFile(String jsonFilePath, String schemaFilePath) {
        try {
            // Read JSON file
            JsonNode jsonNode = objectMapper.readTree(new File(jsonFilePath));

            // Read JSON schema
            JsonNode schemaNode = objectMapper.readTree(new File(schemaFilePath));

            // Create schema factory
            JsonSchemaFactory schemaFactory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
            JsonSchema schema = schemaFactory.getSchema(schemaNode);

            // Validate
            Set<ValidationMessage> validationMessages = schema.validate(jsonNode);

            return new ValidationResult(
                    validationMessages.isEmpty(),
                    validationMessages
            );
        } catch (IOException e) {
            return new ValidationResult(false, null,
                    "Error reading file: " + e.getMessage()
            );
        } catch (Exception e) {
            return new ValidationResult(false, null,
                    "Validation error: " + e.getMessage()
            );
        }
    }

    /**
     * Validates all JSON files in a given directory against a specific schema
     *
     * @param directoryPath Path to the directory containing JSON files
     * @param schemaFilePath Path to the JSON schema file
     * @return ValidationSummary containing results for all files
     */
    public static ValidationSummary validateJsonFilesInDirectory(String directoryPath, String schemaFilePath) {
        ValidationSummary summary = new ValidationSummary();

        try (Stream<Path> paths = Files.walk(Paths.get(directoryPath))) {
            paths.filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .forEach(path -> {
                        ValidationResult result = validateJsonFile(
                                path.toString(),
                                schemaFilePath
                        );
                        summary.addResult(path.getFileName().toString(), result);
                    });
        } catch (IOException e) {
            summary.setOverallError("Error accessing directory: " + e.getMessage());
        }

        return summary;
    }
}