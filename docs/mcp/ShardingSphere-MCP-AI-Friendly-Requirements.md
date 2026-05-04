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

# ShardingSphere MCP AI-Friendly Requirements

## 1. 文档目标

本文档用于梳理 ShardingSphere MCP 面向大模型使用体验的轻量改进需求。

目标不是重新设计 MCP runtime，也不是引入复杂 planner、权限平台或长期记忆系统。
目标是在当前已经具备的 resource-first surface、descriptor catalog、prompts、completion、workflow guidance 和 LLM E2E 基础上，
补齐最影响大模型原生、便捷、舒服、清晰使用的缺口。

## 2. 当前基线

当前 ShardingSphere MCP 已经具备以下基础能力：

- 通过 `resources/list`、`resources/templates/list`、`tools/list` 暴露模型可见能力。
- 通过 `shardingsphere://capabilities` 聚合 resources、tools、prompts、completion targets、navigation 和 fingerprints。
- 以 resource-first 方式暴露数据库、schema、table、column、index、view、sequence 等元数据。
- 通过 `search_metadata` 支持元数据搜索。
- 通过 `execute_query` 和 `execute_update` 区分只读 SQL 与有副作用 SQL。
- 通过 `plan_encrypt_rule`、`plan_mask_rule`、`apply_workflow`、`validate_workflow` 支持加密和脱敏 workflow。
- 通过 `next_actions`、`recommended_next_tool`、`requires_user_approval` 等字段引导模型继续下一步。
- 通过 opt-in LLM usability E2E 验证模型能使用 MCP surface 完成基础任务。

本需求文档默认延续这些能力，不要求推倒重建。

## 3. 产品目标

- 让模型能快速判断当前应该读资源、调用工具、询问用户还是停止。
- 让模型不用阅读历史设计文档，也能从协议可见 surface 理解当前能力边界。
- 让有副作用操作始终先 preview，再由用户确认后执行。
- 让常见模型错误能通过结构化恢复信息修正，而不是依赖自由文本猜测。
- 让 descriptor、README 和测试共同保护模型可见契约，防止回归。

## 4. 非目标

- 不新增重型 planner、自动遍历图引擎或跨会话记忆。
- 不把 `apply_workflow` 或 `execute_update` 变成自动执行工具。
- 不在本阶段引入完整用户身份、角色、租户或细粒度权限平台。
- 不要求兼容第三方数据库 MCP Server 的工具命名。
- 不把所有 resources 复制成一组完整 `list_*` tools；是否增加薄工具另行评估。
- 不在本阶段解决存量数据迁移、回填或生产变更审批系统集成。

### 4.1 实现前需要重新分析的问题

以下问题不是需求方向的重新澄清，而是进入实现前必须从当前代码重新取证的事实。
实现时应自行分析，不应再把这些问题作为用户澄清项抛回。

- 当前 MCP public surface：以 descriptor、capabilities 和 README 为准核对，避免文档写出不存在的 tool 或 resource。
- `execute_update` preview 当前返回结构：先看现有 handler 和 test，再决定只补字段、统一命名，还是调整 guidance。
- workflow preview 当前 guidance 形态：确认 `apply_workflow` 与 SQL preview 是否能复用同一套轻量 `next_actions` 词汇。
- 错误恢复现状：分别检查 missing database、missing execution mode、wrong SQL tool、unknown resource/tool、旧 `plan_id` 的当前返回。
- descriptor lint 放置位置：优先复用现有 descriptor loader 或 support 层测试，不新增复杂 lint 框架。
- capabilities contract test 边界：只验证 section 和 shape，不做大段 snapshot。
- 历史文档误导风险：只给容易误导当前实现契约的旧设计说明加状态标注，不批量重写历史文档。

### 4.2 不需要重新澄清的问题

以下结论已经固定，除非用户明确改变范围，否则不再作为问题反复确认。

- 不切换或创建 git 分支。
- 不改成 heavy planner、vector memory、跨会话长期记忆或完整 auth platform。
- 保持 resource-first 作为当前 MCP surface 的主要发现路径。
- 先实现 P0/P1 中直接降低模型困惑和操作风险的事项。
- `mcp/README.md` 与 `mcp/README_ZH.md` 的当前 public surface 说明应同步。
- 真实 LLM E2E 保持 opt-in，不进入默认 CI 门禁。

## 5. 使用场景

### 5.1 元数据检查

用户希望查看某个逻辑库、表、列或索引信息。

模型应能：

1. 先读取 `shardingsphere://capabilities` 或 `shardingsphere://databases`。
2. 使用 `search_metadata` 或 detail resource 定位对象。
3. 在只需检查元数据时停止，不主动执行 SQL。

