@echo off
echo ========================================
echo    Проверка доступности MSP серверов
echo ========================================
echo.

echo [1/6] Проверка локального сервера (localhost:3306)...
ping -n 1 localhost >nul 2>&1
if %errorlevel% equ 0 (
    echo    ✓ Локальный сервер доступен
) else (
    echo    ✗ Локальный сервер недоступен
)
echo.

echo [2/6] Проверка dev сервера (AWS RDS)...
ping -n 1 uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com >nul 2>&1
if %errorlevel% equ 0 (
    echo    ✓ Dev сервер доступен
) else (
    echo    ✗ Dev сервер недоступен
)
echo.

echo [3/6] Проверка test сервера...
ping -n 1 test-mysql-server >nul 2>&1
if %errorlevel% equ 0 (
    echo    ✓ Test сервер доступен
) else (
    echo    ✗ Test сервер недоступен (ожидаемо для локальной разработки)
)
echo.

echo [4/6] Проверка staging сервера...
ping -n 1 staging-mysql-server >nul 2>&1
if %errorlevel% equ 0 (
    echo    ✓ Staging сервер доступен
) else (
    echo    ✗ Staging сервер недоступен (ожидаемо для локальной разработки)
)
echo.

echo [5/6] Проверка prod сервера (AWS RDS)...
ping -n 1 uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com >nul 2>&1
if %errorlevel% equ 0 (
    echo    ✓ Prod сервер доступен
) else (
    echo    ✗ Prod сервер недоступен
)
echo.

echo [6/6] Проверка backup сервера...
ping -n 1 backup-mysql-server >nul 2>&1
if %errorlevel% equ 0 (
    echo    ✓ Backup сервер доступен
) else (
    echo    ✗ Backup сервер недоступен (ожидаемо для локальной разработки)
)
echo.

echo ========================================
echo    Проверка завершена
echo ========================================
echo.
echo Для детальной проверки подключений к БД:
echo 1. Запустите приложение с нужным профилем
echo 2. Используйте API: GET /api/database/health
echo.
pause 