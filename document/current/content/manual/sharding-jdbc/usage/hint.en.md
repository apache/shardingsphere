+++
toc = true
title = "Hint"
weight = 3
+++

## Introduction

ShardingSphere uses ThreadLocal to manage sharding-columns and sharding-values. Developers can use HintManager to add sharding conditions by coding, sharding conditions effective only on current thread.

Main usages of Hint:

1. Sharding column does not exist in SQL or tables in databases, but exist in external business logic. Therefore, it is possible to get the sharding result by Hint.

2. Mandatory Master routing strategy based in Hint.

## Sharding with hint

### configuration

Hint is used for mandatory data sharding, which requires HintManager to be used together with database or table ShardingStrategy. If configuring DatabaseShardingStrategy with hint strategy, HintManager can be used to inject the database sharding value. 
Similarly, if configuring TableShardingStrategy with hint strategy, you can inject table sharding values using HintManager. Therefore, before Hint is used, Hint sharding strategy needs to be configured.

The code is as follows:

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

### Add sharding columns and sharding values

- To add Sharding columns and corresponding data of data source by hintManager.addDatabaseShardingValue.
- To add Sharding columns and corresponding data of table by hintManager.addTableShardingValue.

> In the case of sharding databases without sharding tables, you can use `hintManager.setDatabaseShardingValue` method to add sharding value to force routing to only one database. 
In this way, the SQL parsing and rewriting phases are skipped to improve overall execution efficiency.

### Clean sharding columns and sharding values

The added Sharding columns and corresponding data are saved in ThreadLocal, so you need to clean the content of the ThreadLocal by calling hintManager.close() after the end of the operations.

__hintManager implement AutoCloseable interface, and it is recommended to use the *Try with resource* to clean automatically.__

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

## Master force route with hint

### Instantiation

Same with sharding with hint.

### Add sharding columns and sharding values

- Use hintManager.setMasterRouteOnly to set force route.

There are two overload methods of the registration for each kind of sharding, and the shorter method can simplify the sharding injection of the = condition.

### Clean sharding columns and sharding values

Same with sharding with hint.

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
