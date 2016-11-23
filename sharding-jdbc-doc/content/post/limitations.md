+++
date = "2016-01-08T16:14:21+08:00"
title = "使用限制"
weight = 8
+++

# 使用限制

## `JDBC`未支持列表

`Sharding-JDBC`暂时未支持不常用的`JDBC`方法。

### `DataSource`接口
- 不支持`timeout`相关操作

### `Connection`接口
- 不支持存储过程，函数，游标的操作
- 不支持执行`native`的`SQL`
- 不支持`savepoint`相关操作
- 不支持`Schema/Catalog`的操作
- 不支持自定义类型映射

### `Statement`和`PreparedStatement`接口
- 不支持返回多结果集的语句（即存储过程，非`SELECT`多条数据）
- 不支持国际化字符的操作

### 对于`ResultSet`接口
- 不支持对于结果集指针位置判断
- 不支持通过非next方法改变结果指针位置
- 不支持修改结果集内容
- 不支持获取国际化字符
- 不支持获取`Array`

### JDBC 4.1
- 不支持JDBC 4.1接口新功能

查询所有未支持方法，请阅读`com.dangdang.ddframe.rdb.sharding.jdbc.unsupported`包。

## SQL语句限制

###  不支持DDL语句
###  不支持子语句
###  不支持`UNION` 和 `UNION ALL`
###  不支持特殊`INSERT`
每条`INSERT`语句只能插入一条数据，不支持`VALUES`后有多行数据的语句
###  不支持`DISTINCT`聚合
###  不支持`dual`虚拟表
