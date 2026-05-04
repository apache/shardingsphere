<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# ShardingSphere MCP AI-Friendly Lightweight Requirements

## 1. 文档目的

本文档定义 ShardingSphere MCP 面向大模型原生、便捷、舒服、清晰使用的轻量改进需求。

核心目标不是增加一套新的智能系统，而是把模型在使用 MCP 时需要猜的地方降到最低：

- 少猜当前有哪些工具和资源。
- 少猜下一步应该读资源、调用工具、问用户还是停止。
- 少猜工具返回值和 output schema 的对应关系。
- 少猜错误后该如何安全恢复。
- 少猜有副作用操作何时可以执行。

本文档只描述需求和验收标准，不绑定具体实现方案。

## 2. 当前基线

当前 ShardingSphere MCP 已具备以下基础能力：

- 通过 `resources/list`、`resources/templates/list`、`tools/list` 暴露模型可见能力。
- 通过 `shardingsphere://capabilities` 聚合 resources、tools、prompts、completion targets、resource navigation 和 fingerprints。
- 以 resource-first 方式暴露 database、schema、table、column、index、view、sequence 等逻辑元数据。
- 通过 `search_metadata` 支持元数据搜索。
- 通过 `execute_query` 与 `execute_update` 区分只读 SQL 与有副作用 SQL。
- 通过 `plan_encrypt_rule`、`plan_mask_rule`、`apply_workflow`、`validate_workflow` 支持加密和脱敏 workflow。
- 通过 `next_actions`、推荐工具字段和用户确认字段引导模型继续下一步。
- 通过 descriptor、prompt、completion 和 opt-in LLM usability 测试保护部分模型可见契约。

后续需求应在这些能力上补齐体验缺口，不推倒重建。

## 3. 设计原则

新增或调整需求必须满足以下原则：

- 能明显降低模型误用、绕路、重组参数或错误恢复成本。
- 优先复用现有 descriptor、resource、tool、prompt、completion、workflow 和 recovery 机制。
- 保持 resource-first 发现路径，不复制一整套 `list_*` 或 `describe_*` 工具矩阵。
- 有副作用操作必须先 preview，并要求用户确认后才能 execute。
- 每个需求都应能通过轻量测试、descriptor lint、contract test 或文档核对验证。
- 不引入重型 planner、跨会话长期记忆、向量搜索、模型调用排序或完整权限平台。

## 4. 非目标

本阶段明确不做以下事项：

- 不新增自动 planner、图遍历引擎或跨会话长期记忆。
- 不新增完整 RBAC、租户系统、生产审批系统或外部工单集成。
- 不把 `execute_update`、`apply_workflow` 变成自动越过用户确认的执行工具。
- 不把所有 resource 复制成完整工具矩阵。
- 不在 MCP 内解决存量数据迁移、回填或生产变更审批。
- 不为了兼容旧 PRD 中的早期工具名而增加兼容层。
- 不把 opt-in LLM E2E 变成默认 CI 必跑门禁。

### 4.1 过渡与过度设计清理结论

以下内容保留为过渡说明，但不得作为当前实现契约：

- 旧 PRD、技术设计和详细设计中的早期工具矩阵。
- README 中关于发布、registry、opt-in LLM smoke 的运行说明。
- `specs/003-mcp-ai-friendly-guided-interaction/` 中为溯源保留的 Spec Kit 草稿。

以下内容本轮先从活跃需求中清理掉：

- 当前 session workflow 列表或详情 resource。现有 `plan_id` 返回、当前 session completion 和 recovery 已覆盖主要恢复路径。
- metadata freshness 字段。它会引入元数据批次一致性语义，当前没有证据证明是模型使用 MCP 的主要阻塞。
- 配置环境变量引用。它更像运维安全能力，不是模型原生使用 MCP 的核心路径。
- normalized golden transcript 大套件、real-model E2E 扩展、model-confusion 测试矩阵、sampling/progress/logging/roots 边界测试。它们可以作为未来质量工程候选，但不进入当前轻量改进范围。

## 5. 使用场景

### 5.1 元数据检查

用户希望查看逻辑库、表、列、索引、视图或 sequence。

模型应能：

1. 读取 `shardingsphere://capabilities` 或 `shardingsphere://databases` 了解当前 surface。
2. 使用 `search_metadata` 定位对象，或直接读取 detail resource。
3. 从搜索结果中拿到可直接读取的 resource URI。
4. 在只需元数据时停止，不主动执行 SQL。

