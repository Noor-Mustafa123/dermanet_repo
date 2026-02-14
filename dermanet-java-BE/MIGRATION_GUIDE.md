# Migration Guide: Node.js to Java Spring Boot

## Overview

This document outlines the migration from the Node.js/Express backend to Java Spring Boot.

## Key Differences

### 1. Framework
- **Node.js**: Express.js
- **Java**: Spring Boot with Spring MVC

### 2. Database Access
- **Node.js**: Mongoose ODM for MongoDB
- **Java**: Spring Data JPA with PostgreSQL

### 3. Authentication
- **Node.js**: JWT with manual cookie handling
- **Java**: JWT with Spring Security

### 4. Dependency Management
- **Node.js**: npm/package.json
- **Java**: Maven/pom.xml

## API Endpoint Mapping

All endpoints remain the same. The base URL is `http://localhost:5000/api`

| Feature | Node.js Route | Java Controller |
|---------|--------------|-----------------|
| Auth | `/api/auth/*` | `AuthController` |
| Products | `/api/products/*` | `ProductController` |
| Cart | `/api/cart/*` | `CartController` |
| Coupons | `/api/coupons/*` | `CouponController` |
| Payments | `/api/payments/*` | `PaymentController` |
| Analytics | `/api/analytics/*` | `AnalyticsController` |

## Data Model Changes

### MongoDB → PostgreSQL

The data models have been converted from MongoDB documents to JPA entities:

#### User
- `_id` → `id` (Long)
- `cartItems` now uses a separate `CartItem` entity with proper relationships

#### Product
- `_id` → `id` (Long)
- All fields remain similar with proper JPA annotations

#### Order
- `_id` → `id` (Long)
- `products` array → `orderItems` (One-to-Many relationship)

#### Coupon
- `_id` → `id` (Long)
- `userId` → `user` (Many-to-One relationship)

## Configuration Changes

### Environment Variables

Node.js `.env`:
```
MONGO_URI=mongodb://...
UPSTASH_REDIS_URL=redis://...
```

Java `application.properties`:
```
spring.datasource.url=jdbc:postgresql://localhost:5432/dermanet
spring.redis.host=localhost
spring.redis.port=6379
```

## Running the Application

### Node.js
```bash
npm install
npm run dev
```

### Java
```bash
mvn clean install
mvn spring-boot:run
```

Or with Docker:
```bash
docker-compose up -d  # Start PostgreSQL and Redis
mvn spring-boot:run   # Start the application
```

## Testing the Migration

1. Start the Java backend
2. Test each endpoint using the same requests as the Node.js version
3. Verify data persistence in PostgreSQL
4. Check Redis caching for featured products

## Benefits of Java/Spring Boot

1. **Type Safety**: Compile-time type checking
2. **Better IDE Support**: Enhanced autocomplete and refactoring
3. **Enterprise Features**: Built-in support for transactions, caching, security
4. **Performance**: Better performance for CPU-intensive operations
5. **Scalability**: Better suited for large-scale applications

## Potential Issues

1. **Database Migration**: Need to migrate data from MongoDB to PostgreSQL
2. **Date Handling**: Different date/time handling between JavaScript and Java
3. **JSON Serialization**: Different default behaviors
4. **Error Messages**: May need to adjust error message formats

## Next Steps

1. Set up PostgreSQL database
2. Configure environment variables
3. Run the application
4. Test all endpoints
5. Migrate existing data (if any)
6. Update frontend to handle any response format differences
