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

# Research: MCP HTTP Transport Configuration Compliance

## Source Inventory

- MCP Streamable HTTP transport: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- MCP Authorization: `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`
- OAuth Protected Resource Metadata: `https://www.rfc-editor.org/rfc/rfc9728.html`
- OAuth Token Introspection: `https://www.rfc-editor.org/rfc/rfc7662.html`
- OAuth Bearer Token Usage: `https://www.rfc-editor.org/rfc/rfc6750.html`
- OAuth Resource Indicators: `https://www.rfc-editor.org/rfc/rfc8707.html`
- Repository MCP Java SDK version: `mcp/bootstrap/pom.xml` sets `mcp-java-sdk.version` to `1.1.2`.
- Repository protocol target: `MCPTransportConstants` uses `ProtocolVersions.MCP_2025_11_25` and supports `MCP_2025_06_18`.

## Decision 1: Split HTTP Configuration By Responsibility

- **Decision**: Treat the current flat `HttpTransportConfiguration` as a compatibility DTO that should evolve into listener, exposure, authorization, and metadata groups.
- **Rationale**:
  - Current fields mix Tomcat binding, DNS rebinding protection, local shared-secret auth, OAuth introspection, and protected resource metadata in one constructor.
  - The repository conduct rules prioritize readability, simplicity, consistency, and removing unused code promptly.
  - Separate groups make validation branches visible and reduce accidental coupling between static token and OAuth metadata.
- **Alternatives considered**:
  - Keep the flat class and only add comments: rejected because the main problem is mixed ownership, not missing comments.
  - Add more booleans to the flat class: rejected because it would increase ambiguity.

## Decision 2: Delete Static `accessToken`

- **Decision**: The implementation path removes `transport.http.accessToken`.
- **Rationale**:
  - MCP Authorization is OAuth-oriented when the MCP server acts as a protected resource.
  - The current static token path compares a shared secret but also forces `authorizationServers` and emits OAuth protected resource metadata, creating a misleading OAuth shape.
  - Static shared secrets are weaker than OAuth introspection for remote HTTP and redundant for loopback-only local mode.
- **Compatibility behavior**:
  - No production static-token mode is retained by this package.
  - A temporary test fixture may reference the old field only to prove migration failure or deletion behavior.
  - Old `accessToken` YAML must fail with migration guidance to OAuth Bearer authorization.
- **Alternatives considered**:
  - Keep `accessToken` unchanged for convenience: rejected because it preserves the semantic mismatch.
  - Keep `accessToken` and call it OAuth: rejected because a static string comparison cannot validate issuer, resource, expiration, or scope.

## Decision 3: Replace `allowRemoteAccess`

- **Decision**: Replace `allowRemoteAccess` with an explicit exposure mode.
- **Rationale**:
  - The field does not affect runtime request behavior; it only validates that non-loopback binding was intentional.
  - `exposure.mode: local|remote` expresses the same intent while grouping it with `allowedOrigins`.
- **Alternatives considered**:
  - Delete it with no replacement: rejected because accidental `0.0.0.0` exposure should remain hard to configure.
  - Keep it unchanged forever: rejected because its name suggests runtime access control that it does not perform.

## Decision 4: Keep Exact Origin Allowlist For Remote HTTP

- **Decision**: Preserve or strengthen exact Origin validation for non-loopback HTTP exposure.
- **Rationale**:
  - MCP guidance emphasizes DNS rebinding protection for Streamable HTTP.
  - Existing code already rejects missing, malformed, loopback-only, or unlisted Origin values for remote access.
  - MCP requires rejecting invalid present Origin values; rejecting a missing Origin is stricter than the protocol minimum and must be recorded as a ShardingSphere hardening or compatibility decision.
  - This is one of the fields with clear security value and should not be merged away.
- **Alternatives considered**:
  - Allow wildcard origins: rejected because it weakens DNS rebinding protection.
  - Rely only on OAuth authorization: rejected because Origin protection and token authorization address different threats.
  - Reject every missing remote Origin: rejected because MCP requires rejecting invalid present Origin values but does not require breaking non-browser clients that cannot send Origin.
  - Accept missing Origin for all remote requests: rejected because missing Origin is allowed only after OAuth Bearer authorization succeeds.

## Decision 5: Canonicalize Protected Resource Metadata

- **Decision**: Require OAuth `protectedResource` to be an HTTPS URL without fragment, reject query components unless justified, and avoid internal URI inference for production OAuth deployments.
- **Rationale**:
  - Protected resource metadata is client-facing security metadata.
  - The current fallback derives from the incoming request and can advertise loopback or internal reverse-proxy addresses.
  - OAuth token audience/resource validation should use the same canonical resource URI that metadata advertises.
- **Alternatives considered**:
  - Always infer from request URL: rejected because reverse proxies and local bindings make this unreliable.
  - Make `protectedResource` mandatory for all HTTP: rejected because local non-OAuth HTTP does not need metadata.

## Decision 6: Use MCP-Standard Scope Fields Only

- **Decision**: Keep `scopesSupported` as the only configured scope list for this slice. It maps to MCP/OAuth `scopes_supported`; do not add a custom `requiredScopes` configuration field.
- **Rationale**:
  - MCP and RFC 9728 standardize `scopes_supported` metadata and `WWW-Authenticate` `scope` challenge behavior, not a `requiredScopes` configuration name.
  - MCP says clients use `WWW-Authenticate` `scope` challenge guidance first, and fall back to `scopes_supported` when the challenge omits scope.
  - For this first configuration cleanup, the configured `scopesSupported` value is the server-configured basic functionality scope set used for metadata, token scope validation, and challenge guidance.
