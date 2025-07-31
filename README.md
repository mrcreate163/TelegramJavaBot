# AI Content Generator Telegram Bot

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-green)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue)
![Docker](https://img.shields.io/badge/Docker-ready-blue)

## 📋 О проекте

Telegram-бот для генерации контента с помощью AI. Помогает создавать посты для социальных сетей, сценарии для видео, хештеги и другой контент. Проект разработан как демонстрация навыков backend-разработки на Java/Spring Boot.

## 🚀 Возможности

- **Генерация различных типов контента**: посты, сценарии для Reels, идеи для Stories, хештеги, заголовки
- **Настройка параметров AI**: язык ответов, длина контента, стиль общения
- **Управление идеями**: сохранение, просмотр истории, изменение статусов
- **Гибкая архитектура**: легко добавлять новые типы контента и AI-провайдеры

## 🛠 Технологии

### Backend
- **Java 17** - основной язык разработки
- **Spring Boot 3.5.4** - фреймворк для создания приложения
- **Spring Data JPA** - работа с базой данных
- **PostgreSQL** - основная база данных
- **Telegram Bot API** - интеграция с Telegram
- **WebClient** - асинхронные HTTP-запросы к AI API

### DevOps
- **Docker** - контейнеризация приложения
- **Docker Compose** - оркестрация сервисов
- **Maven** - сборка проекта

## 📁 Структура проекта

```
src/main/java/prototype/javabot/
├── bot/                    # Telegram bot логика
│   └── TelegramBot.java   # Основной класс бота
├── config/                 # Конфигурация
├── controller/             # REST API endpoints
├── model/                  # Доменные модели
│   ├── ContentIdea.java   # Сущность для хранения идей
│   ├── ContentType.java   # Типы контента
│   └── aiSettings/        # Настройки AI
├── repository/             # JPA репозитории
├── service/               # Бизнес-логика
│   ├── AiService.java     # Интеграция с AI
│   ├── ContentService.java # Управление контентом
│   └── UserStateService.java # Состояние пользователей
└── TelegramJavaBotApplication.java
```

## 🚦 Запуск проекта

### Требования
- Java 17+
- Docker и Docker Compose
- PostgreSQL (или используйте Docker)

### Локальный запуск

1. Клонируйте репозиторий:
```bash
git clone https://github.com/yourusername/telegram-content-bot.git
cd telegram-content-bot
```

2. Запустите PostgreSQL через Docker:
```bash
docker-compose up -d
```

3. Создайте файл `application-local.yaml` в `src/main/resources/`:
```yaml
telegram:
  bot:
    username: your_bot_username
    token: your_bot_token

openrouter:
  api-key: your_api_key
```

4. Запустите приложение:
```bash
./mvnw spring-boot:run -Dspring.profiles.active=local
```

### Запуск через Docker

1. Соберите образ:
```bash
docker build -t telegram-content-bot .
```

2. Запустите через docker-compose:
```bash
docker-compose up
```

## 💡 Архитектурные решения

### Паттерны проектирования

1. **Service Layer Pattern** - бизнес-логика отделена от контроллеров
2. **Repository Pattern** - абстракция доступа к данным через JPA
3. **State Pattern** - управление состоянием пользователей через `UserStateService`
4. **Strategy Pattern** (заложен в архитектуру) - возможность смены AI-провайдеров

### Обработка ошибок

- Graceful error handling в боте с информативными сообщениями пользователю
- Логирование всех критических операций
- Fallback механизмы для сохранения слишком длинного контента

### Масштабируемость

- Stateless архитектура (состояние в памяти для MVP, легко мигрировать на Redis)
- Возможность горизонтального масштабирования
- Подготовлена структура для добавления новых AI-провайдеров

## 📊 API Endpoints

| Метод | Endpoint | Описание |
|-------|----------|----------|
| GET | `/api/ideas` | Получить все идеи |
| POST | `/api/ideas` | Создать новую идею |
| PUT | `/api/ideas/{id}/status` | Изменить статус идеи |
| DELETE | `/api/ideas/{id}` | Удалить идею |

## 🔧 Конфигурация

Основные параметры конфигурации:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/telegram_ai_bot
  jpa:
    hibernate:
      ddl-auto: update # В продакшене использовать validate

telegram:
  bot:
    username: ${TELEGRAM_BOT_USERNAME}
    token: ${TELEGRAM_BOT_TOKEN}

openrouter:
  api-key: ${OPENROUTER_API_KEY}
  model: ${OPENROUTER_MODEL:mistralai/mistral-7b-instruct}
```

## 🧪 Тестирование

```bash
# Запуск всех тестов
./mvnw test

# Запуск с покрытием
./mvnw test jacoco:report
```

## 📈 Планы развития

- [ ] Добавить интеграцию с Redis для хранения состояний
- [ ] Реализовать метрики и мониторинг (Micrometer + Prometheus)
- [ ] Добавить юнит и интеграционные тесты
- [ ] Внедрить Liquibase для версионирования БД
- [ ] Добавить CI/CD pipeline
- [ ] Реализовать Rate Limiting
- [ ] Добавить поддержку множественных AI-провайдеров

## 👤 Автор

**Ваше имя**
- GitHub: [@Ilya_Pletnyov](https://github.com/mrcreate163)

