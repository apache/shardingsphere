+++
pre = "<b>5.9. </b>"
title = "高可用"
weight = 9
chapter = true
+++

## DatabaseDiscoveryProviderAlgorithm

### 全限定类名

[`org.apache.shardingsphere.dbdiscovery.spi.DatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/api/src/main/java/org/apache/shardingsphere/dbdiscovery/spi/DatabaseDiscoveryProviderAlgorithm.java)

### 定义

数据库发现提供算法的定义

### 已知实现

| *配置标识*                     | *详细说明*                                     | *全限定类名*                  |
| ---------------------------- | -----------------------------------------------| ---------------------------- |
| MySQL.MGR                    | 基于 MySQL MGR 的数据库发现算法                   | [`org.apache.shardingsphere.dbdiscovery.mysql.type.MGRMySQLDatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/provider/mysql/src/main/java/org/apache/shardingsphere/dbdiscovery/mysql/type/MGRMySQLDatabaseDiscoveryProviderAlgorithm.java) |
| MySQL.NORMAL_REPLICATION     | 基于 MySQL 主从同步的数据库发现算法                | [`org.apache.shardingsphere.dbdiscovery.mysql.type.MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/provider/mysql/src/main/java/org/apache/shardingsphere/dbdiscovery/mysql/type/MySQLNormalReplicationDatabaseDiscoveryProviderAlgorithm.java) |
| openGauss.NORMAL_REPLICATION | 基于 openGauss 主从同步的数据库发现算法            | [`org.apache.shardingsphere.dbdiscovery.opengauss.OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm`](https://github.com/apache/shardingsphere/blob/master/features/db-discovery/provider/opengauss/src/main/java/org/apache/shardingsphere/dbdiscovery/opengauss/OpenGaussNormalReplicationDatabaseDiscoveryProviderAlgorithm.java) |
