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

# Feature Specification: MCP Prompt And Completion Guidance Hardening

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-17
**Status**: Draft
**Input**: User asked to use Speckit to organize a new requirement spec for improving MCP Prompts and completions, while avoiding over-design and not switching branches.

## Branch Constraint

- Work MUST remain on the existing `001-shardingsphere-mcp` branch.
- Branch creation, branch switching, committing, pushing, and staging are out of scope for this specification package.
- This package is documentation-only until the user gives an explicit implementation command.
- Existing worktree changes are out of scope and MUST NOT be reverted, reformatted, staged, or otherwise changed by this package.

## Goal

Improve MCP prompt and completion guidance with the smallest useful change set:

1. Prompts should tell models not to guess MCP identifiers or workflow handles.
2. Prompts should direct models to use completion or nearest resources before choosing uncertain arguments.
3. `prompts/get` should reject missing required arguments with MCP invalid-params semantics.
4. Completion should only answer arguments declared by the requested prompt or resource-template completion target.
5. Metadata completion should infer a logical database only when there is exactly one safe runtime database candidate.
6. Tests should prove the reduced guessing behavior without adding broad new product scope.

This spec intentionally avoids new feature prompts, semantic algorithm recommendation, large ranking redesign, and broad LLM evaluation expansion.

## Confirmed Design Decisions

- Missing required `prompts/get` arguments are part of this scope and MUST be handled as invalid parameters, using JSON-RPC code `-32602`.
- Completion requests for arguments not declared by the target completion descriptor MUST be handled as invalid parameters, using JSON-RPC code `-32602`.
  This is a local design decision based on MCP completion input-validation requirements, not a separately named MCP error case.
- Completion MUST NOT return an empty success response for undeclared target arguments because that hides a client/protocol misuse.
- The implementation should stay small: prompt wording, prompt argument validation, completion target validation, safe single-database inference, and focused tests.
- Source reference: MCP Prompts says `prompts/get` accepts arguments, missing required arguments are `-32602`, and prompt arguments should be validated.
- Source reference: MCP Completion says completion is for prompt/resource-template arguments, supports contextual arguments, returns at most 100 values, and must validate inputs.

## User Scenarios & Testing

### User Story 1 - Model Uses Completion Or Resources Before Guessing (Priority: P1)

As an MCP user, I want prompt guidance to stop the model from guessing database, schema, table, column, algorithm, or plan identifiers when the current context is incomplete.

**Why this priority**: Guessing identifiers is the main failure mode for prompt-led MCP usage.
A small prompt wording improvement can guide the model toward existing completion and resource mechanisms.

**Independent Test**: Render each existing prompt and verify it contains concise guidance to use completion or the nearest resource before choosing uncertain identifiers.

**Acceptance Scenarios**:

1. **Given** a prompt is rendered for metadata inspection, **When** the database or schema is uncertain,
   **Then** the prompt instructs the model to complete the value or read the nearest metadata resource instead of guessing.
2. **Given** a prompt is rendered for encrypt or mask planning, **When** table, column, algorithm, or plan ID is uncertain,
   **Then** the prompt instructs the model to complete the value or read the nearest feature or workflow resource first.
3. **Given** a prompt is rendered for safe SQL execution, **When** database or schema is uncertain, **Then** the prompt instructs the model to resolve scope before executing or previewing SQL.

---

### User Story 2 - Prompt Output Ends With A Clear User-Facing Result (Priority: P2)

As an MCP user, I want prompt guidance to tell the model when to stop and how to summarize the confirmed result, so the model does not keep calling tools after the useful work is done.

**Why this priority**: Current prompts already have stop conditions. A short final-answer rule makes the stop condition easier for a model to turn into a useful response.

**Independent Test**: Render each existing prompt and verify it contains a concise final-answer rule that asks for confirmed facts, selected MCP path, and any required next user action.

**Acceptance Scenarios**:

