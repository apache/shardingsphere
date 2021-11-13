+++
title = "Spring namespace configuration change history"
weight = 3

+++

## ShardingSphere-5.0.0-beta

### Sharding

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding-5.0.0.xsd)

\<sharding:rule />

| *Name*                                | *Type*    | *Description*                               |
| ------------------------------------- | --------- | ------------------------------------------- |
| id                                    | Attribute | Spring Bean Id                              |
| table-rules (?)                       | Tag       | Sharding table rule configuration           |
| auto-table-rules (?)                  | Tag       | Automatic sharding table rule configuration |
| binding-table-rules (?)               | Tag       | Binding table rule configuration            |
| broadcast-table-rules (?)             | Tag       | Broadcast table rule configuration          |
| default-database-strategy-ref (?)     | Attribute | Default database strategy name              |
| default-table-strategy-ref (?)        | Attribute | Default table strategy name                 |
| default-key-generate-strategy-ref (?) | Attribute | Default key generate strategy name          |
| default-sharding-column (?)           | Attribute | Default sharding column name                |

\<sharding:table-rule />

| *Name*                    | *Type*    | *Description*              |
| ------------------------- | --------- | -------------------------- |
| logic-table               | Attribute | Logic table name           |
| actual-data-nodes         | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. |
| actual-data-sources       | Attribute | Data source names for auto sharding table |
| database-strategy-ref     | Attribute | Database strategy name for standard sharding table     |
| table-strategy-ref        | Attribute | Table strategy name for standard sharding table        |
| sharding-strategy-ref     | Attribute | sharding strategy name for auto sharding table         |
| key-generate-strategy-ref | Attribute | Key generate strategy name |

\<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

\<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                            |
| ------------ | --------- | -------------------------------------------------------- |
| logic-tables | Attribute | Binding table name, multiple tables separated with comma |

\<sharding:broadcast-table-rules />

| *Name*                   | *Type* | *Description*                      |
| ------------------------ | ------ | ---------------------------------- |
| broadcast-table-rule (+) | Tag    | Broadcast table rule configuration |

\<sharding:broadcast-table-rule />

| *Name* | *Type*    | *Description*        |
| ------ | --------- | -------------------- |
| table  | Attribute | Broadcast table name |

\<sharding:standard-strategy />

| *Name*          | *Type*    | *Description*                   |
| --------------- | --------- | ------------------------------- |
| id              | Attribute | Standard sharding strategy name |
| sharding-column | Attribute | Sharding column name            |
| algorithm-ref   | Attribute | Sharding algorithm name         |

\<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                |
| ---------------- | --------- | ------------------------------------------------------------ |
| id               | Attribute | Complex sharding strategy name                               |
| sharding-columns | Attribute | Sharding column names, multiple columns separated with comma |
| algorithm-ref    | Attribute | Sharding algorithm name                                      |

\<sharding:hint-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Hint sharding strategy name |
| algorithm-ref | Attribute | Sharding algorithm name     |

\<sharding:none-strategy />

| *Name* | *Type*    | *Description*          |
| ------ | --------- | ---------------------- |
| id     | Attribute | Sharding strategy name |

\<sharding:key-generate-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Key generate strategy name  |
| column        | Attribute | Key generate column name    |
| algorithm-ref | Attribute | Key generate algorithm name |

\<sharding:sharding-algorithm />

| *Name*    | *Type*    | *Description*                 |
| --------- | --------- | ----------------------------- |
| id        | Attribute | Sharding algorithm name       |
| type      | Attribute | Sharding algorithm type       |
| props (?) | Tag       | Sharding algorithm properties |

\<sharding:key-generate-algorithm />

| *Name*    | *Type*    | *Description*                     |
| --------- | --------- | --------------------------------- |
| id        | Attribute | Key generate algorithm name       |
| type      | Attribute | Key generate algorithm type       |
| props (?) | Tag       | Key generate algorithm properties |

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/sharding) and [Built-in Key Generate Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/keygen) for more details about type of algorithm.

##### Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

### Readwrite-Splitting

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/readwrite-splitting/readwrite-splitting-5.0.0.xsd)

\<readwrite-splitting:rule />