### 5.2 安全 SQL 执行

用户希望执行 SQL 或确认 SQL 影响范围。

模型应能：

1. 使用 `execute_query` 执行单条只读 `SELECT` 或 `EXPLAIN ANALYZE`。
2. 对 DML、DDL、DCL、事务控制和 savepoint 使用 `execute_update`。
3. 对有副作用 SQL 先调用 `execute_update` 的 `execution_mode=preview`。
4. 从 preview 返回中复用服务器提供的执行参数。
5. 只有用户明确确认后，才使用 `execution_mode=execute`。

### 5.3 加密和脱敏 workflow

用户希望为逻辑表列规划、预览、执行并验证加密或脱敏规则。

模型应能：

1. 读取逻辑 metadata 和 feature algorithm 资源。
2. 调用 `plan_encrypt_rule` 或 `plan_mask_rule`。
3. 如果返回 `clarifying`，按结构化缺失字段继续同一个 `plan_id`。
4. 如果返回 `planned`，先调用 `apply_workflow` 的 `execution_mode=preview`。
5. 用户确认后执行或导出 manual artifacts。
6. 最后调用 `validate_workflow` 验证结果。

### 5.4 错误恢复

模型常见错误应可通过结构化字段恢复。

典型错误包括：

- 缺少 `database`。
- 缺少 `execution_mode`。
- 将有副作用 SQL 发给 `execute_query`。
- 调用未知 tool 或 resource。
- 使用过期、未知或跨 session 的 `plan_id`。

## 6. 实现前分析与设计闸门

以下内容不是新的产品范围，也不是需要反复向用户确认的问题。
它们是进入实现前必须从当前代码中复核的事实。

如果代码事实与本文档需求不一致，应先更新需求或设计记录，再进入实现。

### 6.1 必须复核的代码事实

- 当前 public surface：descriptor、`shardingsphere://capabilities`、`mcp/README.md` 和 `mcp/README_ZH.md` 是否一致。
- 当前 `next_actions`：现有字段、推荐工具字段、用户确认字段和 workflow guidance 是否已经有可复用结构。
- 当前 `search_metadata`：结果构造位置、支持的对象类型、可安全推导的 resource URI pattern，以及不能推导时的返回方式。
- 当前 output schema：七个核心工具的 descriptor schema 与真实 payload、状态值、enum 大小写和嵌套字段是否一致。
- 当前 recovery：缺 `database`、缺 `execution_mode`、SQL 工具用错、未知 tool/resource、旧 `plan_id` 的实际错误形态。
- descriptor lint 落点：优先复用现有 descriptor loader、catalog 或 support 层测试，不新增独立 lint 框架。
- capabilities contract 边界：只验证 section 和 shape，不做大快照，不依赖真实模型。
- P1/P2 可行性：只有现有上下文能轻量支持时才继续分析，不为了 P1/P2 引入新存储、planner 或复杂索引。

### 6.2 分析产物要求

每个进入实现的需求至少应形成以下分析产物：

- 当前行为证据：相关 descriptor、handler、resource、workflow、completion、test 或 README 路径。
- 最小变更映射：需要改的生产代码、测试和文档路径。
- 验收测试映射：每个行为分支对应一个明确测试或文档核对项。
- 不做项说明：明确哪些看似相关的能力本次不做，以及原因。
- 回滚边界：说明回滚后恢复到哪个现有行为，不影响 HTTP/STDIO 基础暴露。

### 6.3 设计约束

- 设计必须优先复用现有 MCP descriptor、resource、tool、prompt、completion、workflow 和 error conversion 机制。
- 设计不得新增完整 `list_*` 工具矩阵、通用图遍历、自动 planner、向量检索、跨会话记忆或完整权限平台。
- P0 设计必须能通过确定性单测、descriptor lint、capabilities contract test 或文档一致性检查验证。
- P1/P2 设计必须在 P0 稳定后单独评估，不得阻塞 P0。

## 7. P0 需求

P0 是最小必做集合，目标是立即降低模型猜测成本和操作风险。

### FR-001 当前 public surface 唯一可信

README、descriptor、capabilities 和容易被模型读取的设计文档必须明确区分当前实现契约与历史设计。

验收标准：

