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
- Repository MCP Java SDK version: `mcp/bootstrap/pom.xml` sets `mcp-java-sdk.version` to `1.1.2`.
- Repository protocol target: `MCPTransportConstants` uses `ProtocolVersions.MCP_2025_11_25` and supports `MCP_2025_06_18`.

## Decision 1: Keep This Slice Transport-Only

- **Decision**: This package changes transport selection and HTTP listener configuration only.
  It does not add MCP HTTP OAuth Authorization.
- **Rationale**:
  - MCP Authorization is optional for MCP implementations.
  - An open-source default should not require OAuth infrastructure to start an MCP HTTP server.
  - Partial OAuth configuration would add complex fields without delivering a complete authorization product.
  - The current user direction is to avoid adding authorization now.
- **Alternatives considered**:
  - Add `authorization.type: oauth`: rejected because MCP does not require it and the user does not want authorization in this slice.
  - Add `tokenValidation.type: introspection`: rejected because it is an OAuth implementation choice and belongs in a future OAuth package.
  - Add `oauth` as a placeholder subtree: rejected because placeholders make the API look supported before the feature exists.

## Decision 2: Use `transport.type` As The Only Transport Selector

- **Decision**: Replace `transport.http.enabled` and `transport.stdio.enabled` with one required `transport.type` value.
  Supported values are `STREAMABLE_HTTP` and `STDIO`.
- **Rationale**:
  - MCP standard transports are stdio and Streamable HTTP.
  - `STREAMABLE_HTTP` and `STDIO` are ShardingSphere YAML enum values for those standard transports, not literal values defined by MCP.
  - Two booleans can express invalid combinations such as both enabled or both disabled.
  - A single selector is smaller and more reviewable.
  - The repository conduct rules prioritize readability, simplicity, consistency, and removing unused code promptly.
- **Alternatives considered**:
  - Keep both booleans: rejected because it preserves contradictory states.
  - Use lowercase MCP prose names such as `stdio` and `streamable-http`: rejected because ShardingSphere YAML commonly uses uppercase type values for typed options and the package needs a stable repository-facing enum.
  - Add `authorization: none` or mode selectors alongside the booleans: rejected because this slice is transport-only.

## Decision 3: Default Streamable HTTP Listener Values

- **Decision**: Keep `http.bindHost`, `http.port`, and `http.endpointPath` as optional HTTP listener overrides.
  Default them to `127.0.0.1`, `18088`, and `/mcp`.
- **Rationale**:
  - An HTTP server needs a bind address, port, and endpoint path at runtime, but users should not have to write stable local defaults.
  - MCP Streamable HTTP recommends localhost binding for local servers.
  - MCP requires a single HTTP endpoint path, but does not require that path to be user-configured.
  - Remote exposure is already expressed by choosing a non-loopback bind host such as `0.0.0.0` or a specific interface IP.
- **Alternatives considered**:
  - Require all HTTP listener fields in every HTTP config: rejected because the defaults are stable and keep the API smaller.
  - Delete `bindHost` entirely: rejected because operators still need a way to bind a specific interface for remote deployments.
  - Delete `port` or `endpointPath` entirely: rejected because operators may need to avoid port conflicts or mount the endpoint under a different path.
  - Keep a second `remote.enabled` flag: rejected because it duplicates what `bindHost` already expresses.

## Decision 4: Do Not Retain Static `accessToken`

- **Decision**: `transport.http.accessToken` is not retained as a supported production authorization mode.
- **Rationale**:
  - Static shared-secret HTTP authorization is not an MCP Authorization implementation.
  - Replacing it with OAuth is out of current scope.
  - Keeping it would preserve a misleading security boundary.
- **Compatibility behavior**:
  - Old `accessToken` YAML should fail with targeted guidance that built-in HTTP authorization is not supported in this slice.
  - A temporary test fixture may reference the old field only to prove migration failure or deletion behavior.
- **Alternatives considered**:
  - Keep `accessToken` unchanged for convenience: rejected because it preserves the semantic mismatch.
  - Replace `accessToken` with OAuth now: rejected because OAuth is explicitly deferred.

## Decision 5: Delete Remote Mode Fields

- **Decision**: Remove `allowRemoteAccess`, `remote`, and `exposure` from the approved YAML shape without replacement.
- **Rationale**:
  - These fields are not MCP protocol concepts.
  - `bindHost` already expresses whether the server binds only to localhost or to a non-loopback interface.
  - A second remote switch creates a mismatch risk: one field says remote, another field decides the actual socket binding.
