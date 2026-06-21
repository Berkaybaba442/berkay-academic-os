package org.example;

public class ModuleFeatures {
    private boolean searchEnabled;
    private boolean editorEnabled;
    private boolean googleDriveEnabled;
    private boolean localFilesEnabled;
    private boolean previewEnabled;
    private boolean liveMonitor;
    private boolean loggingEnabled;

    public ModuleFeatures() {
    }

    public boolean isSearchEnabled() {
        return searchEnabled;
    }

    public boolean isEditorEnabled() {
        return editorEnabled;
    }

    public boolean isGoogleDriveEnabled() {
        return googleDriveEnabled;
    }

    public boolean isLocalFilesEnabled() {
        return localFilesEnabled;
    }

    public boolean isPreviewEnabled() {
        return previewEnabled;
    }

    public boolean isLiveMonitor() {
        return liveMonitor;
    }

    public boolean isLoggingEnabled() {
        return loggingEnabled;
    }

    public void setSearchEnabled(boolean searchEnabled) {
        this.searchEnabled = searchEnabled;
    }

    public void setEditorEnabled(boolean editorEnabled) {
        this.editorEnabled = editorEnabled;
    }

    public void setGoogleDriveEnabled(boolean googleDriveEnabled) {
        this.googleDriveEnabled = googleDriveEnabled;
    }

    public void setLocalFilesEnabled(boolean localFilesEnabled) {
        this.localFilesEnabled = localFilesEnabled;
    }

    public void setPreviewEnabled(boolean previewEnabled) {
        this.previewEnabled = previewEnabled;
    }

    public void setLiveMonitor(boolean liveMonitor) {
        this.liveMonitor = liveMonitor;
    }

    public void setLoggingEnabled(boolean loggingEnabled) {
        this.loggingEnabled = loggingEnabled;
    }
}