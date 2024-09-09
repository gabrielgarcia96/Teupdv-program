package com.garciasolutions.teupdv.models.entities;

import javafx.application.Platform;
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
    private static final String GITHUB_TOKEN = "";

    private static Stage progressStage;

    private static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");
        return connection;
    }

    public static boolean checkForUpdates(Window owner) throws IOException {
        String localVersion = getLocalVersion();
        String remoteVersion = getLatestVersion();

        if (localVersion.equals(remoteVersion)) {
            showAlert(Alert.AlertType.INFORMATION, "Atualização", "O software já está na versão mais recente.");
            return false; // Nenhuma atualização necessária
        } else {
            Platform.runLater(() -> showProgressIndicator(owner));

            try {
                downloadAndUpdateSoftware(remoteVersion);
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                Platform.runLater(() -> hideProgressIndicator());
            }
            return true; // Atualização concluída
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
            progressStage = null;
        }
    }

    private static String getLatestVersion() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = createConnection(url);

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

        downloadFile(jarUrlStr, LOCAL_JAR_PATH);
        downloadFile(exeUrlStr, LOCAL_EXE_PATH);
        downloadFile(versionUrlStr, LOCAL_VERSION_FILE);

        System.out.println("Atualizações aplicadas com sucesso.");
    }

    private static void downloadFile(String fileUrlStr, String localPath) throws IOException {
        URL fileUrl = new URL(fileUrlStr);
        HttpURLConnection connection = createConnection(fileUrl);
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN); // Adicione o cabeçalho de autorização

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
            throw new IOException("Failed to download file from " + fileUrlStr + ". HTTP response code: " + responseCode);
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
