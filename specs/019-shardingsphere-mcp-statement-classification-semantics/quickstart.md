# Quickstart: ShardingSphere MCP Statement Classification Semantics

## 1. 普通查询 CTE

请求：

```sql
WITH recent_orders AS (
  SELECT * FROM orders WHERE created_at >= CURRENT_DATE
)
SELECT * FROM recent_orders
```

期望语义：

- `statement_class = query`
- `statement_type = SELECT`
- `result_kind = result_set`

产品解释：

- 这是典型查询语义。
- `WITH` 只是 CTE 写法，不改变它是查询的事实。

## 2. SQL Server 风格的 CTE-prefixed UPDATE

请求：

```sql
WITH target_orders AS (
  SELECT order_id FROM orders WHERE status = 'NEW'
)
UPDATE orders
SET status = 'DONE'
FROM target_orders
WHERE orders.order_id = target_orders.order_id
```

期望语义：

- `statement_class = dml`
- `statement_type = UPDATE`
- `result_kind = update_count`

产品解释：

- 这是写操作。
- 不能因为首关键字是 `WITH` 就进入查询路径。

## 3. PostgreSQL / openGauss data-modifying CTE

请求：

```sql
WITH updated_orders AS (
  UPDATE orders
  SET status = 'DONE'
  WHERE status = 'NEW'
  RETURNING *
)
SELECT * FROM updated_orders
```

期望语义：

- `statement_class = dml`
- `statement_type = SELECT`
- `result_kind = result_set`

产品解释：

- 整条语句会修改数据，所以治理语义是 `dml`。
- 外层 `SELECT` 让这次执行返回结果集，所以 `result_kind` 是 `result_set`。
- 这正是为什么必须把副作用分类和返回形状拆开。

## 4. Agent 的使用建议

当 Agent 需要判断这条 SQL 是否会写数据时：

- 看 `statement_class`
- 不要只看 `statement_type`
- 更不要把 `result_kind = result_set` 当成“只读查询”的充分条件

当 Agent 需要决定如何解析返回值时：

- 看 `result_kind`
- `result_set` 解析列和行
- `update_count` 解析 `affected_rows`

当 Agent 需要给用户解释执行性质时：

- 同时展示 `statement_class` 和 `statement_type`
- 例如：`DML (SELECT result via data-modifying CTE)`
