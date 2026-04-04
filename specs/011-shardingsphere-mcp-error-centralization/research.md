# Research: ShardingSphere MCP Error Centralization and Protocol Error Conversion

## Decision 1: 使用 repository-owned 异常基类加按错误码分层的子类

- **Decision**: 在 `mcp/core` 中新增一个抽象的 `MCPProtocolException extends RuntimeException`，
  并以 `MCPErrorCode` 为边界建立稳定子类：
  `MCPInvalidRequestException`、`MCPNotFoundException`、`MCPUnsupportedException`、
  `MCPTimeoutException`、`MCPTransactionStateException`、
  `MCPQueryFailedException`、`MCPUnavailableException`。
- **Rationale**:
  - 用户明确要求内部直接抛异常，不再返回协议错误对象。
  - 按错误码分层可以保证协议映射稳定，不需要在入口继续猜测语义。
  - `RuntimeException` 能保持当前调用链最小改动，不强迫所有方法声明 checked exception。
- **Alternatives considered**:
  - 继续使用 `IllegalArgumentException` / `IllegalStateException`，在入口靠 `instanceof` 猜语义:
    rejected，因为语义不稳定，仍然会把协议映射逻辑扩散到入口。
  - 只保留一个 `MCPProtocolException` 加枚举字段: rejected，
    因为 review 和单测不如按类型断言清晰。

## Decision 2: 再加一层高频命名叶子异常，但只覆盖固定重复场景

- **Decision**: 在 code-family exceptions 之上，只为高频且重复的固定场景创建命名叶子异常，
  例如 `UnsupportedToolException`、`UnsupportedResourceUriException`、
  `DatabaseCapabilityNotFoundException`、`InvalidPageTokenException`。
- **Rationale**:
  - 用户允许为确定异常创建独立类。
  - 这些场景出现在多个入口和回归测试里，命名异常能减少 message-based 断言。
  - 只覆盖高频稳定场景，能避免为了“异常全命名”而制造过多样板类。
- **Alternatives considered**:
  - 给每个 message 都建一个异常类: rejected，因为会放大文件数量和维护成本。
  - 完全不建叶子异常，只用 family exceptions: accepted as fallback，
    但对高频固定场景可读性较弱。

## Decision 3: 只保留一个集中错误转换组件，tool 与 resource 共用

- **Decision**: 在 `mcp/core` 中新增单一的 `MCPProtocolErrorConverter`，
  作为唯一的协议错误转换实现，负责：
  - 异常到 `MCPError` 的映射
  - `MCPError` 到 `MCPErrorResponse` payload 的转换
- **Rationale**:
  - 用户要求“必须都统一在一个集中的地方”。
  - tool 与 resource 的 public 错误形状已经一致，适合共用一套 converter。
  - converter 放在 core，bootstrap 不需要再知道异常细节。
- **Alternatives considered**:
  - `MCPToolPayloadResolver` 和 `MCPResourceController` 各写一份本地 catch-and-map:
    rejected，因为会重新制造两份协议规则。
  - 把转换逻辑塞进 `MCPToolPayloadResult.error(...)` 或 `MCPResourceResponseFactory`:
    rejected，因为这些类只是容器或中间工厂，不应成为全局错误规则中心。

## Decision 4: 协议边界只在两个总入口 catch，内部链路一律上抛

- **Decision**: 运行时错误只在两个总入口被捕获并转为 `MCPErrorResponse`：
  - `MCPToolPayloadResolver.resolve(...)`
  - `MCPResourceController.handle(...)`
- **Rationale**:
  - 这两个类已经是 tool / resource 的稳定协议入口。
  - `execute_query` 是 tool 子路径，因此其错误自然经由 `MCPToolPayloadResolver` 收敛。
  - 这样可以把 metadata、tool dispatch、execute、resource handler 都统一成纯异常语义。
- **Alternatives considered**:
  - 在更外层的 bootstrap transport 再 catch: rejected，
    因为那会把 core 的协议规则漏到 transport 层。
  - 在每个子 dispatcher / handler 内局部 catch: rejected，
    因为这正是当前分散问题的来源。

## Decision 5: `execute_query` 失败路径不再使用 `ExecuteQueryResponse.error(...)`

- **Decision**: `ExecuteQueryResponse` 收敛为 success-only 模型；
  一旦 `execute_query` 失败，执行链直接抛异常，
  最终由 `MCPToolPayloadResolver` 转成 `MCPErrorResponse`。
- **Rationale**:
  - 用户已经明确指出“其他地方一旦出了错误，直接抛异常”。
  - 当前 `ExecuteQueryResponse.error(...)` 让同一个 tool 同时拥有成功 envelope 和失败 envelope 两套模型。
  - 收敛后，`execute_query` 成功与失败的协议边界清晰：成功返回 query result，失败返回 error response。
- **Alternatives considered**:
  - 保留 `ExecuteQueryResponse.error(...)`，只在更外层再包一层 `MCPErrorResponse`:
    rejected，因为会继续保留内部失败结果对象。

## Decision 6: 保留 `search_metadata` 的跳过语义，改为前置能力判断

- **Decision**: `search_metadata` 遇到某个 object type 对当前数据库不支持时，
  继续保持“跳过该分支”的行为，但实现方式改成前置 capability 判断，
  不通过 “抛异常后 catch 并 continue” 来实现。
- **Rationale**:
  - 该语义是现有兼容行为，不能因为异常收敛而扩大失败范围。
  - 通过能力判断决定是否进入查询，可以保持异常语义只用于真正失败。
- **Alternatives considered**:
  - 对每个 object type 都直接查询，抛 `unsupported` 后本地吞掉:
    rejected，因为这会让“异常即失败”的边界重新模糊。

## Decision 7: 只改错误处理主链路，不修改深层资源管理器的非错误逻辑

- **Decision**: 本轮尽量把修改聚焦在错误表达和协议转换边界，
  深层类如 `MCPJdbcTransactionResourceManager` 保持现有业务逻辑，
  由上层执行器把其抛出的通用异常包成明确的 MCP 异常。
- **Rationale**:
  - 用户明确禁止无关小修小改。
  - 上层执行器本来就是 SQL / transaction 的错误语义汇聚点，
    在这里做异常封装改动面最小。
- **Alternatives considered**:
  - 深入每个下游类，把所有 `IllegalStateException` 都改成新异常:
    rejected，因为容易扩大范围并引入与本轮无关的重构。
