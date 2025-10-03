package de.syntaxjason.ui.panel;

import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.model.ServerConnectionStatus;
import de.syntaxjason.model.Session;
import de.syntaxjason.service.multiplier.IMultiplierService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.service.session.ISessionService;
import de.syntaxjason.service.timer.ITimerService;
import de.syntaxjason.service.twitch.ITwitchService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.Duration;

public class DashboardPanel extends JPanel {
    private final ITimerService timerService;
    private final ITimerManager timerManager;
    private final IMultiplierService multiplierService;
    private final ISessionService sessionService;
    private final IServerService serverService;
    private final ITwitchService twitchService;

    private JLabel timerLabel;
    private JButton pauseButton;
    private JLabel sessionInfoLabel;
    private JLabel multiplierLabel;
    private JLabel serverStatusLabel;
    private JLabel twitchStatusLabel;

    public DashboardPanel(ITimerService timerService, ITimerManager timerManager, IMultiplierService multiplierService, ISessionService sessionService, IServerService serverService, ITwitchService twitchService) {
        this.timerService = timerService;
        this.timerManager = timerManager;
        this.multiplierService = multiplierService;
        this.sessionService = sessionService;
        this.serverService = serverService;
        this.twitchService = twitchService;

        initializeUI();
        startTimerUpdateThread();
        setupListeners();
    }

    private void setupListeners() {
        timerManager.registerPauseListener(this::updatePauseState);
        serverService.registerConnectionStatusListener(this::updateServerStatus);
        twitchService.registerConnectionStatusListener(this::updateTwitchStatus);
    }

