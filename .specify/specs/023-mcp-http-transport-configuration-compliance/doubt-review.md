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
- **Static token retention too broad**: Valid and actionable. Removed production retention as an accepted reason; only temporary test-fixture migration coverage remains unless the user explicitly approves production retention.
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

- Pre-implementation decisions must still resolve the open questions in `tasks.md`.
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
- **`accessToken` deletion phrased symmetrically**: Valid and actionable. Reworded open decisions so deletion is the default and retention requires new explicit user approval.

## Round 3 Closure Review

**Review command**: `codex exec --ephemeral --sandbox read-only -C <repo-root> - <<'EOF' ... EOF`  
**Exit code**: 0  
**Mode**: Read-only Codex CLI adversarial review  
**Date**: 2026-05-17

### Round 3 Result

- **Blocking findings**: None.
- **Non-blocking temp-path nit**: Valid and actionable. Replaced the historical absolute review prompt path with a portable placeholder.
