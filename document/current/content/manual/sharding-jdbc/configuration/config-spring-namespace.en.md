+++
toc = true
title = "Spring Namespace"
weight = 4
+++

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.

## Example

### Sharding

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding 
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <context:annotation-config />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.spring.namespace.jpa.entity" />
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
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="org.apache.shardingsphere.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="org.apache.shardingsphere.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:key-generator id="orderKeyGenerator" type="SNOWFLAKE" column="order_id" />
    <sharding:key-generator id="itemKeyGenerator" type="SNOWFLAKE" column="order_item_id" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds0,ds1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" key-generator-ref="orderKeyGenerator" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" key-generator-ref="itemKeyGenerator" />
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

### Read-write splitting

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave  
                        http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="org.apache.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.spring.namespace.jpa.entity" />
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
    
    <bean id="randomStrategy" class="org.apache.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" strategy-ref="randomStrategy">
            <master-slave:props>
                <prop key="sql.show">${sql_show}</prop>
                <prop key="executor.size">10</prop>
                <prop key="foo">bar</prop>
            </master-slave:props>
    </master-slave:data-source>
</beans>
```

### Sharding + Read-write splitting

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:p="http://www.springframework.org/schema/p"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:tx="http://www.springframework.org/schema/tx"
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding 
                        http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd">
    <context:annotation-config />
    <context:component-scan base-package="org.apache.shardingsphere.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="org.apache.shardingsphere.example.spring.namespace.jpa.entity" />
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
    
    <bean id="ds_master0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_master0_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_master0_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master0_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_master1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_master1_slave0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1_slave0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_master1_slave1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master1_slave1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="randomStrategy" class="org.apache.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    
    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds_ms$->{user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order$->{order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item$->{order_id % 2}" />
    
    <sharding:key-generator id="orderKeyGenerator" type="SNOWFLAKE" column="order_id" />
    <sharding:key-generator id="itemKeyGenerator" type="SNOWFLAKE" column="order_item_id" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds_master0,ds_master0_slave0,ds_master0_slave1,ds_master1,ds_master1_slave0,ds_master1_slave1">
            <sharding:master-slave-rules>
                <sharding:master-slave-rule id="ds_ms0" master-data-source-name="ds_master0" slave-data-source-names="ds_master0_slave0, ds_master0_slave1" strategy-ref="randomStrategy" />
                <sharding:master-slave-rule id="ds_ms1" master-data-source-name="ds_master1" slave-data-source-names="ds_master1_slave0, ds_master1_slave1" strategy-ref="randomStrategy" />
            </sharding:master-slave-rules>
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_ms$->{0..1}.t_order$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" key-generator-ref="orderKeyGenerator" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_ms$->{0..1}.t_order_item$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" key-generator-ref="itemKeyGenerator" />
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

### Orchestration

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding"
       xmlns:master-slave="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave"
       xmlns:reg="http://shardingsphere.apache.org/schema/shardingsphere/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/reg 
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/reg/reg.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding 
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/sharding/sharding.xsd
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave  
                           http://shardingsphere.apache.org/schema/shardingsphere/orchestration/masterslave/master-slave.xsd">
    
    <reg:registry-center id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo" overwtite="false" />
    <sharding:data-source id="shardingMasterSlaveDataSource" registry-center-ref="regCenter" />
    <master-slave:data-source id="masterSlaveDataSource" registry-center-ref="regCenter" />
</beans>
```

## Configuration reference

### Sharding

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/sharding/sharding.xsd

#### \<sharding:data-source />

| *Name*         | *Type*    | *Description*               |
| -------------- | --------- | --------------------------- |
| id             | Attribute | Spring Bean Id              |
| sharding-rule  | Tag       | Sharding rule configuration |
| props (?)      | Tag       | Properties                  |

#### \<sharding:sharding-rule />

| *Name*                            | *Type*    | *Description*                                                                                                                         |
| --------------------------------- | --------- | ------------------------------------------------------------------------------------------------------------------------------------- |
| data-source-names                 | Attribute | Data source bean list. Multiple data sources names separated with comma                                                               | 
| table-rules                       | Tag       | Table rule configurations                                                                                                             |
| binding-table-rules (?)           | Tag       | Binding table rule configurations                                                                                                     |
| broadcast-table-rules (?)         | Tag       | Broadcast table rule configurations                                                                                                   |
| default-data-source-name (?)      | Attribute | If table not configure at table rule, will route to defaultDataSourceName                                                             |
| default-database-strategy-ref (?) | Attribute | Default database sharding strategy, reference id of \<sharding:xxx-strategy>, Default for not sharding                                |
| default-table-strategy-ref (?)    | Attribute | Default table sharding strategy, reference id of \<sharding:xxx-strategy>, Default for not sharding                                   |
| default-key-generator (?)         | Attribute | Default key generator configuration, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID. Default key generator is `org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator` |

