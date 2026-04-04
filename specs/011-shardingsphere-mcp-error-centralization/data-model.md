# Data Model: ShardingSphere MCP Error Centralization and Protocol Error Conversion

## Core Runtime Entities

### MCPProtocolException

- **Purpose**: MCP 内部抛出的抽象运行时异常基类，代表一个已经确定协议语义的失败，
  但自身不负责构造 payload。
- **Fields**:
  - `message`
  - `cause`
- **Validation rules**:
  - 所有已知稳定协议错误都应优先通过其子类表达，而不是重新抛裸 `IllegalStateException`。
  - `message` 必须可直接暴露给现有 MCP 客户端，不包含栈信息或内部类名。

### Code-Family Exceptions

- **Purpose**: 与 `MCPErrorCode` 一一对齐的稳定异常族。
- **Members**:
  - `MCPInvalidRequestException`
  - `MCPNotFoundException`
  - `MCPUnsupportedException`
  - `MCPTimeoutException`
  - `MCPTransactionStateException`
  - `MCPQueryFailedException`
  - `MCPUnavailableException`
- **Validation rules**:
  - 每个 family exception 对应且只对应一个 `MCPErrorCode`。
  - 新的业务错误如果已有稳定 `MCPErrorCode` 归属，应优先落到对应 family exception。

### Named Leaf Exceptions

- **Purpose**: 为固定高频场景提供可读性更高、测试更稳定的命名异常。
- **Initial candidates**:
  - `UnsupportedToolException`
  - `UnsupportedResourceUriException`
  - `DatabaseCapabilityNotFoundException`
  - `InvalidPageTokenException`
  - `QueryDidNotReturnResultSetException`
  - `StatementClassNotSupportedException`
- **Validation rules**:
  - 叶子异常必须继承某个 family exception。
  - 叶子异常只用于固定场景，不为一次性 message 大量扩增类数量。

### MCPProtocolErrorConverter

- **Purpose**: 集中把异常转换为 `MCPError` 与 `MCPErrorResponse` payload。
- **Inputs**:
  - `Throwable`
- **Outputs**:
  - `MCPError`
  - `MCPErrorResponse`
  - `Map<String, Object>` payload
- **Validation rules**:
  - 生产代码中只有该组件能构造 `MCPErrorResponse`。
  - 同一异常输入必须稳定映射到同一 `error_code`。
  - 未知异常必须回退到 `unavailable`。

### Success-Only Result Models

- **Purpose**: 只承载成功结果的轻量模型，避免错误继续作为“数据返回值”传递。
- **Candidates**:
  - `MetadataQueryResult` success-only 版本，或被内联移除
  - `ToolDispatchResult` success-only 版本
  - `ExecuteQueryResponse` success-only 版本
  - `MCPToolPayloadResult` 被动容器版本，只表达 payload 与 `isError`
- **Validation rules**:
  - 不再包含 `getError()`、`error(...)` 这类失败承载接口。
  - 错误一律通过异常链路表达。

## Relationships

- metadata query、tool dispatch、resource handler、SQL execute 在失败时抛出 `MCPProtocolException`
  或可明确映射的异常。
- `MCPToolPayloadResolver.resolve(...)` 与 `MCPResourceController.handle(...)`
  使用同一个 `MCPProtocolErrorConverter`。
- `MCPProtocolErrorConverter` 输出 `MCPErrorResponse` payload，
  再由 tool / resource 入口包装为各自协议需要的返回值。
- `ExecuteQueryResponse` 只存在于 `execute_query` 成功路径。

## Canonical Error Flows

### Tool Flow

```text
MCPToolPayloadResolver.resolve(...)
  -> dispatch metadata / execute / capability logic
  -> internal failure throws MCPProtocolException (or mapped exception)
  -> catch in resolver
  -> MCPProtocolErrorConverter.toPayload(...)
  -> MCPToolPayloadResult(payload, isError=true)
```

### Resource Flow

```text
MCPResourceController.handle(...)
  -> dispatch resource handler
  -> internal failure throws MCPProtocolException (or mapped exception)
  -> catch in controller
  -> MCPProtocolErrorConverter.toPayload(...)
  -> return MCPErrorResponse payload map
```

### Execute Query Flow

```text
MCPSQLExecutionFacade / JDBC executors
  -> any failure throws typed exception
  -> no ExecuteQueryResponse.error(...)
  -> bubble to MCPToolPayloadResolver
  -> convert to MCPErrorResponse payload
```

## Mapping Invariants

- `MCPInvalidRequestException` -> `INVALID_REQUEST`
- `MCPNotFoundException` -> `NOT_FOUND`
- `MCPUnsupportedException` -> `UNSUPPORTED`
- `MCPTimeoutException` -> `TIMEOUT`
- `MCPTransactionStateException` -> `TRANSACTION_STATE_ERROR`
- `MCPQueryFailedException` -> `QUERY_FAILED`
- `MCPUnavailableException` -> `UNAVAILABLE`

## Legacy Fallback Rules

- `MCPSessionNotExistedException` 应改造成 `MCPNotFoundException` 子类，
  或至少由 converter 显式映射为 `NOT_FOUND`。
- `IllegalArgumentException` 只作为过渡期 fallback 映射到 `INVALID_REQUEST`，
  不能继续作为新的主流错误表达方式。
- 未被显式识别的 `RuntimeException` / `Exception` 统一回退为 `UNAVAILABLE`。

## Behavior Invariants

- 生产代码中的运行时失败路径必须只有一个 `MCPErrorResponse` 构造中心。
- `search_metadata` 的“跳过不支持 object type”不视为失败，不得依赖异常吞掉实现。
- 同一确定场景的错误码和 message 在 tool / resource 之间必须一致。
- 本轮设计不允许为了错误收敛扩大到无关业务重构。
