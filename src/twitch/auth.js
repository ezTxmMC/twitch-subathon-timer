const crypto = require("crypto");
const http = require("http");
const { shell } = require("electron");
const fetch = require("electron-fetch").default;
const fs = require("fs");
const path = require("path");
const os = require("os");

class TwitchAuth {
  constructor(configManager) {
    this.config = configManager;
    this.currentUser = null;
    this.callbackServer = null;
    this.authState = null;
    this.authDir = path.join(os.homedir(), "AppData", "Local", "subathon");
    this.authPath = path.join(this.authDir, "auth.json");

    // Ensure auth directory exists
    if (!fs.existsSync(this.authDir)) {
      fs.mkdirSync(this.authDir, { recursive: true });
    }
  }

  async login() {
    return new Promise((resolve, reject) => {
      const clientId = this.config.get("twitch.clientId");
      const redirectUri = this.config.get("twitch.redirectUri");
      const scopes = this.config.get("twitch.scopes");

      this.authState = crypto.randomBytes(16).toString("hex");

      const authUrl = new URL("https://id.twitch.tv/oauth2/authorize");
      authUrl.searchParams.append("response_type", "code");
      authUrl.searchParams.append("client_id", clientId);
      authUrl.searchParams.append("redirect_uri", redirectUri);
      authUrl.searchParams.append("scope", scopes.join(" "));
      authUrl.searchParams.append("state", this.authState);

      shell.openExternal(authUrl.toString());

      this.startCallbackServer(resolve, reject);
    });
  }

  // Load persisted authentication
  async loadPersistedAuth() {
    try {
      if (fs.existsSync(this.authPath)) {
        const data = fs.readFileSync(this.authPath, "utf8");
        const authData = JSON.parse(data);

        // Validate token is still valid
        const isValid = await this.validateToken(authData.accessToken);

        if (isValid) {
          this.currentUser = authData;
          console.log("[Auth] Restored session for:", authData.displayName);
          return authData;
        } else {
          console.log("[Auth] Token expired, deleting persisted auth");
          this.deletePersistedAuth();
          return null;
        }
      }
    } catch (error) {
      console.error("[Auth] Failed to load persisted auth:", error);
      this.deletePersistedAuth();
    }
    return null;
  }

  // Save authentication to disk
  savePersistedAuth(userData) {
    try {
      fs.writeFileSync(
        this.authPath,
        JSON.stringify(userData, null, 2),
        "utf8"
      );
      console.log("[Auth] Authentication persisted");
    } catch (error) {
      console.error("[Auth] Failed to save auth:", error);
    }
  }

  // Delete persisted authentication
  deletePersistedAuth() {
    try {
      if (fs.existsSync(this.authPath)) {
        fs.unlinkSync(this.authPath);
        console.log("[Auth] Persisted auth deleted");
      }
    } catch (error) {
      console.error("[Auth] Failed to delete persisted auth:", error);
    }
  }

  // Validate if token is still valid
  async validateToken(token) {
    try {
      const response = await fetch("https://id.twitch.tv/oauth2/validate", {
        headers: {
          Authorization: `OAuth ${token}`,
        },
      });
      return response.ok;
    } catch (error) {
      console.error("[Auth] Token validation failed:", error);
      return false;
    }
  }

  startCallbackServer(resolve, reject) {
    const port = new URL(this.config.get("twitch.redirectUri")).port;

    this.callbackServer = http.createServer(async (req, res) => {
      const url = new URL(req.url, `http://localhost:${port}`);
      const code = url.searchParams.get("code");
      const state = url.searchParams.get("state");

      if (state !== this.authState) {
        res.writeHead(400);
        res.end("Invalid state");
        reject(new Error("Invalid state"));
        return;
      }

      if (!code) {
        res.writeHead(400);
        res.end("No code provided");
        reject(new Error("No code provided"));
        return;
      }

      try {
        const token = await this.exchangeCodeForToken(code);
        const user = await this.fetchUserInfo(token);

        this.currentUser = {
          ...user,
          accessToken: token,
        };

        // Save authentication for persistence
        this.savePersistedAuth(this.currentUser);

        res.writeHead(200, { "Content-Type": "text/html" });
        res.end(
          "<html><body><h1>Login erfolgreich!</h1><p>Du kannst dieses Fenster schließen.</p></body></html>"
        );

        this.callbackServer.close();
        resolve(this.currentUser);
      } catch (error) {
        res.writeHead(500);
        res.end("Error during authentication");
        reject(error);
      }
    });

    this.callbackServer.listen(port);
  }

  async exchangeCodeForToken(code) {
    const clientId = this.config.get("twitch.clientId");
    const clientSecret = this.config.get("twitch.clientSecret");
    const redirectUri = this.config.get("twitch.redirectUri");

    if (!clientSecret) {
      throw new Error(
        "Twitch Client Secret not configured! Add to .env or config.json"
      );
    }

    const response = await fetch("https://id.twitch.tv/oauth2/token", {
      method: "POST",
      headers: { "Content-Type": "application/x-www-form-urlencoded" },
      body: new URLSearchParams({
        client_id: clientId,
        client_secret: clientSecret,
        code: code,
        grant_type: "authorization_code",
        redirect_uri: redirectUri,
      }),
    });

    if (!response.ok) {
      const error = await response.text();
      console.error("[Auth] Token exchange failed:", error);
      throw new Error("Token exchange failed");
    }

    const data = await response.json();
    return data.access_token;
  }

  async fetchUserInfo(token) {
    const clientId = this.config.get("twitch.clientId");

    const response = await fetch("https://api.twitch.tv/helix/users", {
      headers: {
        Authorization: `Bearer ${token}`,
        "Client-Id": clientId,
      },
    });

    if (!response.ok) {
      throw new Error("Failed to fetch user info");
    }

    const data = await response.json();
    const user = data.data[0];

    return {
      id: user.id,
      login: user.login,
      displayName: user.display_name,
      profileImageUrl: user.profile_image_url,
    };
  }

  async logout() {
    this.currentUser = null;

    if (this.callbackServer) {
      this.callbackServer.close();
      this.callbackServer = null;
    }

    // Delete persisted authentication
    this.deletePersistedAuth();
  }

  getCurrentUser() {
    return this.currentUser;
  }
}

module.exports = TwitchAuth;
