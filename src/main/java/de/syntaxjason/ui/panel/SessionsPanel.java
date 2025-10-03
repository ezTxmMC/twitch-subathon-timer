package de.syntaxjason.ui.panel;

import de.syntaxjason.model.Session;
import de.syntaxjason.model.EventType;
import de.syntaxjason.service.session.ISessionService;
import de.syntaxjason.service.timer.ITimerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class SessionsPanel extends JPanel {
    private final ISessionService sessionService;
    private final ITimerService timerService;
    private JTable sessionsTable;
    private DefaultTableModel tableModel;
    private JPanel statsPanel;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public SessionsPanel(ISessionService sessionService, ITimerService timerService) {
        this.sessionService = sessionService;
        this.timerService = timerService;
        initializeUI();
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(30, 30, 30, 30));

        statsPanel = new JPanel();
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
        statsPanel.setOpaque(false);

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);

        refreshSessions();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 0));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("SESSION VERWALTUNG");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JButton newSessionButton = createModernButton("Neue Session", new Color(80, 150, 80));
        newSessionButton.addActionListener(e -> showNewSessionDialog());

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(newSessionButton, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 20, 0));
        panel.setOpaque(false);

        panel.add(createSessionsTablePanel());

        JScrollPane statsScrollPane = new JScrollPane(statsPanel);
        statsScrollPane.setBorder(null);
        statsScrollPane.setOpaque(false);
        statsScrollPane.getViewport().setOpaque(false);
        panel.add(statsScrollPane);

        return panel;
    }

    private JPanel createSessionsTablePanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JLabel label = new JLabel("ALLE SESSIONS");
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(150, 150, 160));

        String[] columnNames = {"Name", "Start", "Status", "Events", "Minuten"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        sessionsTable = new JTable(tableModel);
        sessionsTable.setBackground(new Color(40, 40, 45));
        sessionsTable.setForeground(Color.WHITE);
        sessionsTable.setSelectionBackground(new Color(100, 65, 165));
        sessionsTable.setSelectionForeground(Color.WHITE);
        sessionsTable.setRowHeight(40);
        sessionsTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        sessionsTable.getTableHeader().setBackground(new Color(35, 35, 40));
        sessionsTable.getTableHeader().setForeground(Color.WHITE);
        sessionsTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        sessionsTable.setGridColor(new Color(60, 60, 65));

        sessionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateStatsPanel();
            }
        });

        JScrollPane scrollPane = new JScrollPane(sessionsTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 65), 2));

        panel.add(label, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private void updateStatsPanel() {
        if (statsPanel == null) {
            return;
        }

        statsPanel.removeAll();

        int selectedRow = sessionsTable.getSelectedRow();

        if (selectedRow < 0) {
            JLabel noSelectionLabel = new JLabel("Keine Session ausgewählt");
            noSelectionLabel.setForeground(new Color(150, 150, 160));
            noSelectionLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            statsPanel.add(noSelectionLabel);
            statsPanel.revalidate();
            statsPanel.repaint();
            return;
        }

        List<Session> sessions = sessionService.getAllSessions();

        if (selectedRow >= sessions.size()) {
            statsPanel.revalidate();
            statsPanel.repaint();
            return;
        }

        Session session = sessions.get(selectedRow);

        JLabel titleLabel = new JLabel("SESSION STATISTIKEN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        statsPanel.add(titleLabel);
        statsPanel.add(Box.createVerticalStrut(20));

        addStatCard("Gesamt Events", String.valueOf(session.getTotalEvents()));
        addStatCard("Gesamt Minuten", session.getTotalMinutesAdded() + " Min");
        addStatCard("Dauer", session.getDurationMinutes() + " Min");

        statsPanel.add(Box.createVerticalStrut(20));

        JLabel eventBreakdownLabel = new JLabel("EVENT BREAKDOWN");
        eventBreakdownLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        eventBreakdownLabel.setForeground(new Color(150, 150, 160));
        eventBreakdownLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        statsPanel.add(eventBreakdownLabel);
        statsPanel.add(Box.createVerticalStrut(10));

        for (EventType type : EventType.values()) {
            int count = session.getEventCounts().getOrDefault(type, 0);
            int minutes = session.getTotalMinutesAddedMap().getOrDefault(type, 0);
            addEventStatRow(type.getDisplayName(), count, minutes);
        }

        statsPanel.revalidate();
        statsPanel.repaint();
    }

    private void addStatCard(String label, String value) {
        JPanel card = new JPanel(new BorderLayout(10, 0));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        card.setOpaque(false);
        card.setBorder(new EmptyBorder(10, 15, 10, 15));

        JLabel labelComponent = new JLabel(label);
        labelComponent.setForeground(new Color(150, 150, 160));
        labelComponent.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JLabel valueComponent = new JLabel(value);
        valueComponent.setForeground(Color.WHITE);
        valueComponent.setFont(new Font("Segoe UI", Font.BOLD, 18));

        card.add(labelComponent, BorderLayout.NORTH);
        card.add(valueComponent, BorderLayout.CENTER);

        statsPanel.add(card);
        statsPanel.add(Box.createVerticalStrut(5));
    }

    private void addEventStatRow(String eventName, int count, int minutes) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        row.setOpaque(false);
        row.setBorder(new EmptyBorder(5, 20, 5, 15));

        JLabel nameLabel = new JLabel(eventName);
        nameLabel.setForeground(new Color(200, 200, 210));
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JLabel statsLabel = new JLabel(String.format("%dx (%d Min)", count, minutes));
        statsLabel.setForeground(new Color(100, 65, 165));
        statsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));

        row.add(nameLabel, BorderLayout.WEST);
        row.add(statsLabel, BorderLayout.EAST);

        statsPanel.add(row);
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        panel.setOpaque(false);

        JButton loadButton = createModernButton("Session Laden", new Color(100, 65, 165));
        loadButton.addActionListener(e -> loadSelectedSession());

        JButton archiveButton = createModernButton("Archivieren", new Color(70, 70, 75));
        archiveButton.addActionListener(e -> archiveSelectedSession());

        JButton deleteButton = createModernButton("Löschen", new Color(200, 80, 80));
        deleteButton.addActionListener(e -> deleteSelectedSession());

        JButton refreshButton = createModernButton("Aktualisieren", new Color(70, 70, 75));
        refreshButton.addActionListener(e -> refreshSessions());

        panel.add(loadButton);
        panel.add(archiveButton);
        panel.add(deleteButton);
        panel.add(refreshButton);

        return panel;
    }
