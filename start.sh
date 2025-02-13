#!/bin/bash

# Start MySQL service
service mysql start

# Wait for MySQL to be ready
until mysqladmin ping -h "localhost" --port=3356 --silent; do
    echo "Waiting for MySQL..."
    sleep 1
done

# Run Spring Boot application
java -jar /app/app.jar --server.port=58080
