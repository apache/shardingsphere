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

# Feature Specification: MCP Encrypt/Mask Scorecard 100

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-15
**Status**: Draft
**Input**: User description: "Make the agreed MCP and E2E score dimensions reach 100/100, using Speckit to manage requirements, without switching branches."

## User Scenarios & Testing

### User Story 1 - Govern the Current Score Baseline (Priority: P1)

As a reviewer, I need the current independent score baseline to be traceable so that historical 100/100 Speckit records do not hide reopened gaps.

**Why this priority**: Score movement must be evidence-driven. A dimension cannot become 100/100 because an old package said so.

**Independent Test**: Read `scorecard.md` and verify every current score, target score, gap, owner package, and closing evidence is explicit.

**Acceptance Scenarios**:

1. **Given** the current score baseline is 84/100, **When** a reviewer opens the scorecard, **Then** every dimension below 100 lists the exact evidence needed to reach 100.
2. **Given** older Speckit packages already contain historical 100/100 claims, **When** this package is used for current planning,
   **Then** historical claims are treated as reusable evidence only after current verification.

---

### User Story 2 - Complete Encrypt and Mask Workflow Quality (Priority: P1)

As an MCP operator, I need encrypt and mask planning, apply, and validation flows to be complete, safe, and model-friendly inside the declared V1 feature scope.

**Why this priority**: The functional completeness dimension is scoped to encrypt and mask only for this checkpoint.

**Independent Test**: Run focused unit and E2E tests for encrypt and mask descriptors, resources, prompts, completions, workflow planning, preview, apply, approval, and validation.

**Acceptance Scenarios**:

1. **Given** a logical table column requires encryption, **When** the model follows the encrypt prompt and calls the MCP tools,
   **Then** the workflow returns reviewable rule, DistSQL, physical DDL, next actions, validation layers, and approval-gated apply behavior.
2. **Given** a logical table column requires masking, **When** the model follows the mask prompt and calls the MCP tools,
   **Then** the workflow returns reviewable rule, DistSQL, next actions, validation layers, and approval-gated apply behavior.

---

### User Story 3 - Prove E2E and LLM Realism (Priority: P2)

As a release reviewer, I need default and opt-in E2E lanes to prove the actual product paths rather than only helper code or static artifacts.

**Why this priority**: The E2E score remains below 100 until MySQL, STDIO, distribution, and LLM evaluation evidence is current and repeatable.

**Independent Test**: Run the default MCP E2E lane plus documented opt-in lanes for MySQL, STDIO, distribution, packaged runtime, and LLM evaluation where credentials/runtime are available.

**Acceptance Scenarios**:

1. **Given** a default developer environment, **When** the default E2E lane runs, **Then** HTTP contract, H2 production, approval, completion, and resource URI behavior are verified.
2. **Given** opt-in infrastructure is available, **When** the opt-in lanes run,
   **Then** MySQL, STDIO, distribution, packaged runtime, and LLM evaluation evidence is recorded with exit codes and artifact paths.

---

### User Story 4 - Preserve Protocol Correctness Without Markdown Overreach (Priority: P2)

As a protocol reviewer, I need MCP outputs to follow official structured content and text fallback rules without treating Markdown as a mandatory tool-result format.

**Why this priority**: Official MCP does not require Markdown tool output. For database gateway results, structured JSON is the stable contract.

**Independent Test**: Verify tool results use `structuredContent` plus serialized JSON text fallback where applicable, and prompt templates may remain Markdown.

**Acceptance Scenarios**:

1. **Given** a tool returns structured data, **When** an MCP client receives the result, **Then** the response includes schema-conforming `structuredContent` and a text fallback.
2. **Given** prompt guidance is meant for model-readable instructions, **When** the prompt is read,
   **Then** Markdown may be used for human/model readability without becoming a tool-result requirement.

## Edge Cases

