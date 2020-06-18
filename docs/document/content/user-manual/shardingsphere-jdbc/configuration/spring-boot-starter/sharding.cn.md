+++
title = "数据分片"
weight = 1
+++

## 配置示例

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

## 配置项说明

```properties
spring.shardingsphere.datasource.names= # 省略数据源配置

spring.shardingsphere.rules.sharding.tables.<table_name>.actual-data-nodes= # 由数据源名 + 表名组成，以小数点分隔。多个表以逗号分隔，支持inline表达式。缺省表示使用已知数据源与逻辑表名称生成数据节点，用于广播表（即每个库中都需要一个同样的表用于关联查询，多为字典表）或只分库不分表且所有库的表结构完全一致的情况

# 分库策略，缺省表示使用默认分库策略，以下的分片策略只能选其一

# 用于单分片键的标准分片场景
spring.shardingsphere.rules.sharding.tables.<table_name>.database-strategy.standard.<sharding_algorithm_name>.sharding-column= # 分片列名称
spring.shardingsphere.rules.sharding.tables.<table_name>.database-strategy.standard.<sharding_algorithm_name>.sharding-algorithm-name= # 分片算法名称

# 用于多分片键的复合分片场景
spring.shardingsphere.rules.sharding.tables.<table_name>.database-strategy.complex.<sharding_algorithm_name>.sharding-columns= # 分片列名称，多个列以逗号分隔
spring.shardingsphere.rules.sharding.tables.<table_name>.database-strategy.complex.<sharding_algorithm_name>.sharding-algorithm-name= # 分片算法名称

# 用于Hint 的分片策略
spring.shardingsphere.rules.sharding.tables.<table_name>.database-strategy.hint.<sharding_algorithm_name>.sharding-algorithm-name= # 分片算法名称

# 分表策略，同分库策略
spring.shardingsphere.rules.sharding.tables.<table_name>.table-strategy.xxx= # 省略

# 分布式序列策略配置
spring.shardingsphere.rules.sharding.tables.<table_name>.key-generate-strategy.column= # 分布式序列列名称
spring.shardingsphere.rules.sharding.tables.<table_name>.key-generate-strategy.key-generator-name= # 分布式序列算法名称

spring.shardingsphere.rules.sharding.binding-tables[0]= # 绑定表规则列表
spring.shardingsphere.rules.sharding.binding-tables[1]= # 绑定表规则列表
spring.shardingsphere.rules.sharding.binding-tables[x]= # 绑定表规则列表

spring.shardingsphere.rules.sharding.broadcast-tables[0]= # 广播表规则列表
spring.shardingsphere.rules.sharding.broadcast-tables[1]= # 广播表规则列表
spring.shardingsphere.rules.sharding.broadcast-tables[x]= # 广播表规则列表

spring.shardingsphere.sharding.default-database-strategy.xxx= # 默认数据库分片策略
spring.shardingsphere.sharding.default-table-strategy.xxx= # 默认表分片策略
spring.shardingsphere.sharding.default-key-generate-strategy.xxx= # 默认分布式序列策略

# 分片算法配置
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding_algorithm_name>.type= # 分片算法类型
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding_algorithm_name>.props.xxx=# 分片算法属性配置

# 分布式序列算法配置
spring.shardingsphere.rules.sharding.key-generators.<sharding_algorithm_name>.type= # 分布式序列算法类型
spring.shardingsphere.rules.sharding.key-generators.<sharding_algorithm_name>.props.xxx= # 分布式序列算法属性配置
```

## 注意事项

行表达式标识符可以使用 `${...}` 或 `$->{...}`，但前者与 Spring 本身的属性文件占位符冲突，因此在 Spring 环境中使用行表达式标识符建议使用 `$->{...}`。
