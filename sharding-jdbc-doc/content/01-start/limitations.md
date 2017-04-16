+++
toc = true
date = "2016-12-06T22:38:50+08:00"
title = "使用限制"
weight = 4
prev = "/01-start/features"
next = "/01-start/sql-supported"

+++

## JDBC未支持列表

Sharding-JDBC暂时未支持不常用的JDBC方法。

### DataSource接口
- 不支持timeout相关操作

### Connection接口
- 不支持存储过程，函数，游标的操作
- 不支持执行native的SQL
- 不支持savepoint相关操作
- 不支持Schema/Catalog的操作
- 不支持自定义类型映射

### Statement和PreparedStatement接口
- 不支持返回多结果集的语句（即存储过程，非SELECT多条数据）
- 不支持国际化字符的操作

### 对于ResultSet接口
- 不支持对于结果集指针位置判断
- 不支持通过非next方法改变结果指针位置
- 不支持修改结果集内容
- 不支持获取国际化字符
- 不支持获取Array

### JDBC 4.1
- 不支持JDBC 4.1接口新功能

查询所有未支持方法，请阅读com.dangdang.ddframe.rdb.sharding.jdbc.unsupported包。

## SQL语句限制

###  不支持DDL语句
###  不支持子语句
###  不支持UNION 和 UNION ALL
###  不支持特殊INSERT
每条INSERT语句只能插入一条数据，不支持VALUES后有多行数据的语句
###  不支持DISTINCT聚合
###  不支持dual虚拟表
