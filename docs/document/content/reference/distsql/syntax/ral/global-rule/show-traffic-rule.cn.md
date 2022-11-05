+++
title = "SHOW TRAFFIC RULE"
weight = 9
+++

### 描述

`SHOW TRAFFIC RULE` 语法用于查询指定的双路由规则
### 语法

```sql
ShowTrafficRule ::=
  'SHOW' 'TRAFFIC' ('RULES' | 'RULE' ruleName)?

ruleName ::=
  identifier
```
### 补充说明

- 未指定 `ruleName` 时，默认查询所有双路由规则

### 返回值说明

| 列                   | 说明         |
|---------------------|--------------|
| name                | 双路由规则名称 |
| labels              | 计算节点标签   |
| algorithm_type      | 双路由算法类型 |
| algorithm_props     | 双路由算法参数 |
| load_balancer_type  | 负载均衡器类型 |
| load_balancer_props | 负载均衡器参数 |

### 示例

- 查询指定双路由规则

```sql
SHOW TRAFFIC RULE sql_match_traffic;
```

```sql
mysql> SHOW TRAFFIC RULE sql_match_traffic;
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| name              | labels | algorithm_type | algorithm_props                                                                | load_balancer_type | load_balancer_props |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| sql_match_traffic | OLTP   | SQL_MATCH      | sql=SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5; | RANDOM             |                     |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
1 row in set (0.00 sec)
```

- 查询所有双路由规则

```sql
SHOW TRAFFIC RULES;
```

```sql
mysql> SHOW TRAFFIC RULES;
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| name              | labels | algorithm_type | algorithm_props                                                                | load_balancer_type | load_balancer_props |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
| sql_match_traffic | OLTP   | SQL_MATCH      | sql=SELECT * FROM t_order WHERE order_id = 1; UPDATE t_order SET order_id = 5; | RANDOM             |                     |
+-------------------+--------+----------------+--------------------------------------------------------------------------------+--------------------+---------------------+
1 row in set (0.04 sec)
```

### 保留字

`SHOW`、`TRAFFIC`、`RULE`、`RULES`

### 相关链接

- [保留字](/cn/reference/distsql/syntax/reserved-word/)