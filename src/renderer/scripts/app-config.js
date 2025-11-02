let appConfigLoaded = false;

// App Config page setup and handlers
// eslint-disable-next-line no-unused-vars
function setupAppConfigPage() {
  // Only attach listeners once
  if (!appConfigLoaded) {
    const saveBtn = document.getElementById("save-app-config-btn");
    const resetBtn = document.getElementById("reset-app-config-btn");

    if (saveBtn) {
      saveBtn.addEventListener("click", handleSaveAppConfig);
    }

    if (resetBtn) {
      resetBtn.addEventListener("click", handleResetAppConfig);
    }

    appConfigLoaded = true;
  }

  // Always reload config when entering the page
  loadAppConfig();
}

async function loadAppConfig() {
  try {
    const config = await electronAPI.config.getAll();
    _appConfig = config;
    if (config?.server?.url && typeof api?.setBaseURL === "function") {
      api.setBaseURL(config.server.url);
    }

    // Add null checks for all elements
    const serverUrl = document.getElementById("config-server-url");
    const serverPort = document.getElementById("config-server-port");
    const serverAutoStart = document.getElementById("config-server-auto-start");
    const twitchClientId = document.getElementById("config-twitch-client-id");
    const twitchRedirectUri = document.getElementById(
      "config-twitch-redirect-uri"
    );
    const appTheme = document.getElementById("config-app-theme");
    const appLanguage = document.getElementById("config-app-language");
    const appStartMinimized = document.getElementById(
      "config-app-start-minimized"
    );
    const appCloseToTray = document.getElementById("config-app-close-to-tray");
    const overlayTheme = document.getElementById("config-overlay-theme");
    const overlayPosition = document.getElementById("config-overlay-position");
    const overlayFontSize = document.getElementById("config-overlay-font-size");
    const overlayShowAlerts = document.getElementById(
      "config-overlay-show-alerts"
    );
    const overlayShowWheel = document.getElementById(
      "config-overlay-show-wheel"
    );

    if (serverUrl) serverUrl.value = config.server.url;
    if (serverPort) serverPort.value = config.server.port;
    if (serverAutoStart) serverAutoStart.checked = config.server.autoStart;
    if (twitchClientId) twitchClientId.value = config.twitch.clientId;
    if (twitchRedirectUri) twitchRedirectUri.value = config.twitch.redirectUri;
    if (appTheme) appTheme.value = config.app.theme;
    if (appLanguage) appLanguage.value = config.app.language;
    if (appStartMinimized)
      appStartMinimized.checked = config.app.startMinimized;
    if (appCloseToTray) appCloseToTray.checked = config.app.closeToTray;
    if (overlayTheme) overlayTheme.value = config.overlay.defaultTheme;
    if (overlayPosition) overlayPosition.value = config.overlay.defaultPosition;
    if (overlayFontSize) overlayFontSize.value = config.overlay.defaultFontSize;
    if (overlayShowAlerts)
      overlayShowAlerts.checked = config.overlay.showAlerts;
    if (overlayShowWheel) overlayShowWheel.checked = config.overlay.showWheel;
  } catch (error) {
    console.error("Failed to load config", error);
    void error;
  }
}

async function handleSaveAppConfig() {
  try {
    await electronAPI.config.set(
      "server.url",
      document.getElementById("config-server-url").value
    );
    await electronAPI.config.set(
      "server.port",
      parseInt(document.getElementById("config-server-port").value)
    );
    await electronAPI.config.set(
      "server.autoStart",
      document.getElementById("config-server-auto-start").checked
    );

    await electronAPI.config.set(
      "twitch.clientId",
      document.getElementById("config-twitch-client-id").value
    );
    await electronAPI.config.set(
      "twitch.redirectUri",
      document.getElementById("config-twitch-redirect-uri").value
    );

    await electronAPI.config.set(
      "app.theme",
      document.getElementById("config-app-theme").value
    );
    await electronAPI.config.set(
      "app.language",
      document.getElementById("config-app-language").value
    );
    await electronAPI.config.set(
      "app.startMinimized",
      document.getElementById("config-app-start-minimized").checked
    );
    await electronAPI.config.set(
      "app.closeToTray",
      document.getElementById("config-app-close-to-tray").checked
    );

    await electronAPI.config.set(
      "overlay.defaultTheme",
      document.getElementById("config-overlay-theme").value
    );
    await electronAPI.config.set(
      "overlay.defaultPosition",
      document.getElementById("config-overlay-position").value
    );
    await electronAPI.config.set(
      "overlay.defaultFontSize",
      document.getElementById("config-overlay-font-size").value
    );
    await electronAPI.config.set(
      "overlay.showAlerts",
      document.getElementById("config-overlay-show-alerts").checked
    );
    await electronAPI.config.set(
      "overlay.showWheel",
      document.getElementById("config-overlay-show-wheel").checked
    );

    showNotification("Gespeichert", "App Konfiguration gespeichert", "success");

    const updatedConfig = await electronAPI.config.getAll();
    _appConfig = updatedConfig;
    if (updatedConfig?.server?.url && typeof api?.setBaseURL === "function") {
      api.setBaseURL(updatedConfig.server.url);
    }
    if (typeof _currentPage !== "undefined" && _currentPage === "overlay") {
      if (typeof setupOverlayPage === "function") {
        await setupOverlayPage();
      }
    }
  } catch (error) {
    showNotification("Fehler", "Speichern fehlgeschlagen", "error");
    void error;
  }
}

async function handleResetAppConfig() {
  if (!confirm("Konfiguration wirklich zurücksetzen?")) return;

  try {
    await electronAPI.config.reset();
    await loadAppConfig();
    if (typeof _currentPage !== "undefined" && _currentPage === "overlay") {
      if (typeof setupOverlayPage === "function") {
        await setupOverlayPage();
      }
    }
    showNotification(
      "Zurückgesetzt",
      "Konfiguration auf Standard zurückgesetzt",
      "info"
    );
  } catch (error) {
    showNotification("Fehler", "Reset fehlgeschlagen", "error");
    void error;
  }
}
