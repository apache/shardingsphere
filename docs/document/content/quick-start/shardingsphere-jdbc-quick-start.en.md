+++
pre = "<b>2.1. </b>"
title = "ShardingSphere-JDBC"
weight = 1
+++

## Scenarios

There are four ways you can configure Apache ShardingSphere: `Java`, `YAML`, `Spring namespace` and `Spring boot starter`. 
Developers can choose the preferred method according to their requirements. 

## Limitations

Currently only Java language is supported.

## Requirements

The development environment requires Java JRE 8 or later.

## Procedure

1. Rules configuration.

Please refer to [User Manual](/en/user-manual/shardingsphere-jdbc/) for more details.

2. Import Maven dependency

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> Notice: Please change `${latest.release.version}` to the actual version.



3. Edit `application.yml`.


```java
spring:
  shardingsphere:
    datasource:
      names: ds_0, ds_1
      ds_0:
        type: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.mysql.cj.jdbc.Driver
        jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password: 
      ds_1:
        type: com.zaxxer.hikari.HikariDataSource
        driverClassName: com.mysql.cj.jdbc.Driver
        jdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
        username: root
        password: 
    rules:
      sharding:
        tables:
            ...
```
