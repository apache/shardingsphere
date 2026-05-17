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

# Requirements Checklist: MCP HTTP Transport Configuration Compliance

**Purpose**: Validate the Speckit package before any implementation starts.  
**Created**: 2026-05-17  
**Package**: `.specify/specs/023-mcp-http-transport-configuration-compliance/`

## Governance

- [x] Active branch confirmed without switching.
- [x] Package created manually without branch-changing Speckit commands.
- [x] Documentation-only work performed before implementation authorization.
- [x] Repository rules from `AGENTS.md` and `CODE_OF_CONDUCT.md` considered.
- [x] Source-driven official guidance recorded, with `mcp-builder` kept as a future implementation review gate.
- [x] Doubt-driven Codex CLI review rounds completed and findings classified in `doubt-review.md`.

## Requirement Quality

- [x] Every current `HttpTransportConfiguration` field has a future action category.
- [x] Static `accessToken` is confirmed for deletion, not assumed to stay.
- [x] Production static-token authorization is rejected.
- [x] Remote HTTP exposure keeps exact Origin allowlist protection.
- [x] OAuth metadata is separated from static token behavior.
- [x] Protected resource URI canonicalization is required when configured.
- [x] Scope metadata and challenge semantics use MCP-standard `scopes_supported` and `scope`; no custom `requiredScopes` field is added.
- [x] RFC 6750 challenge behavior is required.
- [x] RFC 7662 introspection security is required.
- [x] RFC 8707 resource/audience context is required.
- [x] RFC 9728 metadata fields and endpoint placement are required.
- [x] Reverse-proxy public resource URI risk is called out.
- [x] Migration behavior is included as a requirement.
- [x] Missing remote Origin is allowed only after OAuth Bearer authorization succeeds.
- [x] Active introspection responses without `exp` are accepted for the current request but not cached.
- [x] Scope challenge policy uses configured `scopesSupported` as the first-version server-configured basic functionality scope set.
- [x] Protected resource metadata URL placement is treated as a discovery contract.
- [x] POST, GET, and DELETE security coverage is mapped to future implementation decisions.

## Testability

- [x] YAML validation branches are mapped to future test tasks.
- [x] HTTP authorization and metadata behavior are mapped to future test tasks.
- [x] Remote HTTP E2E security behavior is mapped to future test tasks.
- [x] Dedicated HTTP transport configuration validator coverage is mapped to future tasks.
- [x] Documentation/distribution examples are mapped to future tasks.
- [x] mcp-builder review is a future verification task for implementation.
- [x] Commands and evidence are required before completion.

## Confirmed Decisions Before Code

- [x] Delete `transport.http.accessToken`; do not retain production `static-token` mode.
- [x] Replace `allowRemoteAccess` with `exposure.mode`.
- [x] Use targeted validation errors and migration docs for removed or renamed flat YAML fields.
- [x] Do not add custom `requiredScopes`; use configured `scopesSupported` with MCP-standard metadata and challenge semantics.
- [x] Require `protectedResource.uri` for production OAuth deployments.
- [x] Allow missing Origin on non-loopback HTTP only after OAuth Bearer authorization succeeds.
- [x] Accept active introspection responses without `exp` for the current request, but do not cache successful validation without expiration.
- [x] Serve endpoint-scoped protected resource metadata, keep root well-known support, and ensure `resource_metadata` challenge resolution.
- [x] Apply identical Origin and authorization gates to POST, GET, and DELETE on the MCP endpoint.
- [x] Define `authorizationServers` as issuer identifiers that must remain consistent with accepted token issuers.
