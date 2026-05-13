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

# Tasks: MCP Standard and E2E Hardening

**Input**: Design documents from `.specify/specs/014-mcp-standard-and-e2e-hardening/`
**Prerequisites**: `spec.md`, `plan.md`, `checklists/requirements.md`
**Tests**: Required for every implementation task that changes Java, YAML descriptors, transport behavior, distribution scripts, or E2E fixtures.
**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel when write scopes are disjoint.
- **[Story]**: Which user story supports the task.
- Every implementation task names the primary file or package path.

## Phase 1: Setup and Evidence Inventory

- [x] T001 Record the mcp-builder review baseline in the Speckit specification.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/spec.md`
- [x] T002 Confirm no branch switch is allowed and no branch-changing Speckit command is used.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/checklists/requirements.md`
- [x] T003 [P] Inventory all current public tool names and annotations.
  Paths: `mcp/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [x] T004 [P] Inventory every tool declaring `outputSchema` and representative success/error payloads.
  Paths: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`,
  `mcp/features/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`,
  `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`
- [x] T005 [P] Inventory E2E running-mode PR path filters, enablement flags, required services, current commands, and current durations.
  Paths: `test/e2e/mcp/pom.xml`, `test/e2e/mcp/src/test/resources/env/e2e-env.properties`
- [x] T006 [P] Inventory distribution startup and release manifest paths.
  Paths: `distribution/mcp/`, `mcp/server.json`, `test/e2e/mcp/src/test/java/`

**Checkpoint**: Every gap from the baseline review has a source path and an owner before implementation starts.

---

## Phase 2: Foundational Decisions

- [x] T010 [US1] Define the `database_gateway_` preferred tool naming policy with no old-name alias handling.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/plan.md`
- [x] T011 [US1] Define complete `execution.taskSupport` cleanup because MCP Tasks are experimental and out of scope.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`
- [x] T012 [US4] Define the structured-output-only cleanup policy that removes any `response_format` or Markdown option from code, descriptors, tests, and docs.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/plan.md`
- [x] T013 [US2] Define release validation for `mcp/server.json` snapshot metadata and package transports.
  Paths: `mcp/server.json`, `test/e2e/mcp/src/test/java/`
- [x] T014 [US3] Define the PR-gated E2E evidence matrix for all MCP running modes and MCP-related path filters.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/plan.md`

**Checkpoint**: Naming, execution metadata, response format, release validation, and E2E evidence policies are decided.

---

## Phase 3: User Story 1 - Accurate Tool Semantics

**Goal**: MCP clients can trust tool names, annotations, schemas, and execution metadata.
**Independent Test**: Tool-list contract and descriptor tests prove the exposed semantics match runtime side effects.

### Tests for User Story 1

- [x] T020 [P] [US1] Add descriptor tests proving planning tools are not read-only or idempotent when they create workflow state.
  Paths: `mcp/features/encrypt/src/test/java/`, `mcp/features/mask/src/test/java/`, `mcp/support/src/test/java/`
- [x] T021 [P] [US1] Add golden or contract tests for the `database_gateway_` public tool naming policy.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`
- [x] T022 [P] [US1] Add contract tests proving `execution.taskSupport` is removed from production descriptor output and model-contract snapshots.
  Path: `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/`

### Implementation for User Story 1

- [x] T023 [US1] Correct `database_gateway_plan_encrypt_rule` annotations to match workflow plan side effects.
  Path: `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`
- [x] T024 [US1] Correct `database_gateway_plan_mask_rule` annotations to match workflow plan side effects.
  Path: `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`
- [x] T025 [US1] Apply the `database_gateway_` preferred tool naming policy and remove old generic names without compatibility aliases.
  Paths: `mcp/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`, `mcp/README.md`
- [x] T026 [US1] Remove `execution.taskSupport` DTOs, YAML fields, validation keys, payload builders, transport adapter fields, test fixtures, and model-contract snapshots.
  Paths: `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/`,
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/`,
  `test/e2e/mcp/src/test/resources/golden/model-contract/`

**Checkpoint**: Public tool descriptors no longer mislead clients about safety or supported protocol fields.

---

## Phase 4: User Story 2 - Distribution and Release Readiness

**Goal**: Release artifacts are proven through the same paths users run.
**Independent Test**: Packaged scripts, Docker entrypoint, and manifest validation have current pass/fail evidence.

### Tests for User Story 2

- [x] T030 [P] [US2] Add packaged distribution tests that start through `bin/start.sh` or the platform-equivalent script.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/`
- [x] T031 [P] [US2] Add Docker entrypoint or equivalent registry package smoke coverage for HTTP and STDIO transport selection.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/`
- [x] T032 [P] [US2] Add release manifest validation that rejects snapshot versions and snapshot package identifiers.
  Paths: `mcp/server.json`, `test/e2e/mcp/src/test/java/`

### Implementation for User Story 2

