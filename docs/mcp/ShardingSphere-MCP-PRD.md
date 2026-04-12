# ShardingSphere MCP PRD

## 文档信息
- 产品名称：ShardingSphere MCP
- 文档版本：定稿候选版
- 文档类型：PRD
- 当前阶段：需求确认版

## 1. 产品概述
- ShardingSphere MCP 是面向大模型、Agent 和 AI 平台的统一数据库 MCP 入口。
- 它对上提供统一的数据库访问与 SQL 执行公共面。
- 它对下屏蔽多种数据库在对象暴露、SQL 执行、结果模型、错误语义和治理边界上的差异。

### 一句话定义
- 上层只需接入一个 ShardingSphere MCP 服务，即可用一致方式访问多种数据库的结构信息、可用对象范围和 SQL 执行能力。

## 2. 背景与问题
- 当前数据库接入主要存在以下问题：
- 不同数据库 MCP 的资源命名、工具定义、返回结构和能力边界不一致。
- 上层 Agent 需要分别适配多种数据库，接入成本高。
- 企业希望统一数据库访问的对象暴露、审计、超时和结果限制。
- 即使同样是列库、查结构、执行 SQL，不同数据库的行为、错误和结果表达也不一致。
- ShardingSphere 具备统一接入多种数据库的能力，因此适合作为统一 MCP 出口。

## 3. 产品目标
- 统一不同数据库的 MCP 接入方式。
- 统一数据库对象发现与元数据读取能力。
- 统一 SQL 执行工具和执行结果模型。
- 统一错误语义、运行边界和事务语义。
- 统一能力声明表达。
- 降低 Agent 对多种数据库 MCP 的适配成本。
- 让结构变化和 DCL 变化能在分钟级反映到 MCP 出口。

## 4. 非目标
- 不负责自然语言理解、SQL 自动生成和问答体验。
- 不抹平所有数据库专有对象和专有语义。
- 不暴露 ShardingSphere 专有扩展面作为核心公共面。
- 不兼容第三方数据库 MCP Server 的命名和行为细节。
- 不支持 `USE`、`SET`、`COPY`、`LOAD`、`CALL`。
- 不支持各数据库专有高风险元命令。

## 5. 版本范围

### V1 正式支持数据库
- MySQL
- PostgreSQL
- openGauss
- SQL Server
- MariaDB
- Oracle
- ClickHouse
- Doris
- Hive
- Presto
- Firebird
- H2

### V1 不纳入正式验收范围
- 其他类 SQL 或测试型方言

## 6. 目标用户
- 大模型 Agent 开发者
- IDE / CLI 智能助手接入方
- 企业内部 AI 平台
- 数据分析 Agent
- 数据问答 Agent
- 运维排障 Agent
- 需要统一接入多种数据库的平台团队

## 7. 典型使用场景
- Agent 统一发现多个数据库中的可访问对象。
- Agent 统一读取表结构、字段、视图等元数据。
- Agent 在不同数据库下使用同一套工具执行 SQL。
- 平台统一承接审计、超时和结果限制要求。
- 数据库结构变化后，Agent 在分钟级看到 MCP 资源更新。
- MCP 会话内通过事务控制语句管理事务边界。

## 8. 产品原则
- 只做公共面，不做扩展面。
- 先统一高频对象和高频工具，再扩展长尾能力。
- 统一调用语义优先于统一底层实现方式。
- 能统一的能力必须稳定一致，不能统一的能力必须明确标识。
- 安全保守优先于能力激进。
- 所有 V1 公共对象必须有正式承载路径。
- 所有 V1 正式支持数据库必须满足统一契约。
- 对原生支持事务控制的数据库，V1 必须统一支持事务控制语义。
- 对原生支持 `savepoint` 的数据库，V1 必须统一支持 `savepoint` 语义。
- 对原生不支持事务控制或 `savepoint` 的数据库，必须通过 `capability` 明确声明不支持，并对相关语句统一返回 `unsupported`。

## 9. 统一对象模型

