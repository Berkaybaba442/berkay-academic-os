package org.example;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.text.DecimalFormat;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class PreviewService {

    private static final long DEFAULT_TEXT_PREVIEW_LIMIT = 2L * 1024L * 1024L;
    private static final long DEFAULT_IMAGE_PREVIEW_LIMIT = 10L * 1024L * 1024L;

    public PreviewMode resolvePreviewMode(Path file, PreviewOptions options) {
        return decide(file, options).getMode();
    }

    public boolean isPreviewSizeAllowed(Path file, PreviewOptions options) {
        return isTextPreviewSizeAllowed(file, options);
    }

    public boolean isTextPreviewSizeAllowed(Path file, PreviewOptions options) {
        return isSizeAllowed(file, options == null ? DEFAULT_TEXT_PREVIEW_LIMIT : options.getMaxPreviewSizeBytes());
    }

    public boolean isImagePreviewSizeAllowed(Path file, PreviewOptions options) {
        return isSizeAllowed(file, options == null ? DEFAULT_IMAGE_PREVIEW_LIMIT : options.getMaxImagePreviewSizeBytes());
    }

    public PreviewDecision decide(Path file, PreviewOptions options) {
        if (file == null || !Files.exists(file)) {
            return new PreviewDecision(PreviewMode.UNSUPPORTED, "Dosya bulunamadı.");
        }

        if (Files.isDirectory(file)) {
            return new PreviewDecision(PreviewMode.UNSUPPORTED, "Klasörler önizlenmez.");
        }

        String extension = getExtension(file);

        if (options != null && !options.isExtensionAllowed(extension)) {
            return new PreviewDecision(PreviewMode.UNSUPPORTED, "Bu uzantı config tarafından önizleme dışında bırakıldı: " + displayExtension(extension));
        }

        if (options != null && options.shouldOpenExternally(extension)) {
            return new PreviewDecision(PreviewMode.EXTERNAL, "Bu uzantı config tarafından harici açılacak şekilde işaretli: " + displayExtension(extension));
        }

        if (isTextExtension(extension)) {
            if (options != null && !options.isPreviewTextFiles()) {
                return new PreviewDecision(PreviewMode.EXTERNAL, "Metin önizleme config ile kapalı.");
            }
            if (!isTextPreviewSizeAllowed(file, options)) {
                return new PreviewDecision(PreviewMode.EXTERNAL, "Metin dosyası önizleme boyut limitini aşıyor.");
            }
            return new PreviewDecision(PreviewMode.EDITABLE_TEXT, "Metin önizleme uygun.");
        }

        if (isImageExtension(extension)) {
            if (options != null && !options.isPreviewImageFiles()) {
                return new PreviewDecision(PreviewMode.EXTERNAL, "Görsel önizleme config ile kapalı.");
            }
            if (!isImagePreviewSizeAllowed(file, options)) {
                return new PreviewDecision(PreviewMode.EXTERNAL, "Görsel önizleme boyut limitini aşıyor.");
            }
            return new PreviewDecision(PreviewMode.IMAGE, "Görsel önizleme uygun.");
        }

        if (".pdf".equals(extension)) {
            if (options == null || options.isOpenPdfExternally()) {
                return new PreviewDecision(PreviewMode.EXTERNAL, "PDF dosyaları güvenli biçimde harici görüntüleyicide açılır.");
            }
            return new PreviewDecision(PreviewMode.PDF, "PDF için dahili bilgi kartı gösteriliyor.");
        }

        return new PreviewDecision(PreviewMode.UNSUPPORTED, "Desteklenmeyen önizleme türü: " + displayExtension(extension));
    }

    public String buildMetadataText(Path file) {
        if (file == null) {
            return "Dosya bilgisi alınamadı.";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("Ad: ").append(file.getFileName() == null ? file : file.getFileName()).append('\n');
        builder.append("Yol: ").append(file.toAbsolutePath().normalize()).append('\n');
        builder.append("Uzantı: ").append(displayExtension(getExtension(file))).append('\n');

        try {
            builder.append("Boyut: ").append(formatBytes(Files.size(file))).append('\n');
        } catch (IOException e) {
            builder.append("Boyut: okunamadı\n");
        }

        try {
            FileTime modified = Files.getLastModifiedTime(file);
            String formatted = modified.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            builder.append("Son değişiklik: ").append(formatted).append('\n');
        } catch (IOException e) {
            builder.append("Son değişiklik: okunamadı\n");
        }

        builder.append("Tür: ").append(Files.isDirectory(file) ? "Klasör" : "Dosya");
        return builder.toString();
    }

    public String getExtension(Path file) {
        if (file == null || file.getFileName() == null) {
            return "";
        }
        String name = file.getFileName().toString();
        int index = name.lastIndexOf('.');
        if (index < 0 || index == name.length() - 1) {
            return "";
        }
        return name.substring(index).toLowerCase(Locale.ROOT);
    }

    private boolean isSizeAllowed(Path file, long limitBytes) {
        if (file == null || !Files.exists(file) || limitBytes <= 0) {
            return false;
        }

        try {
            return Files.size(file) <= limitBytes;
        } catch (IOException e) {
            return false;
        }
    }

    private boolean isTextExtension(String extension) {
        return switch (extension) {
            case ".txt", ".md", ".markdown", ".json", ".csv", ".tsv", ".java", ".kt", ".kts",
                 ".xml", ".html", ".css", ".js", ".ts", ".jsx", ".tsx", ".py", ".c", ".cpp",
                 ".h", ".hpp", ".ino", ".yml", ".yaml", ".toml", ".ini", ".conf", ".properties",
                 ".gradle", ".log", ".sh", ".bash", ".zsh", ".sql" -> true;
            default -> false;
        };
    }

    private boolean isImageExtension(String extension) {
        return switch (extension) {
            case ".png", ".jpg", ".jpeg", ".gif", ".bmp", ".webp" -> true;
            default -> false;
        };
    }

    private String displayExtension(String extension) {
        return extension == null || extension.isBlank() ? "uzantısız" : extension;
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        double value = bytes;
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        while (value >= 1024 && unitIndex < units.length - 1) {
            value /= 1024.0;
            unitIndex++;
        }
        return new DecimalFormat("0.##").format(value) + " " + units[unitIndex];
    }

    public enum PreviewMode {
        EDITABLE_TEXT,
        IMAGE,
        PDF,
        EXTERNAL,
        UNSUPPORTED
    }

    public static class PreviewDecision {
        private final PreviewMode mode;
        private final String message;

        public PreviewDecision(PreviewMode mode, String message) {
            this.mode = mode;
            this.message = message;
        }

        public PreviewMode getMode() {
            return mode;
        }

        public String getMessage() {
            return message;
        }
    }
}