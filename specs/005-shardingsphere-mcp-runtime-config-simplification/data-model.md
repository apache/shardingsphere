# Data Model: ShardingSphere MCP Runtime Configuration Simplification

## Core Domain Entities

### LaunchConfigurationDocument

- **Purpose**: 描述 YAML launch configuration 的顶层结构。
- **Fields**:
  - `transport`
  - `runtimeDatabases`
- **Validation rules**:
  - `transport` 与 `runtimeDatabases` 处于同一抽象层级。
  - `runtimeDatabases` 与运行时对象 `MCPLaunchConfiguration.runtimeDatabases`
    一一对应。

### DirectRuntimeDatabasesConfiguration

- **Purpose**: 描述 canonical direct JDBC runtime 拓扑。
- **Fields**:
  - `runtimeDatabases`
- **Validation rules**:
  - `runtimeDatabases` 必须按 unique logical database 名称键控。
  - canonical 配置不得再通过 `runtime` 包裹层表达。
  - canonical 配置不得再把 `runtime.props`、`runtime.defaults`、
    `runtime.databaseDefaults`、`runtime.databases` 作为有效输入。

### LogicalDatabaseBindingConfiguration

- **Purpose**: 描述一个 logical database 的 direct JDBC binding。
- **Fields**:
  - `database`
  - `databaseType`
  - `jdbcUrl`
  - `username`
  - `password`
  - `driverClassName`
- **Validation rules**:
  - `database` 必须显式命名且在一个 runtime 中唯一。
  - `databaseType` 与 `jdbcUrl` 是 direct binding 的最小必填项。
  - `driverClassName` 为可选覆盖项，而不是必填项。
  - schema 范围不再由 operator 配置提供。

### RuntimeSchemaDiscoveryFacts

- **Purpose**: 描述 direct runtime 在 metadata 发现阶段由 JDBC 自动识别出的 schema 事实。
- **Fields**:
  - `discoveredSchemas`
- **Validation rules**:
  - schema facts 由 JDBC metadata 自动识别，而不是 operator 手工填写。
  - schema facts 允许为空，但空值必须来自真实发现结果，而不是隐式配置回填。

### DerivedDatabaseCapabilityFacts

- **Purpose**: 描述 direct runtime 为一个 logical database 自动推导出的 capability 事实。
- **Fields**:
  - `database`
  - `databaseType`
  - `databaseVersion`
  - `schemaSemantics`
  - `supportsCrossSchemaSql`
  - `supportsExplainAnalyze`
- **Validation rules**:
  - capability facts 必须通过 deterministic 规则生成，而不是 operator 手工回填。
  - `databaseVersion` 缺失时，系统回退到 safe type-level defaults。
  - `supportsExplainAnalyze` 的最终值必须与 `execute_query` 的实际拦截语义一致。

### LegacyRuntimeInput

- **Purpose**: 描述会被显式拒绝的历史配置形态。
- **Fields**:
  - `runtime.props`
  - `runtime.defaults`
  - `runtime.databaseDefaults`
  - `runtime.databases`
  - legacy `supportsCrossSchemaSql`
  - legacy `supportsExplainAnalyze`
- **Validation rules**:
  - legacy runtime keys 只允许进入 validation 入口，不允许再被 canonicalization。
  - 一旦出现 legacy runtime keys，系统必须 fail fast。
  - legacy capability booleans 在 canonical 配置中同样必须 fail fast。

## Relationships

- `LaunchConfigurationDocument.runtimeDatabases` 直接承载
  `LogicalDatabaseBindingConfiguration` 的映射。
- 每个 logical database 在 metadata 加载和 capability 装配后生成一个
  `DerivedDatabaseCapabilityFacts`。
- `LegacyRuntimeInput` 只参与拒绝诊断，不再生成 canonical runtime 配置。

## Canonical Configuration Shape

```yaml
runtimeDatabases:
  orders:
    databaseType: H2
    jdbcUrl: jdbc:h2:file:./data/orders
    driverClassName: org.h2.Driver
```

## Legacy Rejection Reference

### Legacy single-db input

```yaml
runtime:
  props:
    databaseName: logic_db
    databaseType: H2
    jdbcUrl: jdbc:h2:mem:logic
```

Expected behavior:

- 配置加载失败。
- 错误消息明确要求改用 `runtimeDatabases.logic_db`。

### Legacy wrapped multi-db input

```yaml
runtime:
  databases:
    logic_db:
      databaseType: H2
      jdbcUrl: jdbc:h2:mem:logic
```

Expected behavior:

- 配置加载失败。
- 错误消息明确要求改用顶级 `runtimeDatabases`。

## State Transitions

- `raw_yaml -> validated`
  - loader 读取 `transport` 与 `runtimeDatabases`。
- `validated -> metadata_loaded`
  - direct runtime 根据 binding 与 JDBC metadata 自动装载 runtime metadata。
- `metadata_loaded -> capability_derived`
  - capability assembler 根据 type-level defaults、数据库版本和运行时 metadata
    生成 `DerivedDatabaseCapabilityFacts`。
- `legacy_input_detected -> rejected_with_diagnostics`
  - 发现旧 `runtime.*` key 或 legacy capability booleans 时直接失败，并输出迁移提示。
