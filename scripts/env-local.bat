@echo off
echo Установка переменных окружения для LOCAL профиля...
echo.

set SPRING_PROFILES_ACTIVE=local
set SERVER_PORT=8080
set DB_HOST=localhost
set DB_PORT=3306
set DB_NAME=autorent
set DB_USER=root
set DB_PASS=root

echo SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
echo SERVER_PORT=%SERVER_PORT%
echo DB_HOST=%DB_HOST%
echo DB_PORT=%DB_PORT%
echo DB_NAME=%DB_NAME%
echo DB_USER=%DB_USER%
echo.

echo Переменные окружения установлены для LOCAL профиля
echo Приложение будет использовать локальную MySQL базу данных
echo.
pause 