- [x] T033 [US2] Update packaged distribution process support to exercise script-level startup where possible.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/fixture/support/PackagedDistributionProcessSupport.java`
- [x] T034 [US2] Add or update Docker entrypoint smoke fixture support.
  Paths: `distribution/mcp/src/main/bin/docker-entrypoint.sh`, `distribution/mcp/Dockerfile`, `test/e2e/mcp/src/test/java/`
- [x] T035 [US2] Add release-time manifest validation command or test.
  Paths: `mcp/server.json`, `test/e2e/mcp/src/test/java/`

**Checkpoint**: Distribution evidence proves shipped startup and release metadata, not only classpath-equivalent Java startup.

---

## Phase 5: User Story 3 - Complete E2E Evidence

**Goal**: Reviewers can verify every MCP E2E running mode in PR when MCP-related paths change.
**Independent Test**: The E2E matrix maps every running mode to path filter, PR job, command, enablement flag, artifact, and timeout budget.

### Tests for User Story 3

- [x] T040 [P] [US3] Add schema validation tests for representative `structuredContent` payloads of every tool declaring `outputSchema`.
  Paths: `mcp/core/src/test/java/`, `mcp/features/*/src/test/java/`, `mcp/support/src/test/java/`
- [x] T041 [P] [US3] Add or strengthen MySQL, STDIO, distribution, Docker or registry smoke, remote HTTP, and live LLM PR command documentation and smoke assertions.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/`
- [x] T042 [P] [US3] Add remote HTTP security coverage for token, origin, protocol-version, session, and DELETE behavior.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/programmatic/`
- [x] T043 [P] [US3] Add live LLM evidence requirements, artifact names, and pass/fail thresholds.
  Path: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/`

### Implementation for User Story 3

- [x] T044 [US3] Validate returned `structuredContent` against declared `outputSchema` before exposing tool results.
  Paths: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/`,
  `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/`
- [x] T045 [US3] Add or update E2E evidence matrix documentation for PR path filters, running-mode defaults, commands, artifacts, and duration budgets.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/plan.md`
- [x] T046 [US3] Add timeout and artifact expectations for MySQL, STDIO, distribution, Docker or registry smoke, remote HTTP, and live LLM running modes.
  Paths: `test/e2e/mcp/pom.xml`, `test/e2e/mcp/src/test/resources/env/e2e-env.properties`, `test/e2e/mcp/src/test/java/`
- [x] T047 [US3] Configure PR path filtering so MCP E2E jobs run only when MCP-related paths change.
  Paths: `.github/workflows/`, `test/e2e/mcp/`

**Checkpoint**: E2E realism gaps have repeatable commands, artifacts, and pass/fail thresholds.

---

## Phase 6: User Story 4 - Stable Structured Outputs

**Goal**: Operators and clients get stable JSON without alternate response formats.
**Independent Test**: Representative tools preserve JSON contracts and do not expose `response_format` or Markdown options.

### Tests for User Story 4

- [x] T050 [P] [US4] Add tests proving tool input schemas and descriptors do not expose `response_format` or Markdown-specific output options.
  Paths: `mcp/core/src/test/java/`, `mcp/bootstrap/src/test/java/`
- [x] T051 [P] [US4] Add pagination boundary tests for `database_gateway_search_metadata` default size, maximum size, empty page, and next-page token.
  Path: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/`

### Implementation for User Story 4

- [x] T052 [US4] Remove any `response_format` or Markdown response-format implementation, test fixture, README text, and Speckit task that treats Markdown as supported or planned behavior.
  Paths: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`,
  `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/`
- [x] T053 [US4] Reduce default `database_gateway_search_metadata` page size to the agreed context-budget value or document measured justification.
  Path: `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`

**Checkpoint**: Human-readable output is additive, compact, and subordinate to the structured JSON contract.

---

## Phase 7: Verification and Handoff

- [x] T060 Run scoped module tests for every touched MCP module.
  Command: `./mvnw -pl <module> -DskipITs -Dspotless.skip=true -Dtest=<ClassName> -Dsurefire.failIfNoSpecifiedTests=false test`
- [x] T061 Run scoped Checkstyle or Spotless gates for touched modules.
  Commands: `./mvnw -pl <module> -am -Pcheck checkstyle:check`, `./mvnw spotless:apply -Pcheck -pl <module>`
- [x] T062 Run every MCP E2E running mode in PR when MCP-related paths change.
  Command: `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true test -B -ntp`
- [x] T063 Record artifacts and elapsed time for every PR-gated MCP E2E running mode.
  Paths: `test/e2e/mcp/target/`, `.specify/specs/014-mcp-standard-and-e2e-hardening/`
- [x] T064 Update Speckit evidence after each completed implementation slice.
  Path: `.specify/specs/014-mcp-standard-and-e2e-hardening/`

**Final Checkpoint**: Every requirement has passing evidence, no branch switch occurred, and no unrelated dirty files were reverted.
