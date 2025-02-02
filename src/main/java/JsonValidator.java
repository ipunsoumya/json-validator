import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.fge.jackson.JsonLoader;
import com.github.fge.jsonschema.main.JsonSchemaFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JsonValidator {
    private static final Logger logger = LoggerFactory.getLogger(JsonValidator.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    public static class ValidationResult {
        private final String filePath;
        private final boolean isValid;
        private final List<String> errors;

        public ValidationResult(String filePath, boolean isValid, List<String> errors) {
            this.filePath = filePath;
            this.isValid = isValid;
            this.errors = errors != null ? errors : new ArrayList<>();
        }

        public ValidationResult(String filePath, boolean isValid) {
            this(filePath, isValid, new ArrayList<>());
        }

        public String getFilePath() { return filePath; }
        public boolean isValid() { return isValid; }
        public List<String> getErrors() { return errors; }
    }

    public static List<ValidationResult> validateMultipleJsonFiles(List<String> jsonPaths, String schemaPath) {
        logger.info("Starting validation of {} files against schema: {}", jsonPaths.size(), schemaPath);

        try {
            String schemaContent = loadFileContent(schemaPath).orElseThrow(() -> 
                new RuntimeException("Failed to load schema file"));
            logger.info("Schema loaded successfully");

            return jsonPaths.stream().map(jsonPath -> {
                logger.info("Validating file: {}", jsonPath);
                try {
                    String jsonContent = loadFileContent(jsonPath).orElseThrow(() ->
                        new RuntimeException("Failed to load JSON file"));
                    boolean isValid = validateJsonContent(jsonContent, schemaContent);
                    if (isValid) {
                        logger.info("Validation successful for: {}", jsonPath);
                        return new ValidationResult(jsonPath, true);
                    } else {
                        logger.error("Validation failed for: {}", jsonPath);
                        return new ValidationResult(jsonPath, false);
                    }
                } catch (Exception e) {
                    logger.error("Validation failed for: {} - {}", jsonPath, e.getMessage());
                    List<String> errors = new ArrayList<>();
                    errors.add(e.getMessage());
                    return new ValidationResult(jsonPath, false, errors);
                }
            }).collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Schema loading failed: {}", e.getMessage());
            return jsonPaths.stream()
                .map(path -> new ValidationResult(path, false, 
                    List.of("Schema loading failed: " + e.getMessage())))
                .collect(Collectors.toList());
        }
    }

    private static java.util.Optional<String> loadFileContent(String filePath) {
        try {
            return java.util.Optional.of(Files.readString(Paths.get(filePath)));
        } catch (IOException e) {
            logger.error("Error reading file: {}", e.getMessage());
            return java.util.Optional.empty();
        }
    }

    private static boolean validateJsonContent(String jsonContent, String schemaContent) {
        try {
            logger.debug("Creating JSON Schema factory");
            JsonSchemaFactory factory = JsonSchemaFactory.byDefault();

            logger.debug("Loading schema");
            var jsonSchema = factory.getJsonSchema(JsonLoader.fromString(schemaContent));

            logger.debug("Loading JSON content for validation");
            var jsonToValidate = JsonLoader.fromString(jsonContent);

            logger.debug("Performing validation");
            var report = jsonSchema.validate(jsonToValidate);

            if (report.isSuccess()) {
                logger.debug("Validation successful");
                return true;
            } else {
                var errors = new ArrayList<String>();
                report.forEach(pm -> errors.add(pm.getMessage()));
                logger.debug("Validation failed with {} errors", errors.size());
                return false;
            }
        } catch (Exception e) {
            logger.error("Error during validation", e);
            return false;
        }
    }
}