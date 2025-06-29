@echo off
echo Starting AutoRent with DEV profile...
echo.

REM Установка профиля
set SPRING_PROFILES_ACTIVE=dev

REM Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=dev

echo.
echo Application started with DEV profile
echo Database: AWS RDS MySQL
echo Application: http://localhost:8080
pause 