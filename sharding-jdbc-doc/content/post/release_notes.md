+++
date = "2016-02-05T17:03:18+08:00"
title = "Release Notes"
weight = 1
+++

# Release Notes

## 1.1.1-SNAPSHOT

### 结构调整

1. [ISSUE #49](https://github.com/dangdangdotcom/sharding-jdbc/issues/49) 调整属性配置
1. [ISSUE #51](https://github.com/dangdangdotcom/sharding-jdbc/issues/51) 重构Hint接口

### 缺陷修正

1. [ISSUE #43](https://github.com/dangdangdotcom/sharding-jdbc/issues/43) yaml文件中包含中文，且操作系统模式不是utf-8编码导致的yaml不能解析
1. [ISSUE #48](https://github.com/dangdangdotcom/sharding-jdbc/issues/48) yaml文件读取后未关闭

## 1.1.0

### 新功能

1. [ISSUE #40](https://github.com/dangdangdotcom/sharding-jdbc/issues/40) 支持YAML文件配置
1. [ISSUE #41](https://github.com/dangdangdotcom/sharding-jdbc/issues/41) 支持Spring命名空间配置
1. [ISSUE #42](https://github.com/dangdangdotcom/sharding-jdbc/issues/42) 支持inline表达式配置

### 缺陷修正

1. [ISSUE #25](https://github.com/dangdangdotcom/sharding-jdbc/issues/25) OR表达式下会出现重复结果问题

## 1.0.1

### 功能提升

1. [ISSUE #39](https://github.com/dangdangdotcom/sharding-jdbc/issues/39) 增加使用暗示(Hint)方式注册分片键值的方式进行SQL路由的功能

### 缺陷修正

1. [ISSUE #11](https://github.com/dangdangdotcom/sharding-jdbc/issues/11) count函数在某些情况下返回不正确
1. [ISSUE #13](https://github.com/dangdangdotcom/sharding-jdbc/issues/13) Insert 语句 没有写列名 进行了全路由
1. [ISSUE #16](https://github.com/dangdangdotcom/sharding-jdbc/issues/16) 改造多线程执行模型
1. [ISSUE #18](https://github.com/dangdangdotcom/sharding-jdbc/issues/18) 查询Count时，使用getObject()取数会报异常
1. [ISSUE #19](https://github.com/dangdangdotcom/sharding-jdbc/issues/19) sum和avg函数，不加别名不执行merger，加了空指针异常
1. [ISSUE #38](https://github.com/dangdangdotcom/sharding-jdbc/issues/38) JPA与Sharding-JDBC的兼容问题。JPA会自动增加SELECT的列别名，导致ORDER BY只能通过别名，而非列名称获取ResultSet的数据。

## 1.0.0
1. 初始版本。
