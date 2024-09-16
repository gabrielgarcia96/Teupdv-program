package com.garciasolutions.teupdv.models.controller;

import com.garciasolutions.teupdv.models.data.DatabaseConnect;
import com.garciasolutions.teupdv.models.data.DatabaseProduct;
import com.garciasolutions.teupdv.models.data.DatabaseUser;
import com.garciasolutions.teupdv.models.entities.AcessLevel;
import com.garciasolutions.teupdv.models.entities.Venda;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import javafx.stage.FileChooser;
import javafx.stage.FileChooser.ExtensionFilter;


public class OpenModalController {

    private DatabaseProduct databaseProduct;
    private Stage reportStage;


    public OpenModalController(DatabaseProduct databaseProduct) {
        this.databaseProduct = databaseProduct;
    }

    public void openReportModal() {
        reportStage = new Stage(); // Armazena a referência ao Stage
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        // Labels e DatePickers
        Label startDateLabel = new Label("Data Inicial:");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(30)); // Valor padrão, últimos 30 dias

        Label endDateLabel = new Label("Data Final:");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now()); // Valor padrão, data atual

        Button generateButton = new Button("Gerar Relatório");
        generateButton.setOnAction(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.WARNING, "Data Inválida", "Por favor, selecione ambas as datas.");
                return;
            }
            if (endDate.isBefore(startDate)) {
                showAlert(Alert.AlertType.WARNING, "Data Inválida", "A data final não pode ser anterior à data inicial.");
                return;
            }
            showSalesReport(startDate, endDate); // Chama o metodo com as datas
            reportStage.close();
        });

        vbox.getChildren().addAll(startDateLabel, startDatePicker, endDateLabel, endDatePicker, generateButton);
        reportStage.setScene(new Scene(vbox, 300, 200));
        reportStage.setTitle("Gerar Relatório de Vendas");
        reportStage.show();
    }

    public void openCancellationReportModal() {
        Stage cancelReportStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label startDateLabel = new Label("Data Inicial:");
        DatePicker startDatePicker = new DatePicker();
        startDatePicker.setValue(LocalDate.now().minusDays(30)); // Valor padrão, últimos 30 dias

        Label endDateLabel = new Label("Data Final:");
        DatePicker endDatePicker = new DatePicker();
        endDatePicker.setValue(LocalDate.now()); // Valor padrão, data atual

        Button generateButton = new Button("Gerar Relatório");
        generateButton.setOnAction(e -> {
            LocalDate startDate = startDatePicker.getValue();
            LocalDate endDate = endDatePicker.getValue();
            if (startDate == null || endDate == null) {
                showAlert(Alert.AlertType.WARNING, "Data Inválida", "Por favor, selecione ambas as datas.");
                return;
            }
            if (endDate.isBefore(startDate)) {
                showAlert(Alert.AlertType.WARNING, "Data Inválida", "A data final não pode ser anterior à data inicial.");
                return;
            }
            showCancellationReport(startDate, endDate); // Chama o metodo com as datas
            cancelReportStage.close();
        });

        vbox.getChildren().addAll(startDateLabel, startDatePicker, endDateLabel, endDatePicker, generateButton);
        cancelReportStage.setScene(new Scene(vbox, 300, 200));
        cancelReportStage.setTitle("Gerar Relatório de Cancelamentos");
        cancelReportStage.show();
    }


    public void openFiscalRegistrationModal() {
        Stage fiscalStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label cnpjLabel = new Label("CNPJ:");
        TextField cnpjTextField = new TextField();
        cnpjTextField.setMaxWidth(200);

        Label nameLabel = new Label("Nome da Empresa:");
        TextField nameTextField = new TextField();
        nameTextField.setMaxWidth(200);

        Label addressLabel = new Label("Endereço:");
        TextField addressTextField = new TextField();
        addressTextField.setMaxWidth(200);

        Label cityLabel = new Label("Cidade:");
        TextField cityTextField = new TextField();
        cityTextField.setMaxWidth(200);

        Label cepLabel = new Label("CEP:");
        TextField cepTextField = new TextField();
        cepTextField.setMaxWidth(200);

        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> {
            String cnpj = cnpjTextField.getText();
            String name = nameTextField.getText();
            String address = addressTextField.getText();
            String city = cityTextField.getText();
            String cep = cepTextField.getText();

            // Validar campos
            if (cnpj.isEmpty() || name.isEmpty() || address.isEmpty() || city.isEmpty() || cep.isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Todos os campos devem ser preenchidos.");
                return;
            }

            // Salvar dados no banco de dados
            databaseProduct.saveFiscalData(cnpj, name, address, city, cep);
            fiscalStage.close();
        });

        vbox.getChildren().addAll(cnpjLabel, cnpjTextField, nameLabel, nameTextField, addressLabel, addressTextField, cityLabel, cityTextField, cepLabel, cepTextField, saveButton);
        fiscalStage.setScene(new Scene(vbox, 300, 350));
        fiscalStage.setTitle("Cadastro Fiscal");
        fiscalStage.show();
    }

    public void openUserRegistrationModal() {
        Stage userStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        // Criar os componentes do formulário
        Label usernameLabel = new Label("Nome de Usuário:");
        TextField usernameTextField = new TextField();
        usernameTextField.setMaxWidth(200);

        Label passwordLabel = new Label("Senha:");
        PasswordField passwordField = new PasswordField();
        passwordField.setMaxWidth(200);

        Label roleLabel = new Label("Tipo de Usuário:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Gestor", "Caixa");
        roleComboBox.setValue("Gestor"); // Valor padrão

        Button registerButton = new Button("Cadastrar");
        registerButton.setOnAction(e -> {
            String username = usernameTextField.getText().trim();
            String password = passwordField.getText().trim();
            String role = roleComboBox.getValue();

            if (username.isEmpty() || password.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Campos Vazios", "Por favor, preencha todos os campos.");
                return;
            }

            // Define o nível de acesso com base no tipo de usuário
            int accessLevel = role.equals("Gestor") ? 2 : 3;

            DatabaseUser dbUser = new DatabaseUser();
            boolean success = dbUser.registerUser(username, password, accessLevel);

            if (success) {
                showAlert(Alert.AlertType.INFORMATION, "Sucesso", "Usuário cadastrado com sucesso.");
            } else {
                showAlert(Alert.AlertType.ERROR, "Erro", "Não foi possível cadastrar o usuário.");
            }

            userStage.close();
        });

        vbox.getChildren().addAll(usernameLabel, usernameTextField, passwordLabel, passwordField, roleLabel, roleComboBox, registerButton);
        userStage.setScene(new Scene(vbox, 300, 250));
        userStage.setTitle("Cadastro de Usuário");
        userStage.show();
    }


    public void showSalesReport(LocalDate startDate, LocalDate endDate) {
        Stage reportDisplayStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        MenuBar menuBar = new MenuBar();
        Menu reportMenu = new Menu("Tipos de Pagamento");
        menuBar.getMenus().add(reportMenu);

        MenuItem cashItem = new MenuItem("Dinheiro");
        MenuItem creditCardItem = new MenuItem("Cartão de Crédito");
        MenuItem debitCardItem = new MenuItem("Cartão de Débito");
        MenuItem pixItem = new MenuItem("Pix");
        MenuItem allItem = new MenuItem("Todos");

        reportMenu.getItems().addAll(cashItem, creditCardItem, debitCardItem, pixItem, allItem);

        TableView<Venda> tableView = new TableView<>();
        TableColumn<Venda, String> codeColumn = new TableColumn<>("Código");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("codigoProduto"));

        TableColumn<Venda, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));

        TableColumn<Venda, Double> priceColumn = new TableColumn<>("Preço");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("preco"));
        priceColumn.setCellFactory(column -> new TableCell<Venda, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(currencyFormat.format(item).replace("R$", "R$ ").replace(".", ","));
                }
            }
        });

        TableColumn<Venda, Integer> quantityColumn = new TableColumn<>("Quantidade");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));

        TableColumn<Venda, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        totalColumn.setCellFactory(column -> new TableCell<Venda, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(currencyFormat.format(item).replace("R$", "R$ ").replace(".", ","));
                }
            }
        });

        TableColumn<Venda, LocalDate> dateColumn = new TableColumn<>("Data");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
        dateColumn.setCellFactory(column -> new TableCell<Venda, LocalDate>() {
            private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));
            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(dateFormat.format(item));
                }
            }
        });



        TableColumn<Venda, String> paymentMethodColumn = new TableColumn<>("Forma de Pagamento");
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));

        tableView.getColumns().addAll(codeColumn, nameColumn, priceColumn, quantityColumn, totalColumn, dateColumn, paymentMethodColumn);

        Label totalLabel = new Label();
        totalLabel.setStyle("-fx-font-size: 14px;");

        // Adicionar o botão de cancelamento
        Button cancelButton = new Button("Cancelar");
        cancelButton.setPadding(new Insets(10, 10, 10, 10));
        cancelButton.setOnAction(e -> {
            Venda vendaSelecionada = tableView.getSelectionModel().getSelectedItem();
            if (vendaSelecionada != null) {
                System.out.println("Venda selecionada: ID = " + vendaSelecionada.getId());
                openCancelModal(vendaSelecionada);
            } else {
                System.out.println("Nenhuma venda selecionada para cancelar.");
            }
        });

        // Botão de Baixar PDF
        Button downloadPdfButton = new Button("Baixar PDF");
        downloadPdfButton.setPadding(new Insets(10, 10, 10, 10));
        downloadPdfButton.setOnAction(e -> {
            double totalSales = calculateTotalSales(tableView); // Calcular o total das vendas
            generatePdfReport(tableView, startDate, endDate, totalSales);
        });

        // Adicionar EventHandlers aos MenuItems
        cashItem.setOnAction(e -> updateTableAndTotal(startDate, endDate, tableView, totalLabel, "Dinheiro"));
        creditCardItem.setOnAction(e -> updateTableAndTotal(startDate, endDate, tableView, totalLabel, "Cartão de Crédito"));
        debitCardItem.setOnAction(e -> updateTableAndTotal(startDate, endDate, tableView, totalLabel, "Cartão de Débito"));
        pixItem.setOnAction(e -> updateTableAndTotal(startDate, endDate, tableView, totalLabel, "Pix"));
        allItem.setOnAction(e -> updateTableAndTotal(startDate, endDate, tableView, totalLabel, null)); // Mostrar todas as vendas

        VBox vboxContainer = new VBox(10);
        vboxContainer.setAlignment(Pos.CENTER);
        vboxContainer.getChildren().addAll(tableView, totalLabel, downloadPdfButton);
        VBox.setMargin(cancelButton, new Insets(10, 10, 10, 10));

        BorderPane bottomPane = new BorderPane();
        bottomPane.setRight(cancelButton);

        BorderPane borderPane = new BorderPane();
        borderPane.setTop(menuBar);
        borderPane.setCenter(vboxContainer);
        borderPane.setBottom(bottomPane);

        Scene scene = new Scene(borderPane, 1024, 600);
        reportDisplayStage.setScene(scene);
        reportDisplayStage.setTitle("Relatório de Vendas");

        // Chama o metodo com todas as vendas ao abrir o relatório
        updateTableAndTotal(startDate, endDate, tableView, totalLabel, null);

        reportDisplayStage.show();
    }

    private double calculateTotalSales(TableView<Venda> tableView) {
        double total = 0.0;
        for (Venda venda : tableView.getItems()) {
            if (venda.getTotal() != null) {
                total += venda.getTotal();
            }
        }
        return total;
    }

    private void generatePdfReport(TableView<Venda> tableView, LocalDate startDate, LocalDate endDate, double totalSales) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(file));
                document.open();

                // Fontes
                Font titleFont = new Font(Font.FontFamily.HELVETICA, 16, Font.BOLD);
                Font textFont = new Font(Font.FontFamily.HELVETICA, 10, Font.NORMAL);

                // Título com intervalo de datas
                document.add(new Paragraph("Relatório de Vendas", titleFont));
                document.add(new Paragraph(String.format("Período: %s a %s", startDate.format(formatter), endDate.format(formatter)), textFont));

                // Adiciona espaçamento antes do total das vendas
                document.add(new Paragraph(" ")); // Adiciona uma linha em branco para espaçamento

                // Total de vendas
                document.add(new Paragraph(String.format("Total das Vendas: R$ %.2f", totalSales), textFont));

                // Adiciona espaçamento adicional antes da tabela
                document.add(new Paragraph(" ")); // Adiciona uma linha em branco para espaçamento

                // Cabeçalho da Tabela
                PdfPTable table = new PdfPTable(7); // Número de colunas
                table.setWidths(new float[]{1f, 2f, 2f, 1.5f, 2f, 2f, 2f}); // Definindo a largura das colunas

                table.addCell(createCell("Código", textFont));
                table.addCell(createCell("Nome", textFont));
                table.addCell(createCell("Preço", textFont));
                table.addCell(createCell("Quantidade", textFont));
                table.addCell(createCell("Total", textFont));
                table.addCell(createCell("Data", textFont));
                table.addCell(createCell("Forma de Pagamento", textFont));

                // Adiciona as linhas da TableView
                for (Venda venda : tableView.getItems()) {
                    table.addCell(createCell(venda.getCodigoProduto() != null ? venda.getCodigoProduto() : "", textFont));
                    table.addCell(createCell(venda.getNomeProduto() != null ? venda.getNomeProduto() : "", textFont));
                    table.addCell(createCell(venda.getPreco() != null ? String.format("R$ %.2f", venda.getPreco()) : "R$ 0,00", textFont));
                    table.addCell(createCell(venda.getQuantidade() != null ? String.valueOf(venda.getQuantidade()) : "0", textFont));
                    table.addCell(createCell(venda.getTotal() != null ? String.format("R$ %.2f", venda.getTotal()) : "R$ 0,00", textFont));
                    table.addCell(createCell(venda.getData() != null ? venda.getData().toString() : "N/A", textFont));
                    table.addCell(createCell(venda.getPaymentMethod() != null ? venda.getPaymentMethod() : "N/A", textFont));
                }

                document.add(table);
                document.close();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private PdfPCell createCell(String text, Font font) {
        PdfPCell cell = new PdfPCell(new Paragraph(text, font));
        cell.setPadding(5); // Adiciona padding interno para cada célula
        return cell;
    }



    private void updateTableAndTotal(LocalDate startDate, LocalDate endDate, TableView<Venda> tableView, Label totalLabel, String paymentMethodFilter) {
        // Atualizar a lista de vendas
        List<Venda> reportData = databaseProduct.getSalesReport(startDate, endDate);

        // Filtrar as vendas não canceladas
        List<Venda> filteredData = reportData.stream()
                .filter(venda -> !venda.isCancelada()) // Filtra vendas não canceladas
                .collect(Collectors.toList());

        // Filtrar por metodo de pagamento se especificado
        if (paymentMethodFilter != null) {
            filteredData = filteredData.stream()
                    .filter(venda -> paymentMethodFilter.equals(venda.getPaymentMethod()))
                    .collect(Collectors.toList());
        }

        // Atualizar a tabela
        tableView.getItems().setAll(filteredData);

        // Calcular o total das vendas não canceladas
        double grandTotal = filteredData.stream()
                .mapToDouble(Venda::getTotal)
                .sum();

        totalLabel.setText("Total das Vendas: R$" + String.format("%.2f", grandTotal));
    }

    public void showCancellationReport(LocalDate startDate, LocalDate endDate) {
        Stage reportDisplayStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        TableView<Venda> tableView = new TableView<>();
        TableColumn<Venda, String> codeColumn = new TableColumn<>("Código");
        codeColumn.setCellValueFactory(new PropertyValueFactory<>("codigoProduto"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        codeColumn.setCellFactory(column -> new TableCell<Venda, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                }
            }
        });
        TableColumn<Venda, String> nameColumn = new TableColumn<>("Nome");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("nomeProduto"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        nameColumn.setCellFactory(column -> new TableCell<Venda, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                }
            }
        });
        TableColumn<Venda, Double> priceColumn = new TableColumn<>("Preço");
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("preco"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        priceColumn.setCellFactory(column -> new TableCell<Venda, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                    setText(currencyFormat.format(item).replace("R$", "R$ ").replace(".", ","));
                }
            }
        });

        TableColumn<Venda, Integer> quantityColumn = new TableColumn<>("Quantidade");
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        quantityColumn.setCellFactory(column -> new TableCell<Venda, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                }
            }
        });
        TableColumn<Venda, Double> totalColumn = new TableColumn<>("Total");
        totalColumn.setCellValueFactory(new PropertyValueFactory<>("total"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        totalColumn.setCellFactory(column -> new TableCell<Venda, Double>() {
            private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(String.valueOf(item));
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                    setText(currencyFormat.format(item).replace("R$", "R$ ").replace(".", ","));
                }
            }
        });

        TableColumn<Venda, LocalDate> dateColumn = new TableColumn<>("Data");
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("data"));
        dateColumn.setCellFactory(column -> new TableCell<Venda, LocalDate>() {
            private final DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy", new Locale("pt", "BR"));

            @Override
            protected void updateItem(LocalDate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(dateFormat.format(item));
                    setStyle("-fx-text-fill: red;"); // Estilo para células, ajuste conforme necessário
                }
            }
        });

        TableColumn<Venda, String> paymentMethodColumn = new TableColumn<>("Forma de Pagamento");
        paymentMethodColumn.setCellValueFactory(new PropertyValueFactory<>("paymentMethod"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        paymentMethodColumn.setCellFactory(column -> new TableCell<Venda, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                }
            }
        });
        TableColumn<Venda, String> cancelReasonColumn = new TableColumn<>("Motivo do Cancelamento");
        cancelReasonColumn.setCellValueFactory(new PropertyValueFactory<>("motivoCancelamento"));
        // Estilizar as células para exibir em vermelho se a venda estiver cancelada
        cancelReasonColumn.setCellFactory(column -> new TableCell<Venda, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: red;"); // Estilo para células com motivo
                }
            }
        });


        tableView.getColumns().addAll(codeColumn, nameColumn, priceColumn, quantityColumn, totalColumn, dateColumn, paymentMethodColumn, cancelReasonColumn);

        // Atualizar a lista de vendas para incluir apenas as canceladas
        List<Venda> reportData = databaseProduct.getCancelledSalesReport(startDate, endDate);
        tableView.getItems().addAll(reportData);

        // Calcular o total das vendas canceladas
        double grandTotal = reportData.stream()
                .mapToDouble(Venda::getTotal)
                .sum();

        Label totalLabel = new Label("Total das Vendas Canceladas: R$" + String.format("%.2f", grandTotal));
        totalLabel.setStyle("-fx-font-size: 14px;");

        vbox.getChildren().addAll(tableView, totalLabel);
        reportDisplayStage.setScene(new Scene(vbox, 800, 600));
        reportDisplayStage.setTitle("Relatório de Cancelamentos");
        reportDisplayStage.show();
    }



    private void openCancelModal(Venda venda) {
        Stage cancelStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label reasonLabel = new Label("Motivo:");
        TextField reasonTextField = new TextField(); // Inicialização do campo de texto
        reasonTextField.setMaxWidth(200);
        Label passwordLabel = new Label("Senha:");
        PasswordField passwordField = new PasswordField(); // Inicialização do campo de senha
        passwordField.setMaxWidth(200);

        Button cancelButton = new Button("Confirmar Cancelamento");
        cancelButton.setOnAction(e -> {
            String reason = reasonTextField.getText().trim();
            String password = passwordField.getText().trim();

            if (reason.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Motivo Obrigatório", "Por favor, insira um motivo para o cancelamento.");
                return;
            }

            if (!validatePassword(password)) {
                showAlert(Alert.AlertType.ERROR, "Senha Inválida", "Senha incorreta ou usuário sem permissão. Apenas usuários com nível 1 ou 2 podem cancelar vendas.");
                return;
            }

            System.out.println("Cancelando venda com ID: " + venda.getId()); // Debug: Verifique o ID

            cancelSale(venda.getId(), reason);
            cancelStage.close();
        });

        vbox.getChildren().addAll(reasonLabel, reasonTextField, passwordLabel, passwordField, cancelButton);
        cancelStage.setScene(new Scene(vbox, 400, 250));
        cancelStage.setTitle("Cancelar Venda");
        cancelStage.show();
    }


    public String normalizePrice(String price) {
        if (price == null || price.isEmpty()) {
            return "0.00"; // Retorna um valor padrão se o campo estiver vazio
        }
        // Substitui a vírgula por ponto
        return price.replace(',', '.');
    }


    private boolean validatePassword(String password) {
        DatabaseUser dbUser = new DatabaseUser();

        // Verifica se há algum usuário com a senha fornecida
        String username = dbUser.getUsernameByPassword(password);
        if (username == null) {
            return false; // Senha não corresponde a nenhum usuário
        }

        // Verifica o nível de acesso do usuário com a senha fornecida
        AcessLevel accessLevel = dbUser.getUserAccessLevel(username, password);
        return accessLevel != null && (accessLevel == AcessLevel.Master || accessLevel == AcessLevel.Gestor);
    }



    public void cancelSale(int id, String reason) {
        String sql = "UPDATE vendas SET cancelada = true, motivo_cancelamento = ? WHERE id = ?";

        try (Connection conn = DatabaseConnect.connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, reason);
            pstmt.setInt(2, id);

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("Venda cancelada com sucesso!");
            } else {
                System.out.println("Nenhuma venda encontrada com o ID fornecido.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }






    public void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}