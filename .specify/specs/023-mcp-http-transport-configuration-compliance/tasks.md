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

# Tasks: MCP HTTP Transport Configuration Compliance

**Input**: `.specify/specs/023-mcp-http-transport-configuration-compliance/spec.md`, `plan.md`, `research.md`, and `source-map.md`  
**Branch Rule**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.  
**Current Rule**: Do not implement code before the user gives an explicit implementation command.

## Phase 1 - Governance And Source Baseline

- [x] T001 Confirm the active branch is `001-shardingsphere-mcp` without switching branches.
- [x] T002 Re-read `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md`.
- [x] T003 Load `source-driven-development` and `doubt-driven-development`, and record `mcp-builder` as a future implementation review gate.
- [x] T004 Verify Codex CLI availability for later doubt-driven cross-model review.
- [x] T005 Record official MCP Streamable HTTP, MCP Authorization, RFC 9728, RFC 7662, RFC 6750, and RFC 8707 source inputs.
- [x] T006 Run first Codex CLI doubt-driven review against the draft package and record classifications in `doubt-review.md`.

## Phase 2 - Speckit Documentation

- [x] T007 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/spec.md`.
- [x] T008 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/plan.md`.
- [x] T009 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/research.md`.
- [x] T010 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/source-map.md`.
- [x] T011 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/tasks.md`.
- [x] T012 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/checklists/requirements.md`.
- [x] T013 Create `.specify/specs/023-mcp-http-transport-configuration-compliance/doubt-review.md`.

## Phase 3 - Pre-Implementation Decisions

- [x] T014 Delete `transport.http.accessToken`; do not retain production `static-token` authorization.
- [x] T015 Replace `allowRemoteAccess` with `exposure.mode`.
- [x] T016 Use targeted validation errors and migration docs for removed or renamed flat YAML fields.
- [x] T017 Do not add custom `requiredScopes`; use MCP-standard `scopesSupported` / `scopes_supported` and challenge `scope` semantics.
- [x] T018 Require `protectedResource.uri` for production OAuth deployments.
- [x] T019 Require HTTPS introspection endpoints except loopback tests, Basic client authentication, fail-closed errors, credential redaction, cache keys including token plus issuer/resource/scope policy, and no caching when `exp` is absent.
- [x] T020 Use RFC 6750 `WWW-Authenticate` and RFC 9728 `resource_metadata`; challenge URI must resolve to served protected resource metadata.
- [x] T049 Allow missing Origin on non-loopback HTTP only for OAuth-authenticated non-browser clients; reject invalid present Origin and reject missing Origin without valid OAuth.
- [x] T050 Accept active introspection responses without `exp` for the current request, but do not cache the successful validation result.
- [x] T051 Use configured `scopesSupported` as the first-version server-configured basic functionality scope set for metadata, scope validation, and challenge guidance.
- [x] T052 Serve protected resource metadata at the endpoint-scoped well-known URI, keep root well-known support, and ensure `resource_metadata` challenge URIs resolve to the served metadata.
- [x] T053 Require identical Origin and authorization gates for POST, GET, and DELETE on the MCP endpoint.

## Phase 4 - Config Model Implementation

- [ ] T021 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java` to express the approved grouped model or a compatibility bridge.
- [ ] T022 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java` for approved YAML shape and migration behavior.
- [ ] T023 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java` to map legacy and new fields explicitly.
- [ ] T024 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidator.java` for listener, exposure, authorization, and metadata validation branches.

## Phase 5 - Authorization And Metadata Runtime

- [ ] T025 Remove or isolate static token handling in `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandler.java`.
- [ ] T026 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidator.java` to use configured `scopesSupported` as the server-configured basic functionality scope set and apply the approved active-without-expiration policy.
- [ ] T027 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServlet.java` to emit metadata only for OAuth-backed authorization, include `bearer_methods_supported`, and derive RFC 9728 well-known URLs.
- [ ] T028 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpOAuthTokenIntrospector.java` for HTTPS, timeout, client-auth, fail-closed, and credential-redaction decisions.
- [ ] T029 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServer.java` and servlet wiring only as needed for the approved metadata registration and method security rules.

## Phase 6 - Tests

- [ ] T030 Add `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidatorTest.java` for every config validation branch, or keep equivalent coverage in `MCPLaunchConfigurationTest` only if the reason is documented.
- [ ] T031 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java` for mapping and migration behavior.
- [ ] T032 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandlerTest.java` for deleted static-token behavior and RFC 6750 challenges.
- [ ] T033 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServletTest.java` for OAuth-only metadata, `bearer_methods_supported`, and RFC 9728 endpoint placement.
- [ ] T034 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidatorTest.java` for canonical resource, issuer invariants, cache behavior, active-without-expiration behavior, and configured scope behavior.
- [ ] T035 Update `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java` for remote exposure and Origin behavior, including invalid present Origin on loopback and the approved missing-Origin policy.
- [ ] T036 Remove or replace `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportAccessTokenE2ETest.java` because static `accessToken` is deleted.
- [ ] T037 Update `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportOAuthIntrospectionE2ETest.java` for OAuth-backed remote authorization and challenge coverage.

## Phase 7 - Docs, Distribution, And Migration

- [ ] T038 Update `distribution/mcp/src/main/resources/conf/mcp-http.yaml` and `mcp-stdio.yaml` examples.
- [ ] T039 Update `mcp/README.md` and `mcp/README_ZH.md` with field migration guidance.
- [ ] T040 Update `docs/mcp/ShardingSphere-MCP-Detailed-Design.md` and related technical design docs if they describe old fields.
- [ ] T041 Add documentation contract coverage for removed or renamed fields.

## Phase 8 - Verification And Review

- [ ] T042 Run scoped `mcp/bootstrap` tests for config, HTTP transport, authorization, and metadata changes.
- [ ] T043 Run focused `test/e2e/mcp` HTTP security/OAuth tests.
- [ ] T044 Run scoped Checkstyle and Spotless for touched modules.
- [ ] T045 Run reverse searches for stale `accessToken`, `allowRemoteAccess`, `requiredScopes`, `authorizationServers`, and metadata references.
- [ ] T046 Run mcp-builder design and implementation reasonableness review and record findings/classification/evidence.
- [ ] T047 Run final doubt-driven review using Codex CLI and classify findings.
- [ ] T048 Confirm no unresolved user questions remain before final handoff.

## Dependencies And Execution Order

- Phase 1 and Phase 2 are documentation setup and may complete before implementation authorization.
- Phase 3 decisions are complete; code changes remain blocked until the user gives an explicit implementation command.
- Phase 4 blocks Phase 5 because runtime behavior depends on the approved config model.
- Phase 6 should be written alongside Phase 4 and Phase 5 changes.
- Phase 7 follows the final field decision and migration behavior.
- Phase 8 is required before completion.
