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

# Tasks: MCP Contract and E2E Gap Triage

**Input**: Design documents from `.specify/specs/016-mcp-contract-e2e-gap-triage/`
**Prerequisites**: `spec.md`, `finding-ledger.md`, `source-path-evidence.md`,
`e2e-test-disposition.md`, `checklists/requirements.md`
**Tests**: Required for every Java, YAML descriptor, HTTP transport, completion, workflow, CI, or E2E change.
**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story supports the task.
- Every implementation task names the primary file or package path.
- Every coding task must start with a branch/path coverage checklist and finish with scoped tests plus style checks.

## Non-Negotiable Invariants

- Protocol behavior uses MCP Specification `2025-11-25`.
- Registry metadata uses the official MCP Registry `server.json` schema dated `2025-12-11`.
- Secret-bearing values must not be requested through MCP form elicitation.
- Remote HTTP must fail closed unless an explicit allowlist permits the origin.
- Helper-unit tests under `test/e2e/mcp` stay as support coverage until a narrower harness module exists.
- Existing owner packages stay authoritative:
  012 owns OAuth fail-closed scorecard work; 013 owns field standardization;
  014 owns output-schema and distribution hardening; 015 owns protocol/API generalization.
- Proxy-first scope, explicit operator control, and no-data-migration boundaries remain intact.

---

## Phase 1: Setup and Evidence Lock

- [x] T001 Confirm current branch remains `001-shardingsphere-mcp` without running branch-changing commands.
  Path: repository root
- [x] T002 [P] Record official MCP `2025-11-25` source references and registry metadata source split.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/spec.md`
- [x] T003 [P] Record exact local source-path evidence for every MCE finding.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/source-path-evidence.md`
- [x] T004 [P] Classify every MCP E2E class and name preserving evidence for reduction candidates.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/e2e-test-disposition.md`
- [x] T005 [P] Lock the adjacent-owner map for packages 012, 013, 014, and 015.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/finding-ledger.md`
- [x] T006 [P] Verify no unresolved target, owner, or `preserved_by` marker remains in package 016.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/checklists/requirements.md`

**Checkpoint**: Implementation can be planned from source-backed tasks without reopening triage.

---

## Phase 2: P0 Safety and Transport

**Goal**: Remove non-standard or safety-sensitive behavior before broader contract polish.
**Independent Test**: HTTP contract and bootstrap unit tests reject unsafe negotiation, origin, and elicitation cases.

### Tests for P0

- [x] T020 [P] [US1] Add secret-safe elicitation tests proving form elicitation never requests secrets.
  Paths: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`,
  `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/`
- [x] T021 [P] [US1] Add strict Streamable HTTP `Accept` negotiation tests for missing and unsupported headers.
  Paths: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`
- [x] T022 [P] [US1] Add remote origin allowlist tests for allowed, unlisted, empty, malformed, and loopback origins.
  Paths: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java`,
  `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactoryTest.java`,
  `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/constraint/LoopbackOriginHeaderConstraintTest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSecurityE2ETest.java`
- [x] T023 [US1] Cross-link OAuth inactive, expired, wrong issuer, introspection failure, challenge, and no-token-passthrough tests to package 012.
  Path: `.specify/specs/012-mcp-scorecard-perfect-100/tasks.md`

### Implementation for P0

- [x] T024 [US1] Remove or bypass form-mode elicitation for secret-bearing workflow questions and document approved secret channels.
  Paths: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolElicitationHandler.java`,
  `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`,
  `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`,
  `mcp/README.md`
- [x] T025 [US1] Replace default `Accept` header injection with MCP-baseline-compliant negotiation behavior.
  Path: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
- [x] T026 [US1] Add explicit remote origin allowlist configuration and validator wiring.
  Paths: `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactory.java`,
  `mcp/README.md`

**Checkpoint**: P0 closure has passing unit and E2E evidence, with OAuth remaining owned by 012.

---

## Phase 3: P1 Tool Contract Enforcement

**Goal**: Tool schemas are enforced before business handlers run.
**Independent Test**: Invalid required fields, types, enum values, and unknown fields fail before handler execution.

### Tests for Tool Contracts

- [x] T030 [P] [US2] Add input-schema validation tests through the existing handler registry path.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistryTest.java`
- [x] T031 [P] [US2] Add SDK-facing tool-call tests proving invalid input never reaches handler dispatch.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`
- [x] T032 [P] [US2] Add one HTTP contract smoke for invalid tool input and stable recovery fields.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`

