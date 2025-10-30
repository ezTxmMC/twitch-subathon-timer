const fs = require('fs');
const path = require('path');
const { app } = require('electron');

class ConfigManager {
    constructor() {
        this.configDir = path.join(app.getPath('appData'), 'subathon');
        this.configPath = path.join(this.configDir, 'config.json');
        this.config = this.loadConfig();
    }

    getDefaultConfig() {
        return {
            server: {
                url: 'http://localhost:8080',
                autoStart: true,
                port: 8080
            },
            twitch: {
                clientId: '0ymyyv7xen2ontrsk4azjseju0e0z3',
                redirectUri: 'http://localhost:17563',
                scopes: [
                    'moderator:read:followers',
                    'channel:read:subscriptions',
                    'channel:read:redemptions',
                    'bits:read'
                ]
            },
            app: {
                theme: 'dark',
                language: 'de',
                startMinimized: false,
                closeToTray: true
            },
            overlay: {
                defaultTheme: 'dark',
                defaultPosition: 'top-right',
                defaultFontSize: 'medium',
                showAlerts: true,
                showWheel: true
            }
        };
    }

    loadConfig() {
        try {
            if (!fs.existsSync(this.configDir)) {
                fs.mkdirSync(this.configDir, { recursive: true });
            }

            if (!fs.existsSync(this.configPath)) {
                const defaultConfig = this.getDefaultConfig();
                this.saveConfig(defaultConfig);
                return defaultConfig;
            }

            const data = fs.readFileSync(this.configPath, 'utf8');
            const config = JSON.parse(data);
            const defaultConfig = this.getDefaultConfig();
            return this.deepMerge(defaultConfig, config);
        } catch (error) {
            console.error('[Config] Error loading:', error);
            return this.getDefaultConfig();
        }
    }

    saveConfig(config = this.config) {
        try {
            fs.writeFileSync(this.configPath, JSON.stringify(config, null, 2), 'utf8');
            this.config = config;
            return true;
        } catch (error) {
            console.error('[Config] Error saving:', error);
            return false;
        }
    }

    get(key) {
        const keys = key.split('.');
        let value = this.config;

        for (const k of keys) {
            if (value && typeof value === 'object' && k in value) {
                value = value[k];
            } else {
                return undefined;
            }
        }

        return value;
    }

    set(key, value) {
        const keys = key.split('.');
        let current = this.config;

        for (let i = 0; i < keys.length - 1; i++) {
            const k = keys[i];
            if (!(k in current)) {
                current[k] = {};
            }
            current = current[k];
        }

        current[keys[keys.length - 1]] = value;
        this.saveConfig();
    }

    getAll() {
        return { ...this.config };
    }

    reset() {
        this.config = this.getDefaultConfig();
        this.saveConfig();
    }

    deepMerge(target, source) {
        const output = { ...target };

        if (this.isObject(target) && this.isObject(source)) {
            Object.keys(source).forEach(key => {
                if (this.isObject(source[key])) {
                    if (!(key in target)) {
                        output[key] = source[key];
                    } else {
                        output[key] = this.deepMerge(target[key], source[key]);
                    }
                } else {
                    output[key] = source[key];
                }
            });
        }

        return output;
    }

    isObject(item) {
        return item && typeof item === 'object' && !Array.isArray(item);
    }
}

module.exports = ConfigManager;
