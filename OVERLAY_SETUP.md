# Overlay Setup Guide

## Overview

The Subathon Timer app now includes an integrated HTTP/WebSocket server on port 8080 that serves browser-source overlays for streaming software like OBS.

## Server Details

### Port

- **HTTP Server**: `http://localhost:8080`
- **WebSocket Server**: `ws://localhost:8080/ws`

### Overlay URLs

Access these URLs in your browser or add them as browser sources in OBS:

1. **Timer Overlay**: `http://localhost:8080/overlay/timer/index.html`
2. **Alerts Overlay**: `http://localhost:8080/overlay/alerts/index.html`
3. **Wheel Overlay**: `http://localhost:8080/overlay/wheel/index.html`
4. **Chat Overlay**: `http://localhost:8080/overlay/chat/index.html`

## Features

### Automatic Server Startup

- Server starts automatically when the Electron app launches
- Reads port from config (default: 8080)
- Gracefully shuts down when app closes

### Real-Time WebSocket Broadcasting

All overlays connect via WebSocket and receive real-time updates:

#### Timer Events

- `timer-start` - Timer started
- `timer-pause` - Timer paused
- `timer-reset` - Timer reset
- `timer-add` - Time added (includes seconds/minutes)

#### Alert Events

- `event-alert` - New Twitch event (follows, subs, bits, raids)
  - Event types: `FOLLOW`, `SUBSCRIPTION`, `GIFTED_SUB`, `BITS`, `RAID`
  - Includes username, tier, amount, bits, viewers

#### Chat Events

- `chat-message` - New chat message
  - Includes username, message, color, badges (mod, sub, VIP)

### API Endpoints

The server provides REST API endpoints at `/api/*`:

- `POST /api/session` - Create session
- `POST /api/session/join` - Join session
- `POST /api/timer/start` - Start timer
- `POST /api/timer/pause` - Pause timer
- `POST /api/timer/reset` - Reset timer
- `POST /api/timer/add` - Add time
- `GET /api/channels` - Get channels
- `POST /api/settings` - Update settings
- `POST /api/events` - Create event

## Integration

### Main Process (`src/main/index.js`)

- Imports `IntegratedServer` class
- Starts server on app ready
- Stops server when app quits
- IPC handler `server:broadcast` for renderer to broadcast events

### Preload Script (`src/main/preload.js`)

- Exposes `electronAPI.server.broadcast(data)` to renderer

### Renderer Process

All page modules now broadcast events to overlays:

- **alerts.js** - Test alerts broadcast to overlay
- **timer.js** - Timer start/pause/reset/add broadcasts
- **chat.js** - Chat messages broadcast to overlay

## Testing

### Test Alerts

1. Open the Alerts page in the app
2. Select an alert type (follow, sub, bits, etc.)
3. Click "Test Alert"
4. Alert will show in both preview AND broadcast to overlay

### Test Timer

1. Open the Timer page
2. Use Start/Pause/Reset buttons
3. Add time using quick actions or manual input
4. Timer overlay updates in real-time

### Test Chat

1. Connect to Twitch
2. Join a channel
3. Messages display in app and broadcast to chat overlay

## OBS Setup

### Adding Overlay as Browser Source

1. In OBS, add new **Browser Source**
2. Set URL to one of the overlay URLs above
3. Set dimensions (recommended: 1920x1080)
4. Check "Shutdown source when not visible" for performance
5. Check "Refresh browser when scene becomes active" if needed

### Recommended Settings

- **Timer Overlay**: 1920x1080, transparent background
- **Alerts Overlay**: 1920x1080, transparent background
- **Chat Overlay**: 400x600, positioned on side
- **Wheel Overlay**: 800x800, centered

## Troubleshooting

### Overlay Not Connecting

- Check that the Electron app is running
- Verify port 8080 is not blocked by firewall
- Check browser console for WebSocket errors

### No Events Showing

- Verify renderer is calling `electronAPI.server.broadcast()`
- Check main process console for broadcast messages
- Ensure WebSocket clients are connected (check server logs)

### API Requests Failing

- Confirm API base URL is set to `http://localhost:8080/api`
- Check CORS headers in server response
- Verify content-type is `application/json`

## Architecture

```
┌─────────────────┐
│  Electron App   │
│   (Renderer)    │
│                 │
│  - Creates      │
│    events       │
│  - Broadcasts   │
│    via IPC      │
└────────┬────────┘
         │ IPC
         ▼
┌─────────────────┐
│  Electron App   │
│  (Main Process) │
│                 │
│  - HTTP Server  │
│  - WebSocket    │
│  - Broadcasts   │
└────────┬────────┘
         │ WebSocket
         ▼
┌─────────────────┐
│  Overlays       │
│  (Browser)      │
│                 │
│  - Timer        │
│  - Alerts       │
│  - Chat         │
│  - Wheel        │
└─────────────────┘
```

## Files Modified

### New Files

- `src/main/server.js` - IntegratedServer class

### Modified Files

- `src/main/index.js` - Server integration
- `src/main/preload.js` - Expose broadcast IPC
- `src/renderer/scripts/alerts.js` - Broadcast test alerts
- `src/renderer/scripts/timer.js` - Broadcast timer events
- `src/renderer/scripts/chat.js` - Broadcast chat messages

## Development Notes

### Adding New Event Types

1. Add handler in renderer (e.g., `timer.js`)
2. Call `electronAPI.server.broadcast({ type: 'event-type', ...data })`
3. Update overlay script to handle new event type
4. Test in browser before adding to OBS

### Customizing Overlays

- HTML: `src/overlay/[type]/index.html`
- CSS: `src/overlay/[type]/styles.css`
- JavaScript: `src/overlay/[type]/script.js` or `scripts.js`

All changes take effect immediately - just refresh the browser source in OBS.
