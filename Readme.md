[![Build Status](https://api.honeybeedev.com/latestVersion/com.oop.datamodule/universal/icon?width=400px)](https://api.honeybeedev.com/latestVersion/com.oop/data/downloadUrl)

## Data Module By OOP-778

Simple, Effective to use data module that supports

- MySQL
- SQLite
- Json Flat Files
- MongoDB
- PostgreSQL

## Artifacts

- universal
    - comes with all modules and has UniversalBody that works on all listed artifacts
- mysql
    - comes only with mysql module
- sqlite
    - comes with only sqlite module
- json
    - comes only with flat file module
- mongodb
    - comes only with mongodb module
- postgresql
    - comes only with postgresql

## Maven

```xml
<repository>
    <id>code-mc</id>
    <url>https://repo.codemc.org/repository/maven-releases/</url>
</repository>

<dependency>
    <groupId>com.oop.datamodule</groupId>
    <artifactId>artifact-name</artifactId>
    <version>latest build version</version>
</dependency>
```

## Gradle

```groovy
repositories {
    maven { url 'https://repo.codemc.org/repository/maven-releases/' }
}

dependencies {
    compile "com.oop.datamodule:artifact:latest build version"
}
```
