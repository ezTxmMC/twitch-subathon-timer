let currentPage = 'dashboard';
let appConfig = null;
let currentSession = null;
let twitchUser = null;

document.addEventListener('DOMContentLoaded', () => {
    setupNavigation();

    electronAPI.on('app-ready', (config) => {
        appConfig = config;
        console.log('[App] Ready');
        loadPage('dashboard');
    });
});

function setupNavigation() {
    const navItems = document.querySelectorAll('.nav-item');

    navItems.forEach(item => {
        item.addEventListener('click', () => {
            const page = item.dataset.page;

            navItems.forEach(i => i.classList.remove('active'));
            item.classList.add('active');

            loadPage(page);
        });
    });
}

async function loadPage(pageName) {
    currentPage = pageName;

    const container = document.getElementById('page-container');

    try {
        const response = await fetch(`pages/${pageName}.html`);
        const html = await response.text();
        container.innerHTML = html;

        await initializePage(pageName);
    } catch (error) {
        console.error(`[App] Failed to load page: ${pageName}`, error);
        container.innerHTML = '<h1>Page not found</h1>';
    }
}

async function initializePage(pageName) {
    switch(pageName) {
        case 'session':
            setupSessionPage();
            break;
        case 'channels':
            setupChannelsPage();
            break;
        case 'twitch':
            setupTwitchPage();
            break;
        case 'timer':
            setupTimerPage();
            break;
        case 'settings':
            setupSettingsPage();
            break;
        case 'events':
            setupEventsPage();
            break;
        case 'alerts':
            setupAlertsPage();
            break;
        case 'chat':
            setupChatPage();
            break;
        case 'app-settings':
            setupAppConfigPage();
            break;
    }
}

function setupSessionPage() {
    const createBtn = document.getElementById('create-session-btn');
    const joinBtn = document.getElementById('join-session-btn');
    const joinInput = document.getElementById('join-code-input');
    const leaveBtn = document.getElementById('leave-session-btn');
    const copyBtn = document.getElementById('copy-code-btn');

    if (createBtn) createBtn.addEventListener('click', handleCreateSession);
    if (joinBtn) joinBtn.addEventListener('click', () => handleJoinSession(joinInput.value));
    if (leaveBtn) leaveBtn.addEventListener('click', handleLeaveSession);
    if (copyBtn) copyBtn.addEventListener('click', handleCopyCode);

    loadCurrentSession();
}

async function handleCreateSession() {
    try {
        const session = await api.createSession();
        currentSession = session;
        displaySession(session);
        showNotification('Session erstellt', 'Session erfolgreich erstellt!', 'success');
    } catch (error) {
        showNotification('Fehler', 'Session konnte nicht erstellt werden', 'error');
    }
}

async function handleJoinSession(code) {
    if (!code) {
        showNotification('Fehler', 'Bitte Code eingeben', 'error');
        return;
    }

    try {
        const session = await api.joinSession(code);
        currentSession = session;
        displaySession(session);
        showNotification('Beigetreten', 'Session erfolgreich beigetreten!', 'success');
    } catch (error) {
        showNotification('Fehler', 'Ungültiger Code oder Session nicht gefunden', 'error');
    }
}

function handleLeaveSession() {
    currentSession = null;
    document.getElementById('no-session').classList.remove('hidden');
    document.getElementById('active-session').classList.add('hidden');
    showNotification('Session verlassen', 'Du hast die Session verlassen', 'info');
}

function handleCopyCode() {
    if (!currentSession) return;

    navigator.clipboard.writeText(currentSession.code);
    showNotification('Kopiert', 'Code in Zwischenablage kopiert!', 'success');
}

function loadCurrentSession() {
    if (currentSession) {
        displaySession(currentSession);
    }
}

function displaySession(session) {
    document.getElementById('no-session').classList.add('hidden');
    document.getElementById('active-session').classList.remove('hidden');

    document.getElementById('session-id-text').textContent = session.sessionId;
    document.getElementById('session-code-text').textContent = session.code;
    document.getElementById('session-owner').textContent = session.ownerName || 'Unknown';
    document.getElementById('session-created').textContent = formatDate(session.createdAt);
}

