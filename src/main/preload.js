const { contextBridge, ipcRenderer } = require('electron');

contextBridge.exposeInMainWorld('electronAPI', {
    config: {
        get: (key) => ipcRenderer.invoke('config:get', key),
        getAll: () => ipcRenderer.invoke('config:getAll'),
        set: (key, value) => ipcRenderer.invoke('config:set', key, value),
        reset: () => ipcRenderer.invoke('config:reset')
    },

    twitch: {
        login: () => ipcRenderer.invoke('twitch:login'),
        logout: () => ipcRenderer.invoke('twitch:logout'),
        getUser: () => ipcRenderer.invoke('twitch:getUser')
    },

    chat: {
        joinChannel: (channelName) => ipcRenderer.invoke('chat:joinChannel', channelName),
        leaveChannel: (channelName) => ipcRenderer.invoke('chat:leaveChannel', channelName),
        sendMessage: (channelName, message) => ipcRenderer.invoke('chat:sendMessage', channelName, message),
        getChannels: () => ipcRenderer.invoke('chat:getChannels')
    },

    window: {
        minimize: () => ipcRenderer.invoke('window:minimize'),
        maximize: () => ipcRenderer.invoke('window:maximize'),
        close: () => ipcRenderer.invoke('window:close')
    },

    on: (channel, callback) => {
        const validChannels = ['app-ready', 'twitch-event', 'timer-update', 'chat-message'];
        if (validChannels.includes(channel)) {
            ipcRenderer.on(channel, (event, ...args) => callback(...args));
        }
    },

    removeListener: (channel, callback) => {
        ipcRenderer.removeListener(channel, callback);
    }
});
