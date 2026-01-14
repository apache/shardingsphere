+++
title = "Optional Plugins"
weight = 6
+++

ShardingSphere only includes the implementation of the core SPI by default, and there is a part of the SPI that contains third-party dependencies in Git Source
Implemented plugins are not included. Retrievable at https://central.sonatype.com/.

SPI and existing implementation classes of SPI corresponding to all plugins can be retrieved at https://shardingsphere.apache.org/document/current/cn/dev-manual/.

If `org.apache.shardingsphere:shardingsphere-jdbc` requires optional plugins, you need to download the JAR containing its SPI implementation and its dependencies from Maven Central.

The following lists all optional plugins in the format of `groupId:artifactId`.

- Database dialect adaptation
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-postgresql`, dialect and compatibility adaptation for Postgres
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-mysql`, dialect and compatibility adaptation for MySQL
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-sqlserver`, dialect and compatibility adaptation for MS SQL Server
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-mariadb`, dialect and compatibility adaptation for MariaDB
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-presto`, dialect and compatibility adaptation for Presto
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-oracle`, dialect and compatibility adaptation for Oracle Database Free and Oracle-XE
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-firebird`, dialect and compatibility adaptation for Firebird
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-clickhouse`, dialect and compatibility adaptation for Clickhouse
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-opengauss`, dialect and compatibility adaptation for openGauss
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-doris`, dialect and compatibility adaptation for Doris FE
  - `org.apache.shardingsphere:shardingsphere-jdbc-dialect-hive`, dialect and compatibility adaptation for HiveServer2
- Data Source Connection Pool
  - `org.apache.shardingsphere:shardingsphere-infra-data-source-pool-hikari`, provides connection pool creation and property adaptation implementation for `dataSources.<data_source_name>.dataSourceClassName=com.zaxxer.hikari.HikariDataSource`
- Permissions and authentication implementation
  - `org.apache.shardingsphere:shardingsphere-authority-simple`, provides permission and authentication implementation with `authority.privilege.type=ALL_PERMITTED`
  - `org.apache.shardingsphere:shardingsphere-authority-database`, provides permission and authentication implementation with `authority.privilege.type=DATABASE_PERMITTED`
- Distributed transactions
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-atomikos`, provides distributed transaction implementation for `transaction.providerType=Atomikos`
  - `org.apache.shardingsphere:shardingsphere-transaction-xa-narayana`, provides distributed transaction implementation for `transaction.providerType=Narayana`
  - `org.apache.shardingsphere:shardingsphere-transaction-base-seata-at`, provides distributed transaction implementation for `transaction.providerType=Seata`
- SQL Translation
  - `org.apache.shardingsphere:shardingsphere-sql-translator-native-provider`, provides SQL translation implementation with `sqlTranslator.type=Native`
- Configuration and metadata persistence repository
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-memory`, provides configuration and metadata persistence repository implementation for `mode.repository.type=Memory`
  - `org.apache.shardingsphere:shardingsphere-standalone-mode-repository-jdbc`, provides configuration and metadata persistence repository implementation for `mode.repository.type=JDBC`
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-zookeeper`, provides configuration and metadata persistence repository implementation for `mode.repository.type=ZooKeeper`
  - `org.apache.shardingsphere:shardingsphere-cluster-mode-repository-etcd`, provides configuration and metadata persistence repository implementation for `mode.repository.type=etcd`
- Configuration loader
  - `org.apache.shardingsphere:shardingsphere-infra-url-classpath`, provides JDBC URL implementation of `jdbc:shardingsphere:classpath:`
  - `org.apache.shardingsphere:shardingsphere-infra-url-absolutepath`, provides JDBC URL implementation of `jdbc:shardingsphere:absolutepath:`
  - `org.apache.shardingsphere:shardingsphere-infra-url-zookeeper`, provides JDBC URL implementation of `jdbc:shardingsphere:zookeeper:`
  - `org.apache.shardingsphere:shardingsphere-infra-url-etcd`, provides JDBC URL implementation of `jdbc:shardingsphere:etcd:`
- Feature core
  - `org.apache.shardingsphere:shardingsphere-sharding-core`, provides core implementation of the `!SHARDING` feature
  - `org.apache.shardingsphere:shardingsphere-broadcast-core`, provides core implementation of the `!BROADCAST` feature
  - `org.apache.shardingsphere:shardingsphere-readwrite-splitting-core`, provides core implementation of the `!READWRITE_SPLITTING` feature
  - `org.apache.shardingsphere:shardingsphere-shadow-core`, provides core implementation of the `!SHADOW` feature
  - `org.apache.shardingsphere:shardingsphere-encrypt-core`, provides core implementation of the `!ENCRYPT` feature
  - `org.apache.shardingsphere:shardingsphere-mask-core`, provides core implementation of the `!MASK` feature
- Row Value Expressions
  - `org.apache.shardingsphere:shardingsphere-infra-expr-espresso`, provides row value expression implementation for `<ESPRESSO>`
- Enhancement of the feature core
  - `org.apache.shardingsphere:shardingsphere-sharding-mysql`, provides MySQL-oriented enhancements and adaptations for the core functionality of `shardingsphere-sharding-core`
- Time service
  - `org.apache.shardingsphere:shardingsphere-database-time-service`, provides a unified timestamp by connecting to the database and executing an SQL query to retrieve the current time, which is used by routing logic such as sharding
- GraalVM Native Image
  - `org.apache.shardingsphere:shardingsphere-infra-reachability-metadata`, provides GraalVM Reachability Metadata for ShardingSphere for `oracle/graal`

In addition to the above optional plugins, ShardingSphere community developers have contributed a number of plugin implementations. These plugins can be found in [ShardingSphere Plugins](https://github.com/apache/shardingsphere-plugin) repository. Plugins in ShardingSphere Plugin repository would remain the same release plan with ShardingSphere, you can build plugin jar by yourself, and install into ShardingSphere.
