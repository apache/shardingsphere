# Quickstart: ShardingSphere MCP Execute Query Schema Semantics

## 1. 先读 capability，再决定怎么传 `schema`

所有接入方都应先调用：

```text
get_capabilities(database)
```

重点看两个字段：

- `defaultSchemaSemantics`
- `schemaExecutionSemantics`

## 2. MySQL-like 场景

### Capability 示例

```json
{
  "database": "logic_db",
  "databaseType": "MySQL",
  "defaultSchemaSemantics": "DATABASE_AS_SCHEMA",
  "schemaExecutionSemantics": "FIXED_TO_DATABASE",
  "supportsCrossSchemaSql": false
}
```

### 解释

- MCP 会统一暴露 `schema = logic_db`
- 这个名字主要用于 metadata、URI 和统一 contract
- 它不代表 MCP 承诺可以用 request-level `schema` 独立切换执行命名空间

### 推荐用法

```text
execute_query(database="logic_db", sql="SELECT * FROM orders")
```

如果调用方传入：

```text
execute_query(database="logic_db", schema="logic_db", sql="SELECT * FROM orders")
```

产品语义上等价于：

- 仍然只执行在 `logic_db` 这个 logical database 内
- `schema` 是对齐 metadata 的 public label
- 不应把它当成 strict SQL qualifier

## 3. PostgreSQL-like 场景

### Capability 示例

```json
{
  "database": "logic_db",
  "databaseType": "PostgreSQL",
  "defaultSchemaSemantics": "NATIVE_SCHEMA",
  "schemaExecutionSemantics": "BEST_EFFORT",
  "supportsCrossSchemaSql": true
}
```

### 解释

- 数据库具备原生 schema 概念
- `schema` 可以作为未限定对象名的 execution hint
- 但 MCP 当前只承诺 `BEST_EFFORT`，不是 strict guarantee

### 推荐用法

当希望依赖默认 schema / `search_path`：

```text
execute_query(database="logic_db", sql="SELECT * FROM orders")
```

当希望给未限定对象名一个目标命名空间 hint：

```text
execute_query(database="logic_db", schema="sales", sql="SELECT * FROM orders")
```

当希望 deterministic targeting：

```text
execute_query(database="logic_db", schema="sales", sql="SELECT * FROM sales.orders")
```

这里真正决定目标对象的是 SQL 中的 `sales.orders`，
而不是 request-level `schema`。

## 4. 调用方决策规则

### 当 `schemaExecutionSemantics = FIXED_TO_DATABASE`

- 可以省略 `schema`
- 即使传 `schema`，也不能把它当成独立执行边界
- metadata 暴露的 `schema` 更像统一命名空间标签

### 当 `schemaExecutionSemantics = BEST_EFFORT`

- 可以不传 `schema`
- 传入 `schema` 时，它是未限定对象名的 execution hint
- 需要严格指向具体对象时，用 SQL 自身显式限定

## 5. 对维护者的落地检查

实现完成后，至少确认以下三点：

1. `get_capabilities(database)` 已返回 `schemaExecutionSemantics`
2. no-native-schema 数据库的 metadata `schema` 已归一化为逻辑 `database` 名称
3. `execute_query` tool descriptor、PRD、`001` contract 与实现对 `schema` 的说明一致
