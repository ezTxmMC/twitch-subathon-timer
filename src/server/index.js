const express = require("express");
const cors = require("cors");
const { v4: uuidv4 } = require("uuid");

class SubathonServer {
  constructor(port = 8080) {
    this.app = express();
    this.port = port;
    this.server = null;

    // In-memory data storage
    this.sessions = new Map();
    this.channels = new Map();
    this.settings = new Map();
    this.eventToggles = new Map();
    this.events = new Map();
    this.timers = new Map();

    this.setupMiddleware();
    this.setupRoutes();
  }

  setupMiddleware() {
    this.app.use(cors());
    this.app.use(express.json());
    this.app.use((req, res, next) => {
      console.log(`[Server] ${req.method} ${req.path}`);
      next();
    });
  }

  setupRoutes() {
    // Session routes
    this.app.post("/api/sessions/create", this.createSession.bind(this));
    this.app.post("/api/sessions/join", this.joinSession.bind(this));
    this.app.get("/api/sessions/:sessionId", this.getSession.bind(this));

    // Channel routes
    this.app.get("/api/channels/:sessionId", this.getChannels.bind(this));
    this.app.post("/api/channels/:sessionId/add", this.addChannel.bind(this));
    this.app.delete(
      "/api/channels/:sessionId/:channelId",
      this.removeChannel.bind(this)
    );

    // Settings routes
    this.app.get("/api/settings/:sessionId", this.getSettings.bind(this));
    this.app.put("/api/settings/:sessionId", this.updateSettings.bind(this));

    // Event toggles routes
    this.app.get(
      "/api/event-toggles/:sessionId",
      this.getEventToggles.bind(this)
    );
    this.app.put(
      "/api/event-toggles/:sessionId",
      this.updateEventToggles.bind(this)
    );

    // Events routes
    this.app.get("/api/events/:sessionId", this.getEvents.bind(this));

    // Timer routes
    this.app.post("/api/timer/:sessionId/start", this.startTimer.bind(this));
    this.app.post("/api/timer/:sessionId/pause", this.pauseTimer.bind(this));
    this.app.post("/api/timer/:sessionId/reset", this.resetTimer.bind(this));
    this.app.post("/api/timer/:sessionId/add", this.addTime.bind(this));

    // Health check
    this.app.get("/api/health", (req, res) => {
      res.json({ status: "ok", timestamp: Date.now() });
    });
  }

  // Session management
  createSession(req, res) {
    const sessionId = uuidv4();
    const code = this.generateSessionCode();

    const session = {
      sessionId,
      code,
      createdAt: Date.now(),
      active: true,
    };

    this.sessions.set(sessionId, session);
    this.channels.set(sessionId, []);
    this.settings.set(sessionId, this.getDefaultSettings());
    this.eventToggles.set(sessionId, this.getDefaultEventToggles());
    this.events.set(sessionId, []);
    this.timers.set(sessionId, {
      seconds: 0,
      running: false,
      startTime: null,
    });

    console.log(`[Server] Created session: ${sessionId} with code: ${code}`);
    res.json(session);
  }

  joinSession(req, res) {
    const { code } = req.body;

    const session = Array.from(this.sessions.values()).find(
      (s) => s.code === code
    );

    if (!session) {
      return res.status(404).json({ error: "Session not found" });
    }

    res.json(session);
  }

  getSession(req, res) {
    const { sessionId } = req.params;
    const session = this.sessions.get(sessionId);

    if (!session) {
      return res.status(404).json({ error: "Session not found" });
    }

    res.json(session);
  }

  // Channel management
  getChannels(req, res) {
    const { sessionId } = req.params;
    const channels = this.channels.get(sessionId) || [];
    res.json(channels);
  }

  addChannel(req, res) {
    const { sessionId } = req.params;
    const channelData = req.body;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const channels = this.channels.get(sessionId) || [];
    const newChannel = {
      channelId: channelData.channelId || uuidv4(),
      channelName: channelData.channelName,
      accessToken: channelData.accessToken,
      addedAt: Date.now(),
    };

    channels.push(newChannel);
    this.channels.set(sessionId, channels);

    console.log(
      `[Server] Added channel ${newChannel.channelName} to session ${sessionId}`
    );
    res.json(newChannel);
  }

  removeChannel(req, res) {
    const { sessionId, channelId } = req.params;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const channels = this.channels.get(sessionId) || [];
    const filtered = channels.filter((c) => c.channelId !== channelId);
    this.channels.set(sessionId, filtered);

    res.json({ success: true });
  }

  // Settings management
  getSettings(req, res) {
    const { sessionId } = req.params;
    const settings = this.settings.get(sessionId) || this.getDefaultSettings();
    res.json(settings);
  }

  updateSettings(req, res) {
    const { sessionId } = req.params;
    const newSettings = req.body;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const currentSettings = this.settings.get(sessionId) || {};
    const updatedSettings = { ...currentSettings, ...newSettings };
    this.settings.set(sessionId, updatedSettings);

    res.json(updatedSettings);
  }

