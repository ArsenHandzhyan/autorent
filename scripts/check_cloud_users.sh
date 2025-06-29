#!/bin/bash

echo "====================================================="
echo "ПРОВЕРКА ПОЛЬЗОВАТЕЛЕЙ В ОБЛАЧНОЙ БД AUTORENT"
echo "====================================================="
echo

echo "Подключение к облачной базе данных..."
echo "Host: uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com"
echo "Database: cmwz7gjxubq6sk64"
echo

# Проверяем наличие MySQL клиента
if ! command -v mysql &> /dev/null; then
    echo "ОШИБКА: MySQL клиент не найден!"
    echo "Установите MySQL Client:"
    echo "  Ubuntu/Debian: sudo apt-get install mysql-client"
    echo "  CentOS/RHEL: sudo yum install mysql"
    echo "  macOS: brew install mysql-client"
    exit 1
fi

echo "Выполняем проверку пользователей..."
echo

# Подключаемся к облачной БД и выполняем скрипт
mysql -h uf63wl4z2daq9dbb.chr7pe7iynqr.eu-west-1.rds.amazonaws.com -P 3306 -u wm02va8ppexvexe1 -psrj7xmugajaa2ww3 cmwz7gjxubq6sk64 < "../database/check_users_cloud.sql"

if [ $? -eq 0 ]; then
    echo
    echo "====================================================="
    echo "ПРОВЕРКА ЗАВЕРШЕНА УСПЕШНО"
    echo "====================================================="
else
    echo
    echo "====================================================="
    echo "ОШИБКА ПРИ ВЫПОЛНЕНИИ ПРОВЕРКИ"
    echo "====================================================="
fi

echo 