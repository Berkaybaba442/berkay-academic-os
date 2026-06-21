package org.example;

import java.util.List;

public class GoogleDriveDataSource {
    private boolean enabled;
    private String rootFolderName;
    private List<String> allowedMimeTypes;

    public GoogleDriveDataSource() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getRootFolderName() {
        return rootFolderName;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setRootFolderName(String rootFolderName) {
        this.rootFolderName = rootFolderName;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }
}