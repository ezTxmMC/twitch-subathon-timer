// Chat page setup and handlers
let chatMessages = [];
let chatStats = {
  totalMessages: 0,
  activeUsers: new Set(),
  messagesPerMin: 0,
};
let chatLoaded = false;

// eslint-disable-next-line no-unused-vars
function setupChatPage() {
  // Only attach listeners once
  if (!chatLoaded) {
    const sendBtn = document.getElementById("send-message-btn");
    const messageInput = document.getElementById("chat-message-input");
    const clearBtn = document.getElementById("clear-chat-btn");

    if (sendBtn) {
      sendBtn.addEventListener("click", handleSendMessage);
    }

    if (messageInput) {
      messageInput.addEventListener("keypress", (e) => {
        if (e.key === "Enter") handleSendMessage();
      });
    }

    if (clearBtn) {
      clearBtn.addEventListener("click", handleClearChat);
    }

    setupChatListener();
    chatLoaded = true;
  }

  // Always reload chat channels when entering the page
  loadChatChannels();
}

async function loadChatChannels() {
  if (!_currentSession) return;

  try {
    const channels = await api.getChannels(_currentSession.sessionId);
    displayChatChannels(channels);
  } catch (error) {
    console.error("Failed to load chat channels");
    void error;
  }
}

function displayChatChannels(channels) {
  const display = document.getElementById("chat-channels-display");
  const select = document.getElementById("chat-channel-select");

  if (!channels || channels.length === 0) {
    display.innerHTML =
      '<span class="text-muted">Keine Channels verbunden</span>';
    return;
  }

  display.innerHTML = channels
    .map((c) => `<span class="chat-channel-tag">${c.channelName}</span>`)
    .join("");

  select.innerHTML =
    '<option value="">Channel wählen...</option>' +
    channels
      .map((c) => `<option value="${c.channelName}">${c.channelName}</option>`)
      .join("");
}

function setupChatListener() {
  electronAPI.on("chat-message", (message) => {
    addChatMessage(message);
  });
}

function addChatMessage(message) {
  chatMessages.push(message);
  chatStats.totalMessages++;
  chatStats.activeUsers.add(message.username);

  const container = document.getElementById("chat-messages");
  const autoScroll = document.getElementById("chat-auto-scroll")?.checked;

  const messageElement = document.createElement("div");
  messageElement.className = "chat-message";
  messageElement.innerHTML = `
    <div class="chat-message-badges">
      ${
        message.isMod
          ? '<span class="chat-badge" style="background: #00c853;">M</span>'
          : ""
      }
      ${
        message.isSubscriber
          ? '<span class="chat-badge" style="background: #9147ff;">S</span>'
          : ""
      }
      ${
        message.isVip
          ? '<span class="chat-badge" style="background: #ff0090;">V</span>'
          : ""
      }
    </div>
    <span class="chat-message-username" style="color: ${
      message.color || "#ffffff"
    };">
      ${message.username}:
    </span>
    <span class="chat-message-text">${escapeHtml(message.message)}</span>
  `;

  container.appendChild(messageElement);

  if (autoScroll) {
    container.scrollTop = container.scrollHeight;
  }

  updateChatStats();

  // Broadcast chat message to overlay
  electronAPI.server
    .broadcast({
      type: "chat-message",
      username: message.username,
      message: message.message,
      color: message.color || "#ffffff",
      isMod: message.isMod || false,
      isSubscriber: message.isSubscriber || false,
      isVip: message.isVip || false,
      timestamp: Date.now(),
    })
    .catch((err) => console.error("Failed to broadcast chat message:", err));
}

async function handleSendMessage() {
  const input = document.getElementById("chat-message-input");
  const select = document.getElementById("chat-channel-select");
  const message = input.value.trim();
  const channel = select.value;

  if (!message || !channel) return;

  try {
    await electronAPI.chat.sendMessage(channel, message);
    input.value = "";
  } catch (error) {
    showNotification(
      "Fehler",
      "Nachricht konnte nicht gesendet werden",
      "error"
    );
    void error;
  }
}

function handleClearChat() {
  chatMessages = [];
  document.getElementById("chat-messages").innerHTML = "";
  showNotification("Chat geleert", "Chat wurde geleert", "info");
}

function updateChatStats() {
  document.getElementById("chat-total-messages").textContent =
    chatStats.totalMessages;
  document.getElementById("chat-active-users").textContent =
    chatStats.activeUsers.size;
}

function escapeHtml(text) {
  const div = document.createElement("div");
  div.textContent = text;
  return div.innerHTML;
}
