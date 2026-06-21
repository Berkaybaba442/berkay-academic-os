package org.example;

public class ModuleDataSources {
    private LocalDataSource local;
    private GoogleDriveDataSource googleDrive;

    public ModuleDataSources() {
    }

    public LocalDataSource getLocal() {
        return local;
    }

    public GoogleDriveDataSource getGoogleDrive() {
        return googleDrive;
    }

    public void setLocal(LocalDataSource local) {
        this.local = local;
    }

    public void setGoogleDrive(GoogleDriveDataSource googleDrive) {
        this.googleDrive = googleDrive;
    }
}