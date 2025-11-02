const WebSocket = require("ws");
const fetch = require("node-fetch");

class EventSubClient {
  constructor(accessToken, userId, clientId) {
    this.accessToken = accessToken;
    this.userId = userId;
    this.clientId = clientId;
    this.ws = null;
    this.sessionId = null;
    this.reconnectAttempts = 0;
    this.maxReconnectAttempts = 5;
    this.eventHandlers = {};
  }

  connect() {
    return new Promise((resolve, reject) => {
      this.ws = new WebSocket("wss://eventsub.wss.twitch.tv/ws");

      this.ws.on("open", () => {
        console.log("[EventSub] Connected");
      });

      this.ws.on("message", async (data) => {
        const message = JSON.parse(data.toString());
        await this.handleMessage(message);

        if (message.metadata?.message_type === "session_welcome") {
          resolve();
        }
      });

      this.ws.on("close", () => {
        console.log("[EventSub] Disconnected");
        this.reconnect();
      });

      this.ws.on("error", (error) => {
        console.error("[EventSub] Error:", error.message);
        reject(error);
      });
    });
  }

  async handleMessage(message) {
    const messageType = message.metadata?.message_type;

    if (messageType === "session_welcome") {
      this.sessionId = message.payload.session.id;
      await this.subscribeToEvents();
      return;
    }

    if (messageType === "notification") {
      await this.handleNotification(message.payload);
      return;
    }

    if (messageType === "session_reconnect") {
      this.reconnect();
      return;
    }
  }

  async handleNotification(payload) {
    const event = payload.event;
    const subscriptionType = payload.subscription.type;

    if (this.eventHandlers[subscriptionType]) {
      this.eventHandlers[subscriptionType](event);
    }
  }

  async subscribeToEvents() {
    const subscriptions = [
      { type: "channel.follow", version: "2" },
      { type: "channel.subscribe", version: "1" },
      { type: "channel.subscription.gift", version: "1" },
      { type: "channel.cheer", version: "1" },
      { type: "channel.raid", version: "1" },
      {
        type: "channel.channel_points_custom_reward_redemption.add",
        version: "1",
      },
    ];

    for (const sub of subscriptions) {
      await this.subscribe(sub.type, sub.version);
    }
  }

  async subscribe(type, version = "1") {
    const condition = this.getCondition(type);

    const response = await fetch(
      "https://api.twitch.tv/helix/eventsub/subscriptions",
      {
        method: "POST",
        headers: {
          "Client-ID": this.clientId,
          Authorization: `Bearer ${this.accessToken}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          type: type,
          version: version,
          condition: condition,
          transport: {
            method: "websocket",
            session_id: this.sessionId,
          },
        }),
      }
    );

    if (!response.ok) {
      const error = await response.text();
      console.error(`[EventSub] Failed to subscribe to ${type}:`, error);
    } else {
      console.log(`[EventSub] Successfully subscribed to ${type}`);
    }
  }

  getCondition(type) {
    const conditions = {
      "channel.follow": {
        broadcaster_user_id: this.userId,
        moderator_user_id: this.userId,
      },
      "channel.raid": {
        to_broadcaster_user_id: this.userId,
      },
    };

    return conditions[type] || { broadcaster_user_id: this.userId };
  }

  on(eventType, handler) {
    this.eventHandlers[eventType] = handler;
  }

  reconnect() {
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      console.error("[EventSub] Max reconnect attempts reached");
      return;
    }

    this.reconnectAttempts++;

    setTimeout(() => {
      this.connect().catch((err) => {
        console.error("[EventSub] Reconnect failed:", err);
      });
    }, 2000 * this.reconnectAttempts);
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}

module.exports = EventSubClient;
