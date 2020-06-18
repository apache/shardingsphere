+++
title = "读写分离"
weight = 2
+++

## 配置示例

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

## 配置项说明

```yaml
dataSources: # 省略数据源配置

rules:
- !MASTER_SLAVE
  dataSources:
    <data_source_name> (+): # 读写分离逻辑数据源名称
      masterDataSourceName: # 主库数据源名称
      slaveDataSourceNames: 
        - <slave_data_source_name> (+) # 从库数据源名称
      loadBalancerName: # 负载均衡算法名称
  loadBalancers:
    <load_balancer_name> (+): # 负载均衡算法名称
      type: # 负载均衡算法类型
      props: # 负载均衡算法属性配置
        # ...

props:
  # ...
```

算法类型的详情，请参见[内置负载均衡算法列表](/cn/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance)。