    private void initializeUI() {
        setLayout(new BorderLayout(20, 20));
        setBackground(new Color(30, 30, 30));
        setBorder(new EmptyBorder(20, 20, 20, 20));

        add(createTopPanel(), BorderLayout.NORTH);
        add(createCenterPanel(), BorderLayout.CENTER);
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("DASHBOARD");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 0));
        statusPanel.setOpaque(false);

        JPanel twitchStatusContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        twitchStatusContainer.setOpaque(false);

        JLabel twitchIconLabel = new JLabel("●");
        twitchIconLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        twitchIconLabel.setForeground(new Color(150, 150, 160));

        twitchStatusLabel = new JLabel("Twitch: Getrennt");
        twitchStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        twitchStatusLabel.setForeground(new Color(150, 150, 160));

        twitchStatusContainer.add(twitchIconLabel);
        twitchStatusContainer.add(twitchStatusLabel);

        JPanel serverStatusContainer = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        serverStatusContainer.setOpaque(false);

        JLabel serverIconLabel = new JLabel("●");
        serverIconLabel.setFont(new Font("Dialog", Font.BOLD, 20));
        serverIconLabel.setForeground(new Color(150, 150, 160));

        serverStatusLabel = new JLabel("Server: Getrennt");
        serverStatusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverStatusLabel.setForeground(new Color(150, 150, 160));

        serverStatusContainer.add(serverIconLabel);
        serverStatusContainer.add(serverStatusLabel);

        statusPanel.add(twitchStatusContainer);
        statusPanel.add(serverStatusContainer);

        panel.add(titleLabel, BorderLayout.WEST);
        panel.add(statusPanel, BorderLayout.EAST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createTimerSection());
        panel.add(Box.createVerticalStrut(20));
        panel.add(createInfoSection());

        return panel;
    }

    private JPanel createTimerSection() {
        JPanel section = new JPanel(new BorderLayout(20, 20));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 250));

        timerLabel = new JLabel("00:00:00");
        timerLabel.setFont(new Font("Segoe UI", Font.BOLD, 80));
        timerLabel.setForeground(Color.WHITE);
        timerLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel timerPanel = new JPanel(new BorderLayout());
        timerPanel.setBackground(new Color(40, 40, 45));
        timerPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 2),
                new EmptyBorder(30, 30, 30, 30)
        ));
        timerPanel.add(timerLabel, BorderLayout.CENTER);

        section.add(timerPanel, BorderLayout.CENTER);
        section.add(createControlButtons(), BorderLayout.SOUTH);

        return section;
    }

    private JPanel createControlButtons() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        panel.setOpaque(false);

        pauseButton = createControlButton("Pause");
        pauseButton.addActionListener(e -> {
            if (timerManager.isPaused()) {
                timerManager.resume();
                pauseButton.setText("Pause");
            } else {
                timerManager.pause();
                pauseButton.setText("Resume");
            }
        });

        JButton resetButton = createControlButton("Reset");
        resetButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(
                    this,
                    "Möchtest du den Timer wirklich zurücksetzen?",
                    "Timer Reset",
                    JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                timerService.reset();
            }
        });

        panel.add(pauseButton);
        panel.add(resetButton);

        return panel;
    }

    private JButton createControlButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(150, 45));
        button.setBackground(new Color(100, 65, 165));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private JPanel createInfoSection() {
        JPanel section = new JPanel(new GridLayout(1, 2, 20, 0));
        section.setOpaque(false);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));

        section.add(createInfoCard("Session Info", createSessionInfoContent()));
        section.add(createInfoCard("Multiplier", createMultiplierContent()));

        return section;
    }

    private JPanel createInfoCard(String title, JPanel content) {
        JPanel card = new JPanel(new BorderLayout(0, 15));
        card.setBackground(new Color(40, 40, 45));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                new EmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(150, 150, 160));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(content, BorderLayout.CENTER);

        return card;
    }

    private JPanel createSessionInfoContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        sessionInfoLabel = new JLabel("Keine aktive Session");
        sessionInfoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sessionInfoLabel.setForeground(Color.WHITE);
        sessionInfoLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(sessionInfoLabel);

        return panel;
    }

    private JPanel createMultiplierContent() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        multiplierLabel = new JLabel("1.0x");
        multiplierLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        multiplierLabel.setForeground(new Color(100, 200, 100));
        multiplierLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel descLabel = new JLabel("Aktueller Multiplikator");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(150, 150, 160));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(multiplierLabel);
        panel.add(Box.createVerticalStrut(5));
        panel.add(descLabel);

        return panel;
    }

    private void startTimerUpdateThread() {
        Thread updateThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(100);
                    SwingUtilities.invokeLater(this::updateDisplay);
                } catch (InterruptedException e) {
                    break;
                }
            }
        });
        updateThread.setDaemon(true);
        updateThread.start();
    }

    private void updateDisplay() {
        Duration remaining = timerService.getRemainingTime();
        long totalSeconds = remaining.getSeconds();
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds);
        timerLabel.setText(timeString);

        updateSessionInfo();
        updateMultiplierInfo();
    }

    private void updateSessionInfo() {
        Session session = sessionService.getCurrentSession();

        if (session == null) {
            sessionInfoLabel.setText("Keine aktive Session");
            return;
        }

        sessionInfoLabel.setText("<html>" +
                "Name: " + session.getName() + "<br>" +
                "Events: " + session.getTotalEvents() + "<br>" +
                "Hinzugefügt: " + session.getTotalMinutesAdded() + " Min" +
                "</html>");
    }

    private void updateMultiplierInfo() {
        double multiplier = multiplierService.getCurrentMultiplier();
        multiplierLabel.setText(String.format("%.1fx", multiplier));

        if (multiplier > 1.0) {
            multiplierLabel.setForeground(new Color(255, 215, 0));
        } else {
            multiplierLabel.setForeground(new Color(100, 200, 100));
        }
    }

    private void updatePauseState(boolean isPaused) {
        SwingUtilities.invokeLater(() -> {
            if (isPaused) {
                pauseButton.setText("Resume");
            } else {
                pauseButton.setText("Pause");
            }
        });
    }

    private void updateTwitchStatus(boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                twitchStatusLabel.setText("Twitch: Verbunden");
                twitchStatusLabel.setForeground(new Color(100, 200, 100));
            } else {
                twitchStatusLabel.setText("Twitch: Getrennt");
                twitchStatusLabel.setForeground(new Color(220, 80, 80));
            }
        });
    }

    private void updateServerStatus(ServerConnectionStatus status) {
        SwingUtilities.invokeLater(() -> {
            serverStatusLabel.setText("Server: " + status.getDisplayName());

            switch (status) {
                case CONNECTED -> serverStatusLabel.setForeground(new Color(100, 200, 100));
                case CONNECTING -> serverStatusLabel.setForeground(new Color(255, 180, 80));
                case ERROR -> serverStatusLabel.setForeground(new Color(220, 80, 80));
                case DISCONNECTED -> serverStatusLabel.setForeground(new Color(150, 150, 160));
            }
        });
    }
}
