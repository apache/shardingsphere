+++
toc = true
title = "Configuration"
weight = 4
prev = "/02-sharding/scenario/"
next = "/02-sharding/sql-supported/"
+++

This section explains configuration domain models in Sharding-JDBC. The following class diagram is about those domain models in Sharding-JDBC.

![The class diagrams for Domain Model](http://ovfotjrsi.bkt.clouddn.com/docs/img/config_domain.png)

## The Factory Method Pattern

The yellow part of the figure represents the Sharding-JDBC entry API, which is provided in the form of factory methods. It includes ShardingDataSourceFactory factory class and MasterSlaveDataSourceFactory factory class. ShardingDataSourceFactory is used to create JDBC driver for Sharding + Read-write splitting, but MasterSlaveDataSourceFactory is to create JDBC driver only for Read-write splitting.

## Configuration Object

The blue part of the figure shows the sharding-jdbc configuration objects. ShardingRuleConfiguration is the entrance to configure the Sharding strategy, and it can include multiple TableRuleConfiguration and MasterSlaveRuleConfiguration. A TableRuleConfiguration is configured for a group of tables with the same sharding strategy. If both Sharding and Read-write splitting are used, you need to set MasterSlaveRuleConfiguration for each logic database used to do Read-write splitting. There is one-to-one correspondence between each TableRuleConfiguration and each ShardingStrategyConfiguration which consists of 5 kinds of strategies to choose from. For details on the use of sharding strategies, please read the [Database Sharding] (/02-guide/sharding/).

MasterSlaveRuleConfiguration is only used for Read-write splitting.

## Internal Object

The red part of the figure represents internal objects, which are used by Sharding-JDBC itself. Therefore users do not look inside those objects. By using ShardingRuleConfiguration and MasterSlaveRuleConfiguration, Sharding-JDBC provides final rules to ShardingDataSource and MasterSlaveDataSource which implement the DataSource interface.

## Operation Steps

1. Create Configuration object.
2. The Configuration object is transformed into the Rule object through the Factory object.
3. The Rule object is bound to the DataSource object through the Factory object.
4. Sharding-JDBC operates Sharding and Read-write splitting on DataSource object.

This section explains configuration domain models in Sharding-JDBC. The following class diagram is about those domain models in Sharding-JDBC.

![The class diagrams for Domain Model](http://ovfotjrsi.bkt.clouddn.com/docs/img/config_domain.png)

## The Factory Method Pattern

The yellow part of the figure represents the Sharding-JDBC entry API, which is provided in the form of factory methods. It includes ShardingDataSourceFactory factory class and MasterSlaveDataSourceFactory factory class. ShardingDataSourceFactory is used to create JDBC driver for Sharding + Read-write splitting, but MasterSlaveDataSourceFactory is to create JDBC driver only for Read-write splitting.

## Configuration Object

The blue part of the figure shows the sharding-jdbc configuration objects. ShardingRuleConfiguration is the entrance to configure the Sharding strategy, and it can include multiple TableRuleConfiguration and MasterSlaveRuleConfiguration. A TableRuleConfiguration is configured for a group of tables with the same sharding strategy. If both Sharding and Read-write splitting are used, you need to set MasterSlaveRuleConfiguration for each logic database used to do Read-write splitting. There is one-to-one correspondence between each TableRuleConfiguration and each ShardingStrategyConfiguration which consists of 5 kinds of strategies to choose from. For details on the use of sharding strategies, please read the [Database Sharding] (/02-guide/sharding/).

MasterSlaveRuleConfiguration is only used for Read-write splitting.

## Internal Object

The red part of the figure represents internal objects, which are used by Sharding-JDBC itself. Therefore users do not look inside those objects. By using ShardingRuleConfiguration and MasterSlaveRuleConfiguration, Sharding-JDBC provides final rules to ShardingDataSource and MasterSlaveDataSource which implement the DataSource interface.

## Operation Steps

1. Create Configuration object.
2. The Configuration object is transformed into the Rule object through the Factory object.
3. The Rule object is bound to the DataSource object through the Factory object.
4. Sharding-JDBC operates Sharding and Read-write splitting on DataSource object.

## 1.JAVA Configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding Configuration
```java
     DataSource getShardingDataSource() throws SQLException {
         ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
         shardingRuleConfig.getTableRuleConfigs().add(getOrderTableRuleConfiguration());
         shardingRuleConfig.getTableRuleConfigs().add(getOrderItemTableRuleConfiguration());
         shardingRuleConfig.getBindingTableGroups().add("t_order, t_order_item");
         shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new InlineShardingStrategyConfiguration("user_id", "demo_ds_${user_id % 2}"));
         shardingRuleConfig.setDefaultTableShardingStrategyConfig(new StandardShardingStrategyConfiguration("order_id", ModuloShardingTableAlgorithm.class.getName()));
         return ShardingDataSourceFactory.createDataSource(createDataSourceMap(), shardingRuleConfig);
     }
     
     TableRuleConfiguration getOrderTableRuleConfiguration() {
         TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
         orderTableRuleConfig.setLogicTable("t_order");
         orderTableRuleConfig.setActualDataNodes("demo_ds_${0..1}.t_order_${0..1}");
         orderTableRuleConfig.setKeyGeneratorColumnName("order_id");
         return orderTableRuleConfig;
     }
     
     TableRuleConfiguration getOrderItemTableRuleConfiguration() {
         TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
         orderItemTableRuleConfig.setLogicTable("t_order_item");
         orderItemTableRuleConfig.setActualDataNodes("demo_ds_${0..1}.t_order_item_${0..1}");
         return orderItemTableRuleConfig;
     }
     
     Map<String, DataSource> createDataSourceMap() {
         Map<String, DataSource> result = new HashMap<>(2, 1);
         result.put("demo_ds_0", DataSourceUtil.createDataSource("demo_ds_0"));
         result.put("demo_ds_1", DataSourceUtil.createDataSource("demo_ds_1"));
         return result;
     }
```

#### Read-write splitting Configuration
```java
     DataSource getMasterSlaveDataSource() throws SQLException {
         MasterSlaveRuleConfiguration masterSlaveRuleConfig = new MasterSlaveRuleConfiguration();
         masterSlaveRuleConfig.setName("demo_ds_master_slave");
         masterSlaveRuleConfig.setMasterDataSourceName("demo_ds_master");
         masterSlaveRuleConfig.setSlaveDataSourceNames(Arrays.asList("demo_ds_slave_0", "demo_ds_slave_1"));
         return MasterSlaveDataSourceFactory.createDataSource(createDataSourceMap(), masterSlaveRuleConfig);
     }
     
     Map<String, DataSource> createDataSourceMap() {
         final Map<String, DataSource> result = new HashMap<>(3, 1);
         result.put("demo_ds_master", DataSourceUtil.createDataSource("demo_ds_master"));
         result.put("demo_ds_slave_0", DataSourceUtil.createDataSource("demo_ds_slave_0"));
         result.put("demo_ds_slave_1", DataSourceUtil.createDataSource("demo_ds_slave_1"));
         return result;
     }
```

## 2.YAML Configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding Configuration
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
  # t_order and t_order are all bindingTables of each other because of their same sharding strategies.
  bindingTables:
    - t_order,t_order
  # The default sharding strategy
  defaultDatabaseStrategy:
    none:
  defaultTableStrategy:
    complex:
      shardingColumns: id, order_id
      algorithmClassName: io.shardingjdbc.core.yaml.fixture.MultiAlgorithm
  props:
    sql.show: true
```

#### The config items for Sharding

```yaml
dataSources: # Config for data source
  <data_source_name> # Config for DB connection pool class. One or many configs are ok.
    driverClassName: # Class name for database driver.
    url: # The url for database connection.
    username: # Username used to access DB.
    password: # Password used to access DB.
    ... # Other configs for connection pool.

defaultDataSourceName: # Default datasource. Notice: Tables without sharding rules are accessed by using the default data source.

tables: # The config for sharding, One or many configs for logic_table_name are ok.
    <logic_table_name>: # Table name for LogicTables
        actualDataNodes: # Actual data nodes configured in the format of *datasource_name.table_name*, multiple configs spliced with commas, supporting the inline expression. The default value is composed of configured datasources and logic table. This default config is to generate broadcast table (*The same table existed in every DB for cascade query*) or to split databases without spliting tables.
        databaseStrategy: # Strategy for sharding databases, only one strategy can be chosen from following strategies:
            standard: # Standard sharding strategy for single sharding column.
                shardingColumn: # Sharding Column
                preciseAlgorithmClassName: # The class name for precise-sharding-algorithm used for = and IN. The default constructor or on-parametric constructor is needed.
                rangeAlgorithmClassName: # (Optional) The class name for range-sharding-algorithm used for BETWEEN. The default constructor or on-parametric constructor is needed.
            complex: # Complex sharding strategy for multiple sharding columns.
                shardingColumns : # Sharding Column, multiple sharding columns spliced with commas. 
                algorithmClassName: # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.
            inline: inline # Inline sharding strategy.
                shardingColumn : # Sharding Column
                algorithmInlineExpression: #  The inline expression conformed to groovy dynamic syntax for sharding. 
            hint: # Hint sharding strategy
                algorithmClassName: # The class name for sharding-algorithm. The default constructor or on-parametric constructor is needed.
            none: # No sharding
        tableStrategy: # Strategy for sharding tables. The details is same as Strategy for sharding databases.
  bindingTables: # Config for Blinding tables
  - A list of logic_table_name, multiple logic_table_names spliced with commas.
  
defaultDatabaseStrategy: # Default strategy for sharding databases. The details is same as databaseStrategy.
 
defaultTableStrategy: # Default strategy for sharding databases. The details is same as tableStrategy.

props: Property Configuration (Optional)
    sql.show: # To show SQL or not. Default: false
    executor.size: # The number of running thread. Default: The number of CPU cores.
```

#### The construction method for data source of Sharding

```java
    DataSource dataSource = ShardingDataSourceFactory.createDataSource(yamlFile);
```

#### Read-write splitting Configuration
```yaml
dataSources:
  db_master: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_master;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_slave_0;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100
  db_slave_1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: org.h2.Driver
    url: jdbc:h2:mem:db_slave_1;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false;MODE=MYSQL
    username: sa
    password: 
    maxActive: 100

masterSlaveRule:
  name: db_ms

  masterDataSourceName: db_master

  slaveDataSourceNames: [db_slave_0, db_slave_1]
```

#### The config items for Read-write splitting

```yaml
dataSource: # Config for data sourc same as previous dataSource.

name: # Data source name for sharding.

masterDataSourceName: Datasource name for Master datasource

slaveDataSourceNames：Datasource name for Slave datasource, multiple datasource put in an Array.
```

#### The construction method for data source of Read-write splitting

```java
    DataSource dataSource = MasterSlaveDataSourceFactory.createDataSource(yamlFile);
```

### More detail on YAML Configuration
!! :implementation class.

[] :multiple items.

## 3. The Configuration for Spring namespace

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

## 4.Spring Boot Configuration

### Import the dependency of maven

```xml
<dependency>
    <groupId>io.shardingjdbc</groupId>
    <artifactId>sharding-jdbc-core-spring-boot-starter</artifactId>
    <version>${latest.release.version}</version>
</dependency>
```

### Configuration Example

#### Sharding Configuration
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
```

#### The details on some Config options for Sharding 
Refer to [The Yaml Configuration for Sharding](#The config items for Sharding)

#### Read-write splitting Configuration
```yaml
sharding.jdbc.datasource.names=ds_master,ds_slave_0,ds_slave_1

sharding.jdbc.datasource.ds_master.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_master.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_master.url=jdbc:mysql://localhost:3306/demo_ds_master
sharding.jdbc.datasource.ds_master.username=root
sharding.jdbc.datasource.ds_master.password=

sharding.jdbc.datasource.ds_slave_0.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_0.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_0.url=jdbc:mysql://localhost:3306/demo_ds_slave_0
sharding.jdbc.datasource.ds_slave_0.username=root
sharding.jdbc.datasource.ds_slave_0.password=

sharding.jdbc.datasource.ds_slave_1.type=org.apache.commons.dbcp.BasicDataSource
sharding.jdbc.datasource.ds_slave_1.driverClassName=com.mysql.jdbc.Driver
sharding.jdbc.datasource.ds_slave_1.url=jdbc:mysql://localhost:3306/demo_ds_slave_1
sharding.jdbc.datasource.ds_slave_1.username=root
sharding.jdbc.datasource.ds_slave_1.password=

sharding.jdbc.config.masterslave.load-balance-algorithm-type=round_robin
sharding.jdbc.config.masterslave.name=ds_ms
sharding.jdbc.config.masterslave.master-data-source-name=ds_master
sharding.jdbc.config.masterslave.slave-data-source-names=ds_slave_0,ds_slave_1

```

#### The details on some Config options for Reading-writing splitting
Refer to [The Yaml configuration for Reading-writing splitting](#The config items for Read-write splitting)
