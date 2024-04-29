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
- `org.apache.shardingsphere:shardingsphere-jdbc`， JDBC 模块
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

对于核心的 `org.apache.shardingsphere:shardingsphere-jdbc`，其内置插件参考[ShardingSphere-JDBC 可选插件](/cn/user-manual/shardingsphere-jdbc/optional-plugins/)。

如果 ShardingSphere-Proxy 需要使用可选插件，需要在 Maven Central 下载包含其 SPI 实现的 JAR 和其依赖的 JAR。

下以 `groupId:artifactId` 的表现形式列出所有的可选插件。

- 单机模式配置信息持久化定义
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc`，基于 JDBC 的持久化
- XA 分布式事务管理器
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`，基于 Narayana 的 XA 分布式事务管理器
- 行表达式
  - `org.apache.shardingsphere:shardingsphere-infra-expr-espresso`，基于 GraalVM Truffle 的 Espresso 实现的使用 Groovy 语法的行表达式
- 数据库类型识别
  - `org.apache.shardingsphere:shardingsphere-infra-database-testcontainers`， 对 `testcontainers-java` 的 `JDBC support` 的 jdbcURL 的识别适配

除了以上可选插件外，ShardingSphere 社区开发者还贡献了大量的插件实现，可以在 [ShardingSphere Plugin](https://github.com/apache/shardingsphere-plugin) 仓库中查看插件的使用说明，ShardingSphere Plugin 仓库中的插件会和 ShardingSphere 保持相同的发布节奏，可以在 https://central.sonatype.com/ 进行检索，并安装到 ShardingSphere 中。
