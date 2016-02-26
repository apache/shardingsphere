+++
date = "2016-02-05T17:03:18+08:00"
title = "Release Notes"
weight = 9
+++

# Release Notes

## 1.0.1-snapshot
1. 修正JPA与Sharding-JDBC的兼容问题。JPA会自动增加SELECT的列别名，导致ORDER BY只能通过别名，而非列名称获取ResultSet的数据。
1. 修正[issue #11](https://github.com/dangdangdotcom/sharding-jdbc/issues/11)
1. 修正[issue #13](https://github.com/dangdangdotcom/sharding-jdbc/issues/13)
1. 修正[issue #16](https://github.com/dangdangdotcom/sharding-jdbc/issues/16)
1. 修正[issue #18](https://github.com/dangdangdotcom/sharding-jdbc/issues/18)

## 1.0.0
1. 初始版本。
