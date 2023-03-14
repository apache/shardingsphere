+++
pre = "<b>5.9. </b>"
title = "HA"
weight = 9
chapter = true
+++

## DatabaseDiscoveryProvider

### Fully-qualified class name

[`org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProvider`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/api/src/main/java/org/apache/shardingsphere/dbdiscovery/spi/DatabaseDiscoveryProvider.java)

### Definition

Database discovery provider algorithm's definition

### Implementation classes

| *Configuration Type*         | *Description*                                                    | *Fully-qualified class name* |
| ---------------------------- | ---------------------------------------------------------------- | ---------------------------- |
| MySQL.MGR                    | MySQL MGR-based database discovery provider algorithm            | [`org.apache.shardingsphere.dbdiscovery.mysql.type.MGRMySQLDatabaseDiscoveryProvider`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/provider/mysql/src/main/java/org/apache/shardingsphere/dbdiscovery/mysql/type/MGRMySQLDatabaseDiscoveryProvider.java) |
| MySQL.NORMAL_REPLICATION     | Database discovery provider algorithm of MySQL’s replication     | [`org.apache.shardingsphere.dbdiscovery.mysql.type.MySQLNormalReplicationDatabaseDiscoveryProvider`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/provider/mysql/src/main/java/org/apache/shardingsphere/dbdiscovery/mysql/type/MySQLNormalReplicationDatabaseDiscoveryProvider.java) |
| openGauss.NORMAL_REPLICATION | Database discovery provider algorithm of openGauss’s replication | [`org.apache.shardingsphere.dbdiscovery.opengauss.OpenGaussNormalReplicationDatabaseDiscoveryProvider`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/provider/opengauss/src/main/java/org/apache/shardingsphere/dbdiscovery/opengauss/OpenGaussNormalReplicationDatabaseDiscoveryProvider.java) |

