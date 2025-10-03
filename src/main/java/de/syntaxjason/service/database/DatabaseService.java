package de.syntaxjason.service.database;

import de.syntaxjason.model.BackupInfo;
import de.syntaxjason.model.EventType;
import de.syntaxjason.model.SabathonConfig;
import de.syntaxjason.model.TimerEvent;
import de.syntaxjason.repository.EventRepository;
import de.syntaxjason.repository.IEventRepository;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.util.PathManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

public class DatabaseService implements IDatabaseService {
    private IEventRepository eventRepository;
    private final IConfigService configService;

    public DatabaseService(IConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void initialize() {
        SabathonConfig config = configService.getConfig();
        this.eventRepository = new EventRepository(config);

        if (config.getDatabaseSettings().isEnableBackups()) {
            createBackup();
        }
    }

    @Override
    public void saveEvent(TimerEvent event) {
        eventRepository.save(event);
    }

    @Override
    public List<TimerEvent> getAllEvents() {
        return eventRepository.findAll();
    }

    @Override
    public List<TimerEvent> getEventsByUsername(String username) {
        return eventRepository.findByUsername(username);
    }

    @Override
    public boolean hasRecentEvent(String username, EventType eventType, int hours) {
        LocalDateTime cutoff = LocalDateTime.now().minusHours(hours);
        return eventRepository.existsByUsernameAndTypeAfter(username, eventType, cutoff);
    }

    @Override
    public void clearAllEvents() {
        eventRepository.deleteAll();
    }

    @Override
    public void createBackup() {
        Path dbPath = PathManager.getDatabasePath();

        if (!Files.exists(dbPath)) {
            return;
        }

        try {
            Path backupsPath = PathManager.getBackupsPath();
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
            Path backupPath = backupsPath.resolve("sabathon_backup_" + timestamp + ".db");

            Files.copy(dbPath, backupPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("Backup erstellt: " + backupPath);

            cleanOldBackups();

        } catch (IOException e) {
            System.err.println("Fehler beim Erstellen des Backups");
            e.printStackTrace();
        }
    }

    @Override
    public List<BackupInfo> getAvailableBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        Path backupsPath = PathManager.getBackupsPath();

        try (Stream<Path> paths = Files.list(backupsPath)) {
            paths.filter(path -> path.getFileName().toString().startsWith("sabathon_backup_"))
                    .filter(path -> path.getFileName().toString().endsWith(".db"))
                    .forEach(path -> {
                        try {
                            LocalDateTime timestamp = extractTimestampFromFilename(path.getFileName().toString());
                            long size = Files.size(path);
                            backups.add(new BackupInfo(path, timestamp, size));
                        } catch (IOException e) {
                            System.err.println("Fehler beim Lesen von Backup: " + path);
                        }
                    });

            backups.sort(Comparator.comparing(BackupInfo::getTimestamp).reversed());

        } catch (IOException e) {
            System.err.println("Fehler beim Auflisten der Backups");
            e.printStackTrace();
        }

        return backups;
    }

    @Override
    public boolean restoreBackup(Path backupPath) {
        if (!Files.exists(backupPath)) {
            System.err.println("Backup nicht gefunden: " + backupPath);
            return false;
        }

        try {
            createBackup();

            Path dbPath = PathManager.getDatabasePath();
            Files.copy(backupPath, dbPath, StandardCopyOption.REPLACE_EXISTING);

            System.out.println("Backup wiederhergestellt: " + backupPath);

            SabathonConfig config = configService.getConfig();
            this.eventRepository = new EventRepository(config);

            return true;

        } catch (IOException e) {
            System.err.println("Fehler beim Wiederherstellen des Backups");
            e.printStackTrace();
            return false;
        }
    }

    private LocalDateTime extractTimestampFromFilename(String filename) {
        String timestampPart = filename.replace("sabathon_backup_", "").replace(".db", "");
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

        try {
            return LocalDateTime.parse(timestampPart, formatter);
        } catch (Exception e) {
            return LocalDateTime.now();
        }
    }

    private void cleanOldBackups() {
        try {
            Path backupsPath = PathManager.getBackupsPath();
            List<Path> backups = Files.list(backupsPath)
                    .filter(path -> path.getFileName().toString().startsWith("sabathon_backup_"))
                    .sorted((p1, p2) -> {
                        try {
                            return Files.getLastModifiedTime(p2).compareTo(Files.getLastModifiedTime(p1));
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .toList();

            int maxBackups = 10;

            if (backups.size() <= maxBackups) {
                return;
            }

            for (int i = maxBackups; i < backups.size(); i++) {
                Files.deleteIfExists(backups.get(i));
                System.out.println("Altes Backup gelöscht: " + backups.get(i).getFileName());
            }
        } catch (IOException e) {
            System.err.println("Fehler beim Aufräumen alter Backups");
            e.printStackTrace();
        }
    }
}
