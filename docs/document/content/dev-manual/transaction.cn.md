+++
pre = "<b>5.10. </b>"
title = "分布式事务"
weight = 10
chapter = true
+++

## ShardingSphereTransactionManager

### 全限定类名

[`org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/spi/ShardingSphereTransactionManager.java)

### 定义

分布式事务管理器

### 已知实现

| *配置标识* | *详细说明*                 | *全限定类名* |
| -------- | ------------------------- | ---------- |
| XA       | 基于 XA 的分布式事务管理器    |[`org.apache.shardingsphere.transaction.xa.XAShardingSphereTransactionManager`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/XAShardingSphereTransactionManager.java) |
| BASE     | 基于 Seata 的分布式事务管理器 |[`org.apache.shardingsphere.transaction.base.seata.at.SeataATShardingSphereTransactionManager`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/base/seata-at/src/main/java/org/apache/shardingsphere/transaction/base/seata/at/SeataATShardingSphereTransactionManager.java) |

## XATransactionManagerProvider

### 全限定类名

[`org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/spi/src/main/java/org/apache/shardingsphere/transaction/xa/spi/XATransactionManagerProvider.java)

### 定义

XA 分布式事务管理器

### 已知实现

| *配置标识* | *详细说明*                     | *全限定类名* |
| --------- | -------------------------------- | ---------- |
| Atomikos  | 基于 Atomikos 的 XA 分布式事务管理器 |[`org.apache.shardingsphere.transaction.xa.atomikos.manager.AtomikosTransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/provider/atomikos/src/main/java/org/apache/shardingsphere/transaction/xa/atomikos/manager/AtomikosTransactionManagerProvider.java)｜
| Narayana  | 基于 Narayana 的 XA 分布式事务管理器 |[`org.apache.shardingsphere.transaction.xa.narayana.manager.NarayanaXATransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/provider/narayana/src/main/java/org/apache/shardingsphere/transaction/xa/narayana/manager/NarayanaXATransactionManagerProvider.java)｜
| Bitronix  | 基于 Bitronix 的 XA 分布式事务管理器 |[`org.apache.shardingsphere.transaction.xa.bitronix.manager.BitronixXATransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/provider/bitronix/src/main/java/org/apache/shardingsphere/transaction/xa/bitronix/manager/BitronixXATransactionManagerProvider.java)｜

## XADataSourceDefinition

### 全限定类名

[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/XADataSourceDefinition.java)

### 定义

用于非 XA 数据源转化为 XA 数据源

### 已知实现 

| *配置标识*  | *详细说明*                                                | *全限定类名* |
| ---------- | ------------------------------------------------------- | ----------- |
| MySQL      | 非 XA 的 MySQL 数据源自动转化为 XA 的 MySQL 数据源           |[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MySQLXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/MySQLXADataSourceDefinition.java)｜
| MariaDB    | 非 XA 的 MariaDB 数据源自动转化为 XA 的 MariaDB 数据源       |[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MariaDBXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/MariaDBXADataSourceDefinition.java)｜
| PostgreSQL | 非 XA 的 PostgreSQL 数据源自动转化为 XA 的 PostgreSQL 数据源 |[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.PostgreSQLXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/PostgreSQLXADataSourceDefinition.java)｜
| Oracle     | 非 XA 的 Oracle 数据源自动转化为 XA 的 Oracle 数据源         |[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.OracleXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/OracleXADataSourceDefinition.java)｜
| SQLServer  | 非 XA 的 SQLServer 数据源自动转化为 XA 的 SQLServer 数据源   |[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.SQLServerXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/SQLServerXADataSourceDefinition.java)｜
| H2         | 非 XA 的 H2 数据源自动转化为 XA 的 H2 数据源                 |[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.H2XADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/H2XADataSourceDefinition.java)｜

## DataSourcePropertyProvider

### 全限定类名

[`org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourcePropertyProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/swapper/DataSourcePropertyProvider.java)

### 定义

用于获取数据源连接池的标准属性

### 已知实现

| *配置标识*                          | *详细说明*                       | *全限定类名* |
| ---------------------------------- | ------------------------------ | ---------- |
| com.zaxxer.hikari.HikariDataSource | 用于获取 HikariCP 连接池的标准属性 |[`org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl.HikariCPPropertyProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/swapper/impl/HikariCPPropertyProvider.java)｜
