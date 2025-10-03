package de.syntaxjason.ui;

import de.syntaxjason.manager.ISyncManager;
import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.manager.SyncManager;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.database.IDatabaseService;
import de.syntaxjason.service.multiplier.IMultiplierService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.service.session.ISessionService;
import de.syntaxjason.service.timer.ITimerService;
import de.syntaxjason.service.twitch.ITwitchService;
import de.syntaxjason.ui.components.NavigationBar;
import de.syntaxjason.ui.panel.*;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    private final ITimerService timerService;
    private final IConfigService configService;
    private final ITimerManager timerManager;
    private final ITwitchService twitchService;
    private final IMultiplierService multiplierService;
    private final IDatabaseService databaseService;
    private final ISessionService sessionService;
    private final IServerService serverService;
    private final ISyncManager syncManager;
    private final String myChannelName;

    private NavigationBar navigationBar;
    private JPanel contentPanel;
    private CardLayout cardLayout;

    private DashboardPanel dashboardPanel;
    private ConfigPanel configPanel;
    private StatsPanel statsPanel;
    private BackupPanel backupPanel;
    private SessionsPanel sessionsPanel;
    private LiveSessionPanel liveSessionPanel;

    public MainFrame(ITimerService timerService, IConfigService configService, ITimerManager timerManager, ITwitchService twitchService, IMultiplierService multiplierService, IDatabaseService databaseService, ISessionService sessionService, IServerService serverService, ISyncManager syncManager, String myChannelName) {
        this.timerService = timerService;
        this.configService = configService;
        this.timerManager = timerManager;
        this.twitchService = twitchService;
        this.multiplierService = multiplierService;
        this.databaseService = databaseService;
        this.sessionService = sessionService;
        this.serverService = serverService;
        this.syncManager = syncManager;
        this.myChannelName = myChannelName;

        initializeFrame();
        createComponents();
        setupLayout();
        setupListeners();
    }

    private void initializeFrame() {
        setTitle("Sabathon Timer - " + myChannelName);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        int width = configService.getConfig().getUiSettings().getWindowWidth();
        int height = configService.getConfig().getUiSettings().getWindowHeight();
        setSize(width, height);

        setLocationRelativeTo(null);

        if (configService.getConfig().getUiSettings().isAlwaysOnTop()) {
            setAlwaysOnTop(true);
        }
    }

    private void createComponents() {
        navigationBar = new NavigationBar();

        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(new Color(30, 30, 30));

        dashboardPanel = new DashboardPanel(timerService, timerManager, multiplierService, sessionService, serverService, twitchService);
        configPanel = new ConfigPanel(configService, twitchService, serverService);
        statsPanel = new StatsPanel(sessionService);
        backupPanel = new BackupPanel(databaseService);
        sessionsPanel = new SessionsPanel(sessionService, timerService);
        liveSessionPanel = new LiveSessionPanel(serverService, configService, timerManager);

        contentPanel.add(dashboardPanel, "dashboard");
        contentPanel.add(configPanel, "config");
        contentPanel.add(statsPanel, "stats");
        contentPanel.add(sessionsPanel, "sessions");
        contentPanel.add(backupPanel, "backups");
        contentPanel.add(liveSessionPanel, "live");
        contentPanel.add(createInfoPanel(), "info");
    }

    private void setupLayout() {
        setLayout(new BorderLayout());
        add(navigationBar, BorderLayout.WEST);
        add(contentPanel, BorderLayout.CENTER);
    }

    private void setupListeners() {
        navigationBar.addNavigationListener(view -> {
            cardLayout.show(contentPanel, view);

            if ("backups".equals(view)) {
                backupPanel.refreshBackups();
            }

            if ("sessions".equals(view)) {
                sessionsPanel.refreshSessions();
            }

            if ("stats".equals(view)) {
                statsPanel.refreshStats();
            }
        });
    }

    private JPanel createInfoPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(new Color(30, 30, 30));
        panel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        JPanel contentBox = new JPanel();
        contentBox.setLayout(new BoxLayout(contentBox, BoxLayout.Y_AXIS));
        contentBox.setBackground(new Color(40, 40, 45));
        contentBox.setBorder(BorderFactory.createEmptyBorder(40, 40, 40, 40));

        JLabel titleLabel = new JLabel("Sabathon Timer");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(new Color(100, 65, 165));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel versionLabel = new JLabel("Version 1.0.0");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        versionLabel.setForeground(new Color(150, 150, 160));
        versionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel channelLabel = new JLabel("Channel: " + myChannelName);
        channelLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        channelLabel.setForeground(Color.WHITE);
        channelLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JTextArea descriptionArea = new JTextArea(
                "Sabathon Timer ist ein Twitch-Timer für Subathons und Charity-Streams.\n\n" +
                        "Features:\n" +
                        "• Multi-Channel Support\n" +
                        "• Live Session Synchronisation\n" +
                        "• Event-basiertes Zeit-Management\n" +
                        "• Multiplier System\n" +
                        "• Session Verwaltung\n" +
                        "• Automatische Backups\n" +
                        "• Detaillierte Statistiken"
        );
        descriptionArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionArea.setForeground(new Color(200, 200, 210));
        descriptionArea.setBackground(new Color(40, 40, 45));
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        descriptionArea.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel copyrightLabel = new JLabel("© 2025 - Entwickelt von SyntaxJason");
        copyrightLabel.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        copyrightLabel.setForeground(new Color(120, 120, 130));
        copyrightLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        contentBox.add(titleLabel);
        contentBox.add(Box.createVerticalStrut(10));
        contentBox.add(versionLabel);
        contentBox.add(Box.createVerticalStrut(30));
        contentBox.add(channelLabel);
        contentBox.add(Box.createVerticalStrut(20));
        contentBox.add(descriptionArea);
        contentBox.add(Box.createVerticalStrut(20));
        contentBox.add(copyrightLabel);

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(contentBox);

        panel.add(wrapper, BorderLayout.CENTER);

        return panel;
    }
}
