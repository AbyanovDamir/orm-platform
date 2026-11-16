# Учебная платформа «Практикум ORM»

Платформа хранит курсы, уроки, расписание и задания. Студент записывается на курс, сдаёт работу и проходит тест. Преподаватель выставляет балл и оставляет комментарий. Проект демонстрирует связи JPA, каскадные операции и работу с ленивой загрузкой.

**Стек** — Java 17+, Spring Boot 3.5.15, Spring Data JPA, Hibernate, PostgreSQL 16, Maven. Веб-интерфейс работает на HTML, CSS и JavaScript.

## Запуск и проверка на Linux

Все команды выполняйте в Bash **внутри Linux-ВМ**. Понадобятся Git, JDK 17+, Maven 3.8+, Docker Engine с Compose v2, `curl` и `jq`. Для Ubuntu и Kubuntu установите инструменты через APT:

```bash
sudo apt update
sudo apt install -y git openjdk-17-jdk maven curl jq
```

Установите [Docker Engine и Compose v2 для Ubuntu](https://docs.docker.com/engine/install/ubuntu/) и запустите службу `sudo systemctl enable --now docker`. Если вашей учётной записи нужен `sudo` для доступа к Docker, используйте `sudo docker compose` в приведённых командах.

### 1. Клонирование

```bash
git clone https://github.com/AbyanovDamir/orm-platform.git
cd orm-platform
```

При работе с ZIP распакуйте его и перейдите в каталог `orm-platform`, где лежит `pom.xml`. Остальные команды выполняются из этого каталога.

### 2. Сборка

```bash
java -version
mvn -version
docker compose version
mvn clean package -DskipTests
```

Ожидаемый результат — `BUILD SUCCESS` и файл `target/orm-platform-1.0.0.jar`. Docker самостоятельно собирает JAR для своего образа.

### 3. Запуск приложения

```bash
test -f .env || cp .env.example .env
docker compose up --build -d
docker compose ps
docker compose logs -f app
```

Пароль `DB_PASSWORD` можно изменить в `.env` до первого запуска пустой базы. Дождитесь строки `Started PlatformApplication` и закройте просмотр логов через **Ctrl+C**. Контейнеры продолжат работать. Порты `8080` и `5432` должны быть свободны внутри ВМ.

Откройте [http://localhost:8080](http://localhost:8080) в браузере **внутри ВМ**. На странице должен появиться курс «Основы Hibernate». Если ВМ доступна по SSH, выполните на своём компьютере `ssh -L 8080:127.0.0.1:8080 user@vm` с адресом ВМ и именем пользователя. Оставьте SSH-сессию открытой и откройте тот же URL на своём компьютере.

### 4. Автоматические тесты

Прогон на H2:

```bash
mvn verify
```

Для прогона на PostgreSQL оставьте Compose запущенным и создайте отдельную базу `platform_test`. Если она уже существует, пропустите `createdb`.

```bash
docker compose exec -T postgres createdb -U platform platform_test
(
  read -rsp 'Введите DB_PASSWORD из файла .env: ' TEST_DB_PASSWORD
  printf '\n'
  export TEST_DB_PASSWORD
  export TEST_DB_URL='jdbc:postgresql://127.0.0.1:5432/platform_test'
  export TEST_DB_USER='platform'
  mvn verify
)
```

Профиль `test` пересоздаёт схему только в указанной тестовой базе. Приложение продолжает использовать базу `platform`. Ожидаются `Tests run: 19, Failures: 0, Errors: 0, Skipped: 0`, `0 Checkstyle violations` и `BUILD SUCCESS`. Отчёты лежат в `target/surefire-reports/`.

### 5. Ручное тестирование

Выполняйте браузерный сценарий на свежих демоданных. Если Анна уже сдавала работу, для точного повтора используйте очистку с удалением данных на шаге 6, затем повторите запуск.

#### Проверка в браузере

| № | Действие | Ожидаемый результат |
| --- | --- | --- |
| 1 | На главной странице выберите «Основы Hibernate» и Алексея Орлова. | Показаны два модуля, занятие, оценка `9 / 10` и предыдущий результат теста `2 / 2`. |
| 2 | Переключитесь на Анну Белову и нажмите «Записаться на курс». | Появляется статус «Вы записаны», поле для решения и кнопка «Открыть тест». |
| 3 | Введите текст решения в задании «Связь курса и модуля» и нажмите «Отправить решение». | Появляется статус «Решение отправлено» и текст работы. После обновления страницы работа видна. |
| 4 | Откройте «Демо · выставить оценку». Попробуйте ввести `11`, затем введите `8` и комментарий «Проверено». | Интерфейс отклоняет `11`. После сохранения допустимого значения отображаются `8 / 10` и комментарий. |
| 5 | Во втором модуле откройте тест. Выберите `@Id`, а во втором вопросе — «Запрос с EntityGraph» и «JPQL с JOIN FETCH». Отправьте ответы. | Отображается результат `2 / 2`. После обновления страницы последняя попытка видна в модуле. |
| 6 | Нажмите «Отписаться». | Появляется кнопка записи. Решение, оценка и последняя попытка остаются на странице. |

Выбор студента и демонстрационная форма оценки имитируют роли. Пользовательские сессии и вход по паролю в учебной версии отсутствуют.

#### Проверка REST API из терминала

Выполняйте три блока в одном терминале. Они находят курс по названию и создают временного студента с уникальным адресом.

**5.1. Чтение курса и создание студента**

```bash
api='http://127.0.0.1:8080/api'
course_id=$(curl -fsS "$api/courses" |
  jq -er 'first(.[] | select(.title == "Основы Hibernate") | .id)')
curl -fsS "$api/courses/$course_id" | jq -r '.modules[].module.title'

email="rest-$(cat /proc/sys/kernel/random/uuid)@example.org"
user_json() {
  jq -nc --arg name "$1" --arg email "$email" '{name:$name,email:$email,role:"STUDENT"}'
}
created=$(curl -fsS -w '\n%{http_code}' -X POST -H 'Content-Type: application/json' \
  --data "$(user_json 'REST Student')" "$api/users")
printf '%s\n' "$created" | tail -n 1
student_id=$(printf '%s\n' "$created" | head -n 1 | jq -er '.id')
```

Ожидаются названия «Модель данных», «Ленивая загрузка» и код `201`.

**5.2. Запись на курс и ошибки API**

```bash
status() { curl -sS -o /dev/null -w '%{http_code}\n' "$@"; }
enrollment="$api/courses/$course_id/enrollments/$student_id"
status -X POST "$enrollment"
status -X POST "$enrollment"
curl -fsS "$api/users/$student_id/enrollments" | jq '.[] | {courseTitle, status}'
status "$api/courses/9223372036854775807"
status -X POST -H 'Content-Type: application/json' --data '{}' "$api/users"
```

Ожидаются коды `201`, `409`, одна запись со статусом `ACTIVE`, затем `404` и `400`.

**5.3. Обновление и удаление данных ручной проверки**

```bash
curl -fsS -X PUT -H 'Content-Type: application/json' \
  --data "$(user_json 'REST Student Updated')" "$api/users/$student_id" | jq -r '.name'
status -X DELETE "$enrollment"
status -X DELETE "$api/users/$student_id"
status "$api/users/$student_id"
```

Ожидаются новое имя, два ответа `204` и `404` после удаления пользователя.

### 6. Остановка и очистка

Остановить и удалить контейнеры проекта, сохранив данные PostgreSQL:

```bash
docker compose down
mvn clean
```

Начать с чистых демоданных можно, удалив том PostgreSQL и локальный образ приложения. Эта команда удаляет **все** данные PostgreSQL данного Compose-проекта, включая ручные работы и тестовую базу:

```bash
docker compose down --volumes --rmi local
mvn clean
```

Для нового запуска повторите шаг 3. После удаления тома приложение создаст схему и демоданные при старте.

## Запуск с установленной PostgreSQL

В интерактивном `psql` от администратора базы создайте роль и базу.

```sql
CREATE ROLE platform LOGIN;
\password platform
CREATE DATABASE platform OWNER platform;
```

Из корня проекта после сборки выполните:

```bash
(
  read -rsp 'Пароль роли platform: ' DB_PASSWORD
  printf '\n'
  export DB_PASSWORD
  export DEMO_DATA=true
  java -jar target/orm-platform-1.0.0.jar --server.address=127.0.0.1
)
```

По умолчанию используется `jdbc:postgresql://localhost:5432/platform` и роль `platform`. Для другого подключения задайте `DB_URL` и `DB_USER`. Hibernate создаёт схему при запуске. Остановить приложение можно сочетанием **Ctrl+C**.

## Модель и правила

В модели 16 JPA-сущностей и 17 таблиц. Имена таблиц приведены ниже. Поля времени занятий и попыток хранятся как `Instant`. Дедлайн действует включительно по дате UTC. `timeLimit` задаёт рекомендуемую длительность теста в минутах. Для каждого вопроса нужно выбрать ответ. Балл начисляется за точное совпадение набора выбранных вариантов с ключом. Повторные попытки сохраняются отдельно.

```mermaid
erDiagram
    USERS ||--o| PROFILES : "имеет"
    USERS ||--o{ COURSES : "ведёт"
    CATEGORIES ||--o{ COURSES : "классифицирует"
    COURSES ||--o{ COURSE_TAG : "содержит"
    TAGS ||--o{ COURSE_TAG : "присвоен"
    USERS ||--o{ ENROLLMENTS : "записывается"
    COURSES ||--o{ ENROLLMENTS : "принимает"
    USERS ||--o{ COURSE_REVIEWS : "оставляет"
    COURSES ||--o{ COURSE_REVIEWS : "получает"
    COURSES ||--o{ MODULES : "содержит"
    MODULES ||--o{ LESSONS : "содержит"
    LESSONS ||--o{ LESSON_SESSIONS : "расписание"
    LESSONS ||--o{ ASSIGNMENTS : "выдаёт"
    ASSIGNMENTS ||--o{ SUBMISSIONS : "получает"
    USERS ||--o{ SUBMISSIONS : "отправляет"
    MODULES ||--o| QUIZZES : "проверяет"
    QUIZZES ||--o{ QUESTIONS : "содержит"
    QUESTIONS ||--o{ ANSWER_OPTIONS : "предлагает"
    QUIZZES ||--o{ QUIZ_SUBMISSIONS : "результаты"
    USERS ||--o{ QUIZ_SUBMISSIONS : "проходит"
```

- `Profile → User` и `Quiz → CourseModule` защищены уникальными внешними ключами. `Course ↔ Tag` использует `@ManyToMany` через `course_tag`.
- `Enrollment` хранит статус и дату записи. Пары «курс + студент», «задание + студент» и «курс + автор отзыва» уникальны в базе.
- Удаление курса каскадно удаляет его модули, уроки, занятия, задания, решения, тесты, попытки, записи и отзывы. Категория, преподаватель и теги остаются. При попытке удалить используемый справочник API возвращает конфликт.
- Набор вопросов создаётся вместе с тестом. После создания можно редактировать название и рекомендуемую длительность. Студенческий ответ API содержит формулировки и варианты без ключа.

## REST API

Базовый префикс — `/api`. Тело передаётся как JSON. Создание возвращает `201`, удаление — `204`. Ошибки возвращаются как `application/problem+json` с текстом в `detail` и, при проверке полей, списком `errors`.

| Ресурс | Операции |
| --- | --- |
| `/users`, `/categories`, `/tags`, `/courses` | `GET` списка, `POST` создания, `GET/PUT/DELETE /{id}` |
| `/users/{id}/profile` | `GET`, `PUT` для создания и обновления, `DELETE` |
| `/courses/{id}/modules` | `POST` для добавления модуля |
| `/modules/{id}/lessons` | `POST` для добавления урока |
| `/lessons/{id}/sessions`, `/lessons/{id}/assignments` | `POST` для создания занятия и задания |
| `/modules/{id}`, `/lessons/{id}`, `/sessions/{id}`, `/assignments/{id}` | `GET/PUT/DELETE` |
| `/courses/{id}/enrollments`, `/users/{id}/enrollments` | `GET` записей |
| `/courses/{id}/enrollments/{studentId}` | `POST` записи, `DELETE` отписки |
| `/courses/{id}/enrollments/{studentId}/completion?teacherId=…` | `PUT` для завершения курса |
| `/assignments/{id}/submissions` | `GET` решений, `POST` для сдачи работы |
| `/users/{id}/submissions` | `GET` работ студента |
| `/submissions/{id}/grade` | `PUT` оценки |
| `/modules/{id}/quiz` | `POST` теста с вопросами и вариантами |
| `/quizzes/{id}` | `GET/PUT/DELETE` |
| `/quizzes/{id}/attempts` | `POST` ответов, `GET` истории попыток |
| `/users/{id}/quiz-attempts` | `GET` результатов студента |
| `/courses/{id}/reviews` | `GET` отзывов, `POST` для студента, завершившего курс |

Примеры тел запросов. Используйте идентификаторы из ответов вашей базы.

```text
POST /api/users
{"name":"Иван","email":"ivan@example.org","role":"STUDENT"}

POST /api/courses
{"title":"JPA","description":"Связи ORM","categoryId":1,"teacherId":1,"duration":"2 недели","startDate":"2030-09-01","tagIds":[1]}

POST /api/assignments/1/submissions
{"studentId":2,"content":"Внешний ключ хранится в таблице модулей."}

PUT /api/submissions/1/grade
{"teacherId":1,"score":9,"feedback":"Добавьте пример mappedBy."}

POST /api/modules/1/quiz
{"title":"Первичный ключ","timeLimit":5,"questions":[{"text":"Аннотация ключа?","type":"SINGLE_CHOICE","options":[{"text":"@Id","correct":true},{"text":"@Table","correct":false}]}]}

POST /api/quizzes/1/attempts
{"studentId":2,"answers":{"1":[1]}}
```

## Архитектура и ленивая загрузка

- `entity` содержит модель и ограничения БД. `repository` предоставляет 16 Spring Data JPA-репозиториев и запросы для выборочной загрузки.
- `service` отвечает за бизнес-правила, границы транзакций и сборку DTO. `web` валидирует входные данные и преобразует исключения в HTTP-ответы.
- `spring.jpa.open-in-view=false`. В интеграционном тесте обращение к `modules` после обычного `findById` и закрытия сессии вызывает `LazyInitializationException`. Другой тест загружает модули через `@EntityGraph`, JPQL `JOIN FETCH` и доступ внутри транзакции.
- При чтении курса `@EntityGraph` заранее загружает модули, категорию и преподавателя. Сервис читает вложенные коллекции внутри транзакции и строит DTO. Hibernate пакетно загружает коллекции, нужные этому сценарию.

## Что покрывают тесты

15 интеграционных тестов проверяют схему, CRUD, каскадное удаление, уникальность записи, дедлайн, сдачу и оценивание работ, прохождение теста, отзывы, HTTP-валидацию и поведение LAZY. Четыре модульных теста проверяют правила начисления баллов. При локальной проверке все 19 тестов прошли на H2 2.3.232 и PostgreSQL 16.9.

## Публикация для сдачи

### Проверки и доставка сборки

Workflow [Verify and deliver](https://github.com/AbyanovDamir/orm-platform/actions/workflows/ci.yml) запускает тесты на PostgreSQL 16, проверяет исходный и тестовый код через Checkstyle и проверяет запуск демопроекта через Docker Compose. После успешной сборки JAR доступен в разделе Artifacts у запуска workflow.

При отправке тега с префиксом `v`, например `v1.0.0`, workflow публикует образ в GitHub Container Registry после прохождения обеих проверок. У образа будет тот же тег, например `ghcr.io/abyanovdamir/orm-platform:v1.0.0`. Доступ к скачиванию регулируется видимостью пакета на GitHub.

Проверьте, что workflow выполнился на ветке, которую сдаёте. Затем передайте ссылку на этот GitHub-репозиторий через форму сдачи.
