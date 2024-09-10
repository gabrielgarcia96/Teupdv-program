package com.garciasolutions.teupdv.models.entities;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Updater {

    private static final String API_URL = "https://api.github.com/repos/gabrielgarcia96/Teupdv-program/releases";
    private static final String LOCAL_JAR_PATH = "C:/teupdv_data/teupdv.jar";
    private static final String LOCAL_EXE_PATH = "C:/teupdv_data/teupdv.exe";
    private static final String LOCAL_VERSION_FILE = "C:/teupdv_data/version.properties";
    private static Stage progressStage;

    public static boolean checkForUpdates(Window owner) throws IOException {
        String localVersion = getLocalVersion();
        String remoteVersion = getLatestVersion();

        System.out.println("Versão local: " + localVersion);
        System.out.println("Versão remota: " + remoteVersion);

        if (localVersion.equals(remoteVersion)) {
            showAlert(Alert.AlertType.INFORMATION, "Atualização", "O software já está na versão mais recente.");
            return false; // Nenhuma atualização necessária
        } else {
            // Mostrar modal de progresso
            Platform.runLater(() -> showProgressIndicator(owner));

            // Iniciar a tarefa de atualização em um thread separado
            Task<Boolean> updateTask = new Task<>() {
                @Override
                protected Boolean call() throws Exception {
                    try {
                        downloadAndUpdateSoftware(remoteVersion);
                        return true;
                    } catch (IOException e) {
                        System.out.println("Erro ao atualizar o software: " + e.getMessage());
                        return false;
                    }
                }

                @Override
                protected void succeeded() {
                    super.succeeded();
                    hideProgressIndicator();
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("Atualização Concluída");
                    alert.setHeaderText(null);
                    alert.setContentText("A atualização foi concluída com sucesso. Por favor, reinicie o software para aplicar as mudanças.");

                    alert.showAndWait().ifPresent(response -> {
                        // Fechar a instância atual
                        Platform.exit();

                        // Reiniciar o aplicativo
                        try {
                            String javaBin = System.getProperty("java.home") + "/bin/java";
                            File currentJar = new File(Updater.class.getProtectionDomain().getCodeSource().getLocation().toURI());
                            if (!currentJar.getName().endsWith(".jar")) {
                                throw new RuntimeException("Este aplicativo não está em um arquivo JAR.");
                            }

                            String jarPath = "C:/teupdv_data/teupdv.jar";

                            // Cria um novo processo para iniciar a nova instância do aplicativo
                            ProcessBuilder builder = new ProcessBuilder(
                                    javaBin,
                                    "-jar",
                                    jarPath
                            );

                            builder.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                            System.out.println("Erro ao reiniciar o aplicativo: " + e.getMessage());
                        }

                        // Encerrar a instância atual
                        System.exit(0);
                    });
                }

                @Override
                protected void failed() {
                    super.failed();
                    hideProgressIndicator();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Erro de Atualização");
                    alert.setHeaderText(null);
                    alert.setContentText("Ocorreu um erro durante a atualização: " + getException().getMessage());
                    alert.showAndWait();
                }
            };

            new Thread(updateTask).start(); // Iniciar a tarefa em um thread separado
            return true; // Atualização iniciada
        }
    }

    private static void showProgressIndicator(Window owner) {
        if (progressStage == null) {
            progressStage = new Stage();
            progressStage.initModality(Modality.APPLICATION_MODAL);
            progressStage.initOwner(owner);

            ProgressIndicator progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(100, 100);

            VBox vbox = new VBox(10, new Label("Atualizando, aguarde..."), progressIndicator);
            vbox.setAlignment(Pos.CENTER);

            Scene progressScene = new Scene(vbox, 300, 150);
            progressStage.setScene(progressScene);
            progressStage.setTitle("Atualização em andamento");
        }
        progressStage.show();
    }

    private static void hideProgressIndicator() {
        if (progressStage != null) {
            progressStage.close();
            progressStage = null; // Libere a referência
        }
    }

    private static String getLatestVersion() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }

                JSONArray releases = new JSONArray(response.toString());
                if (releases.length() > 0) {
                    JSONObject latestRelease = releases.getJSONObject(0);
                    return latestRelease.getString("tag_name");
                } else {
                    throw new IOException("No releases found.");
                }
            }
        } else {
            throw new IOException("Failed to get releases. HTTP response code: " + responseCode);
        }
    }

    private static String getLocalVersion() throws IOException {
        try (InputStream input = new FileInputStream(LOCAL_VERSION_FILE)) {
            Properties properties = new Properties();
            properties.load(input);
            String version = properties.getProperty("version", "Desconhecida");
            System.out.println("Versão local lida do arquivo: " + version);
            return version;
        }
    }

    private static void downloadAndUpdateSoftware(String version) throws IOException {
        String jarUrlStr = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/" + version + "/teupdv.jar";
        String exeUrlStr = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/" + version + "/teupdv.exe";
        String versionUrlStr = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/" + version + "/version.properties";

        System.out.println("URL do JAR: " + jarUrlStr);
        System.out.println("URL do EXE: " + exeUrlStr);
        System.out.println("URL do version.properties: " + versionUrlStr);

        // Baixar e substituir o JAR
        downloadFile(jarUrlStr, LOCAL_JAR_PATH);

        // Baixar e substituir o EXE
        downloadFile(exeUrlStr, LOCAL_EXE_PATH);

        // Baixar e substituir o arquivo version.properties
        downloadFile(versionUrlStr, LOCAL_VERSION_FILE);

        System.out.println("Atualizações aplicadas com sucesso.");
    }

    private static void downloadFile(String fileUrlStr, String localPath) throws IOException {
        URL fileUrl = new URL(fileUrlStr);
        HttpURLConnection connection = (HttpURLConnection) fileUrl.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(localPath)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }
        } else {
            throw new IOException("Failed to download file. HTTP response code: " + responseCode);
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
