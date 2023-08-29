+++
title = "可选插件"
weight = 6
+++

ShardingSphere 默认情况下仅包含核心 SPI 的实现，在 Git Source 存在一部分包含第三方依赖的 SPI
实现的插件未包含在内。可在 https://central.sonatype.com/ 进行检索。

所有插件对应的 SPI 和 SPI 的已有实现类均可在 https://shardingsphere.apache.org/document/current/cn/dev-manual/ 检索。

下以 `groupId:artifactId` 的表现形式列出 ShardingSphere-JDBC 所有的内置插件。

- `org.apache.shardingsphere:shardingsphere-authority-core`，用户权限加载逻辑核心
- `org.apache.shardingsphere:shardingsphere-cluster-mode-core`，集群模式配置信息持久化定义核心
- `org.apache.shardingsphere:shardingsphere-db-discovery-core`，高可用核心
- `org.apache.shardingsphere:shardingsphere-encrypt-core`，数据加密核心
- `org.apache.shardingsphere:shardingsphere-encrypt-sm`， SM 加密算法
- `org.apache.shardingsphere:shardingsphere-infra-context`， Context 的内核运行与元数据刷新机制
- `org.apache.shardingsphere:shardingsphere-logging-core`，日志记录核心
- `org.apache.shardingsphere:shardingsphere-mask-core`，数据脱敏核心
- `org.apache.shardingsphere:shardingsphere-mysql-dialect-exception`， 数据库网关的 MySQL 实现
- `org.apache.shardingsphere:shardingsphere-parser-core`， SQL 解析核心
- `org.apache.shardingsphere:shardingsphere-postgresql-dialect-exception`，数据库网关的 PostgreSQL 实现
- `org.apache.shardingsphere:shardingsphere-readwrite-splitting-core`，读写分离核心
- `org.apache.shardingsphere:shardingsphere-shadow-core`，影子库核心
- `org.apache.shardingsphere:shardingsphere-sharding-core`，数据分片核心
- `org.apache.shardingsphere:shardingsphere-single-core`，单表（所有的分片数据源中仅唯一存在的表）核心
- `org.apache.shardingsphere:shardingsphere-sql-federation-core`，联邦查询执行器核心
- `org.apache.shardingsphere:shardingsphere-sql-parser-mysql`， SQL 解析的 MySQL 方言实现
- `org.apache.shardingsphere:shardingsphere-sql-parser-opengauss`， SQL 解析的 OpenGauss 方言实现
- `org.apache.shardingsphere:shardingsphere-sql-parser-oracle`， SQL 解析的 Oracle 方言解析实现
- `org.apache.shardingsphere:shardingsphere-sql-parser-postgresql`， SQL 解析的 PostgreSQL 方言实现
- `org.apache.shardingsphere:shardingsphere-sql-parser-sql92`， SQL 解析的 SQL 92 方言实现
- `org.apache.shardingsphere:shardingsphere-sql-parser-sqlserver`， SQL 解析的 SQL Server 方言实现
- `org.apache.shardingsphere:shardingsphere-standalone-mode-core`，单机模式配置信息持久化定义核心
- `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc-h2`，单机模式配置信息持久化定义的 H2 实现
- `org.apache.shardingsphere:shardingsphere-traffic-core`，流量治理核心
- `org.apache.shardingsphere:shardingsphere-transaction-core`， XA 分布式事务管理器核心

如果 ShardingSphere-JDBC 需要使用可选插件，需要在 Maven Central 下载包含其 SPI 实现的 JAR 和其依赖的 JAR。

下以 `groupId:artifactId` 的表现形式列出所有的可选插件。

- 单机模式配置信息持久化定义
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc-mysql`，基于 MySQL 的持久化
- 集群模式配置信息持久化定义
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`，基于 Zookeeper 的持久化实现
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`，基于 Etcd 的持久化实现
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-nacos`，基于 Nacos 的持久化实现
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-consul`，基于 Consul 的持久化实现
- XA 分布式事务管理器
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`，基于 Narayana 的 XA 分布式事务管理器
- SQL 翻译
  - `org.apache.shardingsphere:shardingsphere-sql-translator-jooq-provider`，使用 JooQ 的 SQL 翻译器
