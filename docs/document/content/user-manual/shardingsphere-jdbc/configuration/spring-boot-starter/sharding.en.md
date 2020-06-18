+++
title = "Sharding"
weight = 1
+++

## Configuration Example

```properties
spring.shardingsphere.datasource.names=ds0,ds1

spring.shardingsphere.datasource.ds0.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds0.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds0.url=jdbc:mysql://localhost:3306/ds0
spring.shardingsphere.datasource.ds0.username=root
spring.shardingsphere.datasource.ds0.password=root

spring.shardingsphere.datasource.ds1.type=org.apache.commons.dbcp2.BasicDataSource
spring.shardingsphere.datasource.ds1.driver-class-name=com.mysql.jdbc.Driver
spring.shardingsphere.datasource.ds1.url=jdbc:mysql://localhost:3306/ds1
spring.shardingsphere.datasource.ds1.username=root
spring.shardingsphere.datasource.ds1.password=root

spring.shardingsphere.rules.sharding.tables.t_order.actual-data-nodes=ds$->{0..1}.t_order$->{0..7}
spring.shardingsphere.rules.sharding.tables.t_order.database-strategy.standard.database_inline.sharding-column=user_id
spring.shardingsphere.rules.sharding.tables.t_order.database-strategy.standard.database_inline.sharding-algorithm-name=database_inline
spring.shardingsphere.rules.sharding.tables.t_order.table-strategy.standard.t_order_inline.sharding-column=order_id
spring.shardingsphere.rules.sharding.tables.t_order.table-strategy.standard.t_order_inline.sharding-algorithm-name=t_order_inline
spring.shardingsphere.rules.sharding.tables.t_order.key-generate-strategy.column=order_id
spring.shardingsphere.rules.sharding.tables.t_order.key-generate-strategy.key-generator-name=snowflake

spring.shardingsphere.rules.sharding.tables.t_order_item.actual-data-nodes=ds$->{0..1}.t_order_item$->{0..7}
spring.shardingsphere.rules.sharding.tables.t_order_item.database-strategy.standard.database_inline.sharding-column=user_id
spring.shardingsphere.rules.sharding.tables.t_order_item.database-strategy.standard.database_inline.sharding-algorithm-name=database_inline
spring.shardingsphere.rules.sharding.tables.t_order_item.table-strategy.standard.t_order_inline.sharding-column=order_id
spring.shardingsphere.rules.sharding.tables.t_order_item.table-strategy.standard.t_order_inline.sharding-algorithm-name=t_order_item_inline
spring.shardingsphere.rules.sharding.tables.t_order_item.key-generate-strategy.column=order_item_id
spring.shardingsphere.rules.sharding.tables.t_order_item.key-generate-strategy.key-generator-name=snowflake

spring.shardingsphere.rules.sharding.binding-tables=t_order,t_order_item
spring.shardingsphere.rules.sharding.broadcast-tables=t_config

spring.shardingsphere.rules.sharding.sharding-algorithms.database_inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.database_inline.props.algorithm.expression=ds_${user_id % 2}
spring.shardingsphere.rules.sharding.sharding-algorithms.t_order_inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.t_order_inline.props.algorithm.expression=t_order_${order_id % 8}
spring.shardingsphere.rules.sharding.sharding-algorithms.t_order_item_inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.t_order_item_inline.props.algorithm.expression=t_order_item_${order_id % 8}

spring.shardingsphere.sharding.key-generators.snowflake.type=SNOWFLAKE
spring.shardingsphere.sharding.key-generators.snowflake.props.worder.id=123
```

## Configuration Item Explanation

```properties
spring.shardingsphere.datasource.names= # Omit data source configuration

spring.shardingsphere.rules.sharding.tables.<table-name>.actual-data-nodes= # Describe data source names and actual tables, delimiter as point, multiple data nodes separated with comma, support inline expression. Absent means sharding databases only. Example: ds${0..7}.tbl${0..7}

# Databases sharding strategy, use default databases sharding strategy if absent. sharding strategy below can choose only one.

# For single sharding column scenario
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.standard.<sharding-algorithm-name>.sharding-column= # Sharding column name
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.standard.<sharding-algorithm-name>.sharding-algorithm-name= # Sharding algorithm name

# For multiple sharding columns scenario
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.complex.<sharding-algorithm-name>.sharding-columns= # Sharding column names, multiple columns separated with comma
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.complex.<sharding-algorithm-name>.sharding-algorithm-name= # Sharding algorithm name

# Sharding by hint
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.hint.<sharding-algorithm-name>.sharding-algorithm-name= # Sharding algorithm name

# Tables sharding strategy, same as database sharding strategy
spring.shardingsphere.rules.sharding.tables.<table-name>.table-strategy.xxx= # Omitted

# Key generator strategy configuration
spring.shardingsphere.rules.sharding.tables.<table-name>.key-generate-strategy.column= # Column name of key generator
spring.shardingsphere.rules.sharding.tables.<table-name>.key-generate-strategy.key-generator-name= # Key generator name

spring.shardingsphere.rules.sharding.binding-tables[0]= # Binding table name
spring.shardingsphere.rules.sharding.binding-tables[1]= # Binding table name
spring.shardingsphere.rules.sharding.binding-tables[x]= # Binding table name

spring.shardingsphere.rules.sharding.broadcast-tables[0]= # Broadcast tables
spring.shardingsphere.rules.sharding.broadcast-tables[1]= # Broadcast tables
spring.shardingsphere.rules.sharding.broadcast-tables[x]= # Broadcast tables

spring.shardingsphere.sharding.default-database-strategy.xxx= # Default strategy for database sharding
spring.shardingsphere.sharding.default-table-strategy.xxx= # Default strategy for table sharding
spring.shardingsphere.sharding.default-key-generate-strategy.xxx= # Default Key generator strategy

# Sharding algorithm configuration
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.type= # Sharding algorithm type
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.props.xxx=# Sharding algorithm properties

# Key generate algorithm configuration
spring.shardingsphere.rules.sharding.key-generators.<key-generate-algorithm-name>.type= # Key generate algorithm type
spring.shardingsphere.rules.sharding.key-generators.<key-generate-algorithm-name>.props.xxx= # Key generate algorithm properties
```

Please refer to [Built-in Sharding Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/sharding) and [Built-in Key Generate Algorithm List](/en/user-manual/shardingsphere-jdbc/configuration/built-in-algorithm/keygen) for more details about type of algorithm.

## Attention

Inline expression identifier can use `${...}` or `$->{...}`, but `${...}` is conflict with spring placeholder of properties, so use `$->{...}` on spring environment is better.
