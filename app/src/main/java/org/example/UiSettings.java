package org.example;

public class UiSettings {
    private int sidebarWidth;
    private String defaultStatus;
    private boolean webNotesEnabledByDefault;

    public UiSettings() {
    }

    public int getSidebarWidth() {
        return sidebarWidth;
    }

    public String getDefaultStatus() {
        return defaultStatus;
    }

    public boolean isWebNotesEnabledByDefault() {
        return webNotesEnabledByDefault;
    }

    public void setSidebarWidth(int sidebarWidth) {
        this.sidebarWidth = sidebarWidth;
    }

    public void setDefaultStatus(String defaultStatus) {
        this.defaultStatus = defaultStatus;
    }

    public void setWebNotesEnabledByDefault(boolean webNotesEnabledByDefault) {
        this.webNotesEnabledByDefault = webNotesEnabledByDefault;
    }
}