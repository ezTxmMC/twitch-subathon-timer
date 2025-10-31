// Twitch page setup and handlers
// Note: twitchUser is a global variable defined in app.js

function setupTwitchPage() {
  const loginBtn = document.getElementById("twitch-login-btn");
  const logoutBtn = document.getElementById("twitch-logout-btn");

  if (loginBtn) {
    loginBtn.addEventListener("click", handleTwitchLogin);
  }

  if (logoutBtn) {
    logoutBtn.addEventListener("click", handleTwitchLogout);
  }

  loadTwitchUser();
}

async function handleTwitchLogin() {
  try {
    const result = await electronAPI.twitch.login();

    if (result.success) {
      twitchUser = result.user;
      displayTwitchUser(result.user);
      showNotification(
        "Twitch Login",
        "Erfolgreich mit Twitch verbunden!",
        "success"
      );

      if (currentSession) {
        await autoAddChannelToSession();
      }
    }
  } catch (error) {
    showNotification("Fehler", "Twitch Login fehlgeschlagen", "error");
  }
}

async function handleTwitchLogout() {
  try {
    await electronAPI.twitch.logout();
    twitchUser = null;

    document.getElementById("twitch-not-connected").classList.remove("hidden");
    document.getElementById("twitch-connected").classList.add("hidden");

    showNotification("Twitch Logout", "Von Twitch getrennt", "info");
  } catch (error) {
    showNotification("Fehler", "Logout fehlgeschlagen", "error");
  }
}

async function loadTwitchUser() {
  try {
    const result = await electronAPI.twitch.getUser();

    if (result.success) {
      twitchUser = result.user;
      displayTwitchUser(result.user);
    }
  } catch (error) {
    console.error("Failed to load Twitch user");
  }
}

function displayTwitchUser(user) {
  document.getElementById("twitch-not-connected").classList.add("hidden");
  document.getElementById("twitch-connected").classList.remove("hidden");

  document.getElementById("twitch-avatar").src = user.profileImageUrl;
  document.getElementById("twitch-username").textContent = user.displayName;
  document.getElementById("twitch-user-id").textContent = user.id;
}

async function autoAddChannelToSession() {
  if (!currentSession || !twitchUser) return;

  try {
    await api.addChannel(currentSession.sessionId, {
      channelName: twitchUser.displayName,
      channelId: twitchUser.id,
      accessToken: twitchUser.accessToken,
    });

    showNotification(
      "Channel hinzugefügt",
      `${twitchUser.displayName} zur Session hinzugefügt`,
      "success"
    );
  } catch (error) {
    console.error("Failed to auto-add channel");
  }
}
