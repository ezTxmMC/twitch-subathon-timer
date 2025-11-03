// Login page setup and handlers

console.log("[Login] Script loaded");

// eslint-disable-next-line no-unused-vars
function setupLoginPage() {
  console.log("[Login] Setting up login page");

  const loginBtn = document.getElementById("login-twitch-btn");

  if (loginBtn) {
    loginBtn.addEventListener("click", handleLoginClick);
  }
}

async function handleLoginClick() {
  console.log("[Login] Login button clicked");

  const loginView = document.getElementById("login-view");
  const loadingView = document.getElementById("loading-view");

  // Show loading state
  if (loginView) loginView.style.display = "none";
  if (loadingView) loadingView.style.display = "flex";

  try {
    const result = await electronAPI.twitch.login();

    if (result.success) {
      console.log("[Login] Login successful:", result.user);
      _twitchUser = result.user;
      _isLoggedIn = true;

      // Switch to main app view WITHOUT reload
      showMainApp();
    } else {
      throw new Error(result.error || "Login failed");
    }
  } catch (error) {
    console.error("[Login] Login error:", error);

    // Show error and go back to login view
    if (loginView) loginView.style.display = "flex";
    if (loadingView) loadingView.style.display = "none";

    alert("Fehler beim Login: " + error.message);
  }
}
