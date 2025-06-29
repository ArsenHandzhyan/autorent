@echo off
echo Starting AutoRent with STAGING profile...
echo.

REM Установка профиля
set SPRING_PROFILES_ACTIVE=staging

REM Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=staging

echo.
echo Application started with STAGING profile
echo Database: staging-mysql-server:3306/autorent_staging
echo Application: http://localhost:8080
pause 