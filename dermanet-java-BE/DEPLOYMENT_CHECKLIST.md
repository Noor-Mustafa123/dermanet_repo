# Deployment Checklist

Use this checklist to ensure everything is properly configured before deployment.

## ✅ Pre-Deployment Checklist

### 1. Environment Configuration
- [ ] Set all required environment variables
- [ ] Configure database connection (PostgreSQL)
- [ ] Configure Redis connection
- [ ] Set JWT secret keys
- [ ] Configure Stripe API keys
- [ ] Configure Cloudinary credentials
- [ ] Set client URL for CORS
- [ ] Set cookie domain for production

### 2. Database Setup
- [ ] PostgreSQL database created
- [ ] Database user created with proper permissions
- [ ] Connection tested successfully
- [ ] Tables created (auto-created by JPA)
- [ ] Admin user created
- [ ] Sample data loaded (optional)

### 3. Redis Setup
- [ ] Redis server running
- [ ] Connection tested successfully
- [ ] Password configured (if required)

### 4. Security Configuration
- [ ] HTTPS enabled in production
- [ ] Secure cookies enabled (`app.jwt.secureCookie=true`)
- [ ] CORS origins properly configured
- [ ] JWT secrets are strong and unique
- [ ] Database credentials secured

### 5. Application Testing
- [ ] Health check endpoint working (`/api/health`)
- [ ] User signup working
- [ ] User login working
- [ ] JWT token refresh working
- [ ] Product CRUD operations working
- [ ] Cart operations working
- [ ] Coupon validation working
- [ ] Stripe checkout working
- [ ] Analytics endpoint working (admin)

### 6. Performance & Monitoring
- [ ] Redis caching working for featured products
- [ ] Database queries optimized
- [ ] Connection pooling configured
- [ ] Logging configured properly
- [ ] Error tracking set up (optional)
- [ ] Performance monitoring set up (optional)

### 7. Documentation
- [ ] API documentation reviewed
- [ ] Environment variables documented
- [ ] Deployment process documented
- [ ] Rollback procedure documented

## 🚀 Deployment Steps

### Local Development
```bash
# 1. Start dependencies
docker-compose up -d

# 2. Run application
mvn spring-boot:run

# 3. Test
curl http://localhost:5000/api/health
```

### Production Deployment

#### Option 1: Docker
```bash
# 1. Build image
docker build -t dermanet-backend .

# 2. Run container
docker run -d \
  -p 5000:5000 \
  -e DB_HOST=your-db-host \
  -e DB_USERNAME=your-db-user \
  -e DB_PASSWORD=your-db-password \
  -e REDIS_HOST=your-redis-host \
  -e STRIPE_SECRET_KEY=your-stripe-key \
  -e CLOUDINARY_CLOUD_NAME=your-cloud-name \
  -e CLOUDINARY_API_KEY=your-api-key \
  -e CLOUDINARY_API_SECRET=your-api-secret \
  -e JWT_SECRET=your-jwt-secret \
  -e CLIENT_URL=https://your-frontend-url \
  --name dermanet-backend \
  dermanet-backend
```

#### Option 2: JAR Deployment
```bash
# 1. Build JAR
mvn clean package -DskipTests

# 2. Run JAR
java -jar target/dermanet-java-be-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod \
  --spring.datasource.url=jdbc:postgresql://your-db-host:5432/dermanet \
  --spring.datasource.username=your-db-user \
  --spring.datasource.password=your-db-password
```

#### Option 3: Cloud Platform (AWS, Azure, GCP)
- [ ] Configure cloud database (RDS, Azure Database, Cloud SQL)
- [ ] Configure cloud Redis (ElastiCache, Azure Cache, Memorystore)
- [ ] Set up load balancer
- [ ] Configure auto-scaling
- [ ] Set up monitoring and alerts
- [ ] Configure backup strategy

## 🔍 Post-Deployment Verification

### 1. Health Check
```bash
curl https://your-domain.com/api/health
```
Expected: `{"status":"UP","message":"Dermanet API is running"}`

### 2. Authentication Test
```bash
# Sign up
curl -X POST https://your-domain.com/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"name":"Test","email":"test@example.com","password":"test123"}'

# Login
curl -X POST https://your-domain.com/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","password":"test123"}'
```

### 3. Database Connection
- [ ] Check application logs for successful database connection
- [ ] Verify tables are created
- [ ] Test CRUD operations

### 4. Redis Connection
- [ ] Check application logs for successful Redis connection
- [ ] Verify caching is working
- [ ] Test refresh token storage

### 5. External Services
- [ ] Test Stripe integration
- [ ] Test Cloudinary image upload
- [ ] Verify email sending (if configured)

## 🐛 Troubleshooting

### Application Won't Start
1. Check Java version: `java -version` (should be 17+)
2. Check Maven version: `mvn -version` (should be 3.6+)
3. Check logs: `tail -f logs/application.log`
4. Verify environment variables are set

### Database Connection Failed
1. Check database is running
2. Verify connection string
3. Check username/password
4. Verify network connectivity
5. Check firewall rules

### Redis Connection Failed
1. Check Redis is running
2. Verify Redis host and port
3. Check Redis password (if required)
4. Verify network connectivity

### 401 Unauthorized Errors
1. Check JWT secret is configured
2. Verify cookies are being sent
3. Check token expiration
4. Verify CORS configuration

### 500 Internal Server Errors
1. Check application logs
2. Verify database connection
3. Check Redis connection
4. Verify external service credentials

## 📊 Monitoring

### Key Metrics to Monitor
- [ ] Response time
- [ ] Error rate
- [ ] Database connection pool
- [ ] Redis connection pool
- [ ] Memory usage
- [ ] CPU usage
- [ ] Disk usage

### Logging
- [ ] Application logs configured
- [ ] Error logs configured
- [ ] Access logs configured
- [ ] Log rotation configured

## 🔄 Rollback Procedure

If issues occur after deployment:

1. **Stop the new version**
   ```bash
   docker stop dermanet-backend
   # or
   systemctl stop dermanet-backend
   ```

2. **Start the previous version**
   ```bash
   docker start dermanet-backend-old
   # or
   systemctl start dermanet-backend-old
   ```

3. **Verify rollback**
   ```bash
   curl https://your-domain.com/api/health
   ```

4. **Investigate issues**
   - Check logs
   - Review error messages
   - Test in staging environment

## 📝 Notes

- Always test in a staging environment first
- Keep backups of database before major updates
- Document any configuration changes
- Monitor application after deployment
- Have a rollback plan ready

---

**Deployment Date**: _______________
**Deployed By**: _______________
**Version**: _______________
**Notes**: _______________