### Implementation for Tool Contracts

- [x] T033 [US2] Enforce declared `inputSchema` required fields, primitive types, enum values, and unknown-field policy.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/MCPToolArgumentContract.java`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/exception/MCPToolArgumentContractViolationException.java`
- [x] T034 [US2] Ensure bootstrap tool calls validate input before dispatch and return model-correctable errors.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPBasicRecoveryPayloadFactory.java`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPRecoveryPayloadFactory.java`,
  `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java`
- [x] T035 [US2] Cross-link output-schema strictness and canonical enum casing to packages 014 and 013.
  Paths: `.specify/specs/014-mcp-standard-and-e2e-hardening/tasks.md`,
  `.specify/specs/013-mcp-protocol-field-standardization/tasks.md`
  Evidence: package 016 now enforces declared input enum values exactly; package 013 remains owner for canonical casing policy,
  and package 014 remains owner for output-schema strictness.

**Checkpoint**: Tool descriptors are executable contracts, not only client hints.

---

## Phase 4: P1 Lifecycle, Completion, URI, and Session Evidence

**Goal**: E2E tests prove the user-visible product paths that lower-level unit tests cannot prove.
**Independent Test**: HTTP and STDIO helpers, completions, resource URIs, and sessions behave deterministically.

### Tests for Runtime Evidence

- [x] T040 [P] [US1] Add HTTP initialized-notification evidence or a sourced exception for Streamable HTTP helper behavior.
  Paths: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/MCPHttpInteractionClient.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`
- [x] T041 [P] [US2] Add product E2E coverage for resource URI encoded spaces, encoded slashes, malformed encoding, and missing variables.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/MetadataDiscoveryE2ETest.java`
- [x] T042 [P] [US3] Add positive completion E2E for metadata table, column, index, sequence, algorithms, and workflow plan IDs.
  Paths: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/MetadataDiscoveryE2ETest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyEncryptWorkflowE2ETest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyMaskWorkflowE2ETest.java`
- [x] T043 [P] [US3] Add two-client isolation tests for session delete, transaction cleanup, workflow approval, and completion-plan scope.
  Paths: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportSessionLifecycleE2ETest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/ExecuteQueryTransactionE2ETest.java`

### Implementation for Runtime Evidence

- [x] T044 [US1] Update HTTP helpers to send initialized notification, or document the sourced SDK exception in E2E and README.
  Paths: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/transport/client/MCPHttpInteractionClient.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/AbstractHttpProgrammaticRuntimeE2ETest.java`,
  `mcp/README.md`
- [x] T045 [US3] Fix session or workflow state cleanup only if new two-client E2E evidence exposes a defect.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/session`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow`,
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow`

**Checkpoint**: Missing E2E evidence is added without reclassifying helper-unit tests as product-path evidence.

---

## Phase 5: Registry, Scope, and E2E Rationalization

**Goal**: Release metadata and product scope are explicit, and E2E assertions stay maintainable.
**Independent Test**: Release workflows validate metadata, docs classify scope, and tests assert stable fields.

### Tests and Release Gates

- [ ] T050 [P] [US3] Add script-level coverage for `server.json` release version and package identifier rewrite behavior.
  Path: `.github/workflows/resources/scripts/prepare-mcp-server-json.py`
- [ ] T051 [P] [US3] Add release-gate validation for official registry schema, package transports, OCI metadata, and snapshot rejection.
  Paths: `.github/workflows/jdk21-subchain-ci.yml`, `.github/workflows/mcp-build.yml`, `mcp/server.json`
- [ ] T052 [P] [US5] Add capability-scope tests or snapshots proving unimplemented optional MCP capabilities are not advertised.
  Paths: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/MCPSyncServerFactoryTest.java`,
  `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandlerTest.java`
- [ ] T053 [P] [US4] Reduce golden and recovery E2E assertions to stable structured fields and one canonical snapshot gate.
  Paths: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportGoldenContractE2ETest.java`,
  `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportRecoveryE2ETest.java`

