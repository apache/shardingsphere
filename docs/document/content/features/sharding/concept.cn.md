+++
title = "核心概念"
weight = 1
+++

## 表

表是透明化数据分片的关键概念。 Apache ShardingSphere 通过提供多样化的表类型，适配不同场景下的数据分片需求。

### 逻辑表

相同结构的水平拆分数据库（表）的逻辑名称，是 SQL 中表的逻辑标识。 例：订单数据根据主键尾数拆分为 10 张表，分别是 `t_order_0` 到 `t_order_9`，他们的逻辑表名为 `t_order`。

### 真实表

在水平拆分的数据库中真实存在的物理表。 即上个示例中的 `t_order_0` 到 `t_order_9`。

### 绑定表

指分片规则一致的一组分片表。 使用绑定表进行多表关联查询时，必须使用分片键进行关联，否则会出现笛卡尔积关联或跨库关联，从而影响查询效率。 例如：`t_order` 表和 `t_order_item` 表，均按照 `order_id` 分片，并且使用 `order_id` 进行关联，则此两张表互为绑定表关系。 绑定表之间的多表关联查询不会出现笛卡尔积关联，关联查询效率将大大提升。 举例说明，如果 SQL 为：

```sql
SELECT i.* FROM t_order o JOIN t_order_item i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

在不配置绑定表关系时，假设分片键 order_id 将数值 10 路由至第 0 片，将数值 11 路由至第 1 片，那么路由后的 SQL 应该为 4 条，它们呈现为笛卡尔积：

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_0 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

在配置绑定表关系，并且使用 `order_id` 进行关联后，路由的 SQL 应该为 2 条：

```sql
SELECT i.* FROM t_order_0 o JOIN t_order_item_0 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);

SELECT i.* FROM t_order_1 o JOIN t_order_item_1 i ON o.order_id=i.order_id WHERE o.order_id in (10, 11);
```

其中 `t_order` 表由于指定了分片条件，ShardingSphere 将会以它作为整个绑定表的主表。 所有路由计算将会只使用主表的策略，那么 `t_order_item` 表的分片计算将会使用 `t_order` 的条件。

注意：绑定表中的多个分片规则，需要按照逻辑表前缀组合分片后缀的方式进行配置，例如：

```yaml
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: ds_${0..1}.t_order_${0..1}
    t_order_item:
      actualDataNodes: ds_${0..1}.t_order_item_${0..1}
  bindingTables:
    - t_order, t_order_item
```

> **命名提示**：ShardingSphere 会通过移除真实表名尾部的数字来推导绑定关系，因此表尾部的数字应仅用于 `_0`、`_1` 这类分片后缀。

### 广播表

指所有的数据源中都存在的表，表结构及其数据在每个数据库中均完全一致。 适用于数据量不大且需要与海量数据的表进行关联查询的场景，例如：字典表。

### 单表

指所有的分片数据源中仅唯一存在的表。 适用于数据量不大且无需分片的表。

注意：符合以下条件的单表会被自动加载：
- 数据加密、数据脱敏等规则中显示配置的单表
- 用户通过 ShardingSphere 执行 DDL 语句创建的单表

其余不符合上述条件的单表，ShardingSphere 不会自动加载，用户可根据需要配置单表规则进行管理。

## 数据节点

数据分片的最小单元，由数据源名称和真实表组成。 例：ds_0.t_order_0。
逻辑表与真实表的映射关系，可分为均匀分布和自定义分布两种形式。

### 均匀分布

指数据表在每个数据源内呈现均匀分布的态势， 例如：

```Nginx
db0
  ├── t_order0
  └── t_order1
db1
  ├── t_order0
  └── t_order1
```

数据节点的配置如下：

```CSS
db0.t_order0, db0.t_order1, db1.t_order0, db1.t_order1
```

### 自定义分布

指数据表呈现有特定规则的分布， 例如：

```Nginx
db0
  ├── t_order0
  └── t_order1
db1
  ├── t_order2
  ├── t_order3
  └── t_order4
