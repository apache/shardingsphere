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

# Doubt Review: MCP HTTP Transport Configuration Compliance

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - < <doubt-review-prompt>`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial review
**Date**: 2026-05-17

## Finding Classifications

- **Doubt review placed too late**: Valid and actionable. Moved initial doubt review into the design baseline and kept a final implementation review gate.
- **Static token design too broad**: Valid and actionable. User-confirmed decisions now delete static `accessToken` entirely; only migration-failure documentation remains.
- **OAuth source grounding incomplete**: Valid and actionable. Added RFC 6750, RFC 7662, and RFC 8707 to source inventory and requirements.
- **Protected resource validation too weak**: Valid and actionable. Strengthened OAuth resource identifier requirement to HTTPS URL without fragment, with query rejected unless justified.
- **Authorization servers not required when metadata emitted**: Valid and actionable. Added MCP-mandatory non-empty `authorization_servers` requirement for OAuth protected resource metadata.
- **WWW-Authenticate challenge missing**: Valid and actionable. Added RFC 6750 and RFC 9728 challenge requirements and test tasks.
- **`bearer_methods_supported` missing**: Valid and actionable. Added metadata requirement and servlet test task.
- **Loopback Origin policy incomplete**: Valid and actionable. Added present invalid/non-loopback Origin rejection for loopback binding while keeping missing Origin valid for local non-browser clients.
- **Reverse-proxy exposure under-modeled**: Valid and actionable. Added public resource URI policy and risk.
- **OAuth introspection security underspecified**: Valid and actionable. Added HTTPS, timeout, fail-closed, credential redaction, client-auth, and cache-key decision tasks.
- **Token cache coupling unaddressed**: Valid and actionable. Added cache key and invalidation requirements.
- **Authorization server and expected issuer drift**: Valid and actionable. Added invariant requirement.
- **Metadata endpoint placement missing**: Valid and actionable. Added RFC 9728 endpoint-placement task.
- **mcp-builder gate too vague**: Valid and actionable. Added evidence format for mcp-builder findings, classification, and evidence.
- **Hidden package 014 coupling**: Valid and actionable. Removed reliance on package 014 for response-format reasoning and scoped it out explicitly.
- **Local absolute paths in source evidence**: Valid and actionable. Replaced machine-local skill paths with portable session evidence wording.
- **Governance checkbox overclaim**: Valid and actionable. This review file records the command and exit code for the completed doubt review.
- **Dedicated validator coverage missing**: Valid and actionable. Added validator test path and future task.

## Remaining Review Gates

- Pre-implementation decisions in `tasks.md` are resolved for this design package.
- Any future implementation touching MCP runtime or E2E code must run mcp-builder review and a final doubt-driven review.

## Round 2 Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial review
**Date**: 2026-05-17

### Round 2 Finding Classifications

- **MCP `authorization_servers` non-enumerable exception**: Valid and actionable. Removed the exception and required non-empty `authorization_servers` for MCP OAuth protected resource metadata.
- **Dirty worktree disclosure**: Valid and actionable for handoff. Existing modified files under `mcp/**` and `test/e2e/mcp/**` are outside this documentation-only package and must be disclosed in the final response.
- **Validator paths inaccurate**: Valid and actionable. Corrected production path to `config/yaml/validator/HttpTransportConfigurationValidator.java` and moved the missing validator test to a candidate new test path.
- **`accessToken` deletion phrased symmetrically**: Valid and actionable. Reworded the decision path; later user-confirmed decisions delete static `accessToken` entirely.

## Round 3 Closure Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial review
**Date**: 2026-05-17

### Round 3 Result

- **Blocking findings**: None.
- **Non-blocking temp-path nit**: Valid and actionable. Replaced the historical absolute review prompt path with a portable placeholder.

## Reanalysis Review: Origin, Introspection, Scope, Metadata, and Method Coverage

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial review
**Date**: 2026-05-17

### Reanalysis Finding Classifications

- **`mcp-builder` treated as current source evidence**: Valid and actionable. Reworded source-map, research, tasks, checklist, and plan so `mcp-builder` is a future implementation gate, not official source evidence for this documentation-only reanalysis.
- **Missing-Origin requirement overclaim**: Valid and actionable. Clarified that MCP requires rejecting invalid present Origin values, while rejecting a missing Origin is a ShardingSphere hardening or compatibility decision.
- **Introspection expiration overclaim**: Valid and actionable. Clarified that RFC 7662 requires `active`, while `exp` is optional; future implementation must define active-without-expiration fail-closed and cache behavior.
- **Scope challenge coupling**: Valid and actionable. Split request-required `WWW-Authenticate` scope challenges from protected resource `scopes_supported` metadata.
- **Metadata discovery contract incomplete**: Valid and actionable. Added requirements and tasks to test `resource_metadata` challenge URI, metadata endpoint registration, and protected resource identity together.
- **HTTP method coverage incomplete**: Valid and actionable. Added explicit POST, GET, and DELETE Origin/authorization policy coverage before implementation.

## Reanalysis Closure Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial review
**Date**: 2026-05-17

### Reanalysis Closure Result

- **Blocking findings**: None.
- **Non-blocking source-map clarity nit**: Valid and actionable. Moved the `mcp-builder` future gate out of Official Sources into a separate Future Review Gates section.
- **Non-blocking checklist granularity nit**: Valid and actionable. Split metadata URL registration from POST, GET, and DELETE method security coverage in the open-decision checklist.
- **Non-blocking issuer checklist nit**: Valid and actionable. Added an explicit open decision for `authorizationServers` issuer identifiers and accepted token issuer invariants.

## User-Confirmed Decision Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial review
**Date**: 2026-05-17

### User-Confirmed Finding Classifications

- **`allowRemoteAccess` still allowed as confirmation gate**: Valid and actionable. Removed the remaining alternative wording and made `exposure.mode` the only design target.
- **`protectedResource.uri` not consistently mandatory**: Valid and actionable. Updated the spec so production OAuth without `protectedResource.uri` fails validation and reverse-proxy deployments use it as the public resource URI.
- **Endpoint-scoped metadata not locked into spec**: Valid and actionable. Updated the protected resource metadata entity to require endpoint-scoped well-known metadata with root well-known support retained.
- **Insufficient-scope status missing**: Valid and actionable. Updated bearer failure requirements to require HTTP 403 with `insufficient_scope` and `scope` challenge behavior.
- **Open-question wording stale**: Valid and actionable. Updated remaining review gate wording to state pre-implementation decisions are resolved for this design package.

## User-Confirmed Decision Closure Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial closure review
**Date**: 2026-05-17

### User-Confirmed Closure Result

- **Blocking findings**: None.
- **Scope**: Rechecked `allowRemoteAccess` replacement, mandatory `protectedResource.uri`, endpoint-scoped protected resource metadata, HTTP 403 `insufficient_scope`, and stale open-question wording.
- **Reviewer conclusion**: No blocking issues remain.

## Scope Semantics Closure Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`
**Exit code**: 0
**Mode**: Read-only Codex CLI adversarial closure review
**Date**: 2026-05-17

### Scope Semantics Closure Result

- **Blocking findings**: None.
- **Scope**: Rechecked that `scopesSupported` is described as server-configured protected-resource scope metadata and challenge guidance, not a custom MCP protocol field or a fixed MCP standard scope-name enum.
- **Reviewer conclusion**: No blocking issues remain.
