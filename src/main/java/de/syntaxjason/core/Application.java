package de.syntaxjason.core;

import com.formdev.flatlaf.FlatDarculaLaf;
import de.syntaxjason.manager.ISyncManager;
import de.syntaxjason.manager.ITimerManager;
import de.syntaxjason.manager.SyncManager;
import de.syntaxjason.manager.TimerManager;
import de.syntaxjason.service.config.ConfigService;
import de.syntaxjason.service.config.IConfigService;
import de.syntaxjason.service.database.DatabaseService;
import de.syntaxjason.service.database.IDatabaseService;
import de.syntaxjason.service.multiplier.IMultiplierService;
import de.syntaxjason.service.multiplier.MultiplierService;
import de.syntaxjason.service.server.IServerService;
import de.syntaxjason.service.server.ServerService;
import de.syntaxjason.service.session.SessionService;
import de.syntaxjason.service.timer.ITimerService;
import de.syntaxjason.service.timer.TimerService;
import de.syntaxjason.service.twitch.ITwitchService;
import de.syntaxjason.service.twitch.TwitchService;
import de.syntaxjason.ui.MainFrame;
import de.syntaxjason.ui.dialog.StartupDialog;
import de.syntaxjason.util.PathManager;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

public class Application {
    private final IConfigService configService;
    private IDatabaseService databaseService;
    private ITimerService timerService;
    private IMultiplierService multiplierService;
    private SessionService sessionService;
    private IServerService serverService;
    private ITwitchService twitchService;
    private ITimerManager timerManager;
    private ISyncManager syncManager;
    private MainFrame mainFrame;
    private String myChannelName;

    public Application() {
        setupLookAndFeel();

        System.out.println("=== Subathon Timer gestartet ===");
        System.out.println("AppData Pfad: " + PathManager.getAppDataPath());
        System.out.println("Config Pfad: " + PathManager.getConfigPath());
        System.out.println("Datenbank Pfad: " + PathManager.getDatabasePath());
        System.out.println("================================");

        this.configService = new ConfigService();
        configService.loadConfig();
    }

    private void setupLookAndFeel() {
        try {
            FlatDarculaLaf.setup();

            UIManager.put("Button.arc", 10);
            UIManager.put("Component.arc", 10);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("Component.focusWidth", 2);
            UIManager.put("ScrollBar.showButtons", false);
            UIManager.put("ScrollBar.width", 12);

        } catch (Exception e) {
            System.err.println("FlatLaf konnte nicht initialisiert werden");
            e.printStackTrace();
        }
    }

    public void start() {
        showStartupDialog();
    }

    private void showStartupDialog() {
        SwingUtilities.invokeLater(() -> {
            StartupDialog dialog = new StartupDialog();
            dialog.setVisible(true);

            if (!dialog.isConfirmed()) {
                System.exit(0);
                return;
            }

            this.myChannelName = dialog.getChannelName();

            initializeServices();
            startServices();
            launchUI();
        });
    }

    private void initializeServices() {
        this.databaseService = new DatabaseService(configService);
        this.timerService = new TimerService(configService);
        this.sessionService = new SessionService(myChannelName);
        this.sessionService.setTimerService(timerService);
        this.multiplierService = new MultiplierService();
        this.serverService = new ServerService(myChannelName);
        this.timerManager = new TimerManager(timerService, configService, multiplierService, sessionService, serverService);
        this.syncManager = new SyncManager(serverService, configService, timerService, multiplierService, sessionService, timerManager);
        this.twitchService = new TwitchService(configService, timerManager, myChannelName);
    }

    private void startServices() {
        databaseService.initialize();
        sessionService.initialize();
        syncManager.initialize();
        twitchService.connect();
        //timerManager.start();
    }

    private void launchUI() {
        SwingUtilities.invokeLater(() -> {
            mainFrame = new MainFrame(
                    timerService,
                    configService,
                    timerManager,
                    twitchService,
                    multiplierService,
                    databaseService,
                    sessionService,
                    serverService,
                    syncManager,
                    myChannelName
            );
            mainFrame.setVisible(true);
        });
    }

    static void main() {
        Application app = new Application();
        app.start();
    }
}
