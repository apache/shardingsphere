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

# Feature Specification: MCP Encrypt/Mask Scoped Scorecard 100

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-16
**Status**: Draft
**Input**: User description: "Use Speckit to restate the scoped requirements and split concrete tasks to raise every score dimension to 100, without switching branches."

## Scope Calibration

This package supersedes older scorecard planning for the current checkpoint only. Historical Speckit packages remain evidence sources, but they do not close this package automatically.

### In Scope

- MCP protocol conformance for revision `2025-11-25` only.
- MCP Java SDK version fixed at `1.1.2`; no upgrade, dependency drift, or SDK migration work.
- Protocol behavior that SDK `1.1.2` can express through current ShardingSphere MCP code paths.
- Encrypt and mask workflow completeness only.
- Readability-first implementation elegance: minimal abstractions, clear structure, no broad framework rewrites.
- Current branch must remain `001-shardingsphere-mcp`; branch-changing commands are forbidden.

### Non-Goals

- MCP `icons` and `Tool.execution` support, because this project does not need them under the fixed SDK boundary.
- Compatibility proof for protocol revisions other than `2025-11-25`.
- Sharding, readwrite-splitting, shadow, traffic governance, mode governance, observability, and general administration workflows.
- SDK version upgrade, dependency version change, package-management changes, or global environment changes.
- Data migration, historical data backfill, rollback orchestration, or persistent audit storage.
- Over-designed shared frameworks that make encrypt/mask code harder to read.

## User Scenarios & Testing

### User Story 1 - Freeze the Scoped Score Contract (Priority: P1)

As a reviewer, I need a current score contract that reflects the narrowed scope so that irrelevant protocol fields, SDK upgrades, and non-encrypt/mask features do not remain hidden blockers.

**Why this priority**: All downstream tasks depend on the same scoring rules.

**Independent Test**: Read `scorecard.md` and verify every dimension has a current score, target score, scoped gap, and closing evidence rule.

**Acceptance Scenarios**:

1. **Given** SDK `1.1.2` is fixed, **When** protocol conformance is scored, **Then** SDK-unexposed fields such as icons are non-goals rather than implementation gaps.
2. **Given** functional completeness is scoped to encrypt/mask, **When** the scorecard is reviewed,
   **Then** sharding, readwrite-splitting, shadow, traffic, mode, and observability are not counted against the score.

---

### User Story 2 - Prove Scoped MCP Protocol Correctness (Priority: P1)

As an MCP client integrator, I need the declared `2025-11-25` resource, tool, prompt, completion, and transport behavior to be correct and repeatable under SDK `1.1.2`.

**Why this priority**: Protocol correctness is a release gate for every MCP client path.

**Independent Test**: Run focused bootstrap and contract tests for declared methods, protocol headers, structured content, output schema validation, and HTTP session behavior.

**Acceptance Scenarios**:

1. **Given** a `2025-11-25` client, **When** it lists and calls declared MCP primitives, **Then** ShardingSphere MCP returns only supported capabilities with schema-conforming payloads.
2. **Given** a tool declares an output schema, **When** it returns data, **Then** `structuredContent` conforms to that schema and a serialized JSON text fallback is present.

---

### User Story 3 - Complete Encrypt and Mask Workflows (Priority: P1)

As a ShardingSphere-Proxy operator, I need encrypt and mask workflows to support discovery, planning, preview, approval-gated apply, validation, and recovery guidance.

**Why this priority**: Functional completeness is now judged only on encrypt and mask.

**Independent Test**: Run focused unit and product-path E2E tests for encrypt/mask resources, prompts, completions, plan tools, workflow apply, validation, and recovery payloads.

**Acceptance Scenarios**:

1. **Given** an encrypt request for a logical table column, **When** the workflow is previewed and approved,
   **Then** generated physical DDL, DistSQL, final names, validation layers, and next actions are visible and deterministic.
