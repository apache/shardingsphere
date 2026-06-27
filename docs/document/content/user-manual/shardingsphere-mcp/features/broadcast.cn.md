+++
title = "广播表"
weight = 4
+++

广播表 MCP 功能插件帮助用户为 ShardingSphere-Proxy 逻辑库规划、审查、执行和校验广播表规则变更。
本功能只生成广播表规则 DistSQL，不生成物理表 DDL、索引 DDL、数据迁移、回填、数据探测、物理元数据探测或存储单元变更任务。

## 前置条件

- 目标 `runtimeDatabases` 应连接到 ShardingSphere-Proxy。
- 用户应提供逻辑库和广播表名。
- 已有规则状态来自 Proxy 可见的 DistSQL 资源。

## 自然语言示例

- 查看 `<logic-database>` 当前有哪些广播表规则。
- 为 `config_region` 和 `config_feature` 规划广播表规则，先预览不要执行。
- 删除 `config_region` 的广播表规则，并校验结果。

## 审查重点

- 确认计划语句是 `CREATE BROADCAST TABLE RULE` 或 `DROP BROADCAST TABLE RULE`。
- 确认所有表名都是逻辑表名。
- 执行 workflow 前，确认返回的 `plan_id`、`resources_to_read`、`next_actions` 和 `distsql_artifacts`。
- 广播表规划不需要算法推荐或算法属性要求。
- 执行前先预览 workflow，执行后校验 Proxy 可见规则状态。

规则变更的通用审查流程见[规则变更流程](../plugin-workflow/)。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 不创建、修改或探测物理表。
- 不注册、修改、注销或修复存储单元。
- 对象名内容不能包含反引号、NUL、回车或换行。
