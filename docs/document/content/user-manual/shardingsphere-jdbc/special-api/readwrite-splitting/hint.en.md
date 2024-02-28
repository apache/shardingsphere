+++
title = "Hint"
weight = 1
+++

## Background

Apache ShardingSphere uses `ThreadLocal` to manage primary database routing marks for mandatory routing. A primary database routing mark can be added to `HintManager` through programming, and this value is valid only in the current thread.

`Hint` is mainly used to perform mandatory data operations in the primary database for read/write splitting scenarios.

## Procedure

1. Call `HintManager.getInstance()` to obtain HintManager instance.
2. Call `HintManager.setWriteRouteOnly()` method to set the primary database routing marks.
3. Execute SQL statements to complete routing and execution.
4. Call `HintManager.close()` to clear the content of ThreadLocal.

## Sample

### Primary Route with Hint

#### Get HintManager

The same as sharding based on hint.

#### Configure Primary Database Route

- Use `hintManager.setWriteRouteOnly` to configure primary database route.

#### Clean Hint Value

The same as data sharding based on hint.

#### Code:

```java
String sql = "SELECT * FROM t_order";
try (HintManager hintManager = HintManager.getInstance();
     Connection conn = dataSource.getConnection();
     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setWriteRouteOnly();
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```

### Route to the specified database with Hint

#### Get HintManager

The same as sharding based on hint.

#### Configure Database Route

- Use `hintManager.setDataSourceName` to configure database route.

#### Code:

```java
String sql = "SELECT * FROM t_order";
try (HintManager hintManager = HintManager.getInstance();
     Connection conn = dataSource.getConnection();
     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
    hintManager.setDataSourceName("ds_0");
    try (ResultSet rs = preparedStatement.executeQuery()) {
        while (rs.next()) {
            // ...
        }
    }
}
```

## Related References

- [Core Feature: Read/write Splitting](/en/features/readwrite-splitting/)
- [Developer Guide: Read/write Splitting](/en/dev-manual/infra-algorithm/)
