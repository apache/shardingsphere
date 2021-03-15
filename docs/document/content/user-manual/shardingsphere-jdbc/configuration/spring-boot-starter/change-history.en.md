+++
title = "Change History"
weight = 7
+++

## 5.0.0-alpha

### Replica Query

#### Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit the data source configuration, please refer to the usage

spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.primary-data-source-name= # Primary data source name
spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.replica-data-source-names= # Replica data source names, multiple data source names separated with comma
spring.shardingsphere.rules.replica-query.data-sources.<replica-query-data-source-name>.load-balancer-name= # Load balance algorithm name

# Load balance algorithm configuration
spring.shardingsphere.rules.replica-query.load-balancers.<load-balance-algorithm-name>.type= # Load balance algorithm type
spring.shardingsphere.rules.replica-query.load-balancers.<load-balance-algorithm-name>.props.xxx= # Load balance algorithm properties
```

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/load-balance) for more details about type of algorithm.

## 4.1.0

### shadow database

#### Configuration Item Explanation

```properties
spring.shardingsphere.orchestration.<orchestration.name>.orchestration-type= # Orchestration governance type
spring.shardingsphere.orchestration.<orchestration.name>.instance-type= # Instance type
spring.shardingsphere.orchestration.<orchestration.name>.server-lists= # The address of the registry server, separated by commas
spring.shardingsphere.orchestration.<orchestration.name>.namespace= # Registry namespace
spring.shardingsphere.orchestration.<orchestration.name>.props.overwrite= # The local configuration overrides the registry configuration

spring.shardingsphere.shadow.column= # Shadow field name
spring.shardingsphere.shadow.shadow-mappings.<product-data-source-name>= # Shadow database name

spring.shardingsphere.shadow.shardingRule.default-data-source-name= # Default shadow database
spring.shardingsphere.shadow.shardingRule.default-database-strategy.inline.sharding-column= # Shadow database sharding rules
spring.shardingsphere.shadow.shardingRule.default-database-strategy.inline.algorithm-expression= # Shadow database fragmentation expression

spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.actual-data-nodes= # Shadow database actual node
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.table-strategy.inline.sharding-column= # Fragment column in shadow database table
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.table-strategy.inline.algorithm-expression= # Shadow database sub-table expression
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.key-generator.type= # Distributed id algorithm
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.key-generator.column= # The column corresponding to the distributed id
spring.shardingsphere.shadow.shardingRule.tables.<logic-table-name>.key-generator.props.worker.id= # Start id

spring.shardingsphere.shadow.shardingRule.binding-tables= # Binding table
spring.shardingsphere.shadow.shardingRule.broadcast-tables= # Broadcast table

# Encryptor
spring.shardingsphere.shadow.shardingRule.encryptRule.encryptors.<encryptor-name>.type= # Encryptor type
spring.shardingsphere.shadow.shardingRule.encryptRule.encryptors.<encryptor-name>.props.<property-name>= # Property configuration

# Encryption and decryption rules
spring.shardingsphere.shadow.shardingRule.encryptRule.tables.<shadow-table-name>.columns.<shadow-column-name>.cipherColumn= # Ciphertext column
spring.shardingsphere.shadow.shardingRule.encryptRule.tables.<shadow-table-name>.columns.<shadow-column-name>.encryptor= # Encryptor name
spring.shardingsphere.shadow.shardingRule.encryptRule.tables.<shadow-table-name>.columns.<shadow-column-name>.plainColumn= # Plain text

```

## 4.0.0-RC2

### Support designated plaintext column and ciphertext column, support JNDI

#### Configuration Item Explanation

```properties
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.type= # Encryptor type, you can customize or choose the built-in type: MD5/AES
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.props.<property-name>= # Property configuration
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.cipherColumn= # Ciphertext field
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.plainColumn= # Plain text
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.encryptor= # Encryptor name
spring.shardingsphere.encrypt.tables.<logic-table-name>.columns.<logic-column-name>.assistedQueryColumn= # Auxiliary query field

spring.shardingsphere.props.query.with.cipher.comlum= # Whether to use encrypted columns

spring.shardingsphere.datasource.<datasource-name>.jndi-name= # jndi name

spring.shardingsphere.masterslave.load-balance-algorithm-type= # Load balancing algorithm

