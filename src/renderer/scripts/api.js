class API {
    constructor() {
        this.baseURL = 'http://localhost:8080/api';
    }

    async request(endpoint, options = {}) {
        try {
            const response = await fetch(`${this.baseURL}${endpoint}`, {
                headers: {
                    'Content-Type': 'application/json',
                    ...options.headers
                },
                ...options
            });

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            return await response.json();
        } catch (error) {
            console.error(`[API] Error: ${endpoint}`, error);
            throw error;
        }
    }

    async createSession() {
        return this.request('/sessions/create', { method: 'POST' });
    }

    async joinSession(code) {
        return this.request('/sessions/join', {
            method: 'POST',
            body: JSON.stringify({ code })
        });
    }

    async getSession(sessionId) {
        return this.request(`/sessions/${sessionId}`);
    }

    async getChannels(sessionId) {
        return this.request(`/channels/${sessionId}`);
    }

    async addChannel(sessionId, channelData) {
        return this.request(`/channels/${sessionId}/add`, {
            method: 'POST',
            body: JSON.stringify(channelData)
        });
    }

    async removeChannel(sessionId, channelId) {
        return this.request(`/channels/${sessionId}/${channelId}`, {
            method: 'DELETE'
        });
    }

    async getSettings(sessionId) {
        return this.request(`/settings/${sessionId}`);
    }

    async updateSettings(sessionId, settings) {
        return this.request(`/settings/${sessionId}`, {
            method: 'PUT',
            body: JSON.stringify(settings)
        });
    }

    async getToggles(sessionId) {
        return this.request(`/event-toggles/${sessionId}`);
    }

    async updateToggles(sessionId, toggles) {
        return this.request(`/event-toggles/${sessionId}`, {
            method: 'PUT',
            body: JSON.stringify(toggles)
        });
    }

    async getEvents(sessionId) {
        return this.request(`/events/${sessionId}`);
    }

    async startTimer(sessionId) {
        return this.request(`/timer/${sessionId}/start`, {
            method: 'POST'
        });
    }

    async pauseTimer(sessionId) {
        return this.request(`/timer/${sessionId}/pause`, {
            method: 'POST'
        });
    }

    async resetTimer(sessionId) {
        return this.request(`/timer/${sessionId}/reset`, {
            method: 'POST'
        });
    }

    async addTime(sessionId, seconds, reason) {
        return this.request(`/timer/${sessionId}/add`, {
            method: 'POST',
            body: JSON.stringify({ seconds, reason })
        });
    }
}

const api = new API();
