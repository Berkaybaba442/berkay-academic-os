package org.example;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class CalendarEventItem {
    private String id;
    private String title;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String description;
    private String location;
    private boolean allDay;

    public CalendarEventItem() {
    }

    public CalendarEventItem(String id, String title, LocalDate date, LocalTime startTime, LocalTime endTime,
                             String description, String location, boolean allDay) {
        this.id = id;
        this.title = title;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.description = description;
        this.location = location;
        this.allDay = allDay;
    }

    public static CalendarEventItem allDay(String id, String title, LocalDate date, String description, String location) {
        return new CalendarEventItem(id, title, date, null, null, description, location, true);
    }

    public static CalendarEventItem timed(String id, String title, LocalDateTime start, LocalDateTime end,
                                          String description, String location) {
        return new CalendarEventItem(
                id,
                title,
                start.toLocalDate(),
                start.toLocalTime(),
                end == null ? null : end.toLocalTime(),
                description,
                location,
                false
        );
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public LocalDate getDate() { return date; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public String getDescription() { return description; }
    public String getLocation() { return location; }
    public boolean isAllDay() { return allDay; }

    public String toDisplayText() {
        StringBuilder builder = new StringBuilder();
        if (allDay || startTime == null) {
            builder.append("[Tüm Gün] ");
        } else {
            builder.append(startTime);
            if (endTime != null) {
                builder.append(" - ").append(endTime);
            }
            builder.append(" | ");
        }
        builder.append(title == null ? "(Başlıksız)" : title);
        if (location != null && !location.isBlank()) {
            builder.append(" @ ").append(location);
        }
        return builder.toString();
    }
}