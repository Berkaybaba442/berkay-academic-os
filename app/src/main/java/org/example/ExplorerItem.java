package org.example;

import java.nio.file.Path;

public class ExplorerItem {
    private final Path path;
    private final boolean directory;

    public ExplorerItem(Path path, boolean directory) {
        this.path = path;
        this.directory = directory;
    }

    public Path getPath() {
        return path;
    }

    public boolean isDirectory() {
        return directory;
    }

    @Override
    public String toString() {
        return path.getFileName().toString();
    }
}