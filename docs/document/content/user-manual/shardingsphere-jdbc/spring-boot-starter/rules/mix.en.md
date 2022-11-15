+++
title = "Mixed Rules"
weight = 8
+++

## Background

ShardingSphere provides a variety of features, such as data sharding, read/write splitting, high availability, and data decryption. These features can be used independently or in combination. 

Below, you will find the parameters' explanation and configuration samples based on SpringBoot Starter.

## Parameters

```properties
spring.shardingsphere.datasource.names= # Please refer to the user manual for the data source configuration
# Standard sharding table configuration
spring.shardingsphere.rules.sharding.tables.<table-name>.actual-data-nodes= # It consists of data source name plus table name, separated by decimal points. Multiple tables are separated by commas, and inline expression is supported. By default, a data node is generated with a known data source and logical table name, used for broadcast tables (that is, each database needs the same table for associated queries, mostly the dictionary table) or the situation when only database sharding is needed and all databases have the same table structure.
# Standard sharding scenarios used for a single shard key
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.standard.sharding-column= # Sharding column name
spring.shardingsphere.rules.sharding.tables.<table-name>.database-strategy.standard.sharding-algorithm-name= # sharding algorithm name
# Table shards strategy. The same as database shards strategy
spring.shardingsphere.rules.sharding.tables.<table-name>.table-strategy.xxx= # Omit
# Distributed sequence strategy configuration
spring.shardingsphere.rules.sharding.tables.<table-name>.key-generate-strategy.column= # Distributed sequence column name
spring.shardingsphere.rules.sharding.tables.<table-name>.key-generate-strategy.key-generator-name= # Distributed sequence algorithm name
# Sharding algorithm configuration
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.type= # Sharding algorithm type
spring.shardingsphere.rules.sharding.sharding-algorithms.<sharding-algorithm-name>.props.xxx= # Sharidng algorithm property configuration
# Distributed sequence algorithm configuration
spring.shardingsphere.rules.sharding.key-generators.<key-generate-algorithm-name>.type= # Distributed sequence algorithm type
spring.shardingsphere.rules.sharding.key-generators.<key-generate-algorithm-name>.props.xxx= # Property configuration of distributed sequence algorithm 
# Dynamic read/write splitting configuration
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.auto-aware-data-source-name= # logical data source name of database discovery
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.dynamic-strategy.write-data-source-query-enabled= # All the read databases went offline. Whether the primary database bears the read traffic.
spring.shardingsphere.rules.readwrite-splitting.data-sources.<readwrite-splitting-data-source-name>.load-balancer-name= # Load balancer algorithm name
# Database discovery configuration
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.data-source-names= # Data source name. Multiple data sources are separated by commas, such as ds_0, ds_1.
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.discovery-heartbeat-name= # Detect heartbeat name
spring.shardingsphere.rules.database-discovery.data-sources.<database-discovery-data-source-name>.discovery-type-name= # Database discovery type name
spring.shardingsphere.rules.database-discovery.discovery-heartbeats.<discovery-heartbeat-name>.props.keep-alive-cron= # cron expression, such as '0/5 * * * * ?'.
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.type= # Database discovery type, such as MySQL.MGR.
spring.shardingsphere.rules.database-discovery.discovery-types.<discovery-type-name>.props.group-name= # Required parameter of database discovery type, such as MGR's group-name.
# Data desensitization configuration
spring.shardingsphere.rules.encrypt.tables.<table-name>.query-with-cipher-column= # Whether the table uses cipher columns for query
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.plain-column= # Plain column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.cipher-column= # Cipher column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.encryptor-name= # Encrypt algorithm name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-column= # Assisted query column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.assisted-query-encryptor-name# Assisted query encrypt algorithm name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.like-query-column= # Like query column name
spring.shardingsphere.rules.encrypt.tables.<table-name>.columns.<column-name>.like-query-encryptor-name# Like query encrypt algorithm name
# Encryption algorithm configuration
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.type= # Encryption algorithm type
spring.shardingsphere.rules.encrypt.encryptors.<encrypt-algorithm-name>.props.xxx= # Encryption algorithm property configuration
spring.shardingsphere.rules.encrypt.queryWithCipherColumn= # Whether use ciphercolumn for queries. You can use the plaincolumn for queries if it's available.
```

