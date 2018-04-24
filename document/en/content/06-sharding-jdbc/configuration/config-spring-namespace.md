+++
toc = true
title = "Spring namespace"
weight = 4
+++

## Spring namespace configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core-spring-namespace</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding

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
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa"/>
    
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
    <tx:annotation-driven/>
    
    <bean id="demo_ds_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="demo_ds_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseShardingStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm"/>
    <sharding:standard-strategy id="tableShardingStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm"/>
    
    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="demo_ds_0, demo_ds_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_${0..1}.t_order_${0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseShardingStrategy" table-strategy-ref="tableShardingStrategy" generate-key-column-name="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>
</beans>
```
##### Introduction for labels

##### Sharding

##### \<sharding:data-source/\>

| *Name*                         | *Type*       | *DataType*  | *Required* |    *Info*       |
| -----------------------------  | ------------ |  ---------  | ---------- | --------------  |
| id                             | Property     |  String     |   Y        | Spring Bean ID  |
| sharding-rule                  | Label        |   -         |   Y        | Sharding Rule   |
| config-map?           | Label        |   -         |   N        | Blinding config map   |
| props?                         | Label        |   -         |   N        | Property Config |

##### \<sharding:sharding-rule/>

| *Name*                        | *Type*      | *DataType*  | *Required* | *Info*                                                                |
| ----------------------------- | ----------- | ----------  | ------     | --------------------------------------------------------------------- |
| data-source-names             | Property    | String      |   Y        | The bean list of data sources, all the BEAN IDs of data sources (including the default data source) needed to be managed by Sharding-JDBC must be configured. Multiple bean IDs are separated by commas.|
| default-data-source-name?      | Property    | String      |   N        | The default name for data source. Tables without sharding rules will be considered in this data source.                        |
| default-database-strategy-ref? | Property    | String      |   N        | The default strategy for sharding databases, which is also the strategy ID in \<sharding:xxx-strategy>. If this property is not set, the strategy of none sharding will be applied.|
| default-table-strategy-ref?   | Property    | String      |   N        | The default strategy for sharding tables which is also the strategy ID in \<sharding:xxx-strategy>. If this property is not set, the strategy of none sharding will be applied. |
| default-key-generator? | Property | String |N|The bean reference of default key generator|
| table-rules                   | Label       |   -         |   Y        | The list of table rules.                                             |
|binding-table-rules?           | Label         | -      | N| Blinding Rule|

##### \<sharding:table-rules/>

| *Name*                        | *Type*      | *DataType* |  *Required* | *Info*  |
| ----------------------------- | ----------- | ---------- | ----------- | ------- |
| table-rule+                   | Label       |   -        |   Y         | sharding rules |


##### \<sharding:table-rule/>

| *Name*                | *Type*       | *DataType* | *Required* | *Info*  |
| --------------------- | ------------ | ---------- | ------     | ------- |
| logic-table           | Property     |  String    |   Y        | LogicTables |
| actual-data-nodes?     | Property     |  String    |   N        | Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs separated with commas, supporting the inline expression. The default value is composed of configured data sources and logic table. This default config is to generate broadcast table (*The same table existed in every DB for cascade query.*) or to split the database without splitting the table.|
| database-strategy-ref? | Property     |  String    |   N        | The strategy for sharding database.Its strategy ID is in \<sharding:xxx-strategy>. The default is default-database-strategy-ref configured in \<sharding:sharding-rule/>    |
| table-strategy-ref?    | Property     |  String    |   N        | The strategy for sharding table. Its strategy ID is in \<sharding:xxx-strategy>. The default is default-table-strategy-ref in \<sharding:sharding-rule/>       |
| logic-index?           | Property     |  String    |   N        | The Logic index name. If you want to use *DROP INDEX XXX* SQL in Oracle/PostgreSQL，This property needs to be set for finding the actual tables.   |
| generate-key-column-name？ | Property| String | N | The generate column|
| key-generator？ | Property | String | N| The bean reference of key generator|


##### \<sharding:binding-table-rules/>

| *Name*                        | *Type*      | *DataType* |  *Required* | *Info*  |
| ----------------------------- | ----------- |  --------- | ------      | ------- |
| binding-table-rule+           | Label       |   -        |   Y         | The rule for binding tables. |

##### \<sharding:binding-table-rule/>

| *Name*                        | *Type*       | *DataType* |  *Required* | *Info*                   |
| ----------------------------- | ------------ | ---------- | ------      | ------------------------ |
| logic-tables                  | Property     |  String    |   Y         | The name of Logic tables, multiple tables are separated by commas.|

##### \<sharding:standard-strategy/>

The standard sharding strategy for single sharding column.

| *Name*                        | *Type*       | *DataType* |  *Required* | *Info*                                                                |
| ----------------------------- | ------------ | ---------- | ------      | --------------------------------------------------------------------- |
| sharding-column               | Property     |  String    |   Y         | The name of sharding column.                                                       |
| precise-algorithm-ref         | Property     |  String    |   Y         | The bean id for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed.   |
| range-algorithm-ref           | Property     |  String    |   N         | The bean id for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed. |


##### \<sharding:complex-strategy/>

The complex sharding strategy for multiple sharding columns.

| *Name*                        | *Type*       | *DataType*  |  *Required* | *Info*                                              |
| ----------------------------- | ------------ | ----------  | ------      | --------------------------------------------------- |
| sharding-columns              | Property     |  String     |   Y         | The name of sharding column. Multiple names separated with commas.                              |
| algorithm-ref                 | Property     |  String     |   Y         | The bean id for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

##### \<sharding:inline-strategy/>

The inline-expression sharding strategy.

| *Name*                        | *Type*       | *DataType* |  *Required* | *Info*       |
| ----------------------------- | ------------ | ---------- | ------      | ------------ |
| sharding-column               | Property     |  String    |   Y         | The  name of sharding column.      |
| algorithm-expression          | Property     |  String    |   Y         | The expression for sharding algorithm. |

##### \<sharding:hint-database-strategy/>

The Hint-method sharding strategy.

| *Name*                        | *Type*       | *DataType* |  *Required* | *Info*                                              |
| ----------------------------- | ------------ | ---------- | ------      | --------------------------------------------------- |
| algorithm-ref                 | Property     |  String    |   Y         | The bean id for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

##### \<sharding:none-strategy/>

The none sharding strategy.

| *Name*                        | *Type*       | *DataType* |  *Required* | *Info*                                              |
| ----------------------------- | ------------ | ---------- | ------ | --------------------------------------------------- |
| id                            | 属性         |  String     |   Y   | Spring Bean ID |

##### \<sharding:props/\>

| *Name*                               | *Type*       | *DataType* | *Required* | *Info*                              |
| ------------------------------------ | ------------ | ---------- | -----      | ----------------------------------- |
| sql.show                             | Property     |  boolean   |   Y        | To show SQLS or not, the default is false.     |
| executor.size                        | Property     |  int       |   N        | The number of running threads.                      |

##### \<sharding:config-map/\>

##### Read-write splitting

##### \<master-slave:data-source/\>

Define datasorce for Reading-writing spliting.

| *Name*                        | *Type*       | *DataType* |  *Required* | *Info*                                   |
| ----------------------------- | ------------ |  --------- | ------      | ---------------------------------------- |
| id                            | Property     |  String    |   Y         | The spring Bean ID                           |
| master-data-source-name       | Property        |   -        |   Y         | The Bean ID of Master database.                         |
| slave-data-source-names       | Property        |   -        |   Y         | The list of Slave databases, multiple items are separated by commas.  |
| strategy-ref?                 | Property        |   -        |   N         | The Bean ID for complex strategy of Master-Slaves. User-defined complex strategy is allowed.|
| strategy-type?                | Property        |  String    |   N         | The complex strategy type of Master-Slaves. <br />The options: ROUND_ROBIN, RANDOM<br />. The default: ROUND_ROBIN |
| config-map?                   | Label         |   -         |   N   |         config map|

##### \<sharding:config-map/\>

##### More details on Spring Configuration

To use inline expression, please configure *ignore-unresolvable* to be true, otherwise placeholder will treat the inline expression as an attribute key and then errors arises.

##### The description of sharding algorithm expression syntax

##### The details on inline expression
${begin..end} # indicate the number range.

${[unit1, unit2, unitX]} # indicate enumeration values

consecutive ${...} in inline expression # The Cartesian product among all the ${...} will be the final expression result, for example: 

An inline expression:

```groovy
dbtbl_${['online', 'offline']}_${1..3}
```

The final expression result:

dbtbl_online_1，dbtbl_online_2，dbtbl_online_3，dbtbl_offline_1，dbtbl_offline_2和dbtbl_offline_3.

##### The groovy code in strings
By using ${}, we can embed groovy code in strings to generate the final expression, for example:

```groovy 
data_source_${id % 2 + 1}
```
data_source_ is the prefix and id % 2 + 1 is groovy code in this example.

#### Read-write splitting

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
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa"/>
    
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
    <tx:annotation-driven/>
    
    <bean id="demo_ds_master" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="demo_ds_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="demo_ds_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>
    
    <bean id="randomStrategy" class="io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />
    
    <master-slave:data-source id="masterSlaveDataSource" master-data-source-name="demo_ds_master" slave-data-source-names="demo_ds_slave_0, demo_ds_slave_1" strategy-ref="randomStrategy" />
</beans>
```