| *Name*               | *Type*    | *Description*                                |
| -------------------- | --------- | -------------------------------------------- |
| id                   | Attribute | Spring Bean Id                               |
| data-source-rule (+) | Tag       | Readwrite-splitting data source rule configuration |

\<readwrite-splitting:data-source-rule />

| *Name*                     | *Type*     | *Description*                                                           |
| -------------------------- | ---------- | ----------------------------------------------------------------------- |
| id                         | Attribute  | Readwrite-splitting data source rule name                               |
| write-data-source-name     | Attribute  | Write data source name                                                  |
| read-data-source-names     | Attribute  | Read data source names, multiple data source names separated with comma |
| load-balance-algorithm-ref | Attribute  | Load balance algorithm name                                             |

\<readwrite-splitting:load-balance-algorithm />

| *Name*    | *Type*     | *Description*                     |
| --------- | ---------- | --------------------------------- |
| id        | Attribute  | Load balance algorithm name       |
| type      | Attribute  | Load balance algorithm type       |
| props (?) | Tag        | Load balance algorithm properties |

Please refer to [Built-in Load Balance Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/load-balance) for more details about type of algorithm.
Please refer to [Use Norms](/en/features/readwrite-splitting/use-norms) for more details about query consistent routing.

### Encryption

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt-5.0.0.xsd)

\<encrypt:rule />

| *Name*                    | *Type*    | *Description*                                                                                  | *Default Value* |
| ------------------------- | --------- | ---------------------------------------------------------------------------------------------- | --------------- |
| id                        | Attribute | Spring Bean Id                                                                                 |                 |
| queryWithCipherColumn (?) | Attribute | Whether query with cipher column for data encrypt. User you can use plaintext to query if have | true            |
| table (+)                 | Tag       | Encrypt table configuration                                                                    |                 |

\<encrypt:table />

| *Name*    | *Type*     | *Description*                |
| --------- | ---------- | ---------------------------- |
| name       | Attribute | Encrypt table name           |
| column (+) | Tag       | Encrypt column configuration |

\<encrypt:column />

| *Name*                    | *Type*     | *Description*              |
| ------------------------- | ---------- | -------------------------- |
| logic-column              | Attribute  | Column logic name          |
| cipher-column             | Attribute  | Cipher column name         |
| assisted-query-column (?) | Attribute  | Assisted query column name |
| plain-column (?)          | Attribute  | Plain column name          |
| encrypt-algorithm-ref     | Attribute  | Encrypt algorithm name     |

\<encrypt:encrypt-algorithm />

| *Name*    | *Type*     | *Description*                |
| --------- | ---------- | ---------------------------- |
| id        | Attribute  | Encrypt algorithm name       |
| type      | Attribute  | Encrypt algorithm type       |
| props (?) | Tag        | Encrypt algorithm properties |

Please refer to [Built-in Encrypt Algorithm List](/en/user-manual/shardingsphere-jdbc/builtin-algorithm/encrypt) for more details about type of algorithm.

### Shadow-DB

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd](http://shardingsphere.apache.org/schema/shardingsphere/shadow/shadow-5.0.0.xsd)

\<shadow:rule />

| *Name*      | *Type*    | *Description*                    |
| ----------- | ---------- | ------------------------------- |
| id          | Attribute  | Spring Bean Id                  |
| column      | Attribute  | Shadow column name              |
| mappings(?) | Tag        | Mapping relationship between production database and shadow database |

\<shadow:mapping />

| *Name*                   | *Type*    | *Description*               |
| ------------------------ | --------- | --------------------------- |
| product-data-source-name | Attribute  | Production database name   |
| shadow-data-source-name  | Attribute  | Shadow database name       |

## 4.x

### Sharding

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd](http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd)

\<sharding:data-source />

| *Name*                                | *Type*    | *Description*                               |
| ------------------------------------- | --------- | ------------------------------------------- |
| id                                    | Attribute | Spring Bean Id                              |
| sharding-rule                         | Tag       | Sharding rule configuration                 |
| props (?)                             | Tag       | Properties                                  |

\<sharding:sharding-rule />

