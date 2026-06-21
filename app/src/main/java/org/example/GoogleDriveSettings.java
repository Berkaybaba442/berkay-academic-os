package org.example;

public class GoogleDriveSettings {
    private boolean enabled;
    private String credentialsPath;
    private String tokensPath;
    private String rootFolderName;

    public GoogleDriveSettings() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getCredentialsPath() {
        return credentialsPath;
    }

    public String getTokensPath() {
        return tokensPath;
    }

    public String getRootFolderName() {
        return rootFolderName;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setCredentialsPath(String credentialsPath) {
        this.credentialsPath = credentialsPath;
    }

    public void setTokensPath(String tokensPath) {
        this.tokensPath = tokensPath;
    }

    public void setRootFolderName(String rootFolderName) {
        this.rootFolderName = rootFolderName;
    }
}