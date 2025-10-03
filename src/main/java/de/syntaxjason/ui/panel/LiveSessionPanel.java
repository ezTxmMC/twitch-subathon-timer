package de.syntaxjason.ui.panel;

import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.model.*;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.ui.dialog.CreateSessionDialog;
import de.syntaxjason.ui.dialog.ServerConnectionDialog;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.List;

public class LiveSessionPanel extends JPanel {
    private final IServerService serverService;
    private final IConfigService configService;
    private final ITimerManager timerManager;

    private JLabel connectionStatusLabel;
    private JButton createSessionButton;
    private JButton joinSessionButton;
    private JButton disconnectButton;
    private JTextField obsUrlField;
    private JButton copyUrlButton;
    private JTextField sessionIdField;
    private JButton copySessionIdButton;
    private DefaultListModel<String> participantListModel;

    public LiveSessionPanel(IServerService serverService, IConfigService configService, ITimerManager timerManager) {
        this.serverService = serverService;
        this.configService = configService;
        this.timerManager = timerManager;
        this.participantListModel = new DefaultListModel<>();

        initializeUI();
        setupListeners();
    }

    private void setupListeners() {
        serverService.registerConnectionStatusListener(this::updateConnectionStatus);
        serverService.registerParticipantListener(this::updateParticipants);
        serverService.registerOBSOverlayListener(this::updateOBSUrl);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
        add(createBottomPanel(), BorderLayout.SOUTH);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("LIVE SESSION");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        statusPanel.setOpaque(false);

        JLabel statusIconLabel = new JLabel("●");
        statusIconLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        statusIconLabel.setForeground(new Color(150, 150, 160));

        connectionStatusLabel = new JLabel("Getrennt");
        connectionStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        connectionStatusLabel.setForeground(new Color(150, 150, 160));

        statusPanel.add(statusIconLabel);
        statusPanel.add(connectionStatusLabel);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statusPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel(new BorderLayout(20, 20));
        panel.setOpaque(false);

        JPanel leftPanel = createParticipantsPanel();
        JPanel rightPanel = new JPanel();
        rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
        rightPanel.setOpaque(false);
        rightPanel.setPreferredSize(new Dimension(400, 0));

        rightPanel.add(createSessionInfoPanel());
        rightPanel.add(Box.createVerticalStrut(20));
        rightPanel.add(createOBSPanel());

        panel.add(leftPanel, BorderLayout.CENTER);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createParticipantsPanel() {
        JPanel panel = new JPanel(new BorderLayout(0, 15));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("TEILNEHMER");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(150, 150, 160));

        JLabel countLabel = new JLabel("0 Teilnehmer");
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        countLabel.setForeground(new Color(150, 150, 160));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(countLabel, BorderLayout.EAST);

        JList<String> participantList = new JList<>(participantListModel);
        participantList.setBackground(new Color(40, 40, 45));
        participantList.setForeground(Color.WHITE);
        participantList.setSelectionBackground(new Color(100, 65, 165));
        participantList.setSelectionForeground(Color.WHITE);
        participantList.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        participantList.setBorder(new EmptyBorder(10, 15, 10, 15));
        participantList.setCellRenderer(new ParticipantCellRenderer());

        JScrollPane scrollPane = new JScrollPane(participantList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 75), 1));
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createSessionInfoPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("SESSION ID");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(150, 150, 160));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        sessionIdField = new JTextField();
        sessionIdField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        sessionIdField.setBackground(new Color(50, 50, 55));
        sessionIdField.setForeground(Color.WHITE);
        sessionIdField.setCaretColor(Color.WHITE);
        sessionIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        sessionIdField.setEditable(false);
        sessionIdField.setText("Noch keine Session aktiv");
        sessionIdField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        sessionIdField.setAlignmentX(Component.LEFT_ALIGNMENT);

        copySessionIdButton = createModernButton("Session ID Kopieren", new Color(80, 100, 180));
        copySessionIdButton.setEnabled(false);
        copySessionIdButton.addActionListener(e -> copySessionIdToClipboard());
        copySessionIdButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(sessionIdField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(copySessionIdButton);

        return panel;
    }

    private JPanel createOBSPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("OBS BROWSER SOURCE");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(150, 150, 160));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel instructionLabel = new JLabel("Füge diese URL als Browser Source in OBS ein:");
        instructionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        instructionLabel.setForeground(new Color(150, 150, 160));
        instructionLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        obsUrlField = new JTextField();
        obsUrlField.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        obsUrlField.setBackground(new Color(50, 50, 55));
        obsUrlField.setForeground(Color.WHITE);
        obsUrlField.setCaretColor(Color.WHITE);
        obsUrlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        obsUrlField.setEditable(false);
        obsUrlField.setText("Noch keine Session aktiv");
        obsUrlField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        obsUrlField.setAlignmentX(Component.LEFT_ALIGNMENT);

        copyUrlButton = createModernButton("URL Kopieren", new Color(100, 65, 165));
        copyUrlButton.setEnabled(false);
        copyUrlButton.addActionListener(e -> copyUrlToClipboard());
        copyUrlButton.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel sizeLabel = new JLabel("<html><i>Empfohlene Größe: 1920x200<br>" +
                "1. Kopiere die URL<br>" +
                "2. Öffne OBS Studio<br>" +
                "3. Füge eine neue 'Browser' Quelle hinzu<br>" +
                "4. Füge die URL ein<br>" +
                "5. Setze Breite auf 1920, Höhe auf 200</i></html>");
        sizeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        sizeLabel.setForeground(new Color(120, 120, 130));
        sizeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(instructionLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(obsUrlField);
        panel.add(Box.createVerticalStrut(10));
        panel.add(copyUrlButton);
        panel.add(Box.createVerticalStrut(15));
        panel.add(sizeLabel);

        return panel;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);

        createSessionButton = createModernButton("Session erstellen", new Color(70, 180, 70));
        createSessionButton.addActionListener(e -> openCreateSessionDialog());

        joinSessionButton = createModernButton("Session beitreten", new Color(100, 65, 165));
        joinSessionButton.addActionListener(e -> openJoinSessionDialog());

        disconnectButton = createModernButton("Verbindung trennen", new Color(220, 80, 80));
        disconnectButton.setEnabled(false);
        disconnectButton.addActionListener(e -> disconnect());

        panel.add(createSessionButton);
        panel.add(joinSessionButton);
        panel.add(disconnectButton);

        return panel;
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(180, 40));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void openCreateSessionDialog() {
        CreateSessionDialog dialog = new CreateSessionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                serverService,
                configService,
                timerManager
        );
        dialog.setVisible(true);
    }

    private void openJoinSessionDialog() {
        ServerConnectionDialog dialog = new ServerConnectionDialog(
                (Frame) SwingUtilities.getWindowAncestor(this),
                serverService,
                timerManager
        );
        dialog.setVisible(true);
    }

    private void disconnect() {
        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Möchtest du die Verbindung wirklich trennen?",
                "Verbindung trennen",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm == JOptionPane.YES_OPTION) {
            serverService.disconnect();
        }
    }

    private void copyUrlToClipboard() {
        String url = obsUrlField.getText();

        if (url.isEmpty() || url.equals("Noch keine Session aktiv")) {
            return;
        }

        StringSelection selection = new StringSelection(url);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

        JOptionPane.showMessageDialog(
                this,
                "OBS Overlay URL in die Zwischenablage kopiert!",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void copySessionIdToClipboard() {
        String sessionId = sessionIdField.getText();

        if (sessionId.isEmpty() || sessionId.equals("Noch keine Session aktiv")) {
            return;
        }

        StringSelection selection = new StringSelection(sessionId);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, selection);

        JOptionPane.showMessageDialog(
                this,
                "Session ID in die Zwischenablage kopiert!",
                "Erfolg",
                JOptionPane.INFORMATION_MESSAGE
        );
    }

    private void updateConnectionStatus(ServerConnectionStatus status) {
        SwingUtilities.invokeLater(() -> {
            connectionStatusLabel.setText(status.getDisplayName());

            switch (status) {
                case CONNECTED -> {
                    connectionStatusLabel.setForeground(new Color(100, 200, 100));
                    createSessionButton.setEnabled(false);
                    joinSessionButton.setEnabled(false);
                    disconnectButton.setEnabled(true);

                    String sessionId = serverService.getCurrentSessionId();
                    if (sessionId != null) {
                        sessionIdField.setText(sessionId);
                        copySessionIdButton.setEnabled(true);
                    }
                }
                case CONNECTING -> {
                    connectionStatusLabel.setForeground(new Color(255, 180, 80));
                    createSessionButton.setEnabled(false);
                    joinSessionButton.setEnabled(false);
                    disconnectButton.setEnabled(false);
                }
                case ERROR -> {
                    connectionStatusLabel.setForeground(new Color(220, 80, 80));
                    createSessionButton.setEnabled(true);
                    joinSessionButton.setEnabled(true);
                    disconnectButton.setEnabled(false);
                    resetOBSUrl();
                    resetSessionId();
                }
                case DISCONNECTED -> {
                    connectionStatusLabel.setForeground(new Color(150, 150, 160));
                    createSessionButton.setEnabled(true);
                    joinSessionButton.setEnabled(true);
                    disconnectButton.setEnabled(false);
                    resetOBSUrl();
                    resetSessionId();
                }
            }
        });
    }

    private void updateParticipants(List<ParticipantInfo> participants) {
        SwingUtilities.invokeLater(() -> {
            participantListModel.clear();

            for (ParticipantInfo participant : participants) {
                participantListModel.addElement(participant.getChannelName());
            }
        });
    }

    private void updateOBSUrl(OBSOverlayInfo info) {
        SwingUtilities.invokeLater(() -> {
            if (info != null) {
                obsUrlField.setText(info.getFullUrl());
                copyUrlButton.setEnabled(true);
            } else {
                resetOBSUrl();
            }
        });
    }

    private void resetOBSUrl() {
        obsUrlField.setText("Noch keine Session aktiv");
        if (copyUrlButton != null) {
            copyUrlButton.setEnabled(false);
        }
    }

    private void resetSessionId() {
        sessionIdField.setText("Noch keine Session aktiv");
        if (copySessionIdButton != null) {
            copySessionIdButton.setEnabled(false);
        }
    }

    private static class ParticipantCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                                                      int index, boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            label.setText("●  " + value);
            label.setBorder(new EmptyBorder(8, 5, 8, 5));

            if (!isSelected) {
                label.setBackground(new Color(40, 40, 45));
                label.setForeground(Color.WHITE);
            }

            return label;
        }
    }
}