1. **Given** metadata inspection completes, **When** the model answers, **Then** the answer summarizes confirmed metadata paths or the next exact MCP resource/tool path.
2. **Given** SQL preview completes, **When** the model answers, **Then** the answer summarizes previewed side effects and waits for approval before execution.
3. **Given** a workflow planning prompt returns a plan ID, **When** the model answers, **Then** the answer names the plan ID and review/apply next step without applying changes by default.

---

### User Story 3 - Completion Rejects Undeclared Arguments (Priority: P1)

As an MCP client integrator, I want completion to respect the descriptor for the requested prompt or resource template.
Clients should not accidentally receive suggestions for arguments that the target does not declare.

**Why this priority**: Completion is a protocol-facing helper. It must be predictable and target-scoped before more intelligent completion is useful.

**Independent Test**: Call completion for an argument that is supported by a provider but not declared by the requested completion target.
Verify the request fails with invalid-params semantics before provider invocation.

**Acceptance Scenarios**:

1. **Given** `inspect_metadata` declares completion for `database` and `schema`, **When** a client requests completion for `table` against that prompt,
   **Then** completion fails with JSON-RPC code `-32602` and does not return table candidates.
2. **Given** a resource-template completion target declares `database`, **When** a client requests completion for `column`,
   **Then** completion fails with JSON-RPC code `-32602` and does not return column candidates.
3. **Given** a declared argument is requested, **When** a matching provider exists, **Then** existing completion behavior remains available.

---

### User Story 4 - Required Prompt Arguments Are Enforced (Priority: P1)

As an MCP client integrator, I want `prompts/get` to reject missing required prompt arguments so clients do not receive partially rendered prompt text with empty required fields.

**Why this priority**: Rendering a prompt with an empty required field weakens model guidance and conflicts with MCP prompt argument validation expectations.

**Independent Test**: Request a prompt with a missing required argument and verify the request fails with invalid-params semantics.

**Acceptance Scenarios**:

1. **Given** `safe_sql_execution` requires `database` and `sql_intent`, **When** either argument is missing, null, or blank after trimming,
   **Then** `prompts/get` fails with JSON-RPC code `-32602` instead of rendering a prompt with an empty field.
2. **Given** a prompt argument is optional, **When** it is missing, **Then** existing rendering behavior remains available.
3. **Given** all required arguments are present, **When** the prompt is requested, **Then** existing prompt rendering still succeeds.

---

### User Story 5 - Metadata Completion Infers Only Safe Single-Database Context (Priority: P2)

As an MCP user in a single logical database runtime, I want completion to infer the database automatically so simple table or column completion works without unnecessary manual context.

**Why this priority**: Single-database runtimes are common, and safe inference improves usability without introducing guessing in multi-database runtimes.

**Independent Test**: Use mocked runtime metadata with one database and multiple databases. Verify only the one-database case fills the missing database context.

**Acceptance Scenarios**:

1. **Given** the runtime has exactly one logical database, **When** table completion is requested without `database`,
   **Then** completion infers that database and continues to schema/table resolution.
2. **Given** the runtime has multiple logical databases, **When** table completion is requested without `database`, **Then** completion returns missing context instead of guessing.
3. **Given** the caller already provides `database`, **When** completion runs, **Then** the provided value is preserved and not overwritten by inference.

## Edge Cases

- Do not add new prompt names as part of this scope.
- Do not move prompt templates to a different storage location.
- Do not add broad semantic algorithm recommendation; completion can list algorithms but must not decide business suitability.
- Do not introduce fuzzy matching beyond existing deterministic completion behavior in this spec.
- Do not change MCP tool execution safety; side-effect preview and approval remain tool-level responsibilities.
- Do not treat single-database inference as permission to guess schema, table, column, algorithm, or plan ID.
- Do not query live databases solely to infer completion context; use existing runtime metadata only.
- Do not render prompt templates when required arguments are missing, null, or blank after trimming.
- Do not expose sensitive values in invalid-params messages; identify the invalid or missing argument name only.
- Do not edit generated `target/` content.

