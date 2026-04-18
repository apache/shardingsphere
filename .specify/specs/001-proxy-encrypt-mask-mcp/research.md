# Research: ShardingSphere-Proxy Encrypt and Mask MCP V1

## 研究目标

这份研究文档回答两个问题：

- 现有 ShardingSphere / MCP 代码已经提供了哪些可复用能力；
- 为了满足本次需求，哪些设计决策应该提前锁定，避免后续实现阶段反复返工。

## R1. 规则工作流采用专用 MCP 工具与资源，而不是让模型直接裸调 `execute_query`

- **结论**
  - 采用“专用工作流工具 + 规则/插件资源 + 受控 SQL/DistSQL 执行”的组合设计。
  - `execute_query` 保留为底层执行能力，不作为面向自然语言规则编排的唯一入口。
- **依据**
  - `mcp/core/.../ExecuteSQLToolHandler.java` 当前只暴露 `database / schema / sql` 等通用参数。
  - `mcp/core/.../MCPSQLExecutionFacade.java` 负责 SQL 分类、执行、元数据刷新，但不理解审批、追问、步骤列表和算法推荐。
  - `mcp/core/.../StatementClassifier.java` 关注的是 SQL 合约安全，不负责业务规划。
- **为什么这样定**
  - 需求里有强约束：先出全局步骤、支持一步一步、反复追问、展示命名方案、区分审批模式。
  - 这些都不是裸 SQL 执行器应该承担的职责。
- **备选方案**
  - 让 LLM 直接拼 SQL / DistSQL 再执行。
  - 否决原因：无法稳定表达审批状态、命名冲突策略和校验闭环，风险过高。

## R2. `database` 必须是显式入参，不能依赖 `USE`

- **结论**
  - 所有规划、执行、校验相关工具都必须要求显式 `database`。
- **依据**
  - `StatementClassifier.java` 明确把 `USE` 识别为 banned command。
  - Proxy DistSQL 执行链依赖当前库或语句内显式库上下文。
- **为什么这样定**
  - 这与用户已确认的“必须显式数据库上下文”一致。
  - 也避免 MCP 会话切库导致的隐式副作用。
- **备选方案**
  - 通过 session 预设当前库。
  - 否决原因：与 MCP 现有 SQL 合约冲突，也会让一步一步执行的状态更难追踪。

## R3. 规则与算法清单优先复用现有 DistSQL 查询能力

- **结论**
  - 现有规则和算法信息优先从现有 DistSQL 语义提取，而不是为 encrypt / mask 自造另一套查询协议。
- **依据**
  - `ShowEncryptRuleExecutor.java` 已能返回逻辑列、密文字段、辅助查询字段、模糊查询字段与算法配置。
  - `ShowMaskRuleExecutor.java` 已能返回表、列、算法类型和参数。
  - `EncryptDistSQLStatementVisitor.java` 与 `MaskDistSQLStatementVisitor.java` 已把
    `SHOW ENCRYPT ALGORITHM PLUGINS` / `SHOW MASK ALGORITHM PLUGINS` 映射到 `ShowPluginsStatement`。
  - `infra/distsql-handler/.../ShowPluginsExecutor.java` 已基于 SPI 服务加载器返回插件列表。
- **为什么这样定**
  - 这天然覆盖内置算法与当前 Proxy 可见的自定义 SPI 算法。
  - 也符合“让 ShardingSphere 干正确的活，MCP 不多干活”的要求。
- **备选方案**
  - MCP 直接自己扫描类路径或复制算法元数据。
  - 否决原因：容易和 Proxy 真实可见插件集合不一致。

## R4. 逻辑视图始终是用户主视角，物理视图只作为实现工件

- **结论**
  - 工作流对用户展示时，默认聚焦逻辑库 / 逻辑表 / 逻辑列。
  - 物理列和索引只在审阅、执行、校验摘要中显式展开。
- **依据**
  - `docs/document/content/reference/encrypt/_index.cn.md` 明确强调 `logicColumn` 是用户编写 SQL 的主入口。
  - 用户已确认“无论加密前后都是逻辑视图，只不过加密前逻辑视图等于物理视图”。
- **为什么这样定**
  - 这是这次产品能力的认知核心，不能让用户被物理列细节拖着走。
- **备选方案**
  - 以物理列 DDL 为第一视图。
  - 否决原因：不符合 ShardingSphere Encrypt 的核心抽象，也不利于自然语言交互。

## R5. 加密与脱敏共用一个工作流入口，但在规划阶段分叉

- **结论**
  - 入口统一，规划与执行分叉。
  - `encrypt` 分支需要考虑派生列、索引和 DistSQL。
  - `mask` 分支优先是规则配置，不强制要求物理 DDL。
- **依据**
  - Encrypt DistSQL 规则结构天然包含 `CIPHER / ASSISTED_QUERY / LIKE_QUERY`。
  - Mask DistSQL 规则结构只关注逻辑列与算法。
