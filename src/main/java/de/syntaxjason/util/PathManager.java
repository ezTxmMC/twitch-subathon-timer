package de.syntaxjason.util;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathManager {
    private static final String APP_NAME = "sabathon";

    public static Path getAppDataPath() {
        String os = System.getProperty("os.name").toLowerCase();

        if (os.contains("win")) {
            Path p = Paths.get(System.getenv("APPDATA"), APP_NAME);
            createDirectoryIfNotExists(p);
            return p;
        }

        if (os.contains("mac")) {
            Path p = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
            createDirectoryIfNotExists(p);
            return p;
        }

        Path p = Paths.get(System.getProperty("user.home"), "." + APP_NAME);
        createDirectoryIfNotExists(p);
        return p;
    }

    public static Path getConfigPath() {
        return getAppDataPath().resolve("config.json");
    }

    public static Path getDatabasePath() {
        return getAppDataPath().resolve("sabathon.db");
    }

    public static Path getBackupsPath() {
        Path backupsPath = getAppDataPath().resolve("backups");
        createDirectoryIfNotExists(backupsPath);
        return backupsPath;
    }

    private static void createDirectoryIfNotExists(Path path) {
        try {
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
        } catch (Exception e) {
            System.err.println("Fehler beim Erstellen des Verzeichnisses: " + path);
            e.printStackTrace();
        }
    }
}
