package com.garciasolutions.teupdv.models.entities;


import javafx.print.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class PrintManager {

    public Printer getSavedPrinter() {
        String url = "jdbc:sqlite:dbgs_restaurante.db"; // Substitua com o caminho do seu banco de dados
        String sql = "SELECT printer_name FROM printer_config ORDER BY id DESC LIMIT 1";
        Printer savedPrinter = null;

        try (Connection conn = DriverManager.getConnection(url);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                String printerName = rs.getString("printer_name");
                for (Printer printer : Printer.getAllPrinters()) {
                    if (printer.getName().equals(printerName)) {
                        savedPrinter = printer;
                        break;
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

        return savedPrinter;
    }



    // method



}

