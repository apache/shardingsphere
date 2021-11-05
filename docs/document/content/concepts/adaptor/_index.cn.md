+++
pre = "<b>3.1. </b>"
title = "接入端"
weight = 1
chapter = true
+++

Apache ShardingSphere 由 ShardingSphere-JDBC 和 ShardingSphere-Proxy 这 2 款既能够独立部署，又支持混合部署配合使用的产品组成。
它们均提供标准化的基于数据库作为存储节点的增量功能，可适用于如 Java 同构、异构语言、云原生等各种多样化的应用场景。

## ShardingSphere-JDBC

ShardingSphere-JDBC 是 Apache ShardingSphere 的第一个产品，也是 Apache ShardingSphere 的前身。
定位为轻量级 Java 框架，在 Java 的 JDBC 层提供的额外服务。
它使用客户端直连数据库，以 jar 包形式提供服务，无需额外部署和依赖，可理解为增强版的 JDBC 驱动，完全兼容 JDBC 和各种 ORM 框架。

* 适用于任何基于 JDBC 的 ORM 框架，如：JPA, Hibernate, Mybatis, Spring JDBC Template 或直接使用 JDBC；
* 支持任何第三方的数据库连接池，如：DBCP, C3P0, BoneCP, HikariCP 等；
* 支持任意实现 JDBC 规范的数据库，目前支持 MySQL，PostgreSQL，Oracle，SQLServer 以及任何可使用 JDBC 访问的数据库。

![ShardingSphere-JDBC Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-jdbc_v3.png)

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy* |
| --------- | --------------------- | ---------------------- |
| 数据库     | `任意`                | MySQL/PostgreSQL       |
| 连接消耗数 | `高`                  | 低                      |
| 异构语言   | `仅Java`              | 任意                    |
| 性能       | `损耗低`              | 损耗略高                |
| 无中心化   | `是`                  | 否                     |
| 静态入口   | `无`                  | 有                     |

ShardingSphere-JDBC 的优势在于对 Java 应用的友好度。

## ShardingSphere-Proxy

ShardingSphere-Proxy 是 Apache ShardingSphere 的第二个产品。
它定位为透明化的数据库代理端，提供封装了数据库二进制协议的服务端版本，用于完成对异构语言的支持。
目前提供 MySQL 和 PostgreSQL（兼容 openGauss 等基于 PostgreSQL 的数据库）版本，它可以使用任何兼容 MySQL/PostgreSQL 协议的访问客户端（如：MySQL Command Client, MySQL Workbench, Navicat 等）操作数据，对 DBA 更加友好。

* 向应用程序完全透明，可直接当做 MySQL/PostgreSQL 使用；
* 适用于任何兼容 MySQL/PostgreSQL 协议的的客户端。

![ShardingSphere-Proxy Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-proxy_v2.png)

|           | *ShardingSphere-JDBC* | *ShardingSphere-Proxy*  |
| --------- | --------------------- | ----------------------- |
| 数据库     | 任意                  | `MySQL/PostgreSQL`      |
| 连接消耗数 | 高                    | `低`                     |
| 异构语言   | 仅Java                | `任意`                   |
| 性能       | 损耗低                | `损耗略高`                |
| 无中心化   | 是                    | `否`                     |
| 静态入口   | 无                    | `有`                     |

ShardingSphere-Proxy 的优势在于对异构语言的支持，以及为 DBA 提供可操作入口。

## 混合架构

ShardingSphere-JDBC 采用无中心化架构，与应用程序共享资源，适用于 Java 开发的高性能的轻量级 OLTP 应用；
ShardingSphere-Proxy 提供静态入口以及异构语言的支持，独立于应用程序部署，适用于 OLAP 应用以及对分片数据库进行管理和运维的场景。

Apache ShardingSphere 是多接入端共同组成的生态圈。
通过混合使用 ShardingSphere-JDBC 和 ShardingSphere-Proxy，并采用同一注册中心统一配置分片策略，能够灵活的搭建适用于各种场景的应用系统，使得架构师更加自由地调整适合与当前业务的最佳系统架构。

![Hybrid Architecture](https://shardingsphere.apache.org/document/current/img/shardingsphere-hybrid-architecture_v2.png)
