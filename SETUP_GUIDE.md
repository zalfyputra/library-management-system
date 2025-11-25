# Setup Guide - Library Management System

This guide will help you set up and run the Library Management System backend.

## 📋 Prerequisites

Before you begin, ensure you have the following installed:
- ✅ Java 17 or higher
- ✅ Docker Desktop (or Docker + Docker Compose)
- ✅ Maven 3.6+ (optional, only if building locally)
- ✅ Git

## 🚀 Quick Start with Docker (Recommended)

### Step 1: Clone the Repository
```bash
git clone <your-repository-url>
cd security-backend
```

### Step 2: Configure Environment
```bash
# Copy the example environment file
cp env.example .env

# Edit .env file with your configurations
# Required: Update email credentials for OTP functionality
```

**Important Environment Variables:**
```env
# Email Configuration (Required for MFA)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-gmail-app-password

# JWT Secret (Use a strong random string in production)
JWT_SECRET=your-very-long-and-secure-secret-key-here

# Database (defaults are fine for Docker)
POSTGRES_DB=library_db
POSTGRES_USER=postgres
POSTGRES_PASSWORD=postgres
```

### Step 3: Start All Services
```bash
# Start all services (PostgreSQL, Redis, Backend)
docker-compose up -d

# Check if all services are running
docker-compose ps

# View logs
docker-compose logs -f app
```

### Step 4: Verify Installation
```bash
# Check if the application is healthy
curl http://localhost:8080/actuator/health

# Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

## 📧 Email Setup for OTP (Gmail)

To enable OTP functionality via email, you need to configure Gmail App Password:

### Step 1: Enable 2-Step Verification
1. Go to your Google Account: https://myaccount.google.com/
2. Navigate to Security
3. Enable 2-Step Verification

### Step 2: Generate App Password
1. Go to: https://myaccount.google.com/apppasswords
2. Select "Mail" and your device
3. Click "Generate"
4. Copy the 16-character password
5. Update your `.env` file:
   ```env
   MAIL_USERNAME=your-email@gmail.com
   MAIL_PASSWORD=your-16-char-app-password
   ```

### Step 3: Restart the Application
```bash
docker-compose down
docker-compose up -d
```

## 🧪 Testing the API

### Option 1: Using Swagger UI (Interactive)
1. Open http://localhost:8080/swagger-ui.html
2. Click on "Authorize" button
3. After getting JWT token, paste it and click "Authorize"
4. Try out the endpoints

### Option 2: Using Postman
1. Import `postman_collection.json` into Postman
2. Set the `base_url` variable to `http://localhost:8080`
3. Follow the authentication flow:
   - Register a new user
   - Login (OTP will be sent to email)
   - Verify OTP
   - Token will be saved automatically

### Option 3: Using cURL

#### Register a User
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "fullname": "John Doe",
    "username": "johndoe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

#### Login (Step 1)
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "password": "password123"
  }'
```

#### Verify OTP (Step 2)
Check your email for the OTP code, then:
```bash
curl -X POST http://localhost:8080/api/auth/verify-otp \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "johndoe",
    "otpCode": "123456"
  }'
```

#### Use JWT Token
Save the token from the response and use it:
```bash
curl -X GET http://localhost:8080/api/articles \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## 🎭 Testing Different Roles

### Create a SUPER_ADMIN User
First, you need to manually promote a user to SUPER_ADMIN via database:

```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres -d library_db

# Update a user to SUPER_ADMIN
UPDATE users SET role = 'SUPER_ADMIN' WHERE username = 'johndoe';

# Exit
\q
```

### Test Role-Based Access
- **VIEWER**: Can only view public articles
- **CONTRIBUTOR**: Can create and update own articles
- **EDITOR**: Can CRUD own articles, delete them too
- **SUPER_ADMIN**: Full access to everything

## 🧪 Running Tests

### Option 1: Inside Docker
```bash
docker-compose exec app mvn test
```

### Option 2: Locally (if Maven installed)
```bash
mvn clean test
```

### View Code Coverage Report
```bash
mvn clean test jacoco:report
open target/site/jacoco/index.html
```

## 🔧 Troubleshooting

### Issue: Port Already in Use
```bash
# Check what's using port 8080
lsof -i :8080

# Kill the process or change the port in docker-compose.yml
```

### Issue: Database Connection Failed
```bash
# Check if PostgreSQL is running
docker-compose ps postgres

# View PostgreSQL logs
docker-compose logs postgres

# Restart PostgreSQL
docker-compose restart postgres
```

### Issue: Email OTP Not Sending
- Verify Gmail App Password is correct
- Check if 2-Step Verification is enabled
- Check application logs:
  ```bash
  docker-compose logs -f app | grep -i mail
  ```

### Issue: Redis Connection Failed
```bash
# Check if Redis is running
docker-compose ps redis

# Restart Redis
docker-compose restart redis
```

### Issue: Application Won't Start
```bash
# View detailed logs
docker-compose logs app

# Rebuild the container
docker-compose down
docker-compose up --build -d
```

## 📊 Monitoring

### View Application Logs
```bash
# All services
docker-compose logs -f

# Only backend
docker-compose logs -f app

# Last 100 lines
docker-compose logs --tail=100 app
```

### Database Access
```bash
# Connect to PostgreSQL
docker-compose exec postgres psql -U postgres -d library_db

# Useful queries
SELECT * FROM users;
SELECT * FROM articles;
SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 10;
```

### Redis Access
```bash
# Connect to Redis CLI
docker-compose exec redis redis-cli

# Check cached keys
KEYS *

# Check a specific cache
GET articles::all
```

## 🛑 Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (⚠️ This will delete all data)
docker-compose down -v

# Stop but keep data
docker-compose stop
```

## 🔄 Updating the Application

```bash
# Pull latest changes
git pull

# Rebuild and restart
docker-compose down
docker-compose up --build -d
```

## 📱 Production Deployment

### Important Checklist
- [ ] Change JWT secret to a strong random value
- [ ] Use environment-specific application properties
- [ ] Enable HTTPS/SSL
- [ ] Configure proper email service (not personal Gmail)
- [ ] Set up proper database backups
- [ ] Configure monitoring and logging
- [ ] Set resource limits in Docker
- [ ] Use secrets management (e.g., AWS Secrets Manager)
- [ ] Set up CI/CD pipeline
- [ ] Enable database connection pooling
- [ ] Configure proper rate limits

### Environment-Specific Configs
Create `application-prod.yml` for production with:
- Production database URL
- Production Redis URL
- Production email service
- Proper logging levels
- Security headers
- CORS restrictions

## 📚 Additional Resources

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API Docs**: http://localhost:8080/api-docs
- **Spring Boot Actuator**: http://localhost:8080/actuator
- **Health Check**: http://localhost:8080/actuator/health

## 🆘 Getting Help

If you encounter issues:
1. Check the troubleshooting section above
2. Review application logs: `docker-compose logs app`
3. Check if all services are running: `docker-compose ps`
4. Verify environment variables: `docker-compose config`
5. Create an issue on GitHub with error logs

## ✅ Verification Checklist

After setup, verify:
- [ ] Application starts without errors
- [ ] Swagger UI is accessible
- [ ] User registration works
- [ ] Email OTP is received
- [ ] Login with OTP works
- [ ] JWT token is generated
- [ ] Can create an article
- [ ] Can view articles
- [ ] Audit logs are being created
- [ ] Rate limiting works (try >60 requests/minute)

---

**Happy Coding! 🚀**

