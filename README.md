# JSON Schema Validator Maven Plugin

This Maven plugin validates JSON data against JSON schemas, providing a robust solution for ensuring data integrity in Scala projects.

The JSON Schema Validator Maven Plugin is designed to integrate seamlessly into your build process, allowing you to validate JSON files against predefined schemas.
It leverages the power of Scala and the flexibility of Maven to provide a reliable and efficient validation mechanism for your JSON data.

This plugin is particularly useful for projects that deal with complex JSON structures, such as configuration files, API responses, or data exchange formats.
By validating your JSON data against a schema, you can catch errors early in the development process, ensuring data consistency and reducing potential issues in production.

## Repository Structure

```
.
├── pom.xml
├── src
│   └── main
│       ├── resources
│       │   ├── input.json
│       │   └── schema.json
│       └── scala
│           ├── JsonValidator.scala
│           └── JsonValidatorMojo.scala
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
    └── maven-archiver
        └── pom.properties
```

Key Files:
- `pom.xml`: Maven project configuration file
- `src/main/resources/input.json`: Sample JSON input file
- `src/main/resources/schema.json`: JSON schema definition file
- `src/main/scala/JsonValidator.scala`: Core validation logic
- `src/main/scala/JsonValidatorMojo.scala`: Maven plugin implementation

## Usage Instructions

### Installation

Prerequisites:
- Java Development Kit (JDK) 11 or higher
- Apache Maven 3.8.1 or higher
- Scala 2.13.16

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
        <schemaFile>src/main/resources/schema.json</schemaFile>
        <inputFile>src/main/resources/input.json</inputFile>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>validate</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

4. Run the validation:

```
mvn json-validator:validate
```

### Configuration Options

- `schemaFile`: Path to the JSON schema file (required)
- `inputFile`: Path to the JSON input file to validate (required)
- `failOnError`: Whether to fail the build if validation errors are found (default: true)

### Common Use Cases

1. Validating configuration files:

```xml
<configuration>
    <schemaFile>src/main/resources/config-schema.json</schemaFile>
    <inputFile>src/main/resources/config.json</inputFile>
</configuration>
```

2. Validating multiple JSON files:

```xml
<configuration>
    <schemaFile>src/main/resources/schema.json</schemaFile>
    <inputFiles>
        <inputFile>src/main/resources/input1.json</inputFile>
        <inputFile>src/main/resources/input2.json</inputFile>
    </inputFiles>
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
   - Problem: "Schema file not found: /path/to/schema.json"
   - Solution: Ensure the `schemaFile` path is correct and the file exists.
   - Diagnostic steps:
     a. Check the file path in your `pom.xml`
     b. Verify the file exists in your project structure
   - Debug command: `mvn json-validator:validate -X`

2. Input file not found
   - Problem: "Input file not found: /path/to/input.json"
   - Solution: Ensure the `inputFile` path is correct and the file exists.
   - Diagnostic steps:
     a. Check the file path in your `pom.xml`
     b. Verify the file exists in your project structure
   - Debug command: `mvn json-validator:validate -X`

3. Validation errors
   - Problem: "JSON validation failed: [error details]"
   - Solution: Review the error message and update your JSON input file to match the schema.
   - Diagnostic steps:
     a. Check the error message for specific validation failures
     b. Compare your input JSON against the schema requirements
   - Debug command: `mvn json-validator:validate -Dfail-on-error=false`

Debugging guidance:
- Enable debug mode: Add `-X` to your Maven command
- Verbose logging: Set the log level to DEBUG in your `logback.xml` configuration
- Log file location: Check `target/json-validator.log` for detailed output
- Required permissions: Ensure read access to schema and input files

Performance optimization:
- Monitor execution time using Maven's built-in time tracking
- Profile the plugin using Java Flight Recorder (JFR)
- Baseline performance: Typically sub-second for small to medium-sized JSON files
- Common bottleneck: Large input files or complex schemas may increase validation time

## Data Flow

The JSON Schema Validator plugin processes data through the following steps:

1. Plugin initialization: Maven loads the plugin configuration from `pom.xml`.
2. Schema loading: The plugin reads the JSON schema file specified in the configuration.
3. Input file reading: The plugin reads the input JSON file(s) to be validated.
4. Validation: The `JsonValidator` class performs the schema validation against the input JSON.
5. Result processing: Validation results are collected and processed.
6. Output generation: The plugin generates validation reports or error messages.
7. Build integration: Based on the validation results and configuration, the plugin may fail the build or continue.

```
[Maven] -> [Plugin Config] -> [Schema File] -> [JsonValidator]
                           -> [Input File]  ->
[JsonValidator] -> [Validation Results] -> [Output/Logs]
                                        -> [Build Status]
```

Important technical considerations:
- The plugin uses the `com.github.java-json-tools:json-schema-validator` library for JSON schema validation.
- Scala is used for the core logic, providing functional programming benefits.
- The plugin integrates with Maven's lifecycle, allowing for seamless build integration.