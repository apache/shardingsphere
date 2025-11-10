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
        <artifactId>shardingsphere-jdbc</artifactId>
    </dependency>

    <!-- ShardingSphere Parser for MySQL -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-parser-sql-engine-mysql</artifactId>
    </dependency>

    <!-- Data Source Pool Implementation -->
    <dependency>
        <groupId>org.apache.shardingsphere</groupId>
        <artifactId>shardingsphere-infra-data-source-pool-hikari</artifactId>
    </dependency>
</dependencies>
```

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
    implementation 'org.apache.shardingsphere:shardingsphere-jdbc'
    implementation 'org.apache.shardingsphere:shardingsphere-parser-sql-engine-mysql'
}
```
