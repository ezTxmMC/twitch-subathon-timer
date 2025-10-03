package de.syntaxjason.ui.panel;

import de.syntaxjason.model.*;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.service.twitch.ITwitchService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.function.Consumer;

public class ConfigPanel extends JPanel {
    private final IConfigService configService;
    private final ITwitchService twitchService;
    private final IServerService serverService;
    private SabathonConfig config;
    private JTable channelTable;
    private DefaultTableModel channelTableModel;

    public ConfigPanel(IConfigService configService, ITwitchService twitchService, IServerService serverService) {
        this.configService = configService;
        this.twitchService = twitchService;
        this.serverService = serverService;
        this.config = configService.getConfig();
        initializeUI();

        configService.registerConfigReloadListener(this::onConfigReloaded);
    }

    private void onConfigReloaded(SabathonConfig newConfig) {
        SwingUtilities.invokeLater(() -> {
            this.config = newConfig;
            reloadUI();
        });
    }

    private void reloadUI() {
        removeAll();
        initializeUI();
        revalidate();
        repaint();

        System.out.println("ConfigPanel UI neu geladen");
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

        JLabel titleLabel = new JLabel("EINSTELLUNGEN");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        panel.add(titleLabel, BorderLayout.WEST);

        return panel;
    }

    private JPanel createCenterPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        panel.add(createSection("Bot Einstellungen", createBotSettings(config)));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createSection("Channel Verwaltung", createChannelManagement(config)));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createSection("Timer Einstellungen", createTimerSettings(config)));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createSection("Erweiterte Einstellungen", createAdvancedSettings(config)));
        panel.add(Box.createVerticalStrut(20));
        panel.add(createSection("Multiplier Rewards", createMultiplierRewards(config)));

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scrollPane, BorderLayout.CENTER);

        return wrapper;
    }

    private JPanel createSection(String title, JPanel content) {
        JPanel section = new JPanel(new BorderLayout(0, 15));
        section.setOpaque(false);
        section.setBorder(new EmptyBorder(0, 0, 20, 0));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(new Color(150, 150, 160));

        section.add(titleLabel, BorderLayout.NORTH);
        section.add(content, BorderLayout.CENTER);

        return section;
    }

    private JPanel createBotSettings(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createPasswordFieldRow(
                "Bot OAuth Token:",
                config.getBotSettings().getBotAccessToken(),
                value -> {
                    config.getBotSettings().setBotAccessToken(value);
                    saveAndSyncConfig();
                }
        ));

        JLabel infoLabel = new JLabel("<html><i>Hinweis: OAuth Token von https://twitchtokengenerator.com<br>Der Bot verwendet automatisch deinen Channel-Namen</i></html>");
        infoLabel.setForeground(new Color(150, 150, 160));
        infoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        infoLabel.setBorder(new EmptyBorder(10, 0, 0, 0));
        panel.add(infoLabel);

        return panel;
    }

    private JPanel createChannelManagement(SabathonConfig config) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setOpaque(false);

        String[] columnNames = {"Channel Name", "Aktiv"};
        channelTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        channelTable = new JTable(channelTableModel);
        channelTable.setBackground(new Color(40, 40, 45));
        channelTable.setForeground(Color.WHITE);
        channelTable.setSelectionBackground(new Color(100, 65, 165));
        channelTable.setSelectionForeground(Color.WHITE);
        channelTable.setRowHeight(35);
        channelTable.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        channelTable.getTableHeader().setBackground(new Color(50, 50, 55));
        channelTable.getTableHeader().setForeground(Color.WHITE);
        channelTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));

        reloadChannelTable();

        JScrollPane tableScrollPane = new JScrollPane(channelTable);
        tableScrollPane.setPreferredSize(new Dimension(0, 200));
        tableScrollPane.setBorder(BorderFactory.createLineBorder(new Color(70, 70, 75)));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setOpaque(false);

        JButton addButton = createModernButton("Hinzufügen", new Color(70, 180, 70));
        addButton.addActionListener(e -> addChannel());

        JButton toggleButton = createModernButton("Aktivieren/Deaktivieren", new Color(100, 65, 165));
        toggleButton.addActionListener(e -> toggleSelectedChannel());

        JButton removeButton = createModernButton("Löschen", new Color(220, 80, 80));
        removeButton.addActionListener(e -> removeSelectedChannel());

        buttonPanel.add(addButton);
        buttonPanel.add(toggleButton);
        buttonPanel.add(removeButton);

        panel.add(tableScrollPane, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createTimerSettings(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createTimerRow("Follower:", EventType.FOLLOWER, config.getEventMinutes(EventType.FOLLOWER)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createTimerRow("Raid:", EventType.RAID, config.getEventMinutes(EventType.RAID)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createTimerRow("Sub:", EventType.SUB, config.getEventMinutes(EventType.SUB)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createTimerRow("Bits:", EventType.BITS, config.getEventMinutes(EventType.BITS)));
        panel.add(Box.createVerticalStrut(10));
        panel.add(createTimerRow("SubGift:", EventType.SUBGIFT, config.getEventMinutes(EventType.SUBGIFT)));

        return panel;
    }

    private JPanel createAdvancedSettings(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        panel.add(createBitsSettings(config));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createRaidSettings(config));
        panel.add(Box.createVerticalStrut(15));
        panel.add(createFollowerSettings(config));

        return panel;
    }

    private JPanel createBitsSettings(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Bits Einstellungen");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(createSettingsRow("Bits pro Minute:", config.getBitsSettings().getMinimumBits(), value -> {
            config.getBitsSettings().setMinimumBits(value);
            saveAndSyncConfig();
        }));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createSettingsRow("Minuten pro Schwellwert:", config.getBitsSettings().getThreshold(), value -> {
            config.getBitsSettings().setThreshold(value);
            saveAndSyncConfig();
        }));

        return panel;
    }

    private JPanel createRaidSettings(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Raid Einstellungen");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(enabledCheckbox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(createSettingsRow("Viewers pro Minute:", config.getRaidSettings().getViewersPerMinute(), value -> {
            config.getRaidSettings().setViewersPerMinute(value);
            saveAndSyncConfig();
        }));
        panel.add(Box.createVerticalStrut(5));
        panel.add(createSettingsRow("Minuten pro Schwellwert:", config.getRaidSettings().getMinutesPerThreshold(), value -> {
            config.getRaidSettings().setMinutesPerThreshold(value);
            saveAndSyncConfig();
        }));

        return panel;
    }

    private JPanel createFollowerSettings(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        JLabel titleLabel = new JLabel("Follower Einstellungen");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        JCheckBox enabledCheckbox = new JCheckBox("Follower aktiviert");
        enabledCheckbox.setSelected(config.getFollowerSettings().isEnabled());
        enabledCheckbox.setForeground(Color.WHITE);
        enabledCheckbox.setOpaque(false);
        enabledCheckbox.setFocusPainted(false);
        enabledCheckbox.setAlignmentX(Component.LEFT_ALIGNMENT);
        enabledCheckbox.addActionListener(e -> {
            config.getFollowerSettings().setEnabled(enabledCheckbox.isSelected());
            saveAndSyncConfig();
        });

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(enabledCheckbox);

        return panel;
    }
    private JPanel createMultiplierRewards(SabathonConfig config) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);

        for (MultiplierReward reward : config.getMultiplierRewards()) {
            panel.add(createMultiplierRewardPanel(reward));
            panel.add(Box.createVerticalStrut(15));
        }

        return panel;
    }

    private JPanel createMultiplierRewardPanel(MultiplierReward reward) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                new EmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(reward.getName());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(10));

        panel.add(createMultiplierRow("Multiplier:", reward.getMultiplier(), value -> {
            reward.setMultiplier(value);
            saveAndSyncConfig();
        }));
        panel.add(Box.createVerticalStrut(5));

        panel.add(createIntRow("Dauer (Min):", reward.getDurationMinutes(), value -> {
            reward.setDurationMinutes(value);
            saveAndSyncConfig();
        }));
        panel.add(Box.createVerticalStrut(5));

        panel.add(createIntRow("Kosten (Bits):", reward.getCostBits(), value -> {
            reward.setCostBits(value);
            saveAndSyncConfig();
        }));

        return panel;
    }

    private JPanel createTimerRow(String label, EventType eventType, int currentValue) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setPreferredSize(new Dimension(150, 30));

        SpinnerNumberModel model = new SpinnerNumberModel(currentValue, 0, 999, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(new Color(50, 50, 55));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(Color.WHITE);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setCaretColor(Color.WHITE);
        spinner.setPreferredSize(new Dimension(100, 35));

        spinner.addChangeListener(e -> {
            int value = (Integer) spinner.getValue();
            config.setEventMinutes(value, eventType);
            saveAndSyncConfig();
        });

        JLabel unitLabel = new JLabel("Minuten");
        unitLabel.setForeground(new Color(150, 150, 160));
        unitLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(spinner);
        rightPanel.add(unitLabel);

        row.add(nameLabel, BorderLayout.WEST);
        row.add(rightPanel, BorderLayout.CENTER);

        return row;
    }

    private JPanel createSettingsRow(String label, int currentValue, Consumer<Integer> onValueChange) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setPreferredSize(new Dimension(200, 25));

        SpinnerNumberModel model = new SpinnerNumberModel(currentValue, 1, 10000, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(new Color(50, 50, 55));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(Color.WHITE);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setCaretColor(Color.WHITE);
        spinner.setPreferredSize(new Dimension(100, 30));

        spinner.addChangeListener(e -> {
            int value = (Integer) spinner.getValue();
            onValueChange.accept(value);
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(spinner, BorderLayout.CENTER);

        return row;
    }

    private JPanel createMultiplierRow(String label, double currentValue, Consumer<Double> onValueChange) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setPreferredSize(new Dimension(150, 25));

        SpinnerNumberModel model = new SpinnerNumberModel(currentValue, 1.0, 10.0, 0.1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(new Color(50, 50, 55));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(Color.WHITE);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setCaretColor(Color.WHITE);
        spinner.setPreferredSize(new Dimension(100, 30));

        spinner.addChangeListener(e -> {
            double value = (Double) spinner.getValue();
            onValueChange.accept(value);
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(spinner, BorderLayout.CENTER);

        return row;
    }

    private JPanel createIntRow(String label, int currentValue, Consumer<Integer> onValueChange) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        nameLabel.setPreferredSize(new Dimension(150, 25));

        SpinnerNumberModel model = new SpinnerNumberModel(currentValue, 0, 10000, 1);
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setBackground(new Color(50, 50, 55));
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setForeground(Color.WHITE);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setCaretColor(Color.WHITE);
        spinner.setPreferredSize(new Dimension(100, 30));

        spinner.addChangeListener(e -> {
            int value = (Integer) spinner.getValue();
            onValueChange.accept(value);
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(spinner, BorderLayout.CENTER);

        return row;
    }

    private JPanel createPasswordFieldRow(String label, String currentValue, Consumer<String> onValueChange) {
        JPanel row = new JPanel(new BorderLayout(10, 0));
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setForeground(Color.WHITE);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        nameLabel.setPreferredSize(new Dimension(150, 30));

        JPasswordField passwordField = new JPasswordField(currentValue);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        passwordField.setBackground(new Color(50, 50, 55));
        passwordField.setForeground(Color.WHITE);
        passwordField.setCaretColor(Color.WHITE);
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(70, 70, 75), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        passwordField.addActionListener(e -> {
            String value = new String(passwordField.getPassword());
            onValueChange.accept(value);
        });

        row.add(nameLabel, BorderLayout.WEST);
        row.add(passwordField, BorderLayout.CENTER);

        return row;
    }

    private void addChannel() {
        String channelName = JOptionPane.showInputDialog(
                this,
                "Channel Namen eingeben:",
                "Channel hinzufügen",
                JOptionPane.PLAIN_MESSAGE
        );

        if (channelName == null || channelName.trim().isEmpty()) {
            return;
        }

        channelName = channelName.trim().toLowerCase();

        boolean exists = config.getChannels().stream()
                .anyMatch(c -> c.getChannelName().equalsIgnoreCase(channelName));

        if (exists) {
            JOptionPane.showMessageDialog(
                    this,
                    "Channel existiert bereits!",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        config.getChannels().add(new ChannelConfig(channelName, true));
        saveAndSyncConfig();
        reloadChannelTable();

        if (twitchService.isConnected()) {
            twitchService.joinChannel(channelName);
        }
    }

    private void toggleSelectedChannel() {
        int selectedRow = channelTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bitte wähle einen Channel aus!",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        ChannelConfig channel = config.getChannels().get(selectedRow);
        channel.setActive(!channel.isActive());
        saveAndSyncConfig();
        reloadChannelTable();

        if (twitchService.isConnected()) {
            if (channel.isActive()) {
                twitchService.joinChannel(channel.getChannelName());
            } else {
                twitchService.partChannel(channel.getChannelName());
            }
        }
    }

    private void removeSelectedChannel() {
        int selectedRow = channelTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(
                    this,
                    "Bitte wähle einen Channel aus!",
                    "Fehler",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        ChannelConfig channel = config.getChannels().get(selectedRow);

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Channel '" + channel.getChannelName() + "' wirklich löschen?",
                "Bestätigung",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (twitchService.isConnected()) {
            twitchService.partChannel(channel.getChannelName());
        }

        config.getChannels().remove(selectedRow);
        saveAndSyncConfig();
        reloadChannelTable();
    }

    private void reloadChannelTable() {
        channelTableModel.setRowCount(0);

        for (ChannelConfig channel : config.getChannels()) {
            channelTableModel.addRow(new Object[]{
                    channel.getChannelName(),
                    channel.isActive() ? "✓ Aktiv" : "✗ Inaktiv"
            });
        }
    }

    private void saveAndSyncConfig() {
        configService.saveConfig();

        if (serverService.isConnected()) {
            serverService.sendConfigUpdate(configService.getConfig());
            System.out.println("Config an Server gesendet");
        }
    }

    private JButton createModernButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(150, 35));
        return button;
    }
}
