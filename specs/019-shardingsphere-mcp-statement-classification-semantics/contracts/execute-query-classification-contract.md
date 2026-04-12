# Contract: Execute Query Statement Classification Semantics

## Request Surface

`execute_query(database, schema?, sql, max_rows?, timeout_ms?)`

请求参数不因本特性新增字段；
本轮变化集中在语义分类和成功响应表达。

## Classification Contract

### Rule 1: `WITH` 不是 statement class

- `WITH` 只能表示使用了 CTE 语法。
- 不能仅凭 `WITH` 把语句判定为 `QUERY`。

### Rule 2: `statement_class` 表示副作用和治理语义

- `QUERY` 表示整条语句没有数据修改副作用。
- `DML` 表示整条语句存在数据修改副作用，
  包括主语句写入和 CTE 内部写入。
- `DDL` / `DCL` / `TRANSACTION_CONTROL` / `SAVEPOINT` / `EXPLAIN_ANALYZE`
  继续沿用现有定义。

### Rule 3: `statement_type` 表示更具体的用户可读语句类型

- 普通查询 CTE：`statement_type = SELECT`
- SQL Server CTE-prefixed UPDATE：`statement_type = UPDATE`
- PostgreSQL data-modifying CTE with outer SELECT：`statement_type = SELECT`

### Rule 4: `result_kind` 表示实际返回形状

- `result_set`
- `update_count`
- `statement_ack`

`result_kind` 不得被用来倒推出 `statement_class`。

## Success Payload Contract

成功 payload 至少应包含：

- `statement_class`
- `statement_type`
- `result_kind`
- `status`
- `truncated`

按返回形状补充：

- `result_kind = result_set`
  - `columns`
  - `rows`
- `result_kind = update_count`
  - `affected_rows`
- `result_kind = statement_ack`
  - `message`

## Normative Examples

### Example A: 查询 CTE

```json
{
  "statement_class": "query",
  "statement_type": "SELECT",
  "result_kind": "result_set",
  "status": "OK",
  "truncated": false
}
```

### Example B: SQL Server CTE-prefixed UPDATE

```json
{
  "statement_class": "dml",
  "statement_type": "UPDATE",
  "result_kind": "update_count",
  "affected_rows": 12,
  "status": "OK",
  "truncated": false
}
```

### Example C: PostgreSQL data-modifying CTE

```json
{
  "statement_class": "dml",
  "statement_type": "SELECT",
  "result_kind": "result_set",
  "status": "OK",
  "truncated": false
}
```

## Consumer Guidance

- capability gate 看 `statement_class`
- audit 看 `statement_class`，可附带 `statement_type`
- 返回值解析看 `result_kind`
- 用户解释时同时看 `statement_class` 和 `statement_type`
