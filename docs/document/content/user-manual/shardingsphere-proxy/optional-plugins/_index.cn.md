+++
title = "可选插件"
weight = 6
+++

ShardingSphere 默认情况下仅包含核心 SPI 的实现，在 Git Source 存在一部分包含第三方依赖的 SPI
实现的插件未包含在内。可在 https://central.sonatype.com/ 进行检索。

所有插件对应的 SPI 和 SPI 的已有实现类均可在 https://shardingsphere.apache.org/document/current/cn/dev-manual/ 检索。

下以 `groupId:artifactId` 的表现形式列出 ShardingSphere-Proxy 所有的内置插件。

- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`，集群模式配置信息持久化定义的 etcd 实现
- `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`，集群模式配置信息持久化定义的 zookeeper 实现
- `org.apache.shardingsphere:shardingsphere-jdbc-core`， JDBC 核心模块
- `org.apache.shardingsphere:shardingsphere-db-protocol-core`，数据库协议核心
- `org.apache.shardingsphere:shardingsphere-mysql-protocol`，数据库协议的 MySQL 实现
- `org.apache.shardingsphere:shardingsphere-postgresql-protocol`，数据库协议的 PostgreSQL 实现
- `org.apache.shardingsphere:shardingsphere-opengauss-protocol`，数据库协议的 OpenGauss 实现
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-core`，用于 ShardingSphere-Proxy 解析与适配访问数据库的协议
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-mysql`，用于 ShardingSphere-Proxy 解析与适配访问数据库的协议的 MySQL 实现
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-postgresql`，用于 ShardingSphere-Proxy 解析与适配访问数据库的协议的 PostgreSQL 实现
- `org.apache.shardingsphere:shardingsphere-proxy-frontend-opengauss`，用于 ShardingSphere-Proxy 解析与适配访问数据库的协议的 openGauss 实现
- `org.apache.shardingsphere:shardingsphere-proxy-backend-core`， ShardingSphere Proxy 的后端核心模块
- `org.apache.shardingsphere:shardingsphere-standalone-mode-core`，单机模式配置信息持久化定义核心

对于核心的 `org.apache.shardingsphere:shardingsphere-jdbc-core`，其内置插件参考[ShardingSphere-JDBC 可选插件](/cn/user-manual/shardingsphere-jdbc/optional-plugins/)。

如果 ShardingSphere-Proxy 需要使用可选插件，需要在 Maven Central 下载包含其 SPI 实现的 JAR 和其依赖的 JAR。

下以 `groupId:artifactId` 的表现形式列出所有的可选插件。

- 单机模式配置信息持久化定义
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc`，基于 JDBC 的持久化
- 集群模式配置信息持久化定义
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-nacos`，基于 Nacos 的持久化实现
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-consul`，基于 Consul 的持久化实现
- XA 分布式事务管理器
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`，基于 Narayana 的 XA 分布式事务管理器
- SQL 翻译
  - `org.apache.shardingsphere:shardingsphere-sql-translator-jooq-provider`，使用 JooQ 的 SQL 翻译器
