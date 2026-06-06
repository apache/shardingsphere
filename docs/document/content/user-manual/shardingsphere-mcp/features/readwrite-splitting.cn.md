+++
title = "读写分离"
weight = 5
+++

读写分离 MCP 功能插件帮助用户为 ShardingSphere-Proxy 逻辑库规划、审查、执行和校验读写分离规则及状态变更。
本功能只生成读写分离 DistSQL，不生成存储单元注册、物理 DDL、索引 DDL、迁移、回填、数据探测或物理元数据探测。

## 前置条件

- 目标 `runtimeDatabases` 应连接到 ShardingSphere-Proxy。
- 写存储单元和读存储单元必须已经存在于 ShardingSphere-Proxy。
- 规划规则时，用户应提供逻辑库、规则名、写存储单元、读存储单元和负载均衡算法意图。

## 自然语言示例

- 查看 `<logic-database>` 的读写分离规则和负载均衡算法插件。
- 规划名为 `rw_ds` 的读写分离规则，写存储单元是 `write_ds`，读存储单元是 `read_ds_0, read_ds_1`。
- 禁用规则 `rw_ds` 中的读存储单元 `read_ds_1`，然后校验状态。

## 审查重点

- 确认规则计划使用 `CREATE`、`ALTER` 或 `DROP READWRITE_SPLITTING RULE`。
- 确认状态计划使用 `ALTER READWRITE_SPLITTING RULE ... ENABLE` 或 `DISABLE`。
- 确认存储单元名是已有逻辑存储单元，workflow 不会创建它们。

规则变更的通用审查流程见[规则变更流程](../plugin-workflow/)。

## 限制

- 仅支持 ShardingSphere-Proxy 逻辑库。
- 不创建或修复存储单元。
- 不探测物理数据源元数据。
- 对象名内容不能包含反引号、NUL、回车或换行。
