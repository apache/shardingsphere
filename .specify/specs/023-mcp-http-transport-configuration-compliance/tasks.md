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

- [ ] T014 Confirm deletion of `transport.http.accessToken` as the default path; retain a renamed non-OAuth `static-token` mode only after new explicit user approval.
- [ ] T015 Decide whether `allowRemoteAccess` becomes `exposure.mode` or remains as a safety confirmation field.
- [ ] T016 Decide the migration behavior for old flat YAML fields.
- [ ] T017 Decide whether `scopesSupported` is split from `requiredScopes`.
- [ ] T018 Decide when `protectedResource.uri` is mandatory for production OAuth deployments.
- [ ] T019 Decide OAuth introspection HTTPS, timeout, client authentication, fail-closed, redaction, and cache-key rules.
- [ ] T020 Decide RFC 6750 and RFC 9728 challenge/metadata endpoint behavior.
- [ ] T049 Decide whether missing Origin on non-loopback HTTP is rejected as ShardingSphere hardening or allowed for OAuth-authenticated non-browser clients.
- [ ] T050 Decide introspection behavior for active tokens without `exp`, including accept/reject policy and whether successful results may be cached.
- [ ] T051 Decide request-required scope challenge strategy separately from `scopesSupported` metadata.
- [ ] T052 Decide exact protected resource metadata URL registration strategy and prove `resource_metadata` challenge URIs resolve to served metadata.
- [ ] T053 Decide whether POST, GET, and DELETE all require identical Origin and authorization gates, or document method-specific exceptions.

## Phase 4 - Config Model Implementation

- [ ] T021 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java` to express the approved grouped model or a compatibility bridge.
- [ ] T022 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java` for approved YAML shape and migration behavior.
- [ ] T023 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java` to map legacy and new fields explicitly.
- [ ] T024 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidator.java` for listener, exposure, authorization, and metadata validation branches.

## Phase 5 - Authorization And Metadata Runtime

- [ ] T025 Remove or isolate static token handling in `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandler.java`.
- [ ] T026 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidator.java` to use explicit required scopes and the approved active-without-expiration policy if scope splitting is approved.
- [ ] T027 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServlet.java` to emit metadata only for OAuth-backed authorization, include `bearer_methods_supported`, and derive RFC 9728 well-known URLs.
- [ ] T028 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpOAuthTokenIntrospector.java` for HTTPS, timeout, client-auth, fail-closed, and credential-redaction decisions.
- [ ] T029 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServer.java` and servlet wiring only as needed for the approved metadata registration and method security rules.

## Phase 6 - Tests

- [ ] T030 Add `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidatorTest.java` for every config validation branch, or keep equivalent coverage in `MCPLaunchConfigurationTest` only if the reason is documented.
- [ ] T031 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java` for mapping and migration behavior.
- [ ] T032 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandlerTest.java` for deleted or isolated static-token behavior and RFC 6750 challenges.
- [ ] T033 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthProtectedResourceMetadataServletTest.java` for OAuth-only metadata, `bearer_methods_supported`, and RFC 9728 endpoint placement.
- [ ] T034 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/OAuthTokenValidatorTest.java` for canonical resource, issuer invariants, cache behavior, active-without-expiration behavior, and required-scope behavior.
- [ ] T035 Update `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java` for remote exposure and Origin behavior, including invalid present Origin on loopback and the approved missing-Origin policy.
- [ ] T036 Update or remove `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportAccessTokenE2ETest.java` based on the static-token decision.
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
- [ ] T045 Run reverse searches for stale `accessToken`, `allowRemoteAccess`, `authorizationServers`, and metadata references.
- [ ] T046 Run mcp-builder design and implementation reasonableness review and record findings/classification/evidence.
- [ ] T047 Run final doubt-driven review using Codex CLI and classify findings.
- [ ] T048 Confirm no unresolved user questions remain before final handoff.

## Dependencies And Execution Order

- Phase 1 and Phase 2 are documentation setup and may complete before implementation authorization.
- Phase 3 plus reanalysis tasks T049-T053 block all code changes.
- Phase 4 blocks Phase 5 because runtime behavior depends on the approved config model.
- Phase 6 should be written alongside Phase 4 and Phase 5 changes.
- Phase 7 follows the final field decision and migration behavior.
- Phase 8 is required before completion.
