<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Source-Driven MCP Standard Map

## Purpose

This file is the source-driven gate for the perfect-100 MCP scorecard.
No implementation may claim a protocol or E2E score of `100/100` unless the behavior maps to an official MCP source below or to a verified MCP Java SDK `1.1.2` API.

## Stack Detected

- `mcp/pom.xml`: MCP subchain uses Java 21.
- `mcp/bootstrap/pom.xml`: MCP Java SDK `1.1.2` through `mcp-core` and `mcp-json-jackson2`.
- `mcp/bootstrap/pom.xml`: embedded Tomcat uses `11.0.18`.
- `pom.xml`: Jackson is managed at `2.16.1`.
- `test/e2e/mcp/pom.xml`: MCP E2E uses Java 21, with live LLM groups disabled by default.

## Official Source Map

- Lifecycle and capability negotiation:
  `https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle`
- Standard transports:
  `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- HTTP authorization:
  `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`
- OAuth Bearer token usage:
  `https://www.rfc-editor.org/rfc/rfc6750.html`
- OAuth token introspection:
  `https://www.rfc-editor.org/rfc/rfc7662.html`
- OAuth resource indicators:
  `https://www.rfc-editor.org/rfc/rfc8707.html`
- OAuth authorization server metadata:
  `https://www.rfc-editor.org/rfc/rfc8414.html`
- Tools:
  `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- Resources and resource templates:
  `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- Prompts:
  `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- Completion:
  `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion`
- Pagination:
  `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/pagination`
- Security best practices:
  `https://modelcontextprotocol.io/docs/tutorials/security/security_best_practices`
- Java SDK implementation reference:
  `https://java.sdk.modelcontextprotocol.io/latest/server/`

## Mandatory Standard Decisions

- Initialization MUST negotiate protocol version, capabilities, and implementation information through standard MCP lifecycle messages.
- STDIO and Streamable HTTP MUST follow MCP transport shapes; stdout must carry only MCP messages for STDIO, and Streamable HTTP must expose a single MCP endpoint for POST and GET.
- Streamable HTTP MUST validate `Origin` when present, prefer localhost binding for local servers, and use proper authentication for protected connections.
- HTTP authorization, when enabled, MUST use MCP authorization semantics based on OAuth 2.1 protected resource metadata, header-only bearer token use,
  resource-scoped token validation, and no token passthrough.
- Complete OAuth token validation MUST prove the presented bearer token is active, issued by a configured authorization server, valid for the configured MCP resource,
  not expired or not-yet-valid, and has the required scopes before the MCP request reaches tool/resource/prompt handling.
- Invalid, expired, inactive, wrong-issuer, wrong-resource, malformed, or introspection-failure tokens MUST fail closed with `401`; active tokens with insufficient
  scope MUST fail with `403` and `WWW-Authenticate: Bearer error="insufficient_scope"`.
- Tool discovery MUST use `tools/list`; tool calls MUST use `tools/call`.
- Tool definitions MUST include valid `inputSchema`; structured outputs SHOULD use `outputSchema` and `structuredContent` with text fallback for compatibility.
- Tool execution errors MUST return tool results with `isError: true`; malformed protocol requests MUST use JSON-RPC protocol errors.
- Resource discovery MUST use `resources/list`; resource reads MUST use `resources/read`; resource templates MUST use `resources/templates/list`.
- Prompt discovery MUST use `prompts/list`; prompt retrieval MUST use `prompts/get`.
- Argument completion MUST use `completion/complete` with `ref/prompt` or `ref/resource`.
- Official MCP list pagination MUST use opaque `cursor` input and `nextCursor` output.
- Completion results MUST cap suggestions at the official maximum and use `values`, optional `total`, and `hasMore`.

## Rejected Non-Standard Patterns

- Do not treat `shardingsphere://capabilities` or any other ShardingSphere resource as a replacement for official MCP capability negotiation or discovery.
- Do not add custom JSON-RPC methods for behavior already covered by official MCP tools, resources, prompts, completion, logging, or pagination.
- Do not use `page`, `page_size`, `has_more`, or `next_offset` as official MCP list pagination fields.
  These may appear only inside application-level structured payloads when clearly documented as domain data.
- Do not encode authorization in query parameters or static transport conventions when HTTP authorization is enabled.
- Do not pass through tokens received from MCP clients to downstream services.
- Do not rely on latest SDK snippets if they are not compatible with MCP Java SDK `1.1.2`.

## Evidence Required For 100

- A protocol evidence matrix that maps each MCP method and payload shape to this source map.
- E2E tests that exercise official MCP discovery and invocation methods, not only ShardingSphere helper resources.
- Golden contracts for tools, resources, prompts, completion, transport errors, and tool execution errors.
- Security evidence for Streamable HTTP `Origin` handling, protected-resource metadata or a documented no-auth local mode, complete OAuth resource-server token validation
  when auth is enabled, and token-passthrough rejection.
- mcp-builder evaluation XML with ten read-only, independent, complex, realistic, verifiable, and stable questions.
- Scoped Maven tests, Checkstyle, and Spotless gates for every touched Java module.

## Current Evidence

- `protocol-evidence-matrix.md` maps lifecycle, transports, authorization, tools, resources, prompts, completion, pagination, and errors to official MCP sources.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPModelFirstContractPayloadBuilder.java` advertises official MCP discovery methods and classifies
  `shardingsphere://capabilities` as a ShardingSphere domain catalog resource.
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml` describes the capability resource as domain catalog data and points protocol discovery to official MCP methods.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServlet.java`
  serves OAuth protected resource metadata for Streamable HTTP.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandler.java`
  emits `WWW-Authenticate` challenges with `resource_metadata` and rejects malformed bearer syntax.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
  requires syntactically valid HTTPS authorization server URLs when HTTP authorization is enabled.
- `test/e2e/mcp/src/test/resources/llm/evaluation/mcp-builder-evaluation.xml` provides the ten-question read-only mcp-builder evaluation artifact.

## Reopened 2026-05-14 OAuth Validation Gate

- User intent: complete OAuth token validation, not only deployment-level bearer token matching.
- Current implementation gap: `HttpBearerAuthorizationHandler` compares the bearer value to `transport.http.accessToken`; it does not introspect a token,
  validate `active`, `iss`, `aud` or resource binding, `exp`, `nbf`, or scope.
- Current integration gap: the SDK delegate still receives `accessToken` through `AccessTokenHeaderConstraint`, so a complete OAuth implementation must avoid
  duplicate static-token validation after the OAuth validator succeeds.
- Source-driven implementation direction: use RFC 7662 token introspection as the first complete validation mode because it supports opaque tokens and JWTs
  without requiring local JWT/JWKS parsing in the MCP server.
- Completion evidence required before this gate can close:
  - unit tests for missing, malformed, inactive, expired, not-yet-valid, wrong issuer, wrong audience/resource, insufficient scope, introspection server error,
    malformed introspection response, and valid token;
  - E2E coverage with a local fake authorization server or introspection endpoint;
  - README and config schema documentation for introspection endpoint, resource, required scopes, client authentication secret handling, cache/fail-closed behavior,
    and migration from deployment-level `accessToken`;
  - Checkstyle, Spotless, and focused MCP bootstrap plus MCP E2E tests.

Verification:

- `EV-031`: Checkstyle and Spotless passed for `mcp/support`, `mcp/core`, `mcp/bootstrap`, and `test/e2e/mcp`.
- `EV-032`: focused unit and E2E authorization/discovery/evaluation tests passed.
- `EV-029`: golden contract plus production H2 official-discovery E2E tests passed.
