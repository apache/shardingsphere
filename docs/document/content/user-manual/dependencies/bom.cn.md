+++
pre = "<b>4.5.1. </b>"
title = "物料清单 (BOM)"
weight = 1
+++

物料清单 (Bill of Materials, BOM) 是 Maven 的标准功能，提供集中化的依赖版本管理。ShardingSphere BOM 确保所有模块使用兼容的版本，消除版本冲突并简化依赖管理。

## 什么是 ShardingSphere BOM？

ShardingSphere BOM (`shardingsphere-bom`) 是一个包含所有 ShardingSphere 模块版本信息的 POM 文件。通过在项目中导入 BOM，您不再需要为单个 ShardingSphere 依赖指定版本。

## 使用 BOM 的优势

* **版本一致性**：确保所有 ShardingSphere 模块使用兼容版本
* **简化依赖管理**：无需为单个模块指定版本
* **轻松升级**：只需更改 BOM 版本即可升级所有 ShardingSphere 依赖
* **减少 POM 体积**：更清晰、更易读的依赖声明
* **冲突预防**：避免传递依赖之间的版本冲突

## Maven 配置

要在 Maven 项目中使用 ShardingSphere BOM，请在您的 `pom.xml` 中添加以下配置：

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

导入 BOM 后，您可以声明 ShardingSphere 依赖而无需指定版本：

```xml
<dependencies>
    <!-- ShardingSphere JDBC 驱动 -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-jdbc-core</artifactId>
    </dependency>

    <!-- MySQL SQL 解析器 -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-sql-parser-mysql</artifactId>
    </dependency>

    <!-- 数据源池实现 -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
    </dependency>
</dependencies>
```

## 完整示例

下面是一个使用 ShardingSphere BOM 的完整 `pom.xml` 示例：

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
        <!-- 核心 ShardingSphere JDBC -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-jdbc-core</artifactId>
        </dependency>

        <!-- 数据库方言 -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-sql-parser-mysql</artifactId>
        </dependency>

        <!-- 连接池 -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
        </dependency>

        <!-- Spring Boot 集成（如果需要） -->
        <dependency>
            <groupId>org.apache.shardingsphere</groupId>
            <artifactId>shardingsphere-spring-boot-starter</artifactId>
        </dependency>
    </dependencies>
</project>
```

## 可用模块

ShardingSphere BOM 包含所有主要模块的版本信息，包括但不限于：

* **核心模块**：`shardingsphere-jdbc-core`、`shardingsphere-proxy-core`
* **SQL 解析器**：`shardingsphere-sql-parser-mysql`、`shardingsphere-sql-parser-postgresql` 等
* **功能模块**：`shardingsphere-sharding`、`shardingsphere-encryption` 等
* **基础设施**：`shardingsphere-infra-annotation`、`shardingsphere-infra-spi` 等
* **数据源池**：`shardingsphere-infra-data-source-pool-hikari` 等
* **Spring 集成**：`shardingsphere-spring-boot-starter` 等

## Gradle 支持

对于 Gradle 用户，您可以通过 `dependencyManagement` 插件使用 BOM：

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
