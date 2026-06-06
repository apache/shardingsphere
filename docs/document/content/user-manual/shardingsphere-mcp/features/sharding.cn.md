+++
title = "分片"
weight = 7
+++

分片 MCP 功能插件帮助用户规划、审查、执行和校验分片表规则、绑定规则、默认分片策略、键生成器、键生成策略，以及安全清理未使用的分片组件。
本功能只生成分片规则 DistSQL，不生成物理 DDL、索引 DDL、迁移、回填、数据探测、物理元数据探测或存储单元变更任务。

## 前置条件

- 目标 `runtimeDatabases` 应连接到 ShardingSphere-Proxy。
- 必要时应明确提供数据节点、存储单元、分片列、算法类型、键生成器、审计器和规则名。
- 清理计划会先读取未使用和 used-by DistSQL 资源，再生成 `DROP SHARDING ALGORITHM`、`DROP SHARDING KEY GENERATOR` 或 `DROP SHARDING AUDITOR`。

## 自然语言示例

- 查看 `<logic-database>` 的分片表规则、表节点、算法、键生成器、审计器和未使用组件。
- 为 `t_order` 规划分片表规则，使用明确的数据节点和标准分片策略。
- 规划默认表分片策略，使用算法 `t_order_inline`。
- 只有在 Proxy 可见状态证明 `t_order_inline` 未被使用时，才删除这个分片算法。

## 审查重点

- 确认表规则计划只使用分片规则 DistSQL 和逻辑对象名。
- 确认键生成器和键生成策略计划不会把键生成器属性与分片算法属性混用。
- 确认清理计划包含 unused-state 和 used-by 检查。
- 执行前先预览 workflow，执行后校验 Proxy 可见规则状态。

规则变更的通用审查流程见[规则变更流程](../plugin-workflow/)。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 不创建物理表、索引或存储单元。
- 不迁移或回填已有数据。
- 对象名内容不能包含反引号、NUL、回车或换行。
