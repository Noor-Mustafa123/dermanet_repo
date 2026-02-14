# Dermanet API Documentation

Base URL: `http://localhost:5000`

## Authentication

All authenticated endpoints require an `accessToken` cookie to be sent with the request.

### Sign Up
```http
POST /api/auth/signup
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "password123"
}
```

### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

### Logout
```http
POST /api/auth/logout
```

### Refresh Token
```http
POST /api/auth/refresh-token
```

### Get Profile
```http
GET /api/auth/profile
```

## Products

### Get All Products (Admin)
```http
GET /api/products
```

### Get Featured Products
```http
GET /api/products/featured
```

### Get Products by Category
```http
GET /api/products/category/{category}
```

### Get Recommended Products
```http
GET /api/products/recommendations
```

### Create Product (Admin)
```http
POST /api/products
Content-Type: application/json

{
  "name": "Product Name",
  "description": "Product description",
  "price": 29.99,
  "image": "data:image/jpeg;base64,...",
  "category": "electronics",
  "isFeatured": false
}
```

### Toggle Featured Product (Admin)
```http
PATCH /api/products/{id}
```

### Delete Product (Admin)
```http
DELETE /api/products/{id}
```

## Cart

### Get Cart Items
```http
GET /api/cart
```

### Add to Cart
```http
POST /api/cart
Content-Type: application/json

{
  "productId": 1,
  "quantity": 2
}
```

### Remove from Cart
```http
DELETE /api/cart?productId={productId}
```

### Remove All from Cart
```http
DELETE /api/cart
```

### Update Quantity
```http
PUT /api/cart/{productId}
Content-Type: application/json

{
  "quantity": 3
}
```

## Coupons

### Get Active Coupon
```http
GET /api/coupons
```

### Validate Coupon
```http
POST /api/coupons/validate
Content-Type: application/json

{
  "code": "GIFT123ABC"
}
```

## Payments

### Create Checkout Session
```http
POST /api/payments/create-checkout-session
Content-Type: application/json

{
  "products": [
    {
      "id": 1,
      "name": "Product Name",
      "price": 29.99,
      "image": "https://...",
      "quantity": 2
    }
  ],
  "couponCode": "GIFT123ABC"
}
```

### Checkout Success
```http
POST /api/payments/checkout-success
Content-Type: application/json

{
  "sessionId": "cs_test_..."
}
```

## Analytics (Admin)

### Get Analytics Data
```http
GET /api/analytics
```

Response:
```json
{
  "analyticsData": {
    "users": 100,
    "products": 50,
    "totalSales": 200,
    "totalRevenue": 5000.00
  },
  "dailySalesData": [
    {
      "date": "2024-01-01",
      "sales": 10,
      "revenue": 250.00
    }
  ]
}
```

## Health Check

### Check API Health
```http
GET /api/health
```

## Error Responses

All endpoints return errors in the following format:

```json
{
  "message": "Error message",
  "error": "Error type",
  "status": 400
}
```

Common HTTP Status Codes:
- 200: Success
- 201: Created
- 400: Bad Request
- 401: Unauthorized
- 403: Forbidden
- 404: Not Found
- 500: Internal Server Error
