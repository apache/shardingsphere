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
  ds_0: 
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_1:
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}  
  
  bindingTables:
    - t_order,t_order_item
  
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds_${user_id % 2}
  
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingjdbc.core.keygen.DefaultKeyGenerator
  
  props:
    # MEMORY_STRICTLY: Proxy holds as many connections as the count of actual tables routed in a database.
    #                 The benefit of this approach is saving memory for Proxy by Stream ResultSet.
    # CONNECTION_STRICTLY: Proxy will release connections after get the overall rows from the ResultSet.
    #                      Meanwhile, the cost of the memory will be increased.
    proxy.mode: CONNECTION_STRICTLY
    sql.show: true
```

### 读写分离

```yaml
dataSources:
  ds_master:
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
  ds_slave_0:
    url: jdbc:mysql://localhost:3306/ds_slave_0
    username: root
    password: 
  ds_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave_1
    username: root
    password: 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave_0
    - ds_slave_1
```
### 数据分片 + 读写分离

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password: 
  ds_0_slave_0: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds_0_slave_0
      username: root
      password: 
  ds_0_slave_1: !!org.apache.commons.dbcp.BasicDataSource
      driverClassName: com.mysql.jdbc.Driver
      url: jdbc:mysql://localhost:3306/ds_0_slave_1
      username: root
      password: 
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 
  ds_1_slave_0: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds_1_slave_0
        username: root
        password: 
  ds_1_slave_1: !!org.apache.commons.dbcp.BasicDataSource
        driverClassName: com.mysql.jdbc.Driver
        url: jdbc:mysql://localhost:3306/ds_1_slave_1
        username: root
        password: 

shardingRule:  
  tables:
    t_order: 
      actualDataNodes: ds_${0..1}.t_order_${0..1}
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_item_${order_id % 2}  
  
  bindingTables:
    - t_order,t_order_item
  
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: ds_${user_id % 2}
  
  defaultTableStrategy:
    none:
  defaultKeyGeneratorClassName: io.shardingjdbc.core.keygen.DefaultKeyGenerator
  
  masterSlaveRules:
      ms_ds_0:
        masterDataSourceName: ds_0
        slaveDataSourceNames:
          - ds_0_slave_0
          - ds_0_slave_1
        loadBalanceAlgorithmType: ROUND_ROBIN
        configMap:
          master-slave-key0: master-slave-value0
      ms_ds_1:
        masterDataSourceName: ds_1
        slaveDataSourceNames: 
          - ds_1_slave_0
          - ds_1_slave_1
        loadBalanceAlgorithmType: ROUND_ROBIN
        configMap:
          master-slave-key1: master-slave-value1

  props:
    # MEMORY_STRICTLY: Proxy holds as many connections as the count of actual tables routed in a database.
    #                 The benefit of this approach is saving memory for Proxy by Stream ResultSet.
    # CONNECTION_STRICTLY: Proxy will release connections after get the overall rows from the ResultSet.
    #                      Meanwhile, the cost of the memory will be increased.
    proxy.mode: CONNECTION_STRICTLY
    sql.show: true
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
