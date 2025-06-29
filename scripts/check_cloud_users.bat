@echo off
echo =====================================================
echo ПРОВЕРКА ПОЛЬЗОВАТЕЛЕЙ В ОБЛАЧНОЙ БД AUTORENT
echo =====================================================
echo.

echo Подключение к облачной базе данных...
echo Host: uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com
echo Database: cmwz7gjxubq6sk64
echo.

REM Проверяем наличие MySQL клиента
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ОШИБКА: MySQL клиент не найден!
    echo Установите MySQL Client или добавьте его в PATH
    pause
    exit /b 1
)

echo Выполняем проверку пользователей...
echo.

REM Подключаемся к облачной БД и выполняем скрипт
mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 cmwz7gjxubq6sk64 < "../database/check_users_cloud.sql"

if %errorlevel% equ 0 (
    echo.
    echo =====================================================
    echo ПРОВЕРКА ЗАВЕРШЕНА УСПЕШНО
    echo =====================================================
) else (
    echo.
    echo =====================================================
    echo ОШИБКА ПРИ ВЫПОЛНЕНИИ ПРОВЕРКИ
    echo =====================================================
)

echo.
pause 