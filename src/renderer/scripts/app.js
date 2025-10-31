// Global shared state - accessible by all page modules
let _currentPage = "dashboard";
let _appConfig = null;
let _currentSession = null;
let _twitchUser = null;

document.addEventListener("DOMContentLoaded", () => {
  setupNavigation();

  electronAPI.on("app-ready", (config) => {
    _appConfig = config;
    console.log("[App] Ready");
    if (config?.server?.url && typeof api?.setBaseURL === "function") {
      api.setBaseURL(config.server.url);
    }
    loadPage("dashboard");
  });
});

function setupNavigation() {
  const navItems = document.querySelectorAll(".nav-item");

  navItems.forEach((item) => {
    item.addEventListener("click", () => {
      const page = item.dataset.page;

      navItems.forEach((i) => i.classList.remove("active"));
      item.classList.add("active");

      loadPage(page);
    });
  });
}

async function loadPage(pageName) {
  __currentPage = pageName;

  const container = document.getElementById("page-container");

  try {
    const response = await fetch(`pages/${pageName}.html`);
    const html = await response.text();
    container.innerHTML = html;

    // Wait for the DOM to be ready before initializing the page
    // This ensures all elements exist when setup functions try to attach listeners
    await new Promise((resolve) => setTimeout(resolve, 0));
    await initializePage(pageName);
  } catch (error) {
    console.error(`[App] Failed to load page: ${pageName}`, error);
    container.innerHTML = "<h1>Page not found</h1>";
  }
}

async function initializePage(pageName) {
  // Page setup functions are defined in their respective modular files
  // and will be called based on the page name
  let setupFn = null;
  switch (pageName) {
    case "session":
      if (typeof setupSessionPage === "function") setupFn = setupSessionPage;
      break;
    case "channels":
      if (typeof setupChannelsPage === "function") setupFn = setupChannelsPage;
      break;
    case "twitch":
      if (typeof setupTwitchPage === "function") setupFn = setupTwitchPage;
      break;
    case "timer":
      if (typeof setupTimerPage === "function") setupFn = setupTimerPage;
      break;
    case "settings":
      if (typeof setupSettingsPage === "function") setupFn = setupSettingsPage;
      break;
    case "events":
      if (typeof setupEventsPage === "function") setupFn = setupEventsPage;
      break;
    case "alerts":
      if (typeof setupAlertsPage === "function") setupFn = setupAlertsPage;
      break;
    case "chat":
      if (typeof setupChatPage === "function") setupFn = setupChatPage;
      break;
    case "app-settings":
      if (typeof setupAppConfigPage === "function")
        setupFn = setupAppConfigPage;
      break;
    case "overlay":
      if (typeof setupOverlayPage === "function") setupFn = setupOverlayPage;
      break;
    case "dashboard":
      // Dashboard doesn't need setup yet
      break;
  }

  if (typeof setupFn === "function") {
    setupFn();
  }
}
