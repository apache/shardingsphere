+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## 1. Import maven dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Notice: Please change `${latest.release.version}` to the actual version.

## 2. Sharding Rule Configuration

ShardingSphere-JDBC can be configured by four methods, `Java`, `YAML`, `Spring namespace` and `Spring boot starter`. Developers can choose the suitable method according to different situations. Please refer to [Configuration Manual](/en/manual/shardingsphere-jdbc/configuration/) for more details.

## 3. Create DataSource

Use ShardingDataSourceFactory and rule configuration objects to create ShardingDataSource, which is realized from DataSource,  a standard JDBC interface. Then, users can use native JDBC or JPA, MyBatis and other ORM frameworks to develop.

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig, props);
```
