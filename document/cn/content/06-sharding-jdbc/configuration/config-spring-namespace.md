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
       xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/sharding"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingjdbc.example.spring.namespace.jpa.entity" />
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
    
    <bean id="ds_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds_0,ds_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_$->{0..1}.t_order_$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_$->{0..1}.t_order_item_$->{0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
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
       xmlns:master-slave="http://shardingjdbc.io/schema/shardingjdbc/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd 
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx 
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave  
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="masterSlaveDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingjdbc.example.spring.namespace.jpa.entity" />
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
    
    <bean id="ds_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="ds_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_slave_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>
    
    <bean id="randomStrategy" class="io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="ds_master" slave-data-source-names="ds_slave_0, ds_slave_1" strategy-ref="randomStrategy" />
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
       xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/sharding"
       xmlns:master-slave="http://shardingjdbc.io/schema/shardingjdbc/masterslave"
       xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context
                        http://www.springframework.org/schema/context/spring-context.xsd
                        http://www.springframework.org/schema/tx
                        http://www.springframework.org/schema/tx/spring-tx.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave
                        http://shardingjdbc.io/schema/shardingjdbc/masterslave/master-slave.xsd">
    <context:annotation-config />
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa" />
    
    <bean id="entityManagerFactory" class="org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean">
        <property name="dataSource" ref="shardingDataSource" />
        <property name="jpaVendorAdapter">
            <bean class="org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter" p:database="MYSQL" />
        </property>
        <property name="packagesToScan" value="io.shardingjdbc.example.spring.namespace.jpa.entity" />
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
    
    <bean id="ds_master_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master_0_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master_0_slave_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master_0_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master_0_slave_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master_1_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master_1_slave_0" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="ds_master_1_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver" />
        <property name="url" value="jdbc:mysql://localhost:3306/ds_master_1_slave_1" />
        <property name="username" value="root" />
        <property name="password" value="" />
    </bean>

    <bean id="randomStrategy" class="io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />

    <master-slave:data-source id="ds_ms_0" master-data-source-name="ds_master_0" slave-data-source-names="ds_master_0_slave_0, ds_master_0_slave_1" strategy-ref="randomStrategy" />
    <master-slave:data-source id="ds_ms_1" master-data-source-name="ds_master_1" slave-data-source-names="ds_master_1_slave_0, ds_master_1_slave_1" strategy-ref="randomStrategy" />

    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="ds_ms_$->{user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_$->{order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item_$->{order_id % 2}" />

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="ds_ms_0,ds_ms_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="ds_ms_$->{0..1}.t_order_$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="ds_ms_$->{0..1}.t_order_item_$->{0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```

### 使用Zookeeper的数据治理

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
       xmlns:master-slave="http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave"
       xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave  
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave/master-slave.xsd">
    
    <reg:zookeeper id="regCenter" server-lists="localhost:2181" namespace="orchestration-spring-namespace-demo" overwtite="false" />
    <sharding:data-source id="shardingMasterSlaveDataSource" registry-center-ref="regCenter" />
    <master-slave:data-source id="masterSlaveDataSource" registry-center-ref="regCenter" />
</beans>
```

### 使用Etcd的数据治理

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
       xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding"
       xmlns:master-slave="http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave"
       xmlns:reg="http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
                           http://www.springframework.org/schema/beans/spring-beans.xsd
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg 
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding 
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave  
                           http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave/master-slave.xsd">
    
    <reg:etcd id="regCenter" server-lists="http://localhost:2379" />
    <sharding:data-source id="shardingMasterSlaveDataSource" registry-center-ref="regCenter" />
    <master-slave:data-source id="masterSlaveDataSource" registry-center-ref="regCenter" />
</beans>
```

## 配置项说明

### 分库分表

命名空间：http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd

#### \<sharding:data-source />

| *名称*         | *类型* | *说明*         |
| -------------- | ----- | -------------- |
| id             | 属性  | Spring Bean Id |
| sharding-rule  | 标签  | 数据分片配置规则 |
| props (?)      | 标签  | 属性配置        |
| config-map (?) | 标签  | 用户自定义配置   |

#### \<sharding:sharding-rule />

| *名称*                            | *类型* | *说明*                                                                    |
| --------------------------------- | ----- | ------------------------------------------------------------------------- |
| data-source-names                 | 属性  | 数据源Bean列表，多个Bean以逗号分隔                                            | 
| table-rules                       | 标签  | 表分片规则配置对象                                                           |
| binding-table-rules (?)           | 标签  | 绑定表规则列表                                                              |
| default-data-source-name (?)      | 属性  | 未配置分片规则的表将通过默认数据源定位                                         |
| default-database-strategy-ref (?) | 属性  | 默认数据库分片策略，对应\<sharding:xxx-strategy>中的策略Id，缺省表示不分库      |
| default-table-strategy-ref (?)    | 属性  | 默认表分片策略，对应\<sharding:xxx-strategy>中的策略Id，缺省表示不分表          |
| default-key-generator (?)         | 属性  | 默认自增列值生成器，缺省使用`io.shardingjdbc.core.keygen.DefaultKeyGenerator` |

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
| generate-key-column-name (?) | 属性  | 自增列名称，缺省表示不使用自增主键生成器                                                                                                                                                                         |
| key-generator (?)            | 属性  | 自增列值生成器，缺省表示使用默认自增列值生成器                                                                                                                                                                    |
| logic-index (?)              | 属性  | 逻辑索引名称，对于分表的Oracle/PostgreSQL数据库中DROP INDEX XXX语句，需要通过配置逻辑索引名称定位所执行SQL的真实分表                                                                                                   |

