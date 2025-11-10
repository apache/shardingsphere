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
        <artifactId>shardingsphere-jdbc</artifactId>
    </dependency>

    <!-- MySQL SQL 解析器 -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-parser-sql-engine-mysql</artifactId>
    </dependency>

    <!-- 数据源池实现 -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
    </dependency>
</dependencies>
```

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
    implementation 'org.apache.shardingsphere:shardingsphere-jdbc'
    implementation 'org.apache.shardingsphere:shardingsphere-parser-sql-engine-mysql'
}
```
