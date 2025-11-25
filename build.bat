@echo off
echo Building Library Management System Backend...
echo.

REM Clean up old containers and images
echo Cleaning up old containers...
docker-compose down 2>nul

echo.
echo Building Docker image...
docker build -t library-backend:latest . || (
    echo.
    echo ========================================
    echo Build failed! Checking for common issues...
    echo ========================================
    echo.
    echo Checking if source files exist...
    if not exist "src\main\java\com\library\SecurityBackendApplication.java" (
        echo ERROR: Main application file not found!
        exit /b 1
    )
    echo Source files OK
    echo.
    echo Checking pom.xml...
    if not exist "pom.xml" (
        echo ERROR: pom.xml not found!
        exit /b 1
    )
    echo pom.xml OK
    echo.
    echo Try running: docker build --no-cache -t library-backend:latest .
    echo Or check Docker logs for more details
    exit /b 1
)

echo.
echo ========================================
echo Build successful!
echo ========================================
echo.
echo Starting services with docker-compose...
docker-compose up -d

echo.
echo ========================================
echo Application started!
echo ========================================
echo.
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo API Docs: http://localhost:8080/api-docs
echo.
echo View logs: docker-compose logs -f app
echo Stop services: docker-compose down
echo.

