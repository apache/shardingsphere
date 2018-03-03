+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "Database Sharding"
weight = 2
prev = "/02-guide/concepts/"
next = "/02-guide/master-slave/"

+++

Read this guide before you start with Quick Start. This section will further introduce the usage of Sharding-JDBC's Database-Sharding in more complex cases.

## Database Information
Two data sources, db0 and db1 are for example to illustrate. Each data source has two sets of tables, t_order_0 and t_order_1, t_order_item_0 and t_order_item_1. The SQLs for creating table:

```sql
CREATE TABLE IF NOT EXISTS t_order_x (
  order_id INT NOT NULL,
  user_id  INT NOT NULL,
  PRIMARY KEY (order_id)
);
CREATE TABLE IF NOT EXISTS t_order_item_x (
  item_id  INT NOT NULL,
  order_id INT NOT NULL,
  user_id  INT NOT NULL,
  PRIMARY KEY (item_id)
);
```
## The correspondence between LogicTable and ActualTable
### Uniform distribution
Tables are evenly distributed in each data source.

```
db0
  ├── t_order_0
  └── t_order_1
db1
  ├── t_order_0
  └── t_order_1
```

Use default configuration for Table rules.

```java
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("db0.t_order_0, db0.t_order_1, db1.t_order_0, db1.t_order_1");
```

### User-defined distribution
Tables are not evenly distributed in each data source.

```
db0
  ├── t_order_0
  └── t_order_1
db1
  ├── t_order_2
  ├── t_order_3
  └── t_order_4
```

Use User-defined configuration for Table rules.

```java
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("db0.t_order_0, db0.t_order_1, db1.t_order_0, db1.t_order_1");
```

### Table distribution for example

```
db0
  ├── t_order_0               user_id is even number   order_id is even number
  ├── t_order_1               user_id is even number   order_id is odd number
  ├── t_order_item_0          user_id is even number   order_id is even number
  └── t_order_item_1          user_id is even number   order_id is odd number
db1
  ├── t_order_0               user_id is odd number    order_id is even number
  ├── t_order_1               user_id is odd number    order_id is odd number
  ├── t_order_item_0          user_id is odd number    order_id is even number
  └── t_order_item_1          user_id is odd number    order_id is odd number
```

## LogicTable and ActualTable

The purpose of Database-Sharding is to spread the data from the original table to different tables in different databases, and to query data without changing the original SQLs. This mapping relation will be illustrated by using LogicTable and ActualTable. Assuming access to the database using PreparedStatement, SQL is as follows:

```sql
select * from t_order where user_id = ? and order_id = ?;
```

when condition is user_id=0 and order=0，Sharding-JDBC will change this SQL to the following SQL:

```sql
select * from db0.t_order_0 where user_id = ? and order_id = ?;
```

t_order in the first SQL is __LogicTable__, and db0.t_order_0 in the second SQL is __ActualTable__.

## Rule Configuration
You can set rule configuration to achieve the above-mentioned functions, and detailed rule configuration will be introduced in this part:

```java
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRule);
    shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRule);
    shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id", "xxx.ModuloDatabaseShardingAlgorithm"));
    shardingRuleConfig.setDefaultTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("order_id", "xxx.ModuloTableShardingAlgorithm"));
```

## Data-Source Configuration
We need to create a DataSource Map object which is used to describe the mapping of data-source's name and data source.

```java
Map<String, DataSource> dataSourceMap = new HashMap<>();
dataSourceMap.put("ds_0", createDataSource("ds_0"));
dataSourceMap.put("ds_1", createDataSource("ds_1"));
```

You can access data source via any kind of connection pool, Here is DBCP.

```java
private DataSource createDataSource(final String dataSourceName) {
    BasicDataSource result = new BasicDataSource();
    result.setDriverClassName(com.mysql.jdbc.Driver.class.getName());
    result.setUrl(String.format("jdbc:mysql://localhost:3306/%s", dataSourceName));
    result.setUsername("root");
    result.setPassword("");
    return result;
}
```

## Strategy Configuration

### The Strategy of data-source and table

There are two dimensions for sharding strategies in Sharding-JDBC:
- DatabaseShardingStrategy: The strategy for data sourcse where data is distributed.
- TableShardingStrategy: The strategy for tables where data is distributed. TableShardingStrategy is dependent on DatabaseShardingStrategy, for those tables exists in corresponding data sources. In addition, The API for those two strategies is same, therefore we will give a detailed introduction for this API.

### Global default strategy for specific table rules

Strategies are closely related to table rules, for strategies apply to specific table rule.

