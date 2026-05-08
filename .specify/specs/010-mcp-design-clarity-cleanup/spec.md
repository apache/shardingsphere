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

# Feature Specification: MCP Design Clarity Cleanup

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-08
**Status**: Draft
**Input**: User asks to organize the over-design, transitional-design, inconsistent-style, and unclear-code findings for `shardingsphere-mcp` and `mcp-e2e` with Speckit, without switching branches.

## Goal

Define a clarity-first cleanup backlog for `shardingsphere-mcp` and `test/e2e/mcp`.

The goal is not to add a larger architecture. The goal is to make the existing MCP modules easier to read, safer to change, and more convenient to extend.
The cleanup removes mixed responsibilities, hard-coded transitional behavior, stringly typed contracts, and oversized test harnesses.

## User Scenarios & Testing

### User Story 1 - Maintainer Reads One Responsibility at a Time (Priority: P1)

A maintainer reviewing MCP core behavior should understand SQL classification, descriptor loading, response contract building, and error recovery without reading unrelated concerns in the same class.
Those unrelated concerns include parsing, legacy migration, model guidance, and validation policy.

**Why this priority**: These classes are on the hot review path for future MCP changes. If they stay overloaded, every feature change carries hidden regression risk.

**Independent Test**: Review the target classes and verify each class has one primary reason to change, with behavior covered by existing or new focused tests.

**Acceptance Scenarios**:

1. **Given** a reviewer opens the statement classification code, **When** they trace how SQL is classified, **Then** they do not need to understand a broad hand-rolled SQL grammar.
2. **Given** a reviewer opens descriptor loading code, **When** they trace YAML loading, **Then** legacy compatibility, model contract guidance, and per-tool validation are separated.
3. **Given** an error response changes, **When** recovery hints are adjusted, **Then** behavior is not coupled to exact exception message text.

---

### User Story 2 - Contributor Extends MCP Contracts Without Guessing (Priority: P1)

A contributor adding a tool, resource, completion hint, or workflow response should know where public field names, next actions, recovery payloads, and descriptor contracts are defined.

**Why this priority**: The current surface is model-facing. Small field-name drift or hidden aliases can confuse clients and tests.

**Independent Test**: Add or modify one public MCP contract in a dry-run review and verify the required schema, constants, tests, and descriptor lint location are obvious.

**Acceptance Scenarios**:

1. **Given** a contributor adds a workflow response field, **When** they search for the field definition, **Then** they find a central contract model or key factory rather than scattered map literals.
2. **Given** a transport adapter builds tool specifications, **When** it handles workflow planning tools, **Then** it does not hard-code feature tool names or feature-specific argument names.
3. **Given** a legacy alias exists, **When** the primary payload is built, **Then** the public contract uses only the clearest canonical field shape.

---

### User Story 3 - E2E Author Reads Scenarios Instead of Harness Internals (Priority: P2)

An E2E test author should understand MCP runtime, HTTP/STDIO contract, and LLM usability scenarios without decoding a large custom conversation runner or repeated nested `Map<String, Object>` casts.

**Why this priority**: E2E tests should protect product behavior. If the harness is harder to read than the scenario, future regressions become easier to miss.

**Independent Test**: Add one new E2E scenario and verify it can use named assertion helpers, runtime fixtures, and action bridges without duplicating protocol schema construction.

**Acceptance Scenarios**:

1. **Given** a test opens the LLM conversation runner, **When** it follows the model loop, **Then** tool bridging, final answer verification, safety validation, and artifacts are separate.
2. **Given** an E2E assertion checks a tool result, **When** it reads nested payloads, **Then** it uses a named helper that explains the contract being asserted.
3. **Given** a smoke test fails, **When** the failing class is inspected, **Then** the class name and scope identify the product surface under test.

## Edge Cases

- Existing MCP clients may rely on legacy aliases; this cleanup may intentionally break those aliases when the clearer contract is documented and tested.
- Some SQL safety checks may still require lightweight lexical checks; this specification forbids broad hidden parsing, not all local inspection.
- Live LLM tests remain opt-in and must not become a default CI requirement.
- The repository may contain unrelated dirty files; implementation must avoid reverting or rewriting them.
- Spec Kit branch-creation scripts must not be used for this feature because the user forbids branch switching.

## Requirements

### Functional Requirements

- **DCC-FR-001**: The Speckit package MUST remain on branch `001-shardingsphere-mcp`.
  No implementation task may require `git switch`, `git checkout`, branch creation, or a Spec Kit script that performs those actions.
