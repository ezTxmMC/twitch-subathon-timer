const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");
const ConfigManager = require("./config");
const TwitchAuth = require("../twitch/auth");
const SubathonServer = require("../server");

let mainWindow = null;
let configManager = null;
let twitchAuth = null;
let server = null;

configManager = new ConfigManager();
twitchAuth = new TwitchAuth(configManager);
server = new SubathonServer(8080);

function createWindow() {
  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 1200,
    minHeight: 700,
    frame: true,
    autoHideMenuBar: true,
    backgroundColor: "#0f0f0f",
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      preload: path.join(__dirname, "preload.js"),
    },
  });

  mainWindow.loadFile(path.join(__dirname, "../renderer/index.html"));

  if (process.env.NODE_ENV === "development") {
    mainWindow.webContents.openDevTools();
  }

  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}

app.whenReady().then(async () => {
  // Start the API server
  try {
    await server.start();
    console.log("[Main] API server started successfully");
  } catch (error) {
    console.error("[Main] Failed to start API server:", error);
  }

  createWindow();

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });

  setTimeout(() => {
    if (mainWindow) {
      mainWindow.webContents.send("app-ready", {
        config: configManager.getAll(),
        server: { url: "http://localhost:8080" },
      });

      const chatClient = twitchAuth.getChatClient();
      if (chatClient) {
        chatClient.onMessage((message) => {
          mainWindow.webContents.send("chat-message", message);
        });
      }
    }
  }, 1000);
});

app.on("window-all-closed", () => {
  if (process.platform !== "darwin") {
    // Stop the server before quitting
    if (server) {
      server
        .stop()
        .then(() => {
          app.quit();
        })
        .catch((error) => {
          console.error("[Main] Error stopping server:", error);
          app.quit();
        });
    } else {
      app.quit();
    }
  }
});

ipcMain.handle("config:get", (event, key) => {
  return configManager.get(key);
});

ipcMain.handle("config:getAll", () => {
  return configManager.getAll();
});

ipcMain.handle("config:set", (event, key, value) => {
  configManager.set(key, value);
  return { success: true };
});

ipcMain.handle("config:reset", () => {
  configManager.reset();
  return { success: true };
});

ipcMain.handle("twitch:login", async () => {
  try {
    const user = await twitchAuth.login();
    return { success: true, user };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle("twitch:logout", async () => {
  try {
    await twitchAuth.logout();
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle("twitch:getUser", () => {
  const user = twitchAuth.getCurrentUser();
  return user ? { success: true, user } : { success: false };
});

ipcMain.handle("window:minimize", () => {
  if (mainWindow) mainWindow.minimize();
});

ipcMain.handle("window:maximize", () => {
  if (mainWindow) {
    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize();
    } else {
      mainWindow.maximize();
    }
  }
});

ipcMain.handle("window:close", () => {
  if (mainWindow) mainWindow.close();
});

ipcMain.handle("chat:joinChannel", async (event, channelName) => {
  try {
    const chatClient = twitchAuth.getChatClient();
    if (!chatClient) {
      return { success: false, error: "Chat not connected" };
    }

    await chatClient.joinChannel(channelName);
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle("chat:leaveChannel", async (event, channelName) => {
  try {
    const chatClient = twitchAuth.getChatClient();
    if (!chatClient) {
      return { success: false };
    }

    await chatClient.leaveChannel(channelName);
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle("chat:sendMessage", async (event, channelName, message) => {
  try {
    const chatClient = twitchAuth.getChatClient();
    if (!chatClient) {
      return { success: false, error: "Chat not connected" };
    }

    await chatClient.sendMessage(channelName, message);
    return { success: true };
  } catch (error) {
    return { success: false, error: error.message };
  }
});

ipcMain.handle("chat:getChannels", () => {
  const chatClient = twitchAuth.getChatClient();
  if (!chatClient) {
    return { success: false, channels: [] };
  }

  return { success: true, channels: chatClient.getChannels() };
});
