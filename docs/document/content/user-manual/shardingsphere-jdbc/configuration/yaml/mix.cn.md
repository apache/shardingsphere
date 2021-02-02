+++
title = "混合规则"
weight = 6
+++

* 配置项说明

## 配置项说明
```yml
rules:
  - !SHARDING
    tables:
      t_order_item_calcite_sharding:
        actualDataNodes: calcite_ds.t_order_item_calcite_sharding_${0..1}
        tableStrategy:
          standard:
            shardingColumn: item_id
            shardingAlgorithmName: table_inline_item_id
      t_user_encrypt_calcite_sharding:
        actualDataNodes: calcite_ds.t_user_encrypt_calcite_sharding_${0..1}
        tableStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: table_inline_user_id
    shardingAlgorithms:
      table_inline_item_id:
        type: INLINE
        props:
          algorithm-expression: t_order_item_calcite_sharding_${item_id % 2}
      table_inline_user_id:
        type: INLINE
        props:
          algorithm-expression: t_user_encrypt_calcite_sharding_${user_id % 2}
  - !ENCRYPT
    encryptors:
      encryptor_aes:
        type: aes
        props:
          aes-key-value: 123456abc
    tables:
      t_user_encrypt_calcite:
        columns:
          pwd:
            plainColumn: plain_pwd
            cipherColumn: cipher_pwd
            encryptorName: encryptor_aes
      t_user_encrypt_calcite_sharding:
        columns:
          pwd:
            plainColumn: plain_pwd
            cipherColumn: cipher_pwd
            encryptorName: encryptor_aes
  - !REPLICA_QUERY
    dataSources:
      calcite_ds:
        name: calcite_ds
        primaryDataSourceName:
          - calcite_jdbc_1
        replicaDataSourceNames:
          - calcite_jdbc_2
        loadBalancerName: roundRobin
    loadBalancers:
      roundRobin:
        type: ROUND_ROBIN
props:
  sql-show: true
  query-with-cipher-column: true
```
