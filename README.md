# JSON Schema Validator Maven Plugin

This Maven plugin validates JSON data against JSON schemas, providing a robust solution for ensuring data integrity in Java projects.

The JSON Schema Validator Maven Plugin is designed to integrate seamlessly into your build process, allowing you to validate JSON files against predefined schemas.
It leverages the power of Java and the flexibility of Maven to provide a reliable and efficient validation mechanism for your JSON data.

This plugin is particularly useful for projects that deal with complex JSON structures, such as configuration files, API responses, or data exchange formats.
By validating your JSON data against a schema, you can catch errors early in the development process, ensuring data consistency and reducing potential issues in production.

## Repository Structure

```
.
├── pom.xml
├── README.md
├── src
│   ├── main
│   │   ├── java
│   │   │   ├── JsonValidator.java
│   │   │   ├── JsonValidatorMojo.java
│   │   │   ├── ValidationResult.java
│   │   │   └── ValidationSummary.java
│   │   └── resources
│   │       ├── input.json
│   │       └── schema.json
│   └── test
│       └── java
│           └── JsonValidatorMojoTest.java
└── target
    ├── classes
    │   ├── input.json
    │   ├── META-INF
    │   │   └── maven
    │   │       ├── com.example
    │   │       │   └── json-schema-validator-plugin
    │   │       │       └── plugin-help.xml
    │   │       └── plugin.xml
    │   └── schema.json
    ├── maven-archiver
    │   └── pom.properties
    └── surefire-reports
        ├── JsonValidatorMojoTest.txt
        └── TEST-JsonValidatorMojoTest.xml
```

Key Files:
- `pom.xml`: Maven project configuration file
- `src/main/java/JsonValidator.java`: Core validation logic
- `src/main/java/JsonValidatorMojo.java`: Maven plugin implementation
- `src/main/java/ValidationResult.java`: Validation result representation
- `src/main/java/ValidationSummary.java`: Validation summary representation
- `src/main/resources/input.json`: Sample JSON input file
- `src/main/resources/schema.json`: JSON schema definition file
- `src/test/java/JsonValidatorMojoTest.java`: Unit tests for the plugin

## Usage Instructions

### Installation

Prerequisites:
- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.8.1 or higher

To install the plugin, add the following to your project's `pom.xml`:

```xml
<plugin>
    <groupId>com.example</groupId>
    <artifactId>json-schema-validator-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
</plugin>
```

### Getting Started

1. Define your JSON schema in a file (e.g., `schema.json`).
2. Create your JSON input file (e.g., `input.json`).
3. Configure the plugin in your `pom.xml`:

```xml
<plugin>
    <groupId>com.example</groupId>
    <artifactId>json-schema-validator-plugin</artifactId>
    <version>1.0-SNAPSHOT</version>
    <configuration>
        <jsonDirectory>${project.basedir}/src/main/resources/json</jsonDirectory>
        <schemaFile>${project.basedir}/src/main/resources/schema/schema.json</schemaFile>
        <failOnError>true</failOnError>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>validate-json</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

4. Run the validation:

```
mvn json-schema-validator:validate-json
```

### Configuration Options

- `jsonDirectory`: Directory containing JSON files to validate (default: `${project.basedir}/src/main/resources/json`)
- `schemaFile`: JSON schema file to validate against (default: `${project.basedir}/src/main/resources/schema/schema.json`)
- `failOnError`: Whether to fail the build on validation errors (default: true)

### Common Use Cases

1. Validating JSON files in a specific directory:

```xml
<configuration>
    <jsonDirectory>${project.basedir}/src/main/resources/configs</jsonDirectory>
    <schemaFile>${project.basedir}/src/main/resources/schema/config-schema.json</schemaFile>
</configuration>
```

2. Allowing the build to continue even if validation fails:

```xml
<configuration>
    <jsonDirectory>${project.basedir}/src/main/resources/json</jsonDirectory>
    <schemaFile>${project.basedir}/src/main/resources/schema/schema.json</schemaFile>
    <failOnError>false</failOnError>
</configuration>
```

### Integration Patterns

- Continuous Integration: Include the plugin in your CI/CD pipeline to ensure all JSON files are valid before deployment.
- Pre-commit hooks: Use the plugin in combination with git hooks to validate JSON files before committing changes.

### Testing & Quality

To run the plugin's tests:

```
mvn test
```

### Troubleshooting

Common issues and solutions:

1. Schema not found
   - Problem: "Schema file does not exist: /path/to/schema.json"
   - Solution: Ensure the `schemaFile` path is correct and the file exists.
   - Diagnostic steps:
     a. Check the file path in your `pom.xml`
     b. Verify the file exists in your project structure
   - Debug command: `mvn json-schema-validator:validate-json -X`

2. JSON directory not found
   - Problem: "JSON directory does not exist: /path/to/json"
   - Solution: Ensure the `jsonDirectory` path is correct and the directory exists.
   - Diagnostic steps:
     a. Check the directory path in your `pom.xml`
     b. Verify the directory exists in your project structure
   - Debug command: `mvn json-schema-validator:validate-json -X`

3. Validation errors
   - Problem: "JSON validation failed. See log for details."
   - Solution: Review the error messages in the log and update your JSON files to match the schema.
   - Diagnostic steps:
     a. Check the error messages for specific validation failures
     b. Compare your JSON files against the schema requirements
   - Debug command: `mvn json-schema-validator:validate-json -Dfail-on-error=false`

Debugging guidance:
- Enable debug mode: Add `-X` to your Maven command
- Verbose logging: Set the log level to DEBUG in your logging configuration
- Log file location: Check Maven's output for detailed validation results
- Required permissions: Ensure read access to schema and JSON files

Performance optimization:
- Monitor execution time using Maven's built-in time tracking
- Profile the plugin using Java profiling tools
- Baseline performance: Typically sub-second for small to medium-sized JSON files
- Common bottleneck: Large input files or complex schemas may increase validation time

## Data Flow

The JSON Schema Validator plugin processes data through the following steps:

1. Plugin initialization: Maven loads the plugin configuration from `pom.xml`.
2. Schema loading: The plugin reads the JSON schema file specified in the configuration.
3. JSON file discovery: The plugin scans the specified directory for JSON files.
4. Validation: For each JSON file, the `JsonValidator` class performs the schema validation.
5. Result processing: `ValidationResult` objects are created for each file, and a `ValidationSummary` is compiled.
6. Output generation: The plugin logs validation results and errors.
7. Build integration: Based on the validation results and configuration, the plugin may fail the build or continue.

```
[Maven] -> [Plugin Config] -> [Schema File] -> [JsonValidator]
                           -> [JSON Files]  ->
[JsonValidator] -> [ValidationResult] -> [ValidationSummary] -> [Output/Logs]
                                                             -> [Build Status]
```

Important technical considerations:
- The plugin uses the `com.networknt.schema` library for JSON schema validation.
- Java is used for the core logic, providing object-oriented programming benefits.
- The plugin integrates with Maven's lifecycle, allowing for seamless build integration.