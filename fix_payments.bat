@echo off
echo Исправление поврежденных примечаний платежей в облачной базе данных...
echo.

REM Ждем 5 секунд перед выполнением
timeout /t 5 /nobreak >nul

REM Выполняем SQL-скрипт
docker run --rm mysql:8.0 mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 -D cmwz7gjxubq6sk64 < fix_payment_notes.sql

echo.
echo Исправление завершено!
pause 