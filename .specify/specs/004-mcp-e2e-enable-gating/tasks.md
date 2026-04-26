# Tasks: MCP E2E Property-Based Enablement

**Input**: Design documents from `/.specify/specs/004-mcp-e2e-enable-gating/`
**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`
**Tests**: Add or update focused selector coverage and run workflow-equivalent Maven commands for the default baseline and representative heavy lanes.

**Organization**: Tasks are grouped by user story so MCP E2E lane governance can be implemented and verified in controlled slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Code Cut Points

- **Rewrite around shared lane gating**
  - `test/e2e/mcp/src/test/resources/env/e2e-env.properties`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/MetadataDiscoveryE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/ExecuteQueryTransactionE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionH2RuntimeSmokeE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionMultiDatabaseE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionMySQLRuntimeSmokeE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/StdioProductionH2RuntimeSmokeE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/StdioProductionMultiDatabaseE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/StdioProductionMySQLRuntimeSmokeE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyEncryptWorkflowE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyMaskWorkflowE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionSmokeE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/smoke/LLMSmokeE2ETest.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/LLMUsabilitySuiteE2ETest.java`
- **New infrastructure expected**
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ETestConfiguration.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ECondition.java`
  - `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ETestConfigurationTest.java`
- **Workflow updates**
  - `.github/workflows/jdk17-subchain-ci.yml`
  - `.github/workflows/mcp-build.yml`
  - `.github/workflows/mcp-llm-e2e.yml`
  - `.github/workflows/mcp-llm-usability-e2e.yml`

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Create `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ETestConfiguration.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ECondition.java` to centralize lane defaults, `EnvironmentPropertiesLoader` integration, and shared lane predicates.
- [ ] T002 Expand `test/e2e/mcp/src/test/resources/env/e2e-env.properties` with the documented `mcp.e2e.<lane>.enabled` defaults while preserving `e2e.timezone`.
- [ ] T003 [P] Add focused configuration coverage in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ETestConfigurationTest.java` for file defaults, JVM overrides, and the composite STDIO-plus-MySQL decision.

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Implement shared lane-predicate methods in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ECondition.java` for `contract`, `production.h2`, `production.mysql`, `production.stdio`, `proxy.workflow`, `distribution`, `llm`, and the composite STDIO-plus-MySQL case.
- [ ] T005 Remove remaining test-selection reads of `mcp.distribution.smoke.enabled` and `mcp.llm.e2e.enabled` from the shared MCP E2E entry points in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionSmokeE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java`.

**Checkpoint**: One shared MCP E2E lane model exists, and no top-level selector still depends on the legacy property names.

---

## Phase 3: User Story 1 - Keep the default MCP E2E run lightweight and deterministic (Priority: P1)

**Goal**: The default `test/e2e/mcp` run executes only the lightweight contract and H2-backed baseline lanes.
**Independent Test**: Run the MCP module tests without any lane override and confirm only contract and HTTP H2 baseline classes participate.

### Implementation for User Story 1

- [ ] T010 [P] [US1] Gate `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/HttpTransportContractE2ETest.java`, `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/MetadataDiscoveryE2ETest.java`, and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/ExecuteQueryTransactionE2ETest.java` through the shared `contract` lane predicate.
- [ ] T011 [P] [US1] Gate `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionH2RuntimeSmokeE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionMultiDatabaseE2ETest.java` through the shared `production.h2` lane predicate.
- [ ] T012 [US1] Keep baseline selector behavior explicit by updating any class-local `isEnabled()` delegates and related test messages in the touched programmatic and HTTP H2 production classes.

**Checkpoint**: The default MCP E2E module run stays on the intended lightweight baseline with no implicit heavy-lane participation.

---

## Phase 4: User Story 2 - Enable only the heavy MCP lane that matters for the current change (Priority: P1)

**Goal**: A contributor can turn on one heavy lane without triggering unrelated heavy suites.
**Independent Test**: Run targeted Maven commands with one heavy-lane override at a time and verify only the intended heavy classes participate.

### Implementation for User Story 2

- [ ] T020 [P] [US2] Gate `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionMySQLRuntimeSmokeE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/StdioProductionMySQLRuntimeSmokeE2ETest.java` so the MySQL-backed HTTP path uses `production.mysql` and the STDIO-plus-MySQL path requires both `production.mysql` and `production.stdio`.
- [ ] T021 [P] [US2] Gate `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/StdioProductionH2RuntimeSmokeE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/StdioProductionMultiDatabaseE2ETest.java` through the shared `production.stdio` lane predicate.
- [ ] T022 [P] [US2] Gate `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyEncryptWorkflowE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/HttpProductionProxyMaskWorkflowE2ETest.java` through the shared `proxy.workflow` lane predicate.
- [ ] T023 [US2] Preserve Docker assumptions in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/AbstractProductionMySQLRuntimeSmokeE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/AbstractProductionProxyWorkflowE2ETest.java` only as secondary runtime guards after lane selection has already been decided.

