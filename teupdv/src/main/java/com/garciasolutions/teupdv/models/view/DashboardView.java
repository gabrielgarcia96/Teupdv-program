package com.garciasolutions.teupdv.models.view;

import com.garciasolutions.teupdv.models.controller.OpenModalController;
import com.garciasolutions.teupdv.models.data.DatabaseProduct;
import com.garciasolutions.teupdv.models.entities.*;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.print.*;
import javafx.print.Paper;
import javafx.print.PrinterJob;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.Alert;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javafx.print.Printer;
import java.util.List;

public class DashboardView extends Application {

    private final AcessLevel accessLevel;
    private DatabaseProduct databaseProduct;
    private ListView<String> salesListView;
    private OpenModalController openModalController;
    private Label totalLabel;
    private double totalAmount = 0.0;
    private Map<String, SelectedProduct> selectedProducts = new HashMap<>();
    private static final String VERSION_FILE_PATH = "C:/teupdv_data/version.properties";
    private PrintManager printManager;
    private Button removeButton;
    private Button payButton;

    public DashboardView(AcessLevel accessLevel) {
        this.accessLevel = accessLevel;
    }

    private TextField searchField;

    private Stage searchStage;
    private Stage productListStage;
    private Stage cadStage;

    @Override
    public void start(Stage stage) throws Exception {
        databaseProduct = new DatabaseProduct();
        openModalController = new OpenModalController(databaseProduct);
        printManager = new PrintManager();

        // Menu
        MenuBar menuBar = new MenuBar();
        Menu menuProducts = new Menu("Produtos");
        MenuItem listProducts = new MenuItem("Lista de Produtos");
        MenuItem cadProducts = new MenuItem("Cadastrar Produtos");
        MenuItem subProducts = new MenuItem("Cadastrar SubProdutos");
        MenuItem listSubProducts = new MenuItem("Lista SubProdutos");
        Menu menuReports = new Menu("Relatórios");
        MenuItem generateReport = new MenuItem("Gerar Relatório");
        MenuItem generateCancelationReport = new MenuItem("Gerar Relatório de Cancelamentos");
        Menu menuSettings = new Menu("Configurações");
        MenuItem cadFiscal = new MenuItem("Cadastro Fiscal");
        MenuItem registerUserItem = new MenuItem("Cadastrar Usuário");
        MenuItem impScanner = new MenuItem("Impressora Config");
        MenuItem updateItem = new MenuItem("Atualizar Software");


        menuProducts.getItems().setAll(cadProducts, subProducts, listProducts, listSubProducts);
        menuSettings.getItems().addAll(cadFiscal, registerUserItem, impScanner ,updateItem);
        menuReports.getItems().setAll(generateReport, generateCancelationReport);

        // Aidiconar itens ao MenuBar para ficar visivel
        menuBar.getMenus().addAll(menuProducts, menuReports, menuSettings);



        // Version Label
        Label versionLabel = new Label("Versão: " + getSoftwareVersion());
        versionLabel.setStyle("-fx-font-size: 12; -fx-text-fill: gray;");

        // Dashboard Layout
        BorderPane mainLayout = new BorderPane();
        Scene scene = new Scene(mainLayout, 1280, 720);
        scene.getStylesheets().add(getClass().getResource("/style.css").toExternalForm());
        stage.setScene(scene);
        stage.setTitle("Dashboard");
        stage.show();

        mainLayout.setTop(menuBar);
        mainLayout.setBottom(versionLabel);
        BorderPane.setAlignment(versionLabel, Pos.BOTTOM_RIGHT);

        // Interface de Vendas
        VBox salesLayout = new VBox(10);
        HBox searchLayout = new HBox(10);

        searchLayout.setId("search-container");
        searchField = new TextField();
        searchField.setPromptText("Buscar por nome ou código");
        searchField.setId("search-field");
        Button searchButton = new Button("Buscar");
        searchButton.setOnAction(e -> openProductSearchModal(searchField.getText())
        );

        searchLayout.getChildren().addAll(searchField, searchButton);

        salesListView = new ListView<>();
        salesListView.setStyle("-fx-border-color: #ccc; -fx-border-radius: 5;");
        removeButton = new Button("Remover");
        removeButton.setDisable(true);
        removeButton.setOnAction(e -> removeSelectedProduct());

        totalLabel = new Label("Total: R$0.00");

        payButton = new Button("Pagar");
        payButton.setDisable(true);
        payButton.setOnAction(e -> openPaymentModal());


        salesLayout.getChildren().addAll(searchLayout, salesListView, removeButton, totalLabel, payButton);
        mainLayout.setCenter(salesLayout);


        stage.getScene().addEventFilter(KeyEvent.KEY_PRESSED, keyEvent -> {
            if (keyEvent.getCode() == KeyCode.MINUS) {
                removeSelectedProduct();
            } else if (keyEvent.getCode() == KeyCode.PLUS) {
                openProductSearchModal(searchField.getText()); // Abre o modal de busca ao pressionar +
            } else if (keyEvent.getCode() == KeyCode.SPACE) {
                openProductSearchModal(searchField.getText()); // Abre o modal de busca ao pressionar ESPAÇO
            }
        });

        // acess level
        if (accessLevel == AcessLevel.Caixa) {
            cadProducts.setDisable(true);
            cadFiscal.setDisable(true);
            registerUserItem.setDisable(true);
            listProducts.setDisable(true);
        } else if (accessLevel == AcessLevel.Gestor) {
            cadProducts.setDisable(false);
            cadFiscal.setDisable(false);
            registerUserItem.setDisable(false);
        } else if (accessLevel == AcessLevel.Master) {
            cadProducts.setDisable(false);
            cadFiscal.setDisable(false);
            registerUserItem.setDisable(false);
        }

        cadFiscal.setOnAction(event -> {
           openModalController.openFiscalRegistrationModal();
       });

        salesListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                SelectedProduct selectedProduct = getSelectedProduct();
                if (selectedProduct != null) {
                    // Crie um campo de texto para a edição da quantidade
                    TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedProduct.getQuantity()));
                    dialog.setTitle("Editar Quantidade");
                    dialog.setHeaderText("Atualize a quantidade do produto:");
                    dialog.setContentText("Quantidade:");

