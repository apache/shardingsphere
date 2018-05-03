+++
toc = true
title = "Hint"
weight = 3
+++

## Sharding with hint

### Instantiation

```java
HintManager hintManager = HintManager.getInstance();
```

### Add sharding columns and sharding values

- To add Sharding columns and corresponding data of data source by hintManager.addDatabaseShardingValue.
- To add Sharding columns and corresponding data of table by hintManager.addTableShardingValue.

There are two overload methods of the registration for each kind of sharding, and the shorter method can simplify the sharding injection of the = condition.

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
