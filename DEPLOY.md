# DEPLOY.md — Job Application Tracker Deployment Guide

## Overview

| Component | Platform | URL Pattern |
|-----------|----------|------------|
| Frontend | Vercel | `https://your-app.vercel.app` |
| Backend | Render / Railway | `https://your-app.onrender.com` |
| Database | Neon PostgreSQL | `postgres://...@ep-xxx.neon.tech/jobtracker` |

---

## Step 1 — Neon PostgreSQL Setup

1. Go to [neon.tech](https://neon.tech) and create a free account.
2. Create a new project: `job-tracker-db`.
3. Create a database named `jobtracker`.
4. Copy the **Connection String** (PostgreSQL URL):
   ```
   postgresql://user:password@ep-xxx-xxx.us-east-2.aws.neon.tech/jobtracker?sslmode=require
   ```
5. The application uses `spring.jpa.hibernate.ddl-auto: update` — tables will be **auto-created** on first startup.

---

## Step 2 — Generate JWT Secret

Generate a secure 256-bit hex secret:
```bash
openssl rand -hex 32
```
Save the output — you'll use it as `JWT_SECRET`.

---

## Step 3 — Backend Deployment (Render)

### Option A: Render (Recommended)

1. Go to [render.com](https://render.com) → **New Web Service**.
2. Connect your GitHub repository.
3. Configure:
   - **Root Directory**: `backend`
   - **Build Command**: `./mvnw clean package -DskipTests`
   - **Start Command**: `java -Xms128m -Xmx384m -jar target/job-tracker-backend-1.0.0.jar`
   - **Runtime**: Java 17
   - **Plan**: Free

4. Set **Environment Variables**:

   | Variable | Value |
   |----------|-------|
   | `DATABASE_URL` | `jdbc:postgresql://ep-xxx.neon.tech/jobtracker?sslmode=require` |
   | `DATABASE_USERNAME` | your Neon DB username |
   | `DATABASE_PASSWORD` | your Neon DB password |
   | `JWT_SECRET` | 64-char hex string from Step 2 |
   | `JWT_EXPIRATION_MS` | `86400000` |
   | `MAIL_HOST` | `smtp.gmail.com` |
   | `MAIL_PORT` | `587` |
   | `MAIL_USERNAME` | your Gmail address |
   | `MAIL_PASSWORD` | your Gmail App Password |
   | `CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app` |
   | `PORT` | `8080` |

> **Note**: For Gmail App Password, go to Google Account → Security → 2-Step Verification → App Passwords.

### Option B: Railway

1. Go to [railway.app](https://railway.app) → **New Project** → **Deploy from GitHub repo**.
2. Select `backend` folder as root.
3. Add same environment variables as above.
4. Railway auto-detects Maven and builds.

---

## Step 4 — Frontend Deployment (Vercel)

1. Go to [vercel.com](https://vercel.com) → **New Project** → Import your GitHub repo.
2. Configure:
   - **Framework Preset**: Vite
   - **Root Directory**: `frontend`
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`

3. Set **Environment Variables**:

   | Variable | Value |
   |----------|-------|
   | `VITE_API_BASE_URL` | `https://your-backend.onrender.com` |

4. Deploy.

### SPA Routing Fix (Vercel)
Create `frontend/public/vercel.json`:
```json
{
  "rewrites": [{ "source": "/(.*)", "destination": "/index.html" }]
}
```

---

## Step 5 — CORS Configuration

Ensure the `CORS_ALLOWED_ORIGINS` env var on your backend includes your Vercel domain exactly:
```
CORS_ALLOWED_ORIGINS=https://your-app.vercel.app
```

No trailing slash. Multiple origins comma-separated.

---

## Step 6 — Gmail SMTP Setup

1. Enable 2-Factor Authentication on your Google account.
2. Go to **Google Account → Security → App Passwords**.
3. Generate a password for "Mail" / "Windows Computer".
4. Use that 16-character password as `MAIL_PASSWORD`.

---

## Step 7 — Local Development

### Backend
```bash
cd backend
# Set env vars
$env:DATABASE_URL = "jdbc:postgresql://localhost:5432/jobtracker"
$env:DATABASE_USERNAME = "postgres"
$env:DATABASE_PASSWORD = "postgres"
$env:JWT_SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"

# Run
.\mvnw.cmd spring-boot:run
```

### Frontend
```bash
cd frontend
# .env already configured for localhost
npm run dev
```

### Full Stack (Docker)
```bash
# Copy .env for mail config
cp .env.example .env  # fill in MAIL_USERNAME, MAIL_PASSWORD

docker-compose up --build
```

Access at:
- Frontend: `http://localhost`
- Backend: `http://localhost:8080`
- API Docs: `http://localhost:8080/api/v1/...`

---

## API Reference

| Endpoint | Method | Auth | Description |
|----------|--------|------|-------------|
| `/api/v1/auth/register` | POST | Public | Register |
| `/api/v1/auth/login` | POST | Public | Login |
| `/api/v1/applications` | GET | JWT | Paginated list |
| `/api/v1/applications/all` | GET | JWT | Full list |
| `/api/v1/applications` | POST | JWT | Create |
| `/api/v1/applications/{id}` | PUT | JWT | Update |
| `/api/v1/applications/{id}/status` | PATCH | JWT | Status update |
| `/api/v1/applications/{id}` | DELETE | JWT | Delete |
| `/api/v1/applications/{id}/interviews` | GET/POST | JWT | Interview rounds |
| `/api/v1/analyzer/analyze` | POST | JWT | Resume analysis |
| `/api/v1/activity` | GET | JWT | Activity feed |
| `/api/v1/analytics/summary` | GET | JWT | Analytics data |

---

## Production Checklist

- [ ] JWT_SECRET is a cryptographically random 64-char hex string
- [ ] DATABASE_URL uses `?sslmode=require` for Neon
- [ ] CORS_ALLOWED_ORIGINS matches Vercel domain exactly
- [ ] Gmail App Password configured (not account password)
- [ ] Vercel rewrite rules added for SPA routing
- [ ] Backend cold-start within Render free-tier time limit
