#!/bin/bash

echo "========================================"
echo "  AutoRent - Предварительный просмотр"
echo "  Thymeleaf шаблонов"
echo "========================================"
echo

# Проверяем наличие Node.js
if ! command -v node &> /dev/null; then
    echo "❌ Ошибка: Node.js не установлен!"
    echo "Установите Node.js с https://nodejs.org/"
    exit 1
fi

echo "✅ Node.js найден"
echo

# Проверяем наличие package.json
if [ ! -f "package.json" ]; then
    echo "❌ Ошибка: package.json не найден!"
    exit 1
fi

echo "✅ package.json найден"
echo

# Устанавливаем зависимости если node_modules не существует
if [ ! -d "node_modules" ]; then
    echo "📦 Установка зависимостей..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ Ошибка установки зависимостей!"
        exit 1
    fi
    echo "✅ Зависимости установлены"
    echo
fi

# Проверяем наличие шаблонов
if [ ! -d "src/main/resources/templates" ]; then
    echo "❌ Ошибка: Папка с шаблонами не найдена!"
    echo "Ожидается: src/main/resources/templates/"
    exit 1
fi

echo "✅ Папка с шаблонами найдена"
echo

echo "🚀 Запуск сервера предварительного просмотра..."
echo
echo "📍 Адрес: http://localhost:3001"
echo "📁 Шаблоны: src/main/resources/templates/"
echo
echo "💡 Нажмите Ctrl+C для остановки сервера"
echo

# Запускаем сервер
npm run preview 