function setupChannelsPage() {
    loadChannels();
}

async function loadChannels() {
    if (!currentSession) {
        document.getElementById('channels-table-body').innerHTML = `
      <tr><td colspan="5" class="text-center text-muted">Keine aktive Session</td></tr>
    `;
        return;
    }

    try {
        const channels = await api.getChannels(currentSession.sessionId);
        displayChannels(channels);
    } catch (error) {
        console.error('Failed to load channels');
    }
}

function displayChannels(channels) {
    const tbody = document.getElementById('channels-table-body');
    const countBadge = document.getElementById('channel-count');

    if (!channels || channels.length === 0) {
        tbody.innerHTML = `
      <tr><td colspan="5" class="text-center text-muted">Keine Channels verbunden</td></tr>
    `;
        if (countBadge) countBadge.textContent = '0 Channels';
        return;
    }

    if (countBadge) {
        countBadge.textContent = `${channels.length} Channel${channels.length !== 1 ? 's' : ''}`;
    }

    tbody.innerHTML = channels.map(channel => `
    <tr>
      <td>${channel.channelName}</td>
      <td><code>${channel.channelId}</code></td>
      <td>${formatDate(channel.joinedAt)}</td>
      <td><span class="badge badge-success">Active</span></td>
      <td>
        <div class="table-actions">
          <button class="btn btn-small btn-danger" onclick="handleRemoveChannel('${channel.id}')">
            Remove
          </button>
        </div>
      </td>
    </tr>
  `).join('');
}

async function handleRemoveChannel(channelId) {
    if (!currentSession) return;

    if (!confirm('Channel wirklich entfernen?')) return;

    try {
        await api.removeChannel(currentSession.sessionId, channelId);
        await loadChannels();
        showNotification('Channel entfernt', 'Channel wurde entfernt', 'info');
    } catch (error) {
        showNotification('Fehler', 'Channel konnte nicht entfernt werden', 'error');
    }
}

function setupTwitchPage() {
    const loginBtn = document.getElementById('twitch-login-btn');
    const logoutBtn = document.getElementById('twitch-logout-btn');

    if (loginBtn) loginBtn.addEventListener('click', handleTwitchLogin);
    if (logoutBtn) logoutBtn.addEventListener('click', handleTwitchLogout);

    loadTwitchUser();
}

async function handleTwitchLogin() {
    try {
        const result = await electronAPI.twitch.login();

        if (result.success) {
            twitchUser = result.user;
            displayTwitchUser(result.user);
            showNotification('Twitch Login', 'Erfolgreich mit Twitch verbunden!', 'success');
        }
    } catch (error) {
        showNotification('Fehler', 'Twitch Login fehlgeschlagen', 'error');
    }
}

async function handleTwitchLogout() {
    try {
        await electronAPI.twitch.logout();
        twitchUser = null;

        document.getElementById('twitch-not-connected').classList.remove('hidden');
        document.getElementById('twitch-connected').classList.add('hidden');

        showNotification('Twitch Logout', 'Von Twitch getrennt', 'info');
    } catch (error) {
        showNotification('Fehler', 'Logout fehlgeschlagen', 'error');
    }
}

async function loadTwitchUser() {
    try {
        const result = await electronAPI.twitch.getUser();

        if (result.success) {
            twitchUser = result.user;
            displayTwitchUser(result.user);
        }
    } catch (error) {
        console.error('Failed to load Twitch user');
    }
}

function displayTwitchUser(user) {
    document.getElementById('twitch-not-connected').classList.add('hidden');
    document.getElementById('twitch-connected').classList.remove('hidden');

    document.getElementById('twitch-avatar').src = user.profileImageUrl;
    document.getElementById('twitch-username').textContent = user.displayName;
    document.getElementById('twitch-user-id').textContent = user.id;
}

