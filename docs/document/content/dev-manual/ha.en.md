+++
pre = "<b>5.9. </b>"
title = "HA"
weight = 9
chapter = true
+++

## DatabaseDiscoveryProviderAlgorithm

### Fully-qualified class name

[`org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-api/src/main/java/org/apache/shardingsphere/dbdiscovery/spi/DatabaseDiscoveryProviderAlgorithm.java)

### Definition

Database discovery provider algorithm's definition

### Implementation classes

| *Configuration Type*         | *Description*                                                    | *Fully-qualified class name* |
| ---------------------------- | ---------------------------------------------------------------- | ---------------------------- |
| MySQL.MGR                    | MySQL MGR-based database discovery provider algorithm            | [`org.apache.shardingsphere.dbdiscovery.mysql.type.MGRMySQLDatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-mysql/src/main/java/org/apache/shardingsphere/dbdiscovery/mysql/type/MGRMySQLDatabaseDiscoveryProviderAlgorithm.java) |
| MySQL.NORMAL_REPLICATION     | Database discovery provider algorithm of MySQL’s replication     | [`org.apache.shardingsphere.dbdiscovery.mysql.type.MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-mysql/src/main/java/org/apache/shardingsphere/dbdiscovery/mysql/type/MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm.java) |
| openGauss.NORMAL_REPLICATION | Database discovery provider algorithm of openGauss’s replication | [`org.apache.shardingsphere.dbdiscovery.opengauss.OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/shardingsphere-features/shardingsphere-db-discovery/shardingsphere-db-discovery-provider/shardingsphere-db-discovery-opengauss/src/main/java/org/apache/shardingsphere/dbdiscovery/opengauss/OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm.java) |

