+++
toc = true
title = "Spring namespace configuration"
weight = 4
prev = "/02-sharding/configuration/config-yaml/"
next = "/02-sharding/configuration/config-spring-boot/"
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
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
    xmlns:context="http://www.springframework.org/schema/context"
    xmlns:sharding="http://shardingjdbc.io/schema/shardingjdbc/sharding" 
    xsi:schemaLocation="http://www.springframework.org/schema/beans 
                        http://www.springframework.org/schema/beans/spring-beans.xsd
                        http://www.springframework.org/schema/context 
                        http://www.springframework.org/schema/context/spring-context.xsd 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding 
                        http://shardingjdbc.io/schema/shardingjdbc/sharding/sharding.xsd 
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
    
    <sharding:data-source id="shardingDataSource">
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
</beans>
```
### Introduction for labels

#### \<sharding:data-source/\>

To define the data source for sharding-jdbc 

| *Name*               | *Type*   | *DataType* | *Required* | *Info*          |
| -------------------- | -------- | ---------- | ---------- | --------------- |
| id                   | Property | String     | Y          | Spring Bean ID  |
| sharding-rule        | Label    | -          | Y          | Sharding Rule   |
| binding-table-rules? | Label    | -          | N          | Blinding Rule   |
| props?               | Label    | -          | N          | Property Config |

#### \<sharding:sharding-rule/>

| *Name*                        | *Type*   | *DataType* | *Required* | *Info*                                   |
| ----------------------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| data-source-names             | Property | String     | Y          | The bean list of data sources, all the BEAN IDs of data sources (including the default data source) needed to be managed by Sharding-JDBC must be configured. Multiple bean IDs are separated by commas. |
| default-data-source-name      | Property | String     | N          | The default name for data source. Tables without sharding rules will be considered in this data source. |
| default-database-strategy-ref | Property | String     | N          | The default strategy for sharding databases, which is also the strategy ID in \<sharding:xxx-strategy>. If this property is not set, the strategy of none sharding will be applied. |
| default-table-strategy-ref    | Property | String     | N          | The default strategy for sharding tables which is also the strategy ID in \<sharding:xxx-strategy>. If this property is not set, the strategy of none sharding will be applied. |
| table-rules                   | Label    | -          | Y          | The list of sharding rules.              |

#### \<sharding:table-rules/>

| *Name*      | *Type* | *DataType* | *Required* | *Info*         |
| ----------- | ------ | ---------- | ---------- | -------------- |
| table-rule+ | Label  | -          | Y          | sharding rules |

#### \<sharding:table-rule/>

| *Name*                | *Type*   | *DataType* | *Required* | *Info*                                   |
| --------------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| logic-table           | Property | String     | Y          | LogicTables                              |
| actual-data-nodes     | Property | String     | N          | Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs separated with commas, supporting the inline expression. The default value is composed of configured data sources and logic table. This default config is to generate broadcast table (*The same table existed in every DB for cascade query.*) or to split the database without splitting the table. |
| database-strategy-ref | Property | String     | N          | The strategy for sharding database.Its strategy ID is in \<sharding:xxx-strategy>. The default is default-database-strategy-ref configured in \<sharding:sharding-rule/> |
| table-strategy-ref    | Property | String     | N          | The strategy for sharding table. Its strategy ID is in \<sharding:xxx-strategy>. The default is default-table-strategy-ref in \<sharding:sharding-rule/> |
| logic-index           | Property | String     | N          | The Logic index name. If you want to use *DROP INDEX XXX* SQL in Oracle/PostgreSQL，This property needs to be set for finding the actual tables. |

#### \<sharding:binding-table-rules/>

| *Name*             | *Type* | *DataType* | *Required* | *Info*                       |
| ------------------ | ------ | ---------- | ---------- | ---------------------------- |
| binding-table-rule | Label  | -          | Y          | The rule for binding tables. |

#### \<sharding:binding-table-rule/>

| *Name*       | *Type*   | *DataType* | *Required* | *Info*                                   |
| ------------ | -------- | ---------- | ---------- | ---------------------------------------- |
| logic-tables | Property | String     | Y          | The name of Logic tables, multiple tables are separated by commas. |

#### \<sharding:standard-strategy/>

The standard sharding strategy for single sharding column.

| *Name*                  | *Type*   | *DataType* | *Required* | *Info*                                   |
| ----------------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| sharding-column         | Property | String     | Y          | The name of sharding column.             |
| precise-algorithm-class | Property | String     | Y          | The class name for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed. |
| range-algorithm-class   | Property | String     | N          | The class name for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed. |

#### \<sharding:complex-strategy/>

The complex sharding strategy for multiple sharding columns.

| *Name*           | *Type*   | *DataType* | *Required* | *Info*                                   |
| ---------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| sharding-columns | Property | String     | Y          | The name of sharding column. Multiple names separated with commas. |
| algorithm-class  | Property | String     | Y          | # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

#### \<sharding:inline-strategy/>

The inline-expression sharding strategy.

| *Name*               | *Type*   | *DataType* | *Required* | *Info*                                 |
| -------------------- | -------- | ---------- | ---------- | -------------------------------------- |
| sharding-column      | Property | String     | Y          | the  name of sharding column.          |
| algorithm-expression | Property | String     | Y          | The expression for sharding algorithm. |

#### \<sharding:hint-database-strategy/>

The Hint-method sharding strategy.

| *Name*          | *Type*   | *DataType* | *Required* | *Info*                                   |
| --------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| algorithm-class | Property | String     | Y          | The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed. |

#### \<sharding:none-strategy/>

The none sharding strategy.

#### \<sharding:props/\>

| *Name*        | *Type*   | *DataType* | *Required* | *Info*                                   |
| ------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| sql.show      | Property | boolean    | Y          | To show SQLS or not, the default is false. |
| executor.size | Property | int        | N          | The number of running threads.           |

#### \<master-slave:data-source/\>

Define datasorce for Reading-writing spliting.

| *Name*                  | *Type*   | *DataType* | *Required* | *Info*                                   |
| ----------------------- | -------- | ---------- | ---------- | ---------------------------------------- |
| id                      | Property | String     | Y          | The spring Bean ID                       |
| master-data-source-name | Label    | -          | Y          | The Bean ID of Master database.          |
| slave-data-source-names | Label    | -          | Y          | The list of Slave databases, multiple items are separated by commas. |
| strategy-ref?           | Label    | -          | N          | The Bean ID for complex strategy of Master-Slaves. User-defined complex strategy is allowed. |
| strategy-type?          | Label    | String     | N          | The complex strategy type of Master-Slaves. <br />The options: ROUND_ROBIN, RANDOM<br />. The default: ROUND_ROBIN |

#### More details on Spring Configuration

To use inline expression, please configure *ignore-unresolvable* to be true, otherwise placeholder will treat the inline expression as an attribute key and then errors arises.

## The description of sharding algorithm expression syntax

### The details on inline expression
${begin..end} # indicate the number range.

${[unit1, unit2, unitX]} # indicate enumeration values

consecutive ${...} in inline expression # The Cartesian product among all the ${...} will be the final expression result, for example: 

An inline expression:

```groovy
dbtbl_${['online', 'offline']}_${1..3}
```

The final expression result:

dbtbl_online_1，dbtbl_online_2，dbtbl_online_3，dbtbl_offline_1，dbtbl_offline_2和dbtbl_offline_3.

### The groovy code in strings
By using ${}, we can embed groovy code in strings to generate the final expression, for example:

```groovy 
data_source_${id % 2 + 1}
```
data_source_ is the prefix and id % 2 + 1 is groovy code in this example.
