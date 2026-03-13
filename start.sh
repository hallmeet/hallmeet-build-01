#!/bin/bash

# Hall Ticket Generation System - Quick Start Script
# This script starts MySQL Docker container and runs the application

echo "========================================="
echo "Hall Ticket Generation System"
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

# Check if JDK (javac) is installed (required for compilation)
if ! command -v javac &> /dev/null; then
    echo "❌ Java Development Kit (JDK) is not installed."
    echo "   Only Java Runtime Environment (JRE) is installed."
    echo "   Maven needs the JDK to compile the project."
    echo ""
    echo "   Please install Java 21 JDK:"
    echo "   sudo apt install openjdk-21-jdk-headless"
    echo ""
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

# Check if JAR file exists
if [ ! -f "target/Hall_Ticket-0.0.1-SNAPSHOT.jar" ]; then
    echo "📦 Building project..."
    chmod +x mvnw
    ./mvnw clean package -DskipTests
    if [ $? -ne 0 ]; then
        echo "❌ Build failed"
        exit 1
    fi
    echo "✅ Build successful"
fi

echo ""
echo "🚀 Starting application..."
echo "📍 Application will be available at: http://localhost:8080"
echo ""

# Run the application
java -jar target/Hall_Ticket-0.0.1-SNAPSHOT.jar



