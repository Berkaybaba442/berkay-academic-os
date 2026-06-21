package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class FileOperationService {

    public List<Path> listSortedDirectory(Path dir, boolean showHiddenFiles) throws IOException {
        List<Path> items = new ArrayList<>();

        try (var stream = Files.list(dir)) {
            stream.filter(path -> showHiddenFiles || !path.getFileName().toString().startsWith("."))
                  .forEach(items::add);
        }

        items.sort(Comparator
                .comparing((Path p) -> !Files.isDirectory(p))
                .thenComparing(p -> p.getFileName().toString().toLowerCase()));

        return items;
    }

    public void createFolder(Path parent, String name) throws IOException {
        validateParentDirectory(parent);
        Path target = resolveSafeChild(parent, name);
        Files.createDirectories(target);
    }

    public void createFolder(Path parent, String name, FileExplorerOptions options) throws IOException {
        ensureAllowed(options != null && options.canCreateFolder(), "Klasör oluşturma izni kapalı");
        createFolder(parent, name);
    }

    public void createEmptyFile(Path parent, String name) throws IOException {
        validateParentDirectory(parent);
        Path file = resolveSafeChild(parent, name);
        if (!Files.exists(file)) {
            Files.createFile(file);
        }
    }

    public void createEmptyFile(Path parent, String name, FileExplorerOptions options) throws IOException {
        ensureAllowed(options != null && options.canCreateFile(), "Dosya oluşturma izni kapalı");
        createEmptyFile(parent, name);
    }

    public String readTextFile(Path file) throws IOException {
        return Files.readString(file, StandardCharsets.UTF_8);
    }

    public void saveTextFile(Path file, String content) throws IOException {
        Files.writeString(file, content, StandardCharsets.UTF_8);
    }

    public void saveTextFile(Path file, String content, FileExplorerOptions options) throws IOException {
        ensureAllowed(options != null && options.canEditFiles(), "Dosya düzenleme izni kapalı");
        saveTextFile(file, content);
    }

    public void delete(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            return;
        }

        if (Files.isDirectory(path)) {
            try (var walk = Files.walk(path)) {
                walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
            } catch (RuntimeException e) {
                if (e.getCause() instanceof IOException ioException) {
                    throw ioException;
                }
                throw e;
            }
        } else {
            Files.deleteIfExists(path);
        }
    }

    public void delete(Path path, FileExplorerOptions options) throws IOException {
        ensureAllowed(options != null && options.canDelete(), "Silme izni kapalı");
        delete(path);
    }

    public void rename(Path source, String newName) throws IOException {
        validateExistingPath(source);
        Path target = resolveSafeSibling(source, newName);
        Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
    }

    public void rename(Path source, String newName, FileExplorerOptions options) throws IOException {
        ensureAllowed(options != null && options.canRename(), "Yeniden adlandırma izni kapalı");
        rename(source, newName);
    }

    public ImportResult importPaths(List<Path> sources, Path targetDirectory, FileExplorerOptions options) throws IOException {
        ensureAllowed(options != null && options.canDragDropImport(), "Drag & Drop import izni kapalı");
        return importPaths(sources, targetDirectory);
    }

    public ImportResult importPaths(List<Path> sources, Path targetDirectory) throws IOException {
        validateParentDirectory(targetDirectory);

        if (sources == null || sources.isEmpty()) {
            return new ImportResult(0, 0);
        }

        int copiedFiles = 0;
        int copiedDirectories = 0;

        for (Path source : sources) {
            if (source == null || !Files.exists(source)) {
                continue;
            }

            Path normalizedSource = source.toAbsolutePath().normalize();
            Path normalizedTargetDirectory = targetDirectory.toAbsolutePath().normalize();

            if (Files.isDirectory(normalizedSource) && normalizedTargetDirectory.startsWith(normalizedSource)) {
                throw new AccessDeniedException("Bir klasör kendi içine veya alt klasörüne import edilemez: " + normalizedSource);
            }

            Path target = uniqueTargetPath(normalizedTargetDirectory, normalizedSource.getFileName().toString());

            if (Files.isDirectory(normalizedSource)) {
                copyDirectoryRecursively(normalizedSource, target);
                copiedDirectories++;
            } else {
                Files.copy(normalizedSource, target, StandardCopyOption.COPY_ATTRIBUTES);
                copiedFiles++;
            }
        }

        return new ImportResult(copiedFiles, copiedDirectories);
    }

    private void copyDirectoryRecursively(Path sourceDirectory, Path targetDirectory) throws IOException {
        try (var walk = Files.walk(sourceDirectory)) {
            for (Path sourcePath : walk.toList()) {
                Path relativePath = sourceDirectory.relativize(sourcePath);
                Path targetPath = targetDirectory.resolve(relativePath).normalize();

                if (Files.isDirectory(sourcePath)) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(Objects.requireNonNull(targetPath.getParent()));
                    Files.copy(sourcePath, targetPath, StandardCopyOption.COPY_ATTRIBUTES);
                }
            }
        }
    }

    private Path uniqueTargetPath(Path parent, String originalName) throws IOException {
        String safeOriginalName = normalizeName(originalName);
        Path candidate = parent.resolve(safeOriginalName).normalize();

        if (!candidate.getParent().equals(parent.normalize())) {
            throw new AccessDeniedException("Geçersiz import hedefi: " + originalName);
        }

        if (!Files.exists(candidate)) {
            return candidate;
        }

        String baseName = safeOriginalName;
        String extension = "";
        int dotIndex = safeOriginalName.lastIndexOf('.');

        if (dotIndex > 0) {
            baseName = safeOriginalName.substring(0, dotIndex);
            extension = safeOriginalName.substring(dotIndex);
        }

        for (int i = 1; i < 10_000; i++) {
            candidate = parent.resolve(baseName + " (" + i + ")" + extension).normalize();
            if (!candidate.getParent().equals(parent.normalize())) {
                throw new AccessDeniedException("Geçersiz import hedefi: " + originalName);
            }
            if (!Files.exists(candidate)) {
                return candidate;
            }
        }

        throw new FileAlreadyExistsException("Çakışmasız dosya adı üretilemedi: " + originalName);
    }

    public List<ExplorerItem> listExplorerItems(Path dir, boolean showHiddenFiles) throws IOException {
        List<ExplorerItem> items = new ArrayList<>();

        try (var stream = Files.list(dir)) {
            stream.filter(path -> showHiddenFiles || !path.getFileName().toString().startsWith("."))
                  .forEach(path -> items.add(new ExplorerItem(path, Files.isDirectory(path))));
        }

        items.sort(Comparator
                .comparing((ExplorerItem item) -> !item.isDirectory())
                .thenComparing(item -> item.getPath().getFileName().toString().toLowerCase()));

        return items;
    }

    private void ensureAllowed(boolean allowed, String message) throws AccessDeniedException {
        if (!allowed) {
            throw new AccessDeniedException(message);
        }
    }

    private void validateParentDirectory(Path parent) throws IOException {
        if (parent == null || !Files.exists(parent) || !Files.isDirectory(parent)) {
            throw new NoSuchFileException(parent == null ? "null" : parent.toString());
        }
    }

    private void validateExistingPath(Path path) throws IOException {
        if (path == null || !Files.exists(path)) {
            throw new NoSuchFileException(path == null ? "null" : path.toString());
        }
    }

    private Path resolveSafeChild(Path parent, String rawName) throws IOException {
        String name = normalizeName(rawName);
        Path target = parent.resolve(name).normalize();
        if (!target.getParent().equals(parent.normalize())) {
            throw new AccessDeniedException("Geçersiz dosya/klasör adı: " + rawName);
        }
        return target;
    }

    private Path resolveSafeSibling(Path source, String rawName) throws IOException {
        Path parent = source.getParent();
        if (parent == null) {
            throw new AccessDeniedException("Kök öğe yeniden adlandırılamaz");
        }
        return resolveSafeChild(parent, rawName);
    }

    private String normalizeName(String rawName) throws IOException {
        if (rawName == null) {
            throw new AccessDeniedException("Ad boş olamaz");
        }

        String name = rawName.trim();
        if (name.isBlank() || name.contains("/") || name.contains("\\") || ".".equals(name) || "..".equals(name)) {
            throw new AccessDeniedException("Geçersiz dosya/klasör adı: " + rawName);
        }

        return name;
    }


    public static class ImportResult {
        private final int copiedFiles;
        private final int copiedDirectories;

        public ImportResult(int copiedFiles, int copiedDirectories) {
            this.copiedFiles = copiedFiles;
            this.copiedDirectories = copiedDirectories;
        }

        public int getCopiedFiles() {
            return copiedFiles;
        }

        public int getCopiedDirectories() {
            return copiedDirectories;
        }

        public int getTotalCopiedItems() {
            return copiedFiles + copiedDirectories;
        }

        public String toStatusMessage() {
            if (getTotalCopiedItems() == 0) {
                return "Durum: Import edilecek geçerli öğe bulunamadı";
            }
            return "Durum: Import tamamlandı - dosya=" + copiedFiles + ", klasör=" + copiedDirectories;
        }
    }
}