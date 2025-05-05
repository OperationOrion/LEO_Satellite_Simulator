package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.sql.*;
import java.util.ArrayList;
import org.mindrot.jbcrypt.BCrypt;


public class SatelliteSimulatorLogin extends Application {

    private static final String URL = "jdbc:postgresql://localhost:5432/SatelliteSimulator";
    private static final String USER = "satellite_user";
    private static final String PASSWORD = "123456789";
    private Runnable onLoginSuccess;

    public void setOnLoginSuccess(Runnable onLoginSuccess) {
        this.onLoginSuccess = onLoginSuccess;
    }

    @Override
    public void start(Stage stage) {
        // Create UI components
        Label emailLabel = new Label("Email:");
        Label passwordLabel = new Label("Password:");
        TextField emailField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Login");
        Label errorLabel = new Label();

        // Layout setup
        VBox loginBox = new VBox(10, emailLabel, emailField, passwordLabel, passwordField, loginButton, errorLabel);
        loginBox.setStyle("-fx-padding: 20;");

        loginButton.setOnAction(e -> {
            try {
                // Perform login and populate dropdown
                boolean success = authenticateUser(emailField.getText(), passwordField.getText());
                if (success) {
                    onLoginSuccess.run();

                    //loadUserSatellites(emailField.getText());
                   // errorLabel.setText("Login successful!");
                } else {
                    errorLabel.setText("Invalid email or password.");
                }
            } catch (Exception ex) {
                errorLabel.setText("Error: " + ex.getMessage());
            }
        });

        Scene scene = new Scene(loginBox, 400, 300);
        stage.setScene(scene);
        stage.setTitle("Satellite Simulator Login");
        stage.show();
    }

    private boolean authenticateUser(String email, String password) throws SQLException{
        // Connect to PostgreSQL and validate credentials
        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD)) {
            String query = "SELECT \"USER_ID\",\"Password\" FROM public.\"Users\" WHERE \"Email\" = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String storedPassword = rs.getString("Password");

               if(verifyPassword(password, storedPassword)) {
                   int userID = rs.getInt("USER_ID");
                   SessionData.setUserID(userID);
                   return true;
               }else
                return false;
               // return storedPassword.equals(password); // Ensure password is hashed if using encryption
            }
        }
        return false;
    }
    private boolean verifyPassword(String enteredPassword, String storedPassword) {
        // For pgcrypto use bcrypt or another hashing mechanism for password validation
        String hashedEnteredPassword = BCrypt.hashpw(enteredPassword, storedPassword);
        return BCrypt.checkpw(enteredPassword, storedPassword);
    }


    public static void main(String[] args) {
        launch();
    }
}