- **Alternatives considered**:
  - Replace `allowRemoteAccess` with `exposure.remote.enabled`: rejected after reanalysis because it still duplicates `bindHost`.
  - Replace it with `exposure.mode: local|remote`: rejected because it is not an MCP transport setting and duplicates binding semantics.

## Decision 6: Remove `allowedOrigins` From YAML But Keep Internal Origin Protection

- **Decision**: Remove `allowedOrigins` from the approved YAML shape.
  MCP-required `Origin` handling stays inside Streamable HTTP runtime behavior.
- **Rationale**:
  - MCP Streamable HTTP requires servers to validate the `Origin` header on incoming connections.
  - MCP requires HTTP 403 when a present Origin is invalid.
  - That requirement does not force a public YAML allowlist field in this package.
  - Removing the field keeps the API focused on starting the endpoint.
- **Alternatives considered**:
  - Keep `allowedOrigins` configurable: rejected because the user asked to remove validation-related configuration and it makes the public API wider.
  - Remove runtime Origin handling entirely: rejected because it would violate MCP Streamable HTTP requirements.

## Decision 7: Defer OAuth Metadata And Token Validation

- **Decision**: `oauthIntrospection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `bearerMethodsSupported`, `expectedIssuer`, `authorization`, and `tokenValidation` are not approved target fields in this package.
- **Rationale**:
  - MCP Authorization is optional.
  - These fields only make sense when implementing MCP HTTP OAuth Authorization.
  - A future OAuth package can design the full contract at once, including protected resource metadata, token validation, bearer challenges, and scope behavior.
  - Keeping partial fields now would make reviewers think OAuth is supported when it is not.
- **Alternatives considered**:
  - Keep the fields as disabled placeholders: rejected because placeholders add public API surface without behavior.
  - Document them as recommended future config in this package: rejected because this package is now transport-only.

## Decision 8: Recheck HTTP Method Coverage

- **Decision**: MCP-required HTTP safety behavior must be specified for POST, GET, and DELETE on the MCP endpoint before implementation.
- **Rationale**:
  - Streamable HTTP uses POST and GET; DELETE may terminate sessions when sessions are enabled.
  - A security policy that covers POST but misses GET or DELETE can leave session or stream behavior inconsistent.
  - This is a runtime wiring decision, not a new configuration field, so it belongs in implementation verification rather than config shape.
- **Alternatives considered**:
  - Limit this package to YAML validation only: rejected because `HttpTransportConfiguration` drives runtime security behavior.

## Decision 9: Defer Code Until A User Command

- **Decision**: This package records the design and task plan only.
- **Rationale**:
  - User explicitly said to wait for a later command before code changes.
  - Speckit is useful here as a requirements and evidence boundary before implementation.
- **Alternatives considered**:
  - Start refactoring immediately: rejected by user instruction.

## Decision 10: Simplify E2E Beyond The Two Auth Classes

- **Decision**: E2E cleanup must remove every current-scope MCP HTTP authorization expectation, not only `HttpTransportAccessTokenE2ETest` and `HttpTransportOAuthIntrospectionE2ETest`.
- **Rationale**:
  - The current E2E and test surface also includes authorization assumptions in runtime fixtures, distribution config helpers, documentation contract tests, and the mcp-builder LLM evaluation artifact.
  - Leaving those tests unchanged would keep OAuth, bearer challenges, protected resource metadata, or old transport booleans as required current behavior even after the YAML API removes them.
  - MCP Streamable HTTP still needs runtime safety coverage, so the replacement tests should focus on `transport.type`, listener defaults, non-loopback binding, the valid GET contract, session/protocol behavior, and internal Origin validation.
- **Required E2E cleanup actions after implementation is authorized**:
  - Delete `HttpTransportAccessTokenE2ETest` because static token success and protected-resource metadata are not current behavior.
  - Delete `HttpTransportOAuthIntrospectionE2ETest` because token introspection, issuer/resource/scope validation, and OAuth challenges are future work.
  - Simplify `HttpTransportSecurityE2ETest` so it has no access token, authorization server, `allowedOrigins`, or OAuth introspection fixture.
  - Update `AbstractConfigBackedRuntimeE2ETest`, `PackagedDistributionTestSupport`, and `PackagedDistributionTestSupportTest` to use minimal `transport.type` HTTP/STDIO configs.
  - Update `MCPDocumentationContractTest` to assert old auth/current-behavior examples are absent or explicitly future/out-of-scope.
  - Replace the mcp-builder evaluation XML authorization question and the validator constants that require `authorization`, `WWW-Authenticate`, `resource_metadata`, or `OAuth` evidence.
  - Keep external LLM API `Authorization: Bearer <api key>` and redaction tests because they are not MCP HTTP authorization.
  - Keep `metadata_introspection_sql` tests because they refer to SQL metadata introspection, not OAuth token introspection.
- **Origin and GET coverage outcome**:
  - A present invalid `Origin` must return HTTP 403.
  - Missing `Origin` should be accepted for non-browser MCP clients rather than inheriting the old remote-allowlist rejection.
  - Present malformed Origin, `Origin: null`, or present non-loopback Origin should return HTTP 403 in this unauthenticated slice.
  - Present loopback Origin should be accepted only for loopback-bound local HTTP.
  - Browser-origin remote allowlists are future work because this package removed `allowedOrigins` and does not add built-in authorization.
  - A valid GET with `Accept: text/event-stream` must be covered; the test should assert either SSE behavior or HTTP 405, whichever is the documented implementation result.
- **Alternatives considered**:
  - Delete only the two auth E2E classes: rejected because stale auth behavior would remain in docs, fixture, distribution, and mcp-builder evaluation tests.
  - Delete every test containing `Authorization` or `introspection`: rejected because external LLM provider auth redaction and SQL metadata introspection are unrelated and must remain.

## Decision 11: Make Transport Branches And Listener Bounds Strict

- **Decision**: Treat `transport.http` as a Streamable HTTP-only subtree.
  A config with `transport.type: STDIO` and `transport.http` present must fail validation instead of carrying ignored HTTP listener settings.
- **Rationale**:
  - MCP defines stdio as communication over standard input and standard output; there is no HTTP listener in that transport.
  - MCP Streamable HTTP requires one MCP endpoint path for POST and GET; those listener fields only make sense for Streamable HTTP.
  - Allowing HTTP fields under STDIO would preserve the same invalid-state smell as the old dual `enabled` booleans.
- **Listener validation boundary**:
  - `bindHost` is the socket bind host/address only.
    Examples: `127.0.0.1`, `0.0.0.0`, or a concrete local interface IP.
    It is not a URL, Origin, path, or remote-mode policy field.
  - `port` defaults to `18088`.
    `0` may be accepted as an explicit ephemeral bind for tests or embedded launches that read the actual local port from the runtime server; distribution examples must use a fixed port.
  - `endpointPath` defaults to `/mcp`.
    It must be one absolute path such as `/mcp` or `/api/mcp`, must not begin with `//`, and must not contain a scheme, host, query, or fragment.
  - Known removed fields and unknown fields under `transport` or `transport.http` should fail loudly so old YAML cannot appear to work while being ignored.
