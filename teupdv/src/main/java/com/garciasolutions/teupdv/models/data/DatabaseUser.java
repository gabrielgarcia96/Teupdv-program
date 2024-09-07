package com.garciasolutions.teupdv.models.data;

import com.garciasolutions.teupdv.models.entities.AcessLevel;
import com.garciasolutions.teupdv.models.view.DashboardView;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.sql.*;

import static com.garciasolutions.teupdv.models.data.DatabaseConnect.connect;

public class DatabaseUser {



    public boolean authenticateUser(String username, String password) {
        boolean isAuthenticated = false;
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Be cautious with plain text passwords. Consider hashing.
            try (ResultSet rs = stmt.executeQuery()) {
                isAuthenticated = rs.next(); // If a record is found, authentication is successful
            }

        } catch (Exception e) {
            e.printStackTrace(); // Handle exceptions appropriately
        }
        return isAuthenticated;
    }

    public AcessLevel getUserAccessLevel(String username, String password) {
        String query = "SELECT access_level FROM users WHERE username = ? AND password = ?";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                int level = rs.getInt("access_level");
                switch (level) {
                    case 1:
                        return AcessLevel.Master;
                    case 2:
                        return AcessLevel.Gestor;
                    case 3:
                        return AcessLevel.Caixa;
                    default:
                        throw new IllegalArgumentException("Unknown access level: " + level);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean registerUser(String username, String password, int accessLevel) {
        String sql = "INSERT INTO users (username, password, access_level) VALUES (?, ?, ?)";
        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password); // Consider hashing the password
            stmt.setInt(3, accessLevel);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0; // Return true if the user was successfully inserted

        } catch (Exception e) {
            e.printStackTrace();
            return false; // Return false if there was an error
        }
    }

    public void openDashboard(AcessLevel level) {
        try {
            DashboardView dashboard = new DashboardView(level);
            Stage stage = new Stage();
            dashboard.start(stage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getUsernameByPassword(String password) {
        String sql = "SELECT username FROM users WHERE password = ?";
        try (Connection conn = DatabaseConnect.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, password);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Login Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
