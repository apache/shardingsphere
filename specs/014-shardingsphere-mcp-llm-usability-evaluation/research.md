# Research: ShardingSphere MCP LLM Usability Evaluation

## Decision 1: 把 “舒服” 定义成低试错、高成功、可恢复，而不是主观评分

- **Decision**: usability 只通过可观测指标来定义，
  包括成功率、首次正确动作率、无效调用率、平均往返数、
  query answer fidelity、boundary confusion rate 和 recovery rate。
- **Rationale**:
  - “舒服” 对大模型来说，本质是低认知负担和低试错成本。
  - 这些指标都可以从实际 MCP trace 中稳定计算出来。
  - 这样后续比较不同实现或不同版本时，结论更可追踪。
- **Alternatives considered**:
  - 加一个人工主观评分字段：rejected，
    因为不稳定、不可回归、不同 reviewer 之间不可比。

## Decision 2: 继续保留 `resource` 与 `tool` 分离，并在评估中显式区分

- **Decision**: 评估模型不尝试把两者揉成统一入口，
  而是分别统计 `resource` 命中、`tool` 选择和 `query` 忠实度。
- **Rationale**:
  - 现有 MCP 规范本来就把 resources 和 tools 定义为不同交互面。
  - ShardingSphere 当前契约也明确区分了稳定结构信息与主动调用行为。
  - 真正的问题是边界是否清楚，而不是抽象个数是否最少。
- **Alternatives considered**:
  - 只做 overall score，不分维度：rejected，
    因为无法定位问题到底出在 resource、tool 还是 query。

## Decision 3: baseline 先做最小 12 个场景

- **Decision**: 第一阶段固定一个最小 12-scenario baseline pack，
  同时允许后续扩展到 20-scenario pack 做边界分析。
- **Rationale**:
  - 用户前文已经接受用一组固定任务做判断。
  - 12 个场景足以覆盖 H2 / MySQL、resource / tool / query / failure recovery。
  - 比直接上 20+ 场景更适合先把阈值校准稳定。
- **Alternatives considered**:
  - 只做 3 到 5 个 smoke 场景：rejected，
    因为无法承载 usability 维度和边界混淆分类。
  - 一开始就做 30+ 场景：rejected，
    因为成本和波动都偏高，不利于先建立稳定 baseline。

## Decision 4: 复用现有 `test/e2e/mcp` 资产，而不是新建独立 runner

- **Decision**: usability evaluation 在实现上继续依赖当前已有的
  `LLME2EScenario`、`LLMMCPConversationRunner`、
  `MCPToolTraceRecord` 和 artifact 体系。
- **Rationale**:
  - 当前仓库已经有真实模型驱动的最小 smoke 能力。
  - usability 层新增的主要是场景 contract、分数计算和 regression 比较。
  - 复用已有 runner 可以减少两套对话框架分叉。
- **Alternatives considered**:
  - 完全重建一个 usability runner：rejected，
    因为会扩大范围且难以和现有 smoke 保持一致。

## Decision 5: scorecard 同时输出 JSON 和 Markdown

- **Decision**: 每次评估 run 至少输出一份 machine-readable JSON scorecard
  和一份 human-readable Markdown summary。
- **Rationale**:
  - JSON 便于后续比较、预算判断和自动化处理。
  - Markdown 更适合 reviewer 快速看结论和场景差异。
- **Alternatives considered**:
  - 只输出控制台日志：rejected，
    因为不利于比较和归档。
  - 只输出 JSON：rejected，
    因为 review 场景阅读成本较高。

## Decision 6: regression 先作为 advisory，不直接做 gate

- **Decision**: 第一阶段只做本地、manual 和 nightly，
  或作为 advisory signal 进入 PR，而不是一开始就阻塞合并。
- **Rationale**:
  - LLM 场景相较 deterministic contract 更容易受到环境和模型波动影响。
  - 在阈值和场景还未稳定前，直接 gate 会制造噪音。
- **Alternatives considered**:
  - 第一轮就变成强制 PR gate：rejected，
    因为风险大于收益。

## Decision 7: 对 “成功但不顺” 做单独建模

- **Decision**: 只要场景中存在多次错误入口选择、无效参数或额外绕路，
  即使最终成功，也要在 scorecard 中体现 degraded usability。
- **Rationale**:
  - 用户前文关心的是 “用得舒服”，而不是 “最后有没有撞对答案”。
  - 这类场景如果只按 pass 计，会掩盖接口边界和命名问题。
- **Alternatives considered**:
  - 只保留最终 pass / fail：rejected，
    因为信号太粗，无法指导改进。
