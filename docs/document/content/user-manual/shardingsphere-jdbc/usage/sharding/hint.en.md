+++
title = "Hint"
weight = 5
+++

## Introduction

Apache ShardingSphere uses ThreadLocal to manage sharding key value or hint route. 
Users can add sharding values to HintManager, and those values only take effect within the current thread. 

Usage of hint:

* Sharding columns are not in SQL and table definition, but in external business logic.
* Some operations forced to do in the primary database.

## Usage

### Sharding with Hint

#### Hint Configuration

Hint algorithms require users to implement the interface of `org.apache.shardingsphere.api.sharding.hint.HintShardingAlgorithm`. 
Apache ShardingSphere will acquire sharding values from HintManager to route.

Take the following configurations for reference:

```yaml
rules:
- !SHARDING
  tables:
    t_order:
      actualDataNodes: demo_ds_${0..1}.t_order_${0..1}
      databaseStrategy:
        hint:
          algorithmClassName: xxx.xxx.xxx.HintXXXAlgorithm
      tableStrategy:
        hint:
          algorithmClassName: xxx.xxx.xxx.HintXXXAlgorithm
  defaultTableStrategy:
    none:
  defaultKeyGenerateStrategy:
    type: SNOWFLAKE
    column: order_id

props:
    sql-show: true
```

#### Get HintManager

```java
HintManager hintManager = HintManager.getInstance();
```

#### Add Sharding Value

- Use `hintManager.addDatabaseShardingValue` to add sharding key value of data source.
- Use `hintManager.addTableShardingValue` to add sharding key value of table.

> Users can use `hintManager.setDatabaseShardingValue` to add sharding in hint route to some certain sharding database without sharding tables. 
After that, SQL parse and rewrite phase will be skipped and the overall enforcement efficiency can be enhanced.

#### Clean Hint Values

Sharding values are saved in `ThreadLocal`, so it is necessary to use `hintManager.close()` to clean `ThreadLocal`.

**`HintManager` has implemented `AutoCloseable`. We recommend to close it automatically with `try with resource`.**

#### Codes:

```java
// Sharding database and table with HintManager
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

// Sharding database and one database route with HintManager
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

### Primary Route with Hint

#### Get HintManager

Be the same as sharding based on hint.

#### Configure Primary Database Route

- Use `hintManager.setPrimaryRouteOnly` to configure primary database route.

#### Clean Hint Value

Be the same as data sharding based on hint.

#### Codes:

```java
String sql = "SELECT * FROM t_order";
try (
        HintManager hintManager = HintManager.getInstance();
        Connection conn = dataSource.getConnection();
        PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setPrimaryRouteOnly();
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```
