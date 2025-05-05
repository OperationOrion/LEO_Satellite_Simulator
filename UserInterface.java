package application;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;

public class UserInterface {

    private static final double WIDTH = 1400;
    private static final double HEIGHT = 1000;
    private Satellite satellite;
    private final ObservableList<VBox> satelliteDataList = FXCollections.observableArrayList();
    private enum VType {INT, DOUBLE}
    private Stage primaryStage;
    private Boolean initialSceneCreated = false;

    // For Testing purposes variables
    String testIntString = "10";
    String testString = "string";
    private final ComboBox<String> satelliteDropdown = new ComboBox<>();

    // Build the animation and provides the user inputs.
    public void build(Stage stage) {

        StackPane root = createInput(stage);

        Scene scene = new Scene(root, WIDTH, HEIGHT);
        scene.setFill(Color.NAVY); // Fallback background color

       ImageView backgroundImageView = prepareBackgroundImageView(scene);
       root.getChildren().add(0, backgroundImageView); // Add behind mainLayout

        stage.setScene(scene);
        stage.setTitle("Satellite Manager");
        stage.show();
    }

  //For creating background image
    private ImageView prepareBackgroundImageView(Scene scene) {
        ImageView imageView = new ImageView();
        try {
        	// Use file: protocol for absolute paths
            String appBackground = "file:C:/Computer Science Major/ProjectOrionV2/src/Images/appBackgroundCustom.png";
            Image image = new Image(appBackground);
            imageView.setImage(image);
            imageView.setPreserveRatio(false);
            // Bind fitWidth and fitHeight to scene's dimensions
            imageView.fitWidthProperty().bind(scene.widthProperty());
            imageView.fitHeightProperty().bind(scene.heightProperty());
        } catch (Exception e) {
            System.err.println("Error loading background image: " + e.getMessage());
        }
        return imageView;
    }

    // Resets the visibility and text fields
    private void resetNewSatellite(TextField idField, TextField massField, TextField areaField, TextField altitudeField, Button submitButton, Button deleteButton) {
        satellite = new Satellite(SessionData.getUserID(), "NewSatellite", 10, 10, 10);
        // Reset fields to default values
        idField.setText("NewSatellite");
        massField.setText("10");
        areaField.setText("10");
        altitudeField.setText("400000");

        // Enable fields and show Submit button
        idField.setDisable(false);
        massField.setDisable(false);
        areaField.setDisable(false);
        altitudeField.setDisable(false);
        submitButton.setVisible(true);
        deleteButton.setVisible(true);
    }

    // Shows the animation as well provides user Validation
    private void showAnimation(TextField idField, TextField massField, TextField areaField, TextField altitudeField, Stage stage) throws DatabaseError, ValidationError {
        // Validate satellite name length

        // __________________________________________________User Input______________________________________________________________________________

        String id = idField.getText().trim();
        if (id.length() < 3) {
            showAlert("Input Error", "Invalid Satellite Name", "Name must be at least three characters long.");
            return;
        }

        double mass, area, altitude;
        try {
            mass = Double.parseDouble(massField.getText());
            area = Double.parseDouble(areaField.getText());
            altitude = Double.parseDouble(altitudeField.getText());

        } catch (NumberFormatException nfe) {
            showAlert("Input Error", "Invalid numeric value.", "Mass, Area, and Altitude must be numeric.");
            return;
        }
        if (mass <= 0 || area <= 0  || altitude <= 0) {
            showAlert("Input Error", "Invalid numeric value.", "Mass, Area, and Altitude must be greater than 0.");
            return;
        }
        if (altitude > 2_000_000) {
            showAlert("Input Error", "Altitude too high.", "Low earth orbit is less than 2000km according to European Space Agency definitions, please consult programming team");
            return;
        }

        // __________________________________________________User Input______________________________________________________________________________


        SatelliteDataBaseManager dbManager = new SatelliteDataBaseManager();

        this.satellite = new Satellite(SessionData.getUserID(), id, mass, area, altitude);
        if (dbManager.isSatelliteNameExists(satellite.getId())) {
            throw new ValidationError(
                    "Duplicate satellite name",
                    "A satellite named “" + satellite.getId() + "” already exists."
            );
        }
        dbManager.addSatellite(satellite);

        // Perform calculations
        double period = OrbitalPeriod.calculatePeriod(satellite);
        double ballistic = Ballistic.calculateBallisticCoefficient(satellite);
        double totalOrbits = new NumOrbitsLifecycle(satellite).calculateNumberOfOrbits();
        String reentryFormattedTime = new Retrograde(satellite).getReentryTimeframe();

        // Create TabPane for simulation and UI tabs
        TabPane layout = new TabPane();
        layout.setStyle("-fx-background-color: transparent;");
        Tab simTab = new Tab("Simulation");

        // Simulation Tab
        Simulation simulation = new Simulation(satellite);
        SubScene simScene = simulation.getSubScene();
        simScene.setFill(Color.BLACK);

        VBox simLayout = new VBox(simScene);
        simTab.setContent(simLayout);

        // UI Tab
        ListView<VBox> listView = new ListView<>(satelliteDataList);
        listView.setPrefSize(300, 400);

        StackPane addSatellite = createInput(stage);
        ScrollPane scrollPane = new ScrollPane(listView);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        BorderPane uiPane = new BorderPane();
        uiPane.setStyle("-fx-background-color: transparent;");
        uiPane.setLeft(scrollPane);
        uiPane.setCenter(addSatellite);

        Tab uiTab = new Tab("UI Tab", uiPane);

        // Add tabs to TabPane
        layout.getTabs().addAll(simTab, uiTab);

        // Create and set new scene
        Scene resultScene = new Scene(layout, WIDTH, HEIGHT);
        resultScene.setFill(Color.BLACK);

        ImageView backgroundImageView = prepareBackgroundImageView(resultScene);
        addSatellite.getChildren().add(0, backgroundImageView); // Add behind mainLayout

        stage.setScene(resultScene);
        showSatelliteData(satellite, period, ballistic, totalOrbits, reentryFormattedTime);
    }