#### \<sharding:table-rules />

| *Name*         | *Type* | *Description*            |
| -------------- | ------ | ------------------------ |
| table-rule (+) | Tag    | Table rule configuration |

#### \<sharding:table-rule />

| *Name*                       | *Type*    | *Description*                                                                                                                                                                                               |
| ---------------------------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logic-table                  | Attribute | Name of logic table                                                                                                                                                                                         |
| actual-data-nodes (?)        | Attribute | Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7} |
| database-strategy-ref (?)    | Attribute | Databases sharding strategy, use default databases sharding strategy if absent                                                                                                                              |
| table-strategy-ref (?)       | Attribute | Tables sharding strategy, use default tables sharding strategy if absent                                                                                                                                    |
| key-generator (?)            | Attribute | Key generator, use default key generator if absent.                                                                                                                                                         |
| logic-index (?)              | Attribute | Name if logic index. If use `DROP INDEX XXX` SQL in Oracle/PostgreSQL, This property needs to be set for finding the actual tables                                                                          |

#### \<sharding:binding-table-rules />

| *Name*                 | *Type* | *Description*                    |
| ---------------------- | ------ | -------------------------------- |
| binding-table-rule (+) | Tag    | Binding table rule configuration |

#### \<sharding:binding-table-rule />

| *Name*       | *Type*    | *Description*                                            |
| ------------ | --------- | -------------------------------------------------------- |
| logic-tables | Attribute | Name of logic table. Multiple names separated with comma |

#### \<sharding:broadcast-table-rules />

| *Name*                   | *Type* | *Description*                      |
| ------------------------ | ------ | ---------------------------------- |
| broadcast-table-rule (+) | Tag    | Broadcast table rule configuration |

#### \<sharding:broadcast-table-rule />

| *Name* | *Type*    | *Description* |
| ------ | --------- | ------------- |
| table  | Attribute | Name of table |

#### \<sharding:standard-strategy />

| *Name*                  | *Type*    | *Description*                                                                                              |
| ----------------------- | --------- | ---------------------------------------------------------------------------------------------------------- |
| id                      | Attribute | Spring Bean Id                                                                                             |
| sharding-column         | Attribute | Name of sharding column                                                                                    |
| precise-algorithm-ref   | Attribute | Reference of precise algorithm used for `=` and `IN`. This class need to implements PreciseShardingAlgorithm |
| range-algorithm-ref (?) | Attribute | Reference of range algorithm used for `BETWEEN`. This class need to implements RangeShardingAlgorithm      |

#### \<sharding:complex-strategy />

| *Name*           | *Type*    | *Description*                                                                                       |
| ---------------- | --------- | --------------------------------------------------------------------------------------------------- |
| id               | Attribute | Spring Bean Id                                                                                      |
| sharding-columns | Attribute | Names of sharding columns. Multiple columns separated with comma                                    |
| algorithm-ref    | Attribute | Reference of complex sharding algorithm. This class need to implements ComplexKeysShardingAlgorithm |

#### \<sharding:inline-strategy />

| *Name*               | *Type*    | *Description*                            |
| -------------------- | --------- | ---------------------------------------- |
| id                   | Attribute | Spring Bean Id                           |
| sharding-column      | Attribute | Name of sharding column                  |
| algorithm-expression | Attribute | Inline expression for sharding algorithm |

#### \<sharding:hint-database-strategy />

| *Name*        | *Type*    | *Description*                                                                             |
| ------------- | --------- | ----------------------------------------------------------------------------------------- |
| id            | Attribute | Spring Bean Id                                                                            |
| algorithm-ref | Attribute | Reference of hint sharding algorithm. This class need to implements HintShardingAlgorithm |

#### \<sharding:none-strategy />

| *Name* | *Type*    | *Description*  |
| ------ | --------- | -------------- |
| id     | Attribute | Spring Bean Id |

