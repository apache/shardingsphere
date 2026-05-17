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

Reduce and clarify MCP HTTP transport configuration without adding built-in HTTP authorization.
The primary design move is to replace dual transport booleans with a single `transport.type` selector and keep the HTTP subtree limited to listener inputs.

MCP Authorization is optional, so this slice does not add OAuth, token introspection, protected resource metadata, bearer challenge handling, or scope configuration.
Current authorization-related flat fields are treated as unsupported or migration-only, not as inputs to a new OAuth model.
Remote mode and Origin allowlist configuration are also removed from the public YAML API.
MCP-required Origin handling remains an internal Streamable HTTP runtime responsibility.

## Technical Context

**Language/Version**: Java 21 for MCP bootstrap and E2E tests.
**Primary Dependencies**: MCP Java SDK `1.1.2`, embedded Tomcat, Jackson, JUnit 5, Mockito.
**Storage**: YAML runtime configuration only; no database storage change.
**Testing**: `mcp/bootstrap` unit tests, HTTP transport tests, `test/e2e/mcp` runtime/security/distribution tests, mcp-builder LLM evaluation artifact tests, and documentation contract tests.
**Target Platform**: ShardingSphere MCP standalone runtime over Streamable HTTP and STDIO.
**Project Type**: Java backend runtime configuration and HTTP transport hardening.
**Performance Goals**: No measurable runtime overhead beyond listener defaulting and Origin checks.
**Constraints**: No branch switching, no production code before user command, no generated `target/` edits, no broad runtime rewrite.
**Scale/Scope**: `mcp/bootstrap`, `distribution/mcp`, `mcp/README.md`, `mcp/README_ZH.md`, and `test/e2e/mcp` once implementation is authorized.

## Source-Driven Framework Basis

- MCP Streamable HTTP requires a single HTTP endpoint that supports POST and GET.
- MCP Streamable HTTP requires servers to validate the `Origin` header on incoming connections and return HTTP 403 when a present Origin is invalid.
- MCP Streamable HTTP recommends localhost binding for local servers and proper authentication for all connections.
- MCP Authorization explicitly says authorization is optional for MCP implementations.
- Therefore this package may remain MCP-compliant without adding OAuth, as long as it does not pretend to implement MCP Authorization and still keeps MCP-required HTTP runtime protections.

## Constitution Check

*GATE: Must pass before implementation. Re-check after design decisions and before code changes.*

- **Proxy-first logical abstraction**: Pass. This package changes MCP transport configuration only and does not alter database workflow behavior.
- **Explicit operator control**: Pass. Non-loopback exposure is expressed only by `http.bindHost`; no duplicate remote switch is added.
- **Minimal safe automation**: Pass. No data migration, backfill, rollback, or schema/rule side effect is introduced.
- **Deterministic naming and transparent changes**: Pass. Removed, renamed, or deferred config fields require migration evidence.
- **Complete verification before completion**: Pass with requirement. Field changes require unit, E2E, docs, and review evidence.
- **Repository rules**: Pass. `AGENTS.md` and `CODE_OF_CONDUCT.md` remain binding.

## Current Field Decision Matrix

- `transport.http.enabled`: remove. Replaced by `transport.type`.
- `transport.stdio.enabled`: remove. Replaced by `transport.type`.
- `bindHost`: keep as optional HTTP listener binding. Default is `127.0.0.1`.
- `bindHost` valid values: local socket bind host/address only, such as `127.0.0.1`, `0.0.0.0`, or a concrete local interface IP. It is not a URL, Origin, or exposure mode.
- `allowRemoteAccess`: remove without replacement. `bindHost` is the only exposure expression.
- `accessToken`: remove or reject as unsupported. Static shared-secret HTTP authorization is not retained.
- `port`: keep as optional HTTP listener port. Default is `18088`.
- `port` valid range: HTTP listener port, with `0` allowed only for explicit ephemeral-port test or embedded launches that read the real runtime port after startup.
- `endpointPath`: keep as optional Streamable HTTP endpoint path. Default is `/mcp`.
- `endpointPath` valid shape: one absolute endpoint path such as `/mcp` or `/api/mcp`; no `//` prefix, scheme, host, query, or fragment.
- `allowedOrigins`: remove from YAML. Origin handling is MCP-required runtime behavior, not a user policy field in this slice.
- `authorizationServers`: out of current scope. Do not add OAuth metadata support in this package.
- `scopesSupported`: out of current scope. Do not add OAuth scope metadata in this package.
- `protectedResource`: out of current scope. Do not add OAuth protected resource metadata in this package.
- `oauthIntrospection`: out of current scope. Do not add OAuth token introspection in this package.
- `bearerMethodsSupported`: out of current scope. Do not add bearer metadata in this package.
- `expectedIssuer`: out of current scope. Do not add issuer validation in this package.
- `authorization`: not an approved target field for this package.
- `tokenValidation`: not an approved target field for this package.

