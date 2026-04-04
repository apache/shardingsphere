# Feature Specification: ShardingSphere MCP Minimal LLM-Driven E2E Validation

**Feature Branch**: `[no-branch-switch-requested]`  
**Created**: 2026-04-05  
**Status**: Design draft  
**Input**: User description:
"这个项目现在的e2e离这个程度还差多远？",
"现在用speckit把这些都补全吧，但不允许切换分支。而且要建一个新的独立的目录，因为有别的进程也在跑speckit, 不要相互影响"

## Scope Statement

本 follow-up 只补当前缺失的这条验收闭环：

- 真实开源模型
- 真实 MCP client 行为
- 真实 MCP HTTP endpoint
- 真实数据库访问
- GitHub Actions 可跑的最小资源 smoke
- 本地可复现的同一路径

本轮的“真实数据库”以仓库内已经随发行包提供的 demo H2 runtime 为第一阶段验收基线。
它必须走打包后的 MCP distribution 和 JDBC-backed `runtimeDatabases`，
不能退回到 `MetadataCatalog` / `DatabaseRuntime` 的内存 fixture。

本轮只做最小可稳定闭环：

- 一个小模型 profile
- 一个固定 smoke 场景
- 只读查询
- 结构化 JSON 断言
- 完整 artifact 留存

本轮不做：

- 不替代现有 deterministic MCP E2E
- 不做模型能力 benchmark 或多模型横评
- 不把写操作、DDL、DCL 纳入第一阶段 LLM smoke
- 不引入依赖外部 SaaS API key 的默认 CI 路径
- 不切换分支
- 不修改其他正在使用的 Speckit 目录

## Problem Statement

当前仓库已经具备较完整的 MCP server-side E2E：

- 能启动真实 runtime
- 能通过 HTTP 完成 `initialize`
- 能调用 metadata tools 与 `execute_query`
- 能在 GitHub Actions 中做 distribution smoke

但还缺关键的一层：

- 没有真实模型参与
- 没有真实模型驱动的 MCP tool selection
- 没有真实模型输出的自动化判定
- 没有面向 GitHub-hosted runner 的最小资源 LLM E2E lane

这意味着当前测试只能证明 “server contract 正常”，
还不能证明 “真实模型经由 MCP 去访问数据库” 这条产品路径已经可回归、可验收、可放进 CI。

## User Scenarios & Testing *(mandatory)*

### User Story 1 - GitHub Actions 上的真实模型 smoke 能通过 MCP 访问数据库 (Priority: P1)

作为 MCP 维护者，我希望在 GitHub-hosted runner 上启动一个最小可用的本地开源模型，
让它通过 MCP 完成库表发现和只读查询，
这样仓库就能用自动化方式证明 “真实模型 -> MCP -> 数据库” 这条链路是真的通的。

**Why this priority**: 这是当前能力缺口最大的部分；没有真实模型参与，
现有 E2E 只能证明 server 正常，不能证明真实模型使用 MCP 的实际效果。

**Independent Test**: 在不依赖外部模型 API secret 的前提下，
启动本地模型服务和打包后的 MCP distribution，
驱动模型完成一个固定只读 smoke 场景；
通过条件是模型实际调用了 MCP discovery tool 和 `execute_query`，
并返回符合预期的结构化 JSON。

**Acceptance Scenarios**:

1. **Given** 打包后的 demo MCP runtime 默认暴露逻辑库 `orders` 和 `billing`，  
   **When** LLM runner 要求模型先定位包含订单表的逻辑库和 schema，  
   **Then** 模型必须通过 MCP discovery tool 找到 `orders.public.orders`，而不是直接猜测。
2. **Given** demo runtime 中 `orders.public.orders` 已有稳定种子数据，  
   **When** LLM runner 要求模型验证订单总数，  
   **Then** 模型必须通过 `execute_query` 执行只读 SQL 并得出 `total_orders = 2`。
3. **Given** GitHub Actions 运行环境没有外部闭源模型 key，  
   **When** 新的 LLM E2E workflow 执行，  
   **Then** 默认链路必须只依赖仓库、Docker、本地模型服务和本地 MCP runtime 完成闭环。

---

### User Story 2 - 失败要可诊断、结果要可判定 (Priority: P1)

作为 reviewer 和 CI 维护者，我希望模型驱动测试即使失败也能给出明确原因和证据，
这样我可以区分是模型没调工具、MCP 返回异常、还是最终结果断言失败。

