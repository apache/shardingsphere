+++
pre = "<b>3.1.2. </b>"
title = "ShardingSphere-Proxy"
weight = 2
+++

## 简介

ShardingSphere-Proxy 是 Apache ShardingSphere 的第二个产品。
它定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
目前提供 MySQL 和 PostgreSQL（兼容 openGauss 等基于 PostgreSQL 的数据库）版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端（如：MySQL Command Client, MySQL Workbench, Navicat 等）操作数据，对 DBA 更加友好。

* 向应用程序完全透明，可直接当做 MySQL/PostgreSQL 使用。
* 适用于任何兼容 MySQL/PostgreSQL 协议的的客户端。

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

## 对比

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy*  |
| --------- | --------------------- | ----------------------- |
| 数据库     | 任意                  | `MySQL/PostgreSQL`      |
| 连接消耗数 | 高                    | `低`                     |
| 异构语言   | 仅Java                | `任意`                   |
| 性能       | 损耗低                | `损耗略高`                |
| 无中心化   | 是                    | `否`                     |
| 静态入口   | 无                    | `有`                     |

ShardingSphere-Proxy 的优势在于对异构语言的支持，以及为 DBA 提供可操作入口。
