+++
toc = true
title = "Hint"
weight = 3
+++

## Introduction

ShardingSphere uses `ThreadLocal` to manage sharding key value or hint route. 
Users can program to add sharding conditions to `HintManager`, and the condition only take effect within the current thread. 
Main application situations of Hint:

1. Sharding field does not exist in SQL or database table structure, but in external business logic. 
So users can operate data according to external sharding results designated by hint.

2. Force to do some data operations in the master database.

## Data Sharding Based on Hint

### configuration

It needs to use `HintManager` along with sharding strategy configurations when using hint to enforce data sharding. 
If `DatabaseShardingStrategy` is configured with hint sharding algorithms, users can use `HintManager` to inject sharding database results. 
In a similar way, if `TableShardingStrategy` is configured with hint sharding algorithms, users can also use `HintManager` to inject sharding table results. 
So it is necessary to configure hint sharding algorithms before using hint.

Here are codes to refer to:

```yaml
shardingRule:
  tables:
   t_order:
        actualDataNodes: demo_ds_${0..1}.t_order_${0..1}
        databaseStrategy:
          hint:
            algorithmClassName: org.apache.shardingsphere.userAlgo.HintAlgorithm
        tableStrategy:
          hint:
            algorithmClassName: org.apache.shardingsphere.userAlgo.HintAlgorithm
  defaultDatabaseStrategy:
    inline:
      shardingColumn: user_id
      algorithmExpression: demo_ds_${user_id % 2}
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
  props:
      sql.show: true
```

### Instantiation

```java
HintManager hintManager = HintManager.getInstance();
```

### Add Sharding Key Value

- Use `hintManager.addDatabaseShardingValue` to add sharding key value of data source.
- Use `hintManager.addTableShardingValue` to add sharding key value of table.

> Users can use `hintManager.setDatabaseShardingValue` to add shards in hint route to some certain sharding database without sharding tables. 
In this way, SQL parsing and rewriting phase will be skipped and the overall enforcement efficiency can be enhanced.

### Clean sharding columns and sharding values

Sharding keys are saved in `ThreadLocal`, so it is necessary to use `hintManager.close()` to clear the content in `ThreadLocal`.

__`HintManager` is implemented with `AutoCloseable` interface. Recommend to use `try with resource` to automatically close it.__

### Code example:

```java
String sql = "SELECT * FROM t_order";
        
try (
        HintManager hintManager = HintManager.getInstance();
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.addDatabaseShardingValue("t_order", "user_id", 1);
    hintManager.addTableShardingValue("t_order", "order_id", 2);
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            ...
        }
    }
}
```

## Forcible Master Database Route Based on Hint

### Instantiation

Be the same as data sharding based on hint.

### Configure Master Database Route

- Use `hintManager.setMasterRouteOnly` to configure master database route.

### Clear Sharding Key Value

Be the same as data sharding based on hint.

### Code example:

```java
String sql = "SELECT * FROM t_order";
try (
        HintManager hintManager = HintManager.getInstance();
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setMasterRouteOnly();
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```
