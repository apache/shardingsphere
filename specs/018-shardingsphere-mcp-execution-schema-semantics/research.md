# Research: ShardingSphere MCP Execute Query Schema Semantics

## Research Goal

把 MCP 中 `database`、`schema`、metadata discovery 与 `execute_query` 之间的产品边界一次性说清楚，
并把方案 A 固化成后续实现的唯一推荐方向。

## Decision 1: `database` 是强边界，`schema` 不是

### Decision

- `database` 继续作为每次 SQL 执行必须显式指定的唯一强边界
- `schema` 只表达数据库内部命名空间意图
- `schema` 不承担第二个路由键、第二个租户边界或第二个鉴权边界职责

### Why

- MCP 当前已经明确不支持跨 `database` 执行
- 事务绑定、runtime database 配置和 capability 发布都以逻辑 `database` 为中心
- 如果把 `schema` 也提升成强边界，
  会与当前的 session 绑定、logical database 路由和统一对象模型发生冲突

### Product Consequence

- 所有 capability、tool、resource 和文档都必须把 `database` 放在第一位
- `schema` 必须被解释为 “database 内部的命名空间 hint”
- 调用方不应通过传 `schema` 期待获得跨 `database` 或独立租户级别的执行保证

## Decision 2: 采用方案 A，不采用方案 B

### Decision

采用方案 A：
在 capability 中新增 `schemaExecutionSemantics`，
由系统显式告诉调用方当前数据库的 execution-time schema 语义。

### Option Comparison

#### 方案 A：显式 capability

**优点**

- 机器可读，Agent 可以先读 capability 再决定是否传 `schema`
- 能把 metadata label 和 execution semantics 区分开
- 可以避免调用方把 MySQL-like 的统一 `schema` 名称误解为 strict SQL qualifier
- 契约变化集中，评审时更容易判断系统到底承诺了什么

**缺点**

- 需要扩展 capability model、response 和对应测试
- 需要补齐各数据库的 capability matrix

#### 方案 B：只写文档，不加 capability 字段

**优点**

- 实现改动更少
- 不需要改 capability payload

**缺点**

- Agent 仍然无法程序化判断当前数据库该不该传 `schema`
- metadata label 与 execute semantics 的关系仍然隐含
- 产品语义只能靠人读文档，无法在 MCP 协议面直接消费

### Recommendation

选择方案 A。
因为 MCP 的主要用户不是手工点击界面的终端用户，而是 Agent、IDE 和平台程序。
这类调用方需要 machine-readable contract，而不是纯人工阅读说明。

## Decision 3: 没有独立 schema 概念的数据库，公共 `schema` 名称使用逻辑 `database` 名称

### Decision

- 对 `defaultSchemaSemantics = DATABASE_AS_SCHEMA` 的数据库，
  MCP 对外统一暴露的 `schema` 名称使用逻辑 `database` 名称
- 不再使用空字符串作为公共 `schema` 名称

### Why

- 空字符串虽然贴近某些 JDBC metadata 返回值，
  但对上层 Agent 来说几乎不可消费，也不利于统一 URI 和 tool 参数
- MCP 公共契约里的 `schema` 是标准化对象模型的一部分，
  需要一个稳定、可读、可引用的名字
- 逻辑 `database` 名称是当前唯一稳定且对外公开的一级标识

### Important Consequence

这一定义解决的是 “公共 contract 的可用性”，
不等于这个 `schema` 名称一定是底层 SQL 可以直接写进限定名的位置。

也就是说：

- 在 MySQL-like 数据库里，
  MCP 暴露的 `schema = logic_db` 更像 public namespace label
- 它帮助统一 metadata、URI 和 tool 输入
- 但它不自动成为底层 SQL 可执行 qualifier 的产品承诺

这正是必须引入 `schemaExecutionSemantics` 的原因。

## Decision 4: V1 只引入两种 execution semantics，先避免过度承诺

### Decision

V1 的 `schemaExecutionSemantics` 只定义两种状态：

- `FIXED_TO_DATABASE`
- `BEST_EFFORT`

### Why

- 当前实现并没有任何可以对外宣称 strict schema switching guarantee 的验证闭环
- 如果现在引入 `STRICT`，很容易让 capability 超前承诺
- 按照最小安全变化原则，先把当前真实语义诚实表达出来更重要

### Semantics Definition

#### `FIXED_TO_DATABASE`

- `schema` 在公共模型中存在，但执行边界仍然固定在 `database`
- request-level `schema` 不能被解释为独立切换执行命名空间
- 典型对应：`defaultSchemaSemantics = DATABASE_AS_SCHEMA`

#### `BEST_EFFORT`

- 数据库有原生 schema 概念
- MCP 可以尝试把 request-level `schema` 用作未限定对象名的执行 hint
- 但 capability 不承诺 strict guarantee
- 要求 deterministic target 时，应以 SQL 自身的显式限定关系为准

### Future Extension

如果后续真的实现了 vendor-specific、可验证的 strict schema selection，
再在 follow-up 中新增 `STRICT` 或等价取值。
V1 当前不抢跑。

## Decision 5: 不通过新增 fail-fast 校验来“修复” schema 语义

### Decision

- 本轮不新增 “schema 无法严格生效就直接报错” 的路径
- 问题优先通过 capability 诚实表达、metadata 统一命名和契约澄清来解决

### Why

- 用户已经明确不希望通过加校验来修复 review point
- 当前问题本质上是产品语义与接口定义不完整，
  不是单纯缺一个异常
- 先把 contract 健全，再决定是否需要更强 enforcement，风险更小

### Consequence

- MySQL-like 数据库不需要因为传了 `schema` 就新增 request rejection
- PostgreSQL-like 数据库也不需要因为做不到 strict guarantee 就新增 fail-fast
- 但是 capability 与文档必须足够清楚，不能让调用方误会系统已经提供了严格保证

## Decision 6: SQL 自身限定名优先于 request-level `schema`

### Decision

- 当 SQL 文本已经包含显式 schema-qualified 或等价 fully-qualified 名称时，
  request-level `schema` 不能覆盖 SQL 自身含义

### Why

- SQL 文本是最终执行对象解析的直接输入
- 如果 request-level `schema` 还能覆盖 SQL 里的显式限定，
  会产生更高层级的不确定性
- 这也为 Agent 提供了一个简单规则：
  需要 deterministic targeting 时，用 SQL 自身显式表达

## Preliminary Dialect Mapping

### Default Rule

- `defaultSchemaSemantics = DATABASE_AS_SCHEMA` 的数据库，
  默认映射到 `schemaExecutionSemantics = FIXED_TO_DATABASE`
- `defaultSchemaSemantics = NATIVE_SCHEMA` 的数据库，
  默认从 `schemaExecutionSemantics = BEST_EFFORT` 起步

### Notes

- 这是一条 V1 的 capability 发布规则，
  不是对所有数据库底层行为完全相同的断言
- 后续若某个方言能提供更强保证，
  需要单独 feature 再提升能力枚举，而不是在本轮泛化承诺

## Implementation Consequences

后续实现至少要覆盖四件事：

1. capability model 增加 `schemaExecutionSemantics`
2. no-native-schema 数据库的 metadata loader 用逻辑 `database` 名称填充公共 `schema`
3. `execute_query` tool descriptor 和 `001` contract 正式纳入 `schema` 字段，
   并把描述改成 optional namespace hint
4. `MCPJdbcStatementExecutor` 的 schema 应用逻辑与 capability 语义对齐，
   避免把 public label 冒充 strict selector
