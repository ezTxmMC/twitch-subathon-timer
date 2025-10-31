// Timer page setup and handlers
let timerInterval = null; // eslint-disable-line no-unused-vars
let currentTimerState = {
  seconds: 0,
  running: false,
};

function setupTimerPage() {
  const startBtn = document.getElementById("start-timer-btn");
  const pauseBtn = document.getElementById("pause-timer-btn");
  const resetBtn = document.getElementById("reset-timer-btn");
  const addTimeBtn = document.getElementById("add-time-btn");

  const quickActionBtns = document.querySelectorAll(".quick-action-btn");
  const testBtns = {
    follow: document.getElementById("test-follow-btn"),
    sub: document.getElementById("test-sub-btn"),
    giftsub: document.getElementById("test-giftsub-btn"),
    bits: document.getElementById("test-bits-btn"),
    raid: document.getElementById("test-raid-btn"),
  };

  if (startBtn) startBtn.addEventListener("click", handleStartTimer);
  if (pauseBtn) pauseBtn.addEventListener("click", handlePauseTimer);
  if (resetBtn) resetBtn.addEventListener("click", handleResetTimer);
  if (addTimeBtn) addTimeBtn.addEventListener("click", handleAddTime);

  quickActionBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      const seconds = parseInt(btn.dataset.seconds);
      handleQuickAddTime(seconds);
    });
  });

  Object.entries(testBtns).forEach(([type, btn]) => {
    if (btn) btn.addEventListener("click", () => handleTestEvent(type));
  });

  setupQuickActions();
  startTimerSync();
}

async function handleStartTimer() {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  try {
    await api.startTimer(_currentSession.sessionId);
    showNotification("Timer gestartet", "Timer läuft", "success");
  } catch (error) {
    showNotification("Fehler", "Timer konnte nicht gestartet werden", "error");
    void error;
  }
}

async function handlePauseTimer() {
  if (!_currentSession) return;

  try {
    await api.pauseTimer(_currentSession.sessionId);
    showNotification("Timer pausiert", "Timer angehalten", "info");
  } catch (error) {
    showNotification("Fehler", "Timer konnte nicht pausiert werden", "error");
    void error;
  }
}

async function handleResetTimer() {
  if (!_currentSession) return;

  if (!confirm("Timer wirklich zurücksetzen?")) return;

  try {
    await api.resetTimer(_currentSession.sessionId);
    showNotification("Timer zurückgesetzt", "Timer auf 0 gesetzt", "info");
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
  if (!_currentSession) return;

  const seconds = parseInt(document.getElementById("add-time-seconds").value);
  const reason = document.getElementById("add-time-reason").value;

  try {
    await api.addTime(_currentSession.sessionId, seconds, reason);
    showNotification("Zeit hinzugefügt", `${seconds}s hinzugefügt`, "success");
  } catch (error) {
    showNotification("Fehler", "Zeit konnte nicht hinzugefügt werden", "error");
    void error;
  }
}

async function handleQuickAddTime(seconds) {
  if (!_currentSession) return;

  try {
    await api.addTime(_currentSession.sessionId, seconds, "Quick Add");
    showNotification("Zeit hinzugefügt", `${seconds}s hinzugefügt`, "success");
  } catch (error) {
    console.error("Failed to add time");
    void error;
  }
}

async function handleTestEvent(type) {
  if (!_currentSession) return;

  const testData = {
    follow: { username: "TestFollower" },
    sub: { username: "TestSub", tier: "1000" },
    giftsub: { username: "TestGifter", amount: 5 },
    bits: { username: "TestCheerer", bits: 100 },
    raid: { username: "TestRaider", viewers: 50 },
  };

  console.log(`Test event: ${type}`, testData[type]);
  showNotification("Test Event", `${type} event gesendet`, "info");
}

function startTimerSync() {
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
        }
      } catch (error) {
        // Silently fail - keep current state
        void error;
      }
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
    status.textContent = currentTimerState.running ? "Running" : "Stopped";
    status.className = `badge ${
      currentTimerState.running ? "badge-success" : "badge-danger"
    }`;
  }
}

electronAPI.on("timer-update", (timerData) => {
  currentTimerState = timerData;
  updateTimerDisplay();
});

// Initialize timer page when DOM is loaded
document.addEventListener("DOMContentLoaded", setupTimerPage);
// Quick action button handlers and setup
function setupQuickActions() {
  const quickActionsContainer = document.querySelector(".quick-actions");
  if (!quickActionsContainer) return;

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
