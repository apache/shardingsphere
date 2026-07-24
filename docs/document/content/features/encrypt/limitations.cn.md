+++
title = "使用限制"
weight = 2
+++

- 需自行处理数据库中原始的存量数据；
- 模糊查询支持 %、_，暂不支持 escape；
- 加密字段无法支持查询不区分大小写功能；
- 加密字段无法支持比较操作，如：大于、小于、ORDER BY、BETWEEN 等；
- 加密字段无法支持计算操作，如：AVG、SUM 以及计算表达式；
- 不支持使用 `;` 分隔的多条 SQL 同时执行；
- 当投影子查询中包含加密字段时，必须使用别名。

## SQL Server OPENQUERY 加密功能

`OPENQUERY` 的加密改写仅支持如下窄形态透传查询：

```sql
UPDATE OPENQUERY (linked_server, 'SELECT <columns> FROM [<schema>.]<table> [WHERE ...]')
SET <encrypt_column> = <literal_or_parameter>
```

不支持以下场景：

- `SELECT` 列表中的字符串字面量或表达式；
- 括号标识符中包含空格，例如 `[Human Resources]`；
- 三部分表名，例如 `db.schema.table`；
- 逗号分隔的多表源；
- `JOIN`、`CROSS APPLY`、`OUTER APPLY`；
- `UNION`、`UNION ALL`、`EXCEPT`、`INTERSECT`；
- `WHERE` 后引用加密列；
- 非字面量、非参数的赋值表达式，例如 `SET col = UPPER('x')`；
- 物理列名包含 `]`。
