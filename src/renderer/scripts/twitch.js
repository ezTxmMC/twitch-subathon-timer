// Twitch page setup and handlers
// Note: _twitchUser is a global variable defined in app.js

let twitchLoaded = false;

// eslint-disable-next-line no-unused-vars
function setupTwitchPage() {
  // Only attach event listeners once
  if (!twitchLoaded) {
    const loginBtn = document.getElementById("twitch-login-btn");
    const logoutBtn = document.getElementById("twitch-logout-btn");

    if (loginBtn) {
      loginBtn.addEventListener("click", handleTwitchLogin);
    }

    if (logoutBtn) {
      logoutBtn.addEventListener("click", handleTwitchLogout);
    }

    twitchLoaded = true;
  }

  // Always update UI with current Twitch user state
  if (_twitchUser) {
    displayTwitchUser(_twitchUser);
  } else {
    // Load Twitch user from main process
    loadTwitchUser();
  }
}

async function handleTwitchLogin() {
  try {
    const result = await electronAPI.twitch.login();

    if (result.success) {
      _twitchUser = result.user;
      displayTwitchUser(result.user);
      showNotification(
        "Twitch Login",
        "Erfolgreich mit Twitch verbunden!",
        "success"
      );

      if (_currentSession) {
        await autoAddChannelToSession();
      }
    }
  } catch (error) {
    showNotification("Fehler", "Twitch Login fehlgeschlagen", "error");
    void error;
  }
}

async function handleTwitchLogout() {
  try {
    await electronAPI.twitch.logout();
    _twitchUser = null;

    const notConnected = document.getElementById("twitch-not-connected");
    const connected = document.getElementById("twitch-connected");

    if (notConnected) notConnected.classList.remove("hidden");
    if (connected) connected.classList.add("hidden");

    showNotification("Twitch Logout", "Von Twitch getrennt", "info");
  } catch (error) {
    showNotification("Fehler", "Logout fehlgeschlagen", "error");
    void error;
  }
}

async function loadTwitchUser() {
  try {
    const result = await electronAPI.twitch.getUser();

    if (result.success) {
      _twitchUser = result.user;
      displayTwitchUser(result.user);
    }
  } catch (error) {
    console.error("Failed to load Twitch user");
    void error;
  }
}

function displayTwitchUser(user) {
  const notConnected = document.getElementById("twitch-not-connected");
  const connected = document.getElementById("twitch-connected");
  const avatar = document.getElementById("twitch-avatar");
  const username = document.getElementById("twitch-username");
  const userId = document.getElementById("twitch-user-id");

  if (notConnected) notConnected.classList.add("hidden");
  if (connected) connected.classList.remove("hidden");
  if (avatar) avatar.src = user.profileImageUrl;
  if (username) username.textContent = user.displayName;
  if (userId) userId.textContent = user.id;
}

async function autoAddChannelToSession() {
  if (!_currentSession || !_twitchUser) return;

  try {
    await api.addChannel(_currentSession.sessionId, {
      channelName: _twitchUser.displayName,
      channelId: _twitchUser.id,
      accessToken: _twitchUser.accessToken,
    });

    showNotification(
      "Channel hinzugefügt",
      `${_twitchUser.displayName} zur Session hinzugefügt`,
      "success"
    );
  } catch (error) {
    console.error("Failed to auto-add channel");
    void error;
  }
}
