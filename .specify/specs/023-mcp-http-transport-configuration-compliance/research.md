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
- MCP builder guidance: local installed `mcp-builder` skill and its best-practices reference were loaded during this session; the package records relevant guidance directly instead of depending on a machine-local absolute path.

## Decision 1: Split HTTP Configuration By Responsibility

- **Decision**: Treat the current flat `HttpTransportConfiguration` as a compatibility DTO that should evolve into listener, exposure, authorization, and metadata groups.
- **Rationale**:
  - Current fields mix Tomcat binding, DNS rebinding protection, local shared-secret auth, OAuth introspection, and protected resource metadata in one constructor.
  - The repository conduct rules prioritize readability, simplicity, consistency, and removing unused code promptly.
  - Separate groups make validation branches visible and reduce accidental coupling between static token and OAuth metadata.
- **Alternatives considered**:
  - Keep the flat class and only add comments: rejected because the main problem is mixed ownership, not missing comments.
  - Add more booleans to the flat class: rejected because it would increase ambiguity.

## Decision 2: Prefer Deleting Static `accessToken`

- **Decision**: The preferred implementation path removes `transport.http.accessToken`.
- **Rationale**:
  - MCP Authorization is OAuth-oriented when the MCP server acts as a protected resource.
  - The current static token path compares a shared secret but also forces `authorizationServers` and emits OAuth protected resource metadata, creating a misleading OAuth shape.
  - Static shared secrets are weaker than OAuth introspection for remote HTTP and redundant for loopback-only local mode.
- **Possible retention exception**:
  - No production retention reason is accepted by this package.
  - A temporary test fixture may keep static-token coverage only long enough to prove migration failure or deletion behavior.
  - Production retention requires a new user-confirmed decision and must be modeled as non-OAuth `static-token`.
  - If retained, it must not publish OAuth protected resource metadata, must not require `authorizationServers`, and must be clearly named as non-OAuth.
- **Alternatives considered**:
  - Keep `accessToken` unchanged for convenience: rejected because it preserves the semantic mismatch.
  - Keep `accessToken` and call it OAuth: rejected because a static string comparison cannot validate issuer, resource, expiration, or scope.

## Decision 3: Replace Or Justify `allowRemoteAccess`

- **Decision**: Replace `allowRemoteAccess` with an explicit exposure mode unless reviewers prefer keeping it as an operator confirmation gate.
- **Rationale**:
  - The field does not affect runtime request behavior; it only validates that non-loopback binding was intentional.
  - `exposure.mode: local|remote` expresses the same intent while grouping it with `allowedOrigins`.
  - A confirmation gate is still a valid safety mechanism if the team wants a low-churn migration.
- **Alternatives considered**:
  - Delete it with no replacement: rejected because accidental `0.0.0.0` exposure should remain hard to configure.
  - Keep it unchanged forever: rejected because its name suggests runtime access control that it does not perform.

## Decision 4: Keep Exact Origin Allowlist For Remote HTTP

- **Decision**: Preserve or strengthen exact Origin validation for non-loopback HTTP exposure.
- **Rationale**:
  - MCP and mcp-builder guidance both emphasize DNS rebinding protection for Streamable HTTP.
  - Existing code already rejects missing, malformed, loopback-only, or unlisted Origin values for remote access.
  - This is one of the fields with clear security value and should not be merged away.
- **Alternatives considered**:
  - Allow wildcard origins: rejected because it weakens DNS rebinding protection.
  - Rely only on OAuth authorization: rejected because Origin protection and token authorization address different threats.

## Decision 5: Canonicalize Protected Resource Metadata

- **Decision**: Require OAuth `protectedResource` to be an HTTPS URL without fragment, reject query components unless justified, and avoid internal URI inference for production OAuth deployments.
- **Rationale**:
  - Protected resource metadata is client-facing security metadata.
  - The current fallback derives from the incoming request and can advertise loopback or internal reverse-proxy addresses.
  - OAuth token audience/resource validation should use the same canonical resource URI that metadata advertises.
- **Alternatives considered**:
  - Always infer from request URL: rejected because reverse proxies and local bindings make this unreliable.
  - Make `protectedResource` mandatory for all HTTP: rejected because local non-OAuth HTTP does not need metadata.

## Decision 6: Split Supported Scopes From Required Scopes

- **Decision**: Treat `scopesSupported` as metadata and introduce or document separate required scopes for token validation.
- **Rationale**:
  - Metadata advertisement and enforcement policy are related but not identical.
  - Current validation requires all `scopesSupported`, which can over-constrain clients if supported scopes are broader than the protected endpoint's requirements.
  - A split makes token validation intent reviewable.
- **Alternatives considered**:
  - Keep one field and document it as both supported and required: acceptable only as a conscious compatibility trade-off, but not the preferred design.

## Decision 7: Preserve Bearer Challenge And Metadata Discovery Semantics

- **Decision**: Treat `WWW-Authenticate` and protected resource metadata as one OAuth discovery contract.
- **Rationale**:
  - RFC 6750 requires Bearer challenges for protected resource failures and defines `invalid_token`, `insufficient_scope`, and `scope` behavior.
  - RFC 9728 defines `resource_metadata` as the challenge parameter that lets clients discover protected resource metadata.
  - Correct metadata without a correct challenge path still leaves OAuth clients unable to recover.
- **Alternatives considered**:
  - Test only metadata servlet output: rejected because the client discovery path starts from an authorization failure.

## Decision 8: Specify Introspection Security And Cache Boundaries

- **Decision**: OAuth introspection must define endpoint HTTPS requirements, client authentication, timeout/fail-closed behavior, credential redaction, and cache key/invalidation semantics before implementation.
- **Rationale**:
  - RFC 7662 makes the introspection endpoint a protected OAuth endpoint and token metadata can vary by authorization context.
  - A cache keyed only by token can become unsafe if issuer, resource, or scope policy changes.
  - Fail-open introspection would expose protected MCP operations when the authorization server is unavailable.
- **Alternatives considered**:
  - Keep the existing cache behavior as an implementation detail: rejected because this package changes the authorization contract.

## Decision 9: Defer Code Until A User Command

- **Decision**: This package records the design and task plan only.
- **Rationale**:
  - User explicitly said to wait for a later command before code changes.
  - Speckit is useful here as a requirements and evidence boundary before implementation.
- **Alternatives considered**:
  - Start refactoring immediately: rejected by user instruction.

## MCP Builder Review Notes

- Tool naming and response-format guidance in the generic mcp-builder reference does not directly apply to this HTTP configuration package.
- Relevant mcp-builder points are transport selection, DNS rebinding protection, actionable error messages, schema clarity, and evaluations.
- Response-format guidance is out of scope because this package changes HTTP transport configuration, not MCP tool result formats. If future work touches tool outputs, it must reopen that decision in the owning tool-contract package.

## Doubt-Driven Claims To Review

- Static `accessToken` should be deleted by default.
- `allowRemoteAccess` should be replaced by `exposure.mode` or justified as a confirmation gate.
- Protected resource metadata should only be emitted for OAuth-backed authorization.
- `scopesSupported` should not be used as the required scope list without an explicit trade-off.
- `WWW-Authenticate`, metadata endpoint placement, and bearer metadata fields must be reviewed together.
- Introspection cache and reverse-proxy public URI policy must be reviewed before implementation.
