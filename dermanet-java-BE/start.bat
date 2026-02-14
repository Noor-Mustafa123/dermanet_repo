@echo off
echo Starting Dermanet Java Backend...

REM Start PostgreSQL and Redis
echo Starting PostgreSQL and Redis...
docker-compose up -d

REM Wait for PostgreSQL to be ready
echo Waiting for PostgreSQL to be ready...
timeout /t 5 /nobreak

REM Build and run the application
echo Building and starting the application...
mvn spring-boot:run

pause
