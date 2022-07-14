+++
title = "高可用"
weight = 4
+++

## 背景信息

通过 `YAML` 格式，ShardingSphere 会根据 YAML 配置，自动完成 `ShardingSphereDataSource` 对象的创建，减少用户不必要的编码工作。

## 参数解释

```yaml
rules:
- !READWRITE_SPLITTING
  dataSources:
    replica_ds:
      dynamicStrategy: Dynamic # 动态读写分离
        autoAwareDataSourceName: # 高可用规则逻辑数据源名称

- !DB_DISCOVERY
  dataSources:
    <data-source-name> (+): # 逻辑数据源名称
      dataSourceNames: # 数据源名称列表
        - <data-source>
        - <data-source>
      discoveryHeartbeatName: # 检测心跳名称
      discoveryTypeName: # 数据库发现类型名称
  
  # 心跳检测配置
  discoveryHeartbeats:
    <discovery-heartbeat-name> (+): # 心跳名称
      props:
        keep-alive-cron: # cron 表达式，如：'0/5 * * * * ?'
  
  # 数据库发现类型配置
  discoveryTypes:
    <discovery-type-name> (+): # 数据库发现类型名称
      type: # 数据库发现类型，如：MySQL.MGR 
      props (?):
        group-name: 92504d5b-6dec-11e8-91ea-246e9612aaf1 # 数据库发现类型必要参数，如 MGR 的 group-name
```

## 配置示例

```yaml
databaseName: database_discovery_db

dataSources:
  ds_0:
    url: jdbc:mysql://127.0.0.1:33306/primary_demo_ds?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
  ds_1:
    url: jdbc:mysql://127.0.0.1:33307/primary_demo_ds?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1
  ds_2:
    url: jdbc:mysql://127.0.0.1:33308/primary_demo_ds?serverTimezone=UTC&useSSL=false
    username: root
    password:
    connectionTimeoutMilliseconds: 3000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 50
    minPoolSize: 1

rules:
  - !READWRITE_SPLITTING
    dataSources:
      replica_ds:
        dynamicStrategy:
          autoAwareDataSourceName: readwrite_ds

  - !DB_DISCOVERY
    dataSources:
      readwrite_ds:
        dataSourceNames:
          - ds_0
          - ds_1
          - ds_2
        discoveryHeartbeatName: mgr-heartbeat
        discoveryTypeName: mgr
    discoveryHeartbeats:
      mgr-heartbeat:
        props:
          keep-alive-cron: '0/5 * * * * ?'
    discoveryTypes:
      mgr:
        type: MySQL.MGR
        props:
          group-name: 558edd3c-02ec-11ea-9bb3-080027e39bd2
```
## 相关参考

- [高可用核心特性](cn/features/ha/)
- [JAVA API：高可用配置](/cn/user-manual/shardingsphere-jdbc/java-api/rules/ha/)
- [Spring Boot Starter：高可用配置](/cn/user-manual/shardingsphere-jdbc/spring-boot-starter/rules/ha/)
- [Spring 命名空间：高可用配置](/cn/user-manual/shardingsphere-jdbc/spring-namespace/rules/ha/)
