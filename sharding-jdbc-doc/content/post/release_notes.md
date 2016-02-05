+++
date = "2016-02-05T17:03:18+08:00"
title = "Release Notes"
weight = 9
+++

# Release Notes

## 1.0.1
1. 修复jpa生成SQL语句与Sharding-JDBC之间的兼容问题。该问题表现为：在order by语句中，jpa会增加select中列的别名，该别名无法正确被Sharding-JDBC识别。