#### \<sharding:key-generator />
| *Name*             | *Type*                       | *Description*                                                                               |
| ----------------- | ---------------------------- | -------------------------------------------------------------------------------------------- |
| column            | Attribute                    | Column name of key generator                                                                 |
| type              | Attribute                    | Type of key generator, use user-defined ones or built-in ones, e.g. SNOWFLAKE, UUID          |
| props-ref         | Attribute                    | Properties, e.g. `worker.id` and `max.tolerate.time.difference.milliseconds` for `SNOWFLAKE` | 
 
#### \<sharding:props />

| *Name*                              | *Type*    | *Description*                                                                  |
| ------------------------------------| --------- | ------------------------------------------------------------------------------ |
| sql.show (?)                        | Attribute | To show SQLS or not, default value: false                                      |
| executor.size (?)                   | Attribute | The number of working threads, default value: CPU count                        |
| max.connections.size.per.query (?)  | int       | Max connection size for every query to every actual database. default value: 1 |
| check.table.metadata.enabled (?)    | boolean   | Check the metadata consistency of all the tables, default value : false         |

### Read-write splitting

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *Name*                  | *Type*    | *Description*                                                                                                 |
| ----------------------- | --------- | ------------------------------------------------------------------------------------------------------------- |
| id                      | Attribute | Spring Bean Id                                                                                                |
| master-data-source-name | Attribute | Reference of master data source                                                                               |
| slave-data-source-names | Attribute | Reference of slave data sources. Multiple columns separated with comma                                        |
| strategy-ref (?)        | Attribute | Reference of load balance algorithm. This class need to implements MasterSlaveLoadBalanceAlgorithm            |
| strategy-type (?)       | Attribute | Load balance algorithm type, values should be: `ROUND_ROBIN` or `RANDOM`. Ignore if `strategy-ref` is present |
| props (?)               | Tag       | Properties                                                                                                    |

#### \<master-slave:props />

| *Name*                              | *Type*    | *Description*                                                                  |
| ----------------------------------- | --------- | ------------------------------------------------------------------------------ |
| sql.show (?)                        | Attribute | To show SQLS or not, default value: false                                      |
| executor.size (?)                   | Attribute | The number of working threads, default value: CPU count                        |
| max.connections.size.per.query (?)  | int       | Max connection size for every query to every actual database. default value: 1 |
| check.table.metadata.enabled (?)    | boolean   | Check the metadata consistency of all the tables, default value : false         |

### Sharding + orchestration

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:sharding-data-source />

| *Name*                  | *Type*    | *Description*                                                |
| ----------------------- | --------- | ------------------------------------------------------------ |
| id                      | Attribute  | ID                                                          |
| data-source-ref (?)     | Attribute  | The id of data source to be orchestrated                    |
| registry-center-ref     | Attribute  | The id of registry center                                   |
| overwrite               | Attribute  | Use local configuration to overwrite registry center or not |

### Read-write splitting + orchestration

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:master-slave-data-source />

| *Name*                  | *Type*    | *Description*                                                |
| ----------------------- | --------- | ------------------------------------------------------------ |
| id                      | Attribute  | ID                                                          |
| data-source-ref (?)     | Attribute  | The id of data source to be orchestrated                    |
| registry-center-ref     | Attribute  | The id of registry center                                   |
| overwrite               | Attribute  | Use local configuration to overwrite registry center or not |

### Orchestration registry center

Namespace: http://shardingsphere.apache.org/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:registry-center />

| *Name*                             | *Type*    | *Description*                                                                   |
| ---------------------------------- | --------- | ------------------------------------------------------------------------------- |
| id                                 | Attribute | Spring Bean Id of registry center                                               |
| server-lists                       | Attribute | Registry servers list, multiple split as comma. Example: host1:2181,host2:2181 |
| namespace (?)                      | Attribute | Namespace of registry                                                          |
| digest (?)                         | Attribute | Digest for registry. Default is not need digest                                |
| operation-timeout-milliseconds (?) | Attribute | Operation timeout time in milliseconds, default value is 500 seconds           |
| max-retries (?)                    | Attribute | Max number of times to retry, default value is 3                                |
| retry-interval-milliseconds (?)    | Attribute | Time interval in milliseconds on each retry, default value is 500 milliseconds |
| time-to-live-seconds (?)           | Attribute | Time to live in seconds of ephemeral keys, default value is 60 seconds          |
