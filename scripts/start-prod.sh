#!/bin/bash

echo "Starting AutoRent with PRODUCTION profile..."
echo

# Установка профиля
export SPRING_PROFILES_ACTIVE=prod

# Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=prod

echo
echo "Application started with PRODUCTION profile"
echo "Database: AWS RDS MySQL (Production)"
echo "Application: http://localhost:8080" 