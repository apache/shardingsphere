+++
pre = "<b>4.1.3. </b>"
title = "不支持项"
weight = 3
+++

## DataSource 接口

* 不支持 timeout 相关操作

## Connection 接口

* 不支持存储过程，函数，游标的操作
* 不支持执行 native SQL
* 不支持 savepoint 相关操作
* 不支持 Schema/Catalog 的操作
* 不支持自定义类型映射

## Statement 和 PreparedStatement 接口

* 不支持返回多结果集的语句（即存储过程，非 SELECT 多条数据）
* 不支持国际化字符的操作

## ResultSet 接口

* 不支持对于结果集指针位置判断
* 不支持通过非 next 方法改变结果指针位置
* 不支持修改结果集内容
* 不支持获取国际化字符
* 不支持获取 Array

## JDBC 4.1

* 不支持 JDBC 4.1 接口新功能

查询所有未支持方法，请阅读 `org.apache.shardingsphere.driver.jdbc.unsupported` 包。
