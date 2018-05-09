+++
pre = "<b>4.2.2. </b>"
toc = true
title = "Configuration Manual"
weight = 2
+++

## Example

### Sharding

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

### Read-write splitting

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

### Orchestration by Zookeeper

```yaml
#Ignore data sources, sharding and read-write splitting configuration

orchestration:
  name: orchestration_ds
  overwrite: true
  zookeeper:
    namespace: orchestration
    serverLists: localhost:2181
```

### Orchestration by Etcd

```yaml
#Ignore data sources, sharding and read-write splitting configuration

orchestration:
  name: orchestration_ds
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

## Configuration reference

### Sharding

```yaml
dataSources: #Data sources configuration, multiple `data_source_name` available
  <data_source_name>: #Different with Sharding-JDBC, do not need configure data source pool here.
    url: #Database URL
    username: #Database username
    password: #Database password

shardingRule: #Ignore sharding rule configuration, same as Sharding-JDBC
```

### Read-write splitting

```yaml
dataSources: #Ignore data source configuration, same as sharding

masterSlaveRule: #Ignore read-write splitting rule configuration, same as Sharding-JDBC
```

### Orchestration by Zookeeper

Same as configuration of Sharding-JDBC.

### Orchestration by Etcd

Same as configuration of Sharding-JDBC.

## Yaml syntax

`!!` means class instantiation

`-` means one or multiple available

`[]` means array, can replace `-` each other
