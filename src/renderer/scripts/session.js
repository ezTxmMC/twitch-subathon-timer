if (currentPage === 'session') {
    setupSessionPage();
}

let currentSession = null;

function setupSessionPage() {
    const createBtn = document.getElementById('create-session-btn');
    const joinBtn = document.getElementById('join-session-btn');
    const joinInput = document.getElementById('join-code-input');
    const leaveBtn = document.getElementById('leave-session-btn');
    const copyBtn = document.getElementById('copy-code-btn');

    if (createBtn) {
        createBtn.addEventListener('click', handleCreateSession);
    }

    if (joinBtn) {
        joinBtn.addEventListener('click', () => handleJoinSession(joinInput.value));
    }

    if (leaveBtn) {
        leaveBtn.addEventListener('click', handleLeaveSession);
    }

    if (copyBtn) {
        copyBtn.addEventListener('click', handleCopyCode);
    }

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
