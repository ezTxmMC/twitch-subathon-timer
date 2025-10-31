// Session page setup and handlers
// Note: _currentSession is a global variable defined in app.js

function setupSessionPage() {
  console.log("[Session] Setting up session page");

  const createBtn = document.getElementById("create-session-btn");
  const joinBtn = document.getElementById("join-session-btn");
  const joinInput = document.getElementById("join-code-input");
  const leaveBtn = document.getElementById("leave-session-btn");
  const copyBtn = document.getElementById("copy-code-btn");

  console.log("[Session] Buttons found:", {
    createBtn,
    joinBtn,
    leaveBtn,
    copyBtn,
  });

  if (createBtn) {
    createBtn.addEventListener("click", handleCreateSession);
    console.log("[Session] Create button listener attached");
  }

  if (joinBtn) {
    joinBtn.addEventListener("click", () => handleJoinSession(joinInput.value));
    console.log("[Session] Join button listener attached");
  }

  if (leaveBtn) {
    leaveBtn.addEventListener("click", handleLeaveSession);
    console.log("[Session] Leave button listener attached");
  }

  if (copyBtn) {
    copyBtn.addEventListener("click", handleCopyCode);
    console.log("[Session] Copy button listener attached");
  }

  load_currentSession();
}

async function handleCreateSession() {
  alert("CREATE SESSION");
  console.log("[Session] Create session button clicked!");
  try {
    console.log("[Session] Calling api.createSession()");
    const session = await api.createSession(
      "session" + crypto.randomUUID(),
      300
    );
    console.log("[Session] Session created:", session);
    _currentSession = session;
    displaySession(session);
    showNotification(
      "Session erstellt",
      "Session erfolgreich erstellt!",
      "success"
    );
  } catch (error) {
    console.log("[Session] Error creating session:" + error);
    showNotification("Fehler", "Session konnte nicht erstellt werden", "error");
    void error;
  }
}

async function handleJoinSession(code) {
  if (!code) {
    showNotification("Fehler", "Bitte Code eingeben", "error");
    return;
  }

  try {
    const session = await api.joinSession(code);
    _currentSession = session;
    displaySession(session);
    showNotification(
      "Beigetreten",
      "Session erfolgreich beigetreten!",
      "success"
    );
  } catch (error) {
    showNotification(
      "Fehler",
      "Ungültiger Code oder Session nicht gefunden",
      "error"
    );
    void error;
  }
}

function handleLeaveSession() {
  _currentSession = null;
  document.getElementById("no-session").classList.remove("hidden");
  document.getElementById("active-session").classList.add("hidden");
  showNotification(
    "Session verlassen",
    "Du hast die Session verlassen",
    "info"
  );
}

function handleCopyCode() {
  if (!_currentSession) return;

  navigator.clipboard.writeText(_currentSession.code);
  showNotification("Kopiert", "Code in Zwischenablage kopiert!", "success");
}

function load_currentSession() {
  if (_currentSession) {
    displaySession(_currentSession);
  }
}

function displaySession(session) {
  document.getElementById("no-session").classList.add("hidden");
  document.getElementById("active-session").classList.remove("hidden");

  document.getElementById("session-id-text").textContent = session.sessionId;
  document.getElementById("session-code-text").textContent = session.code;
  document.getElementById("session-owner").textContent =
    session.ownerName || "Unknown";
  document.getElementById("session-created").textContent = formatDate(
    session.createdAt
  );
}

// Initialize session page when DOM is ready
document.addEventListener("DOMContentLoaded", setupSessionPage);
