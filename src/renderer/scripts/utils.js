function formatTime(seconds) {
  const hours = Math.floor(seconds / 3600);
  const minutes = Math.floor((seconds % 3600) / 60);
  const secs = seconds % 60;

  return `${String(hours).padStart(2, "0")}:${String(minutes).padStart(
    2,
    "0"
  )}:${String(secs).padStart(2, "0")}`;
}

function formatTimestamp(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleTimeString("de-DE");
}

function formatDate(timestamp) {
  const date = new Date(timestamp);
  return date.toLocaleDateString("de-DE");
}

function formatEventType(type) {
  const types = {
    FOLLOW: "Follow",
    SUBSCRIPTION: "Subscription",
    GIFTED_SUB: "Gifted Sub",
    BITS: "Bits",
    RAID: "Raid",
    MANUAL_ADD: "Manual",
  };
  return types[type] || type;
}

function debounce(func, wait) {
  let timeout;
  return function (...args) {
    clearTimeout(timeout);
    timeout = setTimeout(() => func.apply(this, args), wait);
  };
}

function showNotification(title, message, type = "info") {
  console.log(`[${type.toUpperCase()}] ${title}: ${message}`);
}

function generateId() {
  return Math.random().toString(36).substr(2, 9);
}