2. **Given** a mask request for a logical table column, **When** create, alter, or drop is planned,
   **Then** the plan, approval gate, validation result, and recovery guidance are consistent and model-friendly.

---

### User Story 4 - Raise Code, Architecture, and Test Quality (Priority: P1)

As a committer, I need the MCP modules to satisfy repository standards for readable code, public-API tests, mocking discipline, style gates, and evidence-backed coverage.

**Why this priority**: The score cannot reach 100 if the code works but violates local engineering rules.

**Independent Test**: Run static searches, focused tests, Checkstyle, Spotless, and Jacoco reports for touched modules.

**Acceptance Scenarios**:

1. **Given** a test touches static behavior, **When** the test is reviewed, **Then** it uses the project-preferred mocking approach or documents a bounded try-with-resources exception.
2. **Given** a workflow branch exists, **When** the coverage matrix is reviewed, **Then** exactly one scenario owns that branch unless an unreachable branch is documented.

---

### User Story 5 - Provide Operational and Performance Evidence (Priority: P2)

As a release reviewer, I need repeatable E2E, distribution, and performance evidence for the declared encrypt/mask MCP scope.

**Why this priority**: A scorecard at 100 requires product-path proof, not only unit tests.

**Independent Test**: Run default E2E, documented opt-in lanes, distribution smoke, and performance-budget tests where infrastructure is available.

**Acceptance Scenarios**:

1. **Given** a default developer environment, **When** the default MCP E2E lane runs, **Then** HTTP, H2, approval, completion, and resource behavior are verified.
2. **Given** optional infrastructure is available, **When** opt-in lanes run, **Then** Proxy, MySQL, STDIO, distribution, and LLM evaluation evidence is recorded separately from default-lane evidence.

## Edge Cases

- Older Speckit files claim 100/100 under broader or different scoring rules; this package treats them as historical evidence only.
- SDK `1.1.2` cannot expose some optional protocol descriptor fields; those fields remain non-goals for this checkpoint.
- Existing code may still contain compatibility support for other protocol revisions; this package only requires tests and documentation for `2025-11-25`.
- LLM score evidence must not depend on external model credentials or an operator-managed model endpoint.
- External LLM endpoints are allowed only as an explicit debug mode and cannot close scorecard evidence.
- Docker/Testcontainers can provide the local opt-in runtime for MySQL, Proxy workflow, Docker image STDIO, and Ollama-backed LLM lanes.
- LLM opt-in must start a Docker-owned Ollama runtime and may pull `ollama/ollama:0.23.1` and `qwen3:1.7b` online when local caches are empty.
- Performance optimizations must not reduce readability or introduce broad abstractions.

## Requirements

### Functional Requirements