- Historical scorecard claims conflict with current analysis; current package wins for this checkpoint until evidence is refreshed.
- MCP Java SDK `1.1.2` does not expose every optional MCP `2025-11-25` descriptor field; SDK-deferred gaps must be documented and must not be emitted through unofficial protocol surfaces.
- Optional MCP capabilities may stay unimplemented only if capabilities are not advertised and the future scope is documented.
- LLM evaluation may require opt-in credentials or runtime; missing infrastructure keeps the relevant dimension below 100.
- Markdown output is not required for tool results, but JSON text fallback remains required when structured content is returned.

## Requirements

### Functional Requirements

- **FR-001**: The scorecard MUST use the current 12-dimension baseline agreed on 2026-05-15 and target every dimension at 100/100.
- **FR-002**: Functional completeness MUST be scoped to encrypt and mask workflows only for this checkpoint.
- **FR-003**: Markdown MUST NOT be treated as a mandatory MCP tool-result format; structured JSON plus text fallback is the required tool-result contract.
- **FR-004**: Every dimension below 100 MUST have at least one closing task with owner path, expected behavior, and verification evidence.
- **FR-005**: A dimension MUST NOT be marked 100/100 without command, artifact, contract, or source-backed evidence.
- **FR-006**: Historical Speckit 100/100 evidence MAY be reused only after it is revalidated against the current MCP specification, MCP Java SDK version, and local source paths.
- **FR-007**: The package MUST NOT require `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- **FR-008**: Protocol decisions MUST cite MCP Specification `2025-11-25` and MCP Java SDK documentation or local SDK source when SDK behavior is relevant.
- **FR-009**: Encrypt and mask workflow completion MUST include descriptor, resource, prompt, completion, planning, preview/apply, approval, validation, and recovery evidence.
- **FR-010**: E2E completion MUST distinguish default-lane evidence from opt-in MySQL, STDIO, distribution, packaged runtime, and LLM lanes.
- **FR-011**: Code cleanliness completion MUST include searches and/or refactors for nullable production returns and direct static/constructor mocking in touched tests.
- **FR-012**: Operations completion MUST include distribution, startup/configuration, OAuth/authorization where enabled, and performance-budget evidence.

### Key Entities

- **Score Dimension**: A named quality dimension with current score, target score, gap summary, owner path, and closing evidence.
- **Closing Evidence**: A command result, artifact, contract test, source map, or documented SDK limitation that justifies moving a dimension to 100.
- **Workflow Scope**: The V1 encrypt and mask MCP capability set: resources, prompts, completions, plan/apply/validate tools, approval, and validation layers.
- **Evidence Lane**: A verification route such as default unit tests, H2 E2E, MySQL E2E, STDIO E2E, distribution smoke, or LLM evaluation.

## Success Criteria

### Measurable Outcomes

- **SC-001**: All 12 active score dimensions in `scorecard.md` are 100/100 with linked closing evidence.
- **SC-002**: Scoped Maven unit and E2E commands pass for `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features/encrypt`,
  `mcp/features/mask`, `mcp/bootstrap`, and `test/e2e/mcp`.
- **SC-003**: Checkstyle and Spotless pass for every touched Java module.
- **SC-004**: The mcp-builder evaluation artifact contains ten read-only, independent, complex, realistic, verifiable, and stable questions.
  The validation test proves those properties structurally.
- **SC-005**: The current git branch remains `001-shardingsphere-mcp` for the entire package.

## Assumptions

- Current implementation baseline is the user-accepted 2026-05-15 reassessment: overall 84/100, `shardingsphere-mcp` 86/100, `test/e2e/mcp` 80/100.
- The feature scope is not expanded to sharding, readwrite-splitting, shadow, traffic, mode governance, observability, historical data migration, rollback orchestration, or audit persistence.
- Official MCP Specification `2025-11-25` remains the active protocol baseline for this package.
- MCP Java SDK usage is evaluated against the dependency version declared in `mcp/bootstrap`.
