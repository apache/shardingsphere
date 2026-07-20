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

`OPENQUERY` 函数的加密改写不支持以下场景：

- `WHERE` 后引用加密列；
- 物理列名中间包含 `]` 或 `[]`；
- 透传查询中使用 `JOIN`、`CROSS APPLY`、`OUTER APPLY`、`UNION`、`UNION ALL`、`EXCEPT`、`INTERSECT`。
