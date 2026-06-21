package org.example;

public class ModuleItem {
    private String id;
    private String name;
    private String category;
    private String type;
    private boolean enabled;
    private String icon;
    private String message;
    private String target;
    private ModuleFeatures features;
    private WebOptions webOptions;
    private ScriptOptions scriptOptions;
    private ModuleDataSources dataSources;
    
    private java.util.List<FavoriteLocation> favorites;
    private FileExplorerOptions explorerOptions;
    private PreviewOptions previewOptions;

    public ModuleItem() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getType() {
        return type;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getIcon() {
        return icon;
    }

    public String getMessage() {
        return message;
    }

    public String getTarget() {
        return target;
    }

    public ModuleFeatures getFeatures() {
        return features;
    }

    public WebOptions getWebOptions() {
        return webOptions;
    }

    public ScriptOptions getScriptOptions() {
        return scriptOptions;
    }

    public ModuleDataSources getDataSources() {
        return dataSources;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public void setFeatures(ModuleFeatures features) {
        this.features = features;
    }

    public void setWebOptions(WebOptions webOptions) {
        this.webOptions = webOptions;
    }

    public void setScriptOptions(ScriptOptions scriptOptions) {
        this.scriptOptions = scriptOptions;
    }

    public void setDataSources(ModuleDataSources dataSources) {
        this.dataSources = dataSources;
    }
    public java.util.List<FavoriteLocation> getFavorites() {
        return favorites;
    }
    
    public void setFavorites(java.util.List<FavoriteLocation> favorites) {
        this.favorites = favorites;
    }
    
    public FileExplorerOptions getExplorerOptions() {
        return explorerOptions;
    }
    
    public void setExplorerOptions(FileExplorerOptions explorerOptions) {
        this.explorerOptions = explorerOptions;
    }
    
    public PreviewOptions getPreviewOptions() {
        return previewOptions;
    }
    
    public void setPreviewOptions(PreviewOptions previewOptions) {
        this.previewOptions = previewOptions;
    }
}