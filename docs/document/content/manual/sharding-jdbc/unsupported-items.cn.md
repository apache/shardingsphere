+++
pre = "<b>4.1.3. </b>"
title = "JDBC不支持项"
weight = 3
+++

## DataSource接口

- 不支持timeout相关操作

## Connection接口

- 不支持存储过程，函数，游标的操作
- 不支持执行native的SQL
- 不支持savepoint相关操作
- 不支持Schema/Catalog的操作
- 不支持自定义类型映射

## Statement和PreparedStatement接口

- 不支持返回多结果集的语句（即存储过程，非SELECT多条数据）
- 不支持国际化字符的操作

## 对于ResultSet接口

- 不支持对于结果集指针位置判断
- 不支持通过非next方法改变结果指针位置
- 不支持修改结果集内容
- 不支持获取国际化字符
- 不支持获取Array

## JDBC 4.1

- 不支持JDBC 4.1接口新功能

查询所有未支持方法，请阅读`org.apache.shardingsphere.driver.jdbc.unsupported`包。
