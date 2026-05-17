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
- [x] Source-driven official guidance recorded, with `mcp-builder` used as a design sanity check and kept as a future implementation review gate.
- [x] Doubt-driven Codex CLI review rounds completed and findings classified in `doubt-review.md`.

## Requirement Quality

- [x] Every current `HttpTransportConfiguration` field has a future action category.
- [x] `transport.type` is the only approved transport selector.
- [x] MCP Authorization is recorded as optional and not part of the current package.
- [x] Static `accessToken` is not retained as production authorization.
- [x] `bindHost`, `port`, and `endpointPath` are optional and default to `127.0.0.1`, `18088`, and `/mcp`.
- [x] `transport.http` is valid only for `transport.type: STREAMABLE_HTTP`; `STDIO` configs do not carry HTTP listener fields.
- [x] `bindHost`, `port`, and `endpointPath` listener validation boundaries are recorded.
- [x] `endpointPath` rejects URL/query/fragment or ambiguous double-slash forms.
- [x] Remote HTTP exposure is expressed only by non-loopback `bindHost`.
- [x] User-configurable Origin allowlist is removed from YAML.
- [x] MCP-required Origin protection remains an internal runtime requirement.
- [x] Internal Origin matrix is recorded: missing Origin accepted for non-browser clients; malformed, `null`, or non-loopback present Origin rejected; loopback Origin accepted only for loopback-bound local HTTP.
- [x] OAuth, token validation, protected resource metadata, bearer metadata, and scope configuration are deferred to future work.
- [x] Migration behavior is included as a requirement.
- [x] POST, GET, and DELETE Origin coverage is mapped to future implementation decisions.
- [x] `allowRemoteAccess`, `remote`, `exposure`, and `allowedOrigins` are removed from the approved YAML shape.
- [x] Current YAML shape contains no `authorization`, `oauth`, `tokenValidation`, `introspection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `requiredScopes`, or `bearerMethodsSupported` subtree.
- [x] Known removed fields and unknown fields under `transport` or `transport.http` are required to fail loudly rather than be silently ignored.

## Testability

- [x] YAML validation branches are mapped to future test tasks.
- [x] `STDIO` with forbidden `transport.http` is mapped to future validator tests.
- [x] `port: 0` is mapped as ephemeral-port test or embedded usage, not a distribution example.
- [x] HTTP listener defaulting and internal Origin behavior are mapped to future test tasks.
- [x] Remote HTTP E2E security behavior is mapped to future test tasks.
- [x] Static-token and OAuth introspection E2E success paths are mapped for deletion, not retention.
- [x] Shared E2E fixtures and distribution helper tests are mapped to the minimal `transport.type` shape.
- [x] mcp-builder LLM evaluation XML and validator constants are mapped for replacement where they currently require OAuth challenge behavior.
- [x] Valid GET behavior on the single Streamable HTTP endpoint is mapped to future E2E coverage.
- [x] Unrelated external LLM provider authorization redaction and SQL metadata introspection tests are explicitly preserved.
- [x] Dedicated HTTP transport configuration validator coverage is mapped to future tasks.
- [x] Documentation/distribution examples are mapped to future tasks.
- [x] mcp-builder review is a future verification task for implementation.
- [x] Commands and evidence are required before completion.

## Confirmed Decisions Before Code

- [x] Do not add HTTP authorization in this slice.
- [x] Do not retain `transport.http.accessToken` as production `static-token` mode.
- [x] Replace `transport.http.enabled` and `transport.stdio.enabled` with `transport.type`.
- [x] Make the `transport.http` subtree optional for default local Streamable HTTP.
- [x] Reject the `transport.http` subtree when the selected transport is STDIO.
- [x] Do not replace `allowRemoteAccess` with another remote mode field.
- [x] Use targeted validation errors and migration docs for removed, renamed, or deferred flat YAML fields.
- [x] Remove `allowedOrigins` from YAML and keep MCP-required Origin handling internal.
- [x] Apply MCP-required HTTP safeguards to POST, GET, and DELETE on the MCP endpoint.
- [x] Defer MCP OAuth Authorization, token introspection, protected resource metadata, bearer challenges, and scope policy to a future independent Speckit package.
- [x] Document that remote HTTP in this slice has no built-in identity enforcement and needs an external trust boundary when deployed beyond a trusted environment.
- [x] Do not delete by broad `Authorization` or `introspection` keyword search; classify each hit by purpose and path.