                    Optional<String> result = dialog.showAndWait();
                    if (result.isPresent()) {
                        try {
                            int newQuantity = Integer.parseInt(result.get());
                            if (newQuantity > 0) {
                                selectedProduct.setQuantity(newQuantity);
                                updateSalesListView();
                                updateTotalAmount();
                            } else {
                                showAlert(Alert.AlertType.WARNING, "Quantidade Inválida", "A quantidade deve ser um número positivo.");
                            }
                        } catch (NumberFormatException e) {
                            showAlert(Alert.AlertType.WARNING, "Quantidade Inválida", "Digite um número válido.");
                        }
                    }
                }
            }
        });


        searchField.setOnAction(e -> {
            String searchText = searchField.getText().trim();
            if (!searchText.isEmpty()) {
                addProductToSalesList(searchText);
                searchField.clear(); // Limpa o campo de busca após adicionar o produto
            }
        });


        // update software
        updateItem.setOnAction(e -> {
            try {
                boolean updated = Updater.checkForUpdates(stage);

                if (updated) {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Atualização Concluída");
                    alert.setHeaderText(null);
                    alert.setContentText("A atualização foi concluída com sucesso. Por favor, reinicie o software para aplicar as mudanças.");
                    alert.showAndWait();
                    System.exit(0);
                }
            } catch (IOException ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erro de Atualização");
                alert.setHeaderText(null);
                alert.setContentText("Ocorreu um erro durante a atualização: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // Subprodutos
        subProducts.setOnAction(e -> mostrarModalCadastrarSubProduto());
        listSubProducts.setOnAction(e -> mostrarListaSubProdutosModal());

        // Actions for Menu Items
        cadProducts.setOnAction(event -> {
            if (cadStage != null && cadStage.isShowing()) {
                cadStage.toFront();
                return;
            }
            cadStage = new Stage();
            VBox vbox = new VBox(10);
            cadStage.setScene(new Scene(vbox, 300, 400));
            cadStage.setTitle("Cadastre os Produtos");
            cadStage.show();

            Label numberLabel = new Label("Código");
            TextField numberTextField = new TextField();
            numberTextField.setMaxWidth(40);
            Label nameLabel = new Label("Nome Produto");
            TextField nameTextField = new TextField();
            nameTextField.setMaxWidth(250);
            Label priceLabel = new Label("Valor");
            TextField priceTextField = new TextField();
            priceTextField.setMaxWidth(60);
            Label groupLabel = new Label("Grupo");
            ComboBox<String> groupComboBox = new ComboBox<>();
            updateGroupComboBox(groupComboBox);

            Button addGroupButton = new Button("+");
            addGroupButton.setOnAction(e -> openAddGroupModal(groupComboBox));

            CheckBox addSubProductsCheckbox = new CheckBox("Adicionar Subprodutos");

            HBox groupLayout = new HBox(10);
            groupLayout.getChildren().addAll(groupComboBox, addGroupButton);

            Button cadButton = new Button("Cadastrar");
            cadButton.setOnAction(e -> {

                boolean hasSubProducts = addSubProductsCheckbox.isSelected();
                String code = numberTextField.getText().trim();
                String nameProduct = nameTextField.getText().trim();
                String priceProduct = openModalController.normalizePrice(priceTextField.getText().trim()); // Normaliza o preço
                String groupProduct = groupComboBox.getValue();

                try {
                    // Verifica se todos os campos obrigatórios estão preenchidos
                    if (code.isEmpty() || nameProduct.isEmpty() || groupProduct == null || groupProduct.isEmpty()) {
                        showAlert(Alert.AlertType.ERROR, "Erro", "Todos os campos (Código, Nome, e Grupo) devem ser preenchidos.");
                        return;
                    }

                    // Verifica se o código do produto já existe
                    if (databaseProduct.isProductCodeExists(code)) {
                        showAlert(Alert.AlertType.ERROR, "Erro", "Código de produto já existe.");
                        return;
                    }

                    // Verifica se o preço é válido
                    try {
                        Double.parseDouble(priceProduct); // Tenta converter para Double
                    } catch (NumberFormatException ex) {
                        showAlert(Alert.AlertType.ERROR, "Erro", "Preço inválido.");
                        return;
                    }

                    // Insere o produto no banco de dados
                    databaseProduct.insertProductIntoDatabase(code, nameProduct, priceProduct, groupProduct, hasSubProducts);
                    cadStage.close();
                    cadStage = null;
                } catch (Exception ex) {
                    // Captura qualquer exceção não esperada e mostra uma mensagem de erro
                    showAlert(Alert.AlertType.ERROR, "Erro", "Ocorreu um erro ao cadastrar o produto: " + ex.getMessage());
                }
            });

            vbox.getChildren().addAll(numberLabel, numberTextField, nameLabel, nameTextField, priceLabel, priceTextField, groupLabel, groupLayout,addSubProductsCheckbox, cadButton);
        });

        // impressora config teste!
        impScanner.setOnAction(e -> new PrinterConfig().showPrinterConfigModal());

        registerUserItem.setOnAction(event -> openModalController.openUserRegistrationModal());

        listProducts.setOnAction(event -> {
            if (productListStage != null && productListStage.isShowing()) {
                productListStage.toFront();
                return;
            }

            productListStage = new Stage();
            VBox vbox = new VBox(10);
            ListView<String> listView = new ListView<>();
            Button editButton = new Button("Editar");
            Button deleteButton = new Button("Excluir");

            HBox buttonLayout = new HBox(10);
            buttonLayout.getChildren().addAll(editButton, deleteButton);
            vbox.getChildren().addAll(listView, buttonLayout);
            productListStage.setScene(new Scene(vbox, 300, 400));
            productListStage.setTitle("Lista de Produtos");
            productListStage.show();

            // Obter todos os produtos
            List<String> products = databaseProduct.getAllProducts();
            // Formatar os preços para usar vírgula e duas casas decimais
            List<String> formattedProducts = new ArrayList<>();
            DecimalFormat decimalFormat = new DecimalFormat("#0.00"); // Formato com duas casas decimais

            for (String product : products) {
                String[] parts = product.split(" - ");
                String code = parts[0];
                String name = parts[1];
                String priceString = parts[2];
                double price = Double.parseDouble(priceString.replace(',', '.')); // Converte a string para double
                String formattedPrice = decimalFormat.format(price).replace('.', ','); // Formata e troca ponto por vírgula
                formattedProducts.add(code + " - " + name + " - R$" + formattedPrice);
            }

            listView.getItems().addAll(formattedProducts);

            editButton.setOnAction(e -> {
                String selectedProduct = listView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    openProductEditModal(selectedProduct);
                }
            });

            deleteButton.setOnAction(e -> {
                String selectedProduct = listView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null) {
                    String[] parts = selectedProduct.split(" - ");
                    String code = parts[0];
                    databaseProduct.deleteProductFromDatabase(code);
                    listView.getItems().remove(selectedProduct);
                }
            });

            listView.setOnMouseClicked(event1 -> {
                if (event1.getClickCount() == 2) {
                    String selectedProduct = listView.getSelectionModel().getSelectedItem();
                    if (selectedProduct != null) {
                        openProductEditModal(selectedProduct);
                    }
                }
            });
        });


        generateReport.setOnAction(event -> openModalController.openReportModal());
        generateCancelationReport.setOnAction(e -> openModalController.openCancellationReportModal());

    }

//


    public void mostrarListaSubProdutosModal() {
        if (productListStage != null && productListStage.isShowing()) {
            productListStage.toFront();
            return;
        }

        productListStage = new Stage();
        VBox vbox = new VBox(10);
        ListView<String> listView = new ListView<>();
        Button editButton = new Button("Editar");
        Button deleteButton = new Button("Excluir");

        HBox buttonLayout = new HBox(10);
        buttonLayout.getChildren().addAll(editButton, deleteButton);
        vbox.getChildren().addAll(listView, buttonLayout);
        productListStage.setScene(new Scene(vbox, 300, 400));
        productListStage.setTitle("Lista de Sub Produtos");
        productListStage.show();

        // Obter todos os subprodutos
        List<String> subProdutos = databaseProduct.obterTodosOsSubProdutos();
        DecimalFormat decimalFormat = new DecimalFormat("0.00"); // Formatação para duas casas decimais
        List<String> formattedSubProducts = new ArrayList<>();
        for (String subProduto : subProdutos) {
            String[] parts = subProduto.split(" - ");
            String code = parts[0];
            String name = parts[1];
            // Formatar o preço e substituir o ponto por vírgula
            String price = parts[2].replace('.', ','); // Primeiro, substitua o ponto por vírgula
            // Adiciona o valor formatado
            formattedSubProducts.add(code + " - " + name + " - R$" + decimalFormat.format(Double.parseDouble(price.replace(',', '.'))));
        }
        listView.getItems().addAll(formattedSubProducts);

        editButton.setOnAction(e -> {
            String selectedSubProduct = listView.getSelectionModel().getSelectedItem();
            if (selectedSubProduct != null) {
                openSubProductEditModal(selectedSubProduct, listView); // Passar listView para o modal de edição
            }
        });

        deleteButton.setOnAction(e -> {
            String selectedSubProduct = listView.getSelectionModel().getSelectedItem();
            if (selectedSubProduct != null) {
                String[] parts = selectedSubProduct.split(" - ");
                String code = parts[0];
                databaseProduct.deleteSubProductFromDatabase(code);
                listView.getItems().remove(selectedSubProduct);
            }
        });

        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedSubProduct = listView.getSelectionModel().getSelectedItem();
                if (selectedSubProduct != null) {
                    openSubProductEditModal(selectedSubProduct, listView); // Passar listView para o modal de edição
                }
            }
        });
    }



    //
    private void openSubProductEditModal(String selectedSubProduct, ListView<String> listView) {


        Stage editStage = new Stage();
        VBox vbox = new VBox(10);
        editStage.setScene(new Scene(vbox, 300, 400));
        editStage.setTitle("Editar Sub Produto");

        String[] parts = selectedSubProduct.split(" - ");
        String code = parts[0];
        String name = databaseProduct.getSubProductName(code);
        String price = databaseProduct.getSubProductPrice(code).replace('.', ',') ;// Aqui pegamos o preço como string

        Label numberLabel = new Label("Código");
        TextField numberTextField = new TextField(code);
        numberTextField.setMaxWidth(40);
        Label nameLabel = new Label("Nome Sub Produto");
        TextField nameTextField = new TextField(name);
        nameTextField.setMaxWidth(250);
        Label priceLabel = new Label("Valor");
        TextField priceTextField = new TextField(price); // Usar a string diretamente
        priceTextField.setMaxWidth(60);

        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> {
            String newCode = numberTextField.getText();
            String newName = nameTextField.getText();
            String newPrice = priceTextField.getText(); // Manter como string

            // Aqui não precisamos converter, apenas salvar o novo preço
            databaseProduct.updateSubProductInDatabase(newCode, newName, newPrice);

            // Atualizar a lista
            int selectedIndex = listView.getSelectionModel().getSelectedIndex();
            listView.getItems().set(selectedIndex, newCode + " - " + newName + " - " + newPrice); // Atualiza na lista

            editStage.close();
        });

        vbox.getChildren().addAll(numberLabel, numberTextField, nameLabel, nameTextField, priceLabel, priceTextField, saveButton);
        editStage.show();
    }




    //

    public void mostrarModalCadastrarSubProduto() {
        DatabaseProduct databaseProduct = new DatabaseProduct();
        Stage modal = new Stage();
        modal.initModality(Modality.APPLICATION_MODAL);
        modal.setTitle("Cadastrar Sub Produto");

        VBox vbox = new VBox(10);
        vbox.setPadding(new Insets(10));

        Label campoCodigoLabel = new Label("Código:");
        TextField campoCodigo = new TextField();
        campoCodigo.setMaxWidth(40);

        Label campoNomeLabel = new Label("Nome:");
        TextField campoNome = new TextField();
        campoNome.setMaxWidth(250);

        Label campoPrecoLabel = new Label("Preço:");
        TextField campoPreco = new TextField();
        campoPreco.setMaxWidth(60);

        Button botaoCadastrar = new Button("Cadastrar");

        botaoCadastrar.setOnAction(e -> {
            String codigo = campoCodigo.getText();
            String nome = campoNome.getText();
            String preco = campoPreco.getText();

            if (databaseProduct.verificarCodigoProdutoExistente(codigo)) {
                mostrarAlerta("Erro", "Código já cadastrado!");
            } else {
                // Salva o preço como string
                databaseProduct.insertSubProductIntoDatabase(codigo, nome, preco);
                modal.close(); // Fecha o modal após cadastro
            }
        });

        vbox.getChildren().addAll(campoCodigoLabel, campoCodigo,
                campoNomeLabel, campoNome,
                campoPrecoLabel, campoPreco,
                botaoCadastrar);

        Scene scene = new Scene(vbox, 300, 250);
        modal.setScene(scene);
        modal.show();
    }




    private void mostrarAlerta(String titulo, String conteudo) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setContentText(conteudo);
        alert.showAndWait();
    }

    ///

    private SelectedProduct getSelectedProduct() {
        String selectedItem = salesListView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            for (SelectedProduct product : selectedProducts.values()) {
                // A string de exibição da ListView deve coincidir com a representação do produto
                String productDisplayString = product.toString();  // Usa o metodo toString() da SelectedProduct
                if (selectedItem.equals(productDisplayString)) {
                    return product;
                }
            }
        }
        return null;
    }

    private void updateSalesListView() {
        salesListView.getItems().clear();
        for (SelectedProduct product : selectedProducts.values()) {
            salesListView.getItems().add(product.toString());
        }
    }

    private void updateTotalAmount() {
        totalAmount = 0.0;
        for (SelectedProduct product : selectedProducts.values()) {
            totalAmount += product.getTotalPrice(); // Certifique-se de que getTotalPrice considera subprodutos
        }
        // Atualize o label que exibe o total na interface
        totalLabel.setText(String.format("Total: R$%.2f", totalAmount));
    }



    ///

    private void openPaymentModal() {

        if (!isFiscalDataRegistered()) {
            showAlert(Alert.AlertType.ERROR, "Cadastro Fiscal Necessário", "Por favor, registre os dados fiscais antes de realizar o pagamento.");
            return;
        }

        Stage paymentStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label paymentLabel = new Label("Escolha a forma de pagamento:");
        Button cashButton = new Button("Dinheiro");
        Button creditCardButton = new Button("Cartão de Crédito");
        Button debitCardButton = new Button("Cartão de Débito");
        Button pixButton = new Button("Pix");

        // Metodo auxiliar para processar o pagamento e fechar o modal
        EventHandler<ActionEvent> handlePayment = event -> {
            Button sourceButton = (Button) event.getSource();
            String paymentMethod = sourceButton.getText();

            // Processa o pagamento
            processPayment(paymentMethod);

            // Fecha o modal
            paymentStage.close();
        };

        cashButton.setOnAction(handlePayment);
        creditCardButton.setOnAction(handlePayment);
        debitCardButton.setOnAction(handlePayment);
        pixButton.setOnAction(handlePayment);

        vbox.getChildren().addAll(paymentLabel, cashButton, creditCardButton, debitCardButton, pixButton);
        paymentStage.setScene(new Scene(vbox, 300, 200));
        paymentStage.setTitle("Forma de Pagamento");
        paymentStage.show();
    }


    private boolean isFiscalDataRegistered() {
        String[] fiscalData = databaseProduct.getFiscalData();
        // Verifica se algum dos dados fiscais está vazio ou nulo
        for (String data : fiscalData) {
            if (data == null || data.trim().isEmpty()) {
                return false;
            }
        }
        return true;
    }




    private void processPayment(String paymentMethod) {
        if (!isFiscalDataRegistered()) {
            // Exibe uma mensagem de alerta se os dados fiscais não estiverem cadastrados
            showAlert(Alert.AlertType.WARNING, "Cadastro Fiscal Necessário", "Por favor, registre os dados fiscais antes de realizar o pagamento.");
            return;
        }

        LocalDate today = LocalDate.now(); // Data atual

        // Adiciona o registro da venda ao banco de dados
        for (SelectedProduct product : selectedProducts.values()) {
            Venda venda = new Venda(
                    product.getId(),
                    product.getCode(),
                    product.getName(),
                    product.getTotalPrice(), // Aqui usamos getTotalPrice
                    product.getTotalPrice(),  // Assumindo que o valor e o preço são iguais
                    product.getQuantity(),
                    today,
                    paymentMethod,
                    product.getMotivoCancelamento() // Passa a data para a venda
            );
            venda.registrarVenda();
        }

        if (paymentMethod.equals("Dinheiro")) {
            openCashPaymentModal();
        } else {
            double total = totalAmount;
            System.out.println("Forma de pagamento: " + paymentMethod);
            System.out.println("Total a pagar: R$" + String.format("%.2f", total));
            openPrintReceiptModal(paymentMethod);
            // printNFCE(paymentMethod); // Imprime o comprovante após clicar no tipo de pagamento
        }
    }




    public void openPrintReceiptModal(String paymentMethod) {
        Stage printStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label printLabel = new Label("Deseja imprimir o comprovante?");
        Button yesButton = new Button("Sim");
        Button noButton = new Button("Não");

        yesButton.setOnAction(e -> {
            printReceipt(paymentMethod); // Passe o metodo de pagamento aqui
            printStage.close();
        });

        noButton.setOnAction(e -> printStage.close());

        vbox.getChildren().addAll(printLabel, yesButton, noButton);
        printStage.setScene(new Scene(vbox, 300, 150));
        printStage.setTitle("Imprimir Comprovante");
        printStage.show();
    }


    public void printReceipt(String paymentMethod) {
        Printer printer = printManager.getSavedPrinter();
        if (printer == null) {
            showErrorDialog("Falha na impressão", "Nenhuma impressora configurada. Para suporte, clique no link abaixo.");
            return;
        }

        PrinterJob printerJob = PrinterJob.createPrinterJob();
        if (printerJob != null && printerJob.getPrinter().equals(printer)) {
            // Configurando o tamanho da bobina térmica em milímetros
            double widthMM = 80;
            double heightMM = 80;

            // Convertendo milímetros para pontos
            double widthInPoints = widthMM * 72 / 25.4;
            double heightInPoints = heightMM * 72 / 25.4;

            // Criando o layout da página com margens mínimas
            PageLayout pageLayout = printer.createPageLayout(
                    Paper.A4,
                    PageOrientation.PORTRAIT,
                    Printer.MarginType.HARDWARE_MINIMUM
            );

            // Configurando o texto a ser impresso
            javafx.scene.text.Text textNode = new javafx.scene.text.Text(getNFCEText(paymentMethod));
            textNode.setFont(new javafx.scene.text.Font("Times New Roman", 9)); // Define o tamanho da fonte
            textNode.setWrappingWidth(widthInPoints); // Ajusta para a largura da bobina

            // Imprimindo a página diretamente
            boolean success = printerJob.printPage(pageLayout, textNode);
            if (success) {
                printerJob.endJob();
            } else {
                showErrorDialog("Falha na impressão", "Houve uma falha ao tentar imprimir. Para suporte, clique no link abaixo.");
            }
        } else {
            showErrorDialog("Falha na impressão", "Não foi possível criar o trabalho de impressão. Para suporte, clique no link abaixo.");
        }
    }

    private void showErrorDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);

        // Criando o conteúdo com um Hyperlink
        Hyperlink link = new Hyperlink("Entrar em contato via WhatsApp, Clique Aqui!");
        link.setOnAction(e -> getHostServices().showDocument("https://wa.me/5551995000151"));

        VBox content = new VBox();
        content.getChildren().addAll(new Text(message), link);
        alert.getDialogPane().setContent(content);

        alert.showAndWait();
    }

    private double amountGiven = 0.0; // Variável para armazenar o valor dado

    private void openCashPaymentModal() {
        Stage cashPaymentStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label amountGivenLabel = new Label("Valor dado pelo cliente:");
        TextField amountGivenTextField = new TextField();
        amountGivenTextField.setMaxWidth(200);
        amountGivenTextField.setPromptText("Digite o valor dado");

        Label changeLabel = new Label("Troco:");
        Text changeText = new Text("R$0.00");

        // Listener para atualizar o valor do troco conforme o valor pago muda
        amountGivenTextField.textProperty().addListener((observable, oldValue, newValue) -> {
            try {
                amountGiven = Double.parseDouble(newValue);
                double change = amountGiven - totalAmount;
                if (change < 0) {
                    changeText.setText("R$0.00");
                } else {
                    changeText.setText(String.format("R$%.2f", change));
                }
            } catch (NumberFormatException e) {
                changeText.setText("R$0.00");
            }
        });

        Button confirmButton = new Button("Confirmar");
        confirmButton.setOnAction(e -> {
            try {
                amountGiven = Double.parseDouble(amountGivenTextField.getText());
                double change = amountGiven - totalAmount; // Verifique se totalAmount está atualizado

                if (change < 0) {
                    showAlert(Alert.AlertType.WARNING, "Valor Insuficiente", "O valor dado é menor que o total.");
                    return;
                }

                // Exibe informações do pagamento
                System.out.println("Valor dado pelo cliente: R$" + String.format("%.2f", amountGiven));
                System.out.println("Troco a devolver: R$" + String.format("%.2f", change));

                // Atualize a interface para mostrar o troco
                changeText.setText(String.format("R$%.2f", change));

                openPrintReceiptModal("Dinheiro");
                cashPaymentStage.close();
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Por favor, insira um valor válido.");
            }
        });

        vbox.getChildren().addAll(amountGivenLabel, amountGivenTextField, changeLabel, changeText, confirmButton);
        cashPaymentStage.setScene(new Scene(vbox, 300, 200));
        cashPaymentStage.setTitle("Pagamento em Dinheiro");
        cashPaymentStage.show();
    }




    private String getNFCEText(String paymentMethod) {
        String[] fiscalData = databaseProduct.getFiscalData(); // Obtém os dados fiscais

        StringBuilder nfce = new StringBuilder();
        LocalDateTime now = LocalDateTime.now(); // Data e hora atual

        nfce.append("\n");
        nfce.append("Data: ").append(now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")))
                .append(" - Hora: ").append(now.format(DateTimeFormatter.ofPattern("HH:mm:ss\n")));
        nfce.append("--------------------------------------\n");

        // Dados do Emitente
        nfce.append("Emitente: ").append(fiscalData[1].isEmpty() ? " Não Informado" : fiscalData[1]).append("\n");
        nfce.append("CNPJ: ").append(fiscalData[0].isEmpty() ? "Não Informado" : fiscalData[0]).append("\n");
        nfce.append("Endereço: ").append(fiscalData[2].isEmpty() ? "Não Informado" : fiscalData[2]).append("\n");
        nfce.append("Cidade: ").append(fiscalData[3].isEmpty() ? "Não Informado" : fiscalData[3])
                .append(" CEP: ").append(fiscalData[4].isEmpty() ? "Não Informado" : fiscalData[4]).append("\n");
        nfce.append("---------------------------------------\n");

        // Itens da Nota
        for (SelectedProduct product : selectedProducts.values()) {
            nfce.append(product.getCode()).append(" - ");
            nfce.append(product.getName()).append(" ");
            nfce.append("R$").append(String.format("%.2f", product.getTotalPrice())).append(" - "); // Alterado aqui
            nfce.append(" x ").append(product.getQuantity()).append("\n");

            // Adiciona subprodutos
            for (SubProduct sub : product.getSubProducts()) {
                nfce.append("  + ").append(sub.getName()).append(" R$").append(String.format("%.2f", sub.getPrice())).append("\n");
            }
        }

        nfce.append("---------------------------------------\n");
        nfce.append("Quantidade de Itens: ").append(getTotalItems()).append("\n");
        nfce.append("TOTAL: R$").append(String.format("%.2f", totalAmount)).append("\n");

        nfce.append("Forma de Pagamento: ").append(paymentMethod).append("\n");
        if (paymentMethod.equals("Dinheiro")) {
            double change = amountGiven - totalAmount;
            nfce.append("Valor Pago: R$").append(String.format("%.2f", amountGiven)).append("\n");
            nfce.append("TROCO: R$").append(String.format("%.2f", change)).append("\n");
        }
        nfce.append("---------------------------------------\n");
        nfce.append("| NAO E DOCUMENTO FISCAL |\n");
        nfce.append("---------------------------------------\n");
        nfce.append("Obrigado por comprar conosco!\n");

        return nfce.toString();
    }




    private void openProductSearchModal(String searchText) {
        if (searchStage != null && searchStage.isShowing()) {
            searchStage.toFront(); // Coloca o modal de busca em primeiro plano
            return;
        }

        searchStage = new Stage();
        VBox vbox = new VBox(10);
        ListView<String> searchListView = new ListView<>();

        // Adiciona um EventHandler para a tecla ENTER
        searchListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                String selectedProduct = searchListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null && !selectedProduct.isEmpty()) {
                    addProductToSalesListDirectly(selectedProduct);
                    searchStage.close();
                    focusOnSearchField(); // Move o foco de volta para o campo de pesquisa
                } else {
                    System.out.println("Nenhum produto selecionado no ENTER.");
                }
            }
        });

        // Adiciona um EventHandler para clique duplo na lista
        searchListView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                String selectedProduct = searchListView.getSelectionModel().getSelectedItem();
                if (selectedProduct != null && !selectedProduct.isEmpty()) {
                    addProductToSalesListDirectly(selectedProduct);
                    searchStage.close();
                    focusOnSearchField(); // Move o foco de volta para o campo de pesquisa
                } else {
                    System.out.println("Nenhum produto selecionado no clique duplo.");
                }
            }
        });

        // Preenche a lista com os produtos encontrados
        List<String> products;
        if (searchText.isEmpty()) {
            products = databaseProduct.getAllProducts();
        } else {
            products = databaseProduct.searchProducts(searchText);
        }

        searchListView.getItems().clear();
        searchListView.getItems().addAll(products); // Adiciona os produtos à ListView

        // Verifique se a lista está sendo preenchida corretamente
        System.out.println("Produtos encontrados: " + products);

        // Se houver produtos, selecione o primeiro item por padrão
        if (!products.isEmpty()) {
            searchListView.getSelectionModel().select(0);
        }

        // Adiciona a ListView ao VBox e configura a cena
        vbox.getChildren().add(searchListView);
        searchStage.setScene(new Scene(vbox, 300, 400));
        searchStage.setTitle("Buscar Produtos");
        searchStage.show();
    }




    private void addProductToSalesListDirectly(String productInfo) {
        System.out.println("Produto Info: " + productInfo); // Para depuração
        if (productInfo == null || productInfo.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Código Inválido", "Por favor, insira um código de produto válido.");
            return;
        }

        String[] parts = productInfo.split(" - ");
        if (parts.length != 3) { // Alterado para 3 partes considerando preço
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Formato de produto retornado é inválido.");
            return;
        }

        String productCode = parts[0].trim();
        String productName = parts[1].trim();
        double productPrice;
        try {
            productPrice = Double.parseDouble(parts[2].trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Preço", "O preço do produto é inválido.");
            return;
        }

        // Verifica se o produto possui subprodutos
        SelectedProduct selectedProduct = selectedProducts.get(productCode);
        if (databaseProduct.hasSubProducts(productCode)) {
            // Se tiver subprodutos, adiciona o produto principal à lista e abre o modal para seleção de subprodutos
            if (selectedProduct == null) {
                selectedProduct = new SelectedProduct(0, productCode, productName, productPrice, 1, null);
                selectedProducts.put(productCode, selectedProduct);
            } else {
                selectedProduct.incrementQuantity();
            }
            // Abrir modal de seleção de subprodutos
            showSubProductsSelectionModal(productCode);
        } else {
            // Se não tiver subprodutos, adiciona diretamente à lista
            if (selectedProduct == null) {
                selectedProduct = new SelectedProduct(0, productCode, productName, productPrice, 1, null);
                selectedProducts.put(productCode, selectedProduct);
            } else {
                selectedProduct.incrementQuantity();
            }
            updateSalesListView();
            updateTotalAmount();
            updateButtonStates();
        }
    }





    private void focusOnSearchField() {
        searchField.requestFocus();
    }


    private void openProductEditModal(String selectedProduct) {
        Stage editStage = new Stage();
        VBox vbox = new VBox(10);
        editStage.setScene(new Scene(vbox, 300, 400));
        editStage.setTitle("Editar Produto");

        String[] parts = selectedProduct.split(" - ");
        String code = parts[0];

        String name = databaseProduct.getProductName(code);
        double price = databaseProduct.getProductPrice(code);
        String group = databaseProduct.getProductGroup(code);

        // Formata o preço para string com vírgula
        String formattedPrice = String.valueOf(price).replace('.', ',');

        Label numberLabel = new Label("Código");
        TextField numberTextField = new TextField(code);
        numberTextField.setMaxWidth(40);
        Label nameLabel = new Label("Nome Produto");
        TextField nameTextField = new TextField(name);
        nameTextField.setMaxWidth(250);
        Label priceLabel = new Label("Valor");
        TextField priceTextField = new TextField(formattedPrice); // Use formattedPrice aqui
        priceTextField.setMaxWidth(60);
        Label groupLabel = new Label("Grupo");
        ComboBox<String> groupComboBox = new ComboBox<>();
        groupComboBox.getItems().setAll(databaseProduct.getAllGroups());
        groupComboBox.setValue(group);
        groupComboBox.setMaxWidth(120);

        Button saveButton = new Button("Salvar");
        saveButton.setOnAction(e -> {
            String newCode = numberTextField.getText();
            String newName = nameTextField.getText();
            String newPrice = priceTextField.getText().replace(',', '.'); // Troca vírgula por ponto para armazenar
            String newGroup = groupComboBox.getValue();

            databaseProduct.updateProductInDatabase(newCode, newName, newPrice, newGroup);
            editStage.close();
        });

        vbox.getChildren().addAll(numberLabel, numberTextField, nameLabel, nameTextField, priceLabel, priceTextField, groupLabel, groupComboBox, saveButton);
        editStage.show();
    }


    private void addProductToSalesList(String code) {
        if (code == null || code.trim().isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Código Inválido", "Por favor, insira um código de produto válido.");
            return;
        }

        // Pesquisa o produto no banco de dados
        List<String> products = databaseProduct.searchProducts(code);
        if (products.isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Produto Não Encontrado", "Nenhum produto encontrado com o código fornecido.");
            return;
        }

        // Considera que a lista pode ter vários produtos, então selecionamos o primeiro
        String productInfo = products.get(0);
        String[] parts = productInfo.split(" - ");
        if (parts.length != 2) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Formato de produto retornado é inválido.");
            return;
        }

        String productCode = parts[0].trim();
        String productName = parts[1].trim();

        // Verifica se o produto tem subprodutos
        boolean hasSubProducts = databaseProduct.checkIfProductHasSubProducts(productCode); // Metodo para verificar

        if (hasSubProducts) {
            showSubProductsSelectionModal(productCode); // Chama o metodo para mostrar o modal com subprodutos
        }

        // Assume-se que o preço do produto é obtido do banco de dados
        double productPrice = databaseProduct.getProductPrice(productCode);

        // Cria ou atualiza o objeto SelectedProduct na lista
        SelectedProduct selectedProduct = selectedProducts.get(productCode);
        if (selectedProduct != null) {
            selectedProduct.incrementQuantity();
        } else {
            selectedProduct = new SelectedProduct(0, productCode, productName, productPrice, 1, null);
            selectedProducts.put(productCode, selectedProduct);
        }

        // Atualiza a interface de vendas
        updateSalesListView();
        updateTotalAmount();
        updateButtonStates();
    }


    //
  private void showSubProductsSelectionModal(String productCode) {
      Stage subProductsStage = new Stage();
      VBox vbox = new VBox(10);
      ListView<CheckBox> subProductsListView = new ListView<>();

      List<SubProduct> subProducts = databaseProduct.getSubProducts(); // Metodo que busca os subprodutos

      for (SubProduct subProduct : subProducts) {
          CheckBox checkBox = new CheckBox(subProduct.getName() + " - " + subProduct.getPrice());
          subProductsListView.getItems().add(checkBox);
      }

      Button addButton = new Button("Adicionar Subprodutos");
      addButton.setOnAction(e -> {
          for (CheckBox checkBox : subProductsListView.getItems()) {
              if (checkBox.isSelected()) {
                  // Adiciona o subproduto à lista de vendas
                  addSubProductToSalesList(productCode, checkBox.getText());
              }
          }
          subProductsStage.close();
      });

      vbox.getChildren().addAll(subProductsListView, addButton);
      subProductsStage.setScene(new Scene(vbox, 300, 400));
      subProductsStage.setTitle("Selecione os Subprodutos");
      subProductsStage.show();
  }

    private void addSubProductToSalesList(String productCode, String subProductInfo) {
        // Parse o subproduto para obter o preço
        String[] parts = subProductInfo.split(" - ");
        if (parts.length != 2) {
            showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Formato de subproduto retornado é inválido.");
            return;
        }

        String subProductName = parts[0].trim();
        double subProductPrice;

        // Tenta converter o preço para double
        try {
            subProductPrice = Double.parseDouble(parts[1].trim());
        } catch (NumberFormatException e) {
            showAlert(Alert.AlertType.ERROR, "Erro de Preço", "Preço do subproduto é inválido.");
            return;
        }

        // Adiciona o subproduto ao produto principal
        SelectedProduct selectedProduct = selectedProducts.get(productCode);

        if (selectedProduct != null) {
            selectedProduct.addSubProduct(subProductName, subProductPrice); // Método para adicionar subproduto e preço corretamente
        } else {
            selectedProduct = new SelectedProduct(0, productCode, subProductName, subProductPrice, 1, null);
            selectedProducts.put(productCode, selectedProduct);
        }

        // Atualiza o total após adicionar o subproduto
        updateTotalAmount();

        // Atualiza a interface de vendas
        updateSalesListView();
        updateButtonStates();
    }



    private void removeSelectedProduct() {
        String selectedItem = salesListView.getSelectionModel().getSelectedItem();

        if (selectedItem != null) {
            String[] parts = selectedItem.split(" - ");
            if (parts.length != 2) {
                showAlert(Alert.AlertType.ERROR, "Erro de Formato", "Formato de produto inválido.");
                return;
            }

            String code = parts[0];
            SelectedProduct selectedProduct = selectedProducts.get(code);

            if (selectedProduct != null) {
                selectedProducts.remove(code);
                updateSalesListView();
                updateTotalAmount();
                updateButtonStates();
            } else {
                showAlert(Alert.AlertType.WARNING, "Produto não encontrado", "O produto selecionado não pôde ser encontrado.");
            }
        } else {
            showAlert(Alert.AlertType.WARNING, "Nenhum produto selecionado", "Por favor, selecione um produto para remover.");
        }
    }


    private void updateButtonStates() {
        boolean hasProducts = !selectedProducts.isEmpty();
        removeButton.setDisable(!hasProducts);
        payButton.setDisable(!hasProducts);
    }

    private int getTotalItems() {
        return selectedProducts.size(); // Contar o número de produtos distintos
    }

    private double change = 0.0;

    private double getChange() {
        return change;
    }

    private void openAddGroupModal(ComboBox<String> groupComboBox) {
        Stage addGroupStage = new Stage();
        VBox vbox = new VBox(10);
        vbox.setAlignment(Pos.CENTER);

        Label newGroupLabel = new Label("Novo Grupo:");
        TextField newGroupTextField = new TextField();
        newGroupTextField.setMaxWidth(200);
        newGroupTextField.setPromptText("Digite o nome do novo grupo");

        Button addButton = new Button("Adicionar");
        addButton.setOnAction(e -> {
            String newGroup = newGroupTextField.getText().trim();
            if (newGroup.isEmpty()) {
                showAlert(Alert.AlertType.WARNING, "Aviso", "O nome do grupo não pode estar vazio.");
                return;
            }

            if (databaseProduct.isGroupExists(newGroup)) {
                showAlert(Alert.AlertType.ERROR, "Erro", "Grupo já existe.");
                return;
            }

            databaseProduct.insertGroupIntoDatabase(newGroup);
            updateGroupComboBox(groupComboBox);
            addGroupStage.close();
        });

        vbox.getChildren().addAll(newGroupLabel, newGroupTextField, addButton);
        addGroupStage.setScene(new Scene(vbox, 200, 150));
        addGroupStage.setTitle("Adicionar Novo Grupo");
        addGroupStage.show();
    }

    private void updateGroupComboBox(ComboBox<String> groupComboBox) {
        List<String> groups = databaseProduct.getAllGroups();
        groupComboBox.getItems().clear();
        groupComboBox.getItems().addAll(groups);
    }

    private void showAlert(Alert.AlertType alertType, String title, String message) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getSoftwareVersion() {
        try (InputStream input = new FileInputStream(VERSION_FILE_PATH)) {
            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty("version", "Desconhecida");
            System.out.println("Versão carregada: " + version);
            return version;
        } catch (IOException ex) {
            ex.printStackTrace();
            return "Erro ao carregar versão";
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
