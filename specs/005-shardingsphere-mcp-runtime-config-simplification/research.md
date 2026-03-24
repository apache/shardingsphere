# Research: ShardingSphere MCP Runtime Configuration Simplification

## Decision 1: 保留 `runtime` 顶级命名空间

- **Decision**: 继续保留 `runtime` 作为 `transport` 之外的 direct runtime
  配置命名空间，不把 `databases` 或 shared defaults 提升到顶级。
- **Rationale**:
  - 当前根层已经形成 `transport` 与 `runtime` 两块语义区域，层级关系清晰。
  - direct runtime 的数据库拓扑、metadata scope 与 capability derivation
    都属于“运行时装配”问题，而不是 transport 配置。
  - 顶层继续扁平化会让配置根层同时混入 transport、runtime topology 和
    metadata scope，抽象层级更差。
- **Alternatives considered**:
  - 把 `databases` 提到顶级: rejected，因为会削弱命名空间边界并让后续扩展更混乱。
  - 用 `backend` 直接替换 `runtime`: rejected，因为当前 direct runtime 不只包含连接，
    还包含 metadata、capability 与 launch 语义。

## Decision 2: 用 `runtime.databases` 统一 single-db 与 multi-db 形态

- **Decision**: 让 `runtime.databases` 成为 direct JDBC runtime 的唯一
  canonical 配置入口；legacy `runtime.props` 只保留为迁移 alias。
- **Rationale**:
  - 当前 single-db 和 multi-db 走的是两套 direct runtime 形态，重复表达了同一领域。
  - `runtime.databases` 已经是 direct multi-database follow-up 的拓扑模型，
    把 single-db 也统一到该模型上能减少 loader 分支与文档漂移。
  - 统一模型后，配置、测试、README 和序列化输出都能围绕同一结构收敛。
- **Alternatives considered**:
  - 保留 `runtime.props` 作为 canonical single-db path: rejected，因为会继续把 direct runtime
    维持成两套 operator-facing 模型。
  - 彻底删除 `runtime.props` 不留兼容: rejected，因为当前已有测试、示例和现存配置依赖它。

## Decision 3: 把 shared defaults 改名为 `databaseDefaults`，并移除 schema 的 operator 配置面

- **Decision**: canonical shared defaults 改为 `runtime.databaseDefaults`，
  但 `schemaPattern` 与 `defaultSchema` 不再作为 operator-facing YAML 配置，
  schema 范围和默认 schema 改由 JDBC metadata 自动发现。
- **Rationale**:
  - `defaults` 过于宽泛，不利于审阅者一眼识别“这是给 database bindings 用的默认值”。
  - `props` 已在 direct runtime 历史模型里代表另一条 single-db path，不能复用。
  - `schemaPattern` 不是数据库事实，而是 metadata 查询过滤策略，不适合作为长期 operator 配置。
  - `defaultSchema` 可以作为运行时事实保留，但应从连接与 JDBC metadata 自动推导，而不是要求用户手填。
- **Alternatives considered**:
  - 保持 `defaults`: rejected，因为命名仍然过宽，容易与其他默认值概念混淆。
  - 把 `defaults` 改名为 `props`: rejected，因为会和 legacy `runtime.props`
    冲突，语义更差。
  - 保留 `metadata` 子配置: rejected，因为会继续暴露 schema 范围控制，和“自动发现”目标冲突。

## Decision 4: `driverClassName` 保留为 optional override，而不是从 `DatabaseType` 直接推导

- **Decision**: `driverClassName` 继续保留，但只作为 optional override；
  默认路径依赖 JDBC driver 在 classpath 中的自动发现。
- **Rationale**:
  - 当前 direct runtime 里 `driverClassName` 只用于显式 `Class.forName(...)`，
    而为空时系统已经能直接依赖 `DriverManager`。
  - 仓库内的 `DatabaseType` SPI 只暴露数据库类型和 JDBC URL prefixes，
    并不提供 JDBC driver class。
  - 数据库类型与具体 JDBC driver 实现不一定一一对应，强行从 `databaseType`
    推导 driver class 会过度耦合。
- **Alternatives considered**:
  - 让 `driverClassName` 继续保持必填: rejected，因为会把本可自动发现的连接信息变成噪音配置。
  - 从 `DatabaseType` 直接返回 driver class: rejected，因为当前 SPI 契约并不承载该职责。

## Decision 5: `supportsCrossSchemaSql` 与 `supportsExplainAnalyze` 改为自动推导

- **Decision**: direct runtime 不再把这两个 capability booleans 作为常规
  operator 配置，而是根据 capability matrix defaults、数据库版本和运行时 metadata
  自动推导；legacy booleans 仅保留为迁移 shim。
- **Rationale**:
  - 这两个字段本质上是数据库能力，不是连接参数。
  - 让 operator 手工声明 capability 会把 `get_capabilities` 变成“配置回显”，
    而不是运行时事实。
  - `supportsExplainAnalyze` 还会真实影响执行拦截，因此更应从系统统一推导。
- **Alternatives considered**:
  - 继续让 operator 永久配置这两个布尔值: rejected，因为会制造 contract 与真实行为的不一致风险。
  - 只靠 runtime metadata 自动推导全部 capability: rejected，因为部分能力需要依赖
    type-level defaults 或 version-aware policy，而不是单纯 metadata 列表。
  - 通过试执行探测 SQL 来判断: rejected，因为会引入副作用、权限依赖和不可预测性。

## Decision 6: legacy aliases 保留兼容期，但必须显式诊断

- **Decision**: `runtime.props`、`runtime.defaults` 和 legacy capability booleans
  在兼容期内仍可被加载，但必须带 deprecation diagnostics；混用 canonical 与
  legacy keys 时快速失败。
- **Rationale**:
  - 这是 operator-facing YAML 契约变更，完全无兼容会让已有配置一次性失效。
  - 如果只兼容而不诊断，团队会长期维护多套 direct runtime 写法，反而无法完成收敛。
  - 冲突场景必须 fail fast，否则会让 loader 隐式吞掉用户意图。
- **Alternatives considered**:
  - 不兼容任何旧 key: rejected，因为破坏性过强。
  - 兼容旧 key 但不输出诊断: rejected，因为无法推动配置收敛。
