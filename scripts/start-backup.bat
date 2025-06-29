@echo off
echo Starting AutoRent with BACKUP profile...
echo.

REM Установка профиля
set SPRING_PROFILES_ACTIVE=backup

REM Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=backup

echo.
echo Application started with BACKUP profile
echo Database: backup-mysql-server:3306/autorent_backup
echo Application: http://localhost:8082
pause 