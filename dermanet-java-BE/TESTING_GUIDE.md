# Testing Guide

## Prerequisites

- Application running on `http://localhost:5000`
- PostgreSQL and Redis running
- Postman or curl for testing

## Test Sequence

### 1. Health Check

```bash
curl http://localhost:5000/api/health
```

Expected: `{"status":"UP","message":"Dermanet API is running"}`

### 2. Sign Up

```bash
curl -X POST http://localhost:5000/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test User",
    "email": "test@example.com",
    "password": "password123"
  }' \
  -c cookies.txt
```

### 3. Login

```bash
curl -X POST http://localhost:5000/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test@example.com",
    "password": "password123"
  }' \
  -c cookies.txt
```

### 4. Get Profile

```bash
curl http://localhost:5000/api/auth/profile \
  -b cookies.txt
```

### 5. Create Product (Admin Only)

First, create an admin user in the database or change the role of existing user to ADMIN.

```bash
curl -X POST http://localhost:5000/api/products \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "name": "Test Product",
    "description": "A test product",
    "price": 29.99,
    "image": "https://via.placeholder.com/300",
    "category": "electronics",
    "isFeatured": false
  }'
```

### 6. Get Featured Products

```bash
curl http://localhost:5000/api/products/featured
```

### 7. Get Products by Category

```bash
curl http://localhost:5000/api/products/category/electronics
```

### 8. Add to Cart

```bash
curl -X POST http://localhost:5000/api/cart \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "productId": 1,
    "quantity": 2
  }'
```

### 9. Get Cart

```bash
curl http://localhost:5000/api/cart \
  -b cookies.txt
```

### 10. Update Cart Quantity

```bash
curl -X PUT http://localhost:5000/api/cart/1 \
  -H "Content-Type: application/json" \
  -b cookies.txt \
  -d '{
    "quantity": 3
  }'
```

### 11. Get Analytics (Admin Only)

```bash
curl http://localhost:5000/api/analytics \
  -b cookies.txt
```

### 12. Logout

```bash
curl -X POST http://localhost:5000/api/auth/logout \
  -b cookies.txt
```

## Postman Collection

Import this JSON into Postman:

```json
{
  "info": {
    "name": "Dermanet API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Auth",
      "item": [
        {
          "name": "Sign Up",
          "request": {
            "method": "POST",
            "header": [],
            "body": {
              "mode": "raw",
              "raw": "{\n  \"name\": \"Test User\",\n  \"email\": \"test@example.com\",\n  \"password\": \"password123\"\n}",
              "options": {
                "raw": {
                  "language": "json"
                }
              }
            },
            "url": {
              "raw": "http://localhost:5000/api/auth/signup",
              "protocol": "http",
              "host": ["localhost"],
              "port": "5000",
              "path": ["api", "auth", "signup"]
            }
          }
        }
      ]
    }
  ]
}
```

## Common Issues

### 1. 401 Unauthorized
- Make sure you're logged in and cookies are being sent
- Check if the access token has expired (15 minutes)
- Try refreshing the token

### 2. 403 Forbidden
- Endpoint requires admin role
- Check user role in database

### 3. 404 Not Found
- Check if the endpoint URL is correct
- Verify the resource exists (product, cart item, etc.)

### 4. 500 Internal Server Error
- Check application logs
- Verify database connection
- Check Redis connection

## Database Verification

Connect to PostgreSQL and verify data:

```sql
-- Check users
SELECT * FROM users;

-- Check products
SELECT * FROM products;

-- Check orders
SELECT * FROM orders;

-- Check cart items
SELECT * FROM cart_items;

-- Check coupons
SELECT * FROM coupons;
```

## Redis Verification

Connect to Redis and check cached data:

```bash
redis-cli
> KEYS *
> GET featured_products
> GET refresh_token:1
```
