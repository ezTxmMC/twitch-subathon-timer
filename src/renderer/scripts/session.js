let sessionLoaded = false;

console.log("[Session] Script loaded");
console.log("[Session] api available at load time:", typeof api, api);

// Session page setup and handlers
// Note: _currentSession is a global variable defined in app.js

// eslint-disable-next-line no-unused-vars
function setupSessionPage() {
  console.log("[Session] Setting up session page");
  console.log("[Session] api available in setupSessionPage:", typeof api, api);

  // Only attach event listeners once
  if (!sessionLoaded) {
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
      joinBtn.addEventListener("click", () =>
        handleJoinSession(joinInput.value)
      );
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

    sessionLoaded = true;
  }

  // Always update UI with current session state
  load_currentSession();
}

async function handleCreateSession() {
  console.log("[Session] Create session button clicked!");
  console.log("[Session] api object:", api);
  console.log(
    "[Session] api.createSession function:",
    typeof api?.createSession
  );

  if (typeof api === "undefined") {
    console.error("[Session] ERROR: api is undefined!");
    showNotification("Fehler", "API nicht verfügbar", "error");
    return;
  }

  if (typeof api.createSession !== "function") {
    console.error("[Session] ERROR: api.createSession is not a function!");
    showNotification("Fehler", "API Methode nicht verfügbar", "error");
    return;
  }

  try {
    console.log("[Session] Calling api.createSession()");
    const sessionName = "Subathon-" + Date.now();
    const session = await api.createSession(sessionName, 300);
    console.log("[Session] Session created:", session);

    // Transform session to expected format
    const transformedSession = {
      sessionId: session.id || session.sessionId,
      code: session.code || session.id || "NO-CODE",
      ownerName: session.ownerName || "You",
      createdAt: session.createdAt || new Date().toISOString(),
      name: session.name || sessionName,
      goalMinutes: session.goalMinutes || 5,
      currentMinutes: session.currentMinutes || 0,
      isActive: session.isActive || false,
    };

    _currentSession = transformedSession;
    displaySession(transformedSession);
    showNotification(
      "Session erstellt",
      "Session erfolgreich erstellt!",
      "success"
    );
  } catch (error) {
    console.error("[Session] Error creating session:", error);
    showNotification(
      "Fehler",
      "Session konnte nicht erstellt werden: " + error.message,
      "error"
    );
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
    console.log("[Session] Joined session:", session);

    // Transform session to expected format
    const transformedSession = {
      sessionId: session.id || session.sessionId,
      code: session.code || code,
      ownerName: session.ownerName || "Unknown",
      createdAt: session.createdAt || new Date().toISOString(),
      name: session.name || "Joined Session",
      goalMinutes: session.goalMinutes || 5,
      currentMinutes: session.currentMinutes || 0,
      isActive: session.isActive || false,
    };

    _currentSession = transformedSession;
    displaySession(transformedSession);
    showNotification(
      "Beigetreten",
      "Session erfolgreich beigetreten!",
      "success"
    );
  } catch (error) {
    console.error("[Session] Error joining session:", error);
    showNotification(
      "Fehler",
      "Ungültiger Code oder Session nicht gefunden: " + error.message,
      "error"
    );
    void error;
  }
}

function handleLeaveSession() {
  _currentSession = null;
  const noSession = document.getElementById("no-session");
  const activeSession = document.getElementById("active-session");

  if (noSession) noSession.classList.remove("hidden");
  if (activeSession) activeSession.classList.add("hidden");

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
  const noSession = document.getElementById("no-session");
  const activeSession = document.getElementById("active-session");
  const sessionId = document.getElementById("session-id-text");
  const sessionCode = document.getElementById("session-code-text");
  const sessionOwner = document.getElementById("session-owner");
  const sessionCreated = document.getElementById("session-created");

  if (noSession) noSession.classList.add("hidden");
  if (activeSession) activeSession.classList.remove("hidden");
  if (sessionId)
    sessionId.textContent = session.sessionId || session.id || "N/A";
  if (sessionCode) sessionCode.textContent = session.code || "N/A";
  if (sessionOwner) sessionOwner.textContent = session.ownerName || "Unknown";
  if (sessionCreated)
    sessionCreated.textContent = formatDate(
      session.createdAt || new Date().toISOString()
    );
}
