let ws = null;
let currentSeconds = 0;

function connectWebSocket() {
    ws = new WebSocket('ws://localhost:8080/ws');

    ws.onopen = () => {
        console.log('WebSocket connected');
    };

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === 'timer-update') {
            updateTimer(data.seconds, data.running);
        }
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected, reconnecting...');
        setTimeout(connectWebSocket, 2000);
    };
}

function updateTimer(seconds, running) {
    currentSeconds = seconds;

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);
    const secs = seconds % 60;

    const display = document.getElementById('timer');
    display.textContent =
        `${String(hours).padStart(2, '0')}:${String(minutes).padStart(2, '0')}:${String(secs).padStart(2, '0')}`;

    const container = document.querySelector('.timer-container');

    if (seconds < 300) {
        container.classList.add('low-time');
    } else {
        container.classList.remove('low-time');
    }
}

connectWebSocket();
