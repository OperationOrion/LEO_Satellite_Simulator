package application;

public class ValidationError extends SatelliteError {
    public ValidationError(String message, String errorDetails) {
        super(message, errorDetails);
    }
}