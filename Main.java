/* Project name: CMSC495
 * File name: AtmosphericDrag.java
 * Authors: Timothy Eckart, Tyler Blumenshine, Ricardo Gordon, Mitch Mclaughlin, Siddharth Patel
 * Date: 7 Apr 2025
 * Purpose: Calculates atmospheric density and orbital decay based on altitude and satellite properties.
 */

package application;


import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    public void start(Stage primaryStage) {
        // First UI (Login UI)
        // On successful login, switch to the dropdown UI

            SatelliteSimulatorLogin loginUI = new SatelliteSimulatorLogin();
        loginUI.start(primaryStage);

     loginUI.setOnLoginSuccess(() -> {
        UserInterface ui = new UserInterface();
        ui.build(primaryStage);
    });
    }

    public static void main(String[] args) {
        launch(args);
    }
}