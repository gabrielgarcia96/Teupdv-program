package com.garciasolutions.teupdv.models.entities;

import javafx.scene.control.Alert;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Updater {


    private static final String API_URL = "https://api.github.com/repos/gabrielgarcia96/Teupdv-program/releases";
    private static final String LOCAL_SOFTWARE_PATH = "C:/Program Files (x86)/teupdv/teupdv.jar";
    private static final String LOCAL_VERSION_FILE = "C:/Program Files (x86)/teupdv/version.properties";
    public static boolean checkForUpdates() throws IOException {
        String localVersion = getLocalVersion();
        String remoteVersion = getLatestVersion();

        if (localVersion.equals(remoteVersion)) {
            showAlert(Alert.AlertType.INFORMATION, "Atualização", "O software já está na versão mais recente.");
            return false; // Nenhuma atualização necessária
        } else {
            downloadAndUpdateSoftware(remoteVersion);
            return true; // Atualização concluída
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
            String version = properties.getProperty("version");
            System.out.println("Versão local lida do arquivo: " + version);
            return version;
        }
    }

    private static boolean isNewerVersion(String remoteVersion, String localVersion) {
        return remoteVersion.compareTo(localVersion) > 0;
    }

    private static void downloadAndUpdateSoftware(String version) throws IOException {
        String jarUrlStr = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/" + version + "/teupdv.jar";
        String versionUrlStr = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/" + version + "/version.properties";

        System.out.println("URL do JAR: " + jarUrlStr);
        System.out.println("URL do version.properties: " + versionUrlStr);

        URL jarUrl = new URL(jarUrlStr);
        HttpURLConnection jarConnection = (HttpURLConnection) jarUrl.openConnection();
        jarConnection.setRequestMethod("GET");

        int responseCode = jarConnection.getResponseCode();
        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (InputStream input = jarConnection.getInputStream();
                 FileOutputStream output = new FileOutputStream(LOCAL_SOFTWARE_PATH)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                System.out.println("Novo JAR baixado e salvo com sucesso.");
            }

            URL versionUrl = new URL(versionUrlStr);
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
                    System.out.println("Arquivo version.properties atualizado com sucesso.");
                }
            } else {
                throw new IOException("Failed to download version file. HTTP response code: " + versionResponseCode);
            }

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
