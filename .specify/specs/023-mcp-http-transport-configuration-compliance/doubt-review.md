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

## Current-Scope Reanalysis

**Mode**: Documentation-only reanalysis.
**Date**: 2026-05-17
**Scope**: Recheck whether this package should add MCP HTTP authorization, remote exposure switches, or user-configurable Origin policy.

### Findings

- **MCP Authorization is optional**: Valid and decisive.
  The MCP Authorization specification says authorization is optional for MCP implementations.
  This package can remain MCP-compliant without adding OAuth as long as it does not pretend to implement MCP Authorization.
- **Open-source default should stay easy to start**: Valid and decisive.
  Requiring OAuth infrastructure in the first cleanup slice would make local and demo use harder without being required by MCP.
- **Previous OAuth configuration shape was over-scoped for this package**: Valid and actionable.
  Removed `authorization`, `oauth`, `tokenValidation`, `introspection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `requiredScopes`, and `bearerMethodsSupported` from the current target YAML shape.
- **Static `accessToken` should not survive as a substitute**: Valid and actionable.
  The package does not retain static shared-secret HTTP authorization as a production mode.
- **Dual transport booleans are an avoidable invalid-state surface**: Valid and actionable.
  `transport.type` replaces `transport.http.enabled` and `transport.stdio.enabled`.
- **`remote.enabled` duplicates `bindHost`**: Valid and actionable.
  Remote exposure is expressed only by choosing a non-loopback `http.bindHost`.
- **`allowedOrigins` is a user-configurable validation policy surface**: Valid and actionable.
  It is removed from YAML, while MCP-required Origin handling remains an internal runtime requirement.
- **Listener defaults make the API smaller without changing MCP semantics**: Valid and actionable.
  `bindHost`, `port`, and `endpointPath` are defaulted to `127.0.0.1`, `18088`, and `/mcp`, while remaining overridable.

### Closure Result

- **Blocking findings**: None after rescoping the package to a minimal transport selector plus optional HTTP listener overrides.
- **Reviewer conclusion**: OAuth authorization should be deferred to a future independent Speckit package.
  Remote mode fields and Origin allowlist fields should not be part of the current YAML API.

## Future OAuth Review Gate

If a future package adds MCP HTTP OAuth Authorization, it must re-open a separate source-driven and doubt-driven review for:

- OAuth Protected Resource Metadata.
- Token validation mechanism such as introspection or JWT/JWKS.
- Bearer challenge behavior.
- Scope metadata and request-level scope enforcement.
- Reverse proxy public resource URI behavior.

## E2E Simplification Reanalysis

**Mode**: Codex CLI read-only adversarial review plus local reconciliation.
**Date**: 2026-05-17
**Scope**: Recheck whether the E2E cleanup plan fully removes current-scope MCP HTTP authorization expectations while preserving unrelated tests.

### Findings

- **Deleting only two E2E classes is under-scoped**: Valid and actionable.
  The cleanup must also cover documentation contract tests, config validator/swapper tests, runtime fixtures, distribution helper tests, and mcp-builder evaluation artifacts that currently encode OAuth or static-token behavior.
- **`transport.type` needs E2E and distribution contract coverage**: Valid and actionable.
  Future E2E fixtures and packaged distribution tests must prove minimal `transport.type: STREAMABLE_HTTP`, explicit HTTP defaults, and `transport.type: STDIO` without requiring the old HTTP/STDIO booleans.
- **Origin behavior needs a post-`allowedOrigins` matrix**: Valid and actionable.
  `HttpTransportSecurityE2ETest` must no longer depend on allowlists or token headers, and must instead verify the internal Origin policy that remains after YAML simplification: accept missing Origin for non-browser clients, reject malformed, `null`, or non-loopback present Origin with HTTP 403, and accept loopback Origin only for loopback-bound local HTTP.
- **Valid GET behavior is missing from the E2E plan**: Valid and actionable.
  Streamable HTTP must cover a valid GET request with `Accept: text/event-stream` on the same MCP endpoint, asserting either SSE behavior or HTTP 405 according to the implementation.
- **Documentation contract update must reject stale current-behavior examples**: Valid and actionable.
  It is not enough to stop requiring old auth fields; stale examples for `accessToken`, `oauthIntrospection`, `authorizationServers`, `scopesSupported`, `protectedResource`, `WWW-Authenticate`, and `Authorization: Bearer <token>` must fail unless clearly marked as future/out-of-scope.
- **mcp-builder evaluation has two edit points**: Valid and actionable.
  Replacing the XML OAuth authorization question is insufficient unless `MCPBuilderEvaluationArtifactTest` constants stop requiring the `authorization` category and OAuth evidence terms.
- **Keyword deletion would be unsafe**: Valid guardrail.
  External LLM provider `Authorization: Bearer <api key>` redaction tests and SQL `metadata_introspection_sql` tests are unrelated to MCP HTTP authorization and must be preserved.

### Closure Result

- **Previous answer to "is the E2E analysis complete"**: No.
  It missed distribution fixtures, documentation contracts, mcp-builder evaluation artifacts, and valid GET coverage.
- **Current result after documentation update**: The Speckit package now records those missing E2E cleanup boundaries and preservation guardrails.

## Configuration Boundary Reanalysis

**Mode**: Source-driven plus local adversarial re-check.
**Date**: 2026-05-17
**Scope**: Recheck whether the minimal YAML shape still permits meaningless or ambiguous configuration states.

### Claim Under Review

The approved YAML API is minimal and stable if it keeps only `transport.type` plus optional HTTP listener overrides for Streamable HTTP.

### Findings

- **`STDIO` plus `transport.http` was under-specified**: Valid and actionable.
  Earlier docs said `transport.http` is not required for STDIO, but did not say whether it is forbidden.
  Because MCP stdio has no HTTP listener, the docs now require `transport.http` to be absent when `transport.type` is `STDIO`.
- **`endpointPath` needed tighter bounds**: Valid and actionable.
  MCP Streamable HTTP requires one endpoint path for POST and GET.
  The docs now reject URL syntax, query strings, fragments, and double leading slashes instead of only checking that the value starts with `/`.
- **`bindHost` needed product-language precision**: Valid and actionable.
  It is a local socket bind host/address, not a public URL, Origin, or remote-mode switch.
  Examples and non-examples are now recorded in the source map.
- **`port: 0` is a real runtime need but not a distribution default**: Valid trade-off.
  Current test/runtime patterns use ephemeral HTTP ports to avoid conflicts.
  The docs now allow it only for explicit test or embedded launches while keeping fixed port `18088` for distribution examples.
- **Silent unknown-field handling would weaken the API cleanup**: Valid and actionable.
  Known removed fields and unknown fields under `transport` or `transport.http` must fail loudly so stale YAML cannot appear supported.

### Closure Result

- **Blocking findings**: None after adding strict STDIO/HTTP branch validation, listener bounds, and stale-field rejection.
- **Cross-model note**: No new external CLI review was run in this sub-round; the earlier Codex CLI doubt review already covered the larger package scope, and this pass was a bounded documentation-only refinement.
- **Final self-question**: Is there another configuration field that still needs to be invented or retained for current MCP compliance?
  Answer: No. MCP compliance for this slice requires correct transport selection and internal Streamable HTTP behavior, not more public YAML fields.
