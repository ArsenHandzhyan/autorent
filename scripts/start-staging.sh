#!/bin/bash

echo "Starting AutoRent with STAGING profile..."
echo

# Установка профиля
export SPRING_PROFILES_ACTIVE=staging

# Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=staging

echo
echo "Application started with STAGING profile"
echo "Database: staging-mysql-server:3306/autorent_staging"
echo "Application: http://localhost:8080" 