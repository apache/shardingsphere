+++
title = "协议表面"
weight = 1
+++

ShardingSphere-MCP 的 public surface 由 `META-INF/shardingsphere-mcp/mcp-descriptors` 下的 descriptors 定义。
MCP runtime 使用这些 descriptors 发布 tools、resources、resource templates、prompts 和 completions。

## 协议能力

ShardingSphere-MCP 面向 MCP protocol revision `2025-11-25`。

已启用：

- `resources/list`
- `resources/templates/list`
- `resources/read`
- `tools/list`
- `tools/call`
- `prompts/list`
- `prompts/get`
- `completion/complete`

未实现或后续范围：

- Resource subscriptions。
- Resource、tool、prompt list-changed notifications。
- ShardingSphere 产品日志通过 `notifications/message` 输出。
- `progress`。
- `notifications/cancelled`。
- task-augmented requests。
- MCP `icons` 和 `Tool.execution` 字段，等待 MCP Java SDK 边界支持。

`roots` 和 `sampling` 是 client capabilities。
ShardingSphere-MCP 不要求 roots，也不会发送 `sampling/createMessage` 请求。

## Tools

`database_gateway_search_metadata`

- 搜索逻辑库 metadata。
- 可按 `database`、`schema`、`query`、`object_types` 收窄范围。
- `object_types` 支持 `database`、`schema`、`table`、`view`、`column`、`index`、`sequence`。

`database_gateway_validate_proxy_connectivity`

- 在正式接入前校验 JDBC 预检配置。
- 必填输入为 `databaseType`、`jdbcUrl`、`username`、`driverClassName`。
- 可选输入为 `password` 和 `database`；无密码账号可以省略 `password` 或传空字符串。
- 返回 `status`、有序 `checks`、整体 `category` 和结构化 `recovery` 对象。
- 常见失败分类包括 `missing_jdbc_driver`、`authentication_failed`、`authorization_failed`、`connection_timeout`、`invalid_configuration`、`database_unavailable`、`connection_failed` 和 `database_not_visible`。

`database_gateway_execute_query`

- 执行一个 classifier 允许的 `SELECT` 或 `EXPLAIN ANALYZE`。
- 拒绝 DML、DDL、DCL、事务控制、savepoint 和已知有副作用的查询形态。
- `max_rows` 范围是 `0..5000`，省略或 `0` 使用服务端默认值 `100`。
- `timeout_ms` 范围是 `0..300000`，`0` 表示不设置显式 timeout。

`database_gateway_execute_update`

- 预览或执行一个支持的有副作用 SQL。
- `execution_mode=preview` 只做分类和副作用范围预览。
- `execution_mode=execute` 在 review 后执行 SQL。
- 多语句和禁用命令会被拒绝。

`database_gateway_apply_workflow`

- 对当前 session 中已有的 workflow plan 做 preview、执行或导出。
- `execution_mode` 支持 `preview`、`review-then-execute`、`manual-only`。
- `approved_steps` 只能使用 preview 返回的 approval step。

`database_gateway_validate_workflow`

- 校验当前 session 中已有的 workflow plan。
- 用于 planning review 后或 apply 后确认 runtime 状态。

Feature plugin planning tools：

- `database_gateway_plan_encrypt_rule`
- `database_gateway_plan_mask_rule`

## Resources

Runtime 与能力：

- `shardingsphere://capabilities`
- `shardingsphere://runtime`
- `shardingsphere://databases`
- `shardingsphere://databases/{database}`
- `shardingsphere://databases/{database}/capabilities`

Metadata：

- `shardingsphere://databases/{database}/schemas`
- `shardingsphere://databases/{database}/schemas/{schema}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes`
- `shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}`
- `shardingsphere://databases/{database}/schemas/{schema}/views`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns`
- `shardingsphere://databases/{database}/schemas/{schema}/views/{view}/columns/{column}`
- `shardingsphere://databases/{database}/schemas/{schema}/sequences`
- `shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}`

Workflow：

- `shardingsphere://workflows/{plan_id}`

Feature resources：

- `shardingsphere://features/encrypt/algorithms`
- `shardingsphere://features/encrypt/databases/{database}/rules`
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules`
- `shardingsphere://features/mask/algorithms`
- `shardingsphere://features/mask/databases/{database}/rules`
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules`

## Prompts

- `inspect_metadata`：引导模型读取 metadata，不在用户只要求查看 metadata 时执行 SQL。
- `safe_sql_execution`：引导模型区分 read-only query 和 side-effecting SQL。
- `recover_workflow`：引导模型从失败或过期 workflow 中恢复。
- `plan_encrypt_rule`：引导模型规划 Encrypt feature workflow。
- `plan_mask_rule`：引导模型规划 Mask feature workflow。

## Completions

Completions 用于补全运行时名称、metadata identifier、算法和当前 session 中的 workflow `plan_id`。
Client 应在选择不确定的 database、schema、table、column、algorithm 或 `plan_id` 前调用 `completion/complete`，或读取最近的 MCP resource。

## 响应与恢复

列表型业务 payload 通常包含：

- `items`
- `count`
- `has_more`
- `continuation_mode`

大结果 payload 会使用：

- `truncated`
- `total_count`
- `returned_count`
- `large_result_guidance`

可恢复错误 payload 保留 `message`，并增加 `recovery` 提示。
常见恢复场景包括缺失参数、不支持的 tool/resource、非法枚举、workflow 状态错误和 SQL tool 选错。

JSON-RPC 数字错误码属于 MCP 协议错误契约。
