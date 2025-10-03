package de.syntaxjason.ui.panel;

import de.syntaxjason.model.BackupInfo;
import de.syntaxjason.service.database.IDatabaseService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BackupPanel extends JPanel {
    private final IDatabaseService databaseService;
    private JTable backupTable;
    private DefaultTableModel tableModel;
    private JLabel totalBackupsLabel;
    private JLabel totalSizeLabel;
    private JLabel lastBackupLabel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    public BackupPanel(IDatabaseService databaseService) {
        this.databaseService = databaseService;
        initializeUI();
        refreshBackups();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("BACKUP VERWALTUNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JPanel statsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statsPanel.setOpaque(false);

        totalBackupsLabel = createInfoLabel("0 Backups");
        totalSizeLabel = createInfoLabel("0 MB");
        lastBackupLabel = createInfoLabel("Kein Backup");

        statsPanel.add(totalBackupsLabel);
        statsPanel.add(createSeparator());
        statsPanel.add(totalSizeLabel);
        statsPanel.add(createSeparator());
        statsPanel.add(lastBackupLabel);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statsPanel, BorderLayout.EAST);

        return panel;
    }

    private JLabel createInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(new Color(150, 150, 160));
        return label;
    }

    private JLabel createSeparator() {
        JLabel separator = new JLabel("•");
        separator.setForeground(new Color(100, 100, 110));
        separator.setFont(new Font("Segoe UI", Font.BOLD, 16));
        return separator;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JLabel label = new JLabel("VERFÜGBARE BACKUPS");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(150, 150, 160));

        String[] columnNames = {"Dateiname", "Datum", "Größe"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        backupTable = new JTable(tableModel);
        backupTable.setBackground(new Color(40, 40, 45));
        backupTable.setForeground(Color.WHITE);
        backupTable.setSelectionBackground(new Color(100, 65, 165));
        backupTable.setSelectionForeground(Color.WHITE);
        backupTable.setRowHeight(40);
        backupTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        backupTable.getTableHeader().setBackground(new Color(35, 35, 40));
        backupTable.getTableHeader().setForeground(Color.WHITE);
        backupTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        backupTable.setGridColor(new Color(60, 60, 65));

        JScrollPane scrollPane = new JScrollPane(backupTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 2));

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JButton createButton = createModernButton("Backup Erstellen", new Color(80, 150, 80));
        createButton.addActionListener(e -> createBackup());

        JButton restoreButton = createModernButton("Backup Wiederherstellen", new Color(100, 65, 165));
        restoreButton.addActionListener(e -> restoreBackup());

        JButton deleteButton = createModernButton("Backup Löschen", new Color(200, 80, 80));
        deleteButton.addActionListener(e -> deleteBackup());

        JButton refreshButton = createModernButton("Aktualisieren", new Color(70, 70, 75));
        refreshButton.addActionListener(e -> refreshBackups());

        panel.add(createButton);
        panel.add(restoreButton);
        panel.add(deleteButton);
        panel.add(refreshButton);

        return panel;
    }

    private void createBackup() {
        databaseService.createBackup();
        refreshBackups();
        JOptionPane.showMessageDialog(
                this,
                "Backup erfolgreich erstellt",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void restoreBackup() {
        int selectedRow = backupTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bitte wählen Sie ein Backup aus",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Möchten Sie dieses Backup wirklich wiederherstellen?\nDie aktuelle Datenbank wird überschrieben!",
                "Backup wiederherstellen",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<BackupInfo> backups = databaseService.getAvailableBackups();
        BackupInfo backup = backups.get(selectedRow);

        boolean success = databaseService.restoreBackup(backup.getPath());

        if (success) {
            JOptionPane.showMessageDialog(
                    this,
                    "Backup erfolgreich wiederhergestellt",
                    "Erfolg",
                    JOptionPane.INFORMATION_MESSAGE
            );
            refreshBackups();
            return;
        }

        JOptionPane.showMessageDialog(
                this,
                "Fehler beim Wiederherstellen des Backups",
                "Fehler",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void deleteBackup() {
        int selectedRow = backupTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bitte wählen Sie ein Backup aus",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Möchten Sie dieses Backup wirklich löschen?",
                "Backup löschen",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<BackupInfo> backups = databaseService.getAvailableBackups();
        BackupInfo backup = backups.get(selectedRow);

        try {
            java.nio.file.Files.deleteIfExists(backup.getPath());
            refreshBackups();
            JOptionPane.showMessageDialog(
                    this,
                    "Backup erfolgreich gelöscht",
                    "Erfolg",
                    JOptionPane.INFORMATION_MESSAGE
            );
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(
                    this,
                    "Fehler beim Löschen des Backups",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    public void refreshBackups() {
        tableModel.setRowCount(0);

        List<BackupInfo> backups = databaseService.getAvailableBackups();

        long totalSize = 0;
        for (BackupInfo backup : backups) {
            tableModel.addRow(new Object[]{
                    backup.getFileName(),
                    backup.getTimestamp().format(formatter),
                    backup.getFormattedSize()
            });
            totalSize += backup.getSizeBytes();
        }

        updateStats(backups, totalSize);
    }

    private void updateStats(List<BackupInfo> backups, long totalSize) {
        totalBackupsLabel.setText(backups.size() + " Backups");
        totalSizeLabel.setText(formatSize(totalSize));

        if (backups.isEmpty()) {
            lastBackupLabel.setText("Kein Backup");
            return;
        }

        BackupInfo latest = backups.get(0);
        lastBackupLabel.setText("Letztes: " + latest.getTimestamp().format(DateTimeFormatter.ofPattern("dd.MM. HH:mm")));
    }

    private String formatSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }

        if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        }

        return String.format("%.2f MB", bytes / (1024.0 * 1024.0));
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(180, 45));
        return button;
    }
}
