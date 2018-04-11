+++
toc = true
title = "Sharding-JDBC"
weight = 1
+++

## Quick Start

### Sharding-JDBC

#### 1. Add maven dependency

```xml
<!-- import sharding-jdbc core -->
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

#### 2. Configure sharding rule

Sharding-JDBC support 4 types for sharding rule configuration, they are `Java`, `YAML`, `Spring namespace` and `Spring boot starter`. Developers can choose any one for best suitable situation.

#### 3. Create DataSource

Use ShardingDataSourceFactory to create ShardingDataSource, which is a standard JDBC DataSource. Then developers can use it for raw JDBC, JPA, MyBatis or Other JDBC based ORM frameworks.

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);
```