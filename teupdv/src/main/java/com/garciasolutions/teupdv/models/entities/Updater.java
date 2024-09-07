package com.garciasolutions.teupdv.models.entities;


import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Properties;

public class Updater {

    private static final String VERSION_URL = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/1.0.1/version.properties";
    private static final String DOWNLOAD_URL = "https://github.com/gabrielgarcia96/Teupdv-program/releases/download/1.0.1/teupdv.exe";
    private static final String LOCAL_VERSION_FILE = "version.properties";
    private static final String LOCAL_SOFTWARE_PATH = "teupdv.jar";

    public static void checkForUpdates() throws IOException {
        // Obter a versão atual do software local
        String localVersion = getLocalVersion();
        System.out.println("Versão local: " + localVersion);

        // Obter a versão mais recente do servidor
        String remoteVersion = getRemoteVersion();
        System.out.println("Versão remota: " + remoteVersion);

        // Comparar as versões
        if (isNewerVersion(remoteVersion, localVersion)) {
            System.out.println("Nova versão disponível. Atualizando...");
            downloadAndUpdateSoftware();
        } else {
            System.out.println("O software já está atualizado.");
        }
    }

    private static String getLocalVersion() throws IOException {
        // Leitura do arquivo version.properties localizado no diretório de recursos
        try (InputStream input = Updater.class.getResourceAsStream("/" + LOCAL_VERSION_FILE)) {
            if (input == null) {
                throw new FileNotFoundException("Arquivo " + LOCAL_VERSION_FILE + " não encontrado.");
            }
            Properties properties = new Properties();
            properties.load(input);
            return properties.getProperty("version");
        }
    }

    private static String getRemoteVersion() throws IOException {
        URL url = new URL(VERSION_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");;

        int responseCode = connection.getResponseCode();
        System.out.println("HTTP Response Code: " + responseCode);
        System.out.println("Request URL: " + url);

        if (responseCode == HttpURLConnection.HTTP_OK) {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                Properties properties = new Properties();
                properties.load(reader);
                return properties.getProperty("version");
            }
        } else {
            InputStream errorStream = connection.getErrorStream();
            if (errorStream != null) {
                try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorStream))) {
                    String errorResponse = errorReader.lines().reduce("", (acc, line) -> acc + line + "\n");
                    System.out.println("Error response from server: " + errorResponse);
                }
            }
            throw new IOException("Failed to get remote version. HTTP response code: " + responseCode);
        }
    }




    private static boolean isNewerVersion(String remoteVersion, String localVersion) {
        return remoteVersion.compareTo(localVersion) > 0;
    }

    private static void downloadAndUpdateSoftware() throws IOException {
        URL url = new URL(DOWNLOAD_URL);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        try (InputStream input = connection.getInputStream();
             FileOutputStream output = new FileOutputStream(LOCAL_SOFTWARE_PATH)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
            }
        }
        System.out.println("Atualização concluída.");
    }

    public static void main(String[] args) {
        try {
            checkForUpdates();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}