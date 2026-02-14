#!/bin/bash

echo "Starting Dermanet Java Backend..."

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    echo "Docker is not running. Please start Docker first."
    exit 1
fi

# Start PostgreSQL and Redis
echo "Starting PostgreSQL and Redis..."
docker-compose up -d

# Wait for PostgreSQL to be ready
echo "Waiting for PostgreSQL to be ready..."
sleep 5

# Build and run the application
echo "Building and starting the application..."
mvn spring-boot:run

# Cleanup function
cleanup() {
    echo "Stopping services..."
    docker-compose down
}

# Register cleanup function
trap cleanup EXIT
