+++
pre = "<b>4.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
chapter = true
+++

## 简介

ShardingSphere-Proxy 是 Apache ShardingSphere 的第二个产品。
它定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
目前提供 MySQL 和 PostgreSQL 版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端(如：MySQL Command Client, MySQL Workbench, Navicat 等)操作数据，对 DBA 更加友好。

* 向应用程序完全透明，可直接当做 MySQL/PostgreSQL 使用。
* 适用于任何兼容 MySQL/PostgreSQL 协议的的客户端。

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy-brief.png)

## 对比

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy*  | *ShardingSphere-Sidecar* |
| --------- | --------------------- | ----------------------- | ------------------------ |
| 数据库     | 任意                  | `MySQL/PostgreSQL`      | MySQL/PostgreSQL          |
| 连接消耗数 | 高                    | `低`                     | 高                        |
| 异构语言   | 仅Java                | `任意`                   | 任意                      |
| 性能       | 损耗低                | `损耗略高`                | 损耗低                    |
| 无中心化   | 是                    | `否`                     | 是                        |
| 静态入口   | 无                    | `有`                     | 无                        |

ShardingSphere-Proxy 的优势在于对异构语言的支持，以及为 DBA 提供可操作入口。