- **为什么这样定**
  - 用户要求“一起交付”，但又不希望 MCP 对 ShardingSphere 现有机制做额外抽象负担。
- **备选方案**
  - 拆成两个完全独立产品流。
  - 否决原因：会重复一整套步骤清单、审批模式、校验模型和元数据读取。

## R6. 派生列类型策略直接沿用 ShardingSphere 当前默认策略

- **结论**
  - MCP 不自行做源列类型推断。
  - V1 直接沿用当前加密 DDL 默认策略。
- **依据**
  - `EncryptCreateTableTokenGenerator.java` 与 `EncryptAlterTableTokenGeneratorTest.java` 都体现了派生列默认数据类型为 `VARCHAR(4000)`。
  - `EncryptColumnDataType.DEFAULT_DATA_TYPE` 当前即 `VARCHAR(4000)`。
- **为什么这样定**
  - 这是用户明确要求。
  - 这样实现风险最低，也避免 MCP 生成与内核预期不一致的 DDL。
- **备选方案**
  - 按原始列类型映射出更细的物理列类型。
  - 否决原因：那会把 MCP 变成额外的类型推断层，违反“不要多干活”的原则。

## R7. MCP 默认命名采用 `*_cipher` / `*_assisted_query` / `*_like_query`，并显式处理与内核命名差异

- **结论**
  - 对外默认命名采用用户确认的后缀风格。
  - 发生冲突时使用数字后缀，例如 `phone_cipher_1`。
- **依据**
  - 用户已明确要求默认生成 `*_cipher`、`*_assisted_query`、`*_like_query`。
  - `EncryptDerivedColumnSuffix.java` 体现的内核派生后缀是 `_C / _A / _L`。
- **为什么这样定**
  - 这说明 MCP 命名策略必须自己显式产出，而不是依赖某个内部默认后缀。
  - 同时也要把最终改名结果返回给用户。
- **备选方案**
  - 沿用 `_C / _A / _L`。
  - 否决原因：已与本次产品要求冲突。

## R8. 不默认复用已有同名物理列

- **结论**
  - 即使历史物理列名看起来“正好匹配”，V1 也默认新建一套，再做冲突规避。
- **依据**
  - 用户已经明确“默认新建一套再自动改名”。
- **为什么这样定**
  - 这样更安全，避免误接入历史上语义不明或用途未知的列。
- **备选方案**
  - 优先复用同名列。
  - 否决原因：无法保证历史列的真实用途和数据语义。

## R9. 校验范围严格限定为规则、元数据、DDL 和 SQL 可执行性

- **结论**
  - V1 不做历史数据校验，不做结果正确性校验，不做样本数据结果比对。
- **依据**
  - 用户已确认“只做规则 / 元数据 / SQL 可执行性验证”。
  - 用户也明确“不处理数据”。
- **为什么这样定**
  - 这能把 V1 聚焦在可实施、可控、风险可收敛的范围内。
- **备选方案**
  - 执行真实读写回归或数据抽样比对。
  - 否决原因：会越过本轮边界，并引入额外权限与数据敏感性问题。

## R10. 样本数据只作为升级路径，不是默认依赖

- **结论**
  - 默认不读取样本数据。
  - 仅在元数据不足以判断字段语义、且用户明确授权时，才允许少量样本读取。
- **依据**
  - 用户曾问“看少量样本数据有什么用”，且前文已确认样本数据不是默认路径。
- **为什么这样定**
  - 样本读取的价值主要在于推断字段语义与展示效果，不在于规则可执行性。
- **备选方案**
  - 进入工作流就默认读取样本。
  - 否决原因：越权且不必要。

## R11. 索引建议与索引 DDL 必须纳入计划工件，但仍受审批模式约束

- **结论**
  - 对 `assisted_query` 和 `like_query` 派生列，要同步给出索引建议，必要时给出索引 DDL。
- **依据**
  - 用户已确认“要”。
  - 需求已经把查询可用性纳入交付，而不是只追求规则语法成立。
- **为什么这样定**
  - 不把索引纳入计划，会让工作流只完成“能配”，但没有完成“能用”。
- **备选方案**
  - 只提示文本建议，不生成索引工件。
  - 否决原因：对 review-first 和 manual-only 两种模式不够实用。

## R12. V1 删除生命周期覆盖 `encrypt drop` 与 `mask drop`，但边界要显式收紧

- **结论**
  - V1 的删除生命周期覆盖 `encrypt drop` 与 `mask drop`。
  - `mask drop` 继续采用 rule-only 删除路径。
  - `encrypt drop` 至少要覆盖 `DROP ENCRYPT RULE`。
  - 冗余派生列 / 索引的 cleanup 不纳入 MCP 当前交付范围，由用户自行处理。
