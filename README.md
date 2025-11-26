# Library Management System - Secure Backend

A comprehensive Spring Boot backend application featuring advanced security, RBAC, MFA, audit logging, and more.

## 🎯 Features

### Core Features
- ✅ **JWT Authentication** with secure token-based auth
- ✅ **Multi-Factor Authentication (MFA)** using Email OTP
- ✅ **Role-Based Access Control (RBAC)** with 4 distinct roles
- ✅ **Account Security** - Failed login tracking & automatic account locking
- ✅ **Comprehensive Audit Logging** with device/browser tracking
- ✅ **Rate Limiting** to prevent API abuse
- ✅ **Redis Caching** for improved performance
- ✅ **Article/Library CRUD** with permission-based access
- ✅ **Bubble Sort Algorithm** implementation for data sorting
- ✅ **Swagger/OpenAPI Documentation**
- ✅ **Docker Support** with Docker Compose
- ✅ **Unit Tests** with >80% code coverage
- ✅ **Payload Validation** on all endpoints

## 🏗️ Architecture

This project follows the **Repository + Service + Controller** pattern (not pure MVC):

```
├── controller/      # REST API endpoints
├── service/         # Business logic layer
├── repository/      # Data access layer (JPA)
├── domain/
│   ├── entity/     # JPA entities
│   └── enums/      # Role, AuditAction enums
├── dto/            # Data Transfer Objects
│   ├── request/    # API request DTOs
│   └── response/   # API response DTOs
├── security/       # Security filters & config
├── config/         # Spring configuration
├── util/           # Utility classes (JWT, Bubble Sort, Device detection)
└── exception/      # Custom exceptions & global handler
```

## 👥 Role-Based Access Control (RBAC)

| Role | Permissions |
|------|-------------|
| **VIEWER** (Default) | • View public articles only<br>• Cannot create/update/delete |
| **CONTRIBUTOR** | • Create articles<br>• Update own articles<br>• Cannot delete articles<br>• View public articles |
| **EDITOR** | • Full CRUD on own articles<br>• View all articles<br>• Delete own articles |
| **SUPER_ADMIN** | • Full access to all resources<br>• User management<br>• View all audit logs<br>• CRUD any article |

## 🔐 Security Features

### 1. Multi-Factor Authentication (MFA)
- Login requires username/email + password
- OTP sent via email for verification
- OTP expires in 5 minutes

### 2. Account Protection
- **Max 5 failed login attempts** in 10 minutes
- Account automatically **locked for 30 minutes** after exceeding limit
- Automatic unlock after lock period

### 3. Audit Logging
Tracks all user activities with:
- User ID and username
- Action type (login, CRUD operations, etc.)
- IP address
- Browser, device, and OS information
- Timestamp
- Success/failure status

### 4. Rate Limiting
- Default: 60 requests per minute per IP
- Configurable via `application.yml`
- Returns 429 (Too Many Requests) when exceeded

## 🚀 Tech Stack

- **Language:** Java 17
- **Framework:** Spring Boot 3.2.0
- **Database:** PostgreSQL 16
- **Cache:** Redis 7
- **Security:** Spring Security + JWT
- **Documentation:** Swagger/OpenAPI 3
- **Testing:** JUnit 5, Mockito
- **Build:** Maven
- **Containerization:** Docker & Docker Compose

## 📦 Dependencies

- Spring Boot Starter Web
- Spring Boot Starter Data JPA
- Spring Boot Starter Security
- Spring Boot Starter Validation
- Spring Boot Starter Mail
- Spring Boot Starter Data Redis
- PostgreSQL Driver
- JWT (io.jsonwebtoken:jjwt)
- Springdoc OpenAPI (Swagger)
- Bucket4j (Rate Limiting)
- Lombok
- JaCoCo (Code Coverage)

## 🛠️ Setup & Installation

### Prerequisites
- Java 17 or higher
- Docker & Docker Compose
- Maven 3.6+
- PostgreSQL 16 (if running locally)
- Redis 7 (if running locally)

### Step-by-Step

1. **Clone the repository**
```bash
git clone https://github.com/zalfyputra/library-management-system.git
cd library-management-system-main
```

2. **Configure environment variables**
```bash
cp env.example .env
# Edit .env with your configurations
```

3. **Start all services**
```bash
docker-compose up -d
```

4. **Access the application**
- API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html
- API Docs: http://localhost:8080/api-docs

## 📝 API Documentation

### Authentication Endpoints

