// Timer page setup and handlers
let timerInterval = null; // eslint-disable-line no-unused-vars

// Make currentTimerState globally accessible
window.currentTimerState = {
  seconds: 0,
  remainingSeconds: 0,
  running: false,
  paused: false,
};

// Alias for local access
let currentTimerState = window.currentTimerState;

console.log("[Timer] Script loaded");
console.log("[Timer] api available at load time:", typeof api, api);

// eslint-disable-next-line no-unused-vars
function setupTimerPage() {
  console.log("[Timer] Setting up timer page");
  console.log("[Timer] api available in setupTimerPage:", typeof api, api);
  console.log("[Timer] _currentSession:", _currentSession);

  // Always update timer display first
  updateTimerDisplay();

  // Setup quick actions (they clear themselves first)
  setupQuickActions();

  // Start sync if not already running
  if (!timerInterval) {
    startTimerSync();
  }

  // Always re-attach listeners to prevent them from breaking
  attachTimerEventListeners();
}

function attachTimerEventListeners() {
  const startBtn = document.getElementById("start-timer-btn");
  const pauseBtn = document.getElementById("pause-timer-btn");
  const resetBtn = document.getElementById("reset-timer-btn");
  const addTimeBtn = document.getElementById("add-time-btn");

  const testBtns = {
    follow: document.getElementById("test-follow-btn"),
    sub: document.getElementById("test-sub-btn"),
    giftsub: document.getElementById("test-giftsub-btn"),
    bits: document.getElementById("test-bits-btn"),
    raid: document.getElementById("test-raid-btn"),
  };

  // Remove and re-attach main button listeners
  if (startBtn) {
    const newBtn = startBtn.cloneNode(true);
    startBtn.parentNode.replaceChild(newBtn, startBtn);
    newBtn.addEventListener("click", handleStartTimer);
  }

  if (pauseBtn) {
    const newBtn = pauseBtn.cloneNode(true);
    pauseBtn.parentNode.replaceChild(newBtn, pauseBtn);
    newBtn.addEventListener("click", handlePauseTimer);
  }

  if (resetBtn) {
    const newBtn = resetBtn.cloneNode(true);
    resetBtn.parentNode.replaceChild(newBtn, resetBtn);
    newBtn.addEventListener("click", handleResetTimer);
  }

  if (addTimeBtn) {
    const newBtn = addTimeBtn.cloneNode(true);
    addTimeBtn.parentNode.replaceChild(newBtn, addTimeBtn);
    newBtn.addEventListener("click", handleAddTime);
  }

  // Re-attach quick action buttons
  const quickActionBtns = document.querySelectorAll(".quick-action-btn");
  quickActionBtns.forEach((btn) => {
    const newBtn = btn.cloneNode(true);
    btn.parentNode.replaceChild(newBtn, btn);
    newBtn.addEventListener("click", () => {
      const seconds = parseInt(newBtn.dataset.seconds);
      handleQuickAddTime(seconds);
    });
  });

  // Re-attach test event buttons
  Object.entries(testBtns).forEach(([type, btn]) => {
    if (btn) {
      const newBtn = btn.cloneNode(true);
      btn.parentNode.replaceChild(newBtn, btn);
      newBtn.addEventListener("click", () => handleTestEvent(type));
    }
  });
}

async function handleStartTimer() {
  console.log("[Timer] Start button clicked");
  console.log("[Timer] _currentSession:", _currentSession);
  console.log("[Timer] api object:", api);
  console.log("[Timer] api.startTimer:", typeof api?.startTimer);

  if (!_currentSession) {
    showNotification(
      "Fehler",
      "Keine aktive Session. Bitte zuerst eine Session erstellen oder beitreten.",
      "error"
    );
    return;
  }

  try {
    console.log(
      "[Timer] Calling api.startTimer with sessionId:",
      _currentSession.sessionId
    );
    await api.startTimer(_currentSession.sessionId);
    console.log("[Timer] api.startTimer call completed");
    showNotification("Timer gestartet", "Timer läuft", "success");

    // Update current state immediately
    currentTimerState.running = true;
    currentTimerState.paused = false;

    // Broadcast timer start to overlays
    electronAPI.server
      .broadcast({
        type: "timer-start",
        seconds: currentTimerState.remainingSeconds || 0,
        running: true,
        timestamp: Date.now(),
      })
      .catch((err) => console.error("Failed to broadcast timer start:", err));
  } catch (error) {
    showNotification("Fehler", "Timer konnte nicht gestartet werden", "error");
    void error;
  }
}

