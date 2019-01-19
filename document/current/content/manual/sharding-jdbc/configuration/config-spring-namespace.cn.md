+++
toc = true
title = "Spring命名空间配置"
weight = 4
+++

## 注意事项

行表达式标识符可以使用`${...}`或`$->{...}`，但前者与Spring本身的属性文件占位符冲突，因此在Spring环境中使用行表达式标识符建议使用`$->{...}`。

## 配置示例

### 数据分片

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

### 读写分离

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
    
    <bean id="randomStrategy" class="io.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave0, ds_slave1" strategy-ref="randomStrategy">
            <master-slave:props>
                <prop key="sql.show">${sql_show}</prop>
                <prop key="executor.size">10</prop>
                <prop key="foo">bar</prop>
            </master-slave:props>
    </master-slave:data-source>
</beans>
```

### 数据分片 + 读写分离

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
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingsphere.io/schema/shardingsphere/sharding 
                        http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd">
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
    
    <bean id="randomStrategy" class="io.shardingsphere.example.spring.namespace.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    
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

### 数据治理

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:orchestration="http://shardingsphere.io/schema/shardingsphere/orchestration"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingsphere.io/schema/shardingsphere/orchestration
                           http://shardingsphere.io/schema/shardingsphere/orchestration/orchestration.xsd">
    <orchestration:registry-center id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo" operation-timeout-milliseconds="1000" max-retries="3" />
</beans>
```

## 配置项说明

### 分库分表

命名空间：http://shardingsphere.io/schema/shardingsphere/sharding/sharding.xsd

#### \<sharding:data-source />

| *名称*         | *类型* | *说明*         |
| -------------- | ----- | -------------- |
| id             | 属性  | Spring Bean Id |
| sharding-rule  | 标签  | 数据分片配置规则 |
| config-map (?) | 标签  | 用户自定义配置   |
| props (?)      | 标签  | 属性配置        |

#### \<sharding:sharding-rule />

| *名称*                            | *类型* | *说明*                                                                                                  |
| --------------------------------- | ----- | ------------------------------------------------------------------------------------------------------ |
| data-source-names                 | 属性  | 数据源Bean列表，多个Bean以逗号分隔                                                                         | 
| table-rules                       | 标签  | 表分片规则配置对象                                                                                        |
| binding-table-rules (?)           | 标签  | 绑定表规则列表                                                                                            |
| broadcast-table-rules (?)         | 标签  | 广播表规则列表                                                                                            |
| default-data-source-name (?)      | 属性  | 未配置分片规则的表将通过默认数据源定位                                                                       |
| default-database-strategy-ref (?) | 属性  | 默认数据库分片策略，对应\<sharding:xxx-strategy>中的策略Id，缺省表示不分库                                    |
| default-table-strategy-ref (?)    | 属性  | 默认表分片策略，对应\<sharding:xxx-strategy>中的策略Id，缺省表示不分表                                       |
| default-key-generator-ref (?)     | 属性  | 默认自增列值生成器引用，缺省使用`org.apache.shardingsphere.core.keygen.generator.impl.SnowflakeKeyGenerator` |

#### \<sharding:table-rules />

| *名称*         | *类型* | *说明*           |
| -------------- | ----- | --------------- |
| table-rule (+) | 标签  | 表分片规则配置对象 |

#### \<sharding:table-rule />

| *名称*                       | *类型* | *说明*                                                                                                                                                                                                      |
| ---------------------------- | ----- | ---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| logic-table                  | 属性  | 逻辑表名称                                                                                                                                                                                                   |
| actual-data-nodes (?)        | 属性  | 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点。用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况 |
| database-strategy-ref (?)    | 属性  | 数据库分片策略，对应\<sharding:xxx-strategy>中的策略Id，缺省表示使用\<sharding:sharding-rule />配置的默认数据库分片策略                                                                                             |
| table-strategy-ref (?)       | 属性  | 表分片策略，对应\<sharding:xxx-strategy>中的策略Id，缺省表示使用\<sharding:sharding-rule />配置的默认表分片策略                                                                                                    |
| key-generator-ref (?)        | 属性  | 自增列值生成器引用，缺省表示使用默认自增列值生成器                                                                                                                                      |
| logic-index (?)              | 属性  | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表                                                                                                   |

#### \<sharding:binding-table-rules />

| *名称*                 | *类型* | *说明*   |
| ---------------------- | ----- | -------- |
| binding-table-rule (+) | 标签  | 绑定表规则 |

#### \<sharding:binding-table-rule />

| *名称*       | *类型* | *说明*                         |
| ------------ | ----- | ----------------------------- |
| logic-tables | 属性  | 绑定规则的逻辑表名，多表以逗号分隔 |

#### \<sharding:broadcast-table-rules />

| *名称*                    | *类型* | *说明*   |
| ------------------------ | ----- | -------- |
| broadcast-table-rule (+) | 标签  | 广播表规则 |

#### \<sharding:broadcast-table-rule />

| *名称* | *类型* | *说明          |
| ------ | ----- | --------------- |
| table | 属性   | 广播规则的表名 |

#### \<sharding:standard-strategy />

| *名称*                   | *类型* | *说明*                                                         |
| ----------------------- | ------ | ------------------------------------------------------------- |
| id                      | 属性   | Spring Bean Id                                                 |
| sharding-column         | 属性   | 分片列名称                                                      |
| precise-algorithm-ref   | 属性   | 精确分片算法引用，用于=和IN。该类需实现PreciseShardingAlgorithm接口 |
| range-algorithm-ref (?) | 属性   | 范围分片算法引用，用于BETWEEN。该类需实现RangeShardingAlgorithm接口 |

#### \<sharding:complex-strategy />