- `shardingsphere://capabilities` 可作为当前 MCP public surface 的事实源。
- `mcp/README.md` 与 `mcp/README_ZH.md` 中的 public tools 列表与 descriptor/capabilities 一致。
- 文档不得暗示当前已存在完整 `list_databases`、`list_schemas`、`describe_table` 等工具矩阵。
- 保留旧工具清单的 PRD 或设计文档必须标注其为历史设计、未来规划或非当前契约。

### FR-002 统一 next action 与可复用参数

模型引导字段必须收敛到一套稳定词汇，并尽量返回下一步可直接复用的参数。

验收标准：

- 以 `next_actions` 作为主引导字段。
- 每个 action 至少包含 `action_kind`、`reason` 和 `requires_user_approval`。
- 调工具 action 包含 `target_tool` 和 `required_arguments`。
- 读资源 action 包含 `target_resource` 或等价字段。
- 问用户 action 包含 `required_inputs`。
- 以 `next_actions` 为主；已有 `recommended_next_tool`、`suggested_next_tool` 仅作为兼容字段保留，避免继续新增同义字段。
- preview 类响应必须给出可复用的下一步参数，避免模型重新拼 SQL、`plan_id` 或 execution mode。

### FR-003 `search_metadata` 返回可直接读取的 URI

`search_metadata` 命中对象后，应直接告诉模型可以读取哪个 detail resource。

验收标准：

- 能安全推导 detail resource 时，搜索命中包含 `resource_uri`。
- 能安全推导父级资源时，返回 `parent_resource_uri`。
- 能安全推导下一跳时，返回 `next_resource_uris` 或等价结构。
- URI 必须使用当前 descriptor 已暴露的 resource pattern。
- 不能安全推导 URI 时，不返回猜测值，并通过 `derivation_status` 与 `derivation_reason` 说明原因。

### FR-004 output schema 与真实返回对齐

模型可见 output schema 必须能准确描述实际返回形态。

验收标准：

- 核对并修正 `search_metadata`、`execute_query`、`execute_update`、`plan_encrypt_rule`、`plan_mask_rule`、`apply_workflow`、`validate_workflow`。
- enum 值大小写、字段名、必填字段和嵌套对象结构必须与真实返回一致。
- preview、error、clarifying、planned、completed、failed 等常见状态必须在 schema 或示例中可理解。
- schema 不应只写 `array` 或 `object` 而缺少关键字段说明。

### FR-005 常见错误结构化 recovery

最常见的模型错误必须返回可恢复信息，而不是只返回自由文本。

验收标准：

- 缺少 `database` 时，返回 `missing_fields`，并建议读取 `shardingsphere://databases`。
- 缺少 `execution_mode` 时，返回 `missing_fields`，并建议先使用 `execution_mode=preview`。
- 有副作用 SQL 发给 `execute_query` 时，建议改用 `execute_update` preview，并保留用户确认要求。
- 未知 tool 或 resource 时，建议读取 `shardingsphere://capabilities`。
- `plan_id` 不存在、过期或跨 session 时，建议使用当前 session 的 `plan_id` completion，或重新调用对应 planning tool。
- recovery 中的下一步建议应使用 FR-002 的 next action 结构。

### FR-006 最小回归保护

模型可见 surface 必须有轻量自动保护，防止 descriptor 或 capabilities 悄悄退化。

验收标准：

- 增加 descriptor lint，至少覆盖空 description、占位 description、缺少 side-effect/approval 语义、enum 缺少可选值、核心 output schema 缺少关键字段。
- descriptor lint 必须覆盖 navigation 引用不存在的情况。
- 增加 `shardingsphere://capabilities` contract test，覆盖 resources、resourceTemplates、tools、prompts、completionTargets、resourceNavigation、protocolAvailability、fingerprints。
- contract test 不锁定大段快照，不依赖真实模型服务。
- lint 和 contract test 应保持确定性和快速执行。

## 8. P1 需求

P1 是高价值轻量增强，可以在 P0 稳定后按收益排序实现。已清理掉会新增状态查询面或一致性语义的候选项。

### FR-101 资源响应补轻量导航信息

资源读取结果应尽量告诉模型当前所在位置和可走的下一跳。

验收标准：

- list/detail resource 可返回 `self_uri`。
- 能安全推导父级时，返回 `parent_uri`。
- list 结果返回 `count` 或等价数量信息。
- 能安全推导下一跳时，返回 `next_resources`。
- 不要求实现通用图遍历引擎。

