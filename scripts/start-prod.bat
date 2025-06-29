@echo off
echo Starting AutoRent with PRODUCTION profile...
echo.

REM Установка профиля
set SPRING_PROFILES_ACTIVE=prod

REM Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=prod

echo.
echo Application started with PRODUCTION profile
echo Database: AWS RDS MySQL (Production)
echo Application: http://localhost:8080
pause 