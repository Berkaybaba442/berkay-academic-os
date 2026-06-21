package org.example;

public class StorageSettings {
    private String notesDir;
    private String cacheDir;
    private String downloadsDir;

    public StorageSettings() {
    }

    public String getNotesDir() {
        return notesDir;
    }

    public String getCacheDir() {
        return cacheDir;
    }

    public String getDownloadsDir() {
        return downloadsDir;
    }

    public void setNotesDir(String notesDir) {
        this.notesDir = notesDir;
    }

    public void setCacheDir(String cacheDir) {
        this.cacheDir = cacheDir;
    }

    public void setDownloadsDir(String downloadsDir) {
        this.downloadsDir = downloadsDir;
    }
}