**Checkpoint**: Heavy transport, backend, and proxy workflow suites become independently selectable instead of being coupled by hidden defaults.

---

## Phase 5: User Story 3 - Reuse the same enablement style as other ShardingSphere E2E modules (Priority: P1)

**Goal**: MCP E2E uses the same property-file-plus-condition pattern as other repository E2E modules.
**Independent Test**: Inspect the MCP module and confirm a reviewer can find one shared configuration entry point and one shared lane-condition layer.

### Implementation for User Story 3

- [ ] T030 [P] [US3] Replace `@EnabledIfSystemProperty` in `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/PackagedDistributionSmokeE2ETest.java` with the shared `distribution` lane predicate while keeping `mcp.distribution.home` as a separate artifact-location input.
- [ ] T031 [P] [US3] Rewrite `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java` so the top-level LLM selector uses `mcp.e2e.llm.enabled`, while preserving `mcp.llm.base-url`, `mcp.llm.model`, `mcp.llm.api-key`, `mcp.llm.ready-timeout-seconds`, `mcp.llm.request-timeout-seconds`, `mcp.llm.max-turns`, `mcp.llm.artifact-root`, and `mcp.llm.run-id`.
- [ ] T032 [P] [US3] Update `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/smoke/LLMSmokeE2ETest.java` and `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/LLMUsabilitySuiteE2ETest.java` so their assumption messages and top-level enable checks point at the new `llm` lane selector.
- [ ] T033 [US3] Remove all remaining references to `mcp.distribution.smoke.enabled` and `mcp.llm.e2e.enabled` under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/`.

**Checkpoint**: MCP E2E no longer has one-off distribution or LLM gate models; all suites participate through the same shared lane-selection style.

---

## Phase 6: User Story 4 - Let CI and specialized jobs opt into the right MCP lanes explicitly (Priority: P2)

**Goal**: MCP-related workflows state heavy-lane participation explicitly in their Maven commands.
**Independent Test**: Read each affected workflow and confirm the command line itself reveals whether distribution or LLM lanes are enabled.

### Implementation for User Story 4

- [ ] T040 [P] [US4] Update `.github/workflows/jdk17-subchain-ci.yml` and `.github/workflows/mcp-build.yml` so the general MCP module test steps rely on the default baseline and the packaged-distribution smoke steps pass `-Dmcp.e2e.distribution.enabled=true` instead of the legacy distribution selector.
- [ ] T041 [P] [US4] Update `.github/workflows/mcp-llm-e2e.yml` and `.github/workflows/mcp-llm-usability-e2e.yml` so the targeted LLM suite commands pass `-Dmcp.e2e.llm.enabled=true` explicitly.
- [ ] T042 [US4] Review affected MCP workflow command lines and related comments so heavy-lane intent is visible without relying on hidden defaults or legacy property names.

**Checkpoint**: Local and CI executions follow the same lane-selection model, and heavy-lane intent is obvious from workflow commands.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T050 [P] Run `./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false`, `./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true -Dtest=PackagedDistributionSmokeE2ETest -Dsurefire.failIfNoSpecifiedTests=true -Dmcp.e2e.distribution.enabled=true`, and `./mvnw -pl test/e2e/mcp test -DskipITs -Dspotless.skip=true -Dtest=LLMSmokeE2ETest -Dsurefire.failIfNoSpecifiedTests=true -Dmcp.e2e.llm.enabled=true` as the representative baseline and heavy-lane verification commands.
- [ ] T051 [P] Run the relevant scoped style and compile checks for touched areas, including the MCP E2E module and any workflow or message updates that need follow-up fixes.

## Dependencies & Execution Order

- Phase 1 must finish before lane-specific test rewrites begin.
- Phase 2 blocks all user stories because the shared lane predicates and legacy-selector cleanup must exist before suite-by-suite adoption.
- User Story 1 can proceed independently once the shared lane model exists.
- User Story 2 depends on the shared lane predicates from Phase 2 and should follow User Story 1 so the baseline-versus-heavy split is already established.
- User Story 3 depends on Phase 2 and can proceed in parallel with the later part of User Story 2 if file ownership is separated.
- User Story 4 depends on User Story 3 because CI commands should switch only after the new lane names are active in the test module.
- Polish runs last after all desired story phases are complete.
