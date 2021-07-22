[![Build Status](https://api.honeybeedev.com/latestVersion/com.oop.datamodule/universal/icon?width=400px)](https://api.honeybeedev.com/latestVersion/com.oop/data/downloadUrl)

## V3
V3 is the biggest change of data-module I've decided to completely rewrite it from the roots.  
The biggest difference from V2 is that instead of loading every single object from the database into  
Memory we load just the primary keys and then for each property you can define timeout value of inactivity, or with a predicate.

Also I changed how The base structure works, there's now two types of models:
1) Generated
2) Object based

So object based is pretty much using reflection to know about your properties and generated, you don't actually create an object for it, I create an internal object that you can fetch data from.
It supports concurrency & can be used as blocking.

## What's left for it to be fully functional?
 - Add SQL databases support thru HikariCP
 - Finish implementation of Property
 - Finish and actually implement Property Builders.  
 - Add back Migrator (Imported/Exporter)

## Data Module By OOP-778

Simple, Effective and works with most if not all databases!

- MySQL
- SQLite
- MongoDB
- PostgreSQL

## Artifacts

Soon.

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