### 9.1 V1 核心统一对象
- `database`
- `schema`
- `table`
- `view`
- `column`
- `capability`

### 9.2 V1 可选公共对象
- `index`

#### 说明
- `index` 不是所有正式支持数据库的统一强制基线对象。
- `index` 是否支持由数据库级 `capability` 的 `supported_object_types` 明确声明。

### 9.3 V1 不统一对象
- `materialized view`
- `sequence`
- `function / procedure`
- `trigger`
- `event`
- `synonym`
- 数据库专有对象

## 10. 对象语义定义
- `database`：MCP 对外暴露的一级访问目标，也是每次 SQL 执行必须显式指定的目标。
- `schema`：`database` 内部命名空间；在 `execute_query` 中只作为可选 namespace hint，不是第二个强执行边界。
- `table / view`：通过 `(database, schema, name)` 唯一确定。
- `column`：从属于某个表或视图。
- `index`：从属于某个表，仅在当前 `database` 支持时暴露。
- `capability`：描述服务级或数据库级能力边界的声明对象。

### 统一约束
- 每次 SQL 执行必须命中一个且仅一个 `database`。
- V1 不支持跨 `database` SQL 执行。
- 对没有独立 `schema` 概念的数据库，系统仍需暴露统一 `schema` 语义。
- 对这类数据库，默认 `schema` 名称与 `database` 名称保持一致。
- 对这类数据库，公共 `schema` 名称首先服务 MCP 统一 contract；
  是否可作为独立执行命名空间切换由数据库级 capability 的 `schema_execution_semantics` 决定。
- 本文中的 `database` 指 ShardingSphere MCP 对外暴露的逻辑数据库标识，不等同于底层物理数据库实例、存储单元或原生 `catalog`。

## 11. V1 公共 Resources

