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
**Input**: User requested a new Speckit package for `HttpTransportConfiguration` field purpose, necessity, uselessness, optimization, merge opportunities, and MCP compliance issues.
User explicitly forbids branch switching and asks not to change production code before a later command.

## Goal

Make MCP transport configuration smaller, clearer, and standards-aligned without adding HTTP authorization or user-configurable validation strategy in this slice.
The package defines a minimal YAML API that selects exactly one MCP transport and only exposes the HTTP values needed to start a Streamable HTTP endpoint.

MCP Authorization is optional for MCP implementations.
Because ShardingSphere is an open-source project and the current scope is HTTP transport cleanup, this package does not add OAuth, token introspection, protected resource metadata, or scope enforcement configuration.
Any future OAuth support must be handled by a separate Speckit package and reviewed against the MCP Authorization specification at that time.

Approved YAML shape:

```yaml
transport:
  type: STREAMABLE_HTTP
```

Equivalent local HTTP shape with defaults made explicit:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 127.0.0.1
    port: 18088
    endpointPath: /mcp
```

Remote HTTP shape with only the binding overridden:

```yaml
transport:
  type: STREAMABLE_HTTP
  http:
    bindHost: 0.0.0.0
```

STDIO shape:

```yaml
transport:
  type: STDIO
```

## Clarifications

### Session 2026-05-17

- User confirmed design documents may be written now, but production code must wait for an explicit later command.
- User confirmed this must be a completely new Speckit package rather than an addition to package 006 or 016.
- User confirmed `mcp-builder` is available as a skill and should be used for design/implementation reasonableness checks when MCP or MCP E2E implementation changes are planned.
- User allowed doubt-driven review using Codex CLI.
- User requested reanalysis of any worthwhile design questions before implementation; this round remains documentation-only.
- User requested a stricter standards recheck after concern that prior proposals changed too cheaply.
- Earlier reanalysis rejected `exposure.mode`; later reanalysis also rejected `exposure.remote.enabled` because any remote-mode field duplicates `bindHost`.
- User clarified that MCP HTTP Authorization is optional and should not be added now.
  The current target therefore removes `authorization`, `tokenValidation`, `introspection`, `protectedResource`, `authorizationServers`, and `scopesSupported` from the approved YAML shape.
- Static `transport.http.accessToken` is not retained as a production authorization mode.
  If current implementation cleanup is later authorized, this field should be removed or rejected as unsupported rather than replaced with OAuth in this slice.
- User accepted deleting `remote.enabled` because `bindHost` already expresses local versus non-loopback binding.
- User accepted keeping `bindHost` as an optional HTTP field with default `127.0.0.1`.
- User accepted defaulting `port` and `endpointPath`.
  The final Streamable HTTP defaults are `bindHost: 127.0.0.1`, `port: 18088`, and `endpointPath: /mcp`.
- User asked to make the API stable and minimal before output.
  The final target therefore uses `transport.type` instead of `transport.http.enabled` and `transport.stdio.enabled`, and removes `allowRemoteAccess`, `remote`, `exposure`, and `allowedOrigins` from the YAML API.
- User requested reanalysis of E2E simplification.
  The previous E2E analysis was not complete enough because it named the HTTP access-token and OAuth introspection E2E classes but did not fully cover E2E fixtures, distribution contract tests, documentation contract tests, or the mcp-builder LLM evaluation artifact.
  Current E2E cleanup must remove current-scope MCP HTTP authorization expectations while preserving unrelated external LLM provider authorization redaction and SQL metadata introspection tests.
- Later configuration-boundary reanalysis found one remaining invalid-state risk: `transport.type: STDIO` plus a populated `transport.http` subtree.
  The approved shape now treats `transport.http` as valid only when `transport.type` is `STREAMABLE_HTTP`; `STDIO` configs must not carry HTTP listener fields.
- `bindHost` is a local socket bind address, not a public URL, Origin, or remote-mode flag.
  Supported examples are `127.0.0.1` for local loopback, `0.0.0.0` for all IPv4 interfaces, and a concrete local interface IP for one interface.
- `port` remains an HTTP listener port override.
  The default is `18088`; `0` is allowed only as an explicit ephemeral-port binding for tests or embedded launches where the actual local port is read from runtime startup state.
- `endpointPath` is the single Streamable HTTP MCP endpoint path.
  It must be a non-blank absolute path such as `/mcp` or `/api/mcp`, must start with one `/`, and must not contain a scheme, host, query, or fragment.
- The approved transport YAML is strict.
  Known removed fields and unknown fields under `transport` or `transport.http` must fail with targeted migration guidance instead of being silently ignored.

## Hard Constraints

- Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Do not modify production, test, distribution, or E2E code before the user gives an explicit implementation command.
- Preserve existing worktree changes and never revert unrelated edits.
- Use source-driven decisions for MCP behavior; official MCP sources outrank convenience patterns.
- Use `mcp-builder` review gates for any future change touching `mcp/**`, `distribution/mcp/**`, or `test/e2e/mcp/**`.
- Use doubt-driven review for non-trivial design and implementation decisions.
- Run at least one doubt-driven review before pre-implementation decisions are finalized, and run a second review after any implementation slice that changes MCP runtime or E2E behavior.

## User Scenarios and Testing

### User Story 1 - Maintainers Can Classify Every HTTP Field (Priority: P1)

As an MCP transport maintainer, I want the launch YAML to choose exactly one MCP transport so that the API cannot express contradictory states such as HTTP and STDIO both enabled.

**Independent Test**: Read the configuration docs, validation tests, and source map; verify every current `HttpTransportConfiguration` field has a mapped outcome and no field has duplicate ownership.

**Acceptance Scenarios**:

1. Given `transport.type: STREAMABLE_HTTP`, when reviewers inspect the package, then HTTP transport is selected without any `http.enabled` boolean.
2. Given `transport.type: STDIO`, when reviewers inspect the package, then STDIO transport is selected without any `stdio.enabled` boolean.
3. Given `transport.type` is missing or not one of the supported values, when configuration validation runs after implementation is authorized, then startup fails with a targeted message.
4. Given old boolean transport fields are present, when configuration validation runs after implementation is authorized, then startup fails with migration guidance to use `transport.type`.
5. Given `transport.type: STDIO` and a `transport.http` subtree is present, when configuration validation runs after implementation is authorized, then startup fails with guidance that `transport.http` is only valid for `STREAMABLE_HTTP`.

### User Story 2 - HTTP Transport Remains Usable Without Authorization (Priority: P1)

As an open-source ShardingSphere MCP user, I want HTTP transport to remain simple to start locally without OAuth, token, remote-mode, or Origin-list setup so that the default experience is not blocked by nonessential configuration.

**Independent Test**: A local HTTP YAML example has only `transport.type`; it has no HTTP listener overrides, authorization, OAuth, token validation, protected resource metadata, scope fields, remote fields, or Origin allowlist fields.

**Acceptance Scenarios**:

1. Given `transport.type: STREAMABLE_HTTP` and no `transport.http`, when the HTTP transport starts, then the bind host defaults to `127.0.0.1`, the port defaults to `18088`, and the endpoint path defaults to `/mcp`.
2. Given the YAML omits authorization and OAuth fields, when configuration is loaded, then it is treated as unauthenticated HTTP transport.
3. Given docs mention OAuth, when reviewers inspect them, then OAuth is described only as future work outside this package.
4. Given the YAML omits `remote`, `exposure`, and `allowedOrigins`, when configuration is loaded, then no user-configurable remote or Origin policy is required.
5. Given `http.endpointPath` contains a URL, query, fragment, missing leading slash, or double leading slash, when configuration validation runs after implementation is authorized, then startup fails with a targeted endpoint-path message.
6. Given `http.port` is outside the valid TCP listener range, when configuration validation runs after implementation is authorized, then startup fails with a targeted port message.

### User Story 3 - Remote HTTP Exposure Uses Bind Host Only (Priority: P1)

As an operator, I want local versus remote HTTP exposure to be controlled only by `http.bindHost` so that there is no duplicate remote-mode switch.

**Independent Test**: YAML validation and docs show that `127.0.0.1` is the default local binding and `0.0.0.0` or a specific interface IP is an explicit non-loopback binding.

**Acceptance Scenarios**:

1. Given `http.bindHost` is omitted, when HTTP transport starts, then it binds to `127.0.0.1`.
2. Given `http.bindHost: 0.0.0.0`, when HTTP transport starts, then the server listens on all IPv4 interfaces and docs must warn that this can expose the MCP endpoint beyond the local machine.
3. Given `http.bindHost` is a specific interface address, when HTTP transport starts, then the server binds to that interface only.
4. Given `allowRemoteAccess`, `remote`, or `exposure` appears in YAML, when configuration validation runs after implementation is authorized, then startup fails with guidance that `bindHost` is the only exposure control.
5. Given non-loopback binding is used without HTTP authorization in this slice, when operators deploy it, then docs must clearly state that identity enforcement is out of scope and should be provided by network policy or a reverse proxy if needed.
6. Given `http.bindHost` contains a URL, Origin, path, or blank value, when configuration validation runs after implementation is authorized, then startup fails because `bindHost` must be a local bind host/address only.

### User Story 4 - MCP HTTP Runtime Safety Is Internal (Priority: P1)

As an MCP runtime maintainer, I want MCP-required Streamable HTTP safety behavior to be implemented internally rather than exposed as a public YAML policy surface so that the user-facing API stays small.

**Independent Test**: HTTP runtime tests cover endpoint path behavior and MCP-required Origin handling, while YAML contract tests verify that `allowedOrigins` is not part of the approved configuration shape.

**Acceptance Scenarios**:

1. Given Streamable HTTP is enabled, when the server starts, then it provides one MCP endpoint path that supports POST and GET as required by MCP.
2. Given an incoming HTTP request has a present invalid `Origin`, when runtime checks execute, then the server returns HTTP 403 as required by MCP.
3. Given an incoming HTTP request omits `Origin`, when runtime checks execute, then it is accepted as a non-browser MCP client request and is not rejected by the old remote-allowlist rule.
4. Given an incoming HTTP request has a present non-loopback, malformed, or `null` Origin, when runtime checks execute in this unauthenticated slice, then the server returns HTTP 403.
5. Given an old config contains `allowedOrigins`, when configuration validation runs after implementation is authorized, then startup fails with guidance that Origin policy is internal and not configurable in this slice.
6. Given runtime Origin handling needs browser-origin remote allowlists in the future, when that work is proposed, then it must be designed in a separate package instead of adding hidden fields to this package.

### User Story 5 - Compatibility and Migration Are Reviewable (Priority: P2)

As a downstream operator, I want deprecated authorization-related YAML fields to have clear behavior so that old config does not silently imply OAuth support.

**Independent Test**: YAML swapper/validator tests cover old transport booleans, old remote fields, old Origin fields, and old authorization-related fields as unsupported or migration-only fields for this slice.

**Acceptance Scenarios**:

1. Given an old config uses `transport.http.accessToken`, when configuration validation runs after implementation is authorized, then startup fails with a targeted message that built-in HTTP authorization is not supported in this slice.
2. Given an old config uses OAuth metadata or introspection fields, when configuration validation runs after implementation is authorized, then startup fails with a targeted message that MCP OAuth support is future work.
3. Given an old config uses `transport.http.enabled`, `transport.stdio.enabled`, `allowRemoteAccess`, `remote`, `exposure`, or `allowedOrigins`, when configuration validation runs after implementation is authorized, then startup fails with a targeted migration message.
4. Given docs mention removed fields, when documentation contract tests run, then stale examples fail.
5. Given an unknown field appears under `transport` or `transport.http`, when configuration validation runs after implementation is authorized, then startup fails rather than silently ignoring the field.

### User Story 6 - E2E Cleanup Matches Current Scope (Priority: P1)

As an MCP maintainer, I want E2E tests and test fixtures to reflect the transport-only scope so that stale OAuth, token, protected-resource, and allowlist behavior cannot survive as required current behavior.

**Independent Test**: Reverse searches over `test/e2e/mcp`, `mcp/bootstrap/src/test`, distribution config tests, and mcp-builder evaluation artifacts classify each authorization/introspection hit as removed MCP HTTP authorization, preserved external LLM provider authorization, preserved SQL metadata introspection, or future OAuth documentation.

**Acceptance Scenarios**:

1. Given static token and OAuth introspection are out of scope, when future implementation updates E2E, then `HttpTransportAccessTokenE2ETest` and `HttpTransportOAuthIntrospectionE2ETest` are deleted or replaced by migration-failure coverage outside the runtime success path.
2. Given `allowedOrigins` is removed, when `HttpTransportSecurityE2ETest` is updated, then it contains no access token, authorization server, OAuth introspection, or Origin allowlist fixture and instead covers the internal Origin policy matrix.
3. Given Streamable HTTP has one endpoint for POST and GET, when E2E contract tests run, then they cover POST initialization and a valid GET request with `Accept: text/event-stream`, accepting the documented implementation behavior for SSE or HTTP 405.
4. Given `transport.type` is the only selector, when packaged distribution and runtime fixture tests run, then HTTP and STDIO examples use `transport.type` and no longer require `transport.http.enabled`, `transport.stdio.enabled`, `allowRemoteAccess`, or `OAuthIntrospectionConfiguration`.
5. Given the mcp-builder LLM evaluation artifact currently expects OAuth challenges, when it is updated, then both the XML question and the validator constants stop requiring `authorization`, `WWW-Authenticate`, `resource_metadata`, and `OAuth` as current behavior.
6. Given unrelated tests mention `Authorization` or `introspection`, when cleanup is performed, then external LLM API authorization redaction tests and `metadata_introspection_sql` workflow tests remain in place.

## Requirements

### Functional Requirements

- **MHC-FR-001**: This package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands.
- **MHC-FR-002**: This package MUST classify every current `HttpTransportConfiguration` field as required, optional, deprecated, removable, or out of current scope.
- **MHC-FR-003**: The approved YAML API MUST use `transport.type` as the only transport selector.
- **MHC-FR-004**: Supported `transport.type` values MUST be `STREAMABLE_HTTP` and `STDIO`.
  These are ShardingSphere YAML enum values for the two MCP standard transports, not MCP wire-protocol literals.
- **MHC-FR-005**: `transport.http.enabled` and `transport.stdio.enabled` MUST be removed from the approved YAML shape because they can express contradictory states.
- **MHC-FR-006**: When `transport.type` is `STREAMABLE_HTTP`, the `transport.http` subtree MUST be optional.
- **MHC-FR-007**: When `transport.type` is `STREAMABLE_HTTP`, `transport.http.bindHost`, `transport.http.port`, and `transport.http.endpointPath` MUST be optional and MUST default to `127.0.0.1`, `18088`, and `/mcp`.
- **MHC-FR-008**: `transport.http.accessToken` MUST NOT remain as a supported production HTTP authorization mode in this package.
- **MHC-FR-009**: This package MUST NOT add `authorization`, `oauth`, `tokenValidation`, `introspection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `requiredScopes`, or `bearerMethodsSupported` to the approved YAML shape.
- **MHC-FR-010**: If legacy authorization-related fields are still accepted by the parser during transition, they MUST be classified as unsupported or migration-only and MUST NOT imply working OAuth behavior.
- **MHC-FR-011**: `allowRemoteAccess`, `remote`, and `exposure` MUST be removed from the approved YAML shape; `bindHost` is the only exposure-related HTTP field.
- **MHC-FR-012**: `allowedOrigins` MUST be removed from the approved YAML shape; MCP-required Origin protection MUST be implemented internally rather than as user YAML policy in this slice.
- **MHC-FR-013**: Remote HTTP without built-in authorization MUST be documented as requiring an external trust boundary, such as local network isolation, reverse proxy authentication, or deployment-specific access control.
- **MHC-FR-014**: MCP OAuth Authorization, OAuth Protected Resource Metadata, token introspection, bearer challenges, and scope policy MUST be deferred to a future independent Speckit package.
- **MHC-FR-015**: The implementation plan MUST include migration behavior for removed or renamed YAML fields.
- **MHC-FR-016**: Any implementation touching MCP or MCP E2E paths MUST include `mcp-builder` design/implementation review evidence with findings, classification, and evidence links.
- **MHC-FR-017**: Any non-trivial design decision MUST pass a doubt-driven review cycle before implementation starts.
- **MHC-FR-018**: Completion evidence MUST include commands, source links, contract snapshots, or review artifacts; prose-only closure is insufficient.
- **MHC-FR-019**: When `transport.type` is `STDIO`, the `transport.http` subtree MUST be absent; if it is present, startup MUST fail with guidance that `transport.http` is only valid when `transport.type` is `STREAMABLE_HTTP`.
- **MHC-FR-020**: The approved YAML shape MUST NOT include `authorization: none`; absence of authorization fields is the contract for no built-in authorization.
- **MHC-FR-021**: MCP HTTP authorization E2E success-path coverage MUST be removed from the current package, including static token, OAuth introspection, protected resource metadata, bearer challenge, and scope enforcement scenarios.
- **MHC-FR-022**: E2E fixture and distribution helpers MUST be updated to produce the minimal `transport.type` configuration shape and MUST NOT construct `OAuthIntrospectionConfiguration` as a default runtime dependency.
- **MHC-FR-023**: HTTP security E2E MUST cover the internal Origin policy after `allowedOrigins` removal and MUST NOT use access-token or OAuth headers to make remote-origin cases pass.
- **MHC-FR-024**: Streamable HTTP E2E MUST cover the valid GET path on the same MCP endpoint, with the expected outcome documented as SSE support or HTTP 405 according to the implementation.
- **MHC-FR-025**: E2E cleanup MUST preserve unrelated external LLM provider authorization/redaction tests and SQL metadata introspection tests; broad keyword deletion by `Authorization` or `introspection` is forbidden.
- **MHC-FR-026**: Internal Origin handling in this slice MUST accept missing Origin for non-browser MCP clients, MUST reject malformed, `null`, or non-loopback present Origin with HTTP 403, and MUST limit loopback Origin acceptance to loopback-bound local HTTP.
- **MHC-FR-027**: `transport.http.bindHost` MUST be validated as a local socket bind host/address, not as a URL, Origin, or remote exposure mode.
- **MHC-FR-028**: `transport.http.port` MUST be validated as an HTTP listener port; `0` MAY be accepted only for explicit ephemeral-port test or embedded launches, while distribution examples MUST use the default fixed port `18088`.
- **MHC-FR-029**: `transport.http.endpointPath` MUST identify one absolute MCP endpoint path, MUST start with exactly one leading `/`, and MUST NOT contain a scheme, host, query, or fragment.
- **MHC-FR-030**: Known removed fields and unknown fields under `transport` or `transport.http` MUST fail with targeted validation or loading errors instead of being silently ignored.

### Key Entities

- **Transport Selector**: `transport.type`, whose supported values are `STREAMABLE_HTTP` and `STDIO`.
- **HTTP Listener Configuration**: Optional `bindHost`, `port`, and `endpointPath` overrides with defaults `127.0.0.1`, `18088`, and `/mcp`, valid only when `transport.type` is `STREAMABLE_HTTP`.
- **Internal Origin Protection**: Runtime behavior required by MCP Streamable HTTP; not a YAML configuration subtree in this slice.
- **Unsupported Authorization Fields**: Current flat `accessToken`, `oauthIntrospection`, `protectedResource`, `authorizationServers`, and `scopesSupported` fields that must not become this slice's OAuth model.
- **Migration Contract**: Validation and documentation behavior for removed, renamed, or deferred fields.

## Success Criteria

- **MHC-SC-001**: Reviewers can map each current HTTP field to exactly one responsibility and one future action.
- **MHC-SC-002**: The approved YAML shape contains no built-in authorization or OAuth subtree.
- **MHC-SC-003**: The approved YAML shape contains no `enabled`, `allowRemoteAccess`, `remote`, `exposure`, or `allowedOrigins` transport policy fields.
- **MHC-SC-004**: Tests and documentation cover migration from old boolean, remote, Origin, and authorization-related YAML fields to the minimal transport selector model.
- **MHC-SC-005**: `mcp-builder`, source-driven, and doubt-driven evidence are recorded before implementation is considered ready.
- **MHC-SC-006**: The package cites MCP Authorization as optional and defers OAuth details to future work instead of partially implementing them.
- **MHC-SC-007**: E2E and documentation contract tasks explicitly delete or replace current-scope OAuth/token/protected-resource expectations while preserving unrelated authorization and introspection tests.
- **MHC-SC-008**: Invalid branch combinations and stale fields, including `STDIO` plus `transport.http`, fail loudly rather than being ignored.

## Assumptions

- The current repository targets MCP protocol `2025-11-25` while still supporting `2025-06-18` for compatibility.
- `mcp-java-sdk.version` is `1.1.2` in `mcp/bootstrap/pom.xml`.
- Loopback-only local HTTP can remain unauthenticated if MCP-required Origin protection and binding defaults are preserved.
- Remote HTTP is expressed only by setting `http.bindHost` to a non-loopback address and does not provide built-in identity enforcement.
- `port: 0` exists for test and embedded runtime convenience only; human-facing distribution YAML should use the default fixed port unless an operator deliberately chooses another fixed port.

## Out of Scope

- Implementing code changes before the user gives an explicit implementation command.
- Adding MCP HTTP OAuth Authorization.
- Adding token introspection, JWT/JWKS validation, protected resource metadata, or scope enforcement.
- Building a full OAuth authorization server or dynamic client registration flow.
- Changing STDIO transport behavior.
- Allowing HTTP listener fields under STDIO configuration.
- Adding user-configurable Origin allowlists, remote exposure mode fields, or authorization mode selectors.
- Changing business tool semantics, descriptor names, or encrypt/mask workflows unless required by the HTTP configuration cleanup.