- **DCC-FR-002**: Statement classification MUST be narrowed to MCP safety and routing needs.
  Otherwise tokenizer, CTE handling, and target-object extraction MUST be split into named helpers with explicit supported scope.
- **DCC-FR-003**: Descriptor catalog loading MUST separate YAML IO, descriptor swapping, legacy cleanup, descriptor validation, and model-facing contract construction.
- **DCC-FR-004**: Bootstrap transport code MUST NOT hard-code feature tool names or feature-specific workflow argument names.
- **DCC-FR-005**: High-frequency public payload contracts MUST use small typed models or centralized factories/constants instead of scattered string literals.
  This includes `next_actions`, recovery hints, resource hints, and workflow apply payloads.
- **DCC-FR-006**: Error recovery behavior MUST be keyed by structured exception data, error codes, or dedicated recovery providers, not by exact exception message prefixes.
- **DCC-FR-007**: Metadata search MUST separate collection, ranking, pagination, and response assembly enough that each part can be tested and reviewed independently.
- **DCC-FR-008**: Completion candidate production MUST live in core/support logic; bootstrap completion adapters should primarily translate MCP SDK requests and responses.
- **DCC-FR-009**: Feature planning services MUST expose simple testable construction paths so normal collaborator replacement does not require field reflection.
- **DCC-FR-010**: Legacy aliases and transitional contract fields MUST be removed from public model-facing contracts unless an internal-only fallback is explicitly justified.
- **DCC-FR-011**: Direct Mockito static or constructor mocking in touched tests MUST align with repository guidance or document why `AutoMockExtension` cannot be used.
- **DCC-FR-012**: The LLM E2E conversation runner MUST be split or simplified so model loop, tool bridge, final-answer verification, safety checks, and artifacts are separately readable.
- **DCC-FR-013**: E2E assertions over MCP payloads MUST use named helpers for common result shapes instead of repeated nested `Map<String, Object>` casts.
- **DCC-FR-014**: Giant smoke tests MUST be grouped by product surface, such as metadata resources, tool schema, SQL execution, workflow guidance, and transport contracts.
- **DCC-FR-015**: Runtime fixture support MUST separate generic database/runtime setup from LLM scenario data and repeated wait/retry behavior.
- **DCC-FR-016**: Public behavior MAY change when the new shape is more reasonable, readable, and elegant.
  Every intentional behavior or contract change MUST be documented and tested.
- **DCC-FR-017**: New abstractions MUST be accepted only when they reduce a mixed responsibility, remove meaningful duplication, or make a public contract easier to find.
  No DI framework, planner framework, or broad rewrite is in scope.

### Key Entities

- **Responsibility Boundary**: A documented primary reason for a class or helper to change.
- **Public MCP Contract**: A model-facing field shape, descriptor schema, recovery payload, or next action payload.
- **Contract Cleanup Boundary**: The single place where old aliases or transitional fields are removed or, if unavoidable, accepted as internal-only input.
- **E2E Scenario Harness**: Test support that runs runtime, transport, LLM usability, and payload assertions.
- **Clarity Gate**: A review or test condition that prevents the same mixed-responsibility pattern from returning.

## Success Criteria

### Measurable Outcomes

- **DCC-SC-001**: No bootstrap transport class contains hard-coded encrypt/mask planning tool names or feature-specific workflow argument names.
- **DCC-SC-002**: Descriptor loading no longer has one class responsible for YAML IO, legacy cleanup, per-tool output contract policy, and model guidance payload construction.
- **DCC-SC-003**: Statement classification no longer hides tokenizer, CTE, and target-object extraction as private implementation details inside one oversized class without explicit scope.
- **DCC-SC-004**: Common model-facing payloads have either typed contract helpers or centralized key factories with focused tests.
- **DCC-SC-005**: E2E payload assertions for common MCP responses avoid repeated raw nested casts in scenario tests.
- **DCC-SC-006**: The largest MCP and MCP E2E classes have a documented reason to remain large, or are split by responsibility with equivalent test coverage.
- **DCC-SC-007**: All implementation PRs for this feature report branch status, scoped test commands, Checkstyle status, and intentional contract changes.

## Assumptions

- This is a refactoring and requirement-management feature, not a new user-visible MCP capability.
- Existing `mcp` and `test/e2e/mcp` module behavior is the comparison baseline, not a compatibility promise.
- Documentation-only Speckit changes do not require Maven tests.
- Java implementation tasks will run module-scoped Maven tests and Checkstyle before completion.