    // Loads the UserSatellite
    private void loadUserSatellites() throws DatabaseError {
        try {
            SatelliteDataBaseManager dbManager = new SatelliteDataBaseManager();
            String[] satelliteNames = dbManager.getSatelliteNamesAndIds();
            ObservableList<String> satellitesList = FXCollections.observableArrayList(satelliteNames);
            System.out.println("ObservableList: " + satellitesList);
            Platform.runLater(() -> {
                satelliteDropdown.setItems(satellitesList);
            });
        } catch (DatabaseError e) {
            throw new DatabaseError("Failed to load satellites for the user", e.getMessage());
        }
    }

    //Shows the satellite data on a scroll pane
    // For user readability
    public void showSatelliteData(Satellite satellite,
                                  double orbitalPeriod,
                                  double ballistic,
                                  double totalOrbits,
                                  String reentryFormattedTime) {

        String idString = "ID: " + satellite.getId();
        String massString = "Mass: " + satellite.getMass();
        String areaString = "Area: " + satellite.getArea();
        String altitudeString = "Altitude: " + satellite.getAltitude();
        String periodString = "Orbital Period: " + String.format("%.2f", orbitalPeriod) + " minutes";
        String ballisticString = "Ballistic Coefficient: " + String.format("%.2f", ballistic) + " kg/m^2 ";
        String orbitsString = "Total Orbits Before Reentry: " + String.format("%.0f", totalOrbits);
        String reentryString = "Estimated Reentry Time: " + reentryFormattedTime;

        Label idLabel = new Label(idString);
        Label massLabel = new Label(massString);
        Label areaLabel = new Label(areaString);
        Label altitudeLabel = new Label(altitudeString);
        Label periodLabel = new Label(periodString);
        Label ballisticLabel = new Label(ballisticString);
        Label orbitsLabel = new Label(orbitsString);
        Label reentryLabel = new Label(reentryString);

        VBox satelliteBox = new VBox(
                idLabel, massLabel, areaLabel, altitudeLabel,
                periodLabel, ballisticLabel, orbitsLabel, reentryLabel
        );

        satelliteBox.setAlignment(Pos.CENTER_LEFT);
        satelliteBox.setSpacing(5);
        satelliteBox.setStyle("-fx-padding: 10; -fx-border-color: gray; -fx-border-width: 1; -fx-background-color: transparent;");

        Button deleteDataBtn = new Button("Delete");
        deleteDataBtn.setOnAction(evt -> {
            deleteSatelliteBox(satelliteBox);
        });

        // Add it at the bottom
        satelliteBox.getChildren().add(deleteDataBtn);

        satelliteDataList.add(satelliteBox);
    }

    // Provides the category for creating input

