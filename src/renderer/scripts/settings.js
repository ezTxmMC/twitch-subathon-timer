// Settings page setup and handlers
function setupSettingsPage() {
  const saveTogglesBtn = document.getElementById("save-toggles-btn");
  const saveSettingsBtn = document.getElementById("save-settings-btn");

  if (saveTogglesBtn) {
    saveTogglesBtn.addEventListener("click", handleSaveToggles);
  }

  if (saveSettingsBtn) {
    saveSettingsBtn.addEventListener("click", handleSaveSettings);
  }

  loadSettings();
  loadToggles();
}

async function loadSettings() {
  if (!_currentSession) return;

  try {
    const settings = await api.getSettings(_currentSession.sessionId);

    document.getElementById("follow-seconds").value = settings.followSeconds;
    document.getElementById("sub-tier1-seconds").value =
      settings.subTier1Seconds;
    document.getElementById("sub-tier2-seconds").value =
      settings.subTier2Seconds;
    document.getElementById("sub-tier3-seconds").value =
      settings.subTier3Seconds;
    document.getElementById("gifted-sub-seconds").value =
      settings.giftedSubSeconds;
    document.getElementById("bits-per-100").value = settings.bitsPer100;
    document.getElementById("raid-seconds-per-viewer").value =
      settings.raidSecondsPerViewer;
  } catch (error) {
    console.error("Failed to load settings");
    void error;
  }
}

async function loadToggles() {
  if (!_currentSession) return;

  try {
    const toggles = await api.getToggles(_currentSession.sessionId);

    document.getElementById("toggle-follow").checked = toggles.followEnabled;
    document.getElementById("toggle-subscription").checked =
      toggles.subscriptionEnabled;
    document.getElementById("toggle-gifted-sub").checked =
      toggles.giftedSubEnabled;
    document.getElementById("toggle-bits").checked = toggles.bitsEnabled;
    document.getElementById("toggle-raid").checked = toggles.raidEnabled;
  } catch (error) {
    console.error("Failed to load toggles");
    void error;
  }
}

async function handleSaveSettings() {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  const settings = {
    followSeconds: parseInt(document.getElementById("follow-seconds").value),
    subTier1Seconds: parseInt(
      document.getElementById("sub-tier1-seconds").value
    ),
    subTier2Seconds: parseInt(
      document.getElementById("sub-tier2-seconds").value
    ),
    subTier3Seconds: parseInt(
      document.getElementById("sub-tier3-seconds").value
    ),
    giftedSubSeconds: parseInt(
      document.getElementById("gifted-sub-seconds").value
    ),
    bitsPer100: parseInt(document.getElementById("bits-per-100").value),
    raidSecondsPerViewer: parseInt(
      document.getElementById("raid-seconds-per-viewer").value
    ),
  };

  try {
    await api.updateSettings(_currentSession.sessionId, settings);
    showNotification("Gespeichert", "Event Settings gespeichert", "success");
  } catch (error) {
    showNotification("Fehler", "Speichern fehlgeschlagen", "error");
    void error;
  }
}

async function handleSaveToggles() {
  if (!_currentSession) {
    showNotification("Fehler", "Keine aktive Session", "error");
    return;
  }

  const toggles = {
    followEnabled: document.getElementById("toggle-follow").checked,
    subscriptionEnabled: document.getElementById("toggle-subscription").checked,
    giftedSubEnabled: document.getElementById("toggle-gifted-sub").checked,
    bitsEnabled: document.getElementById("toggle-bits").checked,
    raidEnabled: document.getElementById("toggle-raid").checked,
  };

  try {
    await api.updateToggles(_currentSession.sessionId, toggles);
    showNotification("Gespeichert", "Event Toggles gespeichert", "success");
  } catch (error) {
    showNotification("Fehler", "Speichern fehlgeschlagen", "error");
    void error;
  }
}
