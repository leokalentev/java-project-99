### Hexlet tests and linter status:

[![Actions Status](https://github.com/leokalentev/java-project-99/actions/workflows/hexlet-check.yml/badge.svg)](https://github.com/leokalentev/java-project-99/actions)

### Build

[![build project](https://github.com/leokalentev/java-project-99/actions/workflows/main.yml/badge.svg)](https://github.com/leokalentev/java-project-99/actions/workflows/main.yml)

### Test Coverage

[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=leokalentev_java-project-99&metric=coverage)](https://sonarcloud.io/summary/new_code?id=leokalentev_java-project-99)

### Сайт

[![Открыть сайт](https://img.shields.io/badge/перейти-на_сайт-blue?style=for-the-badge)](https://java-project-99-uybr.onrender.com)

---

# Task Manager

**Task Manager** — это система управления задачами, вдохновлённая [Redmine](http://www.redmine.org/). Она позволяет создавать задачи, назначать ответственных, добавлять метки и управлять статусами задач. Для использования системы необходимо пройти регистрацию и аутентификацию.

---

## 🚀 Возможности

- Регистрация и аутентификация пользователей
- CRUD-интерфейсы для задач, меток и статусов
- Механизм авторизации (разграничение прав)
- Сложные связи между сущностями (One-to-Many, Many-to-Many)
- Фильтрация задач по параметрам
- Покрытие проекта автотестами
- Сборка через Gradle, деплой на Render
- Отслеживание ошибок через Sentry

---

## 🛠️ Технологии

- **Java 17**
- **Spring Boot**
- **Hibernate / JPA**
- **PostgreSQL**
- **Gradle**
- **JWT**
- **Sentry**
- **OpenAPI / Swagger**
- **Render** (хостинг)
- **JUnit + MockMvc + JSONAssert**

---

## 🚴‍♂️ Как запустить

1. **Клонируйте репозиторий:**
   ```bash
   git clone https://github.com/leokalentev/java-project-99.git
   cd java-project-99

2. **Создайте БД и укажите параметры подключения:**
   ```bash
   spring:
     datasource:
      url: jdbc:postgresql://localhost:5432/task_manager
      username: postgres
      password: your_password

3. **Запустите миграции:**
   ```bash
   ./gradlew flywayMigrate

4. **Запуск приложения:**
   ```bash
   ./gradlew bootRun

## 🧪 Тесты

1. **Запуск тестов:**
   ```bash
   ./gradlew test

