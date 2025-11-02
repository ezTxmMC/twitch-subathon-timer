// Dashboard page setup and handlers
let dashboardLoaded = false;
let dashboardUpdateInterval = null; // eslint-disable-line no-unused-vars

console.log("[Dashboard] Script loaded");

// eslint-disable-next-line no-unused-vars
function setupDashboardPage() {
  console.log("[Dashboard] Setting up dashboard page");

  // Only attach listeners once
  if (!dashboardLoaded) {
    // Start dashboard updates
    startDashboardUpdates();
    dashboardLoaded = true;
  }

  // Always update dashboard display when entering the page
  updateDashboardDisplay();
}

function startDashboardUpdates() {
  // Clear any existing interval first
  if (dashboardUpdateInterval) {
    clearInterval(dashboardUpdateInterval);
    dashboardUpdateInterval = null;
  }

  // Update dashboard every second
  dashboardUpdateInterval = setInterval(() => {
    if (_currentPage === "dashboard") {
      updateDashboardDisplay();
    }
  }, 1000);
}

function updateDashboardDisplay() {
  updateSessionStatus();
  updateDashboardTimer();
  updateDashboardChannels();
  updateDashboardEvents();
}

function updateSessionStatus() {
  const statusContainer = document.getElementById("session-status");
  if (!statusContainer) return;

  if (_currentSession) {
    statusContainer.innerHTML = `
      <div class="session-info">
        <div style="display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px;">
          <strong>${_currentSession.name || "Subathon Session"}</strong>
          <span class="badge badge-success">Aktiv</span>
        </div>
        <div style="display: grid; grid-template-columns: repeat(2, 1fr); gap: 12px;">
          <div>
            <p class="text-muted" style="margin: 0; font-size: 12px;">Session Code</p>
            <code style="font-size: 14px; font-weight: 700;">${
              _currentSession.code || "N/A"
            }</code>
          </div>
          <div>
            <p class="text-muted" style="margin: 0; font-size: 12px;">Owner</p>
            <p style="margin: 0;">${_currentSession.ownerName || "Unknown"}</p>
          </div>
        </div>
      </div>
    `;
  } else {
    statusContainer.innerHTML = `
      <p class="text-muted">Keine aktive Session</p>
      <button class="btn btn-primary" onclick="loadPage('session')" style="margin-top: 12px;">
        Session erstellen
      </button>
    `;
  }
}

function updateDashboardTimer() {
  const timerDisplay = document.getElementById("dashboard-timer");
  const statusBadge = document.getElementById("dashboard-timer-status");

  if (!timerDisplay || !statusBadge) return;

  if (_currentSession) {
    // Get timer state from global or use defaults
    const seconds =
      window.currentTimerState?.remainingSeconds ||
      _currentSession.remainingSeconds ||
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
  } else {
    timerDisplay.textContent = "00:00:00";
    statusBadge.textContent = "No Session";
    statusBadge.className = "badge badge-secondary";
  }
}

function updateDashboardChannels() {
  const channelsContainer = document.getElementById("dashboard-channels");
  if (!channelsContainer) return;

  if (_twitchUser) {
    channelsContainer.innerHTML = `
      <div style="display: flex; align-items: center; gap: 12px;">
        <div style="width: 40px; height: 40px; border-radius: 50%; background: var(--color-primary); display: flex; align-items: center; justify-content: center; color: white; font-weight: 700;">
          ${_twitchUser.display_name?.charAt(0) || "?"}
        </div>
        <div style="flex: 1;">
          <strong>${_twitchUser.display_name}</strong>
          <p class="text-muted" style="margin: 0; font-size: 12px;">Twitch verbunden</p>
        </div>
        <span class="badge badge-success">Online</span>
      </div>
    `;
  } else {
    channelsContainer.innerHTML = `
      <p class="text-muted">Keine Channels verbunden</p>
      <button class="btn btn-primary" onclick="loadPage('twitch')" style="margin-top: 12px;">
        Twitch verbinden
      </button>
    `;
  }
}

function updateDashboardEvents() {
  const eventsContainer = document.getElementById("dashboard-events");
  if (!eventsContainer) return;

  // Get recent events from storage or show placeholder
  const recentEvents = JSON.parse(
    localStorage.getItem("recentEvents") || "[]"
  ).slice(0, 5);

  if (recentEvents.length > 0) {
    eventsContainer.innerHTML = recentEvents
      .map(
        (event) => `
      <div style="display: flex; justify-content: space-between; align-items: center; padding: 8px 0; border-bottom: 1px solid var(--color-border);">
        <div>
          <strong>${event.username}</strong>
          <span class="text-muted" style="font-size: 12px; margin-left: 8px;">${
            event.type
          }</span>
        </div>
        <span class="text-muted" style="font-size: 12px;">${formatTimeAgo(
          event.timestamp
        )}</span>
      </div>
    `
      )
      .join("");
  } else {
    eventsContainer.innerHTML = `
      <p class="text-muted">Keine Events</p>
      <p style="font-size: 12px; margin-top: 8px;">Events werden hier angezeigt wenn sie auftreten</p>
    `;
  }
}

function formatTimeAgo(timestamp) {
  const now = Date.now();
  const diff = now - timestamp;
  const seconds = Math.floor(diff / 1000);
  const minutes = Math.floor(seconds / 60);
  const hours = Math.floor(minutes / 60);

  if (hours > 0) return `${hours}h ago`;
  if (minutes > 0) return `${minutes}m ago`;
  return `${seconds}s ago`;
}

// Listen for events to update dashboard
electronAPI.on("twitch-event", (event) => {
  // Add to recent events
  const recentEvents = JSON.parse(localStorage.getItem("recentEvents") || "[]");
  recentEvents.unshift({
    username: event.username,
    type: event.eventType,
    timestamp: Date.now(),
  });

  // Keep only last 50 events
  localStorage.setItem(
    "recentEvents",
    JSON.stringify(recentEvents.slice(0, 50))
  );

  // Update dashboard if visible
  if (_currentPage === "dashboard") {
    updateDashboardEvents();
  }
});
