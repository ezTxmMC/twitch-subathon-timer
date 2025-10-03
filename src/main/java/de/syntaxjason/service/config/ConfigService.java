package de.syntaxjason.service.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.syntaxjason.model.SabathonConfig;
import de.syntaxjason.util.LocalDateTimeAdapter;
import de.syntaxjason.util.PathManager;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ConfigService implements IConfigService {
    private SabathonConfig config;
    private final Gson gson;
    private final String configPath;
    private final List<Consumer<SabathonConfig>> reloadListeners;

    public ConfigService() {
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .setPrettyPrinting()
                .create();
        this.configPath = PathManager.getConfigPath().toString();
        this.reloadListeners = new ArrayList<>();
    }

    @Override
    public void loadConfig() {
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            config = new SabathonConfig();
            saveConfig();
            return;
        }

        try (FileReader reader = new FileReader(configFile)) {
            config = gson.fromJson(reader, SabathonConfig.class);

            if (config == null) {
                config = new SabathonConfig();
            }

        } catch (Exception e) {
            System.err.println("Fehler beim Laden der Config: " + e.getMessage());
            config = new SabathonConfig();
        }
    }

    @Override
    public void saveConfig() {
        try {
            File configFile = new File(configPath);
            File parentDir = configFile.getParentFile();

            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }

            try (FileWriter writer = new FileWriter(configFile)) {
                gson.toJson(config, writer);
            }

            notifyReloadListeners();

        } catch (Exception e) {
            System.err.println("Fehler beim Speichern der Config: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public SabathonConfig getConfig() {
        if (config == null) {
            loadConfig();
        }
        return config;
    }

    @Override
    public void registerConfigReloadListener(Consumer<SabathonConfig> listener) {
        reloadListeners.add(listener);
    }

    private void notifyReloadListeners() {
        for (Consumer<SabathonConfig> listener : reloadListeners) {
            listener.accept(config);
        }
    }
}