| *Name*                                | *Type*    | *Description*                               |
| ------------------------------------- | --------- | ------------------------------------------- |
| data-source-names                     | Attribute | Data source Bean list with comma separating multiple Beans |
| table-rules                           | Tag       | Configuration objects of table sharding rules           |
| binding-table-rules (?)               | Tag       | Binding table rule list            |
| broadcast-table-rules (?)             | Tag       | Broadcast table rule list          |
| default-data-source-name (?)	        | Attribute | Tables without sharding rules will be located through default data source              |
| default-database-strategy-ref (?)     | Attribute | Default database sharding strategy, which corresponds to id of <sharding:xxx-strategy>; default means the database is not split              |
| default-table-strategy-ref (?)        | Attribute | Default table sharding strategy,which corresponds to id of <sharding:xxx-strategy>; default means the database is not split   |
| default-key-generator (?) | Attribute | Default key generator configuration, use user-defined ones or built-in ones, e.g. SNOWFLAKE/UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator`    |
| encrypt-rule (?)        | Tag	| Encrypt rule                |

\<sharding:table-rules />

| *Name*                    | *Type*    | *Description*              |
| ------------------------- | --------- | -------------------------- |
| table-rule (+)               | Tag | Configuration objects of table sharding rules           |

\<sharding:table-rule />

| *Name*                    | *Type*    | *Description*              |
| ------------------------- | --------- | -------------------------- |
| logic-table               | Attribute | Logic table name           |
| actual-data-nodes (?)         | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. |
| database-strategy-ref     | Attribute | Database strategy name for standard sharding table     |
| table-strategy-ref        | Attribute | Table strategy name for standard sharding table        |
| key-generate-strategy-ref | Attribute | Key generate strategy name |

\<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

\<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                            |
| ------------ | --------- | -------------------------------------------------------- |
| logic-tables | Attribute | Binding table name, multiple tables separated with comma |

\<sharding:broadcast-table-rules />

| *Name*                   | *Type* | *Description*                      |
| ------------------------ | ------ | ---------------------------------- |
| broadcast-table-rule (+) | Tag    | Broadcast table rule configuration |

\<sharding:broadcast-table-rule />

| *Name* | *Type*    | *Description*        |
| ------ | --------- | -------------------- |
| table  | Attribute | Broadcast table name |

\<sharding:standard-strategy />

| *Name*          | *Type*    | *Description*                   |
| --------------- | --------- | ------------------------------- |
| id              | Attribute | Standard sharding strategy name |
| sharding-column | Attribute | Sharding column name            |
| precise-algorithm-ref (?)  | Attribute | Precise algorithm reference, applied in `=` and `IN`; the class needs to implement `PreciseShardingAlgorithm` interface         |
| range-algorithm-ref (?)  | Attribute | Range algorithm reference, applied in `BETWEEN`; the class needs to implement `RangeShardingAlgorithm` interface        |

\<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                |
| ---------------- | --------- | ------------------------------------------------------------ |
| id               | Attribute | Complex sharding strategy name                               |
| sharding-columns | Attribute | Sharding column names, multiple columns separated with comma |
| algorithm-ref    | Attribute | Complex sharding algorithm reference; the class needs to implement `ComplexKeysShardingAlgorithm` interface |

\<sharding:inline-strategy />

| *Name*           | *Type*    | *Description*                                                |
| ---------------- | --------- | ------------------------------------------------------------ |
| id               | Attribute | Spring Bean Id                              |
| sharding-columns | Attribute | Sharding column names, multiple columns separated with comma |
| algorithm-ref    | Attribute | Sharding algorithm inline expression, which needs to conform to groovy statements |

\<sharding:hint-database-strategy />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| id            | Attribute | Hint sharding strategy name |
| algorithm-ref | Attribute | Hint sharding algorithm; the class needs to implement `HintShardingAlgorithm` interface |

\<sharding:none-strategy />

| *Name* | *Type*    | *Description*          |
| ------ | --------- | ---------------------- |
| id     | Attribute | Spring Bean Id |

\<sharding:key-generator />

| *Name*        | *Type*    | *Description*               |
| ------------- | --------- | --------------------------- |
| column        | Attribute | Auto-increment column name    |
| type            | Attribute | Auto-increment key generator `Type`; self-defined generator or internal Type generator (SNOWFLAKE/UUID) can both be selected  |
| props-ref | Attribute | The Property configuration reference of key generators |

Properties
Property configuration that can include these properties of these key generators.

SNOWFLAKE
| *Name*        | *Data Type*    | *Explanation*               |
| ------------- | --------- | --------------------------- |
| worker.id (?)	| long	| The unique id for working machine, the default value is `0` |
| max.tolerate.time.difference.milliseconds (?)	| long	| The max tolerate time for different server’s time difference in milliseconds, the default value is `10` |
| max.vibration.offset (?) | int	| The max upper limit value of vibrate number, range `[0, 4096)`, the default value is `1`. Notice: To use the generated value of this algorithm as sharding value, it is recommended to configure this property. The algorithm generates key mod `2^n` (`2^n` is usually the sharding amount of tables or databases) in different milliseconds and the result is always `0` or `1`. To prevent the above sharding problem, it is recommended to configure this property, its value is `(2^n)-1` |

\<sharding:encrypt-rules />

| *Name*    | *Type*    | *Description*                 |
| --------- | --------- | ----------------------------- |
| encryptor-rule (+)        | Tag | Encryptor rule |

\<sharding:encrypt-rule />

| *Name*    | *Type*    | *Description*                 |
| --------- | --------- | ----------------------------- |
| encrypt:encrypt-rule(?)       | Tag | Encrypt rule |

\<sharding:props />

| *Name*    | *Type*    | *Description*                     |
| --------- | --------- | --------------------------------- |
| sql.show (?)	| Attribute|	Show SQL or not; default value: false|
|executor.size (?)	|Attribute	|Executing thread number; default value: CPU core number|
|max.connections.size.per.query (?)	|Attribute	|The maximum connection number that each physical database allocates to each query; default value: 1|
|check.table.metadata.enabled (?)	|Attribute	|Whether to check meta-data consistency of sharding table when it initializes; default value: false|
|query.with.cipher.column (?)	|Attribute	|When there is a plainColumn, use cipherColumn or not to query, default value: true|

### Readwrite-Splitting

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd](http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd)

\<master-slave:data-source />

| *Name*                  | *Type*    | *Explanation*                                                |
| :---------------------- | :-------- | :----------------------------------------------------------- |
| id                      | Attribute | Spring Bean id                                               |
| master-data-source-name | Attribute | Bean id of data source in master database                    |
| slave-data-source-names | Attribute | Bean id list of data source in slave database; multiple Beans are separated by commas |
| strategy-ref (?)        | Attribute | Slave database load balance algorithm reference; the class needs to implement `MasterSlaveLoadBalanceAlgorithm` interface |
| strategy-type (?)       | Attribute | Load balance algorithm type of slave database; optional value: ROUND_ROBIN and RANDOM; if there is `load-balance-algorithm-class-name`, the configuration can be omitted |
| config-map (?)          | Tag       | Users’ self-defined configurations                           |
| props (?)               | Tag       | Attribute configurations                                     |

\<master-slave:props />

| *Name*                             | *Type*    | *Explanation*                                                |
| :--------------------------------- | :-------- | :----------------------------------------------------------- |
| sql.show (?)                       | Attribute | Show SQL or not; default value: false                        |
| executor.size (?)                  | Attribute | Executing thread number; default value: CPU core number      |
| max.connections.size.per.query (?) | Attribute | The maximum connection number that each physical database allocates to each query; default value: 1 |
| check.table.metadata.enabled (?)   | Attribute | Whether to check meta-data consistency of sharding table when it initializes; default value: false |

\<master-slave:load-balance-algorithm />

4.0.0-RC2 version added

| *Name*        | *Type*    | *Explanation*                                                |
| :------------ | :-------- | :----------------------------------------------------------- |
| id            | Attribute | Spring Bean Id                                               |
| type          | Attribute | Type of load balance algorithm, ‘RANDOM'或’ROUND_ROBIN’, support custom extension |
| props-ref (?) | Attribute | Properties of load balance algorithm                         |

### Data Masking

#### Configuration Item Explanation

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd](http://shardingsphere.apache.org/schema/shardingsphere/encrypt/encrypt.xsd)

\<encrypt:data-source />

| *Name*    | *Type*     | *Type*                |
| --------- | ---------- | ---------------------------- |
| id       | Attribute | Spring Bean Id |
| data-source-name       | Attribute | Encrypt data source Bean Id  |
| props (?) | Tag       | Attribute configurations |

\<encrypt:encryptors />

| *Name*    | *Type*     | *Type*                |
| --------- | ---------- | ---------------------------- |
| encryptor(+)       | Tag | Encryptor configuration |

\<encrypt:encryptor />

| *Name*    | *Type*     | *Type*                |
| --------- | ---------- | ---------------------------- |
| id       | Attribute | Names of Encryptor |
| type       | Attribute | Types of Encryptor, including MD5/AES or customize type |
| props-re | Attribute       | Attribute configurations |

\<encrypt:tables />

| *Name*    | *Type*     | *Type*                |
| --------- | ---------- | ---------------------------- |
| table(+)       | Tag | Encrypt table configuration |

\<encrypt:table />

| *Name*    | *Type*     | *Type*                |
| --------- | ---------- | ---------------------------- |
| column(+)       | Tag | Encrypt column configuration |

\<encrypt:column />

| *Name*                    | *Type*     | *Description*              |
| ------------------------- | ---------- | -------------------------- |
| logic-column              | Attribute  | Column logic name          |
| cipher-column             | Attribute  | Cipher column name         |
| assisted-query-column (?) | Attribute  | Assisted query column name |
| plain-column (?)          | Attribute  | Plain column name          |

\<encrypt:props />

| *Name*    | *Type*     | *Description*                |
| --------- | ---------- | ---------------------------- |
| sql.show (?)| Attribute  | Show SQL or not; default value: false |
| query.with.cipher.column (?) | Attribute | When there is a plainColumn, use cipherColumn or not to query, default value: true |

### Orchestration

#### Data Sharding + Orchestration

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:master-slave-data-source />

| *Name*      | *Type*    | *Description*                    |
| ----------- | ---------- | ------------------------------- |
| id          | Attribute  | Id                  |
| data-source-ref (?)   | Attribute  | Orchestrated database Id        |
| registry-center-ref          | Attribute  | Registry center Id                  |
| overwrite          | Attribute  | Whether to overwrite local configurations with registry center configurations; if it can, each initialization should refer to local configurations; default means not to overwrite                  |

#### Read-Write Split + Orchestration

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:sharding-data-source />

| *Name*      | *Type*    | *Description*                    |
| ----------- | ---------- | ------------------------------- |
| id          | Attribute  | Id                  |
| data-source-ref (?)   | Attribute  | Orchestrated database Id        |
| registry-center-ref          | Attribute  | Registry center Id                  |
| overwrite          | Attribute  | Use local configuration to overwrite registry center or not |

#### Data Masking + Orchestration

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:encrypt-data-source />

| *Name*      | *Type*    | *Description*                    |
| ----------- | ---------- | ------------------------------- |
| id          | Attribute  | Id                  |
| data-source-ref (?)   | Attribute  | Orchestrated database Id        |
| registry-center-ref          | Attribute  | Registry center Id                  |
| overwrite          | Attribute  | Use local configuration to overwrite registry center or not |

#### Orchestration registry center

Namespace: [http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd](http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd)

\<orchestration:registry-center />

| *Name*      | *Type*    | *Description*                    |
| ----------- | ---------- | ------------------------------- |
| id          | Attribute  | Spring Bean Id of registry center   |   
|type	|Attribute	|Registry center type. Example:zookeeper|
|server-lists	|Attribute	|Registry servers list, multiple split as comma. Example: host1:2181,host2:2181|
|namespace (?)	|Attribute	|Namespace of registry|
|digest (?)	|Attribute	|Digest for registry. Default is not need digest|
|operation-timeout-milliseconds (?)	|Attribute	|Operation timeout time in milliseconds, default value is 500 seconds|
|max-retries (?)	|Attribute	|Max number of times to retry, default value is 3|
|retry-interval-milliseconds (?)	|Attribute	|Time interval in milliseconds on each retry, default value is 500 milliseconds|
|time-to-live-seconds (?)	|Attribute	|Living time of temporary nodes; default value: 60 seconds|
|props-ref (?)	|Attribute	|Other customize properties of registry center|

## 3.x

Attention
Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

### Sharding

#### Configuration Item Explanation

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.io/schema/shardingsphere/sharding 
                        http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingsphere.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
    
    <bean id="ds0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingsphere.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingsphere.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
            <sharding:binding-table-rules>
                <sharding:binding-table-rule logic-tables="t_order, t_order_item" />
            </sharding:binding-table-rules>
            <sharding:broadcast-table-rules>
                <sharding:broadcast-table-rule table="t_config" />
            </sharding:broadcast-table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### Readwrite-splitting

#### Configuration Item Explanation

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.io/schema/shardingsphere/masterslave  
                        http://shardingsphere.io/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingsphere.example.spring.namespace.jpa.entity" />
        <property name="jpaProperties">
            <props>
                <prop key="hibernate.dialect">org.hibernate.dialect.MySQLDialect</prop>
                <prop key="hibernate.hbm2ddl.auto">create</prop>
                <prop key="hibernate.show_sql">true</prop>
            </props>
        </property>
    </bean>
    <bean id="transactionManager" class="org.springframework.orm.jpa.JpaTransactionManager" p:entityManagerFactory-ref="entityManagerFactory" />
    <tx:annotation-driven />
    
    <bean id="ds_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="randomStrategy" class="io.shardingsphere.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" strategy-ref="randomStrategy">
            <master-slave:props>
                <prop key="sql.show">${sql_show}</prop>
                <prop key="executor.size">10</prop>
                <prop key="foo">bar</prop>
            </master-slave:props>
    </master-slave:data-source>
</beans>
```

