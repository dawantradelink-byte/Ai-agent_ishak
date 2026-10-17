# Open AI Agent: Autonomous Cloud AI Team Backend

This backend provides the persistent cloud execution environment for the Open AI Agent Android app, inspired by modern cloud-based AI teammate platforms.

## Core Features
1. **Durable Task Queue & State Machine**: Tasks are persisted with checkpoints across all 5 agents (Mochi, Scout, Forge, Pixel, Guardian).
2. **Persistent Cloud Filesystem**: Mounted `/workspace` volume for persistent file creation, editing, and code deliverables.
3. **Sandboxed Command Execution**: Controlled terminal execution with a security blocklist preventing destructive commands.
4. **Browser Automation Interface**: Web DOM extraction, evidence gathering, and citation verification via Scout.
5. **Reusable Skills & Workflow Learning**: Convert observed workflows into draft skills, evaluate safety, and promote with human approval.
6. **Human-in-the-Loop Security Gatekeeper**: Guardian intercepts elevated commands, destructive file actions, and secret leaks.

---

## Windows Quick-Start Guide (Local Node.js)

### Prerequisites
- Node.js (v18 or newer): Download from [nodejs.org](https://nodejs.org/)

### 1. Open Terminal in the `backend` folder
Press `Win + R`, type `cmd`, and navigate to your extracted folder:
```cmd
cd path\to\Open-AI-Agent\backend
```

### 2. Install Dependencies
```cmd
npm install
```

### 3. Configure Secrets (Optional)
Copy `.env.example` to `.env`:
```cmd
copy .env.example .env
```
Open `.env` in Notepad and add your `GEMINI_API_KEY` (or leave blank to use verified local simulation).

### 4. Start the Cloud Server
```cmd
npm start
```
The server will start on `http://localhost:8080`.

---

## Docker & Cloud VM Deployment

To run in a container on any cloud provider (Google Cloud Run, AWS, DigitalOcean):

### Using Docker Compose
```bash
cd backend
docker-compose up -d
```

### Verifying Health
```bash
curl http://localhost:8080/api/health
```

---

## Connecting the Android App to Your Cloud Backend
1. In the Android app, navigate to **Settings** -> **Cloud Computer & Remote VM**.
2. Enter your backend server URL (e.g. `http://10.0.2.2:8080` for Android Emulator, or your public Cloud IP).
3. Tap **Test & Connect**. The app will immediately link to your persistent container. If no remote URL is entered, the app runs via its internal embedded SQLite engine with zero loss of functionality.
