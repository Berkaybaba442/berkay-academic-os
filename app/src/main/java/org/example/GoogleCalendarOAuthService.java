package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class GoogleCalendarOAuthService {

    private static final GsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final List<String> SCOPES = List.of(CalendarScopes.CALENDAR_READONLY);
    private static final String USER_ID = "user";

    public Credential authorize(Path credentialsPath, Path tokensDir) throws Exception {
        if (credentialsPath == null || !Files.exists(credentialsPath)) {
            throw new IllegalStateException("Google credentials dosyası bulunamadı: " + credentialsPath);
        }

        if (tokensDir == null) {
            throw new IllegalStateException("Google token klasörü null olamaz");
        }
        Files.createDirectories(tokensDir);

        var httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        GoogleClientSecrets clientSecrets;
        try (var in = Files.newInputStream(credentialsPath)) {
            clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        }

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport,
                JSON_FACTORY,
                clientSecrets,
                SCOPES
        )
                .setDataStoreFactory(new FileDataStoreFactory(tokensDir.toFile()))
                .setAccessType("offline")
                .build();

        Credential existingCredential = flow.loadCredential(USER_ID);
        if (existingCredential != null) {
            Long expiresIn = existingCredential.getExpiresInSeconds();
            if (expiresIn == null || expiresIn > 60 || existingCredential.refreshToken()) {
                return existingCredential;
            }
        }

        LocalServerReceiver receiver = new LocalServerReceiver.Builder()
                .setPort(8888)
                .build();

        return new AuthorizationCodeInstalledApp(flow, receiver).authorize(USER_ID);
    }
}