- **Alternatives considered**:
  - Allow `transport.http` under STDIO and ignore it: rejected because it hides stale config and contradicts the single-selector model.
  - Reject `port: 0` everywhere: rejected because the current test/runtime pattern uses ephemeral HTTP ports to avoid conflicts; the product docs can still keep fixed-port examples.
  - Keep the existing endpoint check of "starts with `/`" only: rejected because `/mcp?debug=true`, `/mcp#frag`, or `//mcp` are not clean single endpoint paths.

## Future MCP Builder Review Notes

- `mcp-builder` is used as a design sanity check for MCP server quality, not as protocol authority.
  Official MCP pages remain the normative sources.
- It remains a future design/implementation review gate if implementation touches `mcp/**`, `distribution/mcp/**`, or `test/e2e/mcp/**`.
- Tool naming and response-format guidance in the generic mcp-builder reference does not directly apply to this HTTP configuration package.
- Response-format guidance is out of scope because this package changes HTTP transport configuration, not MCP tool result formats.
- The strict-branch update is consistent with mcp-builder's general guidance for actionable errors: stale or invalid YAML should point operators to the supported `transport.type` shape rather than being ignored.

## Reanalysis Closure Notes

- MCP Authorization is optional, so the current package should not add OAuth configuration.
- Static `accessToken` should not be retained as a production authorization mode.
- OAuth Protected Resource Metadata, token introspection, bearer challenges, and scope metadata are deferred to a future independent package.
- `remote.enabled`, `allowRemoteAccess`, and `exposure` should be deleted because `bindHost` is the only exposure expression.
- `allowedOrigins` should be deleted from YAML, while MCP-required Origin handling remains internal runtime behavior.
- E2E cleanup must include programmatic runtime tests, shared runtime fixtures, distribution helper tests, documentation contract tests, and mcp-builder evaluation artifacts.
- External LLM provider authorization redaction and SQL metadata introspection tests are explicitly preserved.
- Final configuration-boundary reanalysis adds strict rejection for `STDIO` plus `transport.http`, path/query/fragment mistakes in `endpointPath`, non-bind-address values in `bindHost`, and ignored stale or unknown fields.
