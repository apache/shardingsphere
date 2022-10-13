+++
title = "Hint"
weight = 1
+++

## Background

Apache ShardingSphere uses ThreadLocal to manage primary database routing marks for mandatory routing. A primary database routing mark can be added to HintManager through programming, and this value is valid only in the current thread.
Apache ShardingSphere can also route the primary database by adding comments to SQL.

Hint is mainly used to perform mandatory data operations in the primary database under the read/write splitting scenarios.

## Procedure

1. Call `HintManager.getInstance()` to obtain HintManager instance.
2. Call `HintManager.setWriteRouteOnly()` method to set the primary database routing marks.
3. Execute SQL statements to complete routing and execution.
4. Call `HintManager.close()` to clear the content of ThreadLocal.

## Sample

### Primary Route with Hint

#### Use manual programming

##### Get HintManager

Be the same as sharding based on hint.

##### Configure Primary Database Route

- Use `hintManager.setWriteRouteOnly` to configure primary database route.

##### Clean Hint Value

Be the same as data sharding based on hint.

##### Codes:

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

To use SQL Hint function, users need to set `sqlCommentParseEnabled` to `true`.
The comment format only supports `/* */` for now. The content needs to start with `SHARDINGSPHERE_HINT:`, and the attribute name needs to be `WRITE_ROUTE_ONLY`.

##### Codes:
```sql
/* SHARDINGSPHERE_HINT: WRITE_ROUTE_ONLY=true */
SELECT * FROM t_order;
```

## Related References

- [Core Feature: Readwrite Splitting](/en/features/readwrite-splitting/)
- [Developer Guide: Readwrite Splitting](/en/dev-manual/readwrite-splitting/)
