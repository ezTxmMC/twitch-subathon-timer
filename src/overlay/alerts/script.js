let ws = null;
let alertQueue = [];
let isShowingAlert = false;

function connectWebSocket() {
    ws = new WebSocket('ws://localhost:8080/ws');

    ws.onopen = () => {
        console.log('Alerts WebSocket connected');
    };

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === 'event-alert') {
            queueAlert(data);
        }
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected, reconnecting...');
        setTimeout(connectWebSocket, 2000);
    };
}

function queueAlert(alertData) {
    alertQueue.push(alertData);

    if (!isShowingAlert) {
        showNextAlert();
    }
}

function showNextAlert() {
    if (alertQueue.length === 0) {
        isShowingAlert = false;
        return;
    }

    isShowingAlert = true;
    const alertData = alertQueue.shift();

    const container = document.getElementById('alert-container');
    const alert = document.createElement('div');
    alert.className = `alert ${alertData.eventType.toLowerCase()}`;

    let content = '';

    switch (alertData.eventType) {
        case 'FOLLOW':
            content = `<h2>${alertData.username} folgt jetzt!</h2>`;
            break;
        case 'SUBSCRIPTION':
            content = `<h2>${alertData.username} hat abonniert!</h2><p>Tier ${alertData.tier}</p>`;
            break;
        case 'GIFTED_SUB':
            content = `<h2>${alertData.username} hat ${alertData.amount} Subs verschenkt!</h2>`;
            break;
        case 'BITS':
            content = `<h2>${alertData.username} cheered ${alertData.bits} Bits!</h2>`;
            break;
        case 'RAID':
            content = `<h2>${alertData.username} raided mit ${alertData.viewers} Viewern!</h2>`;
            break;
    }

    alert.innerHTML = content;
    container.appendChild(alert);

    setTimeout(() => {
        container.removeChild(alert);
        showNextAlert();
    }, 5000);
}

connectWebSocket();
