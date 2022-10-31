+++
title = "Hint"
weight = 1
+++

## Background

Apache ShardingSphere uses ThreadLocal to manage sharding key values for mandatory routing. A sharding value can be added by programming to the HintManager that takes effect only within the current thread.
Apache ShardingSphere can also do mandatory routing by adding comments to SQL.

Main application scenarios for Hint:
- The sharding fields do not exist in the SQL and database table structure but in the external business logic.
- Certain data operations are forced to be performed in given databases.

## Procedure

1. Call HintManager.getInstance() to obtain an instance of HintManager.
2. Use HintManager.addDatabaseShardingValue, HintManager.addTableShardingValue to set the sharding key value.
3. Execute SQL statements to complete routing and execution.
4. Call HintManager.close to clean up the contents of ThreadLocal.

## Sample

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

> Users can use `hintManager.setDatabaseShardingValue` to set sharding value in hint route to some certain sharding database without sharding tables.

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

#### Use special SQL comments

##### Terms of Use

To use SQL Hint function, users need to set `sqlCommentParseEnabled` to `true`.
The comment format only supports `/* */` for now. The content needs to start with `SHARDINGSPHERE_HINT:`, and optional attributes include:

- `{table}.SHARDING_DATABASE_VALUE`: used to add the data source sharding value corresponding to `{table}` table, multiple attributes are separated by commas;
- `{table}.SHARDING_TABLE_VALUE`: used to add the table sharding value corresponding to `{table}` table, multiple attributes are separated by commas.

> Users can use `SHARDING_DATABASE_VALUE` to set sharding value in hint route to some certain sharding database without sharding tables.

##### Codes:

```sql
/* SHARDINGSPHERE_HINT: t_order.SHARDING_DATABASE_VALUE=1, t_order.SHARDING_TABLE_VALUE=1 */
SELECT * FROM t_order;
```

## Related References

- [Core Feature: Data Sharding](/en/features/sharding/)
- [Developer Guide: Data Sharding](/en/dev-manual/sharding/)
