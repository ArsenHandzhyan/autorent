@echo off
echo Starting AutoRent with LOCAL profile...
echo.

REM Установка профиля
set SPRING_PROFILES_ACTIVE=local

REM Запуск приложения
mvn spring-boot:run -Dspring-boot.run.profiles=local

echo.
echo Application started with LOCAL profile
echo Database: localhost:3306/autorent
echo Application: http://localhost:8080
pause 