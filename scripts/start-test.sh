#!/bin/bash

echo "Starting AutoRent with TEST profile..."
echo

# Установка профиля
export SPRING_PROFILES_ACTIVE=test

# Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=test

echo
echo "Application started with TEST profile"
echo "Database: test-mysql-server:3306/autorent_test"
echo "Application: http://localhost:8081" 