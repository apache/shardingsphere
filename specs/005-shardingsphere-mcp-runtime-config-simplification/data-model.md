# Data Model: ShardingSphere MCP Runtime Configuration Simplification

## Core Domain Entities

### RuntimeConfigurationEnvelope

- **Purpose**: 描述 `transport` 之外 direct runtime 的 canonical 配置包裹层。
- **Fields**:
  - `runtime`
- **Validation rules**:
  - `runtime` 继续作为顶级 direct runtime 命名空间。
  - default launch path 不能依赖多个并列的 direct runtime 根模型。

### DirectRuntimeConfiguration

- **Purpose**: 描述 canonical direct JDBC runtime 配置。
- **Fields**:
  - `databaseDefaults`
  - `databases`
  - `migrationState`
- **Validation rules**:
  - `databases` 必须按 unique logical database 名称键控。
  - `databaseDefaults` 只补缺省值，不得隐式创建 database binding。
  - canonical 配置不得再把 `runtime.props` 作为主写法。

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
  - schema 范围与默认 schema 不再由 operator 配置提供。

### RuntimeSchemaDiscoveryFacts

- **Purpose**: 描述 direct runtime 在 metadata 发现阶段由 JDBC 自动识别出的 schema 事实。
- **Fields**:
  - `discoveredSchemas`
  - `defaultSchema`
- **Validation rules**:
  - 优先使用 `Connection.getSchema()`。
  - 若当前 schema 不可用，则回退到 `DatabaseMetaData.getSchemas()` 过滤后的结果。
  - `defaultSchema` 是运行时事实，可为空，但不是 operator-facing 输入。

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

### LegacyRuntimeAliasInput

- **Purpose**: 描述兼容期内允许进入 loader 的历史配置形态。
- **Fields**:
  - `runtime.props`
  - `runtime.defaults`
  - legacy `supportsCrossSchemaSql`
  - legacy `supportsExplainAnalyze`
- **Validation rules**:
  - legacy alias 只允许进入 canonicalization 入口，不允许作为最终 canonical 输出保留。
  - 若 canonical keys 与对应 legacy aliases 混用，必须 fail fast。
  - 使用 legacy alias 时必须生成 migration diagnostics。

### RuntimeConfigMigrationState

- **Purpose**: 描述一次 direct runtime 配置加载是否经过了 legacy-to-canonical 转换。
- **Fields**:
  - `usedLegacyPropsAlias`
  - `usedLegacyDefaultsAlias`
  - `usedLegacyCapabilityBooleans`
  - `diagnostics`
- **Validation rules**:
  - migration diagnostics 必须可追溯到具体的 legacy key。
  - 无 legacy alias 时，migration state 应为空或无诊断。

## Relationships

- `RuntimeConfigurationEnvelope.runtime` 包含一个 `DirectRuntimeConfiguration`。
- `DirectRuntimeConfiguration.databaseDefaults` 为多个
  `LogicalDatabaseBindingConfiguration` 提供共享缺省值。
- 每个 logical database 在 metadata 加载和 capability 装配后生成一个
  `DerivedDatabaseCapabilityFacts`。
- `LegacyRuntimeAliasInput` 在 loader 中被转换成 canonical
  `DirectRuntimeConfiguration`，并在 `RuntimeConfigMigrationState` 中留下诊断痕迹。

## Canonical Configuration Shape

```yaml
runtime:
  databases:
    orders:
      databaseType: H2
      jdbcUrl: jdbc:h2:file:./data/orders
      driverClassName: org.h2.Driver
```

## Legacy-to-Canonical Mapping

### Legacy single-db path

```yaml
runtime:
  props:
    databaseName: logic_db
    databaseType: H2
    jdbcUrl: jdbc:h2:mem:logic
```

maps to:

```yaml
runtime:
  databases:
    logic_db:
      databaseType: H2
      jdbcUrl: jdbc:h2:mem:logic
```

### Legacy shared defaults alias

```yaml
runtime:
  defaults:
    driverClassName: org.h2.Driver
```

maps to:

```yaml
runtime:
  databaseDefaults:
    driverClassName: org.h2.Driver
```

## State Transitions

- `raw_yaml -> canonicalized`
  - loader 读取 canonical keys 或 legacy aliases。
- `canonicalized -> validated`
  - 检查 logical database 唯一性、必填字段和 canonical/legacy 冲突。
- `validated -> metadata_loaded`
  - direct runtime 根据 binding 与 JDBC metadata 自动装载 runtime metadata。
- `metadata_loaded -> capability_derived`
  - capability assembler 根据 type-level defaults、数据库版本和运行时 metadata
    生成 `DerivedDatabaseCapabilityFacts`。
- `legacy_alias_used -> diagnostics_emitted`
  - 兼容期使用 legacy keys 时记录 deprecation diagnostics。
