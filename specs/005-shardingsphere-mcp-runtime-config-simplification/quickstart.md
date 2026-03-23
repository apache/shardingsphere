# Quickstart: ShardingSphere MCP Runtime Configuration Simplification

## 1. Goal

验证 MCP direct runtime 使用新的 canonical 配置结构启动：

- `runtime.databaseDefaults`
- `runtime.databases`
- 不再在新示例里手工配置 `supportsCrossSchemaSql`
- 不再在新示例里手工配置 `supportsExplainAnalyze`
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
    server:
      bindHost: 127.0.0.1
      port: 18088
      endpointPath: /mcp
  stdio:
    enabled: true

runtime:
  databaseDefaults:
    metadata:
      schemaPattern: public
      defaultSchema: public
  databases:
    orders:
      databaseType: H2
      jdbcUrl: "jdbc:h2:file:./data/mcp-demo-orders;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
      driverClassName: org.h2.Driver
    billing:
      databaseType: H2
      jdbcUrl: "jdbc:h2:file:./data/mcp-demo-billing;MODE=MySQL;DATABASE_TO_UPPER=false;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'conf/demo-h2.sql'"
      driverClassName: org.h2.Driver
```

Notes:

- `driverClassName` 在现代 JDBC 4 driver 场景通常可以省略。
- `schemaPattern` 和 `defaultSchema` 仍保留，因为它们决定 metadata scope。
- capability booleans 由系统自动推导。

## 4. Start the runtime

```bash
bin/start.sh
```

Expected result:

- 配置成功加载为 direct runtime topology。
- 服务不要求 `runtime.props` 才能走 default launch path。
- `get_capabilities(database)` 返回系统自动推导的 capability。

## 5. Validate capability behavior

至少验证：

1. `list_databases` 返回 `orders` 与 `billing`
2. `get_capabilities(database="orders")` 返回 capability 结果
3. 若某 database type / version 不支持 `EXPLAIN ANALYZE`，
   `execute_query(database, "EXPLAIN ANALYZE ...")` 返回 `unsupported`

## 6. Legacy mapping reference

### Legacy single-db alias

Before:

```yaml
runtime:
  props:
    databaseName: logic_db
    databaseType: H2
    jdbcUrl: jdbc:h2:mem:logic
```

Canonical:

```yaml
runtime:
  databases:
    logic_db:
      databaseType: H2
      jdbcUrl: jdbc:h2:mem:logic
```

### Legacy defaults alias

Before:

```yaml
runtime:
  defaults:
    schemaPattern: public
    defaultSchema: public
```

Canonical:

```yaml
runtime:
  databaseDefaults:
    metadata:
      schemaPattern: public
      defaultSchema: public
```

## 7. Migration expectations

- 兼容期内 legacy aliases 仍可被加载。
- canonical keys 与对应 legacy aliases 混用时应 fail fast。
- 新文档与默认配置只展示 canonical 结构。
