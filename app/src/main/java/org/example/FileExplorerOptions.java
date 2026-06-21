package org.example;

public class FileExplorerOptions {
    private String defaultRoot;
    private boolean readOnly;
    private boolean allowCreateFolder;
    private boolean allowCreateFile;
    private boolean allowRename;
    private boolean allowDelete;
    private boolean allowDragDropImport;
    private boolean showPreviewPane;
    private boolean showHiddenFiles;

    public FileExplorerOptions() {
    }

    public String getDefaultRoot() {
        return defaultRoot;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public boolean isAllowCreateFolder() {
        return allowCreateFolder;
    }

    public boolean isAllowCreateFile() {
        return allowCreateFile;
    }

    public boolean isAllowRename() {
        return allowRename;
    }

    public boolean isAllowDelete() {
        return allowDelete;
    }

    public boolean isAllowDragDropImport() {
        return allowDragDropImport;
    }

    public boolean isShowPreviewPane() {
        return showPreviewPane;
    }

    public boolean isShowHiddenFiles() {
        return showHiddenFiles;
    }

    public boolean canCreateFolder() {
        return !readOnly && allowCreateFolder;
    }

    public boolean canCreateFile() {
        return !readOnly && allowCreateFile;
    }

    public boolean canRename() {
        return !readOnly && allowRename;
    }

    public boolean canDelete() {
        return !readOnly && allowDelete;
    }

    public boolean canEditFiles() {
        return !readOnly;
    }

    public boolean canDragDropImport() {
        return !readOnly && allowDragDropImport;
    }

    public String permissionSummary() {
        if (readOnly) {
            return "Salt okunur mod aktif: oluşturma, düzenleme, yeniden adlandırma ve silme kapalı.";
        }

        return "İzinler: "
                + "klasör oluşturma=" + status(allowCreateFolder)
                + ", dosya oluşturma=" + status(allowCreateFile)
                + ", yeniden adlandırma=" + status(allowRename)
                + ", silme=" + status(allowDelete)
                + ", drag-drop=" + status(allowDragDropImport);
    }

    private String status(boolean enabled) {
        return enabled ? "açık" : "kapalı";
    }

    public void setDefaultRoot(String defaultRoot) {
        this.defaultRoot = defaultRoot;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public void setAllowCreateFolder(boolean allowCreateFolder) {
        this.allowCreateFolder = allowCreateFolder;
    }

    public void setAllowCreateFile(boolean allowCreateFile) {
        this.allowCreateFile = allowCreateFile;
    }

    public void setAllowRename(boolean allowRename) {
        this.allowRename = allowRename;
    }

    public void setAllowDelete(boolean allowDelete) {
        this.allowDelete = allowDelete;
    }

    public void setAllowDragDropImport(boolean allowDragDropImport) {
        this.allowDragDropImport = allowDragDropImport;
    }

    public void setShowPreviewPane(boolean showPreviewPane) {
        this.showPreviewPane = showPreviewPane;
    }

    public void setShowHiddenFiles(boolean showHiddenFiles) {
        this.showHiddenFiles = showHiddenFiles;
    }
}