# Research: ShardingSphere MCP Runtime Configuration Simplification

## Decision 1: 用 `runtimeDatabases` 直接作为顶级 canonical runtime 入口

- **Decision**: 不再保留 `runtime` 包裹层，direct JDBC runtime 的 operator-facing
  canonical YAML 直接使用顶级 `runtimeDatabases`。
- **Rationale**:
  - 当前运行时主模型 [MCPLaunchConfiguration](/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/MCPLaunchConfiguration.java)
    已直接暴露 `runtimeDatabases`，顶层 YAML 与运行时对象一一对应更直观。
  - 去掉 `runtime` 包裹层后，YAML swappers 不再需要额外的聚合 DTO 与 envelope
    语义，结构更简单。
  - 这能避免“YAML 看起来是一套模型，运行时其实是另一套模型”的抽象错位。
- **Alternatives considered**:
  - 保留 `runtime` 顶级命名空间: rejected，因为会继续引入额外抽象层和字段映射。
  - 使用新的 `backend` 或 `databases` 顶级名: rejected，因为 `runtimeDatabases`
    已经和当前运行时配置字段对齐。

## Decision 2: 用 `runtimeDatabases` 统一 single-db 与 multi-db 形态

- **Decision**: single-db 与 multi-db 都通过 `runtimeDatabases` 表达，不再保留
  `runtime.props` 作为可接受的迁移输入。
- **Rationale**:
  - 直接把单库表达为只含一个 entry 的 map，和多库没有领域差异。
  - 这样配置、测试、README、默认样例和运行时对象都围绕同一结构收敛。
  - 旧的 `runtime.props` 会继续制造“单库一套模型、多库另一套模型”的心智负担。
- **Alternatives considered**:
  - 继续接受 `runtime.props` 作为 migration alias: rejected，因为这会把旧模型长期保留在实现中。
  - 自动把 `runtime.props` 转成 `runtimeDatabases`: rejected，因为用户仍会看到“文档一套、实现兼容另一套”的边界模糊。

## Decision 3: 删除 shared defaults 模型，保持单库配置一一对应

- **Decision**: 不再保留 `runtime.databaseDefaults` 或任何 shared-defaults
  canonical 写法；每个 logical database entry 必须显式声明自身所需字段。
- **Rationale**:
  - `databaseDefaults` 需要 merge 规则，会让 YAML 层出现额外聚合 DTO 和非对称转换。
  - shared defaults 只是书写糖，不是运行时领域事实。
  - 去掉这层 sugar 后，`YamlRuntimeDatabaseConfiguration` 与
    `RuntimeDatabaseConfiguration` 可以保持最直接的 1:1 映射。
- **Alternatives considered**:
  - 保留 `runtime.databaseDefaults`: rejected，因为它让 YAML 和运行时对象不再一一对应。
  - 把 defaults 合并逻辑下沉到 loader: rejected，因为复杂度只是换了位置，没有消失。

## Decision 4: `driverClassName` 保留为 optional override

- **Decision**: `driverClassName` 继续保留，但只作为 optional override；
  默认路径依赖 JDBC driver 在 classpath 中的自动发现。
- **Rationale**:
  - 当前 direct runtime 在 `driverClassName` 为空时已经可以依赖 `DriverManager`。
  - 仓库内 `DatabaseType` SPI 不提供稳定的 JDBC driver class 事实。
  - 数据库类型和具体 JDBC driver 实现并非总是一一对应。
- **Alternatives considered**:
  - 保持 `driverClassName` 必填: rejected，因为会把噪音配置强加给常见场景。
  - 从 `DatabaseType` 自动推导 driver class: rejected，因为会让 SPI 承担不属于它的职责。

## Decision 5: capability booleans 改为自动推导

- **Decision**: `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` 不再作为
  operator-facing 配置字段，而是根据 database type、database version 与运行时
  metadata 自动推导。
- **Rationale**:
  - 这两个字段表达的是数据库能力，而不是部署输入。
  - 让 operator 手填能力布尔值，会把 `get_capabilities` 变成配置回显，而不是运行时事实。
  - `supportsExplainAnalyze` 会影响执行拦截，因此更需要统一推导。
- **Alternatives considered**:
  - 永久保留 operator 手填布尔值: rejected，因为 contract 与真实行为会分叉。
  - 全靠试执行 SQL 探测: rejected，因为会带来副作用和不可预测性。

## Decision 6: legacy runtime keys 直接拒绝，但提供明确诊断

- **Decision**: `runtime.props`、`runtime.defaults`、`runtime.databaseDefaults`
  和 `runtime.databases` 都不再作为兼容输入加载；系统只保留 targeted diagnostics，
  引导用户改为 `runtimeDatabases`。
- **Rationale**:
  - 一一对应模型的目标是删掉额外转换层，而不是继续在实现里保留多套输入语义。
  - 显式拒绝比“兼容但不推荐”更容易让文档、实现和用户心智保持一致。
  - 对 legacy keys 给出明确错误消息，仍然能提供可执行的迁移路径。
- **Alternatives considered**:
  - 保留一段 migration window: rejected，因为会继续把旧模型留在生产代码里。
  - 静默忽略 legacy keys: rejected，因为这会掩盖用户配置错误。