    public StackPane createInput(Stage stage) {
        Label idLabel = new Label("Satellite Id");
        Label massLabel = new Label("Mass");
        Label areaLabel = new Label("Area");
        Label altitudeLabel = new Label("Altitude");

        // Main Layout
        VBox mainLayout;
        satelliteDropdown.setPromptText("Select a Satellite");
        ObservableList<String> satelliteList = FXCollections.observableArrayList();

        // Load ComboBox with satellite names
        try {
            loadUserSatellites(); // Populates satelliteList
        } catch (DatabaseError e) {
            System.err.println("Error loading satellites: " + e.getMessage());
        }

        // Text fields (initially disabled and smaller size)
        TextField idTextField = new TextField();
        idTextField.setPromptText("Satellite Name");
        idTextField.setDisable(true);
        idTextField.setPrefWidth(200);

        TextField massTextField = new TextField();
        massTextField.setPromptText("mass in Kg");
        validateText(massTextField, VType.DOUBLE);
        massTextField.setDisable(true);
        massTextField.setPrefWidth(200);

        TextField areaTextField = new TextField();
        areaTextField.setPromptText("area in square Meters");
        validateText(areaTextField, VType.DOUBLE);
        areaTextField.setDisable(true);
        areaTextField.setPrefWidth(200);

        TextField altitudeTextField = new TextField();
        altitudeTextField.setPromptText("altitude in meters");
        validateText(altitudeTextField, VType.DOUBLE);
        altitudeTextField.setDisable(true);
        altitudeTextField.setPrefWidth(200);

        // Submit Button (initially invisible)
        Button submitButton = new Button("Submit");
        submitButton.setVisible(false);

        Button exportButton = new Button("Export Satellites to Excel");
        exportButton.setOnAction(event -> {
            try {
                SatelliteDataBaseManager.exportSatellitesToExcel(SessionData.getUserID(),primaryStage);
            } catch (SQLException | IOException e) {
                throw new RuntimeException(e);
            }
            System.out.println("Satellite data exported to Excel successfully.");
        });

        //Delete Button
        Button deleteButton = new Button("Delete");
        deleteButton.setVisible(false);

        if (satelliteDropdown.getValue() != null) {
            idTextField.setText(satellite.getId());
            massTextField.setText(String.valueOf(satellite.getMass()));
            areaTextField.setText(String.valueOf(satellite.getArea()));
            altitudeTextField.setText(String.valueOf(satellite.getAltitude()));

            idTextField.setDisable(false);
            massTextField.setDisable(false);
            areaTextField.setDisable(false);
            altitudeTextField.setDisable(false);
            submitButton.setVisible(true);
            deleteButton.setVisible(true);
        } else {
            idTextField.setDisable(true);
            massTextField.setDisable(true);
            areaTextField.setDisable(true);
            altitudeTextField.setDisable(true);
            submitButton.setVisible(false);
            deleteButton.setVisible(false);
        }

        submitButton.setOnAction(e -> {
            if (!initialSceneCreated){
                submitButtonAction(idTextField, massTextField, areaTextField, altitudeTextField, stage);
                initialSceneCreated = true;

            }else {
                submitButtonAction(idTextField, massTextField, areaTextField, altitudeTextField, (Stage) submitButton.getScene().getWindow());
                ;
            }

        });

        satelliteDropdown.setOnAction(e -> {
            satelliteDropdownAction(idTextField,massTextField,areaTextField,altitudeTextField,submitButton, deleteButton);

        });

        Button newSatelliteButton = new Button("New Satellite");
        newSatelliteButton.setOnAction(e -> {
            resetNewSatellite(idTextField, massTextField, areaTextField, altitudeTextField, submitButton, deleteButton);
            satelliteDropdown.setValue("New Satellite");
        });

        HBox cboBox = new HBox(10, satelliteDropdown, newSatelliteButton);
        cboBox.setAlignment(Pos.CENTER);

        GridPane inputGrid = new GridPane();
        inputGrid.setHgap(10);
        inputGrid.setVgap(10);
        inputGrid.setStyle("-fx-padding: 20;");
        inputGrid.setAlignment(Pos.CENTER);

        inputGrid.add(idLabel, 0, 0);
        inputGrid.add(idTextField, 1, 0);
        inputGrid.add(massLabel, 0, 1);
        inputGrid.add(massTextField, 1, 1);
        inputGrid.add(areaLabel, 0, 2);
        inputGrid.add(areaTextField, 1, 2);
        inputGrid.add(altitudeLabel, 0, 3);
        inputGrid.add(altitudeTextField, 1, 3);

        HBox btnBox = new HBox(10, submitButton, deleteButton);
        if (initialSceneCreated){
            btnBox.getChildren().add(exportButton);
        }
        inputGrid.add(btnBox, 1, 4);

        idTextField.setPrefWidth(200);
        massTextField.setPrefWidth(200);
        areaTextField.setPrefWidth(200);
        altitudeTextField.setPrefWidth(200);

        //set main lay out
        mainLayout = new VBox(20, cboBox, inputGrid);
        mainLayout.setStyle("-fx-padding: 20; -fx-alignment: center; -fx-background-color: transparent;");

        // Use StackPane to layer background image behind mainLayout
        StackPane root = new StackPane();


        root.getChildren().add(mainLayout);


        deleteButton.setOnAction(event -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION,
                    "Are you sure you want to delete this satellite?",
                    ButtonType.YES, ButtonType.CANCEL);

            alert.setTitle("Confirm Deletion");
            alert.setHeaderText(null);
            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.YES) {
                    try {
                        if (SatelliteDataBaseManager.checkExistingSatellite(satellite.getId())) {
                            SatelliteDataBaseManager.deleteSatellite(satellite);
                        }
                    } catch (DatabaseError e) {
                        throw new RuntimeException(e);
                    }

                    resetNewSatellite(idTextField, massTextField, areaTextField, altitudeTextField, submitButton, deleteButton);
                    satelliteDropdown.setPromptText("Select a Satellite");
                    satelliteDropdown.setValue("New Satellite");
                    try {
                        loadUserSatellites();
                    } catch (DatabaseError e) {
                        System.err.println("Error loading satellites: " + e.getMessage());
                    }
                }
            });
        });

        return root;
    }


    // UI_Helper Functions
    // This method shows the alert message for specific user validation
    private void showAlert(String title, String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.getDialogPane().lookup(".content.label").setStyle("-fx-text-fill: red;");
        alert.showAndWait();
    }

    private static void validateText(TextField txtValidate, VType vType) {
        switch (vType) {
            case INT:
                txtValidate.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*")) {
                        txtValidate.setText(newValue.replaceAll("[^\\d]", ""));
                    }
                });
                break;
            case DOUBLE:
                txtValidate.textProperty().addListener((observable, oldValue, newValue) -> {
                    if (!newValue.matches("\\d*(\\.\\d*)?")) {
                        txtValidate.setText(newValue.replaceAll("[^\\d.]", ""));
                    }
                });
                break;
        }
    }

    // Button Actions
    // Deletes satellite from the view box and database
    private void deleteSatelliteBox(VBox satelliteBox){
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Permanently delete “" + satellite.getId() + "”?",
                ButtonType.YES, ButtonType.CANCEL);
        confirm.setTitle("Delete Satellite");
        confirm.setHeaderText(null);
        confirm.showAndWait().ifPresent(resp -> {
            if (resp == ButtonType.YES) {
                try {
                    // Delete from DB
                    SatelliteDataBaseManager.deleteSatellite(satellite);
                    // Remove from UI list
                    satelliteDataList.remove(satelliteBox);
                } catch (DatabaseError e) {
                    showAlert("Deletion Error", "Could not delete satellite", e.getMessage());
                }
            }
        });
    }
    // For handling drop down menu
    private void satelliteDropdownAction (TextField idTextField, TextField massTextField, TextField areaTextField,
                                          TextField altitudeTextField,Button submitButton, Button deleteButton) {
        String selectedSatellite = satelliteDropdown.getValue();
        if (selectedSatellite != null) {
            try {
                if (SatelliteDataBaseManager.checkExistingSatellite(selectedSatellite)) {
                    satellite = SatelliteDataBaseManager.getSatelliteDataByName(selectedSatellite); // Fetch satellite data

                    idTextField.setText(satellite.getId());
                    massTextField.setText(String.valueOf(satellite.getMass()));
                    areaTextField.setText(String.valueOf(satellite.getArea()));
                    altitudeTextField.setText(String.valueOf(satellite.getAltitude()));
                    idTextField.setDisable(false);
                    massTextField.setDisable(false);
                    areaTextField.setDisable(false);
                    altitudeTextField.setDisable(false);
                    submitButton.setVisible(true);
                    deleteButton.setVisible(true);
                } else {
                    resetNewSatellite(idTextField, massTextField, areaTextField, altitudeTextField, submitButton, deleteButton);
                    satelliteDropdown.setValue("New Satellite");
                }
            } catch (SQLException | DatabaseError ex) {
                System.err.println("Error loading satellite data: " + ex.getMessage());
            }
        }
    }
    // for submit actions
    private void submitButtonAction(TextField idTextField, TextField massTextField,
                                    TextField areaTextField, TextField altitudeTextField, Stage stage) {
        try {
            // Call showAnimation with the provided input fields and stage
            showAnimation(idTextField, massTextField, areaTextField, altitudeTextField, stage);
            satelliteDropdown.setValue(satellite.getId());
        } catch (DatabaseError dbError) {
            System.err.println("Database error: " + dbError.getMessage());
            showAlert("Database Error", "An Error Occurred", dbError.getMessage());

        } catch (ValidationError validationError) {
            System.err.println("Validation error: " + validationError.getMessage());
            showAlert("Validation Error", "Input Validation Failed", validationError.getMessage());

        } catch (Exception ex) {
            System.err.println("An unexpected error occurred: " + ex.getMessage());
            showAlert("Unexpected Error", "An Error Occured", ex.getMessage());

        }
    }





}