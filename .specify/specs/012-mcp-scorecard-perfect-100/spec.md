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

# Feature Specification: MCP Scorecard Perfect 100

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-11
**Status**: Draft
**Input**: User requests a Speckit requirement package for the latest MCP and MCP E2E review, with every scored item required to reach 100/100, and no git branch switch during the work.

## Goal

Turn the latest independent score review into a strict Speckit requirement package.
The target is not an average score of 100. Every individual score dimension for the MCP production modules and MCP E2E module MUST reach 100/100.

The active scope covers:

- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/features/encrypt`
- `mcp/features/mask`
- `mcp/bootstrap`
- `distribution/mcp`
- `test/e2e/mcp`
- Model-facing descriptors, prompts, runtime diagnostics, safety contracts, scorecards, and documentation that affect these modules

## Baseline

The latest independent review on 2026-05-11 scored:

- MCP production modules: **87.5/100** average.
- MCP E2E module: **86.3/100** average.
- Equal-weight combined checkpoint: **86.9/100**.

These averages are diagnostic only. Delivery is complete only when every listed dimension reaches 100/100 with evidence.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Speckit files are maintained manually because the standard create-feature script changes branches.
- The score dimensions in this package are locked for the current checkpoint.
  Adding, removing, or renaming a dimension requires an explicit user request.
- No score may be marked 100 based on prose-only reasoning.
- Each 100 score requires automated command output, artifact evidence, or a reviewed contract snapshot.
- Waivers and exception records are not allowed for scoring or completion.
  If evidence is missing, the dimension remains below 100.
- Existing historical Speckit package `011-mcp-llm-product-quality-100` remains historical evidence and is not reused as the current score decision.
- Repository rules from `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md` remain binding.

## User Scenarios & Testing

### User Story 1 - Maintainer Closes Every MCP Production Score Gap (Priority: P1)

A maintainer reads the latest MCP production module scorecard and can see each dimension that is below 100, the exact reason it is below 100, and the evidence required to move it to 100.

**Why this priority**: The production MCP modules define the model-facing product surface, runtime safety, diagnostics, architecture, and maintainability.

**Independent Test**: Review `scorecard.md` and verify every production dimension has a current score, target score, gap reason, and 100-point exit gate.

**Acceptance Scenarios**:

1. **Given** a production dimension such as code readability, **When** the maintainer opens the scorecard,
   **Then** the scorecard shows the current score, the below-100 reason, and the concrete verification gate.
2. **Given** a dimension reaches 100, **When** the maintainer updates evidence, **Then** the scorecard records the command or artifact that proves the gap is closed.
3. **Given** a dimension still has a known caveat, **When** the scorecard is evaluated, **Then** the dimension remains below 100.

---

### User Story 2 - Maintainer Closes Every MCP E2E Score Gap (Priority: P1)

A maintainer reads the MCP E2E scorecard and can distinguish deterministic contract gaps, live LLM gaps, environment stability gaps, and performance-cost gaps.

**Why this priority**: E2E evidence is the proof that the MCP product surface works for protocol clients, packaged runtimes, and real model workflows.

**Independent Test**: Review `scorecard.md` and verify every E2E dimension has a current score, target score, gap reason, and gate that can be validated independently.

**Acceptance Scenarios**:

1. **Given** the live LLM lane is optional by default, **When** scoring model friendliness, **Then** the score stays below 100 until the opt-in gate has repeatable artifact evidence.
2. **Given** MySQL, STDIO, distribution, or LLM lanes are disabled by default, **When** scoring E2E realism, **Then** the score stays below 100 until the required lane evidence is recorded.
3. **Given** Docker or model variance affects a scenario, **When** stability is scored, **Then** the scorecard records deterministic mitigation or keeps the score below 100.

---

### User Story 3 - Reviewer Verifies That 100 Means Full Closure (Priority: P1)

A reviewer can reject any claimed 100 score unless the dimension has no active caveat, has an explicit gate, and has evidence that the gate passed.

**Why this priority**: The user explicitly requires every item to be 100. Averaging, partial credit, and aspirational scoring are not acceptable.

**Independent Test**: Run the Speckit checklist and confirm no requirement allows average-based completion.

**Acceptance Scenarios**:

1. **Given** a dimension has a current score below 100, **When** no new evidence exists, **Then** it cannot be marked complete.
2. **Given** a dimension is declared not applicable, **When** dimensions are locked, **Then** the declaration is invalid.
3. **Given** a summary says the average is 100, **When** any dimension is below 100, **Then** the feature remains incomplete.

---

### User Story 4 - Operator Receives a Clear Implementation Backlog (Priority: P2)

An implementer can convert the scorecard into small, reviewable work items without mixing unrelated refactors, tests, and documentation updates.

**Why this priority**: The current gaps span model UX, architecture, runtime safety, E2E stability, and verification cost.

**Independent Test**: Review `tasks.md` and confirm each task maps to one or more score gaps and can be completed without switching branches.

**Acceptance Scenarios**:

1. **Given** a score gap is caused by a large class, **When** tasks are generated, **Then** the work is split into bounded refactoring tasks and scoped tests.
2. **Given** a score gap is caused by default-disabled E2E lanes, **When** tasks are generated, **Then** the work defines deterministic opt-in gate evidence and a repeatable command.
3. **Given** a task requires external runtime evidence, **When** the task is listed, **Then** the task records the required environment and fallback risk.

### Edge Cases

- If a historical Speckit file claims 100 while the latest review finds gaps, the latest review governs the new package.
- Existing project PR gates are not changed by this package.
  Optional lanes can still be mandatory manual or opt-in evidence for the 100 score.
- If a gap cannot be automated, the score remains below 100 until manual evidence is documented.
- If a branch-changing Speckit command is the normal path, this package uses manual Speckit files instead.

## Requirements

### Functional Requirements

- **MSP-FR-001**: The scorecard MUST define one independent 100-point gate for every production MCP score dimension.
- **MSP-FR-002**: The scorecard MUST define one independent 100-point gate for every MCP E2E score dimension.
- **MSP-FR-003**: A dimension MUST NOT be marked 100 while any known caveat from the latest review remains open.
- **MSP-FR-004**: The final completion decision MUST use all-dimensions-full-score semantics, not average-score semantics.
- **MSP-FR-005**: Each score gap MUST have a concrete exit gate expressed as command evidence, artifact evidence, or contract evidence.
- **MSP-FR-006**: Waivers and exception records MUST NOT be used.
  Deferred evidence remains an open risk and keeps the dimension below 100.
- **MSP-FR-007**: Production module gates MUST cover model use friendliness, natural interaction, clarity, readability,
  architecture, decoupling, protocol correctness, stability, diagnostics, safety, extensibility, performance,
  packaging, compatibility, and tests.
- **MSP-FR-008**: E2E module gates MUST cover model use friendliness, natural interaction, clarity, readability,
  architecture, decoupling, protocol correctness, end-to-end realism, stability, diagnostics, safety, extensibility,
  performance, packaging, compatibility, and test quality.
- **MSP-FR-009**: The backlog MUST preserve the user constraint that no branch switch or branch-changing Speckit command is allowed.
- **MSP-FR-010**: The backlog MUST avoid broad speculative rewrites and split work into small slices tied to score gaps.
- **MSP-FR-011**: Any production code change that claims a score improvement MUST include scoped unit or integration tests and the relevant Checkstyle or Spotless gate.
- **MSP-FR-012**: Any E2E score improvement MUST include deterministic contract tests, opt-in runtime evidence, or LLM artifact evidence as appropriate to the dimension.
- **MSP-FR-013**: Safety-related score improvements MUST preserve explicit preview and approval boundaries for side-effecting SQL and workflows.
- **MSP-FR-014**: Model-facing score improvements MUST keep public payloads compact, canonical, versioned or snapshot-protected, and recoverable by machines.
- **MSP-FR-015**: Architecture and decoupling score improvements MUST reduce concrete coupling, class size, static state, or hardcoded extension boundaries.
- **MSP-FR-016**: Stability and performance score improvements MUST use bounded waits, repeatable environments, measured command output, or documented resource budgets.
- **MSP-FR-017**: The repo-visible handoff under `specs/008-mcp-scorecard-perfect-100/` MUST summarize this Speckit package for reviewers who do not inspect `.specify/`.

### Key Entities

- **Score Dimension**: One independently scored capability area with current score, target score, gap reason, and exit gate.
- **Score Gap**: A specific reason the current dimension does not reach 100.
- **Exit Gate**: The command, artifact, or contract needed before the dimension can become 100.
- **Evidence Record**: A timestamped verification result, artifact path, or reviewed contract snapshot.
- **Open Risk**: A missing or deferred proof point that keeps a dimension below 100 until it is resolved.

## Success Criteria

### Measurable Outcomes

- **MSP-SC-001**: Every MCP production dimension in `scorecard.md` has target score `100/100`.
- **MSP-SC-002**: Every MCP E2E dimension in `scorecard.md` has target score `100/100`.
- **MSP-SC-003**: No dimension can be marked complete without an exit gate and evidence record.
- **MSP-SC-004**: `tasks.md` contains at least one actionable task for every dimension whose baseline score is below 100.
- **MSP-SC-005**: The requirements checklist has no failed item before implementation planning begins.
- **MSP-SC-006**: Final completion cannot be claimed while any score dimension remains below 100.

## Assumptions

- The latest independent review baseline is the scoring source for this package.
- Historical 100-point packages remain useful evidence but do not override the latest baseline.
- Live LLM E2E can remain outside the existing PR gate while still being mandatory manual or opt-in evidence for a perfect score.
- H2 evidence is required for fast deterministic coverage; MySQL, STDIO, packaged distribution, and live LLM evidence are required where a dimension depends on real runtime behavior.
