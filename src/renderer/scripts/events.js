let eventsLoaded = false;

// Events page setup and handlers
// eslint-disable-next-line no-unused-vars
function setupEventsPage() {
  // Only set up listeners and polling once
  if (!eventsLoaded) {
    const refreshBtn = document.getElementById("refresh-events-btn");
    const clearBtn = document.getElementById("clear-events-btn");

    if (refreshBtn) {
      refreshBtn.addEventListener("click", loadEvents);
    }

    if (clearBtn) {
      clearBtn.addEventListener("click", handleClearEvents);
    }

    startEventPolling();
    setupTwitchEventListener();
    eventsLoaded = true;
  }

  // Always load events when entering the page
  loadEvents();
}

function setupTwitchEventListener() {
  electronAPI.on("twitch-event", (event) => {
    console.log("[Events] Received Twitch event:", event);
    // Reload events table to show new event
    if (_currentPage === "events") {
      loadEvents();
    }
  });
}

let eventPollingInterval = null;

function startEventPolling() {
  if (eventPollingInterval) {
    clearInterval(eventPollingInterval);
  }
  eventPollingInterval = setInterval(() => {
    if (_currentPage === "events") {
      loadEvents();
    }
  }, 5000);
}

async function loadEvents() {
  const tbody = document.getElementById("events-table-body");
  if (!tbody) return;

  if (!_currentSession) {
    tbody.innerHTML = `
      <tr><td colspan="7" class="text-center text-muted">Keine aktive Session</td></tr>
    `;
    return;
  }

  try {
    const events = await api.getEvents(_currentSession.sessionId);
    displayEvents(events);
  } catch (error) {
    console.error("Failed to load events");
    void error;
  }
}

function displayEvents(events) {
  const tbody = document.getElementById("events-table-body");

  if (!events || events.length === 0) {
    tbody.innerHTML = `
      <tr><td colspan="7" class="text-center text-muted">Keine Events</td></tr>
    `;
    return;
  }

  tbody.innerHTML = events
    .slice(0, 100)
    .map(
      (event) => `
    <tr>
      <td>${formatTimestamp(event.timestamp)}</td>
      <td><span class="badge badge-primary">${formatEventType(
        event.eventType
      )}</span></td>
      <td>${event.channelName}</td>
      <td>${event.username || "-"}</td>
      <td>${event.amount || "-"}</td>
      <td>${event.addedSeconds}s</td>
      <td>
        <span class="badge ${
          event.processed ? "badge-success" : "badge-warning"
        }">
          ${event.processed ? "Processed" : "Pending"}
        </span>
      </td>
    </tr>
  `
    )
    .join("");
}

async function handleClearEvents() {
  if (!_currentSession) return;

  if (!confirm("Alle Events wirklich löschen?")) return;

  showNotification(
    "Feature nicht verfügbar",
    "Events löschen ist noch nicht implementiert",
    "info"
  );
}