### Orchestration

#### Configuration Item Explanation

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingsphere.io/schema/shardingsphere/orchestration/sharding"
       xmlns:master-slave="http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave"
       xmlns:reg="http://shardingsphere.io/schema/shardingsphere/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/reg 
                           http://shardingsphere.io/schema/shardingsphere/orchestration/reg/reg.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/sharding 
                           http://shardingsphere.io/schema/shardingsphere/orchestration/sharding/sharding.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave  
                           http://shardingsphere.io/schema/shardingsphere/orchestration/masterslave/master-slave.xsd">
    
    <reg:registry-center id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo" overwtite="false" />
    <sharding:data-source id="shardingMasterSlaveDataSource" registry-center-ref="regCenter" />
    <master-slave:data-source id="masterSlaveDataSource" registry-center-ref="regCenter" />
</beans>
```

## 2.x

### Readwrite-splitting

#### The configuration example for Spring namespace

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingsphere.io/schema/shardingjdbc/sharding"
    xmlns:masterslave="http://shardingsphere.io/schema/shardingjdbc/masterslave"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://shardingsphere.io/schema/shardingjdbc/sharding
                        http://shardingsphere.io/schema/shardingjdbc/sharding/sharding.xsd
                        http://shardingsphere.io/schema/shardingjdbc/masterslave
                        http://shardingsphere.io/schema/shardingjdbc/masterslave/master-slave.xsd
                        ">
    <!-- Actual source data Configuration -->
    <bean id="dbtbl_0_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_0_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_0_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_0_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="dbtbl_1_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/dbtbl_1_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <!-- Readwrite-splitting DataSource Configuration -->
    <master-slave:data-source id="dbtbl_0" master-data-source-name="dbtbl_0_master" slave-data-source-names="dbtbl_0_slave_0, dbtbl_0_slave_1" strategy-type="ROUND_ROBIN" />
    <master-slave:data-source id="dbtbl_1" master-data-source-name="dbtbl_1_master" slave-data-source-names="dbtbl_1_slave_0, dbtbl_1_slave_1" strategy-type="ROUND_ROBIN" />
    
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="dbtbl_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 4}" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="dbtbl_0, dbtbl_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="dbtbl_${0..1}.t_order_${0..3}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy"/>
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```