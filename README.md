# Упаковка посылок в кузовы грузовиков

CLI-проект на Java для операций:

- управление базой посылок (создать/найти/удалить);
- погрузка посылок в кузовы (`pack`, `load`);
- выгрузка списка посылок из результата погрузки (`split`, `unload`);
- управление через Telegram-бота (команды из чата).

## Возможности

- 4 алгоритма погрузки: `simple`, `optimized`, `even`, `dense`;
- валидация входных посылок перед погрузкой;
- сохранение результата погрузки в JSON (2 формата: старый `pack` и новый для `load/unload`);
- восстановление посылок из JSON (`split`) и получение списка посылок из JSON (`unload`);
- база посылок в `test-input.txt` с уникальными названиями;
- добавление/просмотр/удаление посылок в базе (`createpackage`, `findpackage`, `deletepackage`);
- погрузка по названиям посылок (загрузить то, чего нет в базе — нельзя);
- Telegram-бот для команд `createpackage`, `findpackage`, `deletepackage`, `load`, `unload`;
- запуск через Gradle и через исполняемый fat jar.

## Структура (основное)

```text
src/main/java/ru/hofftech/omni/shipping
├── ShippingApp.java                 # единый CLI (pack/split/create/find/delete/load/unload)
├── TelegramShippingBotApp.java      # Telegram long-polling бот
├── entities
│   ├── Package.java
│   └── Truck.java
├── interfaces
│   └── PackingAlgorithm.java
└── services
    ├── PackageLoader.java
    ├── PackageRepository.java
    ├── PackageValidator.java
    ├── PackageTextFormatService.java   # текстовый формат посылок
    ├── NamedPackageTextFormatService.java
    ├── PackingResultJsonService.java   # только JSON
    ├── TrucksJsonFileService.java      # JSON для load/unload
    └── packing
        ├── SimplePackingAlgorithm.java
        ├── OptimizedPackingAlgorithm.java
        ├── EvenDistributionPackingAlgorithm.java
        └── DensePackingAlgorithm.java
```

## Требования

- JDK 18+
- рекомендуется использовать Gradle Wrapper (`gradlew`, `gradlew.bat`)
- консоль с UTF-8
- для Telegram-бота нужен токен Telegram Bot API

## Быстрый старт

### Сборка

```bash
./gradlew build
```

Windows:

```powershell
.\gradlew.bat build
```

### Запуск тестов

```bash
./gradlew test
```

Windows:

```powershell
.\gradlew.bat test
```

## Запуск через Gradle

### 1) Запуск CLI через `ShippingApp` (Gradle `run`)

```bash
./gradlew run --args="pack <путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]"
```

Пример:

```bash
./gradlew run --args="pack test-input.txt 6 6 dense 10 result.json"
```

### 2) CLI через задачу `Shipping`

```bash
./gradlew Shipping --args="pack <путь_к_файлу> <ширина_кузова> <высота_куzова> <алгоритм> <количество_машин> [json_файл_результата]"
./gradlew Shipping --args="split <json_вход> <файл_посылок_выход>"
./gradlew Shipping --args="createpackage -name <name> -form <form>"
./gradlew Shipping --args="findpackage <name>"
./gradlew Shipping --args="deletepackage <name>"
./gradlew Shipping --args="load -parcels-text <names> -trucks <sizes> -type <algo> -out <text|json-file> [-out-filename <file>]"
./gradlew Shipping --args="load -parcels-file <parcels.csv> -trucks <sizes> -type <algo> -out <text|json-file> [-out-filename <file>]"
./gradlew Shipping --args="unload -infile <trucks.json> -outfile <parcels.csv> [--withcount]"
```

Примеры:

```bash
./gradlew Shipping --args="pack test-input.txt 6 6 optimized 10 result.json"
./gradlew Shipping --args="split result.json packages.txt"
```

Windows:

```powershell
.\gradlew.bat Shipping --args="pack test-input.txt 6 6 optimized 10 result.json"
.\gradlew.bat Shipping --args="split result.json packages.txt"

# база посылок
.\gradlew.bat Shipping --args="createpackage -name test4x4 -form oooo\no  o\no  o\noooo\n"
.\gradlew.bat Shipping --args="findpackage test4x4"
.\gradlew.bat Shipping --args="deletepackage test4x4"

# load/unload
.\gradlew.bat Shipping --args="load -parcels-text test3x3,test3x2 -trucks 3x3 4x4 -type simple -out text"
.\gradlew.bat Shipping --args="load -parcels-file parcels.csv -trucks 3x3 4x4 -type simple -out json-file -out-filename trucks.json"
.\gradlew.bat Shipping --args="unload -infile trucks.json -outfile parcels.csv"
.\gradlew.bat Shipping --args="unload -infile trucks.json -outfile parcels-with-count.csv --withcount"
```

### 3) Telegram-бот (`TelegramBot`)

Бот поддерживает команды из чата:

- `createpackage`
- `findpackage`
- `deletepackage`
- `load`
- `unload`

Бот читает настройки из файла `.env` в корне проекта:

```env
TELEGRAM_BOT_TOKEN=
TELEGRAM_ALLOWED_CHAT_ID=
```

Также можно задать переменные окружения — они имеют приоритет над `.env`:

- `TELEGRAM_BOT_TOKEN` — токен бота (обязательно);
- `TELEGRAM_ALLOWED_CHAT_ID` — id чата, которому разрешён доступ (опционально, но рекомендуется).

Запуск:

```powershell
$env:TELEGRAM_BOT_TOKEN="123456:abc..."
$env:TELEGRAM_ALLOWED_CHAT_ID="123456789"
.\gradlew.bat TelegramBot
```

Примеры сообщений боту:

```text
/findpackage "test3x3"
/createpackage -name "test4x4" -form "oooo\no  o\no  o\noooo\n"
/load -parcels-text "test3x3,test3x2" -trucks "3x3 4x4" -type "simple" -out text
/unload -infile "trucks.json" -outfile "parcels.csv"
```

Дополнительно:

- можно писать как с `/`, так и без него (`/findpackage ...` и `findpackage ...`);
- `/start` и `/help` показывают список поддерживаемых команд;
- если задан `TELEGRAM_ALLOWED_CHAT_ID`, бот отвечает только этому чату.

## Запуск без Gradle (через jar)

В проекте есть задача `fatJar`, которая собирает исполняемый jar со всеми зависимостями.

### Сборка jar

```bash
./gradlew fatJar
```

Windows:

```powershell
.\gradlew.bat fatJar
```

Артефакт:

```text
build/libs/java-course-1.0-SNAPSHOT-all.jar
```

### Запуск jar

```bash
java -jar build/libs/java-course-1.0-SNAPSHOT-all.jar pack test-input.txt 6 6 optimized 10 result.json
java -jar build/libs/java-course-1.0-SNAPSHOT-all.jar split result.json packages.txt
java -jar build/libs/java-course-1.0-SNAPSHOT-all.jar load -parcels-text "test3x3,test3x2" -trucks "3x3 4x4" -type simple -out json-file -out-filename trucks.json
java -jar build/libs/java-course-1.0-SNAPSHOT-all.jar unload -infile trucks.json -outfile parcels.csv --withcount
```

Запуск Telegram-бота из fat jar:

```bash
TELEGRAM_BOT_TOKEN=123456:abc... TELEGRAM_ALLOWED_CHAT_ID=123456789 java -cp build/libs/java-course-1.0-SNAPSHOT-all.jar ru.hofftech.omni.shipping.TelegramShippingBotApp
```

## Аргументы команды `pack`

- `<путь_к_файлу>` — файл с посылками;
- `<ширина_кузова>` — ширина кузова;
- `<высота_кузова>` — высота кузова;
- `<алгоритм>` — `simple` | `optimized` | `even` | `dense`;
- `<количество_машин>` — максимальное число доступных кузовов;
- `[json_файл_результата]` — опциональный путь для сохранения JSON.

## Аргументы команды `split`

- `<json_вход>` — JSON-файл результата погрузки;
- `<файл_посылок_выход>` — выходной текстовый файл посылок.

## База посылок (`test-input.txt`)

Файл `test-input.txt` — это база посылок. У каждой посылки есть **уникальное имя**.

Формат:

- первая строка блока: `<name>:`
- далее несколько строк формы (символы, пробелы допускаются)
- блоки разделены пустой строкой

Пример:

```text
test3x3:
ooo
ooo
ooo

test3x2:
ooo
ooo

test5x1:
ooooo

test1x1:
o
```

## Команды базы посылок

### `createpackage`

Вход:

```text
createpackage -name "test4x4" -form "oooo\no  o\no  o\noooo\n"
```

Выход:

```text
id(name): "test4x4"
form:
oooo
o  o
o  o
oooo
```

### `findpackage`

Вход:

```text
findpackage "test4x4"
```

Выход аналогичен `createpackage`.

### `deletepackage`

Вход:

```text
deletepackage "test4x4"
```

Выход:

```text
Посылка "test4x4" удалена.
```

## Команда `load`

Погрузка по **названиям** посылок из базы. Если посылки нет в базе — погрузка невозможна.

### Вариант 1: погрузка через текст

```text
load -parcels-text "test3x3,test3x2" -trucks "3x3 4x4" -type "simple" -out text
```

### Вариант 2: погрузка через файл

Файл `parcels.csv`:

```text
"test3x3"
"test3x2"
```

Команда:

```text
load -parcels-file "parcels.csv" -trucks "3x3 4x4" -type "simple" -out json-file -out-filename "trucks.json"
```

JSON (`trucks.json`) создаётся в формате:

```json
[
  {
    "truck_type": "3x3",
    "parcels": [
      {
        "name": "test3x3",
        "coordinates": [[0, 0]]
      }
    ]
  }
]
```

## Команда `unload`

Выгружает список посылок из `trucks.json`:

- **без подсчёта**:

```text
unload -infile "trucks.json" -outfile "parcels.csv"
```

Результат `parcels.csv`:

```text
"test3x3"
"test3x2"
```

- **с подсчётом**:

```text
unload -infile "trucks.json" -outfile "parcels-with-count.csv" --withcount
```

Результат `parcels-with-count.csv`:

```text
"test3x3";1
"test3x2";1
```

## Правила размещения

- посылки не вращаются;
- посылки не должны "висеть в воздухе";
- опора под основанием должна быть больше половины;
- размеры посылки не должны превышать размеры кузова.

## Алгоритмы

- `simple` — по сути одна посылка на кузов;
- `optimized` — пытается разместить больше посылок в имеющихся кузовах;
- `even` — распределяет посылки равномерно по кузовам;
- `dense` — стремится к максимально плотной упаковке и минимальному числу кузовов.

## Пример результата

```text
Результат упаковки (Оптимизированный алгоритм):
Использовано кузовов: 1

Кузов #1:
++++++++
+333   +
+55555 +
+99911 +
+999666+
+999666+
++++++++
```
