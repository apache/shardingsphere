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

# Feature Specification: MCP HTTP Transport Configuration Compliance

**Feature Branch**: `001-shardingsphere-mcp`  
**Created**: 2026-05-17  
**Status**: Draft  
**Input**: User requested a new Speckit package for `HttpTransportConfiguration` field purpose, necessity, uselessness, optimization, merge opportunities, and MCP compliance issues. User explicitly forbids branch switching and asks not to change production code before a later command.

## Goal

Make MCP HTTP transport configuration smaller, clearer, and standards-aligned.
The package separates listener binding, remote exposure, origin protection, authorization, OAuth protected resource metadata, and token validation into traceable responsibilities before any code change begins.

The preferred direction is to remove the built-in static `transport.http.accessToken` field.
If future implementation keeps it, the implementation must document a narrow reason that is stronger than "simple auth is convenient" and must keep it explicitly outside OAuth semantics.

## Clarifications

### Session 2026-05-17

- User confirmed design documents may be written now, but production code must wait for an explicit later command.
- User confirmed this must be a completely new Speckit package rather than an addition to package 006 or 016.
- User prefers deleting `transport.http.accessToken`; retaining it requires a convincing reason and a design that avoids pretending it is OAuth.
- User confirmed `mcp-builder` is available as a skill and should be used for design/implementation reasonableness checks when MCP or MCP E2E implementation changes are planned.
- User allowed doubt-driven review using Codex CLI.
- User requested reanalysis of any worthwhile design questions before implementation; this round remains documentation-only.

## Hard Constraints

- Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Do not modify production, test, distribution, or E2E code before the user gives an explicit implementation command.
- Preserve existing worktree changes and never revert unrelated edits.
- Use source-driven decisions for MCP and OAuth behavior; official MCP and RFC sources outrank convenience patterns.
- Use `mcp-builder` review gates for any future change touching `mcp/**`, `distribution/mcp/**`, or `test/e2e/mcp/**`.
- Use doubt-driven review for non-trivial design and implementation decisions.
- Run at least one doubt-driven review before pre-implementation decisions are finalized, and run a second review after any implementation slice that changes MCP runtime or E2E behavior.

## User Scenarios and Testing

### User Story 1 - Maintainers Can Classify Every HTTP Field (Priority: P1)

As an MCP transport maintainer, I want every HTTP transport field to have a single clear purpose and necessity category so that reviewers can tell whether it is required, optional, deprecated, or removable.

**Independent Test**: Read the configuration docs, validation tests, and source map; verify every current `HttpTransportConfiguration` field has a mapped outcome and no field has duplicate ownership.

**Acceptance Scenarios**:

1. Given `enabled`, `bindHost`, `port`, and `endpointPath`, when reviewers inspect the package, then these are classified as listener/transport fields.
2. Given `allowRemoteAccess` and `allowedOrigins`, when reviewers inspect the package, then remote exposure intent is separated from Origin allowlist enforcement.
3. Given authorization and metadata fields, when reviewers inspect the package, then OAuth-specific metadata is not mixed with static token behavior.

### User Story 2 - MCP Authorization Semantics Stay Standards-Aligned (Priority: P1)

As an MCP client or security reviewer, I want HTTP authorization configuration to represent either no local authorization or standards-aligned OAuth resource-server validation, not a static token disguised as OAuth.

**Independent Test**: Start from YAML validation tests and HTTP authorization tests; prove static `accessToken` is removed or isolated as a non-OAuth compatibility mode with no OAuth metadata advertisement.

**Acceptance Scenarios**:

1. Given remote HTTP access is enabled, when local authorization is required, then the preferred mode is OAuth introspection with issuer, resource, time-window when available, and scope checks.
2. Given a deployment uses only local loopback HTTP, when authorization is disabled, then protected resource metadata is not emitted.
3. Given a static shared secret is retained only after a new explicit user approval, when reviewers inspect the behavior, then it is named as `static-token`, does not require `authorizationServers`, and does not publish OAuth protected resource metadata.

### User Story 3 - Remote HTTP Exposure Is Explicit and Safe (Priority: P1)

As an operator, I want remote HTTP exposure to require explicit configuration and reject unsafe Origin behavior so that MCP Streamable HTTP is not exposed accidentally.

**Independent Test**: Remote HTTP validation and E2E security tests cover loopback binding, non-loopback binding, allowed origins, missing origins, malformed origins, and unlisted origins.

**Acceptance Scenarios**:

1. Given loopback binding, when no Origin is supplied, then local non-browser clients continue to work.
2. Given loopback binding and a present invalid or non-loopback Origin, when the request is validated, then the request is rejected.
3. Given non-loopback binding, when `allowedOrigins` is empty or malformed, then configuration validation fails before startup.
4. Given non-loopback binding, when an incoming request has a present malformed, loopback-only, or unlisted Origin, then the request is rejected.
5. Given non-loopback binding and a missing Origin, when the implementation policy is chosen, then reviewers can tell whether rejection is a ShardingSphere hardening choice or whether strong OAuth authentication is allowed to cover non-browser clients.

