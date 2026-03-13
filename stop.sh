#!/bin/bash

# Stop Hall Ticket Generation System

echo "Stopping Hall Ticket Generation System..."
echo ""

# Stop MySQL container
echo "🐳 Stopping MySQL container..."
docker compose down

echo "✅ All services stopped"




