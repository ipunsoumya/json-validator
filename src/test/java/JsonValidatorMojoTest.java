import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JsonValidatorMojoTest {

    @Mock
    private Log mockLog;

    private JsonValidatorMojo mojo;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mojo = new JsonValidatorMojo();
        
        // Use reflection to set private log field
        try {
            java.lang.reflect.Field logField = AbstractMojo.class.getDeclaredField("log");
            logField.setAccessible(true);
            logField.set(mojo, mockLog);
        } catch (Exception e) {
            fail("Could not set mock log");
        }
    }

    @Test
    void testValidJsonValidation(@TempDir Path tempDir) throws Exception {
        // Create valid JSON files and schema
        Path jsonDir = createValidJsonFiles(tempDir);
        Path schemaFile = createValidSchema(tempDir);

        // Configure mojo
        setMojoFields(mojo, jsonDir.toFile(), schemaFile.toFile());

        // Execute and verify no exception
        assertDoesNotThrow(() -> mojo.execute());
        verify(mockLog, atLeastOnce()).info(contains("Validation passed"));
    }

    @Test
    void testInvalidJsonValidation(@TempDir Path tempDir) throws Exception {
        // Create invalid JSON files and schema
        Path jsonDir = createInvalidJsonFiles(tempDir);
        Path schemaFile = createValidSchema(tempDir);

        // Configure mojo
        setMojoFields(mojo, jsonDir.toFile(), schemaFile.toFile());

        // Expect exception when failOnError is true
        MojoExecutionException exception = assertThrows(
            MojoExecutionException.class, 
            () -> mojo.execute()
        );

        assertTrue(exception.getMessage().contains("JSON validation failed"));
        verify(mockLog, atLeastOnce()).error(contains("Validation failed"));
    }

    @Test
    void testMissingSchemaFile(@TempDir Path tempDir) {
        // Create JSON directory
        Path jsonDir = tempDir.resolve("json");
        jsonDir.toFile().mkdirs();

        // Attempt to validate with non-existent schema
        File nonExistentSchema = new File(tempDir.toFile(), "non-existent-schema.json");

        // Configure mojo
        setMojoFields(mojo, jsonDir.toFile(), nonExistentSchema);

        // Expect exception for missing schema
        MojoExecutionException exception = assertThrows(
            MojoExecutionException.class, 
            () -> mojo.execute()
        );

        assertTrue(exception.getMessage().contains("Schema file does not exist"));
    }

    // Helper method to create valid JSON files
    private Path createValidJsonFiles(Path baseDir) throws IOException {
        Path jsonDir = baseDir.resolve("json");
        jsonDir.toFile().mkdirs();

        // Create a valid JSON file
        Path validJsonFile = jsonDir.resolve("valid.json");
        Files.writeString(validJsonFile, 
            "{\"name\":\"Test\", \"age\":30, \"email\":\"test@example.com\"}"
        );

        return jsonDir;
    }

    // Helper method to create invalid JSON files
    private Path createInvalidJsonFiles(Path baseDir) throws IOException {
        Path jsonDir = baseDir.resolve("json");
        jsonDir.toFile().mkdirs();

        // Create an invalid JSON file
        Path invalidJsonFile = jsonDir.resolve("invalid.json");
        Files.writeString(invalidJsonFile, 
            "{\"name\":30, \"age\":\"Test\", \"invalid\":true}"
        );

        return jsonDir;
    }

    // Helper method to create a valid JSON schema
    private Path createValidSchema(Path baseDir) throws IOException {
        Path schemaDir = baseDir.resolve("schema");
        schemaDir.toFile().mkdirs();

        Path schemaFile = schemaDir.resolve("schema.json");
        Files.writeString(schemaFile, 
            "{" +
            "  \"$schema\": \"http://json-schema.org/draft-07/schema#\"," +
            "  \"type\": \"object\"," +
            "  \"properties\": {" +
            "    \"name\": {\"type\": \"string\"}," +
            "    \"age\": {\"type\": \"number\"}," +
            "    \"email\": {\"type\": \"string\", \"format\": \"email\"}" +
            "  }," +
            "  \"required\": [\"name\", \"age\"]" +
            "}"
        );

        return schemaFile;
    }

    // Helper method to set Mojo fields via reflection
    private void setMojoFields(JsonValidatorMojo mojo, File jsonDir, File schemaFile) {
        try {
            // Use reflection to set private fields
            java.lang.reflect.Field jsonDirField = JsonValidatorMojo.class.getDeclaredField("jsonDirectory");
            jsonDirField.setAccessible(true);
            jsonDirField.set(mojo, jsonDir);

            java.lang.reflect.Field schemaFileField = JsonValidatorMojo.class.getDeclaredField("schemaFile");
            schemaFileField.setAccessible(true);
            schemaFileField.set(mojo, schemaFile);

            java.lang.reflect.Field failOnErrorField = JsonValidatorMojo.class.getDeclaredField("failOnError");
            failOnErrorField.setAccessible(true);
            failOnErrorField.set(mojo, true);
        } catch (Exception e) {
            fail("Could not set Mojo fields via reflection");
        }
    }
}