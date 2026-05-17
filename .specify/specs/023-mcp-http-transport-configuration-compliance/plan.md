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

# Implementation Plan: MCP HTTP Transport Configuration Compliance

**Branch**: `001-shardingsphere-mcp`  
**Date**: 2026-05-17  
**Spec**: `.specify/specs/023-mcp-http-transport-configuration-compliance/spec.md`  
**Input**: Field-level review of `HttpTransportConfiguration` and user direction to create a new Speckit package without switching branches.
**Note**: This plan is documentation-only until the user gives an explicit implementation command.

## Summary

Reduce and clarify MCP HTTP transport configuration.
The primary design move is to separate HTTP listener configuration from security and OAuth metadata configuration, and to remove static `accessToken` unless a narrow non-OAuth compatibility case is explicitly accepted.

## Technical Context

**Language/Version**: Java 21 for MCP bootstrap and E2E tests.  
**Primary Dependencies**: MCP Java SDK `1.1.2`, embedded Tomcat, Jackson, JUnit 5, Mockito.  
**Storage**: YAML runtime configuration only; no database storage change.  
**Testing**: `mcp/bootstrap` unit tests, HTTP transport tests, `test/e2e/mcp` remote HTTP/OAuth security tests, documentation contract tests.  
**Target Platform**: ShardingSphere MCP standalone runtime over Streamable HTTP and STDIO.  
**Project Type**: Java backend runtime configuration and HTTP transport hardening.  
**Performance Goals**: No measurable runtime overhead beyond existing Origin/token checks; token validation cache behavior must remain bounded if OAuth introspection remains enabled.  
**Constraints**: No branch switching, no production code before user command, no generated `target/` edits, no broad runtime rewrite.  
**Scale/Scope**: `mcp/bootstrap`, `distribution/mcp`, `mcp/README.md`, `mcp/README_ZH.md`, and `test/e2e/mcp` once implementation is authorized.

## Source-Driven Framework Basis

- MCP Streamable HTTP requires a single HTTP endpoint and recommends Origin validation for local servers.
- MCP Authorization treats MCP servers as OAuth resource servers when authorization is implemented; access tokens must be validated for the protected resource.
- OAuth Protected Resource Metadata exposes resource identity and authorization server metadata; these fields should not be emitted for a non-OAuth static shared secret.
- `mcp-builder` best practices emphasize service-prefixed tools, concise schemas, pagination, actionable errors, Origin protection, and matching annotations to actual behavior.
- OAuth introspection, bearer challenges, and resource indicators are also grounded in RFC 7662, RFC 6750, and RFC 8707.

## Constitution Check

*GATE: Must pass before implementation. Re-check after design decisions and before code changes.*

- **Proxy-first logical abstraction**: Pass. This package changes MCP transport configuration only and does not alter database workflow behavior.
- **Explicit operator control**: Pass. Remote exposure and authorization become more explicit, not more implicit.
- **Minimal safe automation**: Pass. No data migration, backfill, rollback, or schema/rule side effect is introduced.
- **Deterministic naming and transparent changes**: Pass. Removed or renamed config fields require migration evidence.
- **Complete verification before completion**: Pass with requirement. Field changes require unit, E2E, docs, and review evidence.
- **Repository rules**: Pass. `AGENTS.md` and `CODE_OF_CONDUCT.md` remain binding.

## Current Field Decision Matrix

- `enabled`: keep. Required to choose HTTP vs STDIO.
- `bindHost`: keep. Required listener binding and loopback/remote security input.
- `allowRemoteAccess`: merge candidate. Replace with `exposure: local|remote` or justify as explicit confirmation.
- `accessToken`: preferred delete. If retained, rename/model as non-OAuth `static-token` mode with no OAuth metadata.
- `port`: keep. Required listener binding.
- `endpointPath`: keep. Required Streamable HTTP endpoint and metadata path input.
- `allowedOrigins`: keep but move under Origin/exposure policy. Required for non-loopback exposure.
- `authorizationServers`: keep only for OAuth metadata. Remove static-token coupling.
- `scopesSupported`: split candidate. Keep for metadata, add separate required scopes if token validation needs it.
- `protectedResource`: keep but validate. Require HTTPS URL without fragment for OAuth metadata, reject query components unless justified, and avoid internal URI leakage.
- `oauthIntrospection`: keep for standards-aligned OAuth resource-server validation.

## Proposed Configuration Shape