function setupTimerPage() {
    const startBtn = document.getElementById('start-timer-btn');
    const pauseBtn = document.getElementById('pause-timer-btn');
    const resetBtn = document.getElementById('reset-timer-btn');
    const addTimeBtn = document.getElementById('add-time-btn');

    if (startBtn) startBtn.addEventListener('click', handleStartTimer);
    if (pauseBtn) pauseBtn.addEventListener('click', handlePauseTimer);
    if (resetBtn) resetBtn.addEventListener('click', handleResetTimer);
    if (addTimeBtn) addTimeBtn.addEventListener('click', handleAddTime);
}

async function handleStartTimer() {
    if (!currentSession) {
        showNotification('Fehler', 'Keine aktive Session', 'error');
        return;
    }

    try {
        await api.startTimer(currentSession.sessionId);
        showNotification('Timer gestartet', 'Timer läuft', 'success');
    } catch (error) {
        showNotification('Fehler', 'Timer konnte nicht gestartet werden', 'error');
    }
}

async function handlePauseTimer() {
    if (!currentSession) return;

    try {
        await api.pauseTimer(currentSession.sessionId);
        showNotification('Timer pausiert', 'Timer angehalten', 'info');
    } catch (error) {
        showNotification('Fehler', 'Timer konnte nicht pausiert werden', 'error');
    }
}

async function handleResetTimer() {
    if (!currentSession) return;

    if (!confirm('Timer wirklich zurücksetzen?')) return;

    try {
        await api.resetTimer(currentSession.sessionId);
        showNotification('Timer zurückgesetzt', 'Timer auf 0 gesetzt', 'info');
    } catch (error) {
        showNotification('Fehler', 'Timer konnte nicht zurückgesetzt werden', 'error');
    }
}

async function handleAddTime() {
    if (!currentSession) return;

    const seconds = parseInt(document.getElementById('add-time-seconds').value);
    const reason = document.getElementById('add-time-reason').value;

    try {
        await api.addTime(currentSession.sessionId, seconds, reason);
        showNotification('Zeit hinzugefügt', `${seconds}s hinzugefügt`, 'success');
    } catch (error) {
        showNotification('Fehler', 'Zeit konnte nicht hinzugefügt werden', 'error');
    }
}

function setupSettingsPage() {
    const saveTogglesBtn = document.getElementById('save-toggles-btn');
    const saveSettingsBtn = document.getElementById('save-settings-btn');

    if (saveTogglesBtn) saveTogglesBtn.addEventListener('click', handleSaveToggles);
    if (saveSettingsBtn) saveSettingsBtn.addEventListener('click', handleSaveSettings);

    loadSettings();
    loadToggles();
}

async function loadSettings() {
    if (!currentSession) return;

    try {
        const settings = await api.getSettings(currentSession.sessionId);

        document.getElementById('follow-seconds').value = settings.followSeconds;
        document.getElementById('sub-tier1-seconds').value = settings.subTier1Seconds;
        document.getElementById('sub-tier2-seconds').value = settings.subTier2Seconds;
        document.getElementById('sub-tier3-seconds').value = settings.subTier3Seconds;
        document.getElementById('gifted-sub-seconds').value = settings.giftedSubSeconds;
        document.getElementById('bits-per-100').value = settings.bitsPer100;
        document.getElementById('raid-seconds-per-viewer').value = settings.raidSecondsPerViewer;
    } catch (error) {
        console.error('Failed to load settings');
    }
}

async function loadToggles() {
    if (!currentSession) return;

    try {
        const toggles = await api.getToggles(currentSession.sessionId);

        document.getElementById('toggle-follow').checked = toggles.followEnabled;
        document.getElementById('toggle-subscription').checked = toggles.subscriptionEnabled;
        document.getElementById('toggle-gifted-sub').checked = toggles.giftedSubEnabled;
        document.getElementById('toggle-bits').checked = toggles.bitsEnabled;
        document.getElementById('toggle-raid').checked = toggles.raidEnabled;
    } catch (error) {
        console.error('Failed to load toggles');
    }
}

