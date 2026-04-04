# Research: ShardingSphere MCP Minimal LLM-Driven E2E Validation

## Decision 1: 把 LLM E2E 作为现有 deterministic MCP E2E 之上的新增验证层

- **Decision**: 新增一条独立的 LLM-driven smoke lane，
  不替换现有 `test/e2e/mcp` deterministic contract tests 和 distribution smoke。
- **Rationale**:
  - 真实模型测试更容易抖动，适合做补充验证，不适合立刻独占主回归层。
  - 现有 deterministic tests 已经能稳定证明 MCP server 逻辑；
    新 lane 只需要补齐 “真实模型会不会正确用 MCP” 这一层缺口。
- **Alternatives considered**:
  - 直接把现有 production-runtime E2E 全部改成模型驱动:
    rejected，因为会降低基础回归层的可诊断性和稳定性。
  - 完全不加模型 E2E，只继续保留 deterministic smoke:
    rejected，因为这正是当前用户指出的能力缺口。

## Decision 2: 第一阶段验收数据库固定使用打包 demo H2 runtime

- **Decision**: 第一阶段 smoke contract 固定绑定发行包自带的 H2 demo runtime，
  目标表固定为 `orders.public.orders`，只验证只读查询。
- **Rationale**:
  - 这条路径已经是仓库自带的、真实 JDBC-backed runtime。
  - 它不依赖外部数据库资源或额外 secret，适合 GitHub-hosted runner。
  - 与 demo 种子数据绑定后，最终 JSON 可以稳定断言 `total_orders = 2`。
- **Alternatives considered**:
  - 第一阶段直接编排 MySQL/PostgreSQL 容器:
    rejected，因为成本更高，资源也更重，不利于最小 smoke 先落地。
  - 继续使用内存 `MetadataCatalog` / `DatabaseRuntime` fixture:
    rejected，因为不能满足“真实数据库访问”的目标。

## Decision 3: 使用 repository-owned 的薄 runner，而不是引入通用 agent framework

- **Decision**: 在仓库内实现一个最小化 runner，
  负责驱动模型、发起 MCP 调用、记录 tool trace 和做最终断言。
- **Rationale**:
  - 可以最小化额外依赖和资源消耗。
  - artifact、诊断、只读约束和 JSON 断言都能按仓库需要定制。
  - 比通用 agent framework 更容易与现有 Java/Maven E2E 体系集成。
- **Alternatives considered**:
  - 直接引入 Goose 或其他外部 agent framework:
    rejected，因为第一阶段目标是资源受控、依赖最少、诊断最清晰。
  - 手工硬编码 `tools/call` 顺序来模拟模型:
    rejected，因为这不是真正的模型驱动 E2E。

## Decision 4: 模型接入采用本地 HTTP chat profile，而不是把 provider 写死在测试逻辑里

- **Decision**: runner 通过配置接入本地模型服务，
  代码只依赖“本地 HTTP chat endpoint + tool-calling-compatible response”这一抽象。
- **Rationale**:
  - 这样 workflow 可以按资源预算选择最小模型 profile，
    而不会把测试代码固定死在某一个 provider brand。
  - 本地复现与 CI 只需要改配置，不需要改业务代码。
- **Alternatives considered**:
  - 直接把某个 provider 的 SDK 写进测试代码:
    rejected，因为会增加耦合和维护面。

## Decision 5: 最终通过标准必须是 “trace + JSON”，而不是 “自然语言看起来对”

- **Decision**: 通过判定必须同时满足：
  - trace 中包含 required tool coverage
  - SQL 经过只读校验
  - 最终结构化 JSON 与预期数据匹配
- **Rationale**:
  - 模型可能 hallucinate 出看似正确的自然语言。
  - 仅看最终文本不能证明模型真的调用了 MCP。
  - trace 和 JSON 同时存在，才能让失败可解释。
- **Alternatives considered**:
  - 只检查最终答案文本是否包含 `2`:
    rejected，因为无法证明它来自真实 MCP 查询。
  - 只检查 trace，不检查最终答案:
    rejected，因为不能证明模型是否正确整合了工具结果。

## Decision 6: 第一轮 workflow 以 schedule / manual 为主，不直接做强制 PR gate

- **Decision**: 新增独立 workflow，先支持 `workflow_dispatch` 和 `schedule`。
- **Rationale**:
  - 真实模型 lane 的稳定性需要先在夜间和手动触发中观察。
  - 这样可以先积累 flake 情况和 artifact，再决定是否升级 gate。
- **Alternatives considered**:
  - 直接并入现有 JDK 17 subchain CI:
    rejected，因为第一版不应立刻扩大对主线 CI 的影响面。

## Decision 7: 每次 run 的 artifact 目录必须唯一

- **Decision**: 每次执行都在 `target/llm-e2e/<run-id>/` 下写入独立 artifact。
- **Rationale**:
  - 本地复现、CI 重试和潜在并发都需要隔离。
  - 这与用户明确提出的“不要相互影响”目标一致。
- **Alternatives considered**:
  - 所有 run 共用一个固定 `target/llm-e2e/last/` 目录:
    rejected，因为会覆盖失败证据，也不利于并发。
