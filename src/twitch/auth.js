const crypto = require("crypto");
const http = require("http");
const { shell } = require("electron");
const fetch = require("node-fetch");
const EventSubClient = require("./eventsub");
const TwitchChatClient = require("./chat");

class TwitchAuth {
  constructor(configManager) {
    this.config = configManager;
    this.currentUser = null;
    this.eventSubClient = null;
    this.chatClient = null;
    this.callbackServer = null;
    this.authState = null;
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

        await this.connectEventSub(token, user.id);
        await this.connectChat(user.login, token);

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

  async connectEventSub(token, userId) {
    if (this.eventSubClient) {
      this.eventSubClient.disconnect();
    }

    const clientId = this.config.get("twitch.clientId");
    this.eventSubClient = new EventSubClient(token, userId, clientId);
    await this.eventSubClient.connect();
    console.log("[Auth] EventSub connected");
  }

  async connectChat(username, token) {
    if (this.chatClient) {
      this.chatClient.disconnect();
    }

    this.chatClient = new TwitchChatClient();
    await this.chatClient.connect(username, token);
    console.log("[Auth] Chat connected");
  }

  async logout() {
    if (this.eventSubClient) {
      this.eventSubClient.disconnect();
      this.eventSubClient = null;
    }

    if (this.chatClient) {
      this.chatClient.disconnect();
      this.chatClient = null;
    }

    this.currentUser = null;

    if (this.callbackServer) {
      this.callbackServer.close();
      this.callbackServer = null;
    }
  }

  getCurrentUser() {
    return this.currentUser;
  }

  getEventSubClient() {
    return this.eventSubClient;
  }

  getChatClient() {
    return this.chatClient;
  }
}

module.exports = TwitchAuth;
