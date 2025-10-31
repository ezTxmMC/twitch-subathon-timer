// Channels page setup and handlers
function setupChannelsPage() {
  loadChannels();
}

async function loadChannels() {
  if (!currentSession) {
    document.getElementById("channels-table-body").innerHTML = `
      <tr><td colspan="5" class="text-center text-muted">Keine aktive Session</td></tr>
    `;
    return;
  }

  try {
    const channels = await api.getChannels(currentSession.sessionId);
    displayChannels(channels);
  } catch (error) {
    console.error("Failed to load channels");
    void error;
  }
}

function displayChannels(channels) {
  const tbody = document.getElementById("channels-table-body");
  const countBadge = document.getElementById("channel-count");

  if (!channels || channels.length === 0) {
    tbody.innerHTML = `
      <tr><td colspan="5" class="text-center text-muted">Keine Channels verbunden</td></tr>
    `;
    if (countBadge) countBadge.textContent = "0 Channels";
    return;
  }

  if (countBadge) {
    countBadge.textContent = `${channels.length} Channel${
      channels.length !== 1 ? "s" : ""
    }`;
  }

  tbody.innerHTML = channels
    .map(
      (channel) => `
    <tr>
      <td>${channel.channelName}</td>
      <td><code>${channel.channelId}</code></td>
      <td>${formatDate(channel.joinedAt)}</td>
      <td><span class="badge badge-success">Active</span></td>
      <td>
        <div class="table-actions">
          <button class="btn btn-small btn-danger" onclick="handleRemoveChannel('${
            channel.id
          }')">
            Remove
          </button>
        </div>
      </td>
    </tr>
  `
    )
    .join("");
}

async function handleRemoveChannel(channelId) {
  if (!currentSession) return;

  if (!confirm("Channel wirklich entfernen?")) return;

  try {
    await api.removeChannel(currentSession.sessionId, channelId);
    await loadChannels();
    showNotification("Channel entfernt", "Channel wurde entfernt", "info");
  } catch (error) {
    showNotification("Fehler", "Channel konnte nicht entfernt werden", "error");
    void error;
  }
}

document.addEventListener("DOMContentLoaded", setupChannelsPage);
