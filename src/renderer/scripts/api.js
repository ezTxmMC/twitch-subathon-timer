console.log("[API] Script loading...");
console.log("[API] window object:", typeof window);
console.log("[API] globalThis object:", typeof globalThis);

(function attachAPI(globalObj) {
  console.log("[API] IIFE executing, globalObj:", globalObj);
  class API {
    constructor() {
      this.baseURL = "http://gp01.kernex.host:5020/api";
    }

    setBaseURL(serverUrl) {
      if (!serverUrl || typeof serverUrl !== "string") {
        return;
      }

      const normalized = serverUrl.trim().replace(/\/$/, "");
      this.baseURL = `${normalized}/api`;
      console.log(`[API] Base URL set to ${this.baseURL}`);
    }

    getBaseURL() {
      return this.baseURL;
    }

    async request(endpoint, options = {}) {
      try {
        console.log(
          `[API] Making request to: ${this.baseURL}${endpoint}`,
          options
        );
        const response = await fetch(`${this.baseURL}${endpoint}`, {
          headers: {
            "Content-Type": "application/json",
            ...options.headers,
          },
          ...options,
        });

        console.log(`[API] Response status:`, response.status);
        if (!response.ok) {
          throw new Error(`HTTP ${response.status}`);
        }

        const data = await response.json();
        console.log(`[API] Response data:`, data);
        return data;
      } catch (error) {
        console.error(`[API] Error: ${endpoint}`, error);
        throw error;
      }
    }

    async createSession(name, initialSeconds) {
      console.log("[API] createSession called with:", { name, initialSeconds });
      return this.request("/session/create", {
        method: "POST",
        body: JSON.stringify({ name, initialSeconds }),
      });
    }

    async joinSession(code) {
      return this.request("/session/join", {
        method: "POST",
        body: JSON.stringify({ code }),
      });
    }

    async getSession(sessionId) {
      return this.request(`/session/${sessionId}`);
    }

    async getChannels(sessionId) {
      return this.request(`/channels/${sessionId}`);
    }

    async addChannel(sessionId, channelData) {
      return this.request(`/channels/${sessionId}/add`, {
        method: "POST",
        body: JSON.stringify(channelData),
      });
    }

    async removeChannel(sessionId, channelId) {
      return this.request(`/channels/${sessionId}/${channelId}`, {
        method: "DELETE",
      });
    }

    async getSettings(sessionId) {
      return this.request(`/settings/${sessionId}`);
    }

    async updateSettings(sessionId, settings) {
      return this.request(`/settings/${sessionId}`, {
        method: "PUT",
        body: JSON.stringify(settings),
      });
    }

    async getToggles(sessionId) {
      return this.request(`/event-toggles/${sessionId}`);
    }

    async updateToggles(sessionId, toggles) {
      return this.request(`/event-toggles/${sessionId}`, {
        method: "PUT",
        body: JSON.stringify(toggles),
      });
    }

    async getEvents(sessionId) {
      return this.request(`/events/${sessionId}`);
    }

    async startTimer(sessionId) {
      return this.request(`/timer/${sessionId}/start`, {
        method: "POST",
      });
    }

    async pauseTimer(sessionId) {
      return this.request(`/timer/${sessionId}/pause`, {
        method: "POST",
      });
    }

    async resetTimer(sessionId) {
      return this.request(`/timer/${sessionId}/reset`, {
        method: "POST",
      });
    }

    async addTime(sessionId, seconds, reason) {
      return this.request(`/timer/add-time`, {
        method: "POST",
        body: JSON.stringify({ sessionId, seconds, reason }),
      });
    }
  }

  const apiInstance = new API();
  console.log("[API] API instance created with baseURL:", apiInstance.baseURL);

  if (typeof module !== "undefined" && module.exports) {
    module.exports = {
      API,
      api: apiInstance,
    };
  }

  if (globalObj) {
    globalObj.API = API;
    globalObj.api = apiInstance;
    console.log("[API] API attached to global object");
  }
})(typeof window !== "undefined" ? window : globalThis);
