package org.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PreviewOptions {

    private List<String> allowedExtensions = new ArrayList<>();
    private List<String> openExternallyExtensions = new ArrayList<>();
    private long maxPreviewSizeBytes = 2L * 1024L * 1024L;
    private long maxImagePreviewSizeBytes = 10L * 1024L * 1024L;
    private boolean showFileMetadata = true;
    private boolean previewTextFiles = true;
    private boolean previewImageFiles = true;
    private boolean openPdfExternally = true;

    public List<String> getAllowedExtensions() {
        return allowedExtensions;
    }

    public void setAllowedExtensions(List<String> allowedExtensions) {
        this.allowedExtensions = normalize(allowedExtensions);
    }

    public List<String> getOpenExternallyExtensions() {
        return openExternallyExtensions;
    }

    public void setOpenExternallyExtensions(List<String> openExternallyExtensions) {
        this.openExternallyExtensions = normalize(openExternallyExtensions);
    }

    public long getMaxPreviewSizeBytes() {
        return maxPreviewSizeBytes;
    }

    public void setMaxPreviewSizeBytes(long maxPreviewSizeBytes) {
        this.maxPreviewSizeBytes = Math.max(0, maxPreviewSizeBytes);
    }

    public long getMaxImagePreviewSizeBytes() {
        return maxImagePreviewSizeBytes;
    }

    public void setMaxImagePreviewSizeBytes(long maxImagePreviewSizeBytes) {
        this.maxImagePreviewSizeBytes = Math.max(0, maxImagePreviewSizeBytes);
    }

    public boolean isShowFileMetadata() {
        return showFileMetadata;
    }

    public void setShowFileMetadata(boolean showFileMetadata) {
        this.showFileMetadata = showFileMetadata;
    }

    public boolean isPreviewTextFiles() {
        return previewTextFiles;
    }

    public void setPreviewTextFiles(boolean previewTextFiles) {
        this.previewTextFiles = previewTextFiles;
    }

    public boolean isPreviewImageFiles() {
        return previewImageFiles;
    }

    public void setPreviewImageFiles(boolean previewImageFiles) {
        this.previewImageFiles = previewImageFiles;
    }

    public boolean isOpenPdfExternally() {
        return openPdfExternally;
    }

    public void setOpenPdfExternally(boolean openPdfExternally) {
        this.openPdfExternally = openPdfExternally;
    }

    public boolean isExtensionAllowed(String extension) {
        return allowedExtensions.isEmpty() || allowedExtensions.contains(normalizeExtension(extension));
    }

    public boolean shouldOpenExternally(String extension) {
        return openExternallyExtensions.contains(normalizeExtension(extension));
    }

    private List<String> normalize(List<String> values) {
        List<String> result = new ArrayList<>();
        if (values == null) {
            return result;
        }
        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            String normalized = normalizeExtension(value);
            if (!result.contains(normalized)) {
                result.add(normalized);
            }
        }
        return result;
    }

    private String normalizeExtension(String extension) {
        if (extension == null) {
            return "";
        }
        String cleaned = extension.trim().toLowerCase(Locale.ROOT);
        if (cleaned.isBlank()) {
            return "";
        }
        return cleaned.startsWith(".") ? cleaned : "." + cleaned;
    }
}