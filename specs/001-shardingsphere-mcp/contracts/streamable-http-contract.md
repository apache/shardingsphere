# Contract: Streamable HTTP Transport

## Purpose

Define the transport-level contract for the ShardingSphere MCP HTTP entry point so implementation, integration tests, and operations all share the same baseline.

## Endpoint Baseline

- **Protocol**: MCP Streamable HTTP
- **Protocol version baseline**: `2025-11-25`
- **Endpoint path**: `/mcp`
- **Supported methods**:
  - `POST /mcp`
  - `GET /mcp`
  - `DELETE /mcp`

## Method Semantics

### `POST /mcp`

- Creates a new MCP HTTP session when `MCP-Session-Id` is absent.
- Returns both `MCP-Session-Id` and the negotiated `MCP-Protocol-Version`.
- Session-bound follow-up `POST` dispatch handles MCP tool and resource calls.
- When `transport.http.accessToken` is configured, all `POST` requests require `Authorization: Bearer <token>`.
- Follow-up `POST` requests validate access token, session existence, negotiated protocol version, and local-mode `Origin` rules before dispatch.

### `GET /mcp`

- Provides the server-to-client SSE side of Streamable HTTP.
- Exists in V1 even though business behavior does not depend on server push.
- Must remain compatible with future protocol evolution without changing the endpoint shape.

### `DELETE /mcp`

- Explicitly closes an MCP session.
- Must release session resources and force rollback of any uncommitted transaction before closing.
- Is required in V1 because the service uses a stateful session model.

## Session Headers

- **`Authorization`**
  - Optional in loopback local mode when no access token is configured.
  - Mandatory on all HTTP requests when `transport.http.accessToken` is configured.
  - Uses `Bearer <token>` and acts only as a runtime admission gate.
- **`MCP-Session-Id`**
  - Returned by the server after successful initialization.
  - Mandatory on follow-up HTTP requests.
- **`MCP-Protocol-Version`**
  - Captures the negotiated version.
  - If present after initialization, it must match the session-negotiated version.
  - If omitted while a valid session exists, the server may fall back to the session-negotiated version and log a warning.

## Transport Error Contract

- Missing or invalid access token: `401 Unauthorized`
- Missing required session id on a follow-up request: `400 Bad Request`
- Protocol version mismatch against the negotiated session version: `400 Bad Request`
- Invalid local-mode `Origin`: `403 Forbidden`
- Unknown or closed session id: `404 Not Found`

## Boundary Contract

- Local mode defaults to binding `127.0.0.1`.
- If a loopback-bound runtime receives an explicit `Origin`, its host must still resolve to loopback / localhost.
- If `transport.http.accessToken` is configured, the built-in runtime requires a matching shared bearer token before session / protocol dispatch.
- If the HTTP endpoint is exposed outside a trusted network, still place it behind an external gateway or reverse proxy.

## Session Ownership Contract

- HTTP transport is stateful in V1.
- Cluster routing requires sticky sessions.
- Session state is stored in local memory only.
- Node restart or failure invalidates local sessions and rolls back active transactions.
- V1 does not support distributed session stores or transaction failover recovery.
