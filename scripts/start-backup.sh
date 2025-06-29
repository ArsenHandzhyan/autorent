#!/bin/bash

echo "Starting AutoRent with BACKUP profile..."
echo

# Установка профиля
export SPRING_PROFILES_ACTIVE=backup

# Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=backup

echo
echo "Application started with BACKUP profile"
echo "Database: backup-mysql-server:3306/autorent_backup"
echo "Application: http://localhost:8082" 