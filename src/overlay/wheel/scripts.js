let ws = null;
const canvas = document.getElementById('wheel');
const ctx = canvas.getContext('2d');
const spinBtn = document.getElementById('spin-btn');
const container = document.getElementById('wheel-container');

const segments = [
    { text: '+30s', color: '#9147ff', value: 30 },
    { text: '+1m', color: '#1f69ff', value: 60 },
    { text: '+2m', color: '#ff4444', value: 120 },
    { text: '+5m', color: '#00c853', value: 300 },
    { text: '+10m', color: '#ff9800', value: 600 },
    { text: '+30m', color: '#e91e63', value: 1800 }
];

let currentRotation = 0;
let isSpinning = false;

function connectWebSocket() {
    ws = new WebSocket('ws://localhost:8080/ws');

    ws.onopen = () => {
        console.log('Wheel WebSocket connected');
    };

    ws.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === 'show-wheel') {
            showWheel();
        }

        if (data.type === 'hide-wheel') {
            hideWheel();
        }
    };

    ws.onclose = () => {
        console.log('WebSocket disconnected, reconnecting...');
        setTimeout(connectWebSocket, 2000);
    };
}

function drawWheel() {
    const centerX = canvas.width / 2;
    const centerY = canvas.height / 2;
    const radius = 250;
    const segmentAngle = (2 * Math.PI) / segments.length;

    ctx.clearRect(0, 0, canvas.width, canvas.height);
    ctx.save();
    ctx.translate(centerX, centerY);
    ctx.rotate(currentRotation);

    segments.forEach((segment, index) => {
        const startAngle = index * segmentAngle;
        const endAngle = startAngle + segmentAngle;

        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.arc(0, 0, radius, startAngle, endAngle);
        ctx.closePath();

        ctx.fillStyle = segment.color;
        ctx.fill();

        ctx.strokeStyle = 'white';
        ctx.lineWidth = 3;
        ctx.stroke();

        ctx.save();
        ctx.rotate(startAngle + segmentAngle / 2);
        ctx.textAlign = 'center';
        ctx.fillStyle = 'white';
        ctx.font = 'bold 24px Arial';
        ctx.fillText(segment.text, radius * 0.7, 10);
        ctx.restore();
    });

    ctx.restore();
}

function spinWheel() {
    if (isSpinning) return;

    isSpinning = true;
    spinBtn.disabled = true;

    const spins = 5 + Math.random() * 3;
    const targetRotation = currentRotation + (spins * 2 * Math.PI);
    const duration = 4000;
    const startTime = Date.now();

    function animate() {
        const elapsed = Date.now() - startTime;
        const progress = Math.min(elapsed / duration, 1);
        const easeOut = 1 - Math.pow(1 - progress, 3);

        currentRotation = targetRotation * easeOut;
        drawWheel();

        if (progress < 1) {
            requestAnimationFrame(animate);
        } else {
            isSpinning = false;
            spinBtn.disabled = false;

            const winningSegmentIndex = Math.floor(
                ((currentRotation % (2 * Math.PI)) / (2 * Math.PI)) * segments.length
            );
            const winner = segments[winningSegmentIndex];

            if (ws && ws.readyState === WebSocket.OPEN) {
                ws.send(JSON.stringify({
                    type: 'wheel-result',
                    value: winner.value
                }));
            }

            setTimeout(hideWheel, 3000);
        }
    }

    animate();
}

function showWheel() {
    container.classList.remove('hidden');
    drawWheel();
}

function hideWheel() {
    container.classList.add('hidden');
}

spinBtn.addEventListener('click', spinWheel);
connectWebSocket();
drawWheel();