##### Hint usage

```java
HintManager hintManager = HintManager.getInstance();
hintManager.setMasterRouteOnly();
// other codes
```

#### Sharding + Read-write splitting

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
    <context:component-scan base-package="io.shardingjdbc.example.spring.namespace.jpa"/>
    
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
    <tx:annotation-driven/>
    
    <bean id="demo_ds_master_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_0_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_0_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_0_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_0_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_1_slave_0" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_1_slave_0"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="demo_ds_master_1_slave_1" class="org.apache.commons.dbcp.BasicDataSource" destroy-method="close">
        <property name="driverClassName" value="com.mysql.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3306/demo_ds_master_1_slave_1"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </bean>

    <bean id="randomStrategy" class="io.shardingjdbc.core.api.algorithm.masterslave.RandomMasterSlaveLoadBalanceAlgorithm" />

    <master-slave:data-source id="demo_ds_ms_0" master-data-source-name="demo_ds_master_0" slave-data-source-names="demo_ds_master_0_slave_0, demo_ds_master_0_slave_1" strategy-ref="randomStrategy" />
    <master-slave:data-source id="demo_ds_ms_1" master-data-source-name="demo_ds_master_1" slave-data-source-names="demo_ds_master_1_slave_0, demo_ds_master_1_slave_1" strategy-ref="randomStrategy" />

    <sharding:inline-strategy id="databaseStrategy" sharding-column="user_id" algorithm-expression="demo_ds_ms_${user_id % 2}" />
    <sharding:inline-strategy id="orderTableStrategy" sharding-column="order_id" algorithm-expression="t_order_${order_id % 2}" />
    <sharding:inline-strategy id="orderItemTableStrategy" sharding-column="order_id" algorithm-expression="t_order_item_${order_id % 2}" />

    <sharding:data-source id="shardingDataSource">
        <sharding:sharding-rule data-source-names="demo_ds_ms_0,demo_ds_ms_1">
            <sharding:table-rules>
                <sharding:table-rule logic-table="t_order" actual-data-nodes="demo_ds_ms_${0..1}.t_order_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderTableStrategy" generate-key-column="order_id" />
                <sharding:table-rule logic-table="t_order_item" actual-data-nodes="demo_ds_ms_${0..1}.t_order_item_${0..1}" database-strategy-ref="databaseStrategy" table-strategy-ref="orderItemTableStrategy" generate-key-column="order_item_id" />
            </sharding:table-rules>
        </sharding:sharding-rule>
    </sharding:data-source>

