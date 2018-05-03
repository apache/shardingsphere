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
  defaultKeyGeneratorClass: io.shardingjdbc.core.keygen.DefaultKeyGenerator
  
  props:
    sql.show: true
```

### Read-write splitting

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
