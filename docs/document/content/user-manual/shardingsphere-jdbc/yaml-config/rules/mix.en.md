+++
title = "Mixed Rules"
weight = 10
+++

## Background

ShardingSphere provides a variety of features, such as data sharding, read/write splitting, and data encryption. These features can be used independently or in combination. 
Below, you will find the parameters' explanation and configuration samples based on YAML.

## Parameters

```yaml
rules:
- !SHARDING
  tables:
    <logic_table_name>: # Logical table name:
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
    <sharding_algorithm_name>: # Sharding algorithm name
      type: INLINE
      props:
        algorithm-expression: # INLINE expression
    t_order_inline:
      type: INLINE
      props:
        algorithm-expression: # INLINE expression
  keyGenerators:
    <key_generate_algorithm_name> (+): # Distributed sequence algorithm name
      type: # Distributed sequence algorithm type
      props: # Property configuration of distributed sequence algorithm
- !ENCRYPT
  encryptors:
    <encrypt_algorithm_name> (+): # Encryption and decryption algorithm name
      type: # Encryption and decryption algorithm type
      props: # Encryption and decryption algorithm property configuration
    <encrypt_algorithm_name> (+): # Encryption and decryption algorithm name
      type: # Encryption and decryption algorithm type
  tables:
    <table_name>: # Encryption table name
      columns:
        <column_name> (+): # Encrypt logic column name
          cipher:
            name: # Cipher column name
            encryptorName: # Cipher encrypt algorithm name
          assistedQuery (?):
            name: # Assisted query column name
            encryptorName:  # Assisted query encrypt algorithm name
          likeQuery (?):
            name: # Like query column name
            encryptorName:  # Like query encrypt algorithm name 
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
- !ENCRYPT
  encryptors:
    aes_encryptor:
      type: AES
      props:
        aes-key-value: 123456abc
    assisted_encryptor:
      type: MD5
    like_encryptor:
      type: CHAR_DIGEST_LIKE
  tables:
    t_encrypt:
      columns:
        user_id:
          cipher:
            name: user_cipher
            encryptorName: aes_encryptor
          assistedQuery:
            name: assisted_query_user
            encryptorName: assisted_encryptor
          likeQuery:
            name: like_query_user
            encryptorName: like_encryptor
        order_id:
          cipher:
            name: order_cipher
            encryptorName: aes_encryptor
```