### 5.2 安全 SQL 执行

用户希望执行 SQL 或确认 SQL 影响范围。

模型应能：

1. 用 `execute_query` 执行单条只读 `SELECT` 或 `EXPLAIN ANALYZE`。
2. 对 DML、DDL、DCL、事务控制和 savepoint 使用 `execute_update`。
3. 在执行有副作用 SQL 前先调用 `execute_update` 的 `execution_mode=preview`。
4. 只有用户确认 preview 后，才使用 `execution_mode=execute`。

### 5.3 加密和脱敏规划

用户希望对逻辑表列规划加密或脱敏规则。

模型应能：

1. 读取逻辑表、列和 feature algorithm 资源。
2. 调用 `plan_encrypt_rule` 或 `plan_mask_rule` 生成可 review 的 artifacts。
3. 若返回 clarifying 状态，按 `missing_required_inputs` 和 `pending_questions` 补齐信息。
4. 调用 `apply_workflow` 的 `execution_mode=preview` 预览。
5. 用户确认后执行或导出 manual artifacts。
6. 最后调用 `validate_workflow` 验证结果。

## 6. P0 需求

### FR-001 当前能力说明一致

README、PRD、descriptor 和 capabilities 中的当前对外能力必须一致。

验收标准：

- 当前实现以 resource-first 为主，文档不应暗示已经存在完整 `list_databases`、`list_schemas`、`describe_table` 等工具。
- `mcp/README.md` 和 `mcp/README_ZH.md` 中的 public tools 列表应与 descriptor 暴露结果一致。
- 历史 PRD 或设计文档如保留旧工具清单，应标注其为历史设计或早期目标，而非当前实现契约。

### FR-002 统一 next action 字段

模型引导字段应使用一套稳定词汇。

验收标准：

- 优先使用 `next_actions` 表达后续动作。
- `next_actions` 中的每个动作应是对象，至少包含 `action_kind`、`reason` 和 `requires_user_approval`。
- 调工具动作应包含 `target_tool` 和 `required_arguments`。
- 问用户动作应包含 `required_inputs`。
- `recommended_next_tool` 和 `suggested_next_tool` 应收敛为一个字段名，避免同义字段并存。

### FR-003 常见错误可结构化恢复

最常见的模型错误必须返回可恢复信息。

验收标准：

- 缺少 `database` 时，返回 `missing_fields`，并建议读取 `shardingsphere://databases`。
- 缺少 `execution_mode` 时，返回 `missing_fields`，并建议 `execution_mode=preview`。
- 将有副作用 SQL 发给 `execute_query` 时，建议改用 `execute_update` preview，且保留用户确认要求。
- 调用未知 resource 或 tool 时，建议读取 `shardingsphere://capabilities`。
- `plan_id` 不存在或过期时，建议在当前 session 重新调用对应 planning tool。

### FR-004 复杂工具提供短输出示例

复杂工具 descriptor 或文档应提供短小、无密钥、无环境绑定的输出示例。

验收标准：

- `execute_update` preview 有最小 JSON 示例。
- `plan_encrypt_rule` 有 planned 或 clarifying 的最小 JSON 示例。
- `plan_mask_rule` 有 planned 或 clarifying 的最小 JSON 示例。
- `apply_workflow` preview 有最小 JSON 示例。
- `validate_workflow` 有 passed 或 failed 的最小 JSON 示例。
- 示例不得包含真实密钥、生产库名或环境特定路径。

### FR-005 最小 descriptor lint

descriptor 质量应有轻量自动校验。

验收标准：

- 拒绝空 description。
- 拒绝明显占位 description。
- 有副作用工具必须声明 side-effect 或 approval 语义。
- enum 字段必须列出可选值。
- 核心工具必须有 output schema 的关键字段。
- navigation 中引用的 tool 或 resource 必须存在。

### FR-006 保护 capabilities 核心契约

至少为 `shardingsphere://capabilities` 添加一个轻量 golden contract test。

验收标准：

- 测试覆盖 resources、resourceTemplates、tools、prompts、completionTargets、resourceNavigation、protocolAvailability 和 fingerprints 的存在性。
- 测试不锁定大段运行时数据。
- 测试能在默认本地环境中稳定运行，不依赖真实模型服务。

### FR-007 明确三条首次使用路径

README 和 prompt 应清楚表达三条最常用路径。

验收标准：

- 元数据检查路径不超过 5 步。
- 安全 SQL 执行路径不超过 5 步。
- 加密或脱敏 workflow 路径不超过 6 步。
- 三条路径应与 descriptor 中的 prompts 和 navigation 保持一致。

