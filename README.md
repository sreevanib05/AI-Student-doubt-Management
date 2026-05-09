# DoubtFlow AI

DoubtFlow AI is a production-style academic doubt management dashboard built with React, Tailwind CSS, Spring Boot, JDBC, JWT authentication, and MySQL.

## Project Folders

```text
backend/     Spring Boot APIs, JDBC repositories, JWT auth
frontend/    React dashboard UI
database/    MySQL schema
```

## Local Setup

### 1. Start MySQL

```bash
brew services start mysql
```

### 2. Create the database and app user

Open Terminal and run MySQL as root:

```bash
mysql -u root
```

Then run this SQL. Replace `your_password_here` with your own password:

```sql
CREATE DATABASE IF NOT EXISTS doubtflow_ai CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER IF NOT EXISTS 'doubtflow_user'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT ALL PRIVILEGES ON doubtflow_ai.* TO 'doubtflow_user'@'localhost';
FLUSH PRIVILEGES;
```

### 3. Create the private backend config file

Copy the example:

```bash
cp backend/src/main/resources/application-local.example.properties backend/src/main/resources/application-local.properties
```

Edit `backend/src/main/resources/application-local.properties` and put your MySQL password there.

This file is ignored by Git, so it should stay only on your computer.

### 4. Apply the schema

```bash
mysql -u doubtflow_user -p doubtflow_ai < database/schema.sql
```

### 5. Create the private frontend config file

```bash
cp frontend/.env.example frontend/.env
```

The local API URL is:

```text
http://localhost:8081/api
```

## Run The App

### Backend

```bash
cd backend
mvn spring-boot:run
```

Backend runs at:

```text
http://localhost:8081
```

### Frontend

Open a second Terminal tab:

```bash
cd frontend
npm install
npm run dev
```

Frontend runs at:

```text
http://127.0.0.1:5173
```

## Starter Accounts

These are created automatically when the backend starts if each email is missing from the database.

```text
Admin:
email: admin@doubtflow.ai
password: Admin@123

Mentor:
email: mentor.concepts@doubtflow.ai
password: Mentor@123

Mentor:
email: mentor.coding@doubtflow.ai
password: Mentor@123

Mentor:
email: mentor.debugging@doubtflow.ai
password: Mentor@123
```

Students and mentors can register from the frontend.

## Real-Time Workflow To Demonstrate

### Student submits a doubt

1. Open `http://127.0.0.1:5173`.
2. Register as a student.
3. Go to `Submit Doubt`.
4. Submit a doubt.

The doubt is saved in MySQL with status `OPEN`.

### Admin assigns the mentor

1. Logout from the student account.
2. Login as admin:

```text
email: admin@doubtflow.ai
password: Admin@123
role: Admin/Faculty
```

3. Open `Dashboard` or `Analytics`.
4. Find the student doubt.
5. Select a mentor from the dropdown.
6. Click `Save`.

The doubt status changes from `OPEN` to `ASSIGNED`.

### Mentor resolves the doubt

1. Logout from admin.
2. Login as the selected mentor. Mentors can use a starter account or register themselves from the frontend.
3. Open `Assigned Doubts`.
4. Write a response.
5. Click `Respond`.

The response is saved in MySQL and the doubt status becomes `RESOLVED`.

### Admin creates a new mentor

1. Login as admin.
2. Open `Mentors`.
3. Fill mentor name, email, password, and expertise.
4. Click `Create Mentor`.

The new mentor can now login from the normal login page using role `Mentor`.

### Mentor self-registers

1. Open `Register here` from the login page.
2. Choose `Mentor` as the account type.
3. Enter name, email, password, and expertise.
4. Click `Create account`.

The mentor is signed in immediately and can see doubts assigned by admin/faculty.

## GitHub

This folder is connected to:

```text
https://github.com/sreevanib05/AI-Student-doubt-Management.git
```

Before pushing, check files:

```bash
git status
```

Commit and push:

```bash
git add .
git commit -m "Build DoubtFlow AI full stack dashboard"
git push -u origin main
```

## Deployment Notes

### Backend on Render

This project includes `render.yaml` and `backend/Dockerfile`, so the easiest backend deployment is Render Blueprint / Docker deployment.

In Render:

1. Open Render.
2. Click `New +`.
3. Click `Blueprint`.
4. Connect this GitHub repository.
5. Render will read `render.yaml`.
6. Fill the secret values when Render asks.

Use these Render environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
DB_URL=jdbc:mysql://YOUR_AIVEN_HOST:YOUR_AIVEN_PORT/YOUR_AIVEN_DATABASE?sslMode=REQUIRED&serverTimezone=UTC
DB_USERNAME=your_aiven_username
DB_PASSWORD=your_aiven_password
JWT_SECRET=Render can generate this from render.yaml
CORS_ALLOWED_ORIGINS=https://*.vercel.app,http://localhost:5173,http://127.0.0.1:5173
```

The `YOUR_AIVEN_HOST` value must be copied from the live Aiven service overview. Do not reuse an old host after deleting or recreating the Aiven service. If Render logs show:

```text
UnknownHostException: mysql-...aivencloud.com
```

the hostname in `DB_URL` is invalid or no longer exists. Update `DB_URL` in Render with the current Aiven host and port, then redeploy.

For a custom Vercel domain or a specific production URL, include it too:

```text
CORS_ALLOWED_ORIGINS=https://*.vercel.app,https://YOUR_VERCEL_PROJECT.vercel.app,http://localhost:5173,http://127.0.0.1:5173
```

If your deployment cannot connect to the Aiven hostname, use the IP version of the same JDBC URL.

```text
DB_URL=jdbc:mysql://YOUR_AIVEN_IP:YOUR_AIVEN_PORT/YOUR_AIVEN_DATABASE?sslMode=REQUIRED&serverTimezone=UTC
```

The backend health check URL will be:

```text
https://your-render-backend.onrender.com/api/health
```

Expected healthy response:

```json
{"status":"UP","databaseConfig":"SET","jwt":"UP","profile":"prod"}
```

The deeper readiness check verifies MySQL:

```text
https://your-render-backend.onrender.com/api/ready
```

Expected ready response:

```json
{"status":"UP","database":"UP","databaseConfig":"SET","jwt":"UP","profile":"prod"}
```

### Frontend on Vercel

In Vercel:

1. Click `Add New`.
2. Click `Project`.
3. Import this GitHub repository.
4. Set Root Directory to `frontend`.
5. Set Build Command to `npm run build`.
6. Set Output Directory to `dist`.

The frontend now has a production fallback that calls `/api`, and `frontend/vercel.json` proxies those requests to:

```text
https://doubtflow-ai-backend.onrender.com/api
```

If your Render backend URL is different, update the `/api/(.*)` rewrite destination in `frontend/vercel.json`, or add this Vercel environment variable to call the backend directly:

```text
VITE_API_BASE_URL=https://your-render-backend.onrender.com/api
```

The included `frontend/vercel.json` makes React Router page refreshes work after deployment.

### Deployment Order

Use this order:

1. Deploy backend on Render.
2. Copy the Render backend URL.
3. Deploy frontend on Vercel, using `VITE_API_BASE_URL` only if your backend URL differs from the included Vercel rewrite.
4. Test login/register from the Vercel URL.

### Important Security Note

Do not commit real passwords or API keys. Keep them only in:

```text
backend/src/main/resources/application-local.properties
frontend/.env
Render Environment Variables
Vercel Environment Variables
```
