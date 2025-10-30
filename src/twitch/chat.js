const tmi = require('tmi.js');

class TwitchChatClient {
    constructor() {
        this.client = null;
        this.channels = [];
        this.messageHandlers = [];
        this.connected = false;
    }

    connect(username, token) {
        return new Promise((resolve, reject) => {
            this.client = new tmi.Client({
                options: { debug: false },
                connection: {
                    reconnect: true,
                    secure: true
                },
                identity: {
                    username: username,
                    password: `oauth:${token}`
                },
                channels: []
            });

            this.client.on('connected', () => {
                this.connected = true;
                console.log('[Chat] Connected');
                resolve();
            });

            this.client.on('message', (channel, tags, message, self) => {
                if (self) return;

                const chatMessage = {
                    channel: channel.replace('#', ''),
                    username: tags['display-name'] || tags.username,
                    message: message,
                    color: tags.color,
                    badges: tags.badges,
                    timestamp: Date.now(),
                    id: tags.id,
                    userId: tags['user-id'],
                    isMod: tags.mod,
                    isSubscriber: tags.subscriber,
                    isVip: tags.badges?.vip === '1'
                };

                this.messageHandlers.forEach(handler => handler(chatMessage));
            });

            this.client.on('disconnected', (reason) => {
                this.connected = false;
                console.log('[Chat] Disconnected:', reason);
            });

            this.client.on('notice', (channel, msgid, message) => {
                console.log('[Chat] Notice:', message);
            });

            this.client.on('reconnect', () => {
                console.log('[Chat] Reconnecting...');
            });

            this.client.connect().catch(reject);
        });
    }

    async joinChannel(channelName) {
        if (!this.client || !this.connected) {
            throw new Error('Chat client not connected');
        }

        const channel = channelName.toLowerCase();

        if (this.channels.includes(channel)) {
            return;
        }

        await this.client.join(channel);
        this.channels.push(channel);
        console.log(`[Chat] Joined ${channel}`);
    }

    async leaveChannel(channelName) {
        if (!this.client || !this.connected) {
            return;
        }

        const channel = channelName.toLowerCase();

        if (!this.channels.includes(channel)) {
            return;
        }

        await this.client.part(channel);
        this.channels = this.channels.filter(c => c !== channel);
        console.log(`[Chat] Left ${channel}`);
    }

    async sendMessage(channelName, message) {
        if (!this.client || !this.connected) {
            throw new Error('Chat client not connected');
        }

        const channel = channelName.toLowerCase();

        if (!this.channels.includes(channel)) {
            throw new Error(`Not connected to channel: ${channel}`);
        }

        await this.client.say(channel, message);
    }

    onMessage(handler) {
        this.messageHandlers.push(handler);
    }

    removeMessageHandler(handler) {
        this.messageHandlers = this.messageHandlers.filter(h => h !== handler);
    }

    getChannels() {
        return [...this.channels];
    }

    isConnected() {
        return this.connected;
    }

    disconnect() {
        if (this.client) {
            this.client.disconnect();
            this.client = null;
            this.channels = [];
            this.connected = false;
            console.log('[Chat] Disconnected');
        }
    }
}

module.exports = TwitchChatClient;
