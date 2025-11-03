const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");
const ConfigManager = require("./config");
const TwitchAuth = require("../twitch/auth");
const IntegratedServer = require("./server");

let mainWindow = null;
let configManager = null;
let twitchAuth = null;
let server = null;

configManager = new ConfigManager();
twitchAuth = new TwitchAuth(configManager);
server = new IntegratedServer(configManager);

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
    mainWindow.webContents.openDevTools({ mode: "detach" });
  }

  mainWindow.on("closed", () => {
    mainWindow = null;
  });
}

app.whenReady().then(async () => {
  createWindow();

  // Try to restore persisted authentication
  const persistedUser = await twitchAuth.loadPersistedAuth();

  // Start integrated server
  try {
    const config = configManager.getAll();
    const port = config?.server?.port || 8080;
    await server.start(port);
    console.log(`[Main] Server started successfully on port ${port}`);
  } catch (error) {
    console.error("[Main] Failed to start server:", error);
  }

  app.on("activate", () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow();
    }
  });

  setTimeout(() => {
    if (mainWindow) {
      mainWindow.webContents.send("app-ready", {
        config: configManager.getAll(),
        user: persistedUser, // Send persisted user if available
      });
    }
  }, 1000);
});

app.on("window-all-closed", async () => {
  // Stop server before quitting
  if (server) {
    await server.stop();
  }

  if (process.platform !== "darwin") {
    app.quit();
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
    console.error("[Main] Twitch login error:", error);
    return { success: false, error: error.message };
  }
});

ipcMain.handle("twitch:logout", async () => {
  try {
    await twitchAuth.logout();
    return { success: true };
  } catch (error) {
    console.error("[Main] Twitch logout error:", error);
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

// Server event broadcasting
ipcMain.handle("server:broadcast", (event, data) => {
  if (server) {
    server.broadcast(data);
    return { success: true };
  }
  return { success: false, error: "Server not running" };
});
