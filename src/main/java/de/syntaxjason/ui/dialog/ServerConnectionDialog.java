package de.syntaxjason.ui.dialog;

import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.service.server.IServerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class ServerConnectionDialog extends JDialog {
    private final IServerService serverService;
    private final ITimerManager timerManager;
    private JTextField serverUrlField;
    private JTextField sessionIdField;
    private JButton connectButton;
    private JButton cancelButton;
    private JLabel statusLabel;
    private boolean connected = false;

    public ServerConnectionDialog(Frame parent, IServerService serverService, ITimerManager timerManager) {
        super(parent, "Live Session beitreten", true);
        this.serverService = serverService;
        this.timerManager = timerManager;
        initializeDialog();
    }

    private void initializeDialog() {
        setSize(500, 300);
        setLocationRelativeTo(getParent());
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));
        mainPanel.setBackground(new Color(30, 30, 30));

        mainPanel.add(createTopPanel(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createBottomPanel(), BorderLayout.SOUTH);

        setContentPane(mainPanel);

        setupListeners();
    }

    private JPanel createTopPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Live Session");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(Color.WHITE);

        JLabel subtitleLabel = new JLabel("Verbinde dich mit einer laufenden Session");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        subtitleLabel.setForeground(new Color(150, 150, 160));

        JPanel textPanel = new JPanel();
        textPanel.setLayout(new BoxLayout(textPanel, BoxLayout.Y_AXIS));
        textPanel.setOpaque(false);

        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        subtitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        textPanel.add(titleLabel);
        textPanel.add(Box.createVerticalStrut(5));
        textPanel.add(subtitleLabel);

        panel.add(textPanel, BorderLayout.CENTER);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createInputRow("Server URL:", "ws://localhost:8080"));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createInputRow("Session ID:", ""));
        panel.add(Box.createVerticalStrut(15));

        statusLabel = new JLabel("Bereit zum Verbinden");
        statusLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        statusLabel.setForeground(new Color(150, 150, 160));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(statusLabel);

        return panel;
    }

    private JPanel createInputRow(String label, String defaultValue) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setPreferredSize(new Dimension(100, 30));

        JTextField textField = new JTextField(defaultValue);
        textField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        textField.setBackground(new Color(50, 50, 55));
        textField.setForeground(Color.WHITE);
        textField.setCaretColor(Color.WHITE);
        textField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                BorderFactory.createEmptyBorder(8, 12, 8, 12)
        ));

        if (label.contains("Server")) {
            serverUrlField = textField;
        }

        if (label.contains("Session")) {
            sessionIdField = textField;
        }

        row.add(nameLabel, BorderLayout.WEST);
        row.add(textField, BorderLayout.CENTER);

        return row;
    }

    private JPanel createBottomPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        panel.setOpaque(false);

        cancelButton = createButton("Abbrechen", new Color(70, 70, 75));
        cancelButton.addActionListener(e -> {
            connected = false;
            dispose();
        });

        connectButton = createButton("Verbinden", new Color(100, 65, 165));
        connectButton.addActionListener(e -> connectToServer());

        panel.add(cancelButton);
        panel.add(connectButton);

        return panel;
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 13));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(120, 35));
        return button;
    }

    private void setupListeners() {
        serverService.registerConnectionStatusListener(status -> {
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText(status.getDisplayName());

                switch (status) {
                    case CONNECTED -> {
                        statusLabel.setForeground(new Color(100, 200, 100));
                        connected = true;

                        serverService.sendSessionJoin();

                        Timer timer = new Timer(1000, evt -> dispose());
                        timer.setRepeats(false);
                        timer.start();
                    }
                    case CONNECTING -> {
                        statusLabel.setForeground(new Color(255, 180, 80));
                        connectButton.setEnabled(false);
                    }
                    case ERROR -> {
                        statusLabel.setForeground(new Color(220, 80, 80));
                        connectButton.setEnabled(true);
                    }
                    case DISCONNECTED -> {
                        statusLabel.setForeground(new Color(150, 150, 160));
                        connectButton.setEnabled(true);
                    }
                }
            });
        });
    }

    private void connectToServer() {
        String serverUrl = serverUrlField.getText().trim();
        String sessionId = sessionIdField.getText().trim();

        if (serverUrl.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte Server URL eingeben", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (sessionId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Bitte Session ID eingeben", "Fehler", JOptionPane.ERROR_MESSAGE);
            return;
        }

        serverService.connect(serverUrl, sessionId);

        timerManager.setHost(false);
        timerManager.start();
    }

    public boolean isConnected() {
        return connected;
    }
}