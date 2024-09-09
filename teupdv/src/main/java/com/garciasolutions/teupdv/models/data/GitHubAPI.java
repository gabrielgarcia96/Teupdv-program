package com.garciasolutions.teupdv.models.data;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class GitHubAPI {

    private static final String API_URL = "https://api.github.com/repos/gabrielgarcia96/Teupdv-program/releases";
    private static final String GITHUB_TOKEN = "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";

    private static HttpURLConnection createConnection(URL url) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Authorization", "token " + GITHUB_TOKEN);
        connection.setRequestProperty("Accept", "application/vnd.github.v3+json");// Adicione o cabeçalho de autorização
        return connection;
    }

    public static String getLatestVersion() throws IOException {
        URL url = new URL(API_URL);
        HttpURLConnection connection = createConnection(url);
        int responseCode = connection.getResponseCode();

        System.out.println("Response Code: " + responseCode);

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

    public static void main(String[] args) {
        try {
            String latestVersion = getLatestVersion();
            System.out.println("Última versão disponível: " + latestVersion);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

