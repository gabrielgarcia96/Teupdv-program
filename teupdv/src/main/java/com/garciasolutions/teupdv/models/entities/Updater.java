package com.garciasolutions.teupdv.models.entities;


import javafx.scene.control.Alert;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Updater {

    private static final String VERSION_URL = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/v1.0.1/version.properties";
    private static final String DOWNLOAD_URL = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/v1.0.1/teupdv.jar";
    private static final String LOCAL_VERSION_FILE = "version.properties";
    private static final String LOCAL_SOFTWARE_PATH = "teupdv.jar";

    public static boolean checkForUpdates() throws IOException {
        String localVersion = getLocalVersion();
        System.out.println("Versão local: " + localVersion);

        String remoteVersion = getRemoteVersion();
        System.out.println("Versão remota: " + remoteVersion);

        if (localVersion.equals(remoteVersion)) {
            showAlert(Alert.AlertType.INFORMATION, "Atualização", "O software já está na versão mais recente.");
            return false;
        } else if (isNewerVersion(remoteVersion, localVersion)) {
            System.out.println("Nova versão disponível. Atualizando...");
            downloadAndUpdateSoftware();
            return true;
        } else {
            System.out.println("O software já está atualizado.");
            return false;
        }
    }

    private static String getLocalVersion() throws IOException {
        // Leitura do arquivo version.properties localizado no diretório atual
        try (InputStream input = new FileInputStream(LOCAL_VERSION_FILE)) {
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("version");
        }
    }

    private static String getRemoteVersion() throws IOException {
        URL url = new URL(VERSION_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP Response Code: " + responseCode);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                Properties properties = new Properties();
                properties.load(reader);
                return properties.getProperty("version");
            }
        } else {
            throw new IOException("Failed to get remote version. HTTP response code: " + responseCode);
        }
    }

    private static boolean isNewerVersion(String remoteVersion, String localVersion) {
        return remoteVersion.compareTo(localVersion) > 0;
    }

    private static void downloadAndUpdateSoftware() throws IOException {
        // Caminho para o novo JAR
        URL url = new URL(DOWNLOAD_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        int responseCode = connection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Baixar o novo JAR
            try (InputStream input = connection.getInputStream();
                 FileOutputStream output = new FileOutputStream(LOCAL_SOFTWARE_PATH)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
            }

            // Atualizar o arquivo version.properties
            URL versionUrl = new URL(VERSION_URL);
            HttpURLConnection versionConnection = (HttpURLConnection) versionUrl.openConnection();
            versionConnection.setRequestMethod("GET");

            int versionResponseCode = versionConnection.getResponseCode();
            if (versionResponseCode == HttpURLConnection.HTTP_OK) {
                try (InputStream versionInput = versionConnection.getInputStream();
                     FileOutputStream versionOutput = new FileOutputStream(LOCAL_VERSION_FILE)) {
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = versionInput.read(buffer)) != -1) {
                        versionOutput.write(buffer, 0, bytesRead);
                    }
                }
                System.out.println("Arquivo version.properties atualizado com sucesso.");
            } else {
                throw new IOException("Failed to download version file. HTTP response code: " + versionResponseCode);
            }

            System.out.println("Atualização concluída.");
            // Adicionar um aviso para reiniciar o software
            System.out.println("Por favor, reinicie o software para aplicar a atualização.");
        } else {
            throw new IOException("Failed to download update. HTTP response code: " + responseCode);
        }
    }

    private static void showAlert(Alert.AlertType alertType, String title, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }


}