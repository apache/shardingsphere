# Contract: ShardingSphere MCP Centralized Protocol Error Contract

## Shared Error Payload

所有通过协议返回的失败结果都统一收敛为 `MCPErrorResponse` payload：

```json
{
  "error_code": "invalid_request",
  "message": "Database is required."
}
```

约束如下：

- `error_code` 继续使用当前小写下划线风格
- `message` 继续直接暴露当前稳定文本
- 不新增其他公共字段

## Tool Call Failure Contract

`MCPToolPayloadResolver` 是 tool 的错误转换总入口。

失败时必须满足：

- `structuredContent` 为 `MCPErrorResponse` payload
- `isError` 为 `true`
- `text content` 继续输出同一错误 payload 的 JSON 字符串

兼容性要求：

- success payload 形状保持不变
- `execute_query` 失败时不再返回 `ExecuteQueryResponse` 错误 envelope
- metadata tool、capability tool、execute tool 失败时都遵循同一错误 payload 契约

## Resource Read Failure Contract

`MCPResourceController` 是 resource 的错误转换总入口。

失败时必须满足：

- `handle(resourceUri)` 直接返回 `MCPErrorResponse` payload map
- unsupported URI、database capability 不存在、metadata 不支持等错误都使用同一 payload 形状

兼容性要求：

- `resources/list` 不变
- `resources/read` 的成功内容不变
- 当前资源错误 payload 形状与 `MCPErrorResponse` 保持一致

## Exception-to-Code Mapping Contract

稳定映射如下：

- `MCPInvalidRequestException` -> `invalid_request`
- `MCPNotFoundException` -> `not_found`
- `MCPUnsupportedException` -> `unsupported`
- `MCPTimeoutException` -> `timeout`
- `MCPTransactionStateException` -> `transaction_state_error`
- `MCPQueryFailedException` -> `query_failed`
- `MCPUnavailableException` -> `unavailable`

高频命名异常沿用其父类错误码，例如：

- `UnsupportedToolException` -> `invalid_request`
- `UnsupportedResourceUriException` -> `invalid_request`
- `DatabaseCapabilityNotFoundException` -> `not_found`
- `InvalidPageTokenException` -> `invalid_request`

## Message Stability Contract

本轮必须保留以下已稳定 message 语义：

- `Unsupported tool.`
- `Unsupported resource URI.`
- `Database capability does not exist.`
- `Database and sql are required.`
- `Database is required.`
- `Schema is required.`
- `Table name is required.`
- `Invalid page token.`
- `Index resources are not supported for the current database.`
- `Statement class is not supported.`
- `Query did not return a result set.`

## Explicit Compatibility Notes

- 不引入新的 public error code
- 不改变 `MCPErrorResponse` JSON 字段
- 不改变 tool / resource 成功 payload
- 不允许通过新增一层错误 envelope 来“兼容”旧逻辑