- **Alternatives considered**:
  - Add `requiredScopes`: rejected because it is not an MCP protocol field and adds product-specific configuration before operation-specific authorization exists.
  - Keep `scopesSupported` for metadata only with no scope validation: rejected because protected remote HTTP would lose a useful OAuth authorization check.

## Decision 7: Preserve Bearer Challenge And Metadata Discovery Semantics

- **Decision**: Treat `WWW-Authenticate` and protected resource metadata as one OAuth discovery contract.
- **Rationale**:
  - RFC 6750 requires Bearer challenges for protected resource failures and defines `invalid_token`, `insufficient_scope`, and `scope` behavior.
  - RFC 9728 defines `resource_metadata` as the challenge parameter that lets clients discover protected resource metadata.
  - Correct metadata without a correct challenge path still leaves OAuth clients unable to recover.
- **Alternatives considered**:
  - Test only metadata servlet output: rejected because the client discovery path starts from an authorization failure.

## Decision 8: Specify Introspection Security And Cache Boundaries

- **Decision**: OAuth introspection must define endpoint HTTPS requirements, client authentication, timeout/fail-closed behavior, credential redaction, absent-expiration behavior, and cache key/invalidation semantics before implementation.
- **Rationale**:
  - RFC 7662 makes the introspection endpoint a protected OAuth endpoint and token metadata can vary by authorization context.
  - RFC 7662 makes `active` the required response field but keeps fields such as `exp`, `scope`, and `client_id` optional, so requiring `exp` unconditionally can reject otherwise valid opaque-token deployments.
  - A cache keyed only by token can become unsafe if issuer, resource, or scope policy changes.
  - Fail-open introspection would expose protected MCP operations when the authorization server is unavailable.
- **Alternatives considered**:
  - Keep the existing cache behavior as an implementation detail: rejected because this package changes the authorization contract.
  - Require `exp` for every introspection response: rejected as the default because it is stricter than RFC 7662; acceptable only if documented as a ShardingSphere policy.

## Decision 10: Keep Challenge Scope Standard And Simple

- **Decision**: `WWW-Authenticate scope` uses the configured `scopesSupported` values as the server-configured basic functionality scope set for this slice, without adding a custom required-scope field.
- **Rationale**:
  - RFC 9728 defines `scopes_supported` as scopes used in authorization requests for the protected resource.
  - MCP Authorization says insufficient-scope responses should include scopes needed for the current request.
  - The current MCP endpoint does not yet define per-tool or per-operation scope rules, so a separate configuration would be speculative.
- **Alternatives considered**:
  - Invent operation-specific scope rules in this package: rejected as out of scope for HTTP transport configuration cleanup.

## Decision 11: Treat Metadata URL Placement As A Runtime Contract

- **Decision**: The well-known metadata endpoint, `resource_metadata` challenge URI, and protected resource `resource` value must be tested together.
- **Rationale**:
  - RFC 9728 defines protected resource metadata names and discovery via well-known resources.
  - MCP clients rely on the `resource_metadata` challenge URI during 401/403 recovery.
  - A valid metadata payload is insufficient if the challenge URI points to an unregistered or internally derived endpoint.
- **Alternatives considered**:
  - Only test servlet payload content: rejected because clients discover the servlet through the challenge path.

## Decision 12: Recheck HTTP Method Coverage

- **Decision**: Authorization and Origin policy must be specified for POST, GET, and DELETE on the MCP endpoint before implementation.
- **Rationale**:
  - Streamable HTTP uses POST and GET; DELETE may terminate sessions when sessions are enabled.
  - A security policy that covers POST but misses GET or DELETE can leave session or stream behavior inconsistent.
  - This is a runtime wiring decision, not a new configuration field, so it belongs in implementation verification rather than config shape.
- **Alternatives considered**:
  - Limit this package to YAML validation only: rejected because `HttpTransportConfiguration` drives runtime security behavior.

## Decision 9: Defer Code Until A User Command

- **Decision**: This package records the design and task plan only.
- **Rationale**:
  - User explicitly said to wait for a later command before code changes.
  - Speckit is useful here as a requirements and evidence boundary before implementation.
- **Alternatives considered**:
  - Start refactoring immediately: rejected by user instruction.

## Future MCP Builder Review Notes

- `mcp-builder` is not used as current source evidence for this documentation-only reanalysis.
- It remains a future design/implementation review gate if implementation touches `mcp/**`, `distribution/mcp/**`, or `test/e2e/mcp/**`.
- Tool naming and response-format guidance in the generic mcp-builder reference does not directly apply to this HTTP configuration package.
- Response-format guidance is out of scope because this package changes HTTP transport configuration, not MCP tool result formats. If future work touches tool outputs, it must reopen that decision in the owning tool-contract package.

## Doubt-Driven Claims To Review

- Static `accessToken` should be deleted.
- `allowRemoteAccess` should be replaced by `exposure.mode`.
- Protected resource metadata should only be emitted for OAuth-backed authorization.
- `scopesSupported` is the only configured scope list for this slice and should map to MCP-standard metadata and challenge behavior.
- `WWW-Authenticate`, metadata endpoint placement, and bearer metadata fields must be reviewed together.
- Introspection cache and reverse-proxy public URI policy must be reviewed before implementation.
- Missing remote Origin may be accepted only after OAuth Bearer authorization succeeds.
- Introspection responses without `exp` are accepted for the current request but not cached.
- Scope challenge contents use the configured basic functionality scope set until operation-specific scope policy exists.
- POST, GET, and DELETE method coverage must be verified under the same approved security policy.
