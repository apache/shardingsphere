# Contract: Production Runtime Provider

## Purpose

Define the additional contract introduced by this follow-up for loading a real ShardingSphere-backed production runtime behind the existing MCP domain and transport surfaces.

## Baseline Inheritance

- The public MCP domain surface continues to inherit the V1 resource, tool, result, and error contracts from `001-shardingsphere-mcp/contracts/mcp-domain-contract.md`.
- The HTTP session and transport behavior continues to inherit the Streamable HTTP contract from `001-shardingsphere-mcp/contracts/streamable-http-contract.md`.
- This contract adds only the production-runtime provider obligations required to make those inherited contracts real by default.

## Provider Configuration Contract

- Production launch mode MUST declare a concrete runtime provider.
- Provider configuration MUST include:
  - provider identity or type
  - provider-specific properties required to locate metadata and execution dependencies
  - runtime scope or database-scope inputs when the provider supports them
- Missing mandatory provider configuration MUST fail startup before the HTTP endpoint is published.

## Provider Lifecycle Contract

- Provider startup MUST execute the following stages in order:
  1. configuration validation
  2. dependency validation
  3. metadata reachability validation
  4. execution-path validation
  5. runtime handle publication
- Production mode MUST NOT skip directly from configuration load to HTTP listener publication.
- Provider shutdown MUST release provider-owned resources and invalidate the runtime handle.

## Metadata Projection Contract

- A production provider MUST project real logical databases and metadata objects into the MCP discovery model.
- If the selected runtime scope contains visible logical databases, discovery MUST NOT degrade to a silent empty success response.
- Runtime metadata failures MUST surface as explicit startup or request-time errors.
- Optional object support such as `index` MUST remain governed by capability responses.

## Execution Contract

- A production provider MUST expose a real execution adapter for `execute_query`.
- Requests reaching the adapter MUST already satisfy MCP validation rules for:
  - one statement only
  - unsupported command rejection
  - required database selection
  - transaction-database binding
- Backend-native execution outcomes MUST be mapped back into the MCP unified result and error surfaces.

## Capability Assembly Contract

- Database capability assembly order is fixed:
  1. transaction matrix defaults
  2. runtime metadata facts
  3. deployment overrides
- Providers MAY contribute runtime facts, but MUST NOT bypass the fixed assembly order.
- If runtime facts conflict with deployment overrides or fixed product guarantees, the implementation MUST resolve the conflict deterministically and keep the outcome reviewable.

## Refresh Visibility Contract

- A provider capable of executing DDL or DCL through MCP MUST emit enough information for the MCP layer to enforce:
  - current-session immediate visibility
  - global visibility within 60 seconds
- Lack of refresh support for a selected provider or topology MUST be explicit and MUST block claiming full production readiness for that slice.

## Startup Failure Contract

The following conditions MUST fail startup and surface operator-visible diagnostics:

- missing provider type
- missing required provider properties
- missing provider dependencies or drivers
- unreachable metadata source
- unreachable execution entrypoint
- unsupported topology for the selected provider

## Non-Goals

- This contract does not redefine the public MCP tool/resource names.
- This contract does not introduce automatic network discovery of MCP endpoints.
- This contract does not embed MCP into Proxy or JDBC processes.