### Documentation and Scope

- [ ] T054 [US5] Document optional MCP capability support, unsupported items, and future scope.
  Path: `mcp/README.md`
- [ ] T055 [US5] Document ShardingSphere feature breadth for sharding, readwrite-splitting, shadow, traffic, discovery, governance, and observability.
  Path: `mcp/README.md`
- [ ] T056 [US4] Keep fixture/helper tests as `KEEP-SUPPORT` and exclude them from product-path E2E release claims.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/e2e-test-disposition.md`
- [ ] T057 [US4] Cross-link prompt/resource catalog clarity and canonical error channels to package 015.
  Path: `.specify/specs/015-mcp-protocol-api-generalization/tasks.md`

**Checkpoint**: Release readiness and product scope are explicit, not implied by broad product names or brittle snapshots.

---

## Phase 6: Verification and Handoff

- [x] T060 Run scoped unit tests for every touched MCP module.
  Command: `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=<ClassName> -Dsurefire.failIfNoSpecifiedTests=false test`
- [x] T061 Run scoped E2E tests for every touched product-path class.
  Command: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=<ClassName> -Dsurefire.failIfNoSpecifiedTests=false test`
- [x] T062 Run Checkstyle or Spotless gates for touched modules.
  Commands: `./mvnw -pl <module> -am -Pcheck checkstyle:check`,
  `./mvnw spotless:apply -Pcheck -pl <module>`
- [x] T063 Update 016 evidence after each completed implementation slice.
  Paths: `.specify/specs/016-mcp-contract-e2e-gap-triage/finding-ledger.md`,
  `.specify/specs/016-mcp-contract-e2e-gap-triage/source-path-evidence.md`,
  `.specify/specs/016-mcp-contract-e2e-gap-triage/checklists/requirements.md`
- [x] T064 Record commands, exit codes, residual risks, and adjacent owner updates before claiming closure.
  Path: `.specify/specs/016-mcp-contract-e2e-gap-triage/tasks.md`

**Final Checkpoint**: Every implemented task has passing evidence, no branch switch occurred, and no unrelated dirty files were reverted.

## Recommended First Slice

Start with T021 and T025 together.
They touch one transport behavior, have clear existing contradictory evidence, and can be verified with one bootstrap unit test plus one HTTP contract test.

## Completed Slice Evidence

### 2026-05-14: T021/T025 Strict HTTP Accept Negotiation

- Code: removed servlet-level fallback `Accept` header injection and delegated original HTTP requests to the MCP transport.
- Tests: moved unsupported `Accept` media-type coverage from `HttpTransportSecurityE2ETest` to `HttpTransportContractE2ETest`;
  added POST missing, POST single-media-type, GET missing, and GET wrong-media-type rejection coverage.
- Verification:
  - `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=StreamableHttpMCPServletTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; `StreamableHttpMCPServletTest` ran 22 tests with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpTransportContractE2ETest,HttpTransportSecurityE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; `HttpTransportContractE2ETest` ran 22 tests and `HttpTransportSecurityE2ETest` ran 2 tests, with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpTransportContractE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; `HttpTransportContractE2ETest` ran 22 tests with 0 failures, 0 errors, and 0 skipped after reanalysis coverage expansion.
  - `./mvnw -pl mcp/bootstrap,test/e2e/mcp -Pcheck -DskipTests checkstyle:check`
    exited `0`; 0 Checkstyle violations in `shardingsphere-mcp-bootstrap` and `shardingsphere-test-e2e-mcp`.
  - `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests checkstyle:check`
    exited `0`; 0 Checkstyle violations after the reanalysis coverage expansion.

### 2026-05-14: T022/T026 Remote HTTP Origin Allowlist

- Code: added `transport.http.allowedOrigins` to HTTP configuration and YAML swapping; remote HTTP startup now requires a non-empty valid origin list,
  while loopback binding keeps the existing loopback-origin guard.
- Code: wired remote bindings through `AllowedOriginHeaderConstraint` and removed the unsafe 3-argument validator factory path so remote origin policy is explicit at construction.
- Tests: added allowed, unlisted, empty, malformed, and loopback-origin rejection coverage across YAML swapper, validator factory, origin constraints, and real HTTP E2E.
- Docs: documented `transport.http.allowedOrigins`, placeholder support, remote binding requirements, and 403 behavior in English and Chinese README files.
- Verification:
  - `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=HttpTransportOriginUtilsTest,YamlHttpTransportConfigurationSwapperTest,MCPLaunchConfigurationTest,ServerTransportSecurityValidatorFactoryTest,AllowedOriginHeaderConstraintTest,LoopbackOriginHeaderConstraintTest,StreamableHttpMCPServletTest,MCPDocumentationContractTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; 88 tests ran with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpTransportSecurityE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; `HttpTransportSecurityE2ETest` ran 7 tests with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=PackagedDistributionTestSupportTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; `PackagedDistributionTestSupportTest` ran 6 tests with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl mcp/bootstrap -Pcheck -DskipTests checkstyle:check`
    exited `0`; 0 Checkstyle violations.
  - `./mvnw -pl test/e2e/mcp -Pcheck -DskipTests checkstyle:check`
    exited `0`; 0 Checkstyle violations.
  - `git diff --check`
    exited `0`; no whitespace errors.

### 2026-05-14: T020/T023/T024 Secret-Safe Elicitation

- Source: MCP Elicitation `2025-11-25` says form mode must not collect sensitive values. Local SDK verification showed MCP Java SDK `1.1.2`
  `ElicitRequest` does not expose URL-mode fields, so the safe implementation is to avoid form elicitation for secret-bearing questions.
- Code: `MCPToolElicitationHandler` now gates form elicitation on non-sensitive clarification questions only. Questions marked `secret`, using `input_type=secret`,
  or naming password/token/key/secret/credential fields return as tool clarifications and do not call `createElicitation`.
- Code: removed password-format form schema generation for clarification questions.
- Tests: `MCPToolSpecificationFactoryTest` now proves non-sensitive form elicitation still works, and covers `secret=true`, `input_type=secret`,
  and sensitive field-name fallback without calling MCP form elicitation.
- Docs: English and Chinese README files plus encrypt/mask descriptors document URL mode when available, secret manager, protected environment variables,
  and operator-controlled channels as approved secret paths.
- Cross-link: package 012 remains owner for inactive, expired, wrong issuer, introspection failure, challenge, and no-token-passthrough OAuth evidence.
- Verification:
  - `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=MCPToolSpecificationFactoryTest,MCPDocumentationContractTest,HttpTransportOriginUtilsTest,YamlHttpTransportConfigurationSwapperTest,MCPLaunchConfigurationTest,ServerTransportSecurityValidatorFactoryTest,AllowedOriginHeaderConstraintTest,LoopbackOriginHeaderConstraintTest,StreamableHttpMCPServletTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; 102 tests ran with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl mcp/bootstrap -am -DskipITs -Dspotless.skip=true -Dtest=MCPToolSpecificationFactoryTest,MCPDocumentationContractTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; 17 focused tests ran with 0 failures, 0 errors, and 0 skipped after the final readability cleanup.
  - `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=MCPResourceDescriptorTest,MCPDescriptorCatalogLoaderTest,ResourceHandlerRegistryTest,MCPResourceSpecificationFactoryTest,MCPToolSpecificationFactoryTest,MCPDocumentationContractTest,StreamableHttpMCPServletTest,HttpTransportContractE2ETest,HttpTransportSecurityE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; 89 targeted API/support/core/bootstrap/e2e tests ran with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpTransportSecurityE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; `HttpTransportSecurityE2ETest` ran 7 tests with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,test/e2e/mcp -Pcheck -DskipTests checkstyle:check`
    exited `0`; 0 Checkstyle violations across currently dirty MCP modules.
  - `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,test/e2e/mcp -Pcheck -DskipTests spotless:check`
    exited `0`; Spotless reported all checked Java and POM files clean after `spotless:apply`.
  - `git diff --check`
    exited `2`; failures are Spotless-required Java blank-line indentation in changed files. `git -c core.whitespace=-blank-at-eol diff --check` exited `0`.
  - `rg` scans for password-format elicitation, sample cleartext docs secrets, and local `final` variables in changed files found no remaining unsafe matches.
  - `git branch --show-current`
    exited `0` and returned `001-shardingsphere-mcp`.

### 2026-05-14: T040/T041/T042/T043/T044/T045 Runtime Evidence Closure

- Source: MCP lifecycle, resources, completion, and Streamable HTTP session behavior are locked to official MCP Specification `2025-11-25`.
- Code: HTTP programmatic and interaction-client helpers now send `notifications/initialized` after successful initialize and assert HTTP `202`.
- Code: URI parsing now decodes UTF-8 path segments with malformed-encoding rejection, preserves literal `+`, and rejects raw unexpanded template markers.
- Code: completion descriptors now use standard prompt references only for declared prompt arguments and resource references for resource-template variables.
- Code: workflow plan lookup now rejects plan IDs owned by another MCP session before validate/apply/continue paths run.
- Tests: added product E2E for encoded URI segments, malformed URI encoding, unexpanded resource templates, metadata/resource/prompt completion, algorithm completion, plan-id completion,
  session DELETE isolation, transaction session isolation, DELETE rollback cleanup, and cross-session workflow approval rejection.
- Golden contracts: updated capability and tools/prompts/completion snapshots to reflect declared encrypt prompt arguments and resource-template completion targets.
- Verification:
  - `./mvnw -pl mcp/support -am -DskipITs -Dspotless.skip=true -Dtest=MCPDescriptorCatalogLoaderTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `1` before the descriptor validator fixture was corrected; rerun evidence below exited `0`.
  - `./mvnw -pl mcp/support,mcp/core -am -DskipITs -Dspotless.skip=true -Dtest=MCPUriTemplateUtilsTest,MCPUriPatternTest,InMemoryWorkflowSessionContextTest,WorkflowExecutionToolHandlerTest,WorkflowValidationToolHandlerTest,MCPDescriptorCatalogLoaderTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; 42 focused support/core tests ran with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=MetadataDiscoveryE2ETest,HttpTransportCompletionE2ETest,HttpTransportSessionLifecycleE2ETest,ExecuteQueryTransactionE2ETest,HttpTransportApprovalSafetyE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `1` before the cross-session workflow plan defect was fixed; rerun exited `0`.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=MetadataDiscoveryE2ETest,HttpTransportCompletionE2ETest,HttpTransportSessionLifecycleE2ETest,ExecuteQueryTransactionE2ETest,HttpTransportApprovalSafetyE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; the runtime-evidence E2E slice passed after session-owned workflow plan resolution was added.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpProductionProxyEncryptWorkflowE2ETest,HttpProductionProxyMaskWorkflowE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0`; 18 production Proxy workflow E2E tests ran with 0 failures, 0 errors, and 0 skipped.
  - `./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=HttpTransportGoldenContractE2ETest,HttpTransportContractE2ETest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `1` before golden snapshots were updated, then exited `0` after updating `capabilities.yaml` and `tools-prompts-completion.yaml`.
  - `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,test/e2e/mcp spotless:apply -Pcheck`
    exited `0`; Spotless restored the repository's expected Java blank-line formatting after whitespace review.
  - `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,test/e2e/mcp -DskipTests checkstyle:check -Pcheck`
    exited `1` before Javadoc and variable-distance cleanup, then exited `0` with 0 Checkstyle violations.
  - `./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,test/e2e/mcp -DskipTests spotless:check -Pcheck`
    exited `0`; all touched Java and POM files matched Spotless formatting.
  - `./mvnw -pl mcp/support,mcp/core -am -DskipITs -Dspotless.skip=true -Dtest=MCPUriTemplateUtilsTest,MCPUriPatternTest,InMemoryWorkflowSessionContextTest,WorkflowExecutionToolHandlerTest,WorkflowValidationToolHandlerTest,MCPDescriptorCatalogLoaderTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0` after the final Checkstyle cleanup.
  - `./mvnw -pl mcp/support -am -DskipITs -Dspotless.skip=true -Dtest=MCPUriTemplateUtilsTest -Dsurefire.failIfNoSpecifiedTests=false test`
    exited `0` after the final readability cleanup.
  - `git -c core.whitespace=-blank-at-eol diff --check`
    exited `0`; the adjusted whitespace check matches the repository's Spotless blank-line convention.
- Residual risks: package 015 remains owner for deeper completion provider dispatch/API generalization; package 013 remains owner for global enum casing policy.
