+++
title = "Hint"
weight = 1
+++

## Background

Apache ShardingSphere uses `ThreadLocal` to manage primary database routing marks for mandatory routing. A primary database routing mark can be added to `HintManager` through programming, and this value is valid only in the current thread.
Apache ShardingSphere can also route the primary database by adding comments to SQL.

`Hint` is mainly used to perform mandatory data operations in the primary database for read/write splitting scenarios.

## Procedure

1. Call `HintManager.getInstance()` to obtain HintManager instance.
2. Call `HintManager.setWriteRouteOnly()` method to set the primary database routing marks.
3. Execute SQL statements to complete routing and execution.
4. Call `HintManager.close()` to clear the content of ThreadLocal.

## Sample

### Primary Route with Hint

#### Use manual programming

##### Get HintManager

The same as sharding based on hint.

##### Configure Primary Database Route

- Use `hintManager.setWriteRouteOnly` to configure primary database route.

##### Clean Hint Value

The same as data sharding based on hint.

##### Code:

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

#### Use special SQL comments

##### Terms of Use

For the SQL Hint function, the comment format only supports `/* */` for now. The content needs to start with `SHARDINGSPHERE_HINT:`, and the attribute name needs to be `WRITE_ROUTE_ONLY`.

##### Code:
```sql
/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */
SELECT * FROM t_order;
```

### Route to the specified database with Hint

#### Use manual programming

##### Get HintManager

The same as sharding based on hint.

##### Configure Database Route

- Use `hintManager.setDataSourceName` to configure database route.

##### Code:

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

#### Use special SQL comments

##### Terms of Use:

Currently, the SQL Hint function only supports routing to one data source.
The comment format only supports `/* */` for now. The content needs to start with `SHARDINGSPHERE_HINT:`, and the attribute name needs to be `DATA_SOURCE_NAME`.
Client connections using `MySQL` need to add the `-c` option to preserve comments, because the client defaults to `--skip-comments` to filter comments.

##### Code:
```sql
/* SHARDINGSPHERE_HINT: DATA_SOURCE_NAME=ds_0 */
SELECT * FROM t_order;
```

## Related References

- [Core Feature: Read/write Splitting](/en/features/readwrite-splitting/)
- [Developer Guide: Read/write Splitting](/en/dev-manual/readwrite-splitting/)
