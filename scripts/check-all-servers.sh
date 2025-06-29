#!/bin/bash

echo "========================================"
echo "   Проверка доступности MSP серверов"
echo "========================================"
echo

echo "[1/6] Проверка локального сервера (localhost:3306)..."
if ping -c 1 localhost >/dev/null 2>&1; then
    echo "    ✓ Локальный сервер доступен"
else
    echo "    ✗ Локальный сервер недоступен"
fi
echo

echo "[2/6] Проверка dev сервера (AWS RDS)..."
if ping -c 1 uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com >/dev/null 2>&1; then
    echo "    ✓ Dev сервер доступен"
else
    echo "    ✗ Dev сервер недоступен"
fi
echo

echo "[3/6] Проверка test сервера..."
if ping -c 1 test-mysql-server >/dev/null 2>&1; then
    echo "    ✓ Test сервер доступен"
else
    echo "    ✗ Test сервер недоступен (ожидаемо для локальной разработки)"
fi
echo

echo "[4/6] Проверка staging сервера..."
if ping -c 1 staging-mysql-server >/dev/null 2>&1; then
    echo "    ✓ Staging сервер доступен"
else
    echo "    ✗ Staging сервер недоступен (ожидаемо для локальной разработки)"
fi
echo

echo "[5/6] Проверка prod сервера (AWS RDS)..."
if ping -c 1 uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com >/dev/null 2>&1; then
    echo "    ✓ Prod сервер доступен"
else
    echo "    ✗ Prod сервер недоступен"
fi
echo

echo "[6/6] Проверка backup сервера..."
if ping -c 1 backup-mysql-server >/dev/null 2>&1; then
    echo "    ✓ Backup сервер доступен"
else
    echo "    ✗ Backup сервер недоступен (ожидаемо для локальной разработки)"
fi
echo

echo "========================================"
echo "   Проверка завершена"
echo "========================================"
echo
echo "Для детальной проверки подключений к БД:"
echo "1. Запустите приложение с нужным профилем"
echo "2. Используйте API: GET /api/database/health" 