</beans>

```

#### Orchestration

##### Zookeeper
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
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
    
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
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

##### Introduction for config items of Zookeeper

##### \<reg:zookeeper/>

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是     |       | 注册中心在Spring容器中的主键                                                                         |
| server-lists                    | String | 是     |       | 连接Zookeeper服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: host1:2181,host2:2181 |
| namespace                       | String | 是     |       | Zookeeper的命名空间                                                                                |
| base-sleep-time-milliseconds    | int    | 否     | 1000  | 等待重试的间隔时间的初始值<br />单位：毫秒                                                               |
| max-sleep-time-milliseconds     | int    | 否     | 3000  | 等待重试的间隔时间的最大值<br />单位：毫秒                                                               |
| max-retries                     | int    | 否     | 3     | 最大重试次数                                                                                          |
| session-timeout-milliseconds    | int    | 否     | 60000 | 会话超时时间<br />单位：毫秒                                                                           |
| connection-timeout-milliseconds | int    | 否     | 15000 | 连接超时时间<br />单位：毫秒                                                                           |
| digest                          | String | 否     |       | 连接Zookeeper的权限令牌<br />缺省为不需要权限验证                                                      |

##### Etcd
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
    
    <bean id="preciseModuloDatabaseShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloDatabaseShardingAlgorithm" />
    <bean id="preciseModuloTableShardingAlgorithm" class="io.shardingjdbc.example.spring.namespace.jpa.algorithm.PreciseModuloTableShardingAlgorithm" />
        
    <sharding:standard-strategy id="databaseStrategy" sharding-column="user_id" precise-algorithm-ref="preciseModuloDatabaseShardingAlgorithm" />
    <sharding:standard-strategy id="tableStrategy" sharding-column="order_id" precise-algorithm-ref="preciseModuloTableShardingAlgorithm" />
    
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

##### Introduction for config items of Etcd

##### \<reg:etcd/>

| 属性名                           | 类型   | 是否必填 | 缺省值 | 描述                                                                                               |
| ------------------------------- |:-------|:-------|:------|:---------------------------------------------------------------------------------------------------|
| id                              | String | 是     |       | 注册中心在Spring容器中的主键                                                                           |
| server-lists                    | String | 是     |       | 连接Etcd服务器的列表<br />包括IP地址和端口号<br />多个地址用逗号分隔<br />如: http://host1:2379,http://host2:2379 |
| time-to-live-seconds            | int    | 否     | 60    | 临时节点存活时间<br />单位：秒                                                                         |
| timeout-milliseconds            | int    | 否     | 500   | 每次请求的超时时间<br />单位：毫秒                                                                      |
| max-retries                     | int    | 否     | 3     | 每次请求的最大重试次数                                                                                 |
| retry-interval-milliseconds     | int    | 否     | 200   | 重试间隔时间<br />单位：毫秒                                                                           |


#### B.A.S.E
##### The configuration of transaction manager 

##### SoftTransactionConfiguration Configuration

For configuring transaction manager.

| *Name*                              | *Type*                                     | *Required* | *Default*   | *Info*                                                                                       |
| ---------------------------------- | ------------------------------------------ | ------ | --------- | ------------------------------------------------------------------------------------------- |
| shardingDataSource                 | ShardingDataSource                         | Y     |           | The data source of transaction manager                                                                         |
| syncMaxDeliveryTryTimes            | int                                        | N     | 3         | The maximum number of attempts to send transactions.                                                                 |
| storageType                        | enum                                       | N     | RDB       | The storage type of transaction logs, The options are RDB(creating tables automatically) or MEMORY.                                       |
| transactionLogDataSource           | DataSource                                 | N     | null      | The data source to store the transaction log. if storageType is RDB, this item is required.                                              |
| bestEffortsDeliveryJobConfiguration| NestedBestEffortsDeliveryJobConfiguration  | N     | null      | The config of embedded asynchronous jobs for the Best-Effort-Delivery transaction, please refer to NestedBestEffortsDeliveryJobConfiguration.|

##### NestedBestEffortsDeliveryJobConfiguration Configuration (Only for developing environment)

It is for configuring embedded asynchronous jobs for development environment only. The production environment should adopt the deployed discrete jobs.

| *Name*                              | *Type*                                     | *Required* | *Default*   | *Info*                                                            |
| ---------------------------------- | --------------------------- | ------ | ------------------------ | --------------------------------------------------------------- |
| zookeeperPort                      | int                         | N     | 4181                     | The port of the embedded registry.                                               |
| zookeeperDataDir                   | String                      | N     | target/test_zk_data/nano/| The data directory of the embedded registry.                                      |
| asyncMaxDeliveryTryTimes           | int                         | N     | 3                        | The maximum number of attempts to send transactions asynchronously.                                       |
| asyncMaxDeliveryTryDelayMillis     | long                        | N     | 60000                    | The number of delayed milliseconds to execute asynchronous transactions. The transactions whose creating time earlier than this value will be executed by asynchronous jobs.  |
