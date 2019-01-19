+++
pre = "<b>2.1. </b>"
toc = true
title = "Sharding-JDBC"
weight = 1
+++

## 1. Import maven dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Note: Please change the `${latest.release.version}` to the actual version.

## 2. Configure sharding rule configuration

Sharding-JDBC support 4 types for sharding rule configuration, they are `Java`, `YAML`, `Spring namespace` and `Spring boot starter`. Developers can choose any one for best suitable situation. More details please reference [Configuration Manual](/en/manual/sharding-jdbc/configuration/).

## 3. Create DataSource

Use ShardingDataSourceFactory to create ShardingDataSource, which is a standard JDBC DataSource. Then developers can use it for raw JDBC, JPA, MyBatis or Other JDBC based ORM frameworks.

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```
