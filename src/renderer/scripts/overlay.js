// Overlay page setup and handlers
// Relies on global _appConfig defined in app.js

// eslint-disable-next-line no-unused-vars
async function setupOverlayPage() {
  const overlayBaseUrl = await getOverlayBaseUrl();
  const overlays = [{ key: "timer", path: "timer" }];

  // Attach event handlers
  overlays.forEach(({ key, path }) => {
    const copyBtn = document.getElementById(`copy-${key}-url-btn`);
    const previewBtn = document.getElementById(`preview-${key}-btn`);
    const url = `${overlayBaseUrl}/overlay/${path}`;

    if (copyBtn) {
      copyBtn.onclick = () => handleOverlayCopy(url);
    }

    if (previewBtn) {
      previewBtn.onclick = () => handleOverlayPreview(url);
    }
  });

  // Always update overlay URLs when entering the page
  overlays.forEach(({ key, path }) => {
    const input = overlayBaseUrl.getElementById(`${key}-overlay-url`);
    const url = `${baseUrl}/overlay/${path}`;

    if (input) {
      input.value = url;
    }
  });
}

async function getOverlayBaseUrl() {
  const fallback = "http://localhost:8080";
  return fallback;
}

function normalizeBaseUrl(url) {
  return url ? url.trim().replace(/\/$/, "") : "http://localhost:8080";
}

function handleOverlayCopy(url) {
  if (navigator?.clipboard?.writeText) {
    navigator.clipboard
      .writeText(url)
      .then(() => showNotification("URL kopiert", `${url} kopiert`, "success"))
      .catch(() => fallbackCopyToClipboard(url));
  } else {
    fallbackCopyToClipboard(url);
  }
}

function fallbackCopyToClipboard(text) {
  try {
    const tempInput = document.createElement("input");
    tempInput.style.position = "absolute";
    tempInput.style.left = "-1000px";
    tempInput.value = text;
    document.body.appendChild(tempInput);
    tempInput.select();
    document.execCommand("copy");
    document.body.removeChild(tempInput);
    showNotification("URL kopiert", `${text} kopiert`, "success");
  } catch (error) {
    console.error("[Overlay] Copy failed", error);
    showNotification("Fehler", "URL konnte nicht kopiert werden", "error");
  }
}

function handleOverlayPreview(url) {
  try {
    window.open(url, "_blank", "noopener,noreferrer");
  } catch (error) {
    console.error("[Overlay] Preview failed", error);
    showNotification("Fehler", "Preview konnte nicht geöffnet werden", "error");
  }
}
