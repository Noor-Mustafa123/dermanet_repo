# Dermanet Java Backend

Spring Boot backend implementation for Dermanet e-commerce platform.

## Prerequisites

- Java 17 or higher
- Maven 3.6+
- PostgreSQL 12+
- Redis (for caching and session management)

## Environment Variables

Create a `.env` file or set the following environment variables:

```properties
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=dermanet
DB_USERNAME=postgres
DB_PASSWORD=your_password

# JWT
JWT_SECRET=mySecretKeyForDermanetApp
JWT_EXPIRATION=900000
JWT_REFRESH_EXPIRATION=604800000

# Redis
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=

# Cloudinary
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret

# Stripe
STRIPE_SECRET_KEY=your_stripe_secret_key

# Client
CLIENT_URL=http://localhost:5173
ALLOWED_ORIGINS=http://localhost:5173

# Cookie
COOKIE_DOMAIN=localhost
SECURE_COOKIE=false
```

## Database Setup

1. Create PostgreSQL database:
```sql
CREATE DATABASE dermanet;
```

2. The application will automatically create tables on startup using JPA/Hibernate.

## Build and Run

```bash
# Build the project
mvn clean install

# Run the application
mvn spring-boot:run
```

The server will start on `http://localhost:5000`

## API Endpoints

### Authentication
- POST `/api/auth/signup` - Register new user
- POST `/api/auth/login` - Login user
- POST `/api/auth/logout` - Logout user
- POST `/api/auth/refresh-token` - Refresh access token
- GET `/api/auth/profile` - Get user profile

### Products
- GET `/api/products` - Get all products (Admin only)
- GET `/api/products/featured` - Get featured products
- GET `/api/products/category/{category}` - Get products by category
- GET `/api/products/recommendations` - Get recommended products
- POST `/api/products` - Create product (Admin only)
- PATCH `/api/products/{id}` - Toggle featured status (Admin only)
- DELETE `/api/products/{id}` - Delete product (Admin only)

### Cart
- GET `/api/cart` - Get cart items
- POST `/api/cart` - Add item to cart
- DELETE `/api/cart` - Remove items from cart
- PUT `/api/cart/{id}` - Update item quantity

### Coupons
- GET `/api/coupons` - Get active coupon
- POST `/api/coupons/validate` - Validate coupon code

### Payments
- POST `/api/payments/create-checkout-session` - Create Stripe checkout session
- POST `/api/payments/checkout-success` - Handle successful payment

### Analytics
- GET `/api/analytics` - Get analytics data (Admin only)

## Project Structure

```
src/main/java/io/github/nomus/dermanet/
├── config/              # Configuration classes
├── controller/          # REST controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/           # Exception handling
├── repository/          # JPA repositories
├── security/            # Security configuration
└── service/             # Business logic
```

## Technologies Used

- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- Redis
- JWT (JSON Web Tokens)
- Stripe API
- Cloudinary
- Lombok
