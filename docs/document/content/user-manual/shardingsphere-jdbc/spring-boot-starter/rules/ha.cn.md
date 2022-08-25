+++
title = "高可用"
weight = 3
+++

## 背景信息

`Spring Boot Starter` 配置方式适用于使用 SpringBoot 的业务场景，能够最大程度地利用 SpringBoot 配置初始化及 Bean 管理的能力，自动完成 `ShardingSphereDataSource` 对象的创建。

## 参数解释

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置，请参考使用手册

spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.auto-aware-data-source-name= # 数据库发现的逻辑数据源名称

spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.data-source-names= # 数据源名称，多个数据源用逗号分隔 如：ds_0, ds_1
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.discovery-heartbeat-name= # 检测心跳名称
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.discovery-type-name= # 数据库发现类型名称
spring.shardingsphere.rules.database-discovery.discovery-heartbeats.<discovery-heartbeat-name>.props.keep-alive-cron= # cron 表达式，如：'0/5 * * * * ?'
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.type= # 数据库发现类型，如：MySQL.MGR
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.props.group-name= # 数据库发现类型必要参数，如 MGR 的 group-name
```

## 操作步骤

1. 引入 MAVEN 依赖

```xml
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

> 注意：请将 `${latest.release.version}` 更改为实际的版本号。

## 配置示例
```properties
spring.shardingsphere.datasource.names=ds-0,ds-1,ds-2
spring.shardingsphere.datasource.ds-0.jdbc-url = jdbc:mysql://127.0.0.1:13306/primary_demo_ds?serverTimezone=UTC&useSSL=false
spring.shardingsphere.datasource.ds-0.username=root
spring.shardingsphere.datasource.ds-0.password=
spring.shardingsphere.datasource.ds-0.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds-0.driver-class-name=com.mysql.cj.jdbc.Driver

spring.shardingsphere.datasource.ds-1.jdbc-url = jdbc:mysql://127.0.0.1:13307/primary_demo_ds?serverTimezone=UTC&useSSL=false
spring.shardingsphere.datasource.ds-1.username=root
spring.shardingsphere.datasource.ds-1.password=
spring.shardingsphere.datasource.ds-1.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds-1.driver-class-name=com.mysql.cj.jdbc.Driver

spring.shardingsphere.datasource.ds-2.jdbc-url = jdbc:mysql://127.0.0.1:13308/primary_demo_ds?serverTimezone=UTC&useSSL=false
spring.shardingsphere.datasource.ds-2.username=root
spring.shardingsphere.datasource.ds-2.password=
spring.shardingsphere.datasource.ds-2.type=com.zaxxer.hikari.HikariDataSource
spring.shardingsphere.datasource.ds-2.driver-class-name=com.mysql.cj.jdbc.Driver

spring.shardingsphere.rules.readwrite-splitting.data-sources.replica_ds.dynamic-strategy.auto-aware-data-source-name=readwrite_ds

spring.shardingsphere.rules.database-discovery.data-sources.readwrite_ds.data-source-names=ds-0, ds-1, ds-2
spring.shardingsphere.rules.database-discovery.data-sources.readwrite_ds.discovery-heartbeat-name=mgr-heartbeat
spring.shardingsphere.rules.database-discovery.data-sources.readwrite_ds.discovery-type-name=mgr
spring.shardingsphere.rules.database-discovery.discovery-heartbeats.mgr-heartbeat.props.keep-alive-cron=0/5 * * * * ?
spring.shardingsphere.rules.database-discovery.discovery-types.mgr.type=MGR
spring.shardingsphere.rules.database-discovery.discovery-types.mgr.props.groupName=b13df29e-90b6-11e8-8d1b-525400fc3996
```
## 相关参考

- [高可用核心特性](cn/features/ha/)
- [JAVA API：高可用配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules/ha/)
- [YAML 配置：高可用配置](/cn/user-manual/shardingsphere-jdbc/yaml-config/rules/ha/)
- [Spring 命名空间：高可用配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha/)
