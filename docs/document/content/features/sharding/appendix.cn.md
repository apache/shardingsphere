+++
title = "附录"
weight = 3
+++

有限支持的 SQL：

- 使用 JDBC 规范 `getGeneratedKeys` 接口返回自增主键时，需要配合使用支持自增的分布式主键生成器，不支持其他类型的分布式主键生成器

不支持的 SQL：

- CASE WHEN 中包含子查询
- CASE WHEN 中使用逻辑表名（请使用表别名）
- INSERT INTO tbl_name (col1, col2, …) SELECT * FROM tbl_name WHERE col3 = ?（SELECT 子句不支持 * 和内置分布式主键生成器）
- REPLACE INTO tbl_name (col1, col2, …) SELECT * FROM tbl_name WHERE col3 = ?（SELECT 子句不支持 * 和内置分布式主键生成器）
- SELECT MAX(tbl_name.col1) FROM tbl_name（查询列是函数表达式时，查询列前不能使用表名，可以使用表别名）

其他：

- 分片规则中配置的真实表、分片列和分布式序列需要和数据库中的列保持大小写一致。