```yaml
transport:
  http:
    enabled: true
    listener:
      bindHost: 127.0.0.1
      port: 18088
      endpointPath: /mcp
    exposure:
      mode: local
      allowedOrigins: []
    authorization:
      mode: none
      oauthIntrospection:
        endpoint: ""
        clientId: ""
        clientSecret: ""
        expectedIssuer: ""
        cacheTtlMillis: 0
      requiredScopes: []
    protectedResource:
      uri: ""
      publicBaseUri: ""
      authorizationServers:
        - https://auth.example.test
      scopesSupported: []
      bearerMethodsSupported:
        - header
```

This shape is a design target, not an approved code contract yet.
Backward compatibility and YAML migration behavior must be decided before implementation.
For OAuth mode, `protectedResource.uri` is the canonical HTTPS resource identifier. `publicBaseUri` is only a migration/design placeholder for reverse-proxy deployments and should collapse into a single approved public resource URI before code is written.

## Implementation Phases

### Phase 1 - Design Baseline

1. Run and reconcile doubt-driven review before finalizing design decisions.
2. Confirm the default deletion path for static `accessToken`; retention requires a new explicit user approval and a renamed non-OAuth compatibility mode.
3. Decide whether `allowRemoteAccess` becomes `exposure.mode` or remains a confirmation field.
4. Decide migration behavior for old flat YAML fields.
5. Record official source map and mcp-builder review notes.

### Phase 2 - Validation Model

1. Refactor YAML validation around listener, exposure, authorization, and metadata groups.
2. Add branch-specific validation for loopback local use, remote OAuth use, and invalid legacy fields.
3. Validate OAuth `protectedResource` as an HTTPS URL without fragment and reject query components unless justified.
4. Split `scopesSupported` from required scopes if OAuth validation enforces scope requirements.
5. Define `authorizationServers` and accepted issuer invariants.
6. Define introspection HTTPS, client authentication, timeout, fail-closed, credential redaction, and cache-key behavior.

### Phase 3 - Runtime Wiring

1. Keep Tomcat binding behavior equivalent for listener fields.
2. Keep Origin validation equivalent or stricter for remote access.
3. Remove static token authorization path if deletion is selected.
4. Ensure protected resource metadata is registered only when OAuth metadata is valid.
5. Preserve RFC 6750-compatible `WWW-Authenticate` challenges and RFC 9728 `resource_metadata` behavior.

### Phase 4 - Tests And E2E

1. Update YAML swapper and validator tests for every validation branch.
2. Update authorization handler, OAuth validator, and metadata servlet tests.
3. Update remote HTTP E2E security coverage for Origin, token/OAuth, session, protocol, and DELETE paths.
4. Update distribution examples and README contract tests.

### Phase 5 - Review And Handoff

1. Run scoped Maven tests for touched modules.
2. Run scoped Checkstyle and Spotless gates.
3. Run `mcp-builder` design/implementation reasonableness review.
4. Run doubt-driven review and classify every finding.
5. Confirm no unresolved user questions remain before final handoff.

## Verification Strategy

- Documentation-only package creation: inspect files and run no Maven tests.
- Config model changes: run `mcp/bootstrap` config, authorization, and HTTP transport tests.
- HTTP behavior changes: run focused `test/e2e/mcp` programmatic HTTP security tests.
- Distribution/doc changes: run documentation contract tests and package config smoke tests.
- mcp-builder review evidence: record checklist results for transport behavior, actionable errors, security testing, client ergonomics, and evaluation impact.
- Final implementation: run scoped Checkstyle/Spotless and record command exit codes.

## Risk Register

- **Breaking YAML compatibility**: Removing `accessToken` or reshaping fields breaks existing examples. Mitigate with explicit validation messages and migration docs.
- **OAuth over-modeling**: Adding too many OAuth knobs can make local MCP hard to start. Mitigate by keeping loopback local mode simple.
- **Reverse proxy URI leakage**: Auto-derived metadata may advertise internal URLs. Mitigate with explicit `protectedResource.uri` for production OAuth deployments.
- **Scope ambiguity**: One field currently means both supported and required scopes. Mitigate by splitting or documenting a conscious trade-off.
- **Static-token convenience pressure**: Retaining static token may preserve an attractive insecure pattern. Mitigate by deleting it unless a narrow exception is approved.
- **Challenge drift**: Metadata may be correct while `WWW-Authenticate` points clients to stale metadata. Mitigate with RFC 6750 and RFC 9728 challenge tests.
- **Issuer drift**: Advertised authorization servers and accepted token issuers can diverge. Mitigate with a documented invariant and tests.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None yet | Not applicable | Not applicable |