**Why this priority**: 真实模型测试天然更容易抖动；
如果没有结构化断言和 artifact，失败就不可维护。

**Independent Test**: 模拟 “模型未调用工具”、“输出非 JSON”、“MCP 查询失败” 等场景时，
runner 能把失败原因分类并输出完整 artifact。

**Acceptance Scenarios**:

1. **Given** 模型直接输出答案而没有调用所要求的 MCP tool，  
   **When** LLM runner 完成判定，  
   **Then** 测试必须失败，并明确标记为 `missing_required_tool_coverage`。
2. **Given** 模型输出了多余 prose 或格式错误 JSON，  
   **When** runner 做最终断言，  
   **Then** 测试必须失败，并保留 raw model output、prompt、tool trace 和 assertion report。
3. **Given** MCP runtime 或模型服务在执行中报错，  
   **When** workflow 结束，  
   **Then** 必须上传可复盘 artifact，而不是只留下控制台日志。

---

### User Story 3 - 资源受控且本地可复现 (Priority: P2)

作为仓库维护者，我希望这条 LLM E2E lane 在资源受控的前提下独立运行，
优先作为 nightly / manual smoke，
并且本地可以按同一 contract 复现，
这样它既能补齐产品信心，又不会立刻拖垮主 CI。

**Why this priority**: 用户已经明确要求 GitHub Actions 可跑且尽量少消耗资源；
如果第一版就是重型 gate，会给现有 CI 带来不成比例的风险。

**Independent Test**: 在同一套 prompt、同一 MCP demo runtime 和同一模型 profile 下，
GitHub Actions 与本地命令行都能完成同一 smoke contract。

**Acceptance Scenarios**:

1. **Given** GitHub-hosted CPU runner，  
   **When** workflow 运行默认最小模型 profile，  
   **Then** 它必须在受控的 smoke 范围内完成，不依赖 GPU。
2. **Given** 开发者本地已经启动同类本地模型服务，  
   **When** 按 quickstart 运行指定测试命令，  
   **Then** 应能复现同一条 `orders.public.orders -> total_orders=2` 验收路径。
3. **Given** 当前仓库已有 deterministic MCP E2E 和 distribution smoke，  
   **When** 引入新的 LLM E2E lane，  
   **Then** 它必须是独立 workflow / 独立执行入口，而不是替换现有基础回归层。

### Edge Cases

- 模型在未调用任何 MCP tool 的情况下直接给出看似正确的答案。
- 模型只做了 metadata discovery，没有执行 `execute_query`。
- 模型试图执行 `UPDATE`、`DELETE`、DDL 或跨库 SQL。
- 模型输出有效 JSON，但内容与 tool trace 不一致。
- 模型服务冷启动慢，runner 在首次请求前未等待 ready。
- MCP runtime 初始化成功，但会话未在失败路径上正确关闭。
- artifact 写入共用目录导致不同 run 相互覆盖。
- GitHub-hosted runner 网络抖动导致模型拉取慢，workflow 需要把失败定位到 “environment warmup” 而不是业务断言。

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: 系统 MUST 提供一条新增的、与现有 deterministic MCP E2E 并存的 LLM-driven E2E 验收路径。
- **FR-002**: 该验收路径 MUST 使用真实模型生成的 tool-calling 决策，而不是用硬编码 `tools/call` 请求假装模型参与。
- **FR-003**: 该验收路径 MUST 连接打包后的 MCP distribution HTTP endpoint，而不是只连接测试内存 server。
- **FR-004**: 第一阶段默认验收 MUST 使用仓库内自带的 demo H2 `runtimeDatabases` 作为真实数据库基线，
  不得以 `MetadataCatalog` / `DatabaseRuntime` fixture 作为唯一成功路径。
- **FR-005**: 一个通过的 smoke run MUST 至少覆盖一个 metadata discovery tool 和一个 `execute_query` 调用。
- **FR-006**: 第一阶段 LLM smoke MUST 只允许只读 SQL，不允许写操作、DDL、DCL 或多语句输入。
- **FR-007**: 系统 MUST 以结构化 JSON 作为最终断言对象，而不是依赖自然语言答案文本。
- **FR-008**: 系统 MUST 记录至少以下 artifact：
  prompt、raw model output、tool trace、assertion report、MCP runtime log。