### FR-102 复杂工具提供短输出示例

复杂工具应提供小而静态的示例，让模型快速理解返回形态。

验收标准：

- `execute_update` preview 有最小 JSON 示例。
- `plan_encrypt_rule` 有 `clarifying` 或 `planned` 示例。
- `plan_mask_rule` 有 `clarifying` 或 `planned` 示例。
- `apply_workflow` preview 有最小 JSON 示例。
- `validate_workflow` 有 passed 或 failed 示例。
- 示例不得包含真实密钥、生产库名或环境特定路径。

### FR-103 completion 轻量优化

completion 应更贴近模型输入习惯，但不引入重型能力。

验收标准：

- 有 database/schema 上下文时，table completion 优先返回该上下文内候选。
- 有 database/schema/table 上下文时，column completion 优先返回该表下候选。
- 支持 prefix 优先和 contains fallback。
- `plan_id` completion 继续优先当前 session 最近更新且可继续的 plan。
- 不使用向量检索、模型调用、跨会话学习或用户行为学习。

### FR-104 algorithm resource 暴露属性模板

encrypt 和 mask algorithm resource 应尽量告诉模型算法需要哪些属性。

验收标准：

- 返回 required properties。
- 返回 optional properties。
- 返回 default value。
- 返回 secret flag。
- 返回能力提示，例如是否支持 decrypt、equality filter、like query。
- 不要求先调用 planner 才能知道基础属性缺口。

## 9. P2 需求

P2 是可后置的体验补强，不阻塞核心模型使用链路。

### FR-201 启动成功提示更清晰

MCP runtime 启动成功后应清楚展示连接和排障入口。

验收标准：

- HTTP 模式展示 endpoint、config path、log path、runtime database 数量。
- STDIO 模式说明 stdout 用于 MCP 协议、日志应走 stderr 或文件。
- 如果启用 access token，提示 HTTP 请求需要 bearer token。
- 不在 stdout 输出会污染 STDIO 协议的日志内容。

### FR-202 首次接入排障文档

补充最常见失败路径，帮助模型和用户快速定位问题。

验收标准：

- 覆盖 Java 版本不满足。
- 覆盖 JDBC driver 缺失。
- 覆盖 HTTP token 缺失或错误。
- 覆盖 STDIO stdout 被日志污染。
- 覆盖 tools/list、resources/list 或 capabilities 为空。
- 覆盖 workflow 连接到物理库而不是 ShardingSphere-Proxy 逻辑库的误用。

### FR-203 opt-in LLM usability 场景补充

LLM usability 测试可增加少量高价值场景，但仍保持 opt-in。

验收标准：

- 增加有副作用 SQL 必须先 preview 的场景。
- 增加 metadata search 到 detail resource URI 的场景。
- 增加 plan -> apply preview -> validate 的 workflow 顺序场景。
- 不要求真实模型测试进入默认 CI。

## 10. 实施顺序建议

建议按以下顺序实施：

1. 完成 P0：public surface、next action、search metadata URI、output schema、recovery、回归保护。
2. 再做 P1：资源导航、短示例、completion、algorithm 属性模板。
3. 最后做 P2：启动提示、排障文档、opt-in LLM usability 扩展。

每个需求应尽量独立提交和验证，避免一次性大改 MCP runtime。

## 11. 验证要求

只修改文档时，至少执行：

```bash
git diff --check
```

实现 P0/P1 代码后，优先执行受影响模块的 scoped test，例如：

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

若新增 descriptor lint、capabilities contract test 或 LLM usability 场景，应补充对应模块的单测或 E2E 验证命令。

## 12. 成功标准

- 模型能从 README、descriptor 和 capabilities 获得一致的当前 MCP surface。
- 模型搜索到 metadata 后能直接读取对应 resource，而不是手工拼 URI。
- 模型面对 preview、workflow、error 时能复用结构化 next action 和参数。
- 模型面对常见缺参、错工具、旧 plan 等问题时能通过 recovery 安全修正。
- 模型面对有副作用 SQL 或 workflow apply 时始终先 preview，再等待用户确认。
- 维护者能通过最小 lint 和 capabilities contract test 捕获模型可见 surface 的明显回归。
- 改进保持轻量，不引入重型 planner、跨会话记忆、向量检索或复杂权限平台。