```java
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1");
    orderTableRuleConfig.setDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id", "xxx.ModuloDatabaseShardingAlgorithm"));
    orderTableRuleConfig.setTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("order_id", "xxx.ModuloTableShardingAlgorithm"));
```

If all or most of the tables are in the same sharding strategies, you can use the default strategy to simplify the configuration.

```java
    TableRuleConfiguration orderTableRuleConfig = new TableRuleConfiguration();
    orderTableRuleConfig.setLogicTable("t_order");
    orderTableRuleConfig.setActualDataNodes("ds_0.t_order_0, ds_0.t_order_1, ds_1.t_order_0, ds_1.t_order_1");
    
    TableRuleConfiguration orderItemTableRuleConfig = new TableRuleConfiguration();
    orderItemTableRuleConfig.setLogicTable("t_order_item");
    orderItemTableRuleConfig.setActualDataNodes("ds_0.t_order_item_0,ds_0.t_order_item_1,ds_1.t_order_item_0,ds_1.t_order_item_1");
    
    ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
    shardingRuleConfig.getTableRuleConfigs().add(orderTableRuleConfig);
    shardingRuleConfig.getTableRuleConfigs().add(orderItemTableRuleConfig);
    shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(new ComplexShardingStrategyConfiguration("user_id", "xxx.ModuloDatabaseShardingAlgorithm"));
    shardingRuleConfig.setDefaultTableShardingStrategyConfig(new ComplexShardingStrategyConfiguration("order_id", "xxx.ModuloTableShardingAlgorithm"));
```

### Sharding Column

Sharding Columns set as the first parameter in sharding strategy are condition columns in WHERE in SQL. You can configure multiple sharding columns.

### Sharding Algorithm

Sharding-JDBC provides 5 kinds of sharding strategies. Because of the closely connection between specific business and specific sharding algorithms, Sharding-JDBC not carry out sharding algorithm. Instead, after making a higher level of abstraction, we provide API to allow developers to implement sharding algorithms as they need.

- StandardShardingStrategy

Support =, IN, BETWEEN AND in SQLs for sharding operation. StandardShardingStrategy only supports single sharding column, and provides two sharding algorithms of PreciseShardingAlgorithm and RangeShardingAlgorithm. The PreciseShardingAlgorithm is required to handle the sharding operation of = and IN. The RangeShardingAlgorithm is optional to handle BETWEEN AND. If the RangeShardingAlgorithm is not configured, the BETWEEN-AND SQLs will be executed in all tables.

- ComplexShardingStrategy

Support =, IN, BETWEEN AND in SQLs for sharding operation. ComplexShardingStrategy supports multiple sharding columns. Due to the complex relationship among the multiple sharding columns, Sharding-JDBC only provide algorithm API to allow developers combine different sharding columns and implement the specific algorithm.

- InlineShardingStrategy

This strategy provides sharding support for =, IN in SQLs by means of Groovy's Inline expression. InlineShardingStrategy only supports single sharding column. Some simple sharding algorithm can be configured, e.g. t_user_ $ {user_id% 8} shows us the t_user table is divided into 8 tables via mod(user_id), and the child tables is t_user_0 to t_user_7.

- HintShardingStrategy

Support spliting table by means of Hint method, not SQL Parsing.

- NoneShardingStrategy

Do not split databases or tables.

### Cascade Binding Table

It consists of a group of tables for which the mapping relationship between their logical tables and actual tables is the same. e.g. The order table splited with Order ID, and the order item table also splited with Order ID. As a result, you can configure order table and order item table as BindingTable of each other.

In this condition, if the SQL is as follows：

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?
```

t_order is at the left of FROM, Sharding-JDBC will treat it as driving table for the group of binding tables. All routing calculations will only employ the configured strategy of driving table. Therefore the routing calculation for t_order_item will use the condition of t_order as well. The core of this implementation lies in their same Sharding Column.

## Sharding DataSource Creation

We can get ShardingDataSource from the ShardingDataSourceFactory factory after configuring the rules.

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);

```

## Sharding DataSource Usage

Let us take an example to learn how to use this data source.

```java
String sql = "SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?";
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement preparedStatement = conn.prepareStatement(sql);
                ) {
            preparedStatement.setInt(1, 10);
            preparedStatement.setInt(2, 1001);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                System.out.println(rs.getInt(1));
                System.out.println(rs.getInt(2));
                System.out.println(rs.getInt(3));
            }
            rs.close();
        }
```

Same as the ordinary data, you can use it by the above-mentioned API. At the same time, you can also configure it in Spring, Hibernate framework.

> If you not want to use columns in the table as sharding columns, please refer to: [Mandatory Routing](/02-guide/hint-sharding-value)


