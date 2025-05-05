package application;

import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;

public class SatelliteDataBaseManager {

    public static void exportSatellitesToExcel(int userId, Stage stage) throws SQLException, IOException {
        // Use FileChooser to let the user select the file path
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Satellite Data");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xls"));
        File selectedFile = fileChooser.showSaveDialog(stage);

        // Check if the user selected a file
        if (selectedFile == null) {
            System.out.println("Export cancelled by the user.");
            return; // Exit if no file was selected
        }

        String filePath = selectedFile.getAbsolutePath();

        // Define SQL query to fetch satellite data
        String query = "SELECT \"SatelliteName\", \"Mass\", \"Area\", \"Altitude\" FROM public.\"Satellite\" WHERE \"User_ID\" = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            // Start writing the Excel XML content
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write the XML header
                writer.write("<?xml version=\"1.0\"?>\n");
                writer.write("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\"\n");
                writer.write(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n");
                writer.write(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n");
                writer.write(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");
                // Define styles
                writer.write("<Styles>\n");

// Header Style: Green background and bold borders
                writer.write("<Style ss:ID=\"Header\">\n");
                writer.write("<Interior ss:Color=\"#00FF00\" ss:Pattern=\"Solid\"/>\n"); // Green background
                writer.write("<Borders>\n");
                writer.write("<Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("<Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("<Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("<Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("</Borders>\n");
                writer.write("</Style>\n");

// Data Cell Style: Borders for table cells
                writer.write("<Style ss:ID=\"Data\">\n");
                writer.write("<Borders>\n");
                writer.write("<Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("<Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("<Border ss:Position=\"Top\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("<Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\"/>\n");
                writer.write("</Borders>\n");
                writer.write("</Style>\n");

// Default Style: No borders, plain appearance
                writer.write("<Style ss:ID=\"Default\">\n");
                writer.write("<Font ss:Color=\"#000000\"/>\n"); // Black text, no special formatting
                writer.write("</Style>\n");

                writer.write("</Styles>\n");

// Begin worksheet and table
                writer.write("<Worksheet ss:Name=\"User Satellites\">\n");
                writer.write("<Table>\n");
                //columns
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"125\"/>\n"); // Satellite Name column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"50\"/>\n"); // Mass column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"50\"/>\n"); // Area column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"50\"/>\n"); // Altitude column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"50\"/>\n"); // Period column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"50\"/>\n"); // Ballistics column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"50\"/>\n"); // TotalOrbits column
                writer.write("<Column ss:AutoFitWidth=\"1\" ss:Width=\"120\"/>\n"); // ReentryFormattedTime column
// Write the header row
                writer.write("<Row>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Satellite Name</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Mass</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Area</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Altitude</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Period</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Ballistics</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">TotalOrbits</Data></Cell>\n");
                writer.write("<Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">ReentryFormattedTime</Data></Cell>\n");
                writer.write("</Row>\n");

// Write the satellite data rows
                while (rs.next()) {
                    Satellite satellite = new Satellite(SessionData.getUserID(),rs.getString("SatelliteName"),rs.getInt("Mass"),rs.getInt("Area"),rs.getInt("Altitude"));
                    // Perform calculations
                    double period = OrbitalPeriod.calculatePeriod(satellite);
                    double ballistic = Ballistic.calculateBallisticCoefficient(satellite);
                    double totalOrbits = new NumOrbitsLifecycle(satellite).calculateNumberOfOrbits();
                    String reentryFormattedTime = new Retrograde(satellite).getReentryTimeframe();

                    writer.write("<Row>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"String\">" + rs.getString("SatelliteName") + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"Number\">" + rs.getDouble("Mass") + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"Number\">" + rs.getDouble("Area") + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"Number\">" + rs.getDouble("Altitude") + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"Number\">" + period + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"Number\">" + ballistic + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"Number\">" + totalOrbits + "</Data></Cell>\n");
                    writer.write("<Cell ss:StyleID=\"Data\"><Data ss:Type=\"String\">" + reentryFormattedTime + "</Data></Cell>\n");
                    writer.write("</Row>\n");
                }

// Close the Table and Worksheet tags
                writer.write("</Table>\n");

// Apply autofilter to the table
                writer.write("<AutoFilter x:Range=\"R1C1:R1C8\" xmlns=\"urn:schemas-microsoft-com:office:excel\"/>\n");

                writer.write("</Worksheet>\n");




                writer.write("</Workbook>\n");

            }
        }

        System.out.println("Satellite data exported successfully to: " + filePath);
    }



    // Method to get a Satellite by ID
        public Satellite getSatelliteById(int id) throws DatabaseError {
            Satellite satellite = null;
            String sql = "SELECT * FROM \"Satellite\" WHERE \"Satellite_ID\" = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    satellite = new Satellite(rs.getInt("User_ID"), rs.getString("SatelliteName"),rs.getDouble("Mass"),rs.getDouble("Area"), rs.getDouble("Altitude"));
                    satellite.setSatellite_ID(rs.getInt("Satellite_id"));

                } else {
                    throw new DatabaseError("Satellite not found", "ID: " + id);
                }
            } catch (SQLException e) {
                throw new DatabaseError("Database connection failed", e.getMessage());
            }
            return satellite;
        }
    //getSatelliteDataByName fetches satellite data based on the selected name:
    public static Satellite getSatelliteDataByName(String satelliteName) throws SQLException {
        try (Connection conn =DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM \"Satellite\" WHERE \"SatelliteName\" = ? and \"User_ID\" = ?";
            PreparedStatement stmt = conn.prepareStatement(query);
            stmt.setString(1, satelliteName);
            stmt.setInt(2, SessionData.getUserID());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int satellite_ID = rs.getInt("Satellite_ID");
                int user_ID = rs.getInt("User_ID");
                String id = satelliteName; // Use the satellite name as the ID
                double mass = rs.getDouble("Mass");
                double area = rs.getDouble("Area");
                double altitude = rs.getDouble("Altitude");

                // Create and return a Satellite object
                Satellite satellite = new Satellite(user_ID, id, mass, area, altitude);
                satellite.setSatellite_ID(satellite_ID); // Set the satellite_ID directly
                return satellite;
            }
        }
        throw new SQLException("No satellite found with name: " + satelliteName);
    }

    // Method to insert a new Satellite into the database
        public void addSatellite(Satellite satellite) throws ValidationError, DatabaseError {
            if (satellite.getMass() <= 0) {
                throw new ValidationError("Invalid mass", "Mass must be positive. Given: " + satellite.getMass());
            }

            if (checkExistingSatellite(satellite.getId())) {
                satellite.setSatellite_ID(getSatelliteIdByName(satellite.getId()));
                updateSatellite(satellite);
            }else {
                String sql = "INSERT INTO \"Satellite\" (\"SatelliteName\", \"User_ID\", \"Mass\", \"Altitude\", \"Area\") " +
                        "VALUES (?, ?, ?, ?, ?)";
                try (Connection conn = DatabaseConnection.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {
                    pstmt.setString(1, satellite.getId());
                    pstmt.setInt(2, satellite.getUser_Id());
                    pstmt.setDouble(3, satellite.getMass());
                    pstmt.setDouble(4, satellite.getAltitude());
                    pstmt.setDouble(5, satellite.getArea());
                    pstmt.executeUpdate();
                    satellite.setSatellite_ID(getSatelliteIdByName(satellite.getId()));
                } catch (SQLException e) {
                    throw new DatabaseError("Failed to insert satellite", e.getMessage());
                }
            }
        }
    public static boolean checkExistingSatellite(String satelliteName) throws DatabaseError {
        String sql = "SELECT COUNT(*) AS count FROM \"Satellite\" WHERE \"SatelliteName\" = ? and \"User_ID\" = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, satelliteName);
            pstmt.setInt(2, SessionData.getUserID());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                return count > 0; // Return true if count > 0 (satellite exists)
            }
        } catch (SQLException e) {
            throw new DatabaseError("Database connection failed", e.getMessage());
        }
        return false; // Return false if no satellite found
    }


    // New Method: Delete a Satellite by ID
        public void deleteSatelliteById(int id) throws DatabaseError {
            String sql = "DELETE FROM \"Satellite\" WHERE \"Satellite_ID\" = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setInt(1, id);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DatabaseError("Satellite deletion failed", "No satellite found with ID: " + id);
                }
            } catch (SQLException e) {
                throw new DatabaseError("Database connection failed", e.getMessage());
            }
        }

        // New Method: Update a Satellite's Information
        public void updateSatellite(Satellite satellite) throws ValidationError, DatabaseError {
            if (satellite.getMass() <= 0) {
                throw new ValidationError("Invalid mass", "Mass must be positive. Given: " + satellite.getMass());
            }

            String sql = "UPDATE public.\"Satellite\" SET \"SatelliteName\" = ?, \"User_ID\" = ?, \"Mass\" = ?, \"Altitude\" = ?, " +
                    "\"Area\" = ? WHERE \"Satellite_ID\" = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, satellite.getId());
                pstmt.setLong(2, satellite.getUser_Id());
                pstmt.setDouble(3, satellite.getMass());
                pstmt.setDouble(4, satellite.getAltitude());
                pstmt.setDouble(5, satellite.getArea());
                pstmt.setInt(6, satellite.getSatellite_ID());
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected == 0) {
                    throw new DatabaseError("Satellite update failed", "No satellite found with ID: " + satellite.getSatellite_ID());
                }
            } catch (SQLException e) {
                throw new DatabaseError("Database connection failed", e.getMessage());
            }
        }
        public int getSatelliteIdByName(String satelliteName) throws DatabaseError {
            String sql = "SELECT \"Satellite_ID\" FROM public.\"Satellite\" WHERE \"SatelliteName\" = ? and \"User_ID\" = ?";
            try (Connection conn = DatabaseConnection.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, satelliteName);
                pstmt.setInt(2, SessionData.getUserID());
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("Satellite_ID");
                } else {
                    throw new DatabaseError("Satellite not found", "Name: " + satelliteName);
                }
            } catch (SQLException e) {
                throw new DatabaseError("Database connection failed", e.getMessage());
            }
        }
    public String[] getSatelliteNamesAndIds() throws DatabaseError {
        String sql = "SELECT \"Satellite_ID\", \"SatelliteName\" FROM public.\"Satellite\" WHERE \"User_ID\" = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                     sql,
                     ResultSet.TYPE_SCROLL_INSENSITIVE, // Makes the ResultSet scrollable
                     ResultSet.CONCUR_READ_ONLY         // Makes it read-only
             )) {
            // Set the parameter for USER_ID
            pstmt.setInt(1, SessionData.getUserID());

            ResultSet rs = pstmt.executeQuery();

            // Determine the largest Satellite_ID for array size
            int maxId = 0;
            while (rs.next()) {
                maxId ++;
            }

            // Create an array of satellite names indexed by Satellite_ID
            String[] satelliteNames = new String[maxId + 1];
            rs.beforeFirst(); // Reset the cursor to process rows again
            rs.next();
            for( int i =1;  i <=  maxId; i++){

                satelliteNames[i] = rs.getString("SatelliteName");
                rs.next();
            }
            satelliteNames[0] = "New Satellite";
            return satelliteNames;

        } catch (SQLException e) {
            throw new DatabaseError("Database connection failed", e.getMessage());
        }
    }
    public static void deleteSatellite(Satellite satellite) throws DatabaseError {

        String sql = "DELETE FROM public.\"Satellite\" WHERE \"Satellite_ID\" = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, satellite.getSatellite_ID()); // Sets the parameter in the query
            pstmt.executeUpdate(); // Executes the delete command

        } catch (SQLException e) {
            System.out.println("Error deleting satellite: " + e.getMessage());
        }

    }

    public boolean isSatelliteNameExists(String satelliteName) throws DatabaseError {
        return checkExistingSatellite(satelliteName);
    }




}