## Proposed Configuration Shape

Minimal local HTTP:

```yaml
transport:
  type: STREAMABLE_HTTP
```

Equivalent local HTTP with defaults made explicit:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
```

Remote HTTP without built-in authorization:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 0.0.0.0
```

STDIO:

```yaml
transport:
  type: STDIO
```

Invalid STDIO shape:

```yaml
transport:
  type: STDIO
  http:
    port: 18088
```

`transport.http` is valid only when `transport.type` is `STREAMABLE_HTTP`.

This shape is the approved design target for the next implementation slice, but code changes still require an explicit user command.
No `authorization`, `oauth`, `tokenValidation`, `introspection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `requiredScopes`, or `bearerMethodsSupported` fields are added in this slice.
No `authorization: none` placeholder is added; absence of authorization fields is the no-built-in-authorization contract.
No `remote`, `exposure`, `allowRemoteAccess`, or `allowedOrigins` field is added.
When `transport.type` is `STREAMABLE_HTTP`, missing `transport.http` defaults to `bindHost: 127.0.0.1`, `port: 18088`, and `endpointPath: /mcp`.
When `transport.type` is `STDIO`, `transport.http` must be absent rather than present and ignored.
`STREAMABLE_HTTP` and `STDIO` are ShardingSphere YAML enum values that map to MCP standard transports; MCP does not require these exact YAML literals.
Remote HTTP does not provide built-in identity enforcement in this slice; operators that need identity checks must put the MCP server behind a trusted network boundary or external reverse proxy authorization.
Legacy authorization-related fields fail with targeted migration guidance rather than being silently bridged.
Known removed fields and unknown fields under `transport` or `transport.http` fail loudly instead of being silently ignored.

## Implementation Phases

### Phase 1 - Design Baseline

1. Run and reconcile doubt-driven review before finalizing design decisions.
2. Remove static-token production authorization from the current design.
3. Replace `transport.http.enabled` and `transport.stdio.enabled` with `transport.type`.
4. Delete `allowRemoteAccess`, `remote`, `exposure`, and `allowedOrigins` from the YAML API.
5. Do not add OAuth, token validation, protected resource metadata, or scope fields in this slice.
6. Use targeted validation errors and migration docs for removed, renamed, or deferred YAML fields.
7. Record official source map and `mcp-builder` future-gate notes.

### Phase 2 - Validation Model

1. Refactor YAML validation around `transport.type` and HTTP listener fields only.
2. Make `transport.http` optional when `transport.type` is `STREAMABLE_HTTP`.
3. Default missing `transport.http.bindHost`, `transport.http.port`, and `transport.http.endpointPath` to `127.0.0.1`, `18088`, and `/mcp`.
4. Reject `transport.http` when `transport.type` is `STDIO`.
5. Validate `bindHost` as a bind host/address, `port` as a listener port, and `endpointPath` as one absolute path with no query or fragment.
6. Reject old `enabled`, `allowRemoteAccess`, `remote`, `exposure`, `allowedOrigins`, `accessToken`, `oauthIntrospection`, `protectedResource`, `authorizationServers`, and `scopesSupported` fields as unsupported or migration-only in this slice.
7. Reject unknown fields under `transport` or `transport.http` rather than silently ignoring them.

### Phase 3 - Runtime Wiring

1. Keep Tomcat binding behavior equivalent for listener fields.
2. Keep MCP-required Origin handling internal to Streamable HTTP runtime and outside the YAML API.
3. Remove static token authorization behavior from this slice, or fail old static-token config before runtime if implementation is authorized.
4. Do not register OAuth protected resource metadata endpoints in this slice.
5. Apply MCP-required HTTP safeguards consistently to POST, GET, and DELETE on the MCP endpoint.
6. Use the current-scope Origin matrix: missing Origin is accepted for non-browser clients; malformed, `null`, or non-loopback present Origin returns HTTP 403; loopback Origin is accepted only for loopback-bound local HTTP.

### Phase 4 - Tests And E2E

1. Update YAML swapper and validator tests for every transport selector and HTTP listener default/override validation branch.
2. Update HTTP transport tests for listener defaulting, endpoint path, and internal Origin behavior.
3. Delete `HttpTransportAccessTokenE2ETest` and `HttpTransportOAuthIntrospectionE2ETest` because their success paths require static token, OAuth introspection, protected resource metadata, bearer challenge, and scope behavior that this slice removes.
4. Simplify `HttpTransportSecurityE2ETest` to cover loopback/default HTTP, non-loopback `bindHost`, and the internal Origin matrix without `Authorization`, access token, authorization server, `allowedOrigins`, or OAuth introspection fixtures.
5. Add or update E2E coverage for a valid GET request to the same Streamable HTTP endpoint with `Accept: text/event-stream`; assert the documented behavior, either SSE response or HTTP 405.
6. Update shared E2E runtime and distribution helpers so generated HTTP and STDIO configs use `transport.type` and no longer construct `OAuthIntrospectionConfiguration` as default fixture state.
7. Update distribution examples, packaged distribution tests, and README contract tests.
8. Replace the mcp-builder LLM evaluation authorization question and validator constants with a transport-security/configuration scenario aligned with the unauthenticated transport slice.
9. Preserve external LLM provider `Authorization: Bearer <api key>` handling and secret-redaction tests, and preserve SQL `metadata_introspection_sql` workflow tests.
10. Add stale-field searches for old boolean, remote, Origin, and deferred authorization-related YAML fields.

### Phase 5 - Review And Handoff

1. Run scoped Maven tests for touched modules.
2. Run scoped Checkstyle and Spotless gates.
3. Run `mcp-builder` design/implementation reasonableness review.
4. Run doubt-driven review and classify every finding.
5. Confirm no unresolved user questions remain before final handoff.

## Verification Strategy

- Documentation-only package creation: inspect files and run no Maven tests.
- Config model changes: run `mcp/bootstrap` config and HTTP transport tests.
- HTTP behavior changes: run focused `test/e2e/mcp` programmatic HTTP security tests.
- Distribution/doc changes: run documentation contract tests and package config smoke tests.
- mcp-builder review evidence: record checklist results for transport behavior, actionable errors, security testing, client ergonomics, and evaluation impact.
- Final implementation: run scoped Checkstyle/Spotless and record command exit codes.

## Risk Register

- **Breaking YAML compatibility**: Removing or rejecting transport booleans, remote fields, Origin fields, `accessToken`, and OAuth-related fields breaks existing examples.
  Mitigate with explicit validation messages and migration docs.
- **Remote HTTP without identity checks**: This slice can expose MCP over the network without built-in authorization.
  Mitigate with localhost default, explicit non-loopback `bindHost`, internal MCP Origin handling, and docs requiring an external trust boundary when identity enforcement is needed.
- **OAuth over-modeling**: Adding OAuth knobs now would make local MCP hard to start and exceed the user's current product direction.
  Mitigate by deferring OAuth to a separate future Speckit.
- **Static-token convenience pressure**: Retaining static token may preserve an attractive insecure pattern.
  Mitigate by removing or rejecting it rather than replacing it with OAuth in this slice.
- **Origin policy gaps**: Missing method coverage could leave GET or DELETE behavior inconsistent even though Origin policy is no longer configurable.
  Mitigate with POST, GET, and DELETE tests.
- **E2E stale-auth gaps**: Removing only the obvious access-token E2E classes would leave OAuth expectations in fixtures, documentation tests, distribution tests, or mcp-builder evaluation artifacts.
  Mitigate with explicit path-level cleanup tasks and reverse searches that distinguish MCP HTTP auth from unrelated LLM provider auth or SQL metadata introspection.
- **STDIO stale HTTP subtree**: Leaving `transport.http` accepted under `STDIO` would make old HTTP settings appear meaningful in a non-HTTP launch.
  Mitigate by rejecting the subtree when `transport.type` is `STDIO`.
- **Loose endpoint syntax**: Accepting endpoint paths with query, fragment, URL syntax, or double leading slashes can produce ambiguous servlet mappings.
  Mitigate with explicit endpoint-path validation and tests.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None yet | Not applicable | Not applicable |
