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

# ShardingSphere MCP AI-Native Polish Requirements

## 1. 文档目的

本文档是 `.specify/specs/009-mcp-ai-native-polish/` 的 repo 可见交接版。

目标是继续追问同一个问题：`shardingsphere-mcp` 在被大模型原生、便捷、舒服、清晰使用时，还有哪些小而有效的提升点。

结论是：当前 MCP 已经有较完整的 AI-friendly 基线，下一步不应重建系统，而应补齐模型仍需要猜的字段、顺序、恢复路径、边界、来源和运行状态。

## 2. Spec Kit 来源

完整 Speckit 包位于：

- `.specify/specs/009-mcp-ai-native-polish/spec.md`
- `.specify/specs/009-mcp-ai-native-polish/plan.md`
- `.specify/specs/009-mcp-ai-native-polish/research.md`
- `.specify/specs/009-mcp-ai-native-polish/data-model.md`
- `.specify/specs/009-mcp-ai-native-polish/current-behavior-analysis.md`
- `.specify/specs/009-mcp-ai-native-polish/tasks.md`
- `.specify/specs/009-mcp-ai-native-polish/checklists/requirements.md`

分支约束：

- 当前分支保持 `001-shardingsphere-mcp`。
- 不运行 `git switch`、`git checkout`、分支创建脚本或会切换分支的 Spec Kit 命令。

## 3. 当前基线

009 需求把以下能力视为已完成基线，不再重复设计：

- `shardingsphere://capabilities` 已作为模型可读 public surface。
- capabilities 已包含 `model_contract`、`next_action_contract`、`common_flows`、`security_hints`、payload contracts、protocol availability 和 fingerprints。
- descriptor 已覆盖 tools、resources、resource templates、prompts、completion、annotations、examples 和 output schema。
- `search_metadata` 已包含 `resource_uri`、`parent_resource_uri`、`next_resource_uris`、search context、match kind、matched fields 和 matched value。
- SQL 响应已包含 row count、applied limits、timeout、truncation 和 `next_actions` 等 parse hints。
- `execute_update` 与 `apply_workflow` 已有 preview-first、approval summary、approval question、side-effect scope、reusable arguments 和 user approval 要求。
- recovery 已覆盖缺参数、execution mode、SQL tool 用错、object type、page token、stale workflow plan、unsupported tool/resource 和 unsafe SQL 等常见错误。
- completion 已有 missing context、candidate count、ranking policy、prefix-first、contains fallback 和 current-session plan ordering。
- opt-in LLM usability tests 已存在，且不属于默认 CI。

## 4. P0 需求：立即降低模型猜测成本

- **P0-01 next action 顺序**：多 action 响应应暴露 `order`、`depends_on` 或 `approval_dependency`，让模型知道先问用户还是先调用工具。
- **P0-02 retry 目标明确**：`retry_tool` recovery 应在可知时带上 `source_tool` 或 `target_tool`。
- **P0-03 compact surface summary**：capabilities 应新增短小 `surface_summary`，包含首选资源、公开 tools、主要 flows 和 preview-before-approval 安全规则。
- **P0-04 navigation 类型提示**：resource navigation 应标明 source/target 是 resource、tool、prompt 还是 completion 相关目标。
- **P0-05 completion 就近提示**：tool fields、prompt arguments 或 resource parameters 应就近标明是否支持 completion。
- **P0-06 completion 缺上下文恢复**：completion 对 missing context、no candidates、prefix filtered all candidates 应给出安全 `next_actions`。
- **P0-07 SQL 默认边界**：`execute_query` 缺 `max_rows` 时应使用文档化默认值，并暴露最大 cap、truncated 状态和继续动作。
- **P0-08 search 参数边界**：`search_metadata` 的 `page_size`、`page_token`、scope 和 object types 应有 schema-visible min/max/default 与结构化 recovery。
- **P0-09 blank query 语义**：空 query 应明确是 scope 内 list/search-all，还是必须缩窄范围；不能让模型猜。
- **P0-10 response mode 明确**：tool output schema 应能区分 preview、executed、manual-only、validation、recovery、terminal 等状态。
- **P0-11 preview 语义明确**：`execute_update` preview 应说明它是 SQL 分类和 side-effect scope 摘要，不是 affected rows 估算。
- **P0-12 apply_workflow recovery 精确**：`apply_workflow` 缺或错 `execution_mode` 时，recovery 应指回 `apply_workflow`，默认建议 `execution_mode=preview`。
- **P0-13 public 参数路径精确**：workflow 缺 algorithm properties 时，应使用 `primary_algorithm_properties`、
  `assisted_query_algorithm_properties`、`like_query_algorithm_properties` 等公开参数名。
- **P0-14 metadata SQL 恢复**：模型发送 `SHOW TABLES`、`DESCRIBE` 等元数据 SQL 时，应引导到 logical metadata resources 或 `search_metadata`。
- **P0-15 URI 编码一致**：search result URI、resource navigation 和 workflow `resources_to_read` 应统一处理非 ASCII 与保留字符。
- **P0-16 row object 便利层**：SQL 结果在列名唯一时可返回 object-shaped rows；列名重复或不可命名时必须说明 fallback 原因并保留 positional rows。
- **P0-17 pagination/truncation continuation**：分页、截断或需要缩窄范围时，应提供安全 `next_actions`。
- **P0-18 ambiguity hint**：同名对象跨 database/schema/object type 出现时，应暴露 ambiguity 和 narrowing arguments，不猜最佳匹配。

## 5. P1 需求：让响应更舒服、更可恢复

- **P1-01 empty/not-found reason**：空列表、零命中搜索和 detail not found 应给出 reason、parent/list/search follow-up。
- **P1-02 argument provenance**：可复用参数应标明 `user_provided`、`server_normalized`、`server_generated`、`server_defaulted` 或 `redacted`。
- **P1-03 normalized SQL success hint**：SQL 成功响应在安全且已有分类信息时应暴露 normalized SQL，方便模型复述和续用。
- **P1-04 redaction marker 统一**：algorithm/workflow 敏感字段应有统一 redaction marker、redacted count 或 redacted keys。
- **P1-05 manual-only contract**：manual-only workflow 应结构化表达“用户在 MCP 外部执行后再 validate”。
- **P1-06 EXPLAIN ANALYZE 风险**：database capability 应保守说明某些数据库上 `EXPLAIN ANALYZE` 可能执行查询。
- **P1-07 structured clarification**：缺输入时返回 field、input type、allowed values、default、secret 和 fallback message。
- **P1-08 MCP-native elicitation**：SDK/client 支持时使用原生 elicitation，同时保留 fallback 字段。
- **P1-09 中文 intent 支持**：常见中文加密、脱敏、可逆、不可逆、哈希、等值查询、模糊查询、手机号、
  身份证、邮箱意图应有 deterministic synonyms 或 structured evidence 指引。
- **P1-10 workflow read-back**：当前 session 的 `plan_id` 应可读回 plan status、artifacts、scope 和 next actions。
- **P1-11 completion 单 schema 场景**：单 schema 可安全推断时 completion 可自动补上下文，否则返回 schema-first next action。
- **P1-12 runtime status summary**：提供 secret-free runtime/status 信息，包括 logical database、feature availability、transport 和 safe first checks。

## 6. P2 需求：诊断、包装和质量信号

- **P2-01 JDBC/config recovery 分类**：missing JDBC driver、authentication failed、connection timeout、database unavailable 等应返回安全类别和本地修复建议。
- **P2-02 bounded request id**：错误 payload 可带非持久、无 secret 的 request/trace identifier。
- **P2-03 token-safe health check**：README 或 startup hints 提供一个短小、token-safe 的健康检查路径。
- **P2-04 opt-in usability metrics**：已有 opt-in LLM lane 增加 next-action-follow 与 approval-violation 指标。
- **P2-05 default CI 不变**：real-model 测试仍不进入默认 CI，不要求默认凭证。
- **P2-06 env placeholders**：access token 与 JDBC credentials 支持环境变量占位或至少文档化，并对未解析占位给出安全错误。
- **P2-07 HTTP/Docker examples**：文档给出最小 HTTP、STDIO、Docker bind host、bearer token 配置示例，不嵌入真实 secret。
- **P2-08 bearer-token hint**：HTTP 认证失败时明确指向 bearer token 需求，但不暴露配置值。
- **P2-09 server identity**：协议需要 machine-friendly server name 时使用机器友好名称，display 字段保留人类可读名。
- **P2-10 Proxy topology preflight**：encrypt/mask workflow 连接到物理库而非 ShardingSphere-Proxy 逻辑视图时，在可识别情况下给出安全提示。

## 7. 非目标

- 不新增 `list_*`、`describe_*` 兼容工具矩阵。
- 不新增 planner、graph traversal、semantic/vector search、model-call ranking、跨会话长期记忆或用户行为学习。
- 不新增 preview token、approval token、durable approval record、RBAC 或 tenant isolation。
- 不把 `execute_update` 或 `apply_workflow` 变成绕过用户确认的执行工具。
- 不在 MCP 内做历史数据迁移、回填、生产审批或 rollback orchestration。
- 不把 real-model E2E 变成默认 CI。
- 不为了 P2 包装问题引入 OAuth、完整权限平台或观测平台。

## 8. 验收与验证

- 文档级整理：运行 `git diff --check`。
- Java/descriptor 改动：运行 scoped MCP tests。
- Java 改动：运行 scoped Checkstyle。
- 每个新增模型可见字段都要有 deterministic shape test、descriptor test、payload test 或文档核对项。
- 完成前再次确认当前分支仍为 `001-shardingsphere-mcp`。
