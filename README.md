# Упаковка посылок в кузовы грузовиков

CLI-проект на Java для двух операций:

- упаковка посылок в кузовы (`pack`);
- обратное преобразование результата погрузки из JSON в текстовый формат посылок (`split`).

## Возможности

- 4 алгоритма погрузки: `simple`, `optimized`, `even`, `dense`;
- валидация входных посылок перед погрузкой;
- сохранение результата погрузки в JSON;
- восстановление текстового файла посылок из JSON;
- запуск через Gradle и через исполняемый fat jar.

## Структура (основное)

```text
src/main/java/ru/hofftech/omni/shipping
├── ShippingApp.java                 # класс погрузки (run)
├── JsonToPackagesApp.java           # единый CLI с командами pack/split
├── entities
│   ├── Package.java
│   └── Truck.java
├── interfaces
│   └── PackingAlgorithm.java
└── services
    ├── PackageLoader.java
    ├── PackageValidator.java
    ├── PackageTextFormatService.java   # текстовый формат посылок
    ├── PackingResultJsonService.java   # только JSON
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

### 1) Погрузка (`pack`) через `ShippingApp`

```bash
./gradlew run --args="<путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]"
```

Пример:

```bash
./gradlew run --args="test-input.txt 6 6 dense 10 result.json"
```

### 2) Единый CLI (`pack`/`split`) через `JsonToPackagesApp`

```bash
./gradlew runJsonToPackages --args="pack <путь_к_файлу> <ширина_кузова> <высота_кузова> <алгоритм> <количество_машин> [json_файл_результата]"
./gradlew runJsonToPackages --args="split <json_вход> <файл_посылок_выход>"
```

Примеры:

```bash
./gradlew runJsonToPackages --args="pack test-input.txt 6 6 optimized 10 result.json"
./gradlew runJsonToPackages --args="split result.json packages.txt"
```

Windows:

```powershell
.\gradlew.bat runJsonToPackages --args="pack test-input.txt 6 6 optimized 10 result.json"
.\gradlew.bat runJsonToPackages --args="split result.json packages.txt"
```

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

## Формат входного файла посылок

Каждая посылка — блок строк, блоки разделены пустой строкой.

Пример:

```text
999
999
999

666
666

55555

1

1

333
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
