import java.util.HashMap;
import java.util.Map;

public class ValidationSummary {
    private int totalFiles = 0;
    private int validFiles = 0;
    private int invalidFiles = 0;
    private Map<String, ValidationResult> fileResults = new HashMap<>();
    private String overallError;

    public void addResult(String fileName, ValidationResult result) {
        totalFiles++;
        if (result.isValid()) {
            validFiles++;
        } else {
            invalidFiles++;
        }
        fileResults.put(fileName, result);
    }

    public void setOverallError(String error) {
        this.overallError = error;
    }

    // Getters for summary information
    public int getTotalFiles() {
        return totalFiles;
    }

    public int getValidFiles() {
        return validFiles;
    }

    public int getInvalidFiles() {
        return invalidFiles;
    }

    public Map<String, ValidationResult> getFileResults() {
        return fileResults;
    }

    public String getOverallError() {
        return overallError;
    }
}