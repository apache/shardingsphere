+++
title = "SQL 翻译"
weight = 7
+++

## 原生 SQL 翻译器

类型：NATIVE

可配置属性：

无

**默认使用的 SQL 翻译器，但目前暂未实现**

## 使用 JooQ 的 SQL 翻译器

类型：JOOQ

可配置属性：

无

**由于需要第三方的 JooQ 依赖，因此 ShardingSphere 默认并未包含相关模块，需要使用下面的 Maven 坐标引用该模块**

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sql-translator-jooq-provider</artifactId>
    <version>${project.version}</version>
</dependency>
```
