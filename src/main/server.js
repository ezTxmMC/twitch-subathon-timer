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
      this.app.use(express.json());
      this.app.use(express.urlencoded({ extended: true }));

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

      const overlayPath = path.join(__dirname, "../overlay");
      this.app.use("/overlay", express.static(overlayPath));

      this.server = http.createServer(this.app);
      this.wss = new WebSocket.Server({ server: this.server, path: "/ws" });
      this.setupWebSocket();

      this.server.listen(port, () => {
        console.log(`[Server] HTTP server started on port ${port}`);
        console.log(`[Server] Overlays: http://localhost:${port}/overlay/`);
        console.log(`[Server] WebSocket: ws://localhost:${port}/ws`);
        console.log(`[Server] External API: http://gp01.kernex.host:5020/api`);
        resolve(port);
      });

      this.server.on("error", (error) => {
        console.error("[Server] Failed to start:", error);
        reject(error);
      });
    });
  }

  setupWebSocket() {
    this.wss.on("connection", (ws) => {
      console.log("[WebSocket] Client connected:", this.clients.size + 1);
      this.clients.add(ws);

      ws.on("message", (message) => {
        try {
          const data = JSON.parse(message);
          console.log("[WebSocket] Received:", data);
          if (data.type === "ping") {
            ws.send(JSON.stringify({ type: "pong" }));
          }
        } catch (error) {
          console.error("[WebSocket] Invalid message:", error);
        }
      });

      ws.on("close", () => {
        console.log("[WebSocket] Client disconnected:", this.clients.size - 1);
        this.clients.delete(ws);
      });

      ws.on("error", (error) => {
        console.error("[WebSocket] Error:", error);
        this.clients.delete(ws);
      });

      ws.send(
        JSON.stringify({
          type: "connected",
          message: "Connected to Subathon Timer WebSocket",
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
      this.clients.forEach((client) => client.close());
      this.clients.clear();
      if (this.wss) this.wss.close();
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
