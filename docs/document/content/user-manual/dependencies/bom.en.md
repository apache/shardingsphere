+++
pre = "<b>4.5.1. </b>"
title = "Bill of Materials (BOM)"
weight = 1
+++

A Bill of Materials (BOM) is a standard Maven feature that provides centralized dependency version management. ShardingSphere BOM ensures that all modules use compatible versions, eliminating version conflicts and simplifying dependency management.

## What is ShardingSphere BOM?

The ShardingSphere BOM (`shardingsphere-bom`) is a POM file that contains version information for all ShardingSphere modules. By importing the BOM in your project, you no longer need to specify versions for individual ShardingSphere dependencies.

## Benefits of Using BOM

* **Version Consistency**: Ensures all ShardingSphere modules use compatible versions
* **Simplified Dependency Management**: No need to specify versions for individual modules
* **Easy Upgrades**: Upgrade all ShardingSphere dependencies by changing only the BOM version
* **Reduced POM Size**: Cleaner and more readable dependency declarations
* **Conflict Prevention**: Avoids version conflicts between transitive dependencies

## Maven Configuration

To use ShardingSphere BOM in your Maven project, add the following to your `pom.xml`:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-bom</artifactId>
            <version>${shardingsphere.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

After importing the BOM, you can declare ShardingSphere dependencies without versions:

```xml
<dependencies>
    <!-- ShardingSphere JDBC Driver -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-core</artifactId>
    </dependency>

    <!-- ShardingSphere Parser for MySQL -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-sql-parser-mysql</artifactId>
    </dependency>

    <!-- Data Source Pool Implementation -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
    </dependency>
</dependencies>
```

## Complete Example

Here's a complete example of a `pom.xml` using ShardingSphere BOM:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
         http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.example</groupId>
    <artifactId>shardingsphere-example</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>

    <properties>
        <maven.compiler.source>1.8</maven.compiler.source>
        <maven.compiler.target>1.8</maven.compiler.target>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <shardingsphere.version>5.5.2</shardingsphere.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.apache.shardingsphere</groupId>
                <artifactId>shardingsphere-bom</artifactId>
                <version>${shardingsphere.version}</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <!-- Core ShardingSphere JDBC -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc-core</artifactId>
        </dependency>

        <!-- Database Dialects -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-sql-parser-mysql</artifactId>
        </dependency>

        <!-- Connection Pool -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
        </dependency>

        <!-- Spring Boot Integration (if needed) -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-spring-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

## Available Modules

The ShardingSphere BOM includes version information for all major modules, including but not limited to:

* **Core Modules**: `shardingsphere-jdbc-core`, `shardingsphere-proxy-core`
* **SQL Parsers**: `shardingsphere-sql-parser-mysql`, `shardingsphere-sql-parser-postgresql`, etc.
* **Feature Modules**: `shardingsphere-sharding`, `shardingsphere-encryption`, etc.
* **Infrastructure**: `shardingsphere-infra-annotation`, `shardingsphere-infra-spi`, etc.
* **Data Source Pools**: `shardingsphere-infra-data-source-pool-hikari`, etc.
* **Spring Integration**: `shardingsphere-spring-boot-starter`, etc.

## Gradle Support

For Gradle users, you can use the BOM through the `dependencyManagement` plugin:

```gradle
plugins {
    id 'java'
    id 'io.spring.dependency-management' version '1.0.11.RELEASE'
}

dependencyManagement {
    imports {
        mavenBom "org.apache.shardingsphere:shardingsphere-bom:${shardingsphereVersion}"
    }
}

dependencies {
    implementation 'org.apache.shardingsphere:shardingsphere-jdbc-core'
    implementation 'org.apache.shardingsphere:shardingsphere-sql-parser-mysql'
}
```