### User Story 4 - Protected Resource Metadata Is Canonical (Priority: P2)

As an OAuth client integrator, I want protected resource metadata to expose a canonical `resource` URI and valid authorization server metadata only when OAuth is truly enabled.

**Independent Test**: Metadata servlet tests and OAuth validation tests prove `protectedResource`, `authorizationServers`, `scopesSupported`, and required scope behavior are correctly separated.

**Acceptance Scenarios**:

1. Given OAuth introspection is enabled, when metadata is requested, then `resource` is an HTTPS URL without fragment and matches the resource/audience validation target.
2. Given `protectedResource` is omitted, when a public base URI is unavailable, then the design requires an explicit production URI instead of silently advertising an internal address.
3. Given supported scopes and required scopes differ, when a token is validated, then validation uses required scopes while metadata advertises supported scopes.
4. Given insufficient scope is reported, when clients inspect the `WWW-Authenticate` challenge, then `scope` advertises the scopes needed for the failed request, not blindly every `scopes_supported` value.
5. Given MCP OAuth metadata is emitted, when clients inspect the metadata, then `authorization_servers` is non-empty and `bearer_methods_supported` advertises `header`.
6. Given a client receives `resource_metadata` in a challenge, when it follows the URI, then the same endpoint returns metadata for the canonical resource URI used for audience validation.

### User Story 5 - Compatibility and Migration Are Reviewable (Priority: P2)

As a downstream operator, I want any breaking configuration cleanup to include migration guidance and tests so that old YAML fails with actionable messages instead of ambiguous behavior.

**Independent Test**: YAML swapper/validator tests cover old `accessToken`, new authorization modes, and actionable validation errors.

**Acceptance Scenarios**:

1. Given an old config uses `transport.http.accessToken`, when the field is removed, then startup fails with a targeted message and migration guidance.
2. Given a config enables remote HTTP but no OAuth authorization, when validation runs, then the error explains the required secure alternative.
3. Given docs mention removed fields, when documentation contract tests run, then stale examples fail.

## Requirements

### Functional Requirements

- **MHC-FR-001**: This package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands.
- **MHC-FR-002**: This package MUST classify every current `HttpTransportConfiguration` field as required, optional, deprecated, removable, or merge candidate.
- **MHC-FR-003**: The future design MUST separate listener transport fields from remote exposure, Origin protection, authorization, and protected resource metadata.
- **MHC-FR-004**: `transport.http.enabled`, `bindHost`, `port`, and `endpointPath` MUST remain required for HTTP transport when HTTP is enabled.
- **MHC-FR-005**: Fields that are unused while HTTP is disabled SHOULD NOT remain globally required unless strict schema constraints require it and the reason is documented.
- **MHC-FR-006**: `allowRemoteAccess` MUST either be replaced by an explicit exposure mode or justified as an operator confirmation gate with no runtime behavior.
- **MHC-FR-007**: Non-loopback HTTP exposure MUST require a non-empty exact-origin allowlist.
- **MHC-FR-008**: `transport.http.accessToken` SHOULD be removed as the preferred outcome.
- **MHC-FR-009**: If a static token is retained, it MUST be explicitly modeled as non-OAuth `static-token` mode and MUST NOT publish OAuth protected resource metadata.
- **MHC-FR-010**: OAuth authorization MUST validate active status, issuer, resource/audience, required scopes, and time-window claims such as expiration or not-before when present.
- **MHC-FR-011**: `authorizationServers` MUST only represent OAuth authorization server metadata and MUST use valid HTTPS URIs without fragments.
- **MHC-FR-012**: `scopesSupported` MUST NOT double as both advertised supported scopes and required scopes unless the trade-off is documented and accepted.
- **MHC-FR-013**: OAuth protected resource metadata MUST validate `protectedResource` as an HTTPS URL without fragment; query components SHOULD be rejected unless a documented resource-identifier reason exists.
- **MHC-FR-014**: Production OAuth metadata MUST NOT silently advertise an internal loopback or reverse-proxy-hidden resource URI.
- **MHC-FR-015**: OAuth metadata MUST include `bearer_methods_supported: ["header"]` when bearer tokens are accepted only through the Authorization header.
- **MHC-FR-016**: MCP OAuth protected resource metadata MUST include non-empty `authorization_servers`; this package MUST NOT use RFC 9728's generic non-enumerable authorization-server exception for MCP-compliant OAuth mode.
- **MHC-FR-017**: Bearer failures MUST preserve RFC 6750-compatible `WWW-Authenticate` behavior, including `invalid_token`, `insufficient_scope`, `scope`, and RFC 9728 `resource_metadata` when applicable.
- **MHC-FR-018**: OAuth introspection MUST require HTTPS endpoints except explicitly documented loopback test fixtures, fail closed on introspection errors, redact client credentials, and define timeout behavior.
- **MHC-FR-019**: OAuth introspection cache behavior MUST define cache key dimensions and invalidation for token, issuer/resource/scope policy, expiration, and configuration changes.
- **MHC-FR-020**: Advertised authorization servers and accepted token issuers MUST have a documented invariant so metadata and validation policy cannot drift silently.
- **MHC-FR-021**: Reverse-proxy deployments MUST have an explicit public resource/base URI policy instead of relying only on loopback binding.
- **MHC-FR-022**: The implementation plan MUST include migration behavior for removed or renamed YAML fields.
- **MHC-FR-023**: Any implementation touching MCP or MCP E2E paths MUST include `mcp-builder` design/implementation review evidence with findings, classification, and evidence links.
- **MHC-FR-024**: Any non-trivial design decision MUST pass a doubt-driven review cycle before implementation starts.
- **MHC-FR-025**: Completion evidence MUST include commands, source links, contract snapshots, or review artifacts; prose-only closure is insufficient.
- **MHC-FR-026**: Missing Origin handling for non-loopback HTTP MUST be explicitly classified as either ShardingSphere hardening or an allowed non-browser-client compatibility case protected by OAuth; it MUST NOT be presented as a direct MCP requirement.
- **MHC-FR-027**: OAuth introspection validation MUST treat `active` as the mandatory authoritative response field, validate `exp`, `nbf`, and other time claims when present, and define fail-closed/cache behavior when `exp` is absent.
- **MHC-FR-028**: `WWW-Authenticate` scope challenges MUST use request-required scopes, not automatically mirror `scopesSupported`.
- **MHC-FR-029**: Protected resource metadata endpoint placement and `resource_metadata` challenge URIs MUST be tested as one contract so the advertised metadata URI is actually served.
- **MHC-FR-030**: `authorizationServers` MUST represent OAuth authorization server issuer identifiers and MUST remain consistent with accepted token issuer validation.
- **MHC-FR-031**: Origin protection and authorization behavior MUST be defined consistently for POST, GET, and DELETE requests on the MCP endpoint when HTTP authorization is enabled.