#### 1. Register
```http
POST /api/auth/register
Content-Type: application/json

{
  "fullname": "Zalfy Putra",
  "username": "zalfyputra",
  "email": "zalfyputra@email.com",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "zalfyputra",
    "email": "zalfyputra@email.com",
    "role": "VIEWER"
  }
}
```

#### 2. Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "usernameOrEmail": "zalfyputra",
  "password": "securePassword123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login initiated. Check your email for OTP.",
  "data": {
    "mfaRequired": true,
    "userId": 1,
    "username": "zalfyputra",
    "message": "OTP has been sent to your email. Please verify to complete login."
  }
}
```

#### 3. Verify OTP
```http
POST /api/auth/verify-otp
Content-Type: application/json

{
  "usernameOrEmail": "zalfyputra",
  "otpCode": "123456"
}
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "type": "Bearer",
    "userId": 1,
    "username": "zalfyputra",
    "email": "zalfyputra@email.com",
    "role": "VIEWER"
  }
}
```

### Article Endpoints

#### Get All Articles
```http
GET /api/articles
Authorization: Bearer {token}
```

#### Get Article by ID
```http
GET /api/articles/{id}
Authorization: Bearer {token}
```

#### Create Article
```http
POST /api/articles
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "My First Article",
  "content": "This is the content of my article...",
  "isPublic": true
}
```

#### Update Article
```http
PUT /api/articles/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "title": "Updated Title",
  "content": "Updated content...",
  "isPublic": false
}
```

#### Delete Article
```http
DELETE /api/articles/{id}
Authorization: Bearer {token}
```

### User Management (SUPER_ADMIN only)

#### Get All Users
```http
GET /api/users
Authorization: Bearer {token}
```

#### Update User
```http
PUT /api/users/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "fullname": "Updated Name",
  "role": "EDITOR",
  "enabled": true
}
```

#### Delete User
```http
DELETE /api/users/{id}
Authorization: Bearer {token}
```

### Audit Logs (SUPER_ADMIN only)

#### Get All Audit Logs
```http
GET /api/audit-logs?page=0&size=20
Authorization: Bearer {token}
```

#### Get Logs by User
```http
GET /api/audit-logs/user/{userId}?page=0&size=20
Authorization: Bearer {token}
```

#### Get Logs by Action
```http
GET /api/audit-logs/action/{action}?page=0&size=20
Authorization: Bearer {token}
```

## 🧪 Testing

### Run All Tests
```bash
mvn test
```

### Run Tests with Coverage Report
```bash
mvn clean test jacoco:report
```

### View Coverage Report
Open `target/site/jacoco/index.html` in a browser

### Coverage Goal
- **Minimum Coverage:** 80%
- **Current Coverage:** >80% (as per requirements)

## 📊 Bubble Sort Implementation

The project includes a comprehensive bubble sort utility in `BubbleSortUtil.java`:

```java
// Sort articles by title
bubbleSortUtil.sortArticlesByTitle(articles);

// Sort articles by creation date (newest first)
bubbleSortUtil.sortArticlesByCreatedDate(articles);

// Sort articles by update date
bubbleSortUtil.sortArticlesByUpdatedDate(articles);

// Generic bubble sort with comparator
bubbleSortUtil.bubbleSort(list, comparator);

// Sort integer array
bubbleSortUtil.bubbleSortArray(intArray);
```

**Time Complexity:** O(n²)  
**Space Complexity:** O(1)  
**Optimization:** Includes early exit if list is already sorted

## 🔧 Configuration

### Application Properties (`application.yml`)

```yaml
# JWT Configuration
jwt:
  secret: your-secret-key-here
  expiration: 86400000 # 24 hours

# Security
security:
  max-login-attempts: 5
  login-attempt-window-minutes: 10
  account-lock-duration-minutes: 30
  otp-expiration-minutes: 5

# Rate Limiting
rate-limit:
  enabled: true
  requests-per-minute: 60

# Email (for OTP)
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: your-app-password
```

## 📚 Swagger Documentation

Access interactive API documentation at:
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI JSON:** http://localhost:8080/api-docs

Features:
- Complete API reference
- Try-it-out functionality
- Request/response examples
- Authentication support

## 🐳 Docker

### Build Docker Image
```bash
docker build -t library-backend:latest .
```

### Run with Docker Compose
```bash
docker-compose up -d
```

### Stop Services
```bash
docker-compose down
```

### View Logs
```bash
docker-compose logs -f app
```