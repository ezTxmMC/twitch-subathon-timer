package de.syntaxjason.model;

import java.nio.file.Path;
import java.time.LocalDateTime;

public class BackupInfo {
    private final Path path;
    private final LocalDateTime timestamp;
    private final long sizeBytes;

    public BackupInfo(Path path, LocalDateTime timestamp, long sizeBytes) {
        this.path = path;
        this.timestamp = timestamp;
        this.sizeBytes = sizeBytes;
    }

    public Path getPath() {
        return path;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public String getFormattedSize() {
        if (sizeBytes < 1024) {
            return sizeBytes + " B";
        }

        if (sizeBytes < 1024 * 1024) {
            return String.format("%.2f KB", sizeBytes / 1024.0);
        }

        return String.format("%.2f MB", sizeBytes / (1024.0 * 1024.0));
    }

    public String getFileName() {
        return path.getFileName().toString();
    }
}