async function handleSaveSettings() {
    if (!currentSession) {
        showNotification('Fehler', 'Keine aktive Session', 'error');
        return;
    }

    const settings = {
        followSeconds: parseInt(document.getElementById('follow-seconds').value),
        subTier1Seconds: parseInt(document.getElementById('sub-tier1-seconds').value),
        subTier2Seconds: parseInt(document.getElementById('sub-tier2-seconds').value),
        subTier3Seconds: parseInt(document.getElementById('sub-tier3-seconds').value),
        giftedSubSeconds: parseInt(document.getElementById('gifted-sub-seconds').value),
        bitsPer100: parseInt(document.getElementById('bits-per-100').value),
        raidSecondsPerViewer: parseInt(document.getElementById('raid-seconds-per-viewer').value)
    };

    try {
        await api.updateSettings(currentSession.sessionId, settings);
        showNotification('Gespeichert', 'Event Settings gespeichert', 'success');
    } catch (error) {
        showNotification('Fehler', 'Speichern fehlgeschlagen', 'error');
    }
}

async function handleSaveToggles() {
    if (!currentSession) {
        showNotification('Fehler', 'Keine aktive Session', 'error');
        return;
    }

    const toggles = {
        followEnabled: document.getElementById('toggle-follow').checked,
        subscriptionEnabled: document.getElementById('toggle-subscription').checked,
        giftedSubEnabled: document.getElementById('toggle-gifted-sub').checked,
        bitsEnabled: document.getElementById('toggle-bits').checked,
        raidEnabled: document.getElementById('toggle-raid').checked
    };

    try {
        await api.updateToggles(currentSession.sessionId, toggles);
        showNotification('Gespeichert', 'Event Toggles gespeichert', 'success');
    } catch (error) {
        showNotification('Fehler', 'Speichern fehlgeschlagen', 'error');
    }
}

function setupEventsPage() {
    const refreshBtn = document.getElementById('refresh-events-btn');
    const clearBtn = document.getElementById('clear-events-btn');

    if (refreshBtn) refreshBtn.addEventListener('click', loadEvents);
    if (clearBtn) clearBtn.addEventListener('click', handleClearEvents);

    loadEvents();
}

async function loadEvents() {
    if (!currentSession) {
        document.getElementById('events-table-body').innerHTML = `
      <tr><td colspan="7" class="text-center text-muted">Keine aktive Session</td></tr>
    `;
        return;
    }

    try {
        const events = await api.getEvents(currentSession.sessionId);
        displayEvents(events);
    } catch (error) {
        console.error('Failed to load events');
    }
}

function displayEvents(events) {
    const tbody = document.getElementById('events-table-body');

    if (!events || events.length === 0) {
        tbody.innerHTML = `
      <tr><td colspan="7" class="text-center text-muted">Keine Events</td></tr>
    `;
        return;
    }

    tbody.innerHTML = events.slice(0, 100).map(event => `
    <tr>
      <td>${formatTimestamp(event.timestamp)}</td>
      <td><span class="badge badge-primary">${formatEventType(event.eventType)}</span></td>
      <td>${event.channelName}</td>
      <td>${event.username || '-'}</td>
      <td>${event.amount || '-'}</td>
      <td>${event.addedSeconds}s</td>
      <td>
        <span class="badge ${event.processed ? 'badge-success' : 'badge-warning'}">
          ${event.processed ? 'Processed' : 'Pending'}
        </span>
      </td>
    </tr>
  `).join('');
}

async function handleClearEvents() {
    if (!currentSession) return;

    if (!confirm('Alle Events wirklich löschen?')) return;

    showNotification('Feature nicht verfügbar', 'Events löschen ist noch nicht implementiert', 'info');
}

function setupAlertsPage() {
    console.log('Alerts page setup - not yet implemented');
}

function setupChatPage() {
    console.log('Chat page setup - not yet implemented');
}

function setupAppConfigPage() {
    const saveBtn = document.getElementById('save-app-config-btn');
    const resetBtn = document.getElementById('reset-app-config-btn');

    if (saveBtn) saveBtn.addEventListener('click', handleSaveAppConfig);
    if (resetBtn) resetBtn.addEventListener('click', handleResetAppConfig);

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
