+++
title = "影子库"
weight = 6
+++

影子库 MCP 功能插件帮助用户规划、审查、执行和校验影子规则、默认影子算法，以及安全清理未使用的影子算法。
本功能只生成影子规则 DistSQL，不创建影子数据库或物理表，也不生成物理 DDL、索引 DDL、迁移、回填、数据探测、物理元数据探测或存储单元变更任务。

## 前置条件

- 目标 `runtimeDatabases` 应连接到 ShardingSphere-Proxy。
- 源存储单元和影子存储单元必须已经存在。
- 清理计划会先读取 Proxy 可见的规则、表规则、默认算法和已配置算法状态，再生成 `DROP SHADOW ALGORITHM`。

## 自然语言示例

- 查看 `<logic-database>` 已配置的影子规则和影子算法插件。
- 为表 `t_order` 规划影子规则，源存储单元是 `ds_0`，影子存储单元是 `ds_shadow`，算法使用列匹配策略。
- 只有在 Proxy 可见状态证明 `shadow_by_user_id` 未被使用时，才删除这个影子算法。

## 审查重点

- 确认规则计划使用影子 DistSQL，并引用已有存储单元。
- 确认默认算法计划只修改默认影子算法。
- 选择影子算法前，审查 `algorithm_recommendations`；`SQL_HINT` 没有必填属性。
- `VALUE_MATCH` 需要 `operation`、`column` 和 `value`；`REGEX_MATCH` 需要 `operation`、`column` 和 `regex`。
- 确认清理计划包含目标算法未被使用的证据。
- 执行 workflow 前，确认返回的 `plan_id`、`resources_to_read`、`next_actions` 和 `distsql_artifacts`。

规则变更的通用审查流程见[规则变更流程](../plugin-workflow/)。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 不创建影子数据库、物理表或存储单元。
- 清理能力仅限 DistSQL 可见的未使用影子算法。
- 对象名内容不能包含反引号、NUL、回车或换行。
