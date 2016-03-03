+++
date = "2016-02-05T17:03:18+08:00"
title = "Release Notes"
weight = 1
+++

# Release Notes

## 1.0.1-snapshot
1.增加使用ThreadLocal方式动态生成分区键值的方式进行SQL路由的功能

1. 修正JPA与Sharding-JDBC的兼容问题。JPA会自动增加SELECT的列别名，导致ORDER BY只能通过别名，而非列名称获取ResultSet的数据。
1. 修正[issue #11](https://github.com/dangdangdotcom/sharding-jdbc/issues/11) count函数在某些情况下返回不正确
1. 修正[issue #13](https://github.com/dangdangdotcom/sharding-jdbc/issues/13) Insert 语句 没有写列名 进行了全路由
1. 修正[issue #16](https://github.com/dangdangdotcom/sharding-jdbc/issues/16) 改造多线程执行模型
1. 修正[issue #18](https://github.com/dangdangdotcom/sharding-jdbc/issues/18) 查询Count时，使用getObject()取数会报异常
1. 修正[issue #19](https://github.com/dangdangdotcom/sharding-jdbc/issues/19) sum和avg函数，不加别名不执行merger，加了空指针异常


## 1.0.0
1. 初始版本。