// Fortsetzung von SessionsPanel.java

    private void showNewSessionDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Neue Session erstellen", true);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(400, 200);
        dialog.setLocationRelativeTo(this);

        JPanel formPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JLabel nameLabel = new JLabel("Session Name:");
        JTextField nameField = new JTextField("Session " +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        JLabel minutesLabel = new JLabel("Start-Minuten:");
        JSpinner minutesSpinner = new JSpinner(new SpinnerNumberModel(240, 1, 10000, 10));

        formPanel.add(nameLabel);
        formPanel.add(nameField);
        formPanel.add(minutesLabel);
        formPanel.add(minutesSpinner);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));

        JButton createButton = new JButton("Erstellen");
        createButton.addActionListener(e -> {
            String name = nameField.getText().trim();

            if (name.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Bitte Namen eingeben", "Fehler", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int minutes = (Integer) minutesSpinner.getValue();
            sessionService.startNewSession(name, minutes);
            timerService.reset();

            refreshSessions();
            dialog.dispose();
        });

        JButton cancelButton = new JButton("Abbrechen");
        cancelButton.addActionListener(e -> dialog.dispose());

        buttonPanel.add(createButton);
        buttonPanel.add(cancelButton);

        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void loadSelectedSession() {
        int selectedRow = sessionsTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Bitte Session auswählen", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Session> sessions = sessionService.getAllSessions();
        Session session = sessions.get(selectedRow);

        sessionService.loadSession(session.getId());

        JOptionPane.showMessageDialog(this, "Session geladen!", "Erfolg", JOptionPane.INFORMATION_MESSAGE);
    }

    private void archiveSelectedSession() {
        int selectedRow = sessionsTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Bitte Session auswählen", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        List<Session> sessions = sessionService.getAllSessions();
        Session session = sessions.get(selectedRow);

        sessionService.archiveSession(session.getId());
        refreshSessions();
    }

    private void deleteSelectedSession() {
        int selectedRow = sessionsTable.getSelectedRow();

        if (selectedRow < 0) {
            JOptionPane.showMessageDialog(this, "Bitte Session auswählen", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Möchten Sie diese Session wirklich löschen?",
                "Löschen bestätigen",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        List<Session> sessions = sessionService.getAllSessions();
        Session session = sessions.get(selectedRow);

        sessionService.deleteSession(session.getId());
        refreshSessions();
    }

    public void refreshSessions() {
        if (tableModel == null) {
            return;
        }

        tableModel.setRowCount(0);

        List<Session> sessions = sessionService.getAllSessions();

        for (Session session : sessions) {
            tableModel.addRow(new Object[]{
                    session.getName(),
                    session.getStartTime().format(formatter),
                    session.getStatus().getDisplayName(),
                    session.getTotalEvents(),
                    session.getTotalMinutesAdded()
            });
        }

        updateStatsPanel();
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 45));
        return button;
    }
}