```

数据节点的配置如下：

```CSS
db0.t_order0, db0.t_order1, db1.t_order2, db1.t_order3, db1.t_order4
```

## 分片

### 分片键

用于将数据库（表）水平拆分的数据库字段。 例：将订单表中的订单主键的尾数取模分片，则订单主键为分片字段。 SQL 中如果无分片字段，将执行全路由，性能较差。 除了对单分片字段的支持，Apache ShardingSphere 也支持根据多个字段进行分片。

### 分片算法

用于将数据分片的算法，支持 `=`、`>=`、`<=`、`>`、`<`、`BETWEEN` 和 `IN` 进行分片。 分片算法可由开发者自行实现，也可使用 Apache ShardingSphere 内置的分片算法语法糖，灵活度非常高。

### 自动化分片算法

分片算法语法糖，用于便捷的托管所有数据节点，使用者无需关注真实表的物理分布。 包括取模、哈希、范围、时间等常用分片算法的实现。

### 自定义分片算法

提供接口让应用开发者自行实现与业务实现紧密相关的分片算法，并允许使用者自行管理真实表的物理分布。 自定义分片算法又分为：

- 标准分片算法

用于处理使用单一键作为分片键的 `=`、`IN`、`BETWEEN AND`、`>`、`<`、`>=`、`<=` 进行分片的场景。

- 复合分片算法

用于处理使用多键作为分片键进行分片的场景，包含多个分片键的逻辑较复杂，需要应用开发者自行处理其中的复杂度。

- Hint 分片算法

用于处理使用 `Hint` 行分片的场景。

### 分片策略

包含分片键和分片算法，由于分片算法的独立性，将其独立抽离。 真正可用于分片操作的是分片键 + 分片算法，也就是分片策略。

### 强制分片路由

对于分片字段并非由 SQL 而是其他外置条件决定的场景，可使用 SQL Hint 注入分片值。 例：按照员工登录主键分库，而数据库中并无此字段。 SQL Hint 支持通过 Java API 和 SQL 注释两种方式使用。 详情请参见强制分片路由。

## 行表达式

行表达式是为了解决配置的简化与一体化这两个主要问题。在繁琐的数据分片规则配置中，随着数据节点的增多，大量的重复配置使得配置本身不易被维护。 通过行表达式可以有效地简化数据节点配置工作量。

对于常见的分片算法，使用 Java 代码实现并不有助于配置的统一管理。 通过行表达式书写分片算法，可以有效地将规则配置一同存放，更加易于浏览与存储。

行表达式作为字符串由两部分组成，分别是字符串开头的对应 SPI 实现的 Type Name 部分和表达式部分。 以 `<GROOVY>t_order_${1..3}` 为例，字符
串`<GROOVY>` 部分的子字符串 `GROOVY` 为此行表达式使用的对应 SPI 实现的 Type Name，其被  `<>` 符号包裹来识别。而字符串 `t_order_${1..3}`
为此行表达式的表达式部分。当行表达式不指定 Type Name 时，例如 `t_order_${1..3}`，行表示式默认将使用 `InlineExpressionParser` SPI 的 
`GROOVY` 实现来解析表达式。

以下部分介绍 `GROOVY` 实现的语法规则。

行表达式的使用非常直观，只需要在配置中使用 `${ expression }` 或 `$->{ expression }` 标识行表达式即可。 目前支持数据节点和分片算法这两个部分的配置。 行表达式的内容使用的是 Groovy 的语法，Groovy 能够支持的所有操作，行表达式均能够支持。 例如：

`${begin..end}` 表示范围区间
`${[unit1, unit2, unit_x]}` 表示枚举值

行表达式中如果出现连续多个 `${ expression }` 或 `$->{ expression }` 表达式，整个表达式最终的结果将会根据每个子表达式的结果进行笛卡尔组合。

例如，以下行表达式：

```Groovy
${['online', 'offline']}_table${1..3}
```

最终会解析为：
```PlainText
online_table1, online_table2, online_table3, offline_table1, offline_table2, offline_table3
```

## 分布式主键

传统数据库软件开发中，主键自动生成技术是基本需求。而各个数据库对于该需求也提供了相应的支持，比如 MySQL 的自增键，Oracle 的自增序列等。 数据分片后，不同数据节点生成全局唯一主键是非常棘手的问题。同一个逻辑表内的不同实际表之间的自增键由于无法互相感知而产生重复主键。 虽然可通过约束自增主键初始值和步长的方式避免碰撞，但需引入额外的运维规则，使解决方案缺乏完整性和可扩展性。

目前有许多第三方解决方案可以完美解决这个问题，如 UUID 等依靠特定算法自生成不重复键，或者通过引入主键生成服务等。为了方便用户使用、满足不同用户不同使用场景的需求， Apache ShardingSphere 不仅提供了内置的分布式主键生成器，例如 UUID、SNOWFLAKE，还抽离出分布式主键生成器的接口，方便用户自行实现自定义的自增主键生成器。
