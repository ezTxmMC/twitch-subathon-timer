let sessionTimerInterval = null; // eslint-disable-line no-unused-vars

console.log("[Session] Script loaded");
console.log("[Session] api available at load time:", typeof api, api);

// Session page setup and handlers
// Note: _currentSession is a global variable defined in app.js

// eslint-disable-next-line no-unused-vars
function setupSessionPage() {
  console.log("[Session] Setting up session page");
  console.log("[Session] api available in setupSessionPage:", typeof api, api);

  // Always update UI first to make buttons visible
  updateSessionView();

  // Start timer updates if not already running
  if (!sessionTimerInterval) {
    startSessionTimerUpdates();
  }

  // Always re-attach listeners because DOM elements are recreated
  // when view switches between no-session and active-session
  attachSessionEventListeners();
}

function attachSessionEventListeners() {
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

  // Remove old listeners by cloning (this prevents duplicate listeners)
  if (createBtn) {
    const newCreateBtn = createBtn.cloneNode(true);
    createBtn.parentNode.replaceChild(newCreateBtn, createBtn);
    newCreateBtn.addEventListener("click", handleCreateSession);
    console.log("[Session] Create button listener attached");
  }

  if (joinBtn && joinInput) {
    const newJoinBtn = joinBtn.cloneNode(true);
    joinBtn.parentNode.replaceChild(newJoinBtn, joinBtn);
    newJoinBtn.addEventListener("click", () =>
      handleJoinSession(document.getElementById("join-code-input")?.value || "")
    );
    console.log("[Session] Join button listener attached");
  }

  if (leaveBtn) {
    const newLeaveBtn = leaveBtn.cloneNode(true);
    leaveBtn.parentNode.replaceChild(newLeaveBtn, leaveBtn);
    newLeaveBtn.addEventListener("click", handleLeaveSession);
    console.log("[Session] Leave button listener attached");
  }

  if (copyBtn) {
    const newCopyBtn = copyBtn.cloneNode(true);
    copyBtn.parentNode.replaceChild(newCopyBtn, copyBtn);
    newCopyBtn.addEventListener("click", handleCopyCode);
    console.log("[Session] Copy button listener attached");
  }
}

function startSessionTimerUpdates() {
  // Clear any existing interval first
  if (sessionTimerInterval) {
    clearInterval(sessionTimerInterval);
    sessionTimerInterval = null;
  }

  sessionTimerInterval = setInterval(() => {
    if (_currentPage === "session" && _currentSession) {
      updateSessionTimer();
    }
  }, 500);
}

function updateSessionTimer() {
  const timerDisplay = document.getElementById("session-timer-display");
  const statusBadge = document.getElementById("session-timer-status");

  if (!timerDisplay || !statusBadge) return;

  const seconds =
    window.currentTimerState?.remainingSeconds ||
    _currentSession?.remainingSeconds ||
    0;
  const running = window.currentTimerState?.running || false;
  const paused = window.currentTimerState?.paused || false;

  timerDisplay.textContent = formatTime(seconds);

  // Set status text and badge color
  let statusText = "Stopped";
  let badgeClass = "badge-danger";

  if (running) {
    statusText = "Running";
    badgeClass = "badge-success";
  } else if (paused) {
    statusText = "Paused";
    badgeClass = "badge-warning";
  }

  statusBadge.textContent = statusText;
  statusBadge.className = `badge ${badgeClass}`;
}

function updateSessionView() {
  const noSessionView = document.getElementById("no-session-view");
  const activeSessionView = document.getElementById("active-session-view");

  if (_currentSession) {
    // Show active session view
    if (noSessionView) noSessionView.style.display = "none";
    if (activeSessionView) activeSessionView.style.display = "block";
    displaySession(_currentSession);
  } else {
    // Show create/join view
    if (noSessionView) noSessionView.style.display = "grid";
    if (activeSessionView) activeSessionView.style.display = "none";
  }
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

  // Get values from inputs
  const nameInput = document.getElementById("session-name-input");
  const secondsInput = document.getElementById("initial-seconds-input");

  const sessionName = nameInput?.value || "Subathon-" + Date.now();
  const initialSeconds = parseInt(secondsInput?.value) || 300;

  try {
    console.log("[Session] Calling api.createSession()", {
      sessionName,
      initialSeconds,
    });
    const session = await api.createSession(sessionName, initialSeconds);
    console.log("[Session] Session created:", session);

    // Transform session to expected format
    const transformedSession = {
      sessionId: session.id || session.sessionId,
      code: session.code || session.id || "NO-CODE",
      ownerName: session.ownerName || "You",
      createdAt: session.createdAt || new Date().toISOString(),
      name: session.name || sessionName,
      goalMinutes: session.goalMinutes || Math.floor(initialSeconds / 60),
      currentMinutes: session.currentMinutes || 0,
      remainingSeconds: session.remainingSeconds || initialSeconds,
      isActive: session.isActive || false,
    };

    _currentSession = transformedSession;
    updateSessionView();
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
      remainingSeconds: session.remainingSeconds || 0,
      isActive: session.isActive || false,
    };

    _currentSession = transformedSession;
    updateSessionView();
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
  updateSessionView();
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

function displaySession(session) {
  const sessionName = document.getElementById("session-name");
  const sessionId = document.getElementById("session-id-text");
  const sessionCode = document.getElementById("session-code-text");
  const sessionOwner = document.getElementById("session-owner");
  const sessionCreated = document.getElementById("session-created");

  if (sessionName) sessionName.textContent = session.name || "Subathon Session";
  if (sessionId)
    sessionId.textContent = session.sessionId || session.id || "N/A";
  if (sessionCode) sessionCode.textContent = session.code || "N/A";
  if (sessionOwner) sessionOwner.textContent = session.ownerName || "Unknown";
  if (sessionCreated)
    sessionCreated.textContent = formatDate(
      session.createdAt || new Date().toISOString()
    );

  // Update timer display
  updateSessionTimer();
}
