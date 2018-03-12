+++
toc = true
title = "Orchestration"
weight = 6
prev = "/02-guide/configuration/"
next = "/02-guide/hint-sharding-value/"

+++

Sharding-JDBC provides orchestration for databases in Version 2.0.0.M1, which mainly includes:

* The centralized and dynamic configuration can support the dynamic strategy switching of Sharding and read-write splitting.
* Provide the circuit-breaker mechanism for database access, and the switch that disables access to Slaves.
* Support for Zookeeper and Etcd registry.

# 

## Zookeeper

Please use Zookeeper 3.4.6 and above to set up the registration center. [Reference](https://zookeeper.apache.org/doc/trunk/zookeeperStarted.html)

## Etcd

Please use Etcd V3 and above to set up the registration center. [Reference](https://coreos.com/etcd/docs/latest)

## The structure of registry

The registry is defined in the namespace and you can create the object running node to access the database, by which you can distinguish different accessing instances. The namespace contains two child nodes, namely, config and state.

## The config node

It mainly includes the data-management related configuration information such as data source, Sharding, Read-write splitting, ConfigMap and configuration of the Properties, stored in a JSON format. You can modify this node to get dynamic configuration management.

```
config
    ├──datasource                                # The config of data source 
    ├──sharding                                  # The root node of Sharding configuration
    ├      ├──rule                               # The rule of Sharding
    ├      ├──configmap                          # The ConfigMap config of Sharding, stored in the form of K/V, e.g. {"key1":"value1"}
    ├      ├──props                              # The config of Properties
    ├──masterslave                               # The config of Read-write splitting
    ├      ├──rule                               # The rule of Read-write splitting 
    ├      ├──configmap                          # The ConfigMap config of Read-write splitting, stored in the form of K/V, e.g. {"key1":"value1"}
```

### The child node of data source

It is a collection of multiple database connection pools, and the properties of different connection pools should be configured by users, e.g. DBCP，C3P0，Druid, HikariCP.

```json
[{"name":"demo_ds","clazz":"org.apache.commons.dbcp.BasicDataSource","defaultAutoCommit":"true","defaultReadOnly":"false","defaultTransactionIsolation":"-1","driverClassName":"com.mysql.jdbc.Driver","initialSize":"0","logAbandoned":"false","maxActive":"8","maxIdle":"8","maxOpenPreparedStatements":"-1","maxWait":"-1","minEvictableIdleTimeMillis":"1800000","minIdle":"0","numTestsPerEvictionRun":"3","password":"","removeAbandoned":"false","removeAbandonedTimeout":"300","testOnBorrow":"false","testOnReturn":"false","testWhileIdle":"false","timeBetweenEvictionRunsMillis":"-1","url":"jdbc:mysql://localhost:3306/demo_ds","username":"root","validationQueryTimeout":"-1"}]
```

### The child node of sharding

#### The child node of rule

The configuration of Sharding, including the configs of  Sharding and Read-write splitting.

```json
{"tableRuleConfigs":[{"logicTable":"t_order","actualDataNodes":"demo_ds.t_order_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_id"},{"logicTable":"t_order_item","actualDataNodes":"demo_ds.t_order_item_${0..1}","databaseShardingStrategyConfig":{},"tableShardingStrategyConfig":{"type":"STANDARD","shardingColumn":"order_id","preciseAlgorithmClassName":"io.shardingjdbc.example.orchestration.spring.namespace.mybatis.algorithm.PreciseModuloTableShardingAlgorithm","rangeAlgorithmClassName":""},"keyGeneratorColumnName":"order_item_id"}],"bindingTableGroups":["t_order, t_order_item"],"defaultDatabaseShardingStrategyConfig":{},"defaultTableShardingStrategyConfig":{},"masterSlaveRuleConfigs":[]}
```

#### The child node of ConfigMap

The ConfigMap config of Sharding, stored in the form of K/V.

```json
{"key1":"value1"}
```

#### The child node of props

They are the Sharding Properties in sharding-jdbc configuration.

```json
{"executor.size":"20","sql.show":"true"}
```

### The child node of Master-Slave

#### The child node of rule

The configuration for using Read-write splitting alone.

```json
{"name":"ds_ms","masterDataSourceName":"ds_master","slaveDataSourceNames":["ds_slave_0","ds_slave_1"],"loadBalanceAlgorithmType":"ROUND_ROBIN"}
```

#### The child of ConfigMap

The ConfigMap config of Sharding, stored in the form of K/V.

```json
{"key1":"value1"}
```

## The state node

It contains the nodes of instance and data source.

```
instances
    ├──your_instance_ip_a@-@your_instance_pid_x
    ├──your_instance_ip_b@-@your_instance_pid_y
    ├──....                                    
```

### The instance node 

It includes the running-instance information of database-accessing object, and its child node is the identity of the current running instance. This identify is composed of IP and PID in the running server and always a temporary node. It is registered when the instance is online, and automatically cleaned when the instance is offline. The registry manages the access to the database by monitoring changes in these nodes.

### The data source node

It is used to manage Read-write splitting and dynamically add, remove or disable data sources (Expected in 2.0.0.M3 release).

# Operation guide

## The instance node

You can write DISABLED (case ignored) to the IP address @-@pid node to disable the instance or remove DISABLED to enable.

The commands in Zookeeper:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

The commands in Etcd:

```
etcdctl set /your_app_name/state/instances/your_instance_ip_a@-@your_instance_pid_x DISABLED
```

## The data source node

In use of Read-write splitting or Sharding + Read-write splitting, you can write DISABLED to the child node in data source to disable slaves or remove DISABLED to enable (Expected in 2.0.0.M3 release).

The commands in Zookeeper:

```
[zk: localhost:2181(CONNECTED) 0] set /your_zk_namespace/your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

The commands in Etcd:

```
etcdctl set /your_app_name/state/datasources/your_slave_datasource_name DISABLED
```

# Operation examples

## 1. The JAVA configuration

### Import the dependency of maven 

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### The config example of Zookeeper

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), 
                     new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
     
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        ZookeeperConfiguration result = new ZookeeperConfiguration();
        result.setServerLists("localhost:2181");
        result.setNamespace("orchestration-demo");
        return result;
    }
```

### The config example of Etcd

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(
                 createDataSourceMap(), createShardingRuleConfig(), new ConcurrentHashMap<String, Object>(), new Properties(), 
                 new OrchestrationConfiguration("orchestration-sharding-data-source", getRegistryCenterConfiguration(), false));
    
    private static RegistryCenterConfiguration getRegistryCenterConfiguration() {
        EtcdConfiguration result = new EtcdConfiguration();
        result.setServerLists("http://localhost:2379");
        return result;
    }
```

## 2. The YAML configuration

### Import the dependency of maven 

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Th Configuration examples

#### The introduction for orchestration configs of Sharding in Zookeeper
```yaml
dataSources:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

shardingRule:
  tables:
    config:
      actualDataNodes: db${0..1}.t_config
    t_order: 
      actualDataNodes: db${0..1}.t_order_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
      keyGeneratorClass: io.shardingjdbc.core.yaml.fixture.IncrementKeyGenerator
    t_order_item:
      actualDataNodes: db${0..1}.t_order_item_${0..1}
      #The strategies in other binding tables are same as the first binding table.
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item
  #默认数据库分片策略
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true

orchestration:
  name: demo_yaml_ds_sharding_ms
  overwrite: true
  zookeeper:
    namespace: orchestration-yaml-demo
    serverLists: localhost:2181
```

#### The introduction for orchestration configs of Sharding in Etcd
```yaml
dataSources:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

shardingRule:
  tables:
    config:
      actualDataNodes: db${0..1}.t_config
    t_order: 
      actualDataNodes: db${0..1}.t_order_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_${order_id % 2}
      keyGeneratorColumnName: order_id
      keyGeneratorClass: io.shardingjdbc.core.yaml.fixture.IncrementKeyGenerator
    t_order_item:
      actualDataNodes: db${0..1}.t_order_item_${0..1}
      databaseStrategy: 
        standard:
          shardingColumn: user_id
          preciseAlgorithmClassName: io.shardingjdbc.core.yaml.fixture.SingleAlgorithm
      tableStrategy: 
        inline:
          shardingColumn: order_id
          algorithmInlineExpression: t_order_item_${order_id % 2}
  bindingTables:
    - t_order,t_order_item
  # The default strategy of Sharding 
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true

orchestration:
  name: demo_yaml_ds_sharding_ms
  overwrite: true
  etcd:
    serverLists: http://localhost:2379
```

##### The introduction for orchestration configs of Read-write splitting in Zookeeper

```yaml
dataSources: # The config of data source

shardingRule: # The config of Sharding rules

orchestration: The orchestration configs in Zookeeper
  name: # The node name of the orchestration service
  overwrite: # to decide whether the local configuration can override the registry configuration. If true, the config in each boot is based on the local configuration.
  zookeeper: # The config of registry in Zookeeper
    namespace: # The namespace in Zookeeper
    serverLists: # The server list to connect to Zookeeper, including IP and port, mulitple addresses separated by commas, e.g. host1:2181,host2:2181.
    baseSleepTimeMilliseconds: # The initial value of the interval for retry, unit: Millisecond.
    maxSleepTimeMilliseconds: # The max value of the interval for retry, unit: Millisecond.
    maxRetries: # The number of retry. 
    sessionTimeoutMilliseconds: # Session timeout, unit: Millisecond.
    connectionTimeoutMilliseconds: # Connection timeout, unit: Millisecond.
    digest: # The permission token to connect to Zookeeper, and the default is no permission validation.
```

##### The introduction for orchestration configs of Read-write splitting in Etcd

The configuration items are same as [The introduction for orchestration configs of Read-write splitting in Zookeeper](#The introduction for orchestration configs of Read-write splitting in Zookeeper).

```yaml
dataSources: 

shardingRule:  

orchestration:  
  name:  
  overwrite:  
  etcd: 
    serverLists: 
    timeToLiveSeconds: 
    timeoutMilliseconds: 
    maxRetries: 
    retryIntervalMilliseconds: 
```

##### Sharding DataSource Creation

```java
    DataSource dataSource = OrchestrationShardingDataSourceFactory.createDataSource(yamlFile);
```

##### Read-write splitting DataSource Creation

```java
    DataSource dataSource = OrchestrationMasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### More detail on YAML Configuration
!! :implementation class.

[] :multiple items.

## 3. The Spring namespace configuration

### Import the dependency of maven 

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### The configuration example in Zookeeper

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
    xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true" />
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource" registry-center-ref="regCenter">
        <sharding:sharding-rule data-source-names="dbtbl_0,dbtbl_1" default-data-source-name="dbtbl_0">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="dbtbl_${0..1}.t_order_item_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <reg:zookeeper id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace" base-sleep-time-milliseconds="1000" max-sleep-time-milliseconds="3000" max-retries="3" />
</beans>
```

### The introduction for labels in Zookeeper

#### \<reg:zookeeper/>

| Name                            | Type   | Required | Default | Info                                                                                            |
| ------------------------------- |:-------|:-------  |:------- |:---------------------------------------------------------------------------------------------------|
| id                              | String | Y        |         | The primary key of registry in Spring.                                                                         |
| server-lists                    | String | Y        |         | The server list to connect to Zookeeper<br />Including IP and port<br />multiple servers separated by commas<br />e.g. host1:2181,host2:2181 |
| namespace                       | String | Y        |         | The namespace inf Zookeeper.                                                                                |
| base-sleep-time-milliseconds    | int    | N        | 1000    | The initial value of the interval for retry, unit: Millisecond.                                                              |
| max-sleep-time-milliseconds     | int    | N        | 3000    | The max value of the interval for retry, unit: Millisecond.                                                           |
| max-retries                     | int    | N        | 3       | The number of retry.                                                                                          |
| session-timeout-milliseconds    | int    | N        | 60000   | Session timeout, unit: Millisecond.                                                                          |
| connection-timeout-milliseconds | int    | N        | 15000   | Connection timeout, unit: Millisecond.                                                                           |
| digest                          | String | N        |         | The permission token to connect to Zookeeper, and the default is no permission validation.                                                     |


### The config example in Etcd

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
    xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                        http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                        ">
    <context:property-placeholder location="classpath:conf/rdb/conf.properties" ignore-unresolvable="true" />
    
    <bean id="dbtbl_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="dbtbl_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-class="io.shardingjdbc.spring.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource" registry-center-ref="regCenter">
        <sharding:sharding-rule data-source-names="dbtbl_0,dbtbl_1" default-data-source-name="dbtbl_0">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="dbtbl_${0..1}.t_order_item_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="tableStrategy" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
        </sharding:sharding-rule>
        <sharding:props>
            <prop key="sql.show">true</prop>
        </sharding:props>
    </sharding:data-source>
    
    <reg:etcd id="regCenter" server-lists="http://localhost:2379" time-to-live-seconds="60" timeout-milliseconds="500" max-retries="3" retry-interval-milliseconds="200"/>
</beans>
```

### The introduction for labels in Etcd

#### \<reg:etcd/>

| Name                            | Type   | Required | Default | Info                                                                                              |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | Y     |       | The primary key of registry in Spring.                                                                             |
| server-lists                    | String | Y     |       | The server list to connect to Etcd<br />Including IP and port<br />multiple servers separated by commas<br />e.g. http://host1:2379,http://host2:2379 |
| time-to-live-seconds            | int    | N     | 60    | The survival time of temporary nodes <br /> Unit: Second.                                                                        |
| timeout-milliseconds            | int    | N     | 500   | Session timeout, unit: Millisecond.                                                                     |
| max-retries                     | int    | N     | 3     | The number of retry.                                                                                |
| retry-interval-milliseconds     | int    | N     | 200   | The interval of retry<br />Unit: Millisecond.                                                                         |


## 4. The Spring Boot Configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-orchestration-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### The configuration example in Zookeeper

#### The introduction for orchestration configs of Sharding in Spring Boot

```yaml
sharding.jdbc.datasource.names=ds,ds_0,ds_1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driverClassName=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_${user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=demo_spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.zookeeper.namespace=orchestration-spring-boot-sharding-test
sharding.jdbc.config.orchestration.zookeeper.server-lists=localhost:2181
```

### The configuration example in Etcd

#### The introduction for orchestration configs of Read-write splitting in Spring Boot

```yaml
sharding.jdbc.datasource.names=ds,ds_0,ds_1
sharding.jdbc.datasource.ds.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds.driverClassName=org.h2.Driver
sharding.jdbc.datasource.ds.url=jdbc:mysql://localhost:3306/ds
sharding.jdbc.datasource.ds.username=root
sharding.jdbc.datasource.ds.password=

sharding.jdbc.datasource.ds_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_0.url=jdbc:mysql://localhost:3306/ds_0
sharding.jdbc.datasource.ds_0.username=root
sharding.jdbc.datasource.ds_0.password=

sharding.jdbc.datasource.ds_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_1.url=jdbc:mysql://localhost:3306/ds_1
sharding.jdbc.datasource.ds_1.username=root
sharding.jdbc.datasource.ds_1.password=

sharding.jdbc.config.sharding.default-data-source-name=ds
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column=user_id
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression=ds_${user_id % 2}
sharding.jdbc.config.sharding.tables.t_order.actualDataNodes=ds_${0..1}.t_order_${0..1}
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order.tableStrategy.inline.algorithmInlineExpression=t_order_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order.keyGeneratorColumnName=order_id
sharding.jdbc.config.sharding.tables.t_order_item.actualDataNodes=ds_${0..1}.t_order_item_${0..1}
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.shardingColumn=order_id
sharding.jdbc.config.sharding.tables.t_order_item.tableStrategy.inline.algorithmInlineExpression=t_order_item_${order_id % 2}
sharding.jdbc.config.sharding.tables.t_order_item.keyGeneratorColumnName=order_item_id

sharding.jdbc.config.orchestration.name=demo_spring_boot_ds_sharding
sharding.jdbc.config.orchestration.overwrite=true
sharding.jdbc.config.orchestration.etcd.server-lists=localhost:2379
```

#### The introduction for orchestration configs of Sharding in Spring Boot
Refer to The introduction for orchestration configs of Sharding in YAML.
