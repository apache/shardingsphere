+++
title = "RUL 语法"
weight = 4
chapter = true
+++

RUL (Resource Utility Language) 为 Apache ShardingSphere 的工具类语言，提供 SQL 解析、SQL 格式化、执行计划预览等功能。

## SQL 工具

| 语句                 | 说明                      | 示例                           |
|:--------------------|:-------------------------|:------------------------------|
| PARSE SQL           | 解析 SQL 并输出抽象语法树    | PARSE SELECT * FROM t_order   |
| FORMAT SQL          | 解析并输出格式化后的 SQL 语句 | FORMAT SELECT * FROM t_order  |
| PREVIEW SQL         | 预览 SQL 执行计划           | PREVIEW SELECT * FROM t_order |
