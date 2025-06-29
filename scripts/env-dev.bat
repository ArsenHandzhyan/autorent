@echo off
echo Установка переменных окружения для DEV профиля...
echo.

set SPRING_PROFILES_ACTIVE=dev
set SERVER_PORT=8080
set DB_HOST=uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com
set DB_PORT=3306
set DB_NAME=cmwz7gjxubq6sk64
set DB_USER=wm02va8ppexvexe1
set DB_PASS=srj7xmugajaa2ww3

echo SPRING_PROFILES_ACTIVE=%SPRING_PROFILES_ACTIVE%
echo SERVER_PORT=%SERVER_PORT%
echo DB_HOST=%DB_HOST%
echo DB_PORT=%DB_PORT%
echo DB_NAME=%DB_NAME%
echo DB_USER=%DB_USER%
echo.

echo Переменные окружения установлены для DEV профиля
echo Приложение будет использовать AWS RDS MySQL базу данных
echo.
pause 