## Samples

```properties
# Sharding configuration
spring.shardingsphere.rules.sharding.tables.t_order.actual-data-nodes=replica-ds-$->{0..1}.t_order_$->{0..1}
spring.shardingsphere.rules.sharding.tables.t_order.table-strategy.standard.sharding-column=order_id
spring.shardingsphere.rules.sharding.tables.t_order.table-strategy.standard.sharding-algorithm-name=t-order-inline
spring.shardingsphere.rules.sharding.tables.t_order.key-generate-strategy.column=order_id
spring.shardingsphere.rules.sharding.tables.t_order.key-generate-strategy.key-generator-name=snowflake
spring.shardingsphere.rules.sharding.tables.t_order_item.actual-data-nodes=replica-ds-$->{0..1}.t_order_item_$->{0..1}
spring.shardingsphere.rules.sharding.tables.t_order_item.table-strategy.standard.sharding-column=order_id
spring.shardingsphere.rules.sharding.sharding-algorithms.database-inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.database-inline.props.algorithm-expression=replica_ds-$->{user_id % 2}
spring.shardingsphere.rules.sharding.sharding-algorithms.t-order-inline.type=INLINE
spring.shardingsphere.rules.sharding.sharding-algorithms.t-order-inline.props.algorithm-expression=t_order_$->{order_id % 2}
spring.shardingsphere.rules.sharding.key-generators.snowflake.type=SNOWFLAKE
# Dynamic read/write splitting configuration
spring.shardingsphere.rules.readwrite-splitting.data-sources.replica-ds-0.dynamic-strategy.auto-aware-data-source-name=readwrite-ds-0
spring.shardingsphere.rules.readwrite-splitting.data-sources.replica-ds-1.dynamic-strategy.auto-aware-data-source-name=readwrite-ds-1
# Database discovery configuration
spring.shardingsphere.rules.database-discovery.data-sources.readwrite-ds-0.data-source-names=ds-0, ds-1, ds-2
spring.shardingsphere.rules.database-discovery.data-sources.readwrite-ds-0.discovery-heartbeat-name=mgr-heartbeat
spring.shardingsphere.rules.database-discovery.data-sources.readwrite-ds-0.discovery-type-name=mgr
spring.shardingsphere.rules.database-discovery.data-sources.readwrite-ds-1.data-source-names=ds-3, ds-4, ds-5
spring.shardingsphere.rules.database-discovery.data-sources.readwrite-ds-1.discovery-heartbeat-name=mgr-heartbeat
spring.shardingsphere.rules.database-discovery.data-sources.readwrite-ds-1.discovery-type-name=mgr
spring.shardingsphere.rules.database-discovery.discovery-heartbeats.mgr-heartbeat.props.keep-alive-cron=0/5 * * * * ?
spring.shardingsphere.rules.database-discovery.discovery-types.mgr.type=MGR
spring.shardingsphere.rules.database-discovery.discovery-types.mgr.props.groupName=b13df29e-90b6-11e8-8d1b-525400fc3996
# Data desensitization configuration
spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.name-encryptor.props.aes-key-value=123456abc
spring.shardingsphere.rules.encrypt.encryptors.name-assisted-encryptor.type=AES
spring.shardingsphere.rules.encrypt.encryptors.name-assisted-encryptor.props.aes-key-value=123456abc
spring.shardingsphere.rules.encrypt.encryptors.name-like-encryptor.type=CHAR_DIGEST_LIKE
spring.shardingsphere.rules.encrypt.encryptors.name-like-encryptor.props.delta=2
spring.shardingsphere.rules.encrypt.encryptors.pwd-encryptor.type=AES
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.cipher-column=username
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.encryptor-name=name-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.assisted-query-column=username_assisted
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.assisted-query-encryptor-name=name-assisted-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.like-query-column=username_like
spring.shardingsphere.rules.encrypt.tables.t_user.columns.username.like-query-encryptor-name=name-like-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.cipher-column=pwd
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.encryptor-name=pwd-encryptor
spring.shardingsphere.rules.encrypt.tables.t_user.query-with-cipher-column=true
spring.shardingsphere.rules.encrypt.tables.t_user.columns.pwd.query-with-cipher-column=false
spring.shardingsphere.props.sql-show=true
```
