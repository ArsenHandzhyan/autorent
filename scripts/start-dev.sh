#!/bin/bash

echo "Starting AutoRent with DEV profile..."
echo

# Установка профиля
export SPRING_PROFILES_ACTIVE=dev

# Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo
echo "Application started with DEV profile"
echo "Database: AWS RDS MySQL"
echo "Application: http://localhost:8080" 