### 正式资源清单
- `shardingsphere://capabilities`
- `shardingsphere://databases`
- `shardingsphere://databases/{database}`
- `shardingsphere://databases/{database}/capabilities`
- `shardingsphere://databases/{database}/schemas`
- `shardingsphere://databases/{database}/schemas/{schema}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables`
- `shardingsphere://databases/{database}/schemas/{schema}/views`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`

### 资源原则
- 资源只表达稳定、可读取、可理解的数据库结构和治理信息。
- 凡列为 V1 公共对象者，必须在 `resources` 和 / 或 `tools` 中有正式承载路径。
- 当当前 `database` 的 `supported_object_types` 不包含 `index` 时，`index` 相关 `resources` 统一返回 `unsupported`。

## 12. V1 公共 Tools

### 正式工具清单
- `list_databases`
- `list_schemas`
- `list_tables`
- `list_views`
- `list_columns`
- `list_indexes`
- `search_metadata`
- `describe_table`
- `describe_view`
- `get_capabilities`
- `execute_query`

### 工具定义
- `list_databases`
- `list_schemas(database)`
- `list_tables(database, schema, search?, page_size?, page_token?)`
- `list_views(database, schema, search?, page_size?, page_token?)`
- `list_columns(database, schema, object_type, object_name, search?, page_size?, page_token?)`
- `list_indexes(database, schema, table, search?, page_size?, page_token?)`
- `search_metadata(database?, schema?, query, object_types?, page_size?, page_token?)`
- `describe_table(database, schema, table)`
- `describe_view(database, schema, view)`
- `get_capabilities(database?)`
- `execute_query(database, schema?, sql, max_rows?, timeout_ms?)`

### 补充定义
- `object_type` 取值为 `table` 或 `view`。
- `execute_query` 每次只允许执行单条 SQL，多语句统一返回 `invalid_request`。
- `database` 是 `execute_query` 的唯一强边界。
- `schema` 是可选 namespace hint，用于表达未限定对象名的目标命名空间意图。
- SQL 已显式包含限定名时，SQL 自身限定关系优先于 request-level `schema`。

#### `search_metadata` 返回项至少应包含
- `database`
- `schema`
- `object_type`
- `object_name`
- `parent_object_type`
- `parent_object_name`

#### 额外约束
- 对 `table / view`，父对象字段可为空。
- 对 `column / index`，父对象字段必填。
- `get_capabilities()` 返回服务级 `capability`。
- `get_capabilities(database)` 返回数据库级 `capability`。
- 当当前 `database` 不支持 `index` 时，`list_indexes` 统一返回 `unsupported`。

## 13. 搜索、过滤与分页

### V1 必须支持
- 列表查询支持关键字搜索。
- 列表查询支持分页。
- 元数据搜索支持按对象类型过滤。
- 分页结果必须返回下一页标识或等价机制。
- 不允许客户端通过完整枚举完成大型数据库结构发现。

### `search_metadata.object_types` 正式支持
- `database`
- `schema`
- `table`
- `view`
- `column`
- `index`

### 补充规则
- `search_metadata` 在 `database` 省略时，表示在当前 runtime 暴露的所有 `database` 范围内搜索元数据。
- 该行为不等同于跨 `database` SQL 执行。
- 当 `schema` 被指定时，`database` 必须同时指定，否则返回 `invalid_request`。

## 14. SQL 执行能力
- SQL 执行通过 `execute_query` 统一暴露。
- 每次 SQL 必须显式指定 `database`。
- V1 不支持跨 `database` SQL 执行。
- `schema` 不构成第二个强执行边界。
- 同一 `database` 内的跨 `schema` SQL 可在数据库能力允许时支持。
- 不支持时必须明确返回 `unsupported`。
- 事务一旦开始，当前事务绑定单一 `database`。
- 事务存续期间若 `execute_query` 指定其他 `database`，统一返回 `conflict`。

## 15. V1 允许的 SQL 类型

### 允许
- `SELECT`
- `WITH ... SELECT`
- `INSERT`
- `UPDATE`
- `DELETE`
- `MERGE`
- `TRUNCATE`
- `CREATE`
- `ALTER`
- `DROP`
- `GRANT`
- `REVOKE`
- `EXPLAIN ANALYZE`
- `BEGIN`
- `START TRANSACTION`
- `COMMIT`
- `ROLLBACK`
- `SAVEPOINT`
- `ROLLBACK TO SAVEPOINT`
- `RELEASE SAVEPOINT`

### 不允许
- `USE`
- `SET`
- `COPY`
- `LOAD`
- `CALL`
- 各数据库专有高风险元命令

### 补充说明
- 允许的 SQL 类型表示 V1 协议允许承载的语句范围，不代表所有正式支持数据库都必须支持该范围内的每一种语句。
- 是否支持以数据库级 `capability` 为准，不支持时统一返回 `unsupported`。

#### V1 对事务语义的一致性承诺分为两层
- 事务控制语义：`BEGIN`、`START TRANSACTION`、`COMMIT`、`ROLLBACK`
- `savepoint` 语义：`SAVEPOINT`、`ROLLBACK TO SAVEPOINT`、`RELEASE SAVEPOINT`
- 对原生支持事务控制的 `database`，统一承诺事务控制语义。
- 对原生支持 `savepoint` 的 `database`，统一承诺 `savepoint` 语义。
- 对不支持事务控制或 `savepoint` 的 `database`，相关语句统一返回 `unsupported`。
- DDL、DCL、`EXPLAIN ANALYZE` 在事务中的行为遵循数据库原生语义，并由 `capability` 显式声明。

## 16. MCP 会话与事务语义
- 每个 MCP 会话对应一个逻辑会话上下文。
- V1 内置运行时直接使用 `session_id` 作为审计标签。
- 会话期间该 `session_id` 保持不变。
- 会话不支持恢复；会话结束即逻辑上下文结束。
- 默认执行模式为 `autocommit = true`。
- 对原生支持事务控制的 `database`，显式执行 `BEGIN` 或 `START TRANSACTION` 后进入事务态。
- 对原生支持 `savepoint` 的 `database`，支持 `SAVEPOINT`、`ROLLBACK TO SAVEPOINT`、`RELEASE SAVEPOINT`。
- 当前事务上下文仅在当前 MCP 会话内有效。
- 事务存续期间绑定单一 `database`，不允许切换目标数据库。
- MCP 会话结束时，未提交事务统一自动回滚。
- V1 不支持通过 `USE` 或 `SET` 修改会话上下文。
- 对原生不支持事务控制或 `savepoint` 的 `database`，相关事务语句统一返回 `unsupported`。

## 17. 统一执行结果模型

### `result_set`
- 用于查询类语句，至少包含：
- `result_kind`
- `columns`
- `rows`
- `truncated`

### `update_count`
- 用于 DML，至少包含：
- `result_kind`
- `affected_rows`

### `statement_ack`
- 用于 DDL、DCL、事务控制和其他非结果集语句，至少包含：
- `result_kind`
- `statement_type`
- `status`
- `message`

### 补充约束
- `INSERT`、`UPDATE`、`DELETE`、`MERGE` 返回 `update_count`。
- `CREATE`、`ALTER`、`DROP`、`TRUNCATE`、`GRANT`、`REVOKE`、事务控制语句默认返回 `statement_ack`。
- `EXPLAIN ANALYZE` 的返回形态由数据库级 `capability` 决定。
- 一次 `execute_query` 只返回一个结果对象。
- V1 不支持多结果集返回。

## 18. 统一结果数据类型约束
- `columns` 至少包含字段名、统一逻辑类型、底层原生类型、是否可空。
- `rows` 必须与 `columns` 顺序对齐。
- `null` 值必须一致表达。
- 日期、时间、时间戳必须使用统一可解析格式返回。
- 高精度 `decimal / numeric` 建议按字符串返回，避免精度丢失。
- `JSON`、数组、二进制等复杂类型必须使用统一可识别表达方式。
- 结果被截断时必须显式返回 `truncated=true`。

## 19. 能力声明

### 19.1 服务级 capability
- 服务级 `capability` 只表达协议公共能力，至少包含：
- `supported_resources`
- `supported_tools`
- `supported_statement_classes`

### 19.2 数据库级 capability
- 数据库级 `capability` 表达某个 `database` 的具体行为边界，至少包含：
- `supported_object_types`
- `supported_statement_classes`
- `supports_transaction_control`
- `supports_savepoint`
- `supported_transaction_statements`
- `default_autocommit`
- `max_rows_default`
- `max_timeout_ms_default`
- `default_schema_semantics`
- `schema_execution_semantics`
- `supports_cross_schema_sql`
- `supports_explain_analyze`
- `ddl_transaction_behavior`
- `dcl_transaction_behavior`
- `explain_analyze_result_behavior`
- `explain_analyze_transaction_behavior`

### 19.3 枚举约束
- `ddl_transaction_behavior = uniform | native | unsupported`
- `dcl_transaction_behavior = uniform | native | unsupported`
- `explain_analyze_result_behavior = result_set | statement_ack | unsupported`
- `explain_analyze_transaction_behavior = uniform | native | unsupported`

### 19.4 说明
- `default_autocommit` 指 ShardingSphere MCP 会话默认行为，不等同于底层数据库原生默认 `autocommit`。
- `default_schema_semantics` 解决 metadata/discovery 侧的统一 schema 语义。
- `schema_execution_semantics` 解决 `execute_query.schema` 在执行时如何解释。
- 对原生支持事务控制的 `database`，`supports_transaction_control = true`。
- 对原生不支持事务控制的 `database`，`supports_transaction_control = false`。
- 对原生支持 `savepoint` 的 `database`，`supports_savepoint = true`。
- 对原生不支持 `savepoint` 的 `database`，`supports_savepoint = false`。
- `index` 是否支持由 `supported_object_types` 明确声明。
- DDL、DCL、`EXPLAIN ANALYZE` 的事务边界行为必须由数据库级 `capability` 明确声明。

## 20. 运行边界与审计

### V1 当前实现
- 内置 runtime 不拦截调用请求。
- HTTP 端点如需对外暴露，应放在受信网络、上游网关、反向代理或其他网络边界之后。
- 元数据读取、工具调用与 SQL 执行都必须纳入审计。

### V1 不纳入正式验收
- 内置对象级可见性裁剪
- 内置语句类别拦截
- 列级可见性裁剪
- 字段脱敏
- 行级数据过滤

## 22. 元数据变化感知与同步 SLA

### V1 正式承诺
- 结构变化和 DCL 变化必须在 1 分钟内反映到 MCP 公共面。
- 由 ShardingSphere 负责感知变化并同步 MCP 出口。

### 覆盖变化包括
- `schema` 新增、删除、重命名
- `table / view` 新增、删除、重命名
- `column` 新增、删除、重命名、类型变化
- `index` 新增、删除

### 补充规则
- 若结构变更通过当前 MCP 会话执行成功，当前会话中应尽快可见。
- 全局视角仍以 1 分钟内同步为准。

## 23. 审计基线

### 审计记录至少应包含
- `session_id`
- `database`
- `operation_class`
- `operation_digest`
- `success_or_failure`
- `error_code`
- `transaction_marker`
- `timestamp`

### 补充说明
- `operation_class` 统一表示 `resource_read`、`metadata_tool`、`query_execution`。
- 对 SQL，`operation_digest` 表示语句摘要。
- 对 `resources / tools`，`operation_digest` 表示资源路径或工具调用摘要。
- `transaction_marker` 至少能标识 `BEGIN`、`COMMIT`、`ROLLBACK`、`SAVEPOINT`。

## 24. 错误模型

### 统一错误码
- `invalid_request`
- `not_found`
- `unsupported`
- `conflict`
- `unavailable`
- `transaction_state_error`
- `query_failed`

### 补充规则
- 在无活动事务时执行 `COMMIT`、`ROLLBACK` 或引用不存在的保存点，返回 `transaction_state_error`。
- 当前 `database` 不支持事务控制时执行事务控制语句，返回 `unsupported`。
- 当前 `database` 不支持 `savepoint` 时执行 `savepoint` 语句，返回 `unsupported`。

## 25. V1 最低统一能力基线
- V1 正式支持数据库必须统一满足以下最低能力基线：
- 统一支持核心对象模型：`database / schema / table / view / column / capability`
- 统一支持公共 `tools`：`list_databases / list_schemas / list_tables / list_views / list_columns / search_metadata / describe_table / describe_view / get_capabilities / execute_query`
- 统一支持 `result_set / update_count / statement_ack` 三类结果模型
- 统一支持结构变化在 1 分钟内同步到 MCP 出口
- 对原生支持事务控制的 `database`，统一支持 `BEGIN / START TRANSACTION / COMMIT / ROLLBACK`
- 对原生支持 `savepoint` 的 `database`，统一支持 `SAVEPOINT / ROLLBACK TO SAVEPOINT / RELEASE SAVEPOINT`
- 对不支持事务控制或 `savepoint` 的 `database`，相关 `capability` 必须明确声明，相关语句统一返回 `unsupported`
- `index` 不属于 V1 强制统一基线对象，是否支持由 `supported_object_types` 声明
- 数据库级 `capability` 必须显式声明 `supported_statement_classes`

## 26. V1 事务能力矩阵
- MySQL：`supports_transaction_control=true`，`supports_savepoint=true`
- PostgreSQL：`true`，`true`
- openGauss：`true`，`true`
- SQL Server：`true`，`true`
- MariaDB：`true`，`true`
- Oracle：`true`，`true`
- ClickHouse：`false`，`false`
- Doris：`true`，`false`
- Hive：`false`，`false`
- Presto：`true`，`false`
- Firebird：`true`，`true`
- H2：`true`，`true`

### 说明
- 上述矩阵必须与数据库级 `capability` 的真实返回保持一致。
- 若后续版本、部署模式或产品范围变化导致某 `database` 的事务能力变化，必须同步更新 `capability` 与验收口径。

## 27. 成功指标
- 支持数据库的正式接入数量
- 上层 Agent 的单次接入成功率
- 多数据库统一工具适用率
- 元数据同步在 1 分钟内达标比例
- SQL 执行成功率
- 事务控制成功率
- `savepoint` 成功率
- 运行边界控制效果
- 上层平台对多数据库 MCP 适配代码的减少比例

## 28. 主要风险
- 不同数据库对象语义差异过大，导致公共面过度抽象。
- 支持矩阵扩大后，不同数据库在 `schema` 语义、对象模型、结果类型和执行能力上的差异显著增加。
- ClickHouse、Doris、Hive、Presto 等数据库会成为统一契约的一致性风险重点区域。
- 结果模型不稳定会导致上层仍需写数据库分支。
- 元数据同步不稳定会直接影响 MCP 可信度。
- 事务语义、`savepoint` 语义、写操作边界和多数据库一致性控制不清会带来安全风险。

## 29. 标准 Demo

### 说明原则
- 服务级 `capability` 只表达协议公共能力。
- 数据库级 `capability` 决定具体 `database` 的事务、高级语句、对象类型和边界行为。
- DDL、DCL、`EXPLAIN ANALYZE` 在事务中的行为以数据库级 `capability` 为准。
- `index` 相关能力仅在 `supported_object_types` 包含 `index` 时暴露。

## 30. 压缩版需求清单
1. 必须提供统一数据库访问与 SQL 执行公共面。
2. 必须统一 `database / schema / table / view / column / capability` 对象语义。
3. `index` 必须作为数据库级可选公共对象，通过 `capability` 明确声明。
4. 必须支持对象发现的搜索、过滤与分页。
5. 必须提供正式公共工具 `execute_query`。
6. 必须支持通用 SQL 执行，但 V1 不包含 `USE`、`SET`、`COPY`、`LOAD`、`CALL` 和各数据库专有高风险元命令。
7. 必须定义 MCP 会话语义、审计标签和事务语义。
8. 必须区分事务控制语义与 `savepoint` 语义。
9. 必须提供统一执行结果模型。
10. 必须补充服务级 `capability` 与数据库级 `capability` 的正式内容模型。
11. 必须保证结构变化在 1 分钟内同步到 MCP 出口。
12. 必须提供统一错误模型，覆盖冲突、事务和执行失败。
13. HTTP 端点如需对外暴露，必须放在受信网络、上游网关、反向代理或其他网络边界之后。
14. 必须明确 DDL、DCL、`EXPLAIN ANALYZE` 在事务中的行为遵循数据库原生语义，并通过 `capability` 声明。
15. 必须提供正式支持数据库的事务能力矩阵，并保证与 `capability` 一致。

## 31. 压缩版验收清单
1. Agent 只接入一个 ShardingSphere MCP 服务即可访问和执行多种数据库上的统一 SQL 公共能力。
2. V1 正式支持数据库都满足统一对象模型、统一 `tools`、统一结果模型和统一错误语义。
3. `search_metadata` 支持 `database / schema / table / view / column / index`。
4. 对原生支持事务控制的 `database`，事务控制语义稳定一致。
5. 对原生支持 `savepoint` 的 `database`，`savepoint` 语义稳定一致。
6. 对不支持事务控制或 `savepoint` 的 `database`，相关语句统一返回 `unsupported`。
7. `index` 仅在 `supported_object_types` 包含该对象时暴露。
8. 通过 MCP 执行成功的结构变更，在当前会话中可快速可见，并在 1 分钟内全局可见。
9. `USE`、`SET`、`COPY`、`LOAD`、`CALL` 在 V1 中被统一拒绝，并返回一致错误语义。
10. DDL、DCL、`EXPLAIN ANALYZE` 在事务中的行为差异已通过数据库级 `capability` 明确声明。
11. MCP 会话结束后上下文不恢复，未提交事务自动回滚。
12. 事务能力矩阵与实际 `capability` 返回一致。
13. 上层不再需要为不同数据库单独适配对象发现、执行工具和返回结构。
