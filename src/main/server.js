const express = require("express");
const http = require("http");
const WebSocket = require("ws");
const path = require("path");

class IntegratedServer {
  constructor(configManager) {
    this.configManager = configManager;
    this.app = express();
    this.server = null;
    this.wss = null;
    this.clients = new Set();
  }

  start(port = 8080) {
    return new Promise((resolve, reject) => {
      // Middleware
      this.app.use(express.json());
      this.app.use(express.urlencoded({ extended: true }));

      // Enable CORS for API requests
      this.app.use((req, res, next) => {
        res.header("Access-Control-Allow-Origin", "*");
        res.header(
          "Access-Control-Allow-Methods",
          "GET, POST, PUT, DELETE, OPTIONS"
        );
        res.header("Access-Control-Allow-Headers", "Content-Type");
        if (req.method === "OPTIONS") {
          return res.sendStatus(200);
        }
        next();
      });

      // Serve overlay static files
      const overlayPath = path.join(__dirname, "../overlay");
      this.app.use("/overlay", express.static(overlayPath));

      // API Routes
      this.setupAPIRoutes();

      // Create HTTP server
      this.server = http.createServer(this.app);

      // Setup WebSocket server
      this.wss = new WebSocket.Server({ server: this.server, path: "/ws" });
      this.setupWebSocket();

      // Start listening
      this.server.listen(port, () => {
        console.log(`[Server] HTTP server started on port ${port}`);
        console.log(
          `[Server] Overlays available at http://localhost:${port}/overlay/`
        );
        console.log(
          `[Server] WebSocket server ready at ws://localhost:${port}/ws`
        );
        resolve(port);
      });

      this.server.on("error", (error) => {
        console.error("[Server] Failed to start:", error);
        reject(error);
      });
    });
  }

  setupAPIRoutes() {
    // Session endpoints
    this.app.post("/api/session/create", (req, res) => {
      const { name, initialSeconds } = req.body;
      console.log("[Server API] Create session:", { name, initialSeconds });

      const session = {
        id: Date.now().toString(),
        sessionId: Date.now().toString(),
        code: Math.random().toString(36).substring(2, 8).toUpperCase(),
        name,
        initialSeconds: parseInt(initialSeconds) || 300,
        remainingSeconds: parseInt(initialSeconds) || 300,
        goalMinutes: Math.floor((parseInt(initialSeconds) || 300) / 60),
        currentMinutes: 0,
        running: false,
        isActive: false,
        ownerName: "You",
        createdAt: new Date().toISOString(),
      };

      res.json(session);
    });

    this.app.post("/api/session", (req, res) => {
      const { name, goalMinutes } = req.body;
      console.log("[Server API] Create session (legacy):", {
        name,
        goalMinutes,
      });

      const session = {
        id: Date.now().toString(),
        name,
        goalMinutes: parseInt(goalMinutes),
        currentMinutes: 0,
        isActive: false,
        createdAt: new Date().toISOString(),
      };

      res.json(session);
    });

    this.app.post("/api/session/join", (req, res) => {
      const { code } = req.body;
      console.log("[Server API] Join session:", code);

      // Mock response - in real app, would look up session by code
      res.json({
        id: Date.now().toString(),
        sessionId: Date.now().toString(),
        code: code,
        name: "Joined Session",
        initialSeconds: 3600,
        remainingSeconds: 1800,
        goalMinutes: 60,
        currentMinutes: 30,
        running: false,
        isActive: true,
        ownerName: "Host",
        createdAt: new Date().toISOString(),
      });
    });

    // Timer endpoints
    this.app.get("/api/timer/:sessionId", (req, res) => {
      const { sessionId } = req.params;
      console.log("[Server API] Get timer status:", sessionId);

      // Mock timer state - in real app, would fetch from database
      res.json({
        sessionId: sessionId,
        remainingSeconds: 1800, // 30 minutes
        running: false,
        lastUpdate: Date.now(),
      });
    });

    this.app.post("/api/timer/:sessionId/start", (req, res) => {
      const { sessionId } = req.params;
      console.log("[Server API] Start timer:", sessionId);

      // Broadcast timer start to overlays
      this.broadcast({
        type: "timer-start",
        timestamp: Date.now(),
      });

      res.json({ success: true });
    });

    this.app.post("/api/timer/:sessionId/pause", (req, res) => {
      const { sessionId } = req.params;
      console.log("[Server API] Pause timer:", sessionId);

      this.broadcast({
        type: "timer-pause",
        timestamp: Date.now(),
      });

      res.json({ success: true });
    });

    this.app.post("/api/timer/:sessionId/reset", (req, res) => {
      const { sessionId } = req.params;
      console.log("[Server API] Reset timer:", sessionId);

      this.broadcast({
        type: "timer-reset",
        timestamp: Date.now(),
      });

      res.json({ success: true });
    });

    this.app.post("/api/timer/:sessionId/add", (req, res) => {
      const { sessionId } = req.params;
      const { seconds } = req.body;
      console.log("[Server API] Add time:", sessionId, seconds);

      this.broadcast({
        type: "timer-add",
        seconds: parseInt(seconds),
        timestamp: Date.now(),
      });

      res.json({ success: true, seconds: parseInt(seconds) });
    });

    // Channels endpoint
    this.app.get("/api/channels", (req, res) => {
      console.log("[API] Get channels");

      // Mock response - in real app, would get from config/database
      res.json([
        { id: "1", name: "channel1", connected: true },
        { id: "2", name: "channel2", connected: false },
      ]);
    });

    // Settings endpoint
    this.app.post("/api/settings", (req, res) => {
      console.log("[API] Update settings:", req.body);
      res.json({ success: true });
    });

    // Events endpoint
    this.app.post("/api/events", (req, res) => {
      const event = req.body;
      console.log("[API] New event:", event);

      // Broadcast event to overlays
      this.broadcast({
        type: "event-alert",
        eventType: event.type,
        username: event.username,
        tier: event.tier,
        amount: event.amount,
        bits: event.bits,
        viewers: event.viewers,
        timestamp: Date.now(),
      });

      res.json({ success: true });
    });
  }

