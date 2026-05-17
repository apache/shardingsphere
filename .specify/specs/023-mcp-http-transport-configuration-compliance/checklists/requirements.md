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
- [x] Static `accessToken` is treated as preferred-delete, not assumed to stay.
- [x] Retaining static token requires an explicit non-OAuth exception.
- [x] Remote HTTP exposure keeps exact Origin allowlist protection.
- [x] OAuth metadata is separated from static token behavior.
- [x] Protected resource URI canonicalization is required when configured.
- [x] Scope metadata and required-scope validation ambiguity is called out.
- [x] RFC 6750 challenge behavior is required.
- [x] RFC 7662 introspection security is required.
- [x] RFC 8707 resource/audience context is required.
- [x] RFC 9728 metadata fields and endpoint placement are required.
- [x] Reverse-proxy public resource URI risk is called out.
- [x] Migration behavior is included as a requirement.
- [x] Missing Origin behavior is classified as product hardening or compatibility policy rather than a direct MCP requirement.
- [x] Active introspection responses without `exp` are called out for explicit accept/reject/cache policy.
- [x] Scope challenge policy is separated from supported-scope metadata.
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

## Open Decisions Before Code

- [ ] Confirm default deletion of `transport.http.accessToken`; retain a renamed non-OAuth `static-token` mode only after new explicit user approval.
- [ ] Replace `allowRemoteAccess` with `exposure.mode` or keep it as confirmation.
- [ ] Choose legacy YAML migration behavior.
- [ ] Split `scopesSupported` from required scopes or document one-field trade-off.
- [ ] Define when `protectedResource.uri` is mandatory.
- [ ] Choose missing-Origin policy for non-loopback HTTP.
- [ ] Choose active-without-expiration introspection policy.
- [ ] Choose request-required scope challenge policy.
- [ ] Choose exact metadata URL registration and `resource_metadata` challenge resolution.
- [ ] Choose POST, GET, and DELETE method security coverage.
- [ ] Define `authorizationServers` issuer identifiers and accepted token issuer invariants.
