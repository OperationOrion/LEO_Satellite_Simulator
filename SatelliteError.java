package application;

public abstract class SatelliteError extends Exception {
    private final String errorDetails;

    //Throws error message for Database Error and ValidationError
    public SatelliteError(String message, String errorDetails) {
        super(message);
        this.errorDetails = errorDetails;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public String toString() {
        return this.getClass().getSimpleName() + ": " + getMessage() + " | Details: " + errorDetails;
    }
}