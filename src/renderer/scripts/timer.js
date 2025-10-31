// Timer page setup and handlers
let timerInterval = null;
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

  startTimerSync();
}

async function handleStartTimer() {
  if (!currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  try {
    await api.startTimer(currentSession.sessionId);
    showNotification("Timer gestartet", "Timer läuft", "success");
  } catch (error) {
    showNotification("Fehler", "Timer konnte nicht gestartet werden", "error");
  }
}

async function handlePauseTimer() {
  if (!currentSession) return;

  try {
    await api.pauseTimer(currentSession.sessionId);
    showNotification("Timer pausiert", "Timer angehalten", "info");
  } catch (error) {
    showNotification("Fehler", "Timer konnte nicht pausiert werden", "error");
  }
}

async function handleResetTimer() {
  if (!currentSession) return;

  if (!confirm("Timer wirklich zurücksetzen?")) return;

  try {
    await api.resetTimer(currentSession.sessionId);
    showNotification("Timer zurückgesetzt", "Timer auf 0 gesetzt", "info");
  } catch (error) {
    showNotification(
      "Fehler",
      "Timer konnte nicht zurückgesetzt werden",
      "error"
    );
  }
}

async function handleAddTime() {
  if (!currentSession) return;

  const seconds = parseInt(document.getElementById("add-time-seconds").value);
  const reason = document.getElementById("add-time-reason").value;

  try {
    await api.addTime(currentSession.sessionId, seconds, reason);
    showNotification("Zeit hinzugefügt", `${seconds}s hinzugefügt`, "success");
  } catch (error) {
    showNotification("Fehler", "Zeit konnte nicht hinzugefügt werden", "error");
  }
}

async function handleQuickAddTime(seconds) {
  if (!currentSession) return;

  try {
    await api.addTime(currentSession.sessionId, seconds, "Quick Add");
    showNotification("Zeit hinzugefügt", `${seconds}s hinzugefügt`, "success");
  } catch (error) {
    console.error("Failed to add time");
  }
}

async function handleTestEvent(type) {
  if (!currentSession) return;

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
    if (currentSession) {
      try {
        const response = await fetch(
          `${api.getBaseURL()}/timer/${currentSession.sessionId}`
        );
        if (response.ok) {
          const timerData = await response.json();
          currentTimerState = timerData;
        }
      } catch (error) {
        // Silently fail - keep current state
      }
    }
    updateTimerDisplay();
  }, 1000);
}

function updateTimerDisplay() {
  const display = document.getElementById("timer-display");
  if (display) {
    display.textContent = formatTime(currentTimerState.seconds);
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
