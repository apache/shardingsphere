+++
pre = "<b>4.2.2. </b>"
toc = true
title = "配置手册"
weight = 2
+++

## 配置示例

### 数据分片

```yaml
dataSources:
  demo_ds_0:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0
    username: root
    password:
  demo_ds_1:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1
    username: root
    password:
shardingRule:
  tables:
    t_order:
      actualDataNodes: demo_ds_${0..1}.t_order_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: demo_ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: demo_ds_${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingjdbc.core.keygen.DefaultKeyGenerator

  props:
    sql.show: true
```

### 读写分离

```yaml
dataSources:
  demo_ds_0:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_0
    username: root
    password:
  demo_ds_1:
    url: jdbc:mysql://127.0.0.1:3306/demo_ds_1
    username: root
    password:
masterSlaveRule:
  name: ds_ms
  masterDataSourceName: demo_ds_0
  slaveDataSourceNames:
    - demo_ds_1
```

### 使用Zookeeper的数据治理

```yaml
#省略数据分片和读写分离配置

orchestration:
  name: orchestration_ds
  overwrite: true
  zookeeper:
    namespace: orchestration
    serverLists: localhost:2181
```

### 使用Etcd的数据治理

```yaml
#省略数据分片和读写分离配置

orchestration:
  name: orchestration_ds
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

## 配置项说明

### 数据分片

```yaml
dataSources: #数据源配置，可配置多个data_source_name
  <data_source_name>: #与Sharding-JDBC配置不同，无需配置数据库连接池
    url: #数据库url连接
    username: #数据库用户名
    password: #数据库密码

shardingRule: #省略数据分片配置，与Sharding-JDBC配置一致
```

### 读写分离

```yaml
dataSources: #省略数据源配置，与数据分片一致

masterSlaveRule: #省略读写分离配置，与Sharding-JDBC配置一致
```

### 使用Zookeeper的数据治理

与Sharding-JDBC配置一致。

### 使用Etcd的数据治理

与Sharding-JDBC配置一致。

## Yaml语法说明

`!!` 表示实例化该类

`-` 表示可以包含一个或多个

`[]` 表示数组，可以与减号相互替换使用
