+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## 1. Import Maven Dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

Notice: Please change `${latest.release.version}` to the actual version.

## 2. Rules Configuration

ShardingSphere-JDBC can be configured by four methods, `Java`, `YAML`, `Spring namespace` and `Spring boot starter`. 
Developers can choose the suitable method according to different situations. 
Please refer to [Configuration Manual](/en/user-manual/shardingsphere-jdbc/configuration/) for more details.

## 3. Create Data Source

Use `ShardingSphereDataSourceFactory` and rule configurations to create `ShardingSphereDataSource`, which implements DataSource interface of JDBC. 
It can be used for native JDBC or JPA, MyBatis and other ORM frameworks.

```java
DataSource dataSource = ShardingSphereDataSourceFactory.createDataSource(dataSourceMap, configurations, properties);
```
