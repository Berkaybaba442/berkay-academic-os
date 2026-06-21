package org.example;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.calendar.Calendar;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.Events;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GoogleCalendarService {

    private final GoogleCalendarOAuthService authService = new GoogleCalendarOAuthService();

    private Calendar calendarService;
    private boolean connected = false;
    private String lastErrorMessage = null;

    public synchronized void connect() throws Exception {
        lastErrorMessage = null;

        Path credentialsPath = resolveCredentialsPath();
        if (!Files.exists(credentialsPath)) {
            connected = false;
            throw new IllegalStateException("credentials dosyası bulunamadı: " + credentialsPath);
        }

        Path tokensDir = resolveTokensDir();
        Files.createDirectories(tokensDir);

        Credential credential = authService.authorize(credentialsPath, tokensDir);
        calendarService = new Calendar.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
        )
                .setApplicationName("Berkay Academic OS")
                .build();

        connected = true;
    }

    public synchronized boolean isConnected() {
        return connected && calendarService != null;
    }

    public synchronized List<CalendarEventItem> loadEventsForMonth(YearMonth month) {
        lastErrorMessage = null;

        if (!isConnected()) {
            return new ArrayList<>();
        }

        try {
            Instant startInstant = month.atDay(1).atStartOfDay(ZoneId.systemDefault()).toInstant();
            Instant endInstant = month.atEndOfMonth().plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant();

            Events events = calendarService.events().list("primary")
                    .setTimeMin(new com.google.api.client.util.DateTime(startInstant.toEpochMilli()))
                    .setTimeMax(new com.google.api.client.util.DateTime(endInstant.toEpochMilli()))
                    .setOrderBy("startTime")
                    .setSingleEvents(true)
                    .setMaxResults(250)
                    .execute();

            List<CalendarEventItem> items = new ArrayList<>();
            if (events.getItems() != null) {
                for (Event event : events.getItems()) {
                    CalendarEventItem item = convert(event);
                    if (item != null) {
                        items.add(item);
                    }
                }
            }

            items.sort(Comparator.comparing(CalendarEventItem::getDate)
                    .thenComparing(e -> e.getStartTime() == null ? LocalTime.MIN : e.getStartTime())
                    .thenComparing(e -> e.getTitle() == null ? "" : e.getTitle(), String.CASE_INSENSITIVE_ORDER));

            return items;
        } catch (Exception e) {
            e.printStackTrace();
            lastErrorMessage = e.getClass().getSimpleName() + ": " + e.getMessage();
            return new ArrayList<>();
        }
    }

    public synchronized String getSourceHint() {
        return isConnected() ? "Kaynak: Google Calendar OAuth" : "Google Calendar bağlı değil";
    }

    public synchronized String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private CalendarEventItem convert(Event event) {
        if (event == null || event.getStart() == null) {
            return null;
        }

        String id = event.getId();
        String title = event.getSummary() == null || event.getSummary().isBlank()
                ? "(Başlıksız Etkinlik)" : event.getSummary();

        String description = event.getDescription();
        String location = event.getLocation();

        if (event.getStart().getDate() != null) {
            LocalDate date = LocalDate.parse(event.getStart().getDate().toStringRfc3339().substring(0, 10));
            return CalendarEventItem.allDay(id, title, date, description, location);
        }

        if (event.getStart().getDateTime() != null) {
            Instant startInstant = Instant.ofEpochMilli(event.getStart().getDateTime().getValue());
            Instant endInstant = event.getEnd() != null && event.getEnd().getDateTime() != null
                    ? Instant.ofEpochMilli(event.getEnd().getDateTime().getValue())
                    : startInstant.plusSeconds(3600);

            LocalDateTime start = LocalDateTime.ofInstant(startInstant, ZoneId.systemDefault());
            LocalDateTime end = LocalDateTime.ofInstant(endInstant, ZoneId.systemDefault());

            return CalendarEventItem.timed(id, title, start, end, description, location);
        }

        return null;
    }

    private Path resolveCredentialsPath() {
        Path workingDir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path projectRoot = workingDir;
        if (workingDir.getFileName() != null && "app".equals(workingDir.getFileName().toString())) {
            projectRoot = workingDir.getParent();
        }
        return projectRoot.resolve("config/google-calendar-credentials.json");
    }

    private Path resolveTokensDir() {
        Path workingDir = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        Path projectRoot = workingDir;
        if (workingDir.getFileName() != null && "app".equals(workingDir.getFileName().toString())) {
            projectRoot = workingDir.getParent();
        }
        return projectRoot.resolve("config/google-calendar-tokens");
    }
}