# 🗄️ MSP Серверы - Краткая инструкция

## 🚀 Быстрый старт

### Windows
```bash
# Проверка доступности всех серверов
scripts\check-all-servers.bat

# Запуск с локальным профилем
scripts\start-local.bat

# Запуск с dev профилем
scripts\start-dev.bat

# Установка переменных окружения
scripts\env-local.bat
scripts\env-dev.bat
```

### Linux/Mac
```bash
# Сделать скрипты исполняемыми
chmod +x scripts/*.sh

# Проверка доступности всех серверов
./scripts/check-all-servers.sh

# Запуск с локальным профилем
./scripts/start-local.sh

# Запуск с dev профилем
./scripts/start-dev.sh
```

## 📊 Мониторинг

После запуска приложения используйте API для мониторинга:

```bash
# Проверка здоровья БД
curl -X GET http://localhost:8080/api/database/health

# Полная диагностика
curl -X GET http://localhost:8080/api/database/diagnostic

# Информация о профиле
curl -X GET http://localhost:8080/api/database/info
```

## 🔧 Профили

| Профиль | Описание | Порт |
|---------|----------|------|
| `local` | Локальная разработка | 8080 |
| `dev` | Разработка (AWS RDS) | 8080 |
| `test` | Тестирование | 8081 |
| `staging` | Предпродакшн | 8080 |
| `prod` | Продакшн (AWS RDS) | 8080 |
| `backup` | Резервный сервер | 8082 |

## 📖 Подробная документация

См. [docs/MSP_SERVERS.md](../docs/MSP_SERVERS.md) для полной документации. 