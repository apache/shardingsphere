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
- [x] T003 Load `source-driven-development` and `doubt-driven-development`, use `mcp-builder` as an MCP design sanity check, and record it as a future implementation review gate.
- [x] T004 Verify Codex CLI availability for later doubt-driven cross-model review.
- [x] T005 Record official MCP Streamable HTTP and MCP Authorization source inputs.
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

- [x] T014 Do not add built-in HTTP authorization in this slice.
- [x] T015 Do not retain `transport.http.accessToken` as a production authorization mode.
- [x] T016 Replace `transport.http.enabled` and `transport.stdio.enabled` with `transport.type`.
- [x] T017 Keep `transport.http.bindHost`, `transport.http.port`, and `transport.http.endpointPath` optional with defaults `127.0.0.1`, `18088`, and `/mcp`, and record their listener validation boundaries.
- [x] T018 Delete `allowRemoteAccess`, `remote`, `exposure`, and `allowedOrigins` from the approved YAML API.
- [x] T019 Treat `authorization`, `oauth`, `tokenValidation`, `introspection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `requiredScopes`, and `bearerMethodsSupported` as out of current scope.
- [x] T020 Use targeted validation errors and migration docs for removed, renamed, deferred, or unknown YAML fields.
- [x] T021 Keep MCP-required Origin handling internal and cover POST, GET, and DELETE on the MCP endpoint.
- [x] T022 Defer MCP OAuth Authorization, OAuth Protected Resource Metadata, token validation, bearer challenges, and scope policy to a future independent Speckit package.
- [x] T022A Reject `transport.http` when `transport.type` is `STDIO`; HTTP listener fields belong only to Streamable HTTP.
- [x] T022B Validate `bindHost`, `port`, and `endpointPath` as listener fields, including `endpointPath` query/fragment rejection and `port: 0` as ephemeral-only test or embedded usage.

## Phase 4 - Config Model Implementation

- [ ] T023 Update transport launch configuration to use `transport.type` as the only transport selector.
- [ ] T024 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java` to express the approved listener model or a compatibility bridge.
- [ ] T025 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java` for approved HTTP YAML shape and migration behavior.
- [ ] T026 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java` to default missing `bindHost`, `port`, and `endpointPath` to `127.0.0.1`, `18088`, and `/mcp`.
- [ ] T027 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/validator/HttpTransportConfigurationValidator.java` for `transport.type`, optional HTTP fields, `STDIO` without `transport.http`, listener override validation, removed-field validation, and unknown-field failure branches.

## Phase 5 - Runtime

- [ ] T028 Remove or reject static token runtime behavior in `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/authorization/HttpBearerAuthorizationHandler.java`.
- [ ] T029 Do not register OAuth protected resource metadata servlets in this slice.
- [ ] T030 Do not wire token introspection or OAuth token validation in this slice.
- [ ] T031 Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServer.java` and servlet wiring only as needed for listener defaults and MCP-required HTTP safeguards.

## Phase 6 - Tests

- [ ] T032 Add or update transport configuration validator tests for `STREAMABLE_HTTP`, `STDIO`, `STDIO` with forbidden `transport.http`, missing `transport.type`, invalid `transport.type`, default HTTP fields, listener overrides, endpoint-path bounds, `port: 0`, and deferred authorization fields.
- [ ] T033 Update YAML swapper tests for `bindHost`, `port`, and `endpointPath` defaulting, listener validation boundaries, and legacy field handling.
- [ ] T034 Update HTTP servlet/server tests for POST, GET, and DELETE MCP-required Origin behavior.
- [ ] T035 Delete `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportAccessTokenE2ETest.java`; do not retain static-token or protected-resource metadata success paths.
- [ ] T036 Delete `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportOAuthIntrospectionE2ETest.java`; OAuth token introspection is future work.
- [ ] T037 Simplify `HttpTransportSecurityE2ETest` for non-loopback binding and the internal Origin matrix without access token, authorization server, `allowedOrigins`, or OAuth introspection fixtures.
- [ ] T038 Add or update valid GET coverage for the Streamable HTTP MCP endpoint with `Accept: text/event-stream`; assert SSE behavior or HTTP 405 according to the implementation.
- [ ] T039 Update `AbstractConfigBackedRuntimeE2ETest`, `PackagedDistributionTestSupport`, and `PackagedDistributionTestSupportTest` so E2E fixtures use minimal `transport.type` HTTP/STDIO configs and no default `OAuthIntrospectionConfiguration`.
- [ ] T040 Add tests that old OAuth-related YAML fields are rejected or documented as unsupported if implementation is authorized to enforce that behavior.
- [ ] T041 Add tests that old boolean, remote, exposure, `allowedOrigins`, and unknown transport YAML fields are rejected with migration guidance.
- [ ] T042 Replace the mcp-builder LLM evaluation authorization question and validator constants with current transport-security/configuration coverage.
- [ ] T043 Preserve external LLM provider `Authorization: Bearer <api key>` and secret-redaction tests, and preserve SQL `metadata_introspection_sql` tests.

## Phase 7 - Docs, Distribution, And Migration

- [ ] T044 Update `distribution/mcp/src/main/resources/conf/mcp-http.yaml` and `mcp-stdio.yaml` examples.
- [ ] T045 Update `mcp/README.md` and `mcp/README_ZH.md` with field migration guidance.
- [ ] T046 Update `docs/mcp/ShardingSphere-MCP-Detailed-Design.md` and related technical design docs if they describe old fields.
- [ ] T047 Add documentation contract coverage for removed, renamed, or deferred fields.
- [ ] T048 Update documentation contract tests so stale current-behavior examples fail for `accessToken`, `oauthIntrospection`, `authorizationServers`, `scopesSupported`, `protectedResource`, `WWW-Authenticate`, and `Authorization: Bearer <token>`.

## Phase 8 - Verification And Review

- [ ] T049 Run scoped `mcp/bootstrap` tests for config and HTTP transport changes.
- [ ] T050 Run focused `test/e2e/mcp` HTTP security, contract, distribution, and mcp-builder evaluation tests affected by this cleanup.
- [ ] T051 Run scoped Checkstyle and Spotless for touched modules.
- [ ] T052 Run reverse searches for stale `transport.http.enabled`, `transport.stdio.enabled`, `transport.http` under STDIO examples, `accessToken`, `allowRemoteAccess`, `remote`, `exposure`, `allowedOrigins`, `authorization`, `oauthIntrospection`, `protectedResource`, `authorizationServers`, `scopesSupported`, `requiredScopes`, `bearerMethodsSupported`, `WWW-Authenticate`, and `resource_metadata` examples.
- [ ] T053 Classify `Authorization` and `introspection` search hits by path so external LLM provider auth and SQL metadata introspection are preserved.
- [ ] T054 Run mcp-builder design and implementation reasonableness review and record findings/classification/evidence.
- [ ] T055 Run final doubt-driven review using Codex CLI and classify findings.
- [ ] T056 Confirm no unresolved user questions remain before final handoff.

## Dependencies And Execution Order

- Phase 1 and Phase 2 are documentation setup and may complete before implementation authorization.
- Phase 3 decisions are complete; code changes remain blocked until the user gives an explicit implementation command.
- Phase 4 blocks Phase 5 because runtime behavior depends on the approved config model.
- Phase 6 should be written alongside Phase 4 and Phase 5 changes.
- Phase 7 follows the final field decision and migration behavior.
- Phase 8 is required before completion.