| *名称*           | *类型* | *说明*                                                   |
| --------------- | ------ | -------------------------------------------------------- |
| id               | 属性  | Spring Bean Id                                           |
| sharding-columns | 属性  | 分片列名称，多个列以逗号分隔                                |
| algorithm-ref    | 属性  | 复合分片算法引用。该类需实现ComplexKeysShardingAlgorithm接口 |

#### \<sharding:inline-strategy />

| *名称*               | *类型* | *说明*                          |
| -------------------- | ----- | ------------------------------ |
| id                   | 属性  | Spring Bean Id                  |
| sharding-column      | 属性  | 分片列名称                       |
| algorithm-expression | 属性  | 分片算法行表达式，需符合groovy语法 |

#### \<sharding:hint-database-strategy />

| *名称*        | *类型* | *说明*                                         |
| ------------- | ----- | ---------------------------------------------- |
| id            | 属性  | Spring Bean Id                                 |
| algorithm-ref | 属性  | Hint分片算法。该类需实现HintShardingAlgorithm接口 |

#### \<sharding:none-strategy />

| *名称* | *类型* | *说明*          |
| ----- | ------ | -------------- |
| id    | 属性    | Spring Bean Id |

#### \<sharding:key-generator />
| *名称*             | *类型*                       | *说明*                                                                           |
| ----------------- | ---------------------------- | ------------------------------------------------------------------------------- |
| column            | 属性                          | 自增列名称                                                                       |
| type              | 属性                          | 自增列值生成器类型，可自定义或选择内置类型：SNOWFLAKE/UUID                            |
| props-ref         | 属性                          | 属性配置, 比如SNOWFLAKE算法的worker.id与max.tolerate.time.difference.milliseconds | 

#### \<sharding:props />

| *名称*                             |  *类型* | *说明*                                        |
| -----------------------------------| ----- | ---------------------------------------------- |
| sql.show (?)                       | 属性  | 是否开启SQL显示，默认值: false                    |
| executor.size (?)                  | 属性  | 工作线程数量，默认值: CPU核数                      |
| max.connections.size.per.query (?) | 属性  | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?)   | 属性  | 是否在启动时检查分表元数据一致性，默认值: false       |

#### \<sharding:config-map />

### 读写分离

命名空间：http://shardingsphere.io/schema/shardingsphere/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *名称*                  | *类型* | *说明*                                                                       |
| ----------------------- | ----- | ---------------------------------------------------------------------------- |
| id                      | 属性  | Spring Bean Id                                                                |
| master-data-source-name | 属性  | 主库数据源Bean Id                                                              |
| slave-data-source-names | 属性  | 从库数据源Bean Id列表，多个Bean以逗号分隔                                        |
| strategy-ref (?)        | 属性  | 从库负载均衡算法引用。该类需实现MasterSlaveLoadBalanceAlgorithm接口               |
| strategy-type (?)       | 属性  | 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`strategy-ref`存在则忽略该配置 |
| config-map (?)          | 标签  | 用户自定义配置                                                                 |
| props (?)               | 标签  | 属性配置                                                                       |

#### \<master-slave:config-map />

#### \<master-slave:props />

| *名称*                             |  *类型* | *说明*                                         |
| -----------------------------------| ----- | ---------------------------------------------- |
| sql.show (?)                       | 属性   | 是否开启SQL显示，默认值: false                    |
| executor.size (?)                  | 属性   | 工作线程数量，默认值: CPU核数                      |
| max.connections.size.per.query (?) | 属性   | 每个物理数据库为每次查询分配的最大连接数量。默认值: 1 |
| check.table.metadata.enabled (?)   | 属性   | 是否在启动时检查分表元数据一致性，默认值: false       |

### 数据分片 + 数据治理

命名空间：http://shardingsphere.io/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:sharding-data-source />

| *名称*              | *类型* | *说明*                                                                    |
| ------------------- | ----- | ------------------------------------------------------------------------ |
| id                  | 属性   | ID                                                                       |
| data-source-ref (?) | 属性   | 被治理的数据库id                                                           |
| registry-center-ref | 属性   | 注册中心id                                                                |
| overwrite           | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。  缺省为不覆盖 |

### 读写分离 + 数据治理

命名空间：http://shardingsphere.io/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:master-slave-data-source />

| *名称*              | *类型* | *说明*                                                                  |
| ------------------- | ----- | ---------------------------------------------------------------------- |
| id                  | 属性   | ID                                                                     |
| data-source-ref (?) | 属性   | 被治理的数据库id                                                         |
| registry-center-ref | 属性   | 注册中心id                                                              |
| overwrite           | 属性   | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。缺省为不覆盖 |

### 数据治理注册中心

命名空间：http://shardingsphere.io/schema/shardingsphere/orchestration/orchestration.xsd

#### \<orchestration:registry-center />

| *名称*                              | *类型* | *说明*                                                                              |
| ----------------------------------- | ----- | ---------------------------------------------------------------------------------- |
| id                                  | 属性  | 注册中心的Spring Bean Id                                                             |
| server-lists                        | 属性  | 连接注册中心服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace (?)                       | 属性  | 注册中心的命名空间                                                                    |
| digest (?)                          | 属性  | 连接注册中心的权限令牌。缺省为不需要权限验证                                             |
| operation-timeout-milliseconds (?)  | 属性  | 操作超时的毫秒数，默认500毫秒                                                          |
| max-retries (?)                     | 属性  | 连接失败后的最大重试次数，默认3次                                                       |
| retry-interval-milliseconds (?)     | 属性  | 重试间隔毫秒数，默认500毫秒                                                            |
| time-to-live-seconds (?)            | 属性  | 临时节点存活秒数，默认60秒                                                             |