  // Event toggles management
  getEventToggles(req, res) {
    const { sessionId } = req.params;
    const toggles =
      this.eventToggles.get(sessionId) || this.getDefaultEventToggles();
    res.json(toggles);
  }

  updateEventToggles(req, res) {
    const { sessionId } = req.params;
    const newToggles = req.body;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const currentToggles = this.eventToggles.get(sessionId) || {};
    const updatedToggles = { ...currentToggles, ...newToggles };
    this.eventToggles.set(sessionId, updatedToggles);

    res.json(updatedToggles);
  }

  // Events management
  getEvents(req, res) {
    const { sessionId } = req.params;
    const events = this.events.get(sessionId) || [];
    res.json(events);
  }

  addEvent(sessionId, eventData) {
    const events = this.events.get(sessionId) || [];
    const newEvent = {
      eventId: uuidv4(),
      timestamp: Date.now(),
      processed: true,
      ...eventData,
    };
    events.unshift(newEvent);
    this.events.set(sessionId, events);
    return newEvent;
  }

  // Timer management
  startTimer(req, res) {
    const { sessionId } = req.params;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const timer = this.timers.get(sessionId);
    if (timer && !timer.running) {
      timer.running = true;
      timer.startTime = Date.now() - timer.seconds * 1000;
      this.timers.set(sessionId, timer);

      // Start timer interval
      this.startTimerInterval(sessionId);

      console.log(`[Server] Started timer for session ${sessionId}`);
    }

    res.json(this.timers.get(sessionId));
  }

  pauseTimer(req, res) {
    const { sessionId } = req.params;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const timer = this.timers.get(sessionId);
    if (timer && timer.running) {
      timer.running = false;
      timer.seconds = Math.floor((Date.now() - timer.startTime) / 1000);
      this.timers.set(sessionId, timer);

      console.log(`[Server] Paused timer for session ${sessionId}`);
    }

    res.json(this.timers.get(sessionId));
  }

  resetTimer(req, res) {
    const { sessionId } = req.params;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const timer = {
      seconds: 0,
      running: false,
      startTime: null,
    };
    this.timers.set(sessionId, timer);

    console.log(`[Server] Reset timer for session ${sessionId}`);
    res.json(timer);
  }

  addTime(req, res) {
    const { sessionId } = req.params;
    const { seconds, reason } = req.body;

    if (!this.sessions.has(sessionId)) {
      return res.status(404).json({ error: "Session not found" });
    }

    const timer = this.timers.get(sessionId);
    if (timer) {
      if (timer.running) {
        // If timer is running, we adjust the start time to add time
        timer.startTime -= seconds * 1000;
      } else {
        // If timer is paused, we directly add seconds
        timer.seconds += seconds;
      }
      this.timers.set(sessionId, timer);

      // Log the event
      this.addEvent(sessionId, {
        eventType: "manual",
        channelName: "System",
        username: "Manual Add",
        amount: seconds,
        addedSeconds: seconds,
        reason: reason || "Manual time add",
      });

      console.log(
        `[Server] Added ${seconds}s to timer for session ${sessionId}`
      );
    }

    res.json(this.timers.get(sessionId));
  }

  startTimerInterval(sessionId) {
    // This would normally be handled by a WebSocket or polling
    // For now, we just update the timer state
    if (!this.timerIntervals) {
      this.timerIntervals = new Map();
    }

    if (this.timerIntervals.has(sessionId)) {
      clearInterval(this.timerIntervals.get(sessionId));
    }

    const interval = setInterval(() => {
      const timer = this.timers.get(sessionId);
      if (timer && timer.running) {
        timer.seconds = Math.floor((Date.now() - timer.startTime) / 1000);
        this.timers.set(sessionId, timer);
      } else {
        clearInterval(interval);
        this.timerIntervals.delete(sessionId);
      }
    }, 1000);

    this.timerIntervals.set(sessionId, interval);
  }

  // Helper methods
  generateSessionCode() {
    return Math.random().toString(36).substring(2, 8).toUpperCase();
  }

  getDefaultSettings() {
    return {
      followTime: 30,
      subTime: 300,
      giftSubTime: 300,
      bitsPerSecond: 10,
      raidPerViewer: 1,
    };
  }

  getDefaultEventToggles() {
    return {
      follows: true,
      subs: true,
      giftSubs: true,
      bits: true,
      raids: true,
    };
  }

  start() {
    return new Promise((resolve, reject) => {
      this.server = this.app.listen(this.port, (err) => {
        if (err) {
          console.error(`[Server] Failed to start:`, err);
          reject(err);
        } else {
          console.log(`[Server] Running on http://localhost:${this.port}`);
          resolve(this.server);
        }
      });
    });
  }

  stop() {
    return new Promise((resolve) => {
      if (this.server) {
        this.server.close(() => {
          console.log("[Server] Stopped");
          resolve();
        });
      } else {
        resolve();
      }
    });
  }
}

module.exports = SubathonServer;