async function handlePauseTimer() {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  try {
    await api.pauseTimer(_currentSession.sessionId);
    showNotification("Timer pausiert", "Timer angehalten", "info");

    // Update current state immediately
    currentTimerState.running = false;
    currentTimerState.paused = true;

    // Broadcast timer pause to overlays
    electronAPI.server
      .broadcast({
        type: "timer-pause",
        seconds: currentTimerState.remainingSeconds || 0,
        running: false,
        timestamp: Date.now(),
      })
      .catch((err) => console.error("Failed to broadcast timer pause:", err));
  } catch (error) {
    showNotification("Fehler", "Timer konnte nicht pausiert werden", "error");
    void error;
  }
}

async function handleResetTimer() {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  if (!confirm("Timer wirklich zurücksetzen?")) return;

  try {
    await api.resetTimer(_currentSession.sessionId);
    showNotification("Timer zurückgesetzt", "Timer auf 0 gesetzt", "info");

    // Update current state immediately
    currentTimerState.remainingSeconds = 0;
    currentTimerState.running = false;
    currentTimerState.paused = false;

    // Broadcast timer reset to overlays
    electronAPI.server
      .broadcast({
        type: "timer-reset",
        seconds: 0,
        running: false,
        timestamp: Date.now(),
      })
      .catch((err) => console.error("Failed to broadcast timer reset:", err));
  } catch (error) {
    showNotification(
      "Fehler",
      "Timer konnte nicht zurückgesetzt werden",
      "error"
    );
    void error;
  }
}

async function handleAddTime() {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  const seconds = parseInt(document.getElementById("add-time-seconds").value);
  const reason = document.getElementById("add-time-reason").value;

  try {
    await api.addTime(_currentSession.sessionId, seconds, reason);
    showNotification("Zeit hinzugefügt", `${seconds}s hinzugefügt`, "success");

    // Update current state immediately (add seconds)
    if (currentTimerState.remainingSeconds !== undefined) {
      currentTimerState.remainingSeconds += seconds;
    }

    // Broadcast timer add to overlays with updated total
    electronAPI.server
      .broadcast({
        type: "timer-add",
        addedSeconds: seconds,
        totalSeconds: currentTimerState.remainingSeconds || seconds,
        running: currentTimerState.running || false,
        timestamp: Date.now(),
      })
      .catch((err) => console.error("Failed to broadcast timer add:", err));
  } catch (error) {
    showNotification("Fehler", "Zeit konnte nicht hinzugefügt werden", "error");
    void error;
  }
}

async function handleQuickAddTime(seconds) {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  try {
    await api.addTime(_currentSession.sessionId, seconds, "Quick Add");
    showNotification("Zeit hinzugefügt", `${seconds}s hinzugefügt`, "success");

    // Update current state immediately (add seconds)
    if (currentTimerState.remainingSeconds !== undefined) {
      currentTimerState.remainingSeconds += seconds;
    }

    // Broadcast timer add to overlays with updated total
    electronAPI.server
      .broadcast({
        type: "timer-add",
        addedSeconds: seconds,
        totalSeconds: currentTimerState.remainingSeconds || seconds,
        running: currentTimerState.running || false,
        timestamp: Date.now(),
      })
      .catch((err) => console.error("Failed to broadcast timer add:", err));
  } catch (error) {
    console.error("Failed to add time");
    void error;
  }
}

