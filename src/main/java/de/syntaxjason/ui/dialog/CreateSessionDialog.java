package de.syntaxjason.ui.dialog;

import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.manager.TimerManager;
import de.syntaxjason.model.SabathonConfig;
import de.syntaxjason.model.ServerConnectionStatus;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.server.IServerService;

import javax.swing.*;
import java.awt.*;

public class CreateSessionDialog extends JDialog {
    private final IServerService serverService;
    private final IConfigService configService;
    private final ITimerManager timerManager;
    private JTextField serverUrlField;
    private JTextField sessionNameField;
    private JSpinner initialMinutesSpinner;
    private JButton createButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private boolean created = false;

    public CreateSessionDialog(Frame parent, IServerService serverService, IConfigService configService, ITimerManager timerManager) {
        super(parent, "Live Session erstellen", true);
        this.serverService = serverService;
        this.configService = configService;
        this.timerManager = timerManager;
        initializeDialog();
    }

    private void initializeDialog() {
        setSize(720, 560);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(new Color(30, 30, 30));

        JLabel titleLabel = new JLabel("Neue Live Session");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(40, 30, 520, 30);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Erstelle eine neue Session und lade andere ein");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitleLabel.setForeground(new Color(150, 150, 160));
        subtitleLabel.setBounds(40, 65, 520, 25);
        mainPanel.add(subtitleLabel);

        JLabel serverUrlLabel = new JLabel("Server URL:");
        serverUrlLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverUrlLabel.setForeground(Color.WHITE);
        serverUrlLabel.setBounds(40, 110, 150, 20);
        mainPanel.add(serverUrlLabel);

        serverUrlField = new JTextField("ws://localhost:8080");
        serverUrlField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        serverUrlField.setBackground(new Color(50, 50, 55));
        serverUrlField.setForeground(Color.WHITE);
        serverUrlField.setCaretColor(Color.WHITE);
        serverUrlField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        serverUrlField.setBounds(40, 135, 520, 40);
        mainPanel.add(serverUrlField);

        JLabel sessionNameLabel = new JLabel("Session Name:");
        sessionNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sessionNameLabel.setForeground(Color.WHITE);
        sessionNameLabel.setBounds(40, 190, 150, 20);
        mainPanel.add(sessionNameLabel);

        sessionNameField = new JTextField("Meine Session");
        sessionNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        sessionNameField.setBackground(new Color(50, 50, 55));
        sessionNameField.setForeground(Color.WHITE);
        sessionNameField.setCaretColor(Color.WHITE);
        sessionNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));
        sessionNameField.setBounds(40, 215, 520, 40);
        mainPanel.add(sessionNameField);

        JLabel minutesLabel = new JLabel("Start-Minuten:");
        minutesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        minutesLabel.setForeground(Color.WHITE);
        minutesLabel.setBounds(40, 270, 150, 20);
        mainPanel.add(minutesLabel);

        initialMinutesSpinner = new JSpinner(new SpinnerNumberModel(240, 1, 10000, 10));
        initialMinutesSpinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) initialMinutesSpinner.getEditor();
        editor.getTextField().setBackground(new Color(50, 50, 55));
        editor.getTextField().setForeground(Color.WHITE);
        editor.getTextField().setCaretColor(Color.WHITE);
        initialMinutesSpinner.setBounds(200, 270, 360, 35);
        mainPanel.add(initialMinutesSpinner);

        statusLabel = new JLabel("Bereit zum Erstellen");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(150, 150, 160));
        statusLabel.setBounds(40, 315, 520, 20);
        mainPanel.add(statusLabel);

        JLabel infoLabel = new JLabel("Hinweis: Bot-Credentials werden automatisch mit allen geteilt");
        infoLabel.setForeground(new Color(100, 65, 165));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setBounds(40, 335, 520, 20);
        mainPanel.add(infoLabel);

        cancelButton = new JButton("Abbrechen");
        cancelButton.setBackground(new Color(70, 70, 75));
        cancelButton.setForeground(Color.WHITE);
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.setBounds(250, 365, 140, 40);
        cancelButton.addActionListener(e -> {
            created = false;
            dispose();
        });
        mainPanel.add(cancelButton);

        createButton = new JButton("Session erstellen");
        createButton.setBackground(new Color(100, 65, 165));
        createButton.setForeground(Color.WHITE);
        createButton.setFocusPainted(false);
        createButton.setBorderPainted(false);
        createButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        createButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        createButton.setBounds(400, 365, 160, 40);
        createButton.addActionListener(e -> createSession());
        mainPanel.add(createButton);

        setContentPane(mainPanel);
        setupListeners();
    }

    private void setupListeners() {
        serverService.registerConnectionStatusListener(status -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(status.getDisplayName());

                switch (status) {
                    case CONNECTED -> {
                        statusLabel.setForeground(new Color(100, 200, 100));
                        created = true;

                        Timer timer = new Timer(1000, evt -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    }
                    case CONNECTING -> {
                        statusLabel.setForeground(new Color(255, 180, 80));
                        createButton.setEnabled(false);
                    }
                    case ERROR -> {
                        statusLabel.setForeground(new Color(220, 80, 80));
                        createButton.setEnabled(true);
                    }
                    case DISCONNECTED -> {
                        statusLabel.setForeground(new Color(150, 150, 160));
                        createButton.setEnabled(true);
                    }
                }
            });
        });
    }

    private void createSession() {
        String serverUrl = serverUrlField.getText().trim();
        String sessionName = sessionNameField.getText().trim();
        int initialMinutes = (Integer) initialMinutesSpinner.getValue();

        if (serverUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte Server URL eingeben", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (sessionName.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte Session Namen eingeben", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sessionId = java.util.UUID.randomUUID().toString();
        SabathonConfig config = configService.getConfig();

        serverService.registerConnectionStatusListener(status -> {
            if (status == ServerConnectionStatus.CONNECTED) {
                SwingUtilities.invokeLater(() -> {
                    serverService.createSession(sessionName, initialMinutes, config);

                    timerManager.setHost(true);
                    timerManager.start();

                    System.out.println("Session Create gesendet - Timer als Host gestartet");
                });
            }
        });

        serverService.connect(serverUrl, sessionId);
    }


    public boolean isCreated() {
        return created;
    }
}