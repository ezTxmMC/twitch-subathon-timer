let ws = null;
const maxMessages = 50;

function connectWebSocket() {
    ws = new WebSocket('ws://localhost:8080/ws');

    ws.onopen = () => {
        console.log('Chat WebSocket connected');
    };

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === 'chat-message') {
            addMessage(data);
        }
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected, reconnecting...');
        setTimeout(connectWebSocket, 2000);
    };
}

function addMessage(messageData) {
    const container = document.getElementById('chat-messages');
    const message = document.createElement('div');
    message.className = 'chat-message';

    const badges = [];
    if (messageData.isMod) {
        badges.push('<span class="chat-badge" style="background: #00c853;">M</span>');
    }
    if (messageData.isSubscriber) {
        badges.push('<span class="chat-badge" style="background: #9147ff;">S</span>');
    }
    if (messageData.isVip) {
        badges.push('<span class="chat-badge" style="background: #ff0090;">V</span>');
    }

    message.innerHTML = `
        <div class="chat-badges">${badges.join('')}</div>
        <span class="chat-username" style="color: ${messageData.color || '#ffffff'};">
            ${escapeHtml(messageData.username)}:
        </span>
        <span class="chat-text">${escapeHtml(messageData.message)}</span>
    `;

    container.appendChild(message);

    while (container.children.length > maxMessages) {
        container.removeChild(container.firstChild);
    }

    container.scrollTop = container.scrollHeight;
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

connectWebSocket();
