+++
title = "Mixed Rules"
weight = 9
+++

## Background

ShardingSphere provides a variety of features, such as data sharding, read/write splitting, high availability, and data decryption. These features can be used independently or in combination. 
Below, you will find the parameters' explanation and configuration samples based on YAML.

## Parameters

```yaml
rules:
  - !SHARDING
    tables:
      <logic-table-name>: # Logical table name:
        actualDataNodes: # consists of logical data source name plus table name (refer to Inline syntax rules)
        tableStrategy: # Table shards strategy. The same as database shards strategy
          standard:
            shardingColumn: # Sharding column name
            shardingAlgorithmName: # Sharding algorithm name
        keyGenerateStrategy:
          column: # Auto-increment column name. By default, the auto-increment primary key generator is not used.
          keyGeneratorName: # Distributed sequence algorithm name
    defaultDatabaseStrategy:
      standard:
        shardingColumn: # Sharding column name
        shardingAlgorithmName: # Sharding algorithm name
    shardingAlgorithms:
      <sharding-algorithm-name>: # Sharding algorithm name
        type: INLINE
        props:
          algorithm-expression: # INLINE expression
      t_order_inline:
        type: INLINE
        props:
          algorithm-expression: # INLINE expression
    keyGenerators:
      <key-generate-algorithm-name> (+): # Distributed sequence algorithm name
        type: # Distributed sequence algorithm type
        props: # Property configuration of distributed sequence algorithm
  - !READWRITE_SPLITTING
    dataSources:
      <data-source-name>: # Read/write splitting logical data source name
        dynamicStrategy: # Read/write splitting type
          autoAwareDataSourceName: # Database discovery logical data source name
      <data-source-name>: # Read/write splitting logical data source name
        dynamicStrategy: # Read/write splitting type
          autoAwareDataSourceName: # Database discovery logical data source name
  - !DB_DISCOVERY
    dataSources:
      <data-source-name>:
        dataSourceNames: # Data source name list
          - ds_0
          - ds_1
          - ds_2
        discoveryHeartbeatName: # Detect heartbeat name
        discoveryTypeName: # Database discovery type name
      <data-source-name>:
        dataSourceNames: # Data source name list
          - ds_3
          - ds_4
          - ds_5
        discoveryHeartbeatName: # Detect heartbeat name
        discoveryTypeName: # Database discovery type name
    discoveryHeartbeats:
      <discovery-heartbeat-name>: # Heartbeat name
        props:
          keep-alive-cron: # cron expression, such as '0/5 * * * * ?'
    discoveryTypes:
      <discovery-type-name>: # Database discovery type name
        type: # Database discovery type, such as MySQL.MGR. 
        props:
          group-name:  # Required parameter of database discovery type, such as MGR's group-name.
  - !ENCRYPT
    encryptors:
      <encrypt-algorithm-name> (+): # Encryption and decryption algorithm name
        type: # Encryption and decryption algorithm type
        props: # Encryption and decryption algorithm property configuration
      <encrypt-algorithm-name> (+): # Encryption and decryption algorithm name
        type: # Encryption and decryption algorithm type
    tables:
      <table-name>: # Encryption table name
        columns:
          <column-name>: # Encryption name
            plainColumn: # Plaincolumn name
            cipherColumn: # Ciphercolumn name
            encryptorName: # Encryption algorithm name
          <column-name>: # Encryption column name
            cipherColumn: # Ciphercolumn name
            encryptorName:  # Encryption algorithm name
```

## Samples

```yaml
rules:
  - !SHARDING
    tables:
      t_order:
        actualDataNodes: replica_ds_${0..1}.t_order_${0..1}
        tableStrategy:
          standard:
            shardingColumn: order_id
            shardingAlgorithmName: t_order_inline
        keyGenerateStrategy:
          column: order_id
          keyGeneratorName: snowflake
    defaultDatabaseStrategy:
      standard:
        shardingColumn: user_id
        shardingAlgorithmName: database_inline
    shardingAlgorithms:
      database_inline:
        type: INLINE
        props:
          algorithm-expression: replica_ds_${user_id % 2}
      t_order_inline:
        type: INLINE
        props:
          algorithm-expression: t_order_${order_id % 2}
      t_order_item_inline:
        type: INLINE
        props:
          algorithm-expression: t_order_item_${order_id % 2}
    keyGenerators:
      snowflake:
        type: SNOWFLAKE
  - !READWRITE_SPLITTING
    dataSources:
      replica_ds_0:
        dynamicStrategy:
          autoAwareDataSourceName: readwrite_ds_0
      replica_ds_1:
        dynamicStrategy:
          autoAwareDataSourceName: readwrite_ds_1
  - !DB_DISCOVERY
    dataSources:
      readwrite_ds_0:
        dataSourceNames:
          - ds_0
          - ds_1
          - ds_2
        discoveryHeartbeatName: mgr-heartbeat
        discoveryTypeName: mgr
      readwrite_ds_1:
        dataSourceNames:
          - ds_3
          - ds_4
          - ds_5
        discoveryHeartbeatName: mgr-heartbeat
        discoveryTypeName: mgr
    discoveryHeartbeats:
      mgr-heartbeat:
        props:
          keep-alive-cron: '0/5 * * * * ?'
    discoveryTypes:
      mgr:
        type: MySQL.MGR
        props:
          group-name: 558edd3c-02ec-11ea-9bb3-080027e39bd2
  - !ENCRYPT
    encryptors:
      aes_encryptor:
        type: AES
        props:
          aes-key-value: 123456abc
      md5_encryptor:
        type: MD5
    tables:
      t_encrypt:
        columns:
          user_id:
            plainColumn: user_plain
            cipherColumn: user_cipher
            encryptorName: aes_encryptor
          order_id:
            cipherColumn: order_cipher
            encryptorName: md5_encryptor
```
