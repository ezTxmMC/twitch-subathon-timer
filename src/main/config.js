const fs = require("fs");
const path = require("path");
const os = require("os");
require("dotenv").config();

class ConfigManager {
  constructor() {
    this.configDir = path.join(os.homedir(), "AppData", "Local", "subathon");
    this.configPath = path.join(this.configDir, "config.json");
    this.config = this.loadConfig();
  }

  loadConfig() {
    if (!fs.existsSync(this.configDir)) {
      fs.mkdirSync(this.configDir, { recursive: true });
    }

    if (fs.existsSync(this.configPath)) {
      try {
        const data = fs.readFileSync(this.configPath, "utf8");
        const config = JSON.parse(data);

        return this.mergeWithEnv(config);
      } catch (error) {
        console.error("[Config] Failed to load config:", error);
        return this.getDefaultConfig();
      }
    }

    const defaultConfig = this.getDefaultConfig();
    this.saveConfig(defaultConfig);
    return defaultConfig;
  }

  mergeWithEnv(config) {
    if (
      !config.twitch.clientId ||
      config.twitch.clientId === "0ymyyv7xen2ontrsk4azjseju0e0z3" ||
      config.twitch.clientId === ""
    ) {
      config.twitch.clientId =
        process.env.TWITCH_CLIENT_ID || config.twitch.clientId;
    }

    if (!config.twitch.clientSecret || config.twitch.clientSecret === "") {
      config.twitch.clientSecret = process.env.TWITCH_CLIENT_SECRET || "";
    }

    return config;
  }

  getDefaultConfig() {
    return {
      server: {
        url: "http://gp01.kernex.host:5020",
        port: 8080,
        autoStart: true,
      },
      twitch: {
        clientId: process.env.TWITCH_CLIENT_ID || "",
        clientSecret: process.env.TWITCH_CLIENT_SECRET || "",
        redirectUri: "http://localhost:17563",
        scopes: ["user:read:email"],
      },
      app: {
        theme: "dark",
        language: "de",
        startMinimized: false,
        closeToTray: true,
      },
      overlay: {
        defaultTheme: "dark",
        defaultPosition: "top-right",
        defaultFontSize: "medium",
        showAlerts: true,
        showWheel: true,
      },
    };
  }

  saveConfig(config) {
    try {
      fs.writeFileSync(
        this.configPath,
        JSON.stringify(config, null, 2),
        "utf8"
      );
    } catch (error) {
      console.error("[Config] Failed to save config:", error);
    }
  }

  get(key) {
    const keys = key.split(".");
    let value = this.config;

    for (const k of keys) {
      if (value && typeof value === "object") {
        value = value[k];
      } else {
        return undefined;
      }
    }

    return value;
  }

  set(key, value) {
    const keys = key.split(".");
    let current = this.config;

    for (let i = 0; i < keys.length - 1; i++) {
      if (!current[keys[i]]) {
        current[keys[i]] = {};
      }
      current = current[keys[i]];
    }

    current[keys[keys.length - 1]] = value;
    this.saveConfig(this.config);
  }

  getAll() {
    return { ...this.config };
  }

  reset() {
    this.config = this.getDefaultConfig();
    this.saveConfig(this.config);
  }
}

module.exports = ConfigManager;
