if (currentPage === 'events') {
    setupEventsPage();
}

function setupEventsPage() {
    const refreshBtn = document.getElementById('refresh-events-btn');
    const clearBtn = document.getElementById('clear-events-btn');

    if (refreshBtn) {
        refreshBtn.addEventListener('click', loadEvents);
    }

    if (clearBtn) {
        clearBtn.addEventListener('click', handleClearEvents);
    }

    loadEvents();
    startEventPolling();
}

let eventPollingInterval = null;

function startEventPolling() {
    eventPollingInterval = setInterval(() => {
        if (currentPage === 'events') {
            loadEvents();
        }
    }, 5000);
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
