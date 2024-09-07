package com.garciasolutions.teupdv.models.data;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnect {
    // Configurações para SQLite
    private static final String URL = "jdbc:sqlite:dbgs_restaurante.db"; // Arquivo local do banco de dados

    public static Connection connect() {
        try {

            return DriverManager.getConnection(URL);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static void main(String[] args) {
        // Testar a conexão
        connect();
    }
}