## Requirements

### Functional Requirements

- **PCG-FR-001**: Existing MCP prompt templates MUST include concise no-guess guidance for uncertain database, schema, table, column, algorithm, and plan ID values.
- **PCG-FR-002**: Existing MCP prompt templates MUST direct the model to use MCP completion or read the nearest relevant resource before choosing uncertain arguments.
- **PCG-FR-003**: Existing MCP prompt templates MUST include a concise final-answer rule that asks the model to summarize confirmed facts,
  selected MCP path, and required next user action when present.
- **PCG-FR-004**: `prompts/get` MUST validate required prompt arguments before rendering template text.
- **PCG-FR-005**: `prompts/get` MUST fail with invalid-params semantics, using JSON-RPC code `-32602`,
  when any required argument is missing, null, or blank after trimming.
- **PCG-FR-006**: `prompts/get` MUST continue to allow missing optional arguments.
- **PCG-FR-007**: Completion MUST validate that the requested argument is declared by the target completion descriptor before invoking providers.
- **PCG-FR-008**: Completion MUST fail with invalid-params semantics, using JSON-RPC code `-32602`, for undeclared target arguments.
- **PCG-FR-009**: Completion MUST NOT return provider candidates for undeclared target arguments.
- **PCG-FR-010**: Metadata completion MUST infer `database` only when the runtime has exactly one logical database and the caller did not provide a database.
- **PCG-FR-011**: Metadata completion MUST keep returning missing-context guidance when more than one logical database exists and `database` is missing.
- **PCG-FR-012**: Metadata completion MUST preserve caller-provided context values and MUST NOT overwrite them with inferred values.
- **PCG-FR-013**: Existing single-schema inference behavior MUST remain compatible with the new single-database inference.
- **PCG-FR-014**: Metadata completion MUST NOT perform live database metadata queries solely for context inference.
- **PCG-FR-015**: Implementation MUST include focused unit tests for prompt rendering, missing required prompt arguments, undeclared completion argument rejection, runtime-metadata-only inference,
  single-database inference, multi-database missing context, and provided-context preservation.
- **PCG-FR-016**: Implementation touching `mcp` or `test/e2e/mcp` MUST receive MCP design review before final handoff.
- **PCG-FR-017**: Implementation MUST report scoped verification commands and exit codes.

### Key Entities

- **Prompt Template**: Existing Markdown prompt body under `META-INF/shardingsphere-mcp/prompts`.
- **Prompt Argument Descriptor**: Descriptor entry that declares required and optional prompt arguments.
- **Completion Target Descriptor**: Descriptor entry that declares which prompt or resource-template arguments support completion.
- **Completion Provider**: SPI implementation that returns candidates for metadata, workflow plan IDs, or feature algorithms.
- **Inferred Context Argument**: A context value filled by the server only when it has exactly one safe candidate.

## Success Criteria

### Measurable Outcomes

- **PCG-SC-001**: All existing MCP prompt templates render no-guess guidance and a final-answer rule.
- **PCG-SC-002**: A prompt request missing a required argument fails with JSON-RPC code `-32602`.
- **PCG-SC-003**: A completion request for an undeclared argument fails with JSON-RPC code `-32602` and returns no candidates.
- **PCG-SC-004**: A single-database completion test shows `database` is inferred and exposed through completion metadata.
- **PCG-SC-005**: A multi-database completion test shows missing-context behavior remains unchanged.
- **PCG-SC-006**: Existing declared completion scenarios for database, schema, plan ID, encrypt algorithms, and mask algorithms continue to pass.

## Assumptions

- Existing MCP prompt Markdown resources remain the canonical storage for prompt bodies.
- Existing descriptor YAML remains the canonical storage for prompt metadata and completion target declarations.
- The target implementation should remain small and compatible with the current MCP Java SDK usage.
- `test/e2e/mcp` is the repository's MCP E2E module path; there is no separate top-level `mcp-e2e` directory in this workspace.
