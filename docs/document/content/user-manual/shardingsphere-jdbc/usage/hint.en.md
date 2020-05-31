+++
title = "Hint"
weight = 3
+++

## Introduction

ShardingSphere uses `ThreadLocal` to manage sharding key value or Hint route. Users can program to add sharding values to `HintManager`, and those values only take effect within the current thread. Main applications of Hint:

1. Sharding fields are not in SQL or table structure, but in external business logic.

2. Some operations forced to do in the master database.

## Sharding Based on Hint

### Hint Configuration

Hint algorithms require users to implement the interface of `org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm`. If ShardingSphere finds `TableRule` in LogicTable has used Hint, it will acquire sharding values from `HintManager` to route.

Take the following configurations for reference:

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
  defaultTableStrategy:
    none:
  defaultKeyGenerator:
    type: SNOWFLAKE
    column: order_id
props:
    sql.show: true
```

### Get HintManager

```java
HintManager hintManager = HintManager.getInstance();
```

### Add Sharding Value

- Use `hintManager.addDatabaseShardingValue` to add sharding key value of data source.
- Use `hintManager.addTableShardingValue` to add sharding key value of table.

> Users can use `hintManager.setDatabaseShardingValue` to add shardings in hint route to some certain sharding database without sharding tables. After that, SQL parse and rewrite phase will be skipped and the overall enforcement efficiency can be enhanced.

### Clean Sharding Values

Sharding values are saved in `ThreadLocal`, so it is necessary to use `hintManager.close()` to clean `ThreadLocal`.

**`HintManager` has implemented `AutoCloseable`. We recommend to close it automatically with `try with resource`.**

### Codes:

```java
// Sharding database and table with hintManager.
        String sql = "SELECT * FROM t_order";
        try (HintManager hintManager = HintManager.getInstance();
             Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            hintManager.addDatabaseShardingValue("t_order", 1);
            hintManager.addTableShardingValue("t_order", 2);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    // ...
                }
            }
        }

// Sharding database and one database route with hintManger.
        String sql = "SELECT * FROM t_order";
        try (HintManager hintManager = HintManager.getInstance();
             Connection conn = dataSource.getConnection();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
            hintManager.setDatabaseShardingValue(3);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                while (rs.next()) {
                    // ...
                }
            }
        }
```

## Forcible Master Database Route Based on Hint

### Get HintManager 

Be the same as sharding based on hint.

### Configure Master Database Route

- Use `hintManager.setMasterRouteOnly` to configure master database route.

### Clean Sharding Value

Be the same as data sharding based on hint.

### Codes:

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

### Example

[hint-example](https://github.com/apache/shardingsphere/tree/master/examples/shardingsphere-jdbc-example/other-feature-example/hint-example)