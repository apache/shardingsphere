+++
toc = true
title = "使用场景"
weight = 3
prev = "/02-sharding/workflow/"
next = "/02-sharding/configuration/"
+++

## 数据库模式

本文档中提供了两个数据源db0和db1，每个数据源之中包含了两组表t_order_0和t_order_1，t_order_item_0和t_order_item_1。这两组表的建表语句为：

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

## 逻辑表与实际表映射关系

### 均匀分布

数据表在每个数据源内呈现均匀分布的态势：

```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_0 
  └── t_order_1
```

Sharding-JDBC可以支持多种规则配置的方式，以下以最为简单和通用的yaml举例。

真实数据节点的配置如下：

```yaml
    t_order:
        actualDataNodes: db0.t_order_0, db0.t_order_1, db1.t_order_0, db1.t_order_1
```

也可以通过inline表达式简化配置：

```yaml
    t_order:
        actualDataNodes: db${0..1}.t_order_${0..1}
```

### 自定义分布

数据表呈现有特定规则的分布：

```
db0
  ├── t_order_0 
  └── t_order_1 
db1
  ├── t_order_2
  ├── t_order_3
  └── t_order_4
```

表规则可以指定每张表在数据源中的分布情况：

```yaml
    t_order:
        actualDataNodes: db0.t_order_0, db0.t_order_1, db1.t_order_2, db1.t_order_3, db1.t_order_4
```

同样可以通过inline表达式简化配置：

```yaml
    t_order:
        actualDataNodes: db0.t_order_${0..1},db1.t_order_${2..4}
```

### 本教程采用的数据分布例子：

```
db0
  ├── t_order_0               user_id为偶数   order_id为偶数
  ├── t_order_1               user_id为偶数   order_id为奇数
  ├── t_order_item_0          user_id为偶数   order_id为偶数
  └── t_order_item_1          user_id为偶数   order_id为奇数
db1
  ├── t_order_0               user_id为奇数   order_id为偶数
  ├── t_order_1               user_id为奇数   order_id为奇数
  ├── t_order_item_0          user_id为奇数   order_id为偶数
  └── t_order_item_1          user_id为奇数   order_id为奇数
```

## 逻辑表与实际表：

配置分库分表的目的是将原有一张表的数据分散到不同库不同表中，且不改变原有SQL语句的情况下来使用这一张表。那么从一张表到多张的映射关系需要使用逻辑表与实际表这两种概念。下面通过一个例子来解释一下。假设在使用PreparedStatement访问数据库，SQL如下：

```sql
select * from t_order where user_id = ? and order_id = ?;
```

当user_id=0且order=0时，Sharding-JDBC将会将SQL语句转换为如下形式：

```sql
select * from db0.t_order_0 where user_id = ? and order_id = ?;
```

其中原始SQL中的t_order就是 __逻辑表__，而转换后的db0.t_order_0就是 __实际表__。

## 规则配置

以上分库分表的形式Sharding-JDBC是通过规则配置来进行的描述的，下面讲通过几个小节来描述规则的详细配置：

```yaml
shardingRule:
  tables:
    t_order:
      actualDataNodes: db${0..1}.t_order_${0..1}
      databaseStrategy:
        inline:
          shardingColumn: user_id
          algorithmExpression: db${user_id % 2}
      tableStrategy:
        inline:
          shardingColumn: order_id
          algorithmExpression: t_order_${order_id % 2}
  bindingTables:
    - t_order,t_order_item
```

## 数据源配置

我们需要构造一个DataSource的Map对象，它是来描述名称与真实数据源映射的。真实的数据源可以使用任意一种数据库连接池，这里使用DBCP来举例：

```yaml
dataSources:
  db0: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db0
    username: root
    password: 
  db1: !!org.apache.commons.dbcp.BasicDataSource
    driverClassName: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/db0
    username: root
    password: 
```

## 策略配置

### 数据源策略与表策略

Sharding-JDBC认为对于分片策略存有两种维度。
- 数据源分片策略DatabaseShardingStrategy：数据被分配的目标数据源。
- 表分片策略TableShardingStrategy：数据被分配的目标表，该目标表存在与该数据的目标数据源内。故表分片策略是依赖与数据源分片策略的结果的。
这里注意的是两种策略的API完全相同，以下针对策略API的讲解将适用于这两种策略。

### 全局默认策略与特定表策略

策略是作用在特定的表规则上的，数据源策略与表策略与特定表相关。

```yaml
    defaultDatabaseStrategy:
      inline:
        shardingColumn: user_id
        algorithmExpression: demo_ds_${user_id % 2}
```

如果分片规则中的所有表或大部分表的分片策略相同，可以使用默认策略来简化配置。

### 分片键

分片键是分片策略的第一个参数。分片键表示的是SQL语句中WHERE中的条件列。分片键可以配置多个。

### 分片算法

Sharding-JDBC提供了5种分片策略。由于分片算法和业务实现紧密相关，因此Sharding-JDBC并未提供内置分片算法，而是通过分片策略将各种场景提炼出来，提供更高层级的抽象，并提供接口让应用开发者自行实现分片算法。

- StandardShardingStrategy

标准分片策略。提供对SQL语句中的=, IN和BETWEEN AND的分片操作支持。StandardShardingStrategy只支持单分片键，提供PreciseShardingAlgorithm和RangeShardingAlgorithm两个分片算法。PreciseShardingAlgorithm是必选的，用于处理=和IN的分片。RangeShardingAlgorithm是可选的，用于处理BETWEEN AND分片，如果不配置RangeShardingAlgorithm，SQL中的BETWEEN AND将按照全库路由处理。

- ComplexShardingStrategy

复合分片策略。提供对SQL语句中的=, IN和BETWEEN AND的分片操作支持。ComplexShardingStrategy支持多分片键，由于多分片键之间的关系复杂，因此Sharding-JDBC并未做过多的封装，而是直接将分片键值组合以及分片操作符交于算法接口，完全由应用开发者实现，提供最大的灵活度。

- InlineShardingStrategy

Inline表达式分片策略。使用Groovy的Inline表达式，提供对SQL语句中的=和IN的分片操作支持。InlineShardingStrategy只支持单分片键，对于简单的分片算法，可以通过简单的配置使用，从而避免繁琐的Java代码开发，如: t_user_${user_id % 8} 表示t_user表按照user_id按8取模分成8个表，表名称为t_user_0到t_user_7。

- HintShardingStrategy

通过Hint而非SQL解析的方式分片的策略。

- NoneShardingStrategy

不分片的策略。

### 级联绑定表

级联绑定表代表一组表，这组表的逻辑表与实际表之间的映射关系是相同的。比如t_order与t_order_item就是这样一组绑定表关系，它们的分库与分表策略是完全相同的,那么可以使用它们的表规则将它们配置成级联绑定表。

那么在进行SQL路由时，如果SQL为：

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.user_id=? AND o.order_id=?
```

其中t_order在FROM的最左侧，Sharding-JDBC将会以它作为整个绑定表的主表。所有路由计算将会只使用主表的策略，那么t_order_item表的分片计算将会使用t_order的条件。故绑定表之间的分区键要完全相同。

## 构造ShardingDataSource

完成规则配置后，我们可以通过ShardingDataSourceFactory工厂得到ShardingDataSource

```java
DataSource dataSource = ShardingDataSourceFactory.createDataSource(dataSourceMap, shardingRuleConfig);

```

## 使用ShardingDataSource
通过一个例子来看看如何使用该数据源
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
该数据源与普通数据源完全相同，你可以通过上例的API形式来使用，也可以将其配置在Spring，Hibernate等框架中使用。