- **FR-001**: The current branch MUST remain `001-shardingsphere-mcp`; `git switch`, `git checkout`, branch creation scripts, and branch-changing Speckit commands MUST NOT be used.
- **FR-002**: The active scorecard MUST use ten dimensions, each with target `100/100`, current score, gap summary, owner paths, and closing evidence.
- **FR-003**: MCP protocol scoring MUST be limited to revision `2025-11-25` and SDK `1.1.2`-expressible behavior.
- **FR-004**: MCP `icons`, `Tool.execution`, SDK upgrades, and non-`2025-11-25` compatibility work MUST NOT be required for a 100 protocol score.
- **FR-005**: Functional completeness MUST be limited to encrypt and mask workflows.
- **FR-006**: Encrypt/mask workflows MUST cover resource discovery, prompt guidance, completion hints, planning, preview, approval-gated apply, validation, and recovery guidance.
- **FR-007**: Side-effecting workflow operations MUST require explicit preview and approval before execution.
- **FR-008**: Tool responses that declare `outputSchema` MUST return schema-conforming `structuredContent` and serialized JSON text fallback.
- **FR-009**: Implementation elegance MUST prioritize readable, minimal, locally useful abstractions over broad framework extraction.
- **FR-010**: Tests MUST comply with repository rules for public API coverage, mocking, reflection limits, parameterized names, and precise assertions.
- **FR-011**: Every score increase MUST be backed by command output, source-backed evidence, artifact evidence, or an explicit non-goal decision.
- **FR-012**: Documentation MUST align README, Speckit scorecard, descriptor behavior, and validator rules for the scoped target.
- **FR-013**: Performance and reliability closure MUST include budgets for descriptor loading, workflow planning, metadata/resource operations, default E2E, and distribution smoke.
- **FR-014**: For touched production classes, public methods and reachable branches MUST be covered through public APIs and varied inputs.
- **FR-015**: Tests MUST NOT invoke private methods by reflection, and production methods MUST NOT be made public only for tests.
- **FR-016**: Direct static or constructor mocking MUST be reviewed case by case; migrate only when it improves readability or reduces leak risk.
- **FR-017**: Opt-in verification lanes MUST be documented separately from default required lanes and must not block default-lane closure when external infrastructure is unavailable.
- **FR-018**: Opt-in lanes SHOULD run locally through Docker/Testcontainers when Docker, network, and machine resources are available.
- **FR-019**: Cross-model second opinion MUST use Codex CLI when invoked, after confirming the exact read-only command.
- **FR-020**: LLM score evidence MUST use a Docker-owned Ollama runtime started by the E2E support layer.
- **FR-021**: LLM score evidence MUST NOT require `MCP_LLM_BASE_URL`, `MCP_LLM_API_KEY`, or a pre-running external model service.
- **FR-022**: External LLM endpoints MAY remain available only through an explicit debug mode and MUST NOT count as score-closing evidence.
- **FR-023**: LLM score evidence MUST use `ollama/ollama:0.23.1`, not `latest`, and SHOULD record the resolved image digest.

### Key Entities

- **Score Dimension**: One of the ten active quality dimensions, with current score, target score, gaps, owner paths, and evidence requirements.
- **Closing Evidence**: A command result, source map, test artifact, E2E artifact, Jacoco report, style gate, or explicit non-goal decision.
- **Workflow Capability**: An encrypt or mask capability spanning resources, prompts, completions, tools, approval, validation, and recovery.
- **Evidence Lane**: A verification route such as unit tests, default H2/HTTP E2E, Proxy/MySQL opt-in E2E, STDIO, distribution smoke, LLM evaluation, or performance budget.
- **Docker-Owned LLM Runtime**: An Ollama container started and owned by the E2E support layer for the duration of the LLM lane.

## Success Criteria

### Measurable Outcomes

- **SC-001**: All ten dimensions in `scorecard.md` are `100/100` with linked closing evidence.
- **SC-002**: Scoped Maven tests pass for `mcp/support`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, `mcp/bootstrap`, and `test/e2e/mcp` as applicable.
- **SC-003**: Checkstyle and Spotless pass for every touched Java module.
- **SC-004**: Jacoco evidence exists for modules whose score depends on branch or method coverage.
- **SC-005**: The mcp-builder evaluation artifact contains ten read-only, independent, complex, realistic, verifiable, and stable questions.
- **SC-006**: README and Speckit documents state the narrowed protocol and feature scope without presenting non-goals as future blockers.
- **SC-007**: `git branch --show-current` reports `001-shardingsphere-mcp` at final verification.
- **SC-008**: LLM smoke and usability score evidence records Docker-owned Ollama runtime usage rather than external endpoint reuse.

## Assumptions

- Current user-accepted baseline is the 2026-05-16 scoped reassessment: overall `88/100`.
- Existing MCP Java SDK dependency remains `1.1.2`.
- Official MCP revision `2025-11-25` is the only protocol revision requiring coverage in this package.
- Encrypt drop remains out of V1 scope unless the user explicitly changes the product boundary.
- The user approved the recommended execution order on 2026-05-16.
- The user authorized a fresh-context doubt-driven review before the next implementation round.
- The user selected Codex CLI for cross-model second opinion.
