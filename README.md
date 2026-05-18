# Job Application Tracker

<div align="center">

![Java](https://img.shields.io/badge/Java_17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot_3-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React_18-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL_15-316192?style=for-the-badge&logo=postgresql&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2CA5E0?style=for-the-badge&logo=docker&logoColor=white)

**A full-stack web app to track job applications with status updates, AI resume analysis, email notifications, and JWT authentication.**

[Live Demo](#) · [Report Bug](https://github.com/praveenramnath1209/Job_Application_Tracker/issues) · [Request Feature](https://github.com/praveenramnath1209/Job_Application_Tracker/issues)

</div>

---

## ✨ Features

- 🔐 **JWT Authentication** — Secure register/login with token-based sessions
- 📋 **Application Pipeline** — Track jobs through Applied → Interview → Offer → Rejected stages
- 📊 **Analytics Dashboard** — Visual charts of application status breakdown and trends
- 🤖 **AI Resume Analyzer** — Match your resume against a job description, get a score & recommendations
- 📧 **Email Notifications** — SMTP-powered alerts for application status changes
- 🌙 **Dark / Light Mode** — Persistent theme toggle
- 🐳 **Docker Compose** — One-command local setup

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                        CLIENT                           │
│              React 18 + Vite + Tailwind                 │
│          (Vercel → https://your-app.vercel.app)         │
└─────────────────────┬───────────────────────────────────┘
                      │  HTTPS REST API (/api/v1/*)
                      ▼
┌─────────────────────────────────────────────────────────┐
│                       BACKEND                           │
│          Spring Boot 3.2 · Java 17 · JWT                │
│            (Render → https://your-app.onrender.com)     │
│                                                         │
│  Controllers → Services → Repositories → JPA Entities   │
└─────────────────────┬───────────────────────────────────┘
                      │  JDBC / HikariCP
                      ▼
┌─────────────────────────────────────────────────────────┐
│                      DATABASE                           │
│              PostgreSQL 15 (Supabase)                   │
└─────────────────────────────────────────────────────────┘
```

---

## 🛠️ Tech Stack

| Layer      | Technology                                   |
|------------|----------------------------------------------|
| Backend    | Java 17, Spring Boot 3.2, Spring Security    |
| Auth       | JWT (JJWT 0.12), BCrypt                      |
| Frontend   | React 18, Vite, Tailwind CSS v4, Zustand     |
| Charts     | Recharts                                     |
| Forms      | React Hook Form + Zod                        |
| Database   | PostgreSQL 15 (Supabase for prod)            |
| ORM        | Spring Data JPA / Hibernate 6               |
| Mail       | Spring Mail + Gmail SMTP                     |
| DevOps     | Docker, Docker Compose, GitHub Actions CI    |
| Deployment | Render (backend) + Vercel (frontend)         |

---

## 🚀 Local Development

### Prerequisites
- Docker Desktop (includes Docker Compose)
- Git

### 1. Clone the repo
```bash
git clone https://github.com/praveenramnath1209/Job_Application_Tracker.git
cd Job_Application_Tracker
```

### 2. Set up environment variables
```bash
cp .env.example .env
# Edit .env with your values — see table below
```

### 3. Start everything with Docker
```bash
docker-compose up --build
```

| Service  | URL                        |
|----------|----------------------------|
| Frontend | http://localhost           |
| Backend  | http://localhost:8080      |
| Database | localhost:5432             |

### Stop
```bash
docker-compose down          # stop containers
docker-compose down -v       # stop + wipe database
```

---

## ☁️ Deployment Guide

### Deployment Order
```
1. Supabase  →  get DATABASE_URL
2. Render    →  deploy backend, get backend URL
3. Vercel    →  deploy frontend with VITE_API_BASE_URL = Render URL
4. Render    →  update CORS_ALLOWED_ORIGINS = Vercel URL
```

### 1️⃣ Supabase (PostgreSQL)
1. Create account at [supabase.com](https://supabase.com)
2. New project → note the **password** you set
3. Go to **Project Settings → Database → Connection String → URI**
4. Copy the URI — it looks like:
   ```
   postgresql://postgres:[PASSWORD]@db.xxxx.supabase.co:5432/postgres
   ```
5. Use this as `DATABASE_URL` on Render

### 2️⃣ Render (Backend)
1. Create account at [render.com](https://render.com)
2. **New → Web Service** → connect your GitHub repo
3. Settings:
   - **Root Directory**: `backend`
   - **Runtime**: Docker
   - **Dockerfile Path**: `./Dockerfile`
4. Add all environment variables from the table below
5. Deploy — your URL will be `https://job-tracker-backend-xxxx.onrender.com`

### 3️⃣ Vercel (Frontend)
1. Create account at [vercel.com](https://vercel.com)
2. **New Project** → import your GitHub repo
3. Settings:
   - **Root Directory**: `frontend`
   - **Framework**: Vite
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
4. Add environment variable:
   ```
   VITE_API_BASE_URL = https://your-render-backend-url.onrender.com
   ```
5. Deploy — your URL will be `https://job-tracker-xxxx.vercel.app`

### 4️⃣ Update CORS on Render
After getting your Vercel URL, go to Render → your service → Environment and update:
```
CORS_ALLOWED_ORIGINS = https://job-tracker-xxxx.vercel.app
```
Then trigger a new deploy.

---

## 🔐 Environment Variables

| Variable               | Description                              | Example                                      |
|------------------------|------------------------------------------|----------------------------------------------|
| `POSTGRES_DB`          | Database name                            | `jobtracker`                                 |
| `POSTGRES_USER`        | Database user                            | `postgres`                                   |
| `POSTGRES_PASSWORD`    | Database password                        | `your_strong_password`                       |
| `DATABASE_URL`         | Full JDBC URL (prod: Supabase URI)       | `jdbc:postgresql://host:5432/db`             |
| `PORT`                 | Backend server port                      | `8080`                                       |
| `JWT_SECRET`           | 256-bit hex JWT signing key              | `404E635266...`                              |
| `JWT_EXPIRATION_MS`    | Token lifetime in milliseconds           | `86400000` (24h)                             |
| `MAIL_HOST`            | SMTP server                              | `smtp.gmail.com`                             |
| `MAIL_PORT`            | SMTP port                                | `587`                                        |
| `MAIL_USERNAME`        | Email address                            | `you@gmail.com`                              |
| `MAIL_PASSWORD`        | Gmail App Password (not account pass)    | `abcd efgh ijkl mnop`                        |
| `CORS_ALLOWED_ORIGINS` | Comma-separated allowed frontend origins | `https://your-app.vercel.app`                |
| `VITE_API_BASE_URL`    | Backend URL (frontend only)              | `https://your-backend.onrender.com`          |

> 💡 **Gmail App Password**: Go to [myaccount.google.com/apppasswords](https://myaccount.google.com/apppasswords) to generate one.

---

## 📸 Screenshots

> _Coming soon — add screenshots of Dashboard, Applications, Analytics, and Resume Analyzer pages._

---

## 📁 Project Structure

```
Job_Application_Tracker/
├── backend/                    # Spring Boot API
│   ├── src/main/java/com/jobtracker/
│   │   ├── config/             # Security, CORS, async config
│   │   ├── controller/         # REST controllers
│   │   ├── service/            # Business logic
│   │   ├── repository/         # JPA repositories
│   │   ├── entity/             # JPA entities
│   │   ├── dto/                # Request/Response DTOs
│   │   ├── security/           # JWT filter, auth
│   │   └── exception/          # Global exception handler
│   └── src/test/               # Unit + integration tests (H2)
├── frontend/                   # React + Vite app
│   ├── src/
│   │   ├── api/                # Axios API clients
│   │   ├── components/         # Reusable UI components
│   │   ├── pages/              # Route pages
│   │   ├── store/              # Zustand state
│   │   └── utils/              # Helpers & formatters
│   └── vercel.json             # Vercel SPA config
├── .env.example                # Environment template
├── docker-compose.yml          # Local dev stack
├── render.yaml                 # Render deploy config
└── README.md
```

---

## 🧪 Running Tests

```bash
# Backend tests only (uses H2 in-memory — no Postgres needed)
cd backend
./mvnw test

# Frontend lint/type check
cd frontend
npm run build
```

---

## 📄 License

MIT © [Praveen Ramnath](https://github.com/praveenramnath1209)
