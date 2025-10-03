package de.syntaxjason.ui.dialog;

import javax.swing.*;
import java.awt.*;

public class StartupDialog extends JDialog {
    private JTextField channelNameField;
    private boolean confirmed = false;

    public StartupDialog() {
        initializeDialog();
    }

    private void initializeDialog() {
        setTitle("Sabathon Timer - Channel Name");
        setModal(true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(740, 500);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(null);
        mainPanel.setBackground(new Color(30, 30, 30));

        JLabel titleLabel = new JLabel("Willkommen zu Sabathon Timer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(50, 40, 520, 30);
        mainPanel.add(titleLabel);

        JLabel subtitleLabel = new JLabel("Bitte gib deinen Twitch Channel Namen ein");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        subtitleLabel.setForeground(new Color(150, 150, 160));
        subtitleLabel.setBounds(50, 75, 520, 25);
        mainPanel.add(subtitleLabel);

        JLabel label = new JLabel("Channel Name:");
        label.setForeground(Color.WHITE);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        label.setBounds(50, 130, 520, 20);
        mainPanel.add(label);

        channelNameField = new JTextField();
        channelNameField.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        channelNameField.setBackground(new Color(50, 50, 55));
        channelNameField.setForeground(Color.WHITE);
        channelNameField.setCaretColor(Color.WHITE);
        channelNameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 65, 165), 2),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        channelNameField.setBounds(50, 160, 520, 60);
        channelNameField.addActionListener(e -> confirmAndClose());
        mainPanel.add(channelNameField);

        JButton cancelButton = createButton("Abbrechen", new Color(70, 70, 75));
        cancelButton.setBounds(350, 260, 140, 45);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });
        mainPanel.add(cancelButton);

        JButton confirmButton = createButton("Bestätigen", new Color(100, 65, 165));
        confirmButton.setBounds(500, 260, 140, 45);
        confirmButton.addActionListener(e -> confirmAndClose());
        mainPanel.add(confirmButton);

        setContentPane(mainPanel);

        SwingUtilities.invokeLater(() -> channelNameField.requestFocusInWindow());
    }

    private JButton createButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 14));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void confirmAndClose() {
        String channelName = channelNameField.getText().trim();

        if (channelName.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bitte gib einen Channel Namen ein",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
            channelNameField.requestFocus();
            return;
        }

        confirmed = true;
        dispose();
    }

    public String getChannelName() {
        return channelNameField.getText().trim();
    }

    public boolean isConfirmed() {
        return confirmed;
    }
}