#### \<sharding:binding-table-rules />

| *名称*                 | *类型* | *说明*   |
| ---------------------- | ----- | -------- |
| binding-table-rule (+) | 标签  | 绑定表规则 |

#### \<sharding:binding-table-rule />

| *名称*       | *类型* | *说明*                         |
| ------------ | ----- | ----------------------------- |
| logic-tables | 属性  | 绑定规则的逻辑表名，多表以逗号分隔 |

#### \<sharding:standard-strategy />

| *名称*                   | *类型* | *说明*                     |
| ----------------------- | ------ | -------------------------- |
| id                      | 属性   | Spring Bean Id             |
| sharding-column         | 属性   | 分片列名称                  |
| precise-algorithm-ref   | 属性   | 精确分片算法引用，用于=和IN   |
| range-algorithm-ref (?) | 属性   | 范围分片算法引用，用于BETWEEN |

#### \<sharding:complex-strategy />

| *名称*           | *类型* | *说明*                   |
| --------------- | ------ | ------------------------ |
| id               | 属性  | Spring Bean Id           |
| sharding-columns | 属性  | 分片列名称，多个列以逗号分隔 |
| algorithm-ref    | 属性  | 复合分片算法引用           |

#### \<sharding:inline-strategy />

| *名称*               | *类型* | *说明*                          |
| -------------------- | ----- | ------------------------------ |
| id                   | 属性  | Spring Bean Id                  |
| sharding-column      | 属性  | 分片列名称                       |
| algorithm-expression | 属性  | 分片算法行表达式，需符合groovy语法 |

#### \<sharding:hint-database-strategy />

| *名称*        | *类型* | *说明*         |
| ------------- | ----- | ------------- |
| id            | 属性  | Spring Bean Id |
| algorithm-ref | 属性  | Hint分片算法    |

#### \<sharding:none-strategy />

| *名称* | *类型* | *说明*          |
| ----- | ------ | -------------- |
| id    | 属性    | Spring Bean Id |

#### \<sharding:props />

| *名称*            | *类型* | *说明*                      |
| ----------------- | ----- | --------------------------- |
| sql.show (?)      | 属性  | 是否开启SQL显示，默认值: false |
| executor.size (?) | 属性  | 工作线程数量，默认值: CPU核数  |

#### \<sharding:config-map />

### 读写分离

命名空间：http://shardingjdbc.io/schema/shardingjdbc/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *名称*                  | *类型* | *说明*                                                                       |
| ----------------------- | ----- | ---------------------------------------------------------------------------- |
| id                      | 属性  | Spring Bean Id                                                                |
| master-data-source-name | 属性  | 主库数据源Bean Id                                                              |
| slave-data-source-names | 属性  | 从库数据源Bean Id列表，多个Bean以逗号分隔                                        |
| strategy-ref (?)        | 属性  | 从库负载均衡算法引用。该类需实现MasterSlaveLoadBalanceAlgorithm接口               |
| strategy-type (?)       | 属性  | 从库负载均衡算法类型，可选值：ROUND_ROBIN，RANDOM。若`strategy-ref`存在则忽略该配置 |
| config-map (?)          | 标签  | 用户自定义配置                                                                 |

#### \<sharding:config-map />

### 数据分片 + 数据治理

命名空间：http://shardingjdbc.io/schema/shardingjdbc/orchestration/sharding/sharding.xsd

#### \<sharding:data-source />

| *名称*              | *类型* | *说明*                                                                 |
| ------------------- | ----- | ---------------------------------------------------------------------- |
| id                  | 属性  | 配置同数据分片                                                           |
| sharding-rule       | 标签  | 配置同数据分片                                                           |
| props (?)           | 标签  | 配置同数据分片                                                           |
| config-map (?)      | 标签  | 配置同数据分片                                                           |
| registry-center-ref | 属性  | 数据治理注册中心Bean引用                                                  |
| overwrite           | 属性  | 本地配置是否覆盖注册中心配置。如果可覆盖，每次启动都以本地配置为准。缺省为不覆盖 |

### 读写分离 + 数据治理

命名空间：http://shardingjdbc.io/schema/shardingjdbc/orchestration/masterslave/master-slave.xsd

#### \<master-slave:data-source />

| *名称*                  | *类型* | *说明*                 |
| ----------------------- | ----- | ---------------------- |
| id                      | 属性  | 配置同读写分离           |
| master-data-source-name | 属性  | 配置同读写分离           |
| slave-data-source-names | 属性  | 配置同读写分离           |
| strategy-ref (?)        | 属性  | 配置同读写分离           |
| strategy-type (?)       | 属性  | 配置同读写分离           |
| config-map (?)          | 标签  | 配置同读写分离           |
| registry-center-ref     | 属性  | 配置同数据分片 + 数据治理 |
| overwrite               | 属性  | 配置同数据分片 + 数据治理 |

### 数据治理注册中心

命名空间：http://shardingjdbc.io/schema/shardingjdbc/orchestration/reg/reg.xsd

#### \<reg:zookeeper />

| *名称*                              | *类型* | *说明*                                                                                |
| ----------------------------------- | ----- | ------------------------------------------------------------------------------------ |
| id                                  | 属性  | 注册中心的Spring Bean Id                                                               |
| server-lists                        | 属性  | 连接Zookeeper服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: host1:2181,host2:2181 |
| namespace                           | 属性  | Zookeeper的命名空间                                                                    |
| base-sleep-time-milliseconds (?)    | 属性  | 等待重试的间隔时间的初始毫秒数，默认1000毫秒                                               |
| max-sleep-time-milliseconds (?)     | 属性  | 等待重试的间隔时间的最大毫秒数，默认3000毫秒                                               |
| max-retries (?)                     | 属性  | 连接失败后的最大重试次数，默认3次                                                         |
| session-timeout-milliseconds (?)    | 属性  | 会话超时毫秒数，默认60000毫秒                                                            |
| connection-timeout-milliseconds (?) | 属性  | 连接超时毫秒数，默认15000毫秒                                                            |
| digest (?)                          | 属性  | 连接Zookeeper的权限令牌。缺省为不需要权限验证                                              |

#### \<reg:etcd />

| *名称*                          | *类型* | *说明*                                                                                         |
| ------------------------------- | ----- | --------------------------------------------------------------------------------------------- |
| id                              | 属性  | 注册中心的Spring Bean Id                                                                        |
| server-lists                    | 属性  | 连接Etcd服务器的列表。包括IP地址和端口号。多个地址用逗号分隔。如: http://host1:2379,http://host2:2379 |
| time-to-live-seconds (?)        | 属性  | 临时节点存活秒数，默认60秒                                                                        |
| timeout-milliseconds (?)        | 属性  | 请求超时毫秒数，默认500毫秒                                                                       |
| max-retries (?)                 | 属性  | 重试间隔毫秒数，默认200毫秒                                                                       |
| retry-interval-milliseconds (?) | 属性  | 请求失败后的最大重试次数，默认3次                                                                  |

### 柔性事务

#### SoftTransactionConfiguration配置

用于配置事务管理器。

| *名称*                              | *类型*                                     | *必填* | *默认值* | *说明*                                                                                     |
| ---------------------------------- | ------------------------------------------ | ------ | ------- | ----------------------------------------------------------------------------------------- |
| shardingDataSource                 | ShardingDataSource                         | 是     |         | 事务管理器管理的数据源                                                                       |
| syncMaxDeliveryTryTimes            | int                                        | 否     | 3       | 同步的事务送达的最大尝试次数                                                                  |
| storageType                        | enum                                       | 否     | RDB     | 事务日志存储类型。可选值: RDB,MEMORY。使用RDB类型将自动建表                                     |
| transactionLogDataSource           | DataSource                                 | 否     | null    | 存储事务日志的数据源，如果storageType为RDB则必填                                               |
| bestEffortsDeliveryJobConfiguration| NestedBestEffortsDeliveryJobConfiguration  | 否     | null    | 最大努力送达型内嵌异步作业配置对象。如需使用，请参考NestedBestEffortsDeliveryJobConfiguration配置 |

#### NestedBestEffortsDeliveryJobConfiguration配置 (仅开发环境)

用于配置内嵌的异步作业，仅用于开发环境。生产环境应使用独立部署的作业版本。

| *名称*                              | *类型*                      | *必填* | *默认值*                   | *说明*                                                           |
| ---------------------------------- | --------------------------- | ------ | ------------------------- | --------------------------------------------------------------- |
| zookeeperPort                      | int                         | 否     | 4181                      | 内嵌的注册中心端口号                                               |
| zookeeperDataDir                   | String                      | 否     | target/test_zk_data/nano/ | 内嵌的注册中心的数据存放目录                                        |
| asyncMaxDeliveryTryTimes           | int                         | 否     | 3                         | 异步的事务送达的最大尝试次数                                        |
| asyncMaxDeliveryTryDelayMillis     | long                        | 否     | 60000                     | 执行异步送达事务的延迟毫秒数，早于此间隔时间的入库事务才会被异步作业执行  |