  setupWebSocket() {
    this.wss.on("connection", (ws) => {
      console.log(
        "[WebSocket] Client connected. Total clients:",
        this.clients.size + 1
      );
      this.clients.add(ws);

      ws.on("message", (message) => {
        try {
          const data = JSON.parse(message);
          console.log("[WebSocket] Received message:", data);

          // Handle client messages if needed
          if (data.type === "ping") {
            ws.send(JSON.stringify({ type: "pong" }));
          }
        } catch (error) {
          console.error("[WebSocket] Invalid message:", error);
        }
      });

      ws.on("close", () => {
        console.log(
          "[WebSocket] Client disconnected. Total clients:",
          this.clients.size - 1
        );
        this.clients.delete(ws);
      });

      ws.on("error", (error) => {
        console.error("[WebSocket] Client error:", error);
        this.clients.delete(ws);
      });

      // Send welcome message
      ws.send(
        JSON.stringify({
          type: "connected",
          message: "Connected to Subathon Timer server",
          timestamp: Date.now(),
        })
      );
    });
  }

  broadcast(data) {
    const message = JSON.stringify(data);
    let sentCount = 0;

    this.clients.forEach((client) => {
      if (client.readyState === WebSocket.OPEN) {
        client.send(message);
        sentCount++;
      }
    });

    console.log(`[WebSocket] Broadcast to ${sentCount} clients:`, data.type);
  }

  stop() {
    return new Promise((resolve) => {
      console.log("[Server] Shutting down...");

      // Close all WebSocket connections
      this.clients.forEach((client) => {
        client.close();
      });
      this.clients.clear();

      // Close WebSocket server
      if (this.wss) {
        this.wss.close();
      }

      // Close HTTP server
      if (this.server) {
        this.server.close(() => {
          console.log("[Server] Shutdown complete");
          resolve();
        });
      } else {
        resolve();
      }
    });
  }
}

module.exports = IntegratedServer;
