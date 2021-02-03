+++
title = "Mixed Rules"
weight = 6
+++


The overlay between rule items in a mixed configuration is associated by the data source name and the table name.

If the previous rule is aggregation-oriented, the next rule needs to use the aggregated logical data source name configured by the previous rule when configuring the data source.
Similarly, if the previous rule is table aggregation-oriented, the next rule needs to use the aggregated logical table name configured by the previous rule when configuring the table.

## Configuration Item Explanation

```yml
dataSources: # Configure the real data source name.
  primary_ds:
    # ...Omit specific configuration.
  replica_ds_0:
    # ...Omit specific configuration.
  replica_ds_0:
    # ...Omit specific configuration.

rules:
  - !SHARDING # Configure data sharding rules.
    tables:
      t_user:
        actualDataNodes: ds.t_user_${0..1} # Data source name 'ds' uses the logical data source name of the read-write separation configuration.
        tableStrategy:
          standard:
            shardingColumn: user_id
            shardingAlgorithmName: t_user_inline
    shardingAlgorithms:
      t_user_inline:
        type: INLINE
        props:
          algorithm-expression: t_user_${user_id % 2}
  
  - !ENCRYPT # Configure data encryption rules.
    tables:
      t_user: # Table `t_user` is the name of the logical table that uses the data sharding configuration.
        columns:
          pwd:
            plainColumn: plain_pwd
            cipherColumn: cipher_pwd
            encryptorName: encryptor_aes
    encryptors:
      encryptor_aes:
        type: aes
        props:
          aes-key-value: 123456abc
  
  - !REPLICA_QUERY # Configure read-write separation rules.
    dataSources:
      ds:
        name: ds # The logical data source name 'ds' for read-write separation is used in data sharding.
        primaryDataSourceName: primary_ds # Use the real data source name 'primary_ds'.
        replicaDataSourceNames:
          - replica_ds_0 # Use the real data source name 'replica_ds_0'.
          - replica_ds_1 # Use the real data source name 'replica_ds_1'.
        loadBalancerName: roundRobin
    loadBalancers:
      roundRobin:
        type: ROUND_ROBIN

props:
  sql-show: true
  query-with-cipher-column: true
```
