package org.example;

import java.time.LocalDate;

public class TaskItem {
    private String title;
    private LocalDate date;
    private String description;
    private String status;

    public TaskItem(String title, LocalDate date, String description, String status) {
        this.title = title;
        this.date = date;
        this.description = description;
        this.status = status;
    }

    public String getTitle() {
        return title;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getStatus() {
        return status;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return title + " | " + date + " | " + status;
    }
}