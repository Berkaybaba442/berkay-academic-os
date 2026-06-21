package org.example;

public class AppSettings {
    private String name;
    private String version;
    private String theme;

    public AppSettings() {
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getTheme() {
        return theme;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setTheme(String theme) {
        this.theme = theme;
    }
}