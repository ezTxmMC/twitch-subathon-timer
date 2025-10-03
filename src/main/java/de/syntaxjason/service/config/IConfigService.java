package de.syntaxjason.service.config;

import de.syntaxjason.model.SabathonConfig;
import java.util.function.Consumer;

public interface IConfigService {
    void loadConfig();
    void saveConfig();
    SabathonConfig getConfig();
    void registerConfigReloadListener(Consumer<SabathonConfig> listener);
}
