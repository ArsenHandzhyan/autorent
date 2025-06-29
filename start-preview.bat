@echo off
echo ========================================
echo   AutoRent - Предварительный просмотр
echo   Thymeleaf шаблонов
echo ========================================
echo.

REM Проверяем наличие Node.js
node --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ❌ Ошибка: Node.js не установлен!
    echo Установите Node.js с https://nodejs.org/
    pause
    exit /b 1
)

echo ✅ Node.js найден
echo.

REM Проверяем наличие package.json
if not exist "package.json" (
    echo ❌ Ошибка: package.json не найден!
    pause
    exit /b 1
)

echo ✅ package.json найден
echo.

REM Устанавливаем зависимости если node_modules не существует
if not exist "node_modules" (
    echo 📦 Установка зависимостей...
    npm install
    if %errorlevel% neq 0 (
        echo ❌ Ошибка установки зависимостей!
        pause
        exit /b 1
    )
    echo ✅ Зависимости установлены
    echo.
)

REM Проверяем наличие шаблонов
if not exist "src\main\resources\templates" (
    echo ❌ Ошибка: Папка с шаблонами не найдена!
    echo Ожидается: src\main\resources\templates\
    pause
    exit /b 1
)

echo ✅ Папка с шаблонами найдена
echo.

echo 🚀 Запуск сервера предварительного просмотра...
echo.
echo 📍 Адрес: http://localhost:3001
echo 📁 Шаблоны: src\main\resources\templates\
echo.
echo 💡 Нажмите Ctrl+C для остановки сервера
echo.

REM Запускаем сервер
npm run preview

pause 