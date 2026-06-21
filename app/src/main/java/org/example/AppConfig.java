package org.example;

import java.util.List;

public class AppConfig {
    private AppSettings app;
    private UiSettings ui;
    private StorageSettings storage;
    private IntegrationSettings integrations;
    private List<ModuleItem> modules;

    public AppConfig() {
    }

    public AppSettings getApp() {
        return app;
    }

    public void setApp(AppSettings app) {
        this.app = app;
    }

    public UiSettings getUi() {
        return ui;
    }

    public void setUi(UiSettings ui) {
        this.ui = ui;
    }

    public StorageSettings getStorage() {
        return storage;
    }

    public void setStorage(StorageSettings storage) {
        this.storage = storage;
    }

    public IntegrationSettings getIntegrations() {
        return integrations;
    }

    public void setIntegrations(IntegrationSettings integrations) {
        this.integrations = integrations;
    }

    public List<ModuleItem> getModules() {
        return modules;
    }

    public void setModules(List<ModuleItem> modules) {
        this.modules = modules;
    }
}