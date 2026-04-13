# Data Model: ShardingSphere MCP Remote HTTP Access Token

## Core Domain Entities

### HttpTransportConfiguration

- **Purpose**: 描述 Streamable HTTP transport 的运行时配置。
- **Fields**:
  - `enabled`
  - `bindHost`
  - `allowRemoteAccess`
  - `accessToken`
  - `port`
  - `endpointPath`
- **Validation rules**:
  - `bindHost` 为非 loopback 时，
    `allowRemoteAccess` 必须为 `true`。
  - `bindHost` 为非 loopback 且 `allowRemoteAccess = true` 时，
    `accessToken` 必须为非空、非空白字符串。
  - `bindHost` 为 loopback 时，
    `accessToken` 可为空；
    但一旦配置非空值，请求认证必须启用。

### SharedAccessToken

- **Purpose**: 表示部署方预置的共享访问密钥。
- **Fields**:
  - `rawValue`
  - `configured`
- **Validation rules**:
  - `rawValue` 不能来自数据库连接凭证。
  - `rawValue` 不能被运行时自动签发为 session token。
  - `configured = true` 时，必须参与每个 HTTP 请求的 admission check。

### AuthorizationHeader

- **Purpose**: 请求侧传递共享 token 的标准头部。
- **Fields**:
  - `scheme`
  - `credential`
- **Validation rules**:
  - `scheme` 必须为 `Bearer`
  - `credential` 必须与 `SharedAccessToken.rawValue` 完全匹配
  - 缺失、空白、格式错误或不匹配都视为认证失败

### AccessDecision

- **Purpose**: 表示 transport 层 admission check 的结果。
- **Fields**:
  - `authenticated`
  - `httpStatus`
  - `message`
- **Validation rules**:
  - 当 `authenticated = false` 时，
    `httpStatus` 必须为 `401`
    或现有 loopback `Origin` 校验产生的 `403`
  - `authenticated = false` 时，不得继续进入 session / protocol 校验

### StreamableHttpRequestContext

- **Purpose**: 描述进入 servlet 的最小 transport 上下文。
- **Fields**:
  - `bindHost`
  - `origin`
  - `authorization`
  - `sessionId`
  - `protocolVersion`
  - `method`
- **Validation rules**:
  - 认证顺序优先于 `sessionId` 与 `protocolVersion`
  - loopback `Origin` 校验与 token 校验都属于 transport boundary
  - admission 通过后，才允许进入会话生命周期校验

## Relationships

- 一个 `HttpTransportConfiguration` 至多关联一个 `SharedAccessToken`
- 一个 `StreamableHttpRequestContext` 至多携带一个 `AuthorizationHeader`
- 一个 `AuthorizationHeader` 与一个 `SharedAccessToken`
  共同决定一次 `AccessDecision`
- `AccessDecision` 先于 session / protocol validation 产生

## Derived Rules

- `allowRemoteAccess` 不是凭证
- `accessToken` 不是 session id
- `accessToken` 不是数据库用户名密码
- `accessToken` 不区分调用方身份，只区分是否允许接入
- 如果部署方需要多身份、多权限或 token 生命周期治理，
  应由后续 feature 或外部网关承接

## Compatibility Notes

- 现有 `HttpTransportConfiguration` 只是增加一个新字段，
  不改变其余字段语义
- 现有 MCP 工具、资源、SQL 执行与 capability 模型不需要新增字段
- 现有 loopback `Origin` 校验保留，作为 token 之外的本地边界规则