```

## 4.0.0-RC1

### Modify the configuration item to spring prefix, increase data desensitization

#### Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Basic configuration of data source

spring.shardingsphere.datasource.<data-source-name>.type=# Database type
spring.shardingsphere.datasource.<data-source-name>.driver-class-name=# Database driver
spring.shardingsphere.datasource.<data-source-name>.url= # Database link address
spring.shardingsphere.datasource.<data-source-name>.username= # Database user name
spring.shardingsphere.datasource.<data-source-name>.password= # Database password
spring.shardingsphere.datasource.<data-source-name>.max-total= # Maximum number of connections

# Master-slave configuration
spring.shardingsphere.masterslave.name= # master database configuration
spring.shardingsphere.masterslave.master-data-source-name= # master database name
spring.shardingsphere.masterslave.slave-data-source-names= # Slave database name

spring.shardingsphere.orchestration.name= # Data governance instance name
spring.shardingsphere.orchestration.overwrite= # Whether the local configuration overrides the registry configuration
spring.shardingsphere.orchestration.registry.type= # Registry type
spring.shardingsphere.orchestration.registry.namespace= # Registry namespace
spring.shardingsphere.orchestration.registry.server-lists= # The list of connected registry servers. Including IP address and port number. Multiple addresses are separated by commas. Such as: host1:3181,host2:3181

spring.shardingsphere.enabled= # Whether to enable sharding

spring.shardingsphere.sharding.default-data-source-name= # Default data source name
spring.shardingsphere.sharding.default-database-strategy.inline.sharding-column= # Shard column
spring.shardingsphere.sharding.default-database-strategy.inline.algorithm-expression= # Fragmentation expression

spring.shardingsphere.sharding.tables.<logic-table-name>.actual-data-nodes= # Real node
spring.shardingsphere.sharding.tables.<logic-table-name>.table-strategy.inline.sharding-column= # Fragment column of the table
spring.shardingsphere.sharding.tables.<logic-table-name>.table-strategy.inline.algorithm-expression= # Sub-table expression
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.type= # Distributed id algorithm
spring.shardingsphere.sharding.tables.<logic-table-name>.key-generator.column= # The column corresponding to the distributed id

spring.shardingsphere.sharding.binding-tables= # Bound table, multiple tables are separated by commas
spring.shardingsphere.sharding.broadcast-tables= # Broadcast table

spring.shardingsphere.props.sql.show= # Print sql
spring.shardingsphere.props.executor.size= # Maximum number of worker threads

# Encryptor
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.type= # Encryptor type
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.qualifiedColumns= # Encrypted table fields
spring.shardingsphere.encrypt.encryptors.<encryptor-name>.props.<property-name>= # Property configuration

# Encryption rules
spring.shardingsphere.sharding.encryptRule.encryptors.<encryptor-name>.qualifiedColumns= # Fields of the fragmented encrypted table
spring.shardingsphere.sharding.encryptRule.encryptors.<encryptor-name>.type= # Fragment encryption type
```

## 3.0.0

### The registry supports general configuration

#### Configuration Item Explanation

```properties
sharding.jdbc.config.orchestration.name= # Orchestration governance name
sharding.jdbc.config.orchestration.overwrite= # The local configuration overrides the registry configuration
sharding.jdbc.config.orchestration.registry.namespace= # Registry namespace
sharding.jdbc.config.orchestration.registry.server-lists= # The address of the registry server, separated by commas
```

## 3.0.0.M1 (Not Apache Release)

### Support spring Boot Starter 2.X, And adapt to etcd registry

#### Configuration Item Explanation

```properties

# etcd registry
sharding.jdbc.config.orchestration.etcd.max-retries= # Maximum number of retries
sharding.jdbc.config.orchestration.etcd.retry-interval-milliseconds= # Retry interval
sharding.jdbc.config.orchestration.etcd.server-lists= # List of connected etcd servers, separated by commas
sharding.jdbc.config.orchestration.etcd.time-to-live-seconds= # Temporary node survival time
sharding.jdbc.config.orchestration.etcd.timeout-milliseconds= # The timeout period of each request

# zookeeper registry
sharding.jdbc.config.orchestration.zookeeper.base-sleep-time-milliseconds= # The initial value of the interval to wait for retry
sharding.jdbc.config.orchestration.zookeeper.connection-timeout-milliseconds= # Connection timeout
sharding.jdbc.config.orchestration.zookeeper.digest= # Permission token to connect to zookeeper
sharding.jdbc.config.orchestration.zookeeper.max-retries= # Maximum number of retries
sharding.jdbc.config.orchestration.zookeeper.max-sleep-time-milliseconds= # The maximum time between waiting for retry
sharding.jdbc.config.orchestration.zookeeper.namespace= # zookeeper namespace
sharding.jdbc.config.orchestration.zookeeper.server-lists= # List of servers connected to zookeeper
sharding.jdbc.config.orchestration.zookeeper.session-timeout-milliseconds= # Session timeout
```

## 2.0.2 (Not Apache Release)

### Sharding strategy

#### Configuration Item Explanation

