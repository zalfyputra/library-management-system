# Troubleshooting Guide

## Docker Build Fails

If you're getting `exit code: 1` during Docker build, try these steps:

### Step 1: Check Docker Resources
Make sure Docker has enough resources:
- **Memory**: At least 4GB
- **CPU**: At least 2 cores

In Docker Desktop: Settings → Resources → Adjust Memory and CPUs

### Step 2: Clean Docker Cache
```bash
# Remove all build cache
docker builder prune -a

# Or do a clean build
docker build --no-cache -t library-backend:latest .
```

### Step 3: Check Detailed Build Logs
```bash
# Build with verbose output
docker build --progress=plain --no-cache -t library-backend:latest . 2>&1 | tee build.log

# Check the build.log file for errors
```

### Step 4: Test Maven Build Locally (if Maven is installed)
```bash
# Clean and compile
mvn clean compile

# If successful, build package
mvn clean package -DskipTests
```

### Step 5: Common Issues and Solutions

#### Issue: "Failed to execute goal...compiler"
**Solution**: Java version mismatch
- Make sure you're using Java 17
- Check Dockerfile uses correct Maven image

#### Issue: "Could not resolve dependencies"
**Solution**: Network/proxy issues
- Check internet connection
- Try adding Maven mirror in pom.xml
- Check if corporate proxy is blocking Maven Central

#### Issue: "OutOfMemoryError"
**Solution**: Increase Docker memory
- Docker Desktop → Settings → Resources → Memory: 6GB+

#### Issue: "No space left on device"
**Solution**: Clean up Docker
```bash
docker system prune -a --volumes
```

### Step 6: Alternative - Build Without Docker

If Docker build keeps failing, you can run locally:

1. **Install Prerequisites:**
   - Java 17
   - Maven 3.6+
   - PostgreSQL 16
   - Redis 7

2. **Setup Database:**
```bash
# PostgreSQL
docker run -d --name postgres -p 5432:5432 \
  -e POSTGRES_DB=library_db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  postgres:16-alpine

# Redis
docker run -d --name redis -p 6379:6379 redis:7-alpine
```

3. **Run Application:**
```bash
mvn spring-boot:run
```

### Step 7: Check Specific Files

Make sure these files exist and are not corrupted:
- `pom.xml`
- `src/main/java/com/library/SecurityBackendApplication.java`
- `src/main/resources/application.yml`

### Step 8: Windows-Specific Issues

#### Line Ending Issues
If files were created on Windows, line endings might cause issues:
```bash
# Configure Git to handle line endings
git config --global core.autocrlf true

# Re-clone or checkout files
git checkout -- .
```

#### Path Length Issues
Windows has a 260 character path limit. Make sure your project path is short:
- Good: `C:\projects\security-backend`
- Bad: `C:\Users\YourName\Documents\Projects\JavaProjects\SpringBoot\security-backend`

### Step 9: Simplify Dockerfile for Testing

Try this minimal Dockerfile to test:

```dockerfile
FROM maven:3.9.5-eclipse-temurin-17

WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

CMD ["java", "-jar", "target/security-backend-1.0.0.jar"]
```

### Step 10: Check Docker Desktop Status

Make sure Docker Desktop is running and healthy:
- Check Docker Desktop tray icon
- Try: `docker ps`
- Try: `docker version`

## Still Having Issues?

Create an issue with:
1. Full build output (from Step 3)
2. Your system info:
   - OS version
   - Docker version: `docker --version`
   - Available memory: `docker system info | grep Memory`
3. Last 50 lines of build log

## Quick Fix: Use Pre-built Approach

If you just want to test the application quickly:

1. Don't use Docker for now
2. Install Java 17 and Maven
3. Run locally with embedded H2 database:

Update `application.yml` temporarily:
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    database-platform: org.hibernate.dialect.H2Dialect
```

Then run:
```bash
mvn spring-boot:run
```

---

**Most Common Solution**: Increase Docker Desktop memory to 6GB and do a clean build:
```bash
docker builder prune -a
docker build --no-cache -t library-backend:latest .
```

