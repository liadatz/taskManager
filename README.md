# taskManager ![version-image](https://img.shields.io/badge/version-1.0.0-green)
#### RESTful Server project created for Computer Science department in Ben Gurion University. 

## How it works
The application was built with:
- [Kotlin](https://github.com/JetBrains/kotlin) as programming language
- [Ktor](https://github.com/ktorio/ktor) as web framework
- [Exposed](https://github.com/JetBrains/Exposed) as Sql framework to persistence layer
- [SQLite](https://github.com/sqlite/sqlite) as database


## API
[SwaggerHub API](https://app.swaggerhub.com/apis-docs/liadatz/TaskManager/1.0.0)


## Getting started

You need just JVM installed.

The server is configured to start on [8080](http://localhost:7000).

Use the build tool [Gradle](https://gradle.org/) to build project.

Build:

```bash
./gradlew clean build
```
Start the server:
```bash
./gradlew run
```

## Modifications
Currently, previous database isn't dropped if exists when initiating the server. If you wish to change this option switch the next line in Application.kt:
```kotlin
MyDatabase.createTables(false)
```
To:
```kotlin
MyDatabase.createTables(true)
```