```properties

# Composite sharding strategy
sharding.jdbc.config.sharding.default-database-strategy.complex.algorithm-class-name= # Compound sharding algorithm
sharding.jdbc.config.sharding.default-database-strategy.complex.sharding-columns= # Compound shard column name, multiple use multiple separation

# Mandatory routing strategy
sharding.jdbc.config.sharding.default-database-strategy.hint.algorithm-class-name # Routing strategy

# Inline line expression fragmentation strategy
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-expression= # inline expression
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column= # Row Expression Fragmentation Strategy Fragmentation Column

# No sharding strategy
sharding.jdbc.config.sharding.default-database-strategy.none= # No sharding strategy

# Standard sharding strategy
sharding.jdbc.config.sharding.default-database-strategy.standard.precise-algorithm-class-name= # Precision sharding strategy
sharding.jdbc.config.sharding.default-database-strategy.standard.range-algorithm-class-name= # Range fragmentation strategy class
sharding.jdbc.config.sharding.default-database-strategy.standard.sharding-column= # Standard fragmentation strategy fragmentation column

# Table fragmentation strategy
# Composite sharding strategy
sharding.jdbc.config.sharding.default-table-strategy.complex.algorithm-class-name= # Compound sharding algorithm
sharding.jdbc.config.sharding.default-table-strategy.complex.sharding-columns= # Compound shard column name, multiple use multiple separation

# Inline line expression fragmentation strategy
sharding.jdbc.config.sharding.default-table-strategy.inline.algorithm-expression= # inline expression
sharding.jdbc.config.sharding.default-table-strategy.inline.sharding-column= # Row Expression Fragmentation Strategy Fragmentation Column

# No sharding strategy
sharding.jdbc.config.sharding.default-table-strategy.none

# Standard sharding strategy
sharding.jdbc.config.sharding.default-table-strategy.standard.precise-algorithm-class-name= # inline expression
sharding.jdbc.config.sharding.default-table-strategy.standard.range-algorithm-class-name= # Range fragmentation strategy class
sharding.jdbc.config.sharding.default-table-strategy.standard.sharding-column= # Standard fragmentation strategy fragmentation column

# Mandatory routing strategy
sharding.jdbc.config.sharding.default-table-strategy.hint.algorithm-class-name= # Routing strategy
```

## 2.0.0 (Not Apache Release)

### Add registry

#### Configuration Item Explanation

```properties

sharding.jdbc.config.orchestration.zookeeper.namespace= # Zookeeper registry namespace
sharding.jdbc.config.orchestration.zookeeper.server-lists= # Zookeeper connects to the list of registry servers. Including IP address and port number. Multiple addresses are separated by commas. Such as: host1:3181,host2:3181
```

## 2.0.0.M3 (Not Apache Release)

### Compatible with'.' and'—' configuration

#### Configuration Item Explanation

```properties
sharding.jdbc.config.masterslave.master-data-source-name= # Primary data source name
sharding.jdbc.config.masterslave.slave-data-source-names= # Slave node data source name

sharding.jdbc.config.orchestration.registry-center.namespace= # Registry namespace
sharding.jdbc.config.orchestration.registry-center.server-lists= # The list of connected registry servers. Including IP address and port number. Multiple addresses are separated by commas. Such as: host1:3181,host2:3181

sharding.jdbc.config.masterslave.config-map.key1= # Custom configuration
```

## 2.0.0.M2 (Not Apache Release)

### Orchestration governance

#### Configuration Item Explanation

```properties

sharding.jdbc.config.orchestration.name= # Data governance instance name
sharding.jdbc.config.orchestration.overwrite= # The local configuration overrides the registry configuration
sharding.jdbc.config.orchestration.registryCenter.namespace= # Registry namespace
sharding.jdbc.config.orchestration.registryCenter.server-lists= # The list of connected registry servers. Including IP address and port number. Multiple addresses are separated by commas. Such as: host1:3181,host2:3181
```

## 2.0.0.M1 (Not Apache Release)

### support sharding-jdbc-spring-boot-starter

#### Configuration Item Explanation

```properties
sharding.jdbc.datasource.names= # Data source name, separated by commas

# Basic configuration of data source
sharding.jdbc.datasource.<data-source-name>.type= # Database type
sharding.jdbc.datasource.<data-source-name>.driver-class-name= # Database driver
sharding.jdbc.datasource.<data-source-name>.url= # Database link address
sharding.jdbc.datasource.<data-source-name>.username= # Database user name
sharding.jdbc.datasource.<data-source-name>.password= # Database password
sharding.jdbc.datasource.<data-source-name>.maxActive= # Maximum number of connections

# Master-slave configuration
sharding.jdbc.config.masterslave.name= # Primary data source name
sharding.jdbc.config.masterslave.masterDataSourceName= # Primary data source name
sharding.jdbc.config.masterslave.slaveDataSourceNames= # slave data source

# default data source
sharding.jdbc.config.sharding.default-data-source-name= # Default data source
sharding.jdbc.config.sharding.default-database-strategy.inline.sharding-column= # 分库字段
sharding.jdbc.config.sharding.default-database-strategy.inline.algorithm-inline-expression= # 分库逻辑

# Configuration of related data tables in the database
sharding.jdbc.config.sharding.tables.<table-name>.actualDataNodes= # The specific location of the related table in the database
sharding.jdbc.config.sharding.tables.<table-name>.tableStrategy.inline.shardingColumn= # Segmentation field of related data table
sharding.jdbc.config.sharding.tables.<table-name>.tableStrategy.inline.algorithmInlineExpression= # Segmentation strategy of related data tables
sharding.jdbc.config.sharding.tables.<table-name>.keyGeneratorColumnName= # id automatically generated column

sharding.jdbc.config.sharding.props.sql.show= # Print sql
sharding.jdbc.config.sharding.props.executor.size= # Number of worker threads
```
