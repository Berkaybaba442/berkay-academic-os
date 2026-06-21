package org.example;

public class LocalDataSource {
    private boolean enabled;
    private String path;

    public LocalDataSource() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getPath() {
        return path;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setPath(String path) {
        this.path = path;
    }
}