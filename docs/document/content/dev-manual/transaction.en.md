+++
pre = "<b>5.10. </b>"
title = "Distributed Transaction"
weight = 10
chapter = true
+++

## ShardingSphereTransactionManager

### Fully-qualified class name

[`org.apache.shardingsphere.transaction.spi.ShardingSphereTransactionManager`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/core/src/main/java/org/apache/shardingsphere/transaction/spi/ShardingSphereTransactionManager.java)

### Definition

ShardingSphere transaction manager service definition

### Implementation classes

| *Configuration Type* | *Description*                         | *Fully-qualified class name* |
| -------------------- | ------------------------------------- | ---------------------------- |
| XA                   | XA distributed transaction manager    | [`org.apache.shardingsphere.transaction.xa.XAShardingSphereTransactionManager`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/XAShardingSphereTransactionManager.java) |
| BASE                 | Seata distributed transaction manager | [`org.apache.shardingsphere.transaction.base.seata.at.SeataATShardingSphereTransactionManager`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/base/seata-at/src/main/java/org/apache/shardingsphere/transaction/base/seata/at/SeataATShardingSphereTransactionManager.java) |

## XATransactionManagerProvider

### Fully-qualified class name

[`org.apache.shardingsphere.transaction.xa.spi.XATransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/spi/src/main/java/org/apache/shardingsphere/transaction/xa/spi/XATransactionManagerProvider.java)

### Definition

XA transaction manager provider definition

### Implementation classes

| *Configuration Type* | *Description*                                        | *Fully-qualified class name* |
| -------------------- | ---------------------------------------------------- | ---------------------------- |
| Atomikos             | XA distributed transaction manager based on Atomikos | [`org.apache.shardingsphere.transaction.xa.atomikos.manager.AtomikosTransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/provider/atomikos/src/main/java/org/apache/shardingsphere/transaction/xa/atomikos/manager/AtomikosTransactionManagerProvider.java) |
| Narayana             | XA distributed transaction manager based on Narayana | [`org.apache.shardingsphere.transaction.xa.narayana.manager.NarayanaXATransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/provider/narayana/src/main/java/org/apache/shardingsphere/transaction/xa/narayana/manager/NarayanaXATransactionManagerProvider.java) |
| Bitronix             | XA distributed transaction manager based on Bitronix | [`org.apache.shardingsphere.transaction.xa.bitronix.manager.BitronixXATransactionManagerProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/provider/bitronix/src/main/java/org/apache/shardingsphere/transaction/xa/bitronix/manager/BitronixXATransactionManagerProvider.java) |

## XADataSourceDefinition

### Fully-qualified class name

[`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.XADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/XADataSourceDefinition.java)

### Definition

XA Data source definition

### Implementation classes

| *Configuration Type* | *Description*                                                           | *Fully-qualified class name* |
| -------------------- | ----------------------------------------------------------------------- | ---------------------------- |
| MySQL                | Auto convert Non XA MySQL data source to XA MySQL data source           | [`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MySQLXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/MySQLXADataSourceDefinition.java) |
| MariaDB              | Auto convert Non XA MariaDB data source to XA MariaDB data source       | [`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.MariaDBXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/MariaDBXADataSourceDefinition.java) |
| PostgreSQL           | Auto convert Non XA PostgreSQL data source to XA PostgreSQL data source | [`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.PostgreSQLXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/PostgreSQLXADataSourceDefinition.java) |
| Oracle               | Auto convert Non XA Oracle data source to XA Oracle data source         | [`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.OracleXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/OracleXADataSourceDefinition.java) |
| SQLServer            | Auto convert Non XA SQLServer data source to XA SQLServer data source   | [`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.SQLServerXADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/SQLServerXADataSourceDefinition.java) |
| H2                   | Auto convert Non XA H2 data source to XA H2 data source                 | [`org.apache.shardingsphere.transaction.xa.jta.datasource.properties.dialect.H2XADataSourceDefinition`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/properties/dialect/H2XADataSourceDefinition.java) |

## DataSourcePropertyProvider

### Fully-qualified class name

[`org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.DataSourcePropertyProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/swapper/DataSourcePropertyProvider.java)

### Definition

Data source property provider service definition

### Implementation classes

| *Configuration Type*               | *Description*                               | *Fully-qualified class name* |
| ---------------------------------- | ------------------------------------------- | ---------------------------- |
| com.zaxxer.hikari.HikariDataSource | Used to get standard properties of HikariCP | [`org.apache.shardingsphere.transaction.xa.jta.datasource.swapper.impl.HikariCPPropertyProvider`](https://github.com/apache/shardingsphere/blob/master/kernel/transaction/type/xa/core/src/main/java/org/apache/shardingsphere/transaction/xa/jta/datasource/swapper/impl/HikariCPPropertyProvider.java) |
