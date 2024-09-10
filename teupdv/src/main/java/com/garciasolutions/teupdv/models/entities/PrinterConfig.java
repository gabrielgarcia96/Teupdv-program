package com.garciasolutions.teupdv.models.entities;

import javafx.collections.ObservableSet;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.print.Printer;
import javafx.print.PrinterJob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PrinterConfig {

    private Printer selectedPrinter;

    public void showPrinterConfigModal() {
        Stage stage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label label = new Label("Selecione a impressora:");
        ComboBox<Printer> printerComboBox = new ComboBox<>();
        Button saveButton = new Button("Salvar");

        // Adiciona o Label para mostrar a impressora selecionada
        Label selectedPrinterLabel = new Label("Impressora selecionada: Nenhuma");

        // Preenche o ComboBox com impressoras disponíveis
        ObservableSet<Printer> printers = Printer.getAllPrinters();
        printerComboBox.getItems().addAll(printers);

        // Carrega a impressora salva e define o valor do ComboBox
        Printer savedPrinter = new PrintManager().getSavedPrinter();
        if (savedPrinter != null) {
            printerComboBox.setValue(savedPrinter);
            selectedPrinterLabel.setText("Impressora selecionada: " + savedPrinter.getName());
        }

        // Atualiza o Label quando uma impressora é selecionada
        printerComboBox.setOnAction(e -> {
            selectedPrinter = printerComboBox.getValue();
            if (selectedPrinter != null) {
                selectedPrinterLabel.setText("Impressora selecionada: " + selectedPrinter.getName());
            } else {
                selectedPrinterLabel.setText("Impressora selecionada: Nenhuma");
            }
        });

        saveButton.setOnAction(e -> {
            if (selectedPrinter != null) {
                savePrinterToDatabase(selectedPrinter.getName());
                System.out.println("Impressora selecionada: " + selectedPrinter.getName());
                stage.close();
            } else {
                System.out.println("Nenhuma impressora selecionada.");
            }
        });

        vbox.getChildren().addAll(label, printerComboBox, selectedPrinterLabel, saveButton);
        stage.setScene(new Scene(vbox, 300, 200));
        stage.setTitle("Configuração da Impressora");
        stage.show();
    }


    private void savePrinterToDatabase(String printerName) {
        String url = "jdbc:sqlite:dbgs_restaurante.db"; // Substitua com o caminho do seu banco de dados
        String sql = "INSERT OR REPLACE INTO printer_config(id, printer_name) VALUES((SELECT id FROM printer_config ORDER BY id DESC LIMIT 1), ?)";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, printerName);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

}
