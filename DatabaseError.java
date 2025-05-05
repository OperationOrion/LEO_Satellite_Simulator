package application;

public class DatabaseError extends SatelliteError{
    public DatabaseError(String message, String errorDetails) {
        super(message, errorDetails);
    }
}