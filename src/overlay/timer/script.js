let ws = null;
let _currentSeconds = 0;
let reconnectAttempts = 0;
const MAX_RECONNECT_ATTEMPTS = 10;

function connectWebSocket() {
  ws = new WebSocket("ws://localhost:8080/ws");

  ws.onopen = () => {
    console.log("WebSocket connected");
    reconnectAttempts = 0;

    // Request initial timer state
    ws.send(JSON.stringify({ type: "request-timer-state" }));
  };

  ws.onmessage = (event) => {
    const data = JSON.parse(event.data);
    console.log("[Overlay Timer] Received:", data.type, data);

    // Handle different timer event types
    if (data.type === "timer-update") {
      // Regular sync update - update immediately
      updateTimer(data.seconds, data.running);
    } else if (data.type === "timer-start") {
      // Timer started
      updateTimer(data.seconds || _currentSeconds, true);
    } else if (data.type === "timer-pause") {
      // Timer paused
      updateTimer(data.seconds || _currentSeconds, false);
    } else if (data.type === "timer-reset") {
      // Timer reset to 0
      updateTimer(0, false);
    } else if (data.type === "timer-add") {
      // Time added - use totalSeconds if available
      const newSeconds =
        data.totalSeconds || _currentSeconds + (data.addedSeconds || 0);
      updateTimer(newSeconds, data.running);
    } else if (data.type === "connected") {
      console.log("[Overlay Timer] Server connection established");
    }
  };

  ws.onclose = () => {
    console.log("WebSocket disconnected, reconnecting...");
    reconnectAttempts++;

    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
      setTimeout(connectWebSocket, 2000);
    } else {
      console.error("Max reconnection attempts reached");
    }
  };

  ws.onerror = (error) => {
    console.error("WebSocket error:", error);
  };
}

function updateTimer(seconds, _running) {
  console.log("TIMER OVERLAY UPDATE");
  _currentSeconds = seconds;

  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  const display = document.getElementById("timer");
  if (display) {
    display.textContent = `${String(hours).padStart(2, "0")}:${String(
      minutes
    ).padStart(2, "0")}:${String(secs).padStart(2, "0")}`;
  }

  const container = document.querySelector(".timer-container");
  if (container) {
    if (seconds < 300) {
      container.classList.add("low-time");
    } else {
      container.classList.remove("low-time");
    }
  }
}

connectWebSocket();
