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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mojo(name = "validate-json", defaultPhase = LifecyclePhase.VALIDATE)
public class JsonValidatorMojo extends AbstractMojo {
    @Parameter(required = true)
    private String schemaPath;

    @Parameter(required = true)
    private String jsonDirectory;

    @Parameter(defaultValue = "*.json")
    private String jsonPattern;

    @Parameter(defaultValue = "true")
    private boolean failOnError;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            Path dir = Paths.get(jsonDirectory);
            if (!Files.exists(dir)) {
                throw new MojoExecutionException("JSON directory does not exist: " + jsonDirectory);
            }

            List<String> jsonFiles;
            try (Stream<Path> paths = Files.walk(dir)) {
                jsonFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".json"))
                    .map(Path::toString)
                    .collect(Collectors.toList());
            }

            if (jsonFiles.isEmpty()) {
                getLog().warn("No JSON files found in directory: " + jsonDirectory);
                return;
            }

            List<JsonValidator.ValidationResult> results = 
                JsonValidator.validateMultipleJsonFiles(jsonFiles, schemaPath);

            int invalidCount = 0;
            for (JsonValidator.ValidationResult result : results) {
                if (!result.isValid()) {
                    invalidCount++;
                    getLog().error("Validation failed for: " + result.getFilePath());
                    for (String error : result.getErrors()) {
                        getLog().error("  - " + error);
                    }
                } else {
                    getLog().info("Validation successful for: " + result.getFilePath());
                }
            }

            if (invalidCount > 0 && failOnError) {
                throw new MojoExecutionException(
                    String.format("JSON validation failed for %d file(s)", invalidCount));
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Error processing JSON files", e);
        }
    }
}