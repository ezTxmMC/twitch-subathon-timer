if (currentPage === 'app-settings') {
    setupAppConfigPage();
}

function setupAppConfigPage() {
    const saveBtn = document.getElementById('save-app-config-btn');
    const resetBtn = document.getElementById('reset-app-config-btn');

    if (saveBtn) {
        saveBtn.addEventListener('click', handleSaveAppConfig);
    }

    if (resetBtn) {
        resetBtn.addEventListener('click', handleResetAppConfig);
    }

    loadAppConfig();
}

async function loadAppConfig() {
    try {
        const config = await electronAPI.config.getAll();

        document.getElementById('config-server-url').value = config.server.url;
        document.getElementById('config-server-port').value = config.server.port;
        document.getElementById('config-server-auto-start').checked = config.server.autoStart;

        document.getElementById('config-twitch-client-id').value = config.twitch.clientId;
        document.getElementById('config-twitch-redirect-uri').value = config.twitch.redirectUri;

        document.getElementById('config-app-theme').value = config.app.theme;
        document.getElementById('config-app-language').value = config.app.language;
        document.getElementById('config-app-start-minimized').checked = config.app.startMinimized;
        document.getElementById('config-app-close-to-tray').checked = config.app.closeToTray;

        document.getElementById('config-overlay-theme').value = config.overlay.defaultTheme;
        document.getElementById('config-overlay-position').value = config.overlay.defaultPosition;
        document.getElementById('config-overlay-font-size').value = config.overlay.defaultFontSize;
        document.getElementById('config-overlay-show-alerts').checked = config.overlay.showAlerts;
        document.getElementById('config-overlay-show-wheel').checked = config.overlay.showWheel;
    } catch (error) {
        console.error('Failed to load config');
    }
}

async function handleSaveAppConfig() {
    try {
        await electronAPI.config.set('server.url', document.getElementById('config-server-url').value);
        await electronAPI.config.set('server.port', parseInt(document.getElementById('config-server-port').value));
        await electronAPI.config.set('server.autoStart', document.getElementById('config-server-auto-start').checked);

        await electronAPI.config.set('twitch.clientId', document.getElementById('config-twitch-client-id').value);
        await electronAPI.config.set('twitch.redirectUri', document.getElementById('config-twitch-redirect-uri').value);

        await electronAPI.config.set('app.theme', document.getElementById('config-app-theme').value);
        await electronAPI.config.set('app.language', document.getElementById('config-app-language').value);
        await electronAPI.config.set('app.startMinimized', document.getElementById('config-app-start-minimized').checked);
        await electronAPI.config.set('app.closeToTray', document.getElementById('config-app-close-to-tray').checked);

        await electronAPI.config.set('overlay.defaultTheme', document.getElementById('config-overlay-theme').value);
        await electronAPI.config.set('overlay.defaultPosition', document.getElementById('config-overlay-position').value);
        await electronAPI.config.set('overlay.defaultFontSize', document.getElementById('config-overlay-font-size').value);
        await electronAPI.config.set('overlay.showAlerts', document.getElementById('config-overlay-show-alerts').checked);
        await electronAPI.config.set('overlay.showWheel', document.getElementById('config-overlay-show-wheel').checked);

        showNotification('Gespeichert', 'App Konfiguration gespeichert', 'success');
    } catch (error) {
        showNotification('Fehler', 'Speichern fehlgeschlagen', 'error');
    }
}

async function handleResetAppConfig() {
    if (!confirm('Konfiguration wirklich zurücksetzen?')) return;

    try {
        await electronAPI.config.reset();
        await loadAppConfig();
        showNotification('Zurückgesetzt', 'Konfiguration auf Standard zurückgesetzt', 'info');
    } catch (error) {
        showNotification('Fehler', 'Reset fehlgeschlagen', 'error');
    }
}
