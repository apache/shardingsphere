+++
title = "SQL Hint"
weight = 3
chapter = true
+++

## 背景信息

目前，主流的关系型数据库基本都提供了 SQL Hint 作为 SQL 语法的补充，SQL Hint 允许用户通过数据库内置的 Hint 语法来干预 SQL 的执行过程，从而完成一些特殊的功能，或实现对 SQL 执行的优化。
ShardingSphere 同样提供了丰富的 SQL Hint 语法，允许用户进行数据分片、读写分离的强制路由以及数据源透传等灵活控制。

## 使用规范

ShardingSphere 的 SQL Hint 语法需要以注释的形式编写在 SQL 中，SQL Hint 语法格式暂时只支持 `/* */`，Hint 内容需要以 `SHARDINGSPHERE_HINT:` 为起始，然后定义不同功能所对应的属性键值对，当存在多个属性时使用逗号分隔。
ShardingSphere 的 SQL Hint 语法格式如下：

```sql
/* SHARDINGSPHERE_HINT: {key} = {value}, {key} = {value} */ SELECT * FROM t_order;
```

如果使用 MySQL 客户端连接，需要添加 `-c` 选项保留注释，客户端默认是 `--skip-comments` 过滤注释。

## 参数解释

ShardingSphere SQL Hint 中可以定义如下的属性，为了兼容低版本 SQL Hint 语法，也可以使用别名中定义的属性： 

| *名称*                        | *别名*                  | *数据类型*     | *说明*                                  | *默认值* |
|-----------------------------|-----------------------|------------|---------------------------------------|-------|
| SHARDING_DATABASE_VALUE (?) | shardingDatabaseValue | Comparable | 数据分片分库值，和 Hint 分片策略配合使用               | -     |
| SHARDING_TABLE_VALUE (?)    | shardingTableValue    | Comparable | 数据分片分表值，和 Hint 分片策略配合使用               | -     |
| WRITE_ROUTE_ONLY (?)        | writeRouteOnly        | boolean    | 读写分离强制路由到主库执行                         | false |
| DATA_SOURCE_NAME (?)        | dataSourceName        | String     | 数据源透传，将 SQL 直接路由到指定数据源                | -     |
| SKIP_SQL_REWRITE (?)        | skipSQLRewrite        | boolean    | 跳过 SQL 改写阶段                           | false |
| DISABLE_AUDIT_NAMES (?)     | disableAuditNames     | String     | 禁用指定 SQL 审计算法                         | -     |
| SHADOW (?)                  | shadow                | boolean    | 影子库强制路由到影子库数据源执行，和影子库 SQL_HINT 算法配合使用 | false |


## SQL Hint

### 数据分片

数据分片 SQL Hint 功能的可选属性包括：

- `{table}.SHARDING_DATABASE_VALUE`：用于添加 `{table}` 表对应的数据源分片键值，多个属性使用逗号分隔；
- `{table}.SHARDING_TABLE_VALUE`：用于添加 `{table}` 表对应的表分片键值，多个属性使用逗号分隔。

> 分库不分表情况下，强制路由至某一个分库时，可使用 `SHARDING_DATABASE_VALUE` 方式设置分片，无需指定 `{table}`。

数据分片 SQL Hint 功能的使用示例：

```sql
/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */ SELECT * FROM t_order;
```

### 读写分离

读写分离 SQL Hint 功能的可选属性为 `WRITE_ROUTE_ONLY`，`true` 表示将当前 SQL 强制路由到主库执行。

读写分离 SQL Hint 功能的使用示例：

```sql
/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */ SELECT * FROM t_order;
```

### 数据源透传

数据源透传 SQL Hint 功能可选属性为 `DATA_SOURCE_NAME`，需要指定注册在 ShardingSphere 逻辑库中的数据源名称。

数据源透传 SQL Hint 功能的使用示例：

```sql
/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_0 */ SELECT * FROM t_order;
```

### 跳过 SQL 改写

跳过 SQL 改写 SQL Hint 功能可选属性为 `SKIP_SQL_REWRITE`，`true` 表示跳过当前 SQL 的改写阶段。

跳过 SQL 改写 SQL Hint 功能的使用示例：

```sql
/* SHARDINGSPHERE_HINT: SKIP_SQL_REWRITE=true */ SELECT * FROM t_order;
```

### 禁用 SQL 审计

禁用 SQL 审计 SQL Hint 功能可选属性为 `DISABLE_AUDIT_NAMES`，需要指定需要禁用的 SQL 审计算法名称，多个 SQL 审计算法需要使用逗号分隔。

禁用 SQL 审计 SQL Hint 功能的使用示例：

```sql
/* SHARDINGSPHERE_HINT: DISABLE_AUDIT_NAMES=sharding_key_required_auditor */ SELECT * FROM t_order;
```

### 影子库压测

影子库压测 SQL Hint 功能可选属性为 `SHADOW`，`true` 表示将当前 SQL 路由至影子库数据源执行。

影子库压测 SQL Hint 功能的使用示例：

```sql
/* SHARDINGSPHERE_HINT: SHADOW=true */ SELECT * FROM t_order;
```
