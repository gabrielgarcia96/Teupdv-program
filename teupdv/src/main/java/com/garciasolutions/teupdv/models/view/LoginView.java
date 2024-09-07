package com.garciasolutions.teupdv.models.view;

import com.garciasolutions.teupdv.models.data.DatabaseUser;
import com.garciasolutions.teupdv.models.entities.AcessLevel;
import com.garciasolutions.teupdv.models.entities.UserSession;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class LoginView extends Application {


    @Override
    public void start(Stage stage) throws Exception {

        // Inputs and Labels
        Label usernameLabel = new Label("Username: ");
        TextField usernameTextField = new TextField();
        usernameTextField.setMaxWidth(200);
        Label passwordLabel = new Label("Password: ");
        PasswordField passwordTextField = new PasswordField();
        passwordTextField.setMaxWidth(200);
        Button btnEnter = new Button("Enter");



        // Actions and Methods

        btnEnter.setOnAction(e -> {
            DatabaseUser databaseUser = new DatabaseUser();
            String username = usernameTextField.getText();
            String password = passwordTextField.getText();

            if (databaseUser.authenticateUser(username, password)) {
                AcessLevel accessLevel = databaseUser.getUserAccessLevel(username, password);
                if (accessLevel != null) {
                    // Configura a sessão do usuário
                    UserSession.getInstance().setCurrentUsername(username);
                    UserSession.getInstance().setAccessLevel(accessLevel);

                    // Redireciona para o dashboard com o nível de acesso
                    databaseUser.openDashboard(accessLevel);
                    stage.close();
                } else {
                    databaseUser.showError("Erro ao recuperar o nível de acesso do usuário");
                }
            } else {
                // Exibe a mensagem de erro
                databaseUser.showError("Usuário ou senha incorretos");
            }
        });





        // Adjust margins for better spacing
        VBox.setMargin(usernameLabel, new Insets(0, 0, 0, 0));
        VBox.setMargin(usernameTextField, new Insets(0, 0, 0, 0));
        VBox.setMargin(passwordLabel, new Insets(0, 0, 0, 0));
        VBox.setMargin(passwordTextField, new Insets(0, 0, 0, 0));
        VBox.setMargin(btnEnter, new Insets(5, 0, 0, 0));

        // Window scene
        VBox loginVbox = new VBox();
        loginVbox.setAlignment(Pos.CENTER);
        loginVbox.setSpacing(10); // Adjust spacing between components
        loginVbox.setPadding(new Insets(20)); // Add padding around the VBox
        loginVbox.getChildren().addAll(usernameLabel, usernameTextField, passwordLabel, passwordTextField, btnEnter);

        // Load the CSS file
        Scene scene = new Scene(loginVbox, 300, 200); // Increased height for better visibility
        scene.getStylesheets().add(getClass().getResource("/loginstyle.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("Login TEUPDV");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