### FR-008 preview 返回可复用执行参数

`execute_update` preview 应降低模型重组 SQL 的机会。

验收标准：

- preview 返回 `suggested_arguments`。
- `suggested_arguments` 包含服务器规范化后的 `sql` 和下一步执行所需的 `execution_mode=execute`。
- preview 明确提示模型必须先让用户确认，再复用 `suggested_arguments` 执行。

## 7. P1 需求

### FR-101 轻量 workflow 查询入口

提供当前 session 内 workflow plan 的轻量查询能力，便于模型恢复上下文。

验收标准：

- 能列出当前 session 的 plan id、workflow kind、status 和 update time。
- 能读取单个 plan 的 status、artifacts 摘要和 recommended next action。
- 不要求跨 session 持久化。
- 不引入复杂搜索或历史记忆。

### FR-102 扩展 LLM usability 场景

现有 LLM usability suite 应增加少量高价值场景。

验收标准：

- 增加 side-effect SQL 必须先 preview 的场景。
- 增加 plan -> apply preview -> validate 的基本 workflow 顺序场景。
- 仍保持 opt-in，不影响默认 CI。

### FR-103 completion 排序补充上下文优先级

completion 排序应优先返回上下文更完整的候选。

验收标准：

- table completion 有 database 和 schema 时，优先返回该上下文内候选。
- column completion 有 database、schema、table 时，优先返回该表下候选。
- plan id completion 继续优先当前 session 的可用 plan。
- 不引入向量搜索、模型调用或跨会话学习。

### FR-104 元数据 freshness 提示

元数据响应应提供轻量 freshness 信息。

验收标准：

- 至少提供 `metadata_fingerprint` 或 `loaded_at` 之一。
- 模型能判断多个资源响应是否来自同一批元数据快照。
- 不要求在本阶段实现主动 refresh 工具。

### FR-105 启动和接入排障文档

补充首次接入最常见失败路径。

验收标准：

- 覆盖 Java 版本不满足。
- 覆盖 JDBC driver 缺失。
- 覆盖 HTTP token 缺失或错误。
- 覆盖 STDIO 模式 stdout 被日志污染。
- 覆盖 tools/list 或 resources/list 为空时的检查路径。

### FR-106 明确 workflow 不负责的事情

加密和脱敏 workflow 文档必须清楚说明边界。

验收标准：

- 明确不迁移存量数据。
- 明确不自动回填。
- 明确不替用户确认生产变更。
- 明确规划 tool 不执行 DDL 或 DistSQL。

## 8. P2 需求

### FR-201 MCP client 配置示例

补充常见 MCP client 的 STDIO 配置示例。

验收标准：

- 至少提供一个通用 STDIO JSON 配置示例。
- 示例应说明 `bin/start.sh conf/mcp-stdio.yaml` 的使用方式。
- 示例应说明 HTTP 与 STDIO 每个进程只能启用一种。

### FR-202 插件 feature 最小模板

为新增 MCP feature 提供最小开发模板说明。

验收标准：

- 模板覆盖 handler、descriptor、prompt 和 test。
- 明确 feature URI 使用 `shardingsphere://features/<feature>/...`。
- 明确新增 tool 或 resource 必须有 descriptor。

### FR-203 历史设计文档状态标注

为旧 PRD、Technical Design 和 Spec 文档增加状态说明。

验收标准：

- 区分当前实现契约、历史设计和下一步规划。
- 避免模型或开发者误把旧工具清单当作当前实现。

### FR-204 用户语言一致性

用户以中文提出需求时，澄清问题应尽量使用中文返回。

验收标准：

- `pending_questions` 或等价澄清字段可保持用户语言。
- descriptor 和协议字段名仍保持英文稳定标识。

## 9. 验证要求

本需求对应的实现应优先使用小范围验证。

建议验证命令：

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

如果只修改文档，可至少执行：

```bash
git diff --check
```

若实现 descriptor lint、capabilities golden test 或 LLM usability 场景，应补充对应模块的单测或 E2E 验证命令。

## 10. 成功标准

- 模型能从当前 README、descriptor 和 capabilities 中得到一致的工具与资源视图。
- 模型面对常见缺参、错工具、旧 plan 等问题时，能通过结构化字段修正下一步。
- 模型面对有副作用 SQL 或 workflow apply 时，会先 preview，再等待用户确认。
- 维护者能通过最小 lint 和 capabilities contract test 捕获模型可见 surface 的明显回归。
- 改进保持轻量，不引入重型 planner、跨会话记忆或复杂权限平台。
