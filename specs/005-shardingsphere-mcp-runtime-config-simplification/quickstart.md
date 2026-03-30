# Quickstart: ShardingSphere MCP Runtime Configuration Simplification

## 1. Goal

验证 MCP direct runtime 使用新的 canonical 配置结构启动：

- `runtimeDatabases`
- 不再在新示例里保留 `runtime` 包裹层
- 不再在新示例里手工配置 `supportsCrossSchemaSql`
- 不再在新示例里手工配置 `supportsExplainAnalyze`
- 不再在新示例里手工配置 `schemaPattern`
- `driverClassName` 只在需要显式覆盖时提供

## 2. Prepare the packaged runtime

From the repository root:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package
```

## 3. Replace `conf/mcp.yaml` with the canonical shape

```yaml
transport:
  http:
    enabled: true
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
  stdio:
    enabled: true

runtimeDatabases:
  orders:
    databaseType: H2
    jdbcUrl: "jdbc:h2:file:./data/mcp-demo-orders;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
  billing:
    databaseType: H2
    jdbcUrl: "jdbc:h2:file:./data/mcp-demo-billing;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
```

Notes:

- 这个示例刻意省略 `driverClassName`，用来强调它只是 optional override。
- 当 classpath 自动发现不足时，再为具体 binding 显式补 `driverClassName`。
- schema 范围由 JDBC metadata 自动发现。
- capability booleans 由系统自动推导。

## 4. Start the runtime

```bash
bin/start.sh
```

Expected result:

- 配置成功加载为 direct runtime topology。
- 服务不要求 `runtime.props` 或 `runtime.databaseDefaults` 才能走 default launch path。
- `get_capabilities(database)` 返回系统自动推导的 capability。

## 5. Validate capability behavior

至少验证：

1. `list_databases` 返回 `orders` 与 `billing`
2. `get_capabilities(database="orders")` 返回 capability 结果
3. 若某 database type / version 不支持 `EXPLAIN ANALYZE`，
   `execute_query(database, "EXPLAIN ANALYZE ...")` 返回 `unsupported`

## 6. Legacy rejection reference

### Legacy single-db input

Before:

```yaml
runtime:
  props:
    databaseName: logic_db
    databaseType: H2
    jdbcUrl: jdbc:h2:mem:logic
```

Use instead:

```yaml
runtimeDatabases:
  logic_db:
    databaseType: H2
    jdbcUrl: jdbc:h2:mem:logic
```

### Legacy wrapped multi-db input

Before:

```yaml
runtime:
  databases:
    logic_db:
      databaseType: H2
      jdbcUrl: jdbc:h2:mem:logic
```

Use instead:

```yaml
runtimeDatabases:
  logic_db:
    databaseType: H2
    jdbcUrl: jdbc:h2:mem:logic
```

## 7. Migration expectations

- legacy `runtime.*` aliases 不再被加载。
- 发现旧 key 时系统应 fail fast，并给出明确替换提示。
- 新文档与默认配置只展示 `runtimeDatabases` canonical 结构。