- **依据**
  - 用户最新确认 `encrypt drop` 需要做。
  - 用户同时坚持“不处理数据”“不做回滚”，因此 encrypt drop 的边界必须锁定在规则生命周期、物理工件规划和验证层面。
- **为什么这样定**
  - 这样既满足“新建、修改、删除都支持”的产品目标，又不越过历史数据恢复边界。
  - 对 encrypt 来说，真正高风险的不是 `DROP ENCRYPT RULE` 本身，而是是否同步删除派生物理工件；把 cleanup 留给用户处理，边界更清晰。
- **备选方案**
  - 继续把 encrypt drop 排除在外。
  - 否决原因：已与最新产品范围冲突。

## R12A. MCP 不承担强语义自然语言理解职责，优先消费上游结构化意图

- **结论**
  - 自然语言入口可以保留，但 MCP 的主输入应是上游模型整理后的结构化意图。
  - MCP 只负责规则相关缺口追问、规划、执行与验证。
- **依据**
  - 用户已明确指出，自然语言理解的主要责任应在大模型，而不是 MCP。
- **为什么这样定**
  - 这更符合“让 ShardingSphere 干正确的活，MCP 不多干活”的原则。
  - 也能避免把 MCP 设计成另一个弱化版意图理解器。
- **备选方案**
  - 让 MCP 自己负责从完全自由文本中做强语义理解。
  - 否决原因：职责边界错误，且会模糊当前需求的实现重点。

## R13. `SHOW ... ALGORITHM PLUGINS` 只适合作为发现入口，不足以单独完成 encrypt 能力推荐

- **结论**
  - `SHOW ENCRYPT ALGORITHM PLUGINS` / `SHOW MASK ALGORITHM PLUGINS` 适合作为推荐池发现入口。
  - 对 encrypt 来说，最终能力判断还需要补充 SPI 元数据或实例探测。
- **依据**
  - `ShowPluginsExecutor.java` 当前只返回 `type`、`type_aliases`、`description` 以及少数场景下的 `supported_database_types`。
  - `PluginMetaDataQueryResultRow.java` 的行构建也只覆盖类型、别名、数据库范围和描述。
  - 但 `EncryptAlgorithmMetaData` 实际上还定义了 `supportDecrypt`、`supportEquivalentFilter`、`supportLike` 三个能力位。
- **为什么这样定**
  - 只靠 plugin 列表，无法直接知道某个 encrypt 算法能否满足“可解密 / 等值查询 / 模糊查询”。
- **备选方案**
  - 仅基于 `show plugins` 结果做最终推荐。
  - 否决原因：缺少关键能力位，推荐结果不可靠。

## R14. 算法参数采集应采用“先选算法，再采参数”的两段式模型

- **结论**
  - 先完成算法族推荐与选择，再采集算法参数。
  - 参数分为 `required`、`optional`、`secret` 三类。
  - `secret` 参数保存在会话上下文中，review 时打码。
- **依据**
  - 内置 encrypt 与 mask 算法都存在稳定的属性键模式，但 SPI 公共层并没有统一的参数 schema 自描述。
- **为什么这样定**
  - 这能兼顾操作简洁性和敏感信息保护。
- **备选方案**
  - 让用户在自然语言阶段一次性给全量参数。
  - 否决原因：体验差，也更容易泄露敏感值。

## R15. 运行拓扑必须显式收敛到 Proxy 逻辑视角

- **结论**
  - 后续实现必须以“连接 Proxy、按 Proxy 逻辑视角执行和校验”为准。
- **依据**
  - 用户已明确确认 MCP 最终连的是 Proxy。
  - 用户已明确确认逻辑元数据验证基于 Proxy。
  - 当前 `mcp/core` 里的 SQL 执行和元数据装载仍然是 JDBC 直连范式。
- **为什么这样定**
  - 否则规格中的“逻辑视图”“规则验证”“Proxy 已安装 SPI 算法池”都可能和实际运行态脱节。
- **备选方案**
  - 延续现有 JDBC 直连物理库范式。
  - 否决原因：会与这次产品边界直接冲突。

## 最终设计约束

- 只支持 Proxy。
- 必须显式 `database`。
- 必须先给全局步骤清单。
- 必须支持一起做和一步一步做。
- 一步一步模式的已确认上下文由服务端保存。
- encrypt 支持 create / alter / drop。
- mask 支持 create / alter / drop。
- 加密默认可能带物理 DDL，脱敏默认规则优先。
- encrypt alter / drop 不做 cleanup 规划，由用户自行处理遗留物理工件。
- 不做历史数据处理。
- 不做回滚与审计落库。
- 不自动复用同名物理列。
- 冲突使用数字后缀。
- 推荐池覆盖内置算法与当前 Proxy 可见的自定义 SPI 算法。
- `show plugins` 负责发现，encrypt 能力推荐还要补 SPI 元数据或实例探测。
- MCP 优先消费上游结构化意图，而不是把强语义理解职责放在自身内部。
