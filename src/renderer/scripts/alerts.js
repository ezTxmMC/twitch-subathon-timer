// Alerts page setup and handlers
let currentAlertType = "follow";

function setupAlertsPage() {
  const alertTypeBtns = document.querySelectorAll(".alert-type-btn");
  const testAlertBtn = document.getElementById("test-alert-btn");
  const saveAlertBtn = document.getElementById("save-alert-btn");
  const resetAlertBtn = document.getElementById("reset-alert-btn");

  alertTypeBtns.forEach((btn) => {
    btn.addEventListener("click", () => {
      alertTypeBtns.forEach((b) => b.classList.remove("active"));
      btn.classList.add("active");
      currentAlertType = btn.dataset.type;
      loadAlert(currentAlertType);
    });
  });

  if (testAlertBtn) {
    testAlertBtn.addEventListener("click", handleTestAlert);
  }

  if (saveAlertBtn) {
    saveAlertBtn.addEventListener("click", handleSaveAlert);
  }

  if (resetAlertBtn) {
    resetAlertBtn.addEventListener("click", handleResetAlert);
  }

  loadAlert(currentAlertType);
}

async function loadAlert(type) {
  const defaultAlerts = {
    follow: {
      name: "Follow Alert",
      duration: 5000,
      sound: "default",
      html: '<div class="alert-content">\n  <h2>{{username}} folgt jetzt!</h2>\n</div>',
      css: ".alert-content {\n  background: linear-gradient(135deg, #9147ff, #1f69ff);\n  padding: 20px;\n  border-radius: 12px;\n  text-align: center;\n}",
      js: 'function showAlert(data) {\n  console.log("Follow from", data.username);\n}',
    },
    sub: {
      name: "Subscription Alert",
      duration: 6000,
      sound: "default",
      html: '<div class="alert-content">\n  <h2>{{username}} hat abonniert!</h2>\n  <p>Tier {{tier}}</p>\n</div>',
      css: ".alert-content {\n  background: linear-gradient(135deg, #9147ff, #1f69ff);\n  padding: 20px;\n  border-radius: 12px;\n  text-align: center;\n}",
      js: 'function showAlert(data) {\n  console.log("Sub from", data.username, "Tier", data.tier);\n}',
    },
    gifted: {
      name: "Gifted Sub Alert",
      duration: 6000,
      sound: "default",
      html: '<div class="alert-content">\n  <h2>{{username}} hat {{amount}} Subs verschenkt!</h2>\n</div>',
      css: ".alert-content {\n  background: linear-gradient(135deg, #9147ff, #1f69ff);\n  padding: 20px;\n  border-radius: 12px;\n  text-align: center;\n}",
      js: 'function showAlert(data) {\n  console.log("Gift from", data.username, data.amount);\n}',
    },
    bits: {
      name: "Bits Alert",
      duration: 5000,
      sound: "default",
      html: '<div class="alert-content">\n  <h2>{{username}} cheered {{bits}} Bits!</h2>\n</div>',
      css: ".alert-content {\n  background: linear-gradient(135deg, #9147ff, #1f69ff);\n  padding: 20px;\n  border-radius: 12px;\n  text-align: center;\n}",
      js: 'function showAlert(data) {\n  console.log("Bits from", data.username, data.bits);\n}',
    },
    raid: {
      name: "Raid Alert",
      duration: 8000,
      sound: "default",
      html: '<div class="alert-content">\n  <h2>{{username}} raided mit {{viewers}} Viewern!</h2>\n</div>',
      css: ".alert-content {\n  background: linear-gradient(135deg, #9147ff, #1f69ff);\n  padding: 20px;\n  border-radius: 12px;\n  text-align: center;\n}",
      js: 'function showAlert(data) {\n  console.log("Raid from", data.username, data.viewers);\n}',
    },
  };

  const alert = defaultAlerts[type];

  document.getElementById("alert-name").value = alert.name;
  document.getElementById("alert-duration").value = alert.duration;
  document.getElementById("alert-sound").value = alert.sound;
  document.getElementById("alert-html").value = alert.html;
  document.getElementById("alert-css").value = alert.css;
  document.getElementById("alert-js").value = alert.js;
}

function handleTestAlert() {
  const preview = document.getElementById("alert-preview");
  const html = document.getElementById("alert-html").value;
  const css = document.getElementById("alert-css").value;

  const testData = {
    follow: { username: "TestFollower" },
    sub: { username: "TestSub", tier: "1" },
    gifted: { username: "TestGifter", amount: 5 },
    bits: { username: "TestCheerer", bits: 100 },
    raid: { username: "TestRaider", viewers: 50 },
  };

  let processedHtml = html;
  Object.entries(testData[currentAlertType]).forEach(([key, value]) => {
    processedHtml = processedHtml.replace(new RegExp(`{{${key}}}`, "g"), value);
  });

  preview.innerHTML = `
    <style>${css}</style>
    ${processedHtml}
  `;

  setTimeout(() => {
    preview.innerHTML =
      '<p class="text-muted text-center">Klicke auf "Test Alert" um eine Vorschau zu sehen</p>';
  }, parseInt(document.getElementById("alert-duration").value));
}

function handleSaveAlert() {
  showNotification("Gespeichert", "Alert wurde gespeichert", "success");
}

function handleResetAlert() {
  loadAlert(currentAlertType);
  showNotification("Zurückgesetzt", "Alert auf Standard zurückgesetzt", "info");
}

document.addEventListener("DOMContentLoaded", setupAlertsPage);