async function handleTestEvent(type) {
  if (!_currentSession) {
    showNotification("Info", "Test Events benötigen keine Session", "info");
  }

  const testData = {
    follow: { username: "TestFollower", eventType: "FOLLOW" },
    sub: { username: "TestSub", tier: "1000", eventType: "SUBSCRIPTION" },
    giftsub: { username: "TestGifter", amount: 5, eventType: "GIFTED_SUB" },
    bits: { username: "TestCheerer", bits: 100, eventType: "BITS" },
    raid: { username: "TestRaider", viewers: 50, eventType: "RAID" },
  };

  const eventData = testData[type];

  // Broadcast event to overlays
  electronAPI.server
    .broadcast({
      type: "event-alert",
      ...eventData,
      timestamp: Date.now(),
    })
    .catch((err) => console.error("Failed to broadcast test event:", err));

  console.log(`Test event: ${type}`, eventData);
  showNotification("Test Event", `${type} event gesendet`, "info");
}

function startTimerSync() {
  // Initial broadcast of current state
  electronAPI.server
    .broadcast({
      type: "timer-update",
      seconds: currentTimerState.remainingSeconds || 0,
      running: currentTimerState.running || false,
      timestamp: Date.now(),
    })
    .catch((err) => console.error("Failed to broadcast initial state:", err));

  timerInterval = setInterval(async () => {
    // Poll the server for timer state
    if (_currentSession) {
      try {
        const response = await fetch(
          `${api.getBaseURL()}/timer/${_currentSession.sessionId}`
        );
        if (response.ok) {
          const timerData = await response.json();
          currentTimerState = timerData;

          // Broadcast timer state to overlays via WebSocket
          electronAPI.server
            .broadcast({
              type: "timer-update",
              seconds: timerData.remainingSeconds || 0,
              running: timerData.running || false,
              timestamp: Date.now(),
            })
            .catch((err) =>
              console.error("Failed to broadcast timer update:", err)
            );
        }
      } catch (error) {
        // Silently fail - keep current state
        void error;
      }
    } else {
      // No session - broadcast zero state
      electronAPI.server
        .broadcast({
          type: "timer-update",
          seconds: 0,
          running: false,
          timestamp: Date.now(),
        })
        .catch((err) =>
          console.error("Failed to broadcast no-session state:", err)
        );
    }
    updateTimerDisplay();
  }, 500);
}

function updateTimerDisplay() {
  const display = document.getElementById("timer-display");
  if (display) {
    display.textContent = formatTime(currentTimerState.remainingSeconds);
  }

  const status = document.getElementById("timer-status");
  if (status) {
    // Set status text and badge color
    let statusText = "Stopped";
    let badgeClass = "badge-danger";

    if (currentTimerState.status === "RUNNING") {
      statusText = "Running";
      badgeClass = "badge-success";
    }
    if (currentTimerState.status === "PAUSED") {
      statusText = "Paused";
      badgeClass = "badge-warning";
    }
    if (currentTimerState.status === "STOPPED") {
      statusText = "Stopped";
      badgeClass = "badge-warning";
    }

    status.textContent = statusText;
    status.className = `badge ${badgeClass}`;
  }
}

electronAPI.on("timer-update", (timerData) => {
  currentTimerState = timerData;
  updateTimerDisplay();
});

// Quick action button handlers and setup
function setupQuickActions() {
  const quickActionsContainer = document.querySelector(".quick-actions");
  if (!quickActionsContainer) return;

  // Clear existing buttons to prevent duplicates
  quickActionsContainer.innerHTML = "";

  const quickActions = [
    { seconds: 30, label: "+30s" },
    { seconds: 60, label: "+1m" },
    { seconds: 300, label: "+5m" },
    { seconds: 600, label: "+10m" },
    { seconds: 1800, label: "+30m" },
    { seconds: 3600, label: "+1h" },
  ];

  quickActions.forEach((action) => {
    const button = document.createElement("button");
    button.className = "quick-action-btn btn btn-secondary";
    button.textContent = action.label;
    button.dataset.seconds = action.seconds;
    button.addEventListener("click", () => handleQuickAddTime(action.seconds));
    quickActionsContainer.appendChild(button);
  });
}
