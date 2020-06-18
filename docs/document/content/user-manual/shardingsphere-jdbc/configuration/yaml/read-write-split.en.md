+++
title = "Read-write Split"
weight = 2
+++

## Configuration Example

```yaml
dataSources:
  master_ds: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/master_ds
    username: root
    password: root
  slave_ds0: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/slave_ds0
    username: root
    password: root
  slave_ds1: !!org.apache.commons.dbcp2.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://127.0.0.1:3306/slave_ds1
    username: root
    password: root

rules:
- !MASTER_SLAVE
  dataSources:
    master_slave_ds:
      masterDataSourceName: master_ds
      slaveDataSourceNames:
        - slave_ds0
        - slave_ds1
      loadBalancerName: roundRobin
  
  loadBalancers:
    roundRobin:
      type: ROUND_ROBIN

props:
  sql.show: true
```

## Configuration Item Explanation

```yaml
dataSource: # Ignore data source configuration

rules:
- !MASTER_SLAVE
  dataSources:
    <data_source_name> (+): # Logic data source name of master slave
      masterDataSourceName: # Master data source name
      slaveDataSourceNames: 
        - <slave_data_source_name> (+) # Slave data source name
      loadBalancerName: # Load balance algorithm name
  loadBalancers:
    <load_balancer_name> (+): # Load balance algorithm name
      type: # Load balance algorithm type
      props: # Load balance algorithm properties
        # ...

props:
  # ...
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.
