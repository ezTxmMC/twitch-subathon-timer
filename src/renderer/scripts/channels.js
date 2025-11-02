let channelsLoaded = false;

// Channels page setup and handlers
// eslint-disable-next-line no-unused-vars
function setupChannelsPage() {
  if (channelsLoaded) {
    // Just reload channels if already initialized
    loadChannels();
    return;
  }

  loadChannels();
  channelsLoaded = true;
}

async function loadChannels() {
  const tbody = document.getElementById("channels-table-body");
  if (!tbody) return;

  if (!_currentSession) {
    tbody.innerHTML = `
      <tr><td colspan="5" class="text-center text-muted">Keine aktive Session</td></tr>
    `;
    return;
  }

  try {
    const channels = await api.getChannels(_currentSession.sessionId);
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

// eslint-disable-next-line no-unused-vars
async function handleRemoveChannel(channelId) {
  if (!_currentSession) return;

  if (!confirm("Channel wirklich entfernen?")) return;

  try {
    // Get channel info before removing to know which chat to leave
    const channels = await api.getChannels(_currentSession.sessionId);
    const channel = channels.find((c) => c.id === channelId);

    // Remove channel from session
    await api.removeChannel(_currentSession.sessionId, channelId);

    // Leave the Twitch chat channel if channel was found
    if (channel?.channelName) {
      try {
        await electronAPI.chat.leaveChannel(channel.channelName);
        console.log(`[Channels] Left Twitch chat: ${channel.channelName}`);
      } catch (chatError) {
        console.error("[Channels] Failed to leave chat:", chatError);
        // Continue anyway since channel was removed from session
      }
    }

    // Reload the channels list
    await loadChannels();
    showNotification("Channel entfernt", "Channel wurde entfernt", "info");
  } catch (error) {
    console.error("[Channels] Failed to remove channel:", error);
    showNotification("Fehler", "Channel konnte nicht entfernt werden", "error");
    void error;
  }
}
