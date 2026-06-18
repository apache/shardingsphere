+++
title = "广播表"
weight = 2
+++

广播表 YAML 配置方式具有非凡的可读性，通过 YAML 格式，能够快速地理解广播表配置，ShardingSphere 会根据 YAML 配置，自动完成 ShardingSphereDataSource 对象的创建，减少用户不必要的编码工作。

## 参数解释

```yaml
rules:
- !BROADCAST
  tables: # 广播表规则列表
    - <table_name>
    - <table_name>
```

## 操作步骤

1. 在 YAML 文件中配置广播表列表
2. 调用 YamlShardingSphereDataSourceFactory 对象的 createDataSource 方法，根据 YAML 文件中的配置信息创建 ShardingSphereDataSource。

## 配置示例

广播表 YAML 配置示例如下：

```yaml
dataSources:
  ds_0:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_0?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:
  ds_1:
    dataSourceClassName: com.zaxxer.hikari.HikariDataSource
    driverClassName: com.mysql.jdbc.Driver
    standardJdbcUrl: jdbc:mysql://localhost:3306/demo_ds_1?serverTimezone=UTC&useSSL=false&useUnicode=true&characterEncoding=UTF-8
    username: root
    password:

rules:
- !BROADCAST
  tables:
    - t_address
```

通过 YamlShardingSphereDataSourceFactory 的 createDataSource 方法，读取 YAML 配置完成数据源的创建。

```java
YamlShardingSphereDataSourceFactory.createDataSource(getFile("/META-INF/broadcast-databases-tables.yaml"));
```