- **FR-009**: 当模型未达到必需 tool 覆盖、尝试执行非只读 SQL、最终 JSON 与期望不一致、
  或 runtime/mode 服务失败时，测试 MUST 明确失败并给出分类原因。
- **FR-010**: 默认 GitHub Actions 路径 MUST 不依赖外部闭源模型 API key。
- **FR-011**: 默认 GitHub Actions 路径 MUST 面向 GitHub-hosted CPU runner 的最小资源 smoke，
  不要求 GPU。
- **FR-012**: 系统 MUST 提供本地复现方式，使开发者能在同一 contract 下复现 CI 行为。
- **FR-013**: 新的 LLM E2E lane MUST 保留现有 deterministic MCP E2E 和 distribution smoke 的独立价值，
  不得替换或弱化它们。
- **FR-014**: 新的 workflow SHOULD 先以 `workflow_dispatch` 和 `schedule` 形式交付，
  再根据稳定性决定是否升级为强制 PR gate。
- **FR-015**: LLM runner SHOULD 支持通过配置切换本地模型服务 profile，
  但默认 smoke contract 只绑定一个最小模型 profile。
- **FR-016**: 每次 run 的 artifact MUST 写入唯一的隔离目录，
  避免并发执行时相互覆盖。
- **FR-017**: 文档 MUST 说明如何启动本地模型服务、如何运行最小 smoke、
  以及如何查看失败 artifact。

### Key Entities *(include if feature involves data)*

- **LLME2EScenario**: 定义本次 smoke 允许的 prompt、必需 tool 覆盖、禁止 SQL 类型、
  以及最终 JSON 期望。
- **ModelServiceProfile**: 描述本地模型服务的 endpoint、model 名称、启动等待策略、
  token/turn 上限与是否启用最小化推理模式。
- **MCPDemoRuntimeTarget**: 描述本次 smoke 使用的 MCP distribution endpoint、demo 逻辑库、
  schema、目标表和预期种子数据。
- **ToolTraceRecord**: 记录单次模型执行期间的 MCP tool 名称、入参、响应摘要与顺序。
- **LLME2EArtifactBundle**: 汇总 prompt、raw output、tool trace、assertion report、
  runtime log 和 run metadata。
- **StructuredFinalAnswer**: 模型最终必须返回的 JSON 结果，
  用于对 demo runtime 的真实数据库状态做 deterministic 断言。

### Assumptions

- 发行包默认 demo runtime 继续暴露逻辑库 `orders` 和 `billing`。
- `orders.public.orders` 表的 demo 种子数据继续保持两条订单记录，
  因而 `SELECT COUNT(*) AS total_orders FROM orders` 的结果稳定为 `2`。
- 现有 deterministic MCP E2E 和 distribution smoke 继续保留，作为这条 LLM E2E 的下层保障。
- 本地模型服务可以通过本机 HTTP endpoint 被测试 runner 访问。

## Non-Goals

- 不做多模型 benchmark、排行榜或回归打分体系。
- 不在第一阶段把写入事务、DDL/DCL、multi-database 切换、长对话规划纳入 LLM smoke。
- 不把现有 `test/e2e/mcp` 的 deterministic coverage 改造成完全依赖模型。
- 不让该 lane 在第一轮就承担所有 PR 的强制 blocking gate。
- 不在本轮为客户环境的外部数据库拓扑再额外扩展一套 LLM smoke contract。

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 仓库新增的 LLM E2E workflow 能在不依赖外部模型 API secret 的情况下，
  跑通 “真实模型 + 打包 MCP distribution + demo H2 runtime” 的 smoke 闭环。
- **SC-002**: 一次通过的 smoke run 100% 包含至少一次 metadata discovery tool 调用和一次 `execute_query` 调用。
- **SC-003**: 一次通过的 smoke run 最终 JSON 100% 能确认 `orders.public.orders` 的 `total_orders = 2`。
- **SC-004**: 一次失败的 smoke run 100% 会上传 prompt、raw model output、tool trace、
  assertion report 和 MCP runtime log。
- **SC-005**: 现有 deterministic MCP E2E 与 distribution smoke 继续可以独立执行，
  不受 LLM lane 是否通过的影响。
- **SC-006**: quickstart 指令可在本地复现同一 smoke contract，
  不需要改动业务代码或手工构造 MCP 请求。
