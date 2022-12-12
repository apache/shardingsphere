+++
title = "SQL Translator"
weight = 7
+++

## Native SQL translator

Type: NATIVE

Attributes:

None

**Default SQL translator, does not implement yet.**

## JooQ SQL translator

Type: JOOQ

Attributes:

None

**Because of it need JooQ dependency, ShardingSphere does not include the module, please use below XML to import it by Maven.**

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-sql-translator-jooq-provider</artifactId>
    <version>${project.version}</version>
</dependency>
```
