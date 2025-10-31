const { app, BrowserWindow, ipcMain } = require("electron");
const path = require("path");
const ConfigManager = require("./config");
const TwitchAuth = require("../twitch/auth");

let mainWindow = null;
let configManager = null;
let twitchAuth = null;

configManager = new ConfigManager();
twitchAuth = new TwitchAuth(configManager);

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

app.whenReady().then(() => {
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
