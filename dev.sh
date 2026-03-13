#!/bin/bash

# Hall Ticket Generation System - Development Script with Hot Reload
# This script runs the application in development mode with hot reload enabled

echo "========================================="
echo "Hall Ticket Generation System"
echo "Development Mode with Hot Reload"
echo "========================================="
echo ""

# Check if Docker is installed
if ! command -v docker &> /dev/null; then
    echo "❌ Docker is not installed. Please install Docker first."
    exit 1
fi

# Check if Docker Compose is available
if ! command -v docker compose &> /dev/null; then
    echo "❌ Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

# Check if Java is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java is not installed. Please install Java 21 JDK first."
    exit 1
fi

# Check if JDK (javac) is installed
if ! command -v javac &> /dev/null; then
    echo "❌ Java Development Kit (JDK) is not installed."
    echo "   Please install Java 21 JDK:"
    echo "   sudo apt install openjdk-21-jdk-headless"
    exit 1
fi

echo "✅ Prerequisites check passed"
echo ""

# Start MySQL container
echo "🐳 Starting MySQL container..."
docker compose up -d

# Wait for MySQL to be ready
echo "⏳ Waiting for MySQL to be ready..."
sleep 5

# Check if MySQL is running
if docker ps | grep -q hall_ticket_mysql; then
    echo "✅ MySQL container is running"
else
    echo "❌ Failed to start MySQL container"
    exit 1
fi

echo ""
echo "🔥 Starting application in DEVELOPMENT mode with HOT RELOAD..."
echo "📍 Application will be available at: http://localhost:8080"
echo "🔄 Hot reload is ENABLED - changes will auto-reload!"
echo ""
echo "💡 Tips:"
echo "   - Java code changes: Auto-reloads (may take 2-5 seconds)"
echo "   - Template/Resource changes: Auto-reloads (may take 1-2 seconds)"
echo "   - Press Ctrl+C to stop"
echo ""

# Run the application using Maven (devtools will be active)
chmod +x mvnw
./mvnw spring-boot:run


