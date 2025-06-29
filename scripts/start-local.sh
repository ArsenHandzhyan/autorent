#!/bin/bash

echo "Starting AutoRent with LOCAL profile..."
echo

# Установка профиля
export SPRING_PROFILES_ACTIVE=local

# Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=local

echo
echo "Application started with LOCAL profile"
echo "Database: localhost:3306/autorent"
echo "Application: http://localhost:8080" 