package org.example;

public class WebOptions {
    private boolean internalBrowser;
    private boolean notesEnabled;
    private boolean autoLoad;

    public WebOptions() {
    }

    public boolean isInternalBrowser() {
        return internalBrowser;
    }

    public boolean isNotesEnabled() {
        return notesEnabled;
    }

    public boolean isAutoLoad() {
        return autoLoad;
    }

    public void setInternalBrowser(boolean internalBrowser) {
        this.internalBrowser = internalBrowser;
    }

    public void setNotesEnabled(boolean notesEnabled) {
        this.notesEnabled = notesEnabled;
    }

    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }
}