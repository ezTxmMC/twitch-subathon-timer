// Global shared state - accessible by all page modules
let _currentPage = "session";
let _appConfig = null;
let _currentSession = null;
let _twitchUser = null;
let _isLoggedIn = false;

// Register app-ready listener before DOMContentLoaded
electronAPI.on("app-ready", (data) => {
  _appConfig = data.config;
  console.log("[App] Ready", data);

  if (data.config?.server?.url && typeof api?.setBaseURL === "function") {
    api.setBaseURL(data.config.server.url);
  }

  // Check if user is already logged in
  if (data.user) {
    _twitchUser = data.user;
    _isLoggedIn = true;
    console.log("[App] User already logged in:", _twitchUser);

    // Wait for DOM to be ready before showing main app
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", () => showMainApp());
    } else {
      showMainApp();
    }
  } else {
    console.log("[App] No user found, showing login");

    // Wait for DOM to be ready before showing login
    if (document.readyState === "loading") {
      document.addEventListener("DOMContentLoaded", () => showLoginScreen());
    } else {
      showLoginScreen();
    }
  }
});

function showLoginScreen() {
  const container = document.getElementById("page-container");
  const sidebar = document.querySelector(".sidebar");

  // Hide sidebar
  if (sidebar) sidebar.style.display = "none";

  // Load login page
  fetch("pages/login.html")
    .then((response) => response.text())
    .then((html) => {
      container.innerHTML = html;
      setTimeout(() => {
        if (typeof setupLoginPage === "function") {
          setupLoginPage();
        }
      }, 0);
    })
    .catch((error) => {
      console.error("[App] Failed to load login page:", error);
      container.innerHTML = "<h1>Failed to load login</h1>";
    });
}

function showMainApp() {
  const sidebar = document.querySelector(".sidebar");

  // Show sidebar
  if (sidebar) sidebar.style.display = "flex";

  // Show user info in sidebar
  updateSidebarUserInfo();

  setupNavigation();
  setupLogoutButton();
  loadPage("session");
}

function updateSidebarUserInfo() {
  const userInfo = document.getElementById("sidebar-user-info");
  const avatar = document.getElementById("sidebar-avatar");
  const username = document.getElementById("sidebar-username");

  if (_twitchUser && userInfo) {
    userInfo.style.display = "flex";
    if (avatar) avatar.src = _twitchUser.profileImageUrl;
    if (username) username.textContent = _twitchUser.displayName;
  }
}

function setupLogoutButton() {
  const logoutBtn = document.getElementById("sidebar-logout-btn");

  if (logoutBtn) {
    logoutBtn.addEventListener("click", async () => {
      if (confirm("Möchtest du dich wirklich abmelden?")) {
        try {
          await electronAPI.twitch.logout();
          _twitchUser = null;
          _isLoggedIn = false;
          _currentSession = null; // Clear session on logout

          // Clean up any running intervals
          if (
            typeof sessionTimerInterval !== "undefined" &&
            sessionTimerInterval
          ) {
            clearInterval(sessionTimerInterval);
          }
          if (typeof timerInterval !== "undefined" && timerInterval) {
            clearInterval(timerInterval);
          }

          showLoginScreen();
        } catch (error) {
          console.error("[App] Logout failed:", error);
          alert("Fehler beim Abmelden");
        }
      }
    });
  }
}

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
  _currentPage = pageName;

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
    case "timer":
      if (typeof setupTimerPage === "function") setupFn = setupTimerPage;
      break;
    case "overlay":
      if (typeof setupOverlayPage === "function") setupFn = setupOverlayPage;
      break;
    case "login":
      if (typeof setupLoginPage === "function") setupFn = setupLoginPage;
      break;
  }

  if (typeof setupFn === "function") {
    setupFn();
  }
}
