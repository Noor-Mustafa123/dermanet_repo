# Quick Start Guide

Get the Dermanet Java backend running in 5 minutes!

## Prerequisites

- Java 17+ installed
- Maven 3.6+ installed
- Docker Desktop running

## Step 1: Start Database Services

```bash
cd dermanet-java-BE
docker-compose up -d
```

This starts PostgreSQL and Redis in Docker containers.

## Step 2: Configure Environment

Create a `.env` file or set these environment variables:

```bash
# Minimum required configuration
export STRIPE_SECRET_KEY=your_stripe_key
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
```

Or use the defaults in `application.properties` for local development.

## Step 3: Run the Application

### Option A: Using Maven
```bash
mvn spring-boot:run
```

### Option B: Using the startup script
```bash
./start.sh  # Linux/Mac
start.bat   # Windows
```

## Step 4: Verify It's Running

```bash
curl http://localhost:5000/api/health
```

Expected response:
```json
{
  "status": "UP",
  "message": "Dermanet API is running"
}
```

## Step 5: Create an Admin User

### Option 1: Using SQL
```sql
-- Connect to PostgreSQL
docker exec -it dermanet-postgres psql -U postgres -d dermanet

-- Generate password hash first using PasswordUtil.java
-- Then insert admin user
INSERT INTO users (name, email, password, role, created_at, updated_at)
VALUES ('Admin', 'admin@dermanet.com', '$2a$10$...', 'ADMIN', NOW(), NOW());
```

### Option 2: Sign up and manually change role
```bash
# Sign up as regular user
curl -X POST http://localhost:5000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Admin","email":"admin@dermanet.com","password":"admin123"}'

# Then update role in database
docker exec -it dermanet-postgres psql -U postgres -d dermanet
UPDATE users SET role = 'ADMIN' WHERE email = 'admin@dermanet.com';
```

## Step 6: Test the API

```bash
# Login
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@dermanet.com","password":"admin123"}' \
  -c cookies.txt

# Create a product
curl -X POST http://localhost:5000/api/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 29.99,
    "image": "https://via.placeholder.com/300",
    "category": "electronics"
  }'

# Get featured products
curl http://localhost:5000/api/products/featured
```

## Troubleshooting

### Port 5000 already in use
```bash
# Change port in application.properties
server.port=8080
```

### Database connection failed
```bash
# Check if PostgreSQL is running
docker ps | grep postgres

# Check logs
docker logs dermanet-postgres
```

### Redis connection failed
```bash
# Check if Redis is running
docker ps | grep redis

# Check logs
docker logs dermanet-redis
```

### Application won't start
```bash
# Check Java version
java -version  # Should be 17+

# Check Maven version
mvn -version   # Should be 3.6+

# Clean and rebuild
mvn clean install
```

## What's Next?

- Read [API_DOCUMENTATION.md](API_DOCUMENTATION.md) for all endpoints
- Follow [TESTING_GUIDE.md](TESTING_GUIDE.md) for comprehensive testing
- Check [MIGRATION_GUIDE.md](MIGRATION_GUIDE.md) for migration details
- See [IMPLEMENTATION_SUMMARY.md](IMPLEMENTATION_SUMMARY.md) for what's included

## Stopping the Application

```bash
# Stop the Java application
Ctrl+C

# Stop Docker containers
docker-compose down
```

## Production Deployment

For production deployment:

1. Set `spring.profiles.active=prod`
2. Configure production database
3. Set secure environment variables
4. Enable HTTPS
5. Configure proper CORS origins
6. Set up monitoring and logging

See README.md for detailed production setup.

---

**Need Help?** Check the documentation files or create an issue.
