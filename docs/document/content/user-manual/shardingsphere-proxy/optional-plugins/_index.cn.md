+++
title = "可选插件"
weight = 6
+++

ShardingSphere 默认情况下仅加载核心 SPI 的实现。
二进制发布包会在 `opt-lib` 目录中随包提供一部分由 ShardingSphere 维护的可选插件，但这些插件不会默认加入 classpath。
若要启用这些插件，需要先将对应的 JAR 复制到 `ext-lib`，再按插件文档补充第三方依赖。
其他未随包提供的插件，可在 https://central.sonatype.com/ 进行检索。

所有插件对应的 SPI 和 SPI 的已有实现类均可在 https://shardingsphere.apache.org/document/current/cn/dev-manual/ 检索。
如果 ShardingSphere-Proxy 需要使用可选插件，需要按具体插件文档准备其 SPI 实现 JAR 和所依赖的第三方 JAR。

下以 `groupId:artifactId` 的表现形式列出所有的可选插件。

- 数据库方言适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-postgresql`，面向 Postgres 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-mysql`，面向 MySQL 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-sqlserver`，面向 MS SQL Server 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-mariadb`，面向 MariaDB 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-presto`，面向 Presto 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-oracle`，面向 Oracle Database Free 和 Oracle-XE 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-firebird`，面向 Firebird 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-clickhouse`，面向 Clickhouse 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-opengauss`，面向 openGauss 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-doris`，面向 Doris FE 的方言与兼容性适配
  - `org.apache.shardingsphere:shardingsphere-proxy-dialect-hive`，面向 HiveServer2 的方言与兼容性适配
- 数据源连接池
  - `org.apache.shardingsphere:shardingsphere-infra-data-source-pool-hikari`，提供 `dataSources.<data_source_name>.dataSourceClassName=com.zaxxer.hikari.HikariDataSource` 的连接池创建与属性适配实现
- 权限与认证实现
  - `org.apache.shardingsphere:shardingsphere-authority-simple`，提供 `authority.privilege.type=ALL_PERMITTED` 的权限与认证实现
  - `org.apache.shardingsphere:shardingsphere-authority-database`，提供 `authority.privilege.type=DATABASE_PERMITTED` 的权限与认证实现
- 分布式事务
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-atomikos`，提供 `transaction.providerType=Atomikos` 的分布式事务实现
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`，提供 `transaction.providerType=Narayana` 的分布式事务实现
  - `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at`，提供 `transaction.providerType=Seata` 的分布式事务实现
- SQL 翻译
  - `org.apache.shardingsphere:shardingsphere-sql-translator-native-provider`，提供 `sqlTranslator.type=Native` 的 SQL 翻译实现
- 配置与元数据持久化仓库
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-memory`，提供 `mode.repository.type=Memory` 的配置与元数据持久化仓库实现
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc`，提供 `mode.repository.type=JDBC` 的配置与元数据持久化仓库实现
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`，提供 `mode.repository.type=ZooKeeper` 的配置与元数据持久化仓库实现
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`，提供 `mode.repository.type=etcd` 的配置与元数据持久化仓库实现
- 行表达式
  - `org.apache.shardingsphere:shardingsphere-infra-expr-espresso`，提供 `<ESPRESSO>` 的行表达式实现
- DistSQL
  - `org.apache.shardingsphere:shardingsphere-sharding-distsql-handler`，提供 `!SHARDING` 相关 DistSQL 的执行端能力
  - `org.apache.shardingsphere:shardingsphere-broadcast-distsql-handler`，提供 `!BROADCAST` 相关 DistSQL 的执行端能力
  - `org.apache.shardingsphere:shardingsphere-readwrite-splitting-distsql-handler`，提供 `!READWRITE_SPLITTING` 相关 DistSQL 的执行端能力
  - `org.apache.shardingsphere:shardingsphere-shadow-distsql-handler`，提供 `!SHADOW` 相关 DistSQL 的执行端能力
  - `org.apache.shardingsphere:shardingsphere-encrypt-distsql-handler`，提供 `!ENCRYPT` 相关 DistSQL 的执行端能力
  - `org.apache.shardingsphere:shardingsphere-mask-distsql-handler`，提供 `!MASK` 相关 DistSQL 的执行端能力
- 功能核心的增强
  - `org.apache.shardingsphere:shardingsphere-sharding-mysql`，为 `shardingsphere-sharding-core` 提供面向 MySQL 的增强与适配
  - `org.apache.shardingsphere:shardingsphere-data-pipeline-feature-sharding`，为 `MIGRATION` 相关 DistSQL 提供分片场景的集成能力
- 时间服务
  - `org.apache.shardingsphere:shardingsphere-database-time-service`，通过连接数据库并执行 “取当前时间” 的 SQL 来提供统一时间戳，供分片等路由逻辑使用

除了以上可选插件外，ShardingSphere 社区开发者还贡献了大量的插件实现，可以在 [ShardingSphere Plugin](https://github.com/apache/shardingsphere-plugin) 仓库中查看插件的使用说明，ShardingSphere Plugin 仓库中的插件会和 ShardingSphere 保持相同的发布节奏，可以手动打包安装到 ShardingSphere 中。
