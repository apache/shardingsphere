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
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65
    
  ds_1:
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65

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
  defaultKeyGeneratorClassName: io.shardingsphere.core.keygen.DefaultKeyGenerator
  
  props:
    proxy.mode: CONNECTION_STRICTLY
    sql.show: false
    
proxyAuthority:
  username: root
  password:
```

### Read-write splitting

```yaml
dataSources:
  ds_master:
    url: jdbc:mysql://localhost:3306/ds_master
    username: root
    password: 
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65
    
  ds_slave_0:
    url: jdbc:mysql://localhost:3306/ds_slave_0
    username: root
    password:
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65 
    
  ds_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_slave_1
    username: root
    password:
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65 

masterSlaveRule:
  name: ds_ms
  masterDataSourceName: ds_master
  slaveDataSourceNames: 
    - ds_slave_0
    - ds_slave_1
    
proxyAuthority:
  username: root
  password:
```

### Sharding + Read-write splitting

```yaml
dataSources:
  ds_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0
    username: root
    password:
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65 
    
  ds_0_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0_slave_0
    username: root
    password: 
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65
          
  ds_0_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_0_slave_1
    username: root
    password:
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65
           
  ds_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1
    username: root
    password: 
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65
      
  ds_1_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1_slave_0
    username: root
    password: 
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65
            
  ds_1_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/ds_1_slave_1
    username: root
    password:
    autoCommit: true
    connectionTimeout: 30000
    idleTimeout: 60000
    maxLifetime: 1800000
    maximumPoolSize: 65 

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
  defaultKeyGeneratorClassName: io.shardingsphere.core.keygen.DefaultKeyGenerator
  
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
    proxy.mode: CONNECTION_STRICTLY
    sql.show: false
    
proxyAuthority:
  username: root
  password:
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
    autoCommit: true #The default config of hikari connection pool
    connectionTimeout: 30000 #The default config of hikari connection pool
    idleTimeout: 60000 #The default config of hikari connection pool
    maxLifetime: 1800000 #The default config of hikari connection pool
    maximumPoolSize: 65 #The default config of hikari connection pool

shardingRule: #Ignore sharding rule configuration, same as Sharding-JDBC
```

### Read-write splitting

```yaml
dataSources: #Ignore data source configuration, same as sharding

masterSlaveRule: #Ignore read-write splitting rule configuration, same as Sharding-JDBC
```

### Proxy Mode

```yaml
  props:
    # MEMORY_STRICTLY: Proxy holds as many connections as the count of actual tables routed in a database.
    #                  The benefit of this approach is saving memory for Proxy by Stream ResultSet.
    # CONNECTION_STRICTLY: Proxy will release connections after get the overall rows from the ResultSet.
    #                      Meanwhile, the cost of the memory will be increased.
    proxy.mode: 
```

### Authorization for Proxy

To perform Authorization for Sharding Proxy when login in. After configuring the username and password, you must use the correct username and password to login into the Proxy.

```yaml
proxyAuthority:
   username: root
   password:
```


### Orchestration by Zookeeper

Same as configuration of Sharding-JDBC.

### Orchestration by Etcd

Same as configuration of Sharding-JDBC.

## Yaml syntax

`!!` means class instantiation

`-` means one or multiple available

`[]` means array, can replace `-` each other
