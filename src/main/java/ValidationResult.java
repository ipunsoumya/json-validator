import com.networknt.schema.ValidationMessage;

import java.util.Set;

public class ValidationResult {
    private boolean isValid;
    private Set<ValidationMessage> validationMessages;
    private String errorMessage;

    public ValidationResult(boolean isValid, Set<ValidationMessage> validationMessages) {
        this.isValid = isValid;
        this.validationMessages = validationMessages;
    }

    public ValidationResult(boolean isValid, Set<ValidationMessage> validationMessages, String errorMessage) {
        this.isValid = isValid;
        this.validationMessages = validationMessages;
        this.errorMessage = errorMessage;
    }

    public boolean isValid() {
        return isValid;
    }

    public Set<ValidationMessage> getValidationMessages() {
        return validationMessages;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}