### Key Entities

- **HTTP Listener Configuration**: `enabled`, `bindHost`, `port`, and `endpointPath`.
- **Remote Exposure Policy**: The operator intent and binding mode that decide whether remote clients may connect.
- **Origin Protection Policy**: Exact HTTP/HTTPS origins accepted for Streamable HTTP requests.
- **Authorization Mode**: The selected local authorization behavior, preferably `none` for loopback-only local use or `oauth-introspection` for protected HTTP.
- **Protected Resource Metadata**: OAuth metadata exposed through `/.well-known/oauth-protected-resource`.
- **Required Scope Policy**: Scope requirements enforced during token validation.
- **Migration Contract**: Validation and documentation behavior for removed or renamed fields.
- **Public Resource URI Policy**: The rule for externally visible HTTPS resource identifiers, including reverse-proxy deployments.

## Success Criteria

- **MHC-SC-001**: Reviewers can map each current HTTP field to exactly one responsibility and one future action.
- **MHC-SC-002**: The preferred implementation path removes `transport.http.accessToken` or documents a narrow retained exception that does not claim OAuth compliance.
- **MHC-SC-003**: OAuth metadata is emitted only for OAuth-backed authorization and uses canonical resource metadata.
- **MHC-SC-004**: Remote HTTP configuration cannot start without explicit exposure intent, exact Origin allowlist, and a standards-aligned authorization plan.
- **MHC-SC-005**: Tests and documentation cover migration from old YAML fields to the new configuration model.
- **MHC-SC-006**: `mcp-builder`, source-driven, and doubt-driven evidence are recorded before implementation is considered ready.
- **MHC-SC-007**: OAuth failure responses, metadata, and introspection behavior are traceable to MCP Authorization, RFC 6750, RFC 7662, RFC 8707, and RFC 9728.
- **MHC-SC-008**: Reanalysis decisions for missing Origin, absent token expiration, scope challenge policy, metadata URL placement, and method coverage are recorded before implementation starts.

## Assumptions

- The current repository targets MCP protocol `2025-11-25` while still supporting `2025-06-18` for compatibility.
- `mcp-java-sdk.version` is `1.1.2` in `mcp/bootstrap/pom.xml`.
- Loopback-only local HTTP can remain unauthenticated if Origin protection and binding constraints are preserved.
- Remote HTTP should be treated as a production security boundary.

## Out of Scope

- Implementing code changes before the user gives an explicit implementation command.
- Building a full OAuth authorization server or dynamic client registration flow.
- Changing STDIO transport behavior.
- Changing business tool semantics, descriptor names, or encrypt/mask workflows unless required by the HTTP configuration cleanup.
