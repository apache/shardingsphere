# Feature Specification: MCP AI-Friendly Lightweight Experience

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-05-04
**Status**: Draft
**Input**: User description: "Use Spec Kit to organize the ShardingSphere MCP AI-friendly requirements, avoid over-design, and do not switch git branches."

## Process Constraints

- This requirement package MUST be prepared on the current branch only.
- Do not run `git switch`, `git checkout`, branch creation scripts, or other branch-changing commands.
- Treat current ShardingSphere MCP as the baseline. Do not redesign the runtime, protocol stack, or feature SPI.
- Keep the scope lightweight: improve model clarity, safe continuation, common recovery, examples, and regression guards.
- Do not introduce a heavy planner, graph traversal engine, cross-session memory, vector ranking, or full authorization platform in this increment.

## Clarification Boundary

### Needs code re-analysis before implementation

The following items are not open product questions. They are evidence-gathering gates that implementation work must answer from current code:

- Current MCP public surface from descriptors, capabilities, and README, so docs do not name absent tools or resources.
- Current `execute_update` preview response shape in handler and tests before adding or renaming guidance fields.
- Current workflow preview guidance shape before sharing next-action vocabulary with SQL preview.
- Current recovery behavior for missing database, missing execution mode, wrong SQL tool, unknown tool/resource, and unavailable `plan_id`.
- Minimal descriptor lint placement in existing descriptor loader or support-layer tests.
- Capabilities contract boundary that verifies section and shape without large snapshots.
- Historical document status risk, limited to documents that could mislead readers about the current contract.

### Does not need user clarification

These decisions remain fixed unless the user explicitly changes scope:

- Do not switch or create git branches.
- Do not add heavy planner, vector memory, cross-session long-term memory, or full authorization platform.
- Preserve resource-first discovery as the current MCP surface.
- Implement the P0/P1 risk-reduction path first.
- Keep `mcp/README.md` and `mcp/README_ZH.md` aligned for current public surface descriptions.
- Keep real-model LLM E2E opt-in instead of default CI gating.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Model sees the current MCP surface consistently (Priority: P1)

As an MCP model-facing client or documentation reader, I want README, descriptors, and capabilities to describe the same public MCP surface
so that the model does not choose tools that are only present in old design documents.

**Why this priority**: Tool/resource mismatch is the fastest way to make a model feel lost.
The current implementation is resource-first, so the public contract must consistently describe resource-first discovery plus the actual tool set.

**Independent Test**: Read `shardingsphere://capabilities` and descriptor-backed lists, then compare the documented public tools and primary paths in README against the protocol-visible surface.

**Acceptance Scenarios**:

1. **Given** a model reads `mcp/README.md`, **When** it looks for public MCP tools, **Then** it sees only the tools currently exposed by descriptors and capabilities.
2. **Given** a historical PRD or design document contains earlier `list_*` or `describe_*` tool ideas, **When** a reader opens that document,
   **Then** the document clearly states whether it is historical design, current contract, or future planning.
3. **Given** a model reads `shardingsphere://capabilities`, **When** it chooses a metadata path, **Then** it can follow resource-first discovery without relying on README-only instructions.

---

### User Story 2 - Model safely handles side-effecting actions (Priority: P1)

As an operator using an LLM with ShardingSphere MCP, I want side-effecting SQL and workflow apply actions to be previewed before execution
so that the model cannot silently mutate data, rules, or physical structure.

**Why this priority**: Explicit operator control is a constitution requirement.
The existing preview behavior should become easier for models to follow with reusable arguments and consistent next-action guidance.

**Independent Test**: Call `execute_update` with `execution_mode=preview` and verify the response contains reusable execute arguments,
approval guidance, side-effect scope, and a structured next action. Verify workflow apply preview follows the same guidance vocabulary.

**Acceptance Scenarios**:

1. **Given** a model receives side-effecting SQL, **When** it calls `execute_update`, **Then** it must use `execution_mode=preview` before any `execution_mode=execute` call.
2. **Given** `execute_update` returns a preview, **When** the model prepares the follow-up call, **Then** it can reuse server-provided `suggested_arguments` instead of reconstructing SQL.
3. **Given** a workflow has planned artifacts, **When** the model applies the workflow,
   **Then** it first calls `apply_workflow` with `execution_mode=preview` and only proceeds after visible user approval.

---

### User Story 3 - Model repairs common mistakes from structured recovery (Priority: P1)

As a model using MCP tools, I want common call-shape mistakes to return structured recovery fields so that I can safely retry without guessing hidden values.

**Why this priority**: Models often omit a database, choose the wrong SQL tool, omit execution mode, call stale `plan_id`, or read an unsupported resource. These should be cheap to recover from.

**Independent Test**: Trigger the five common errors and assert the response includes a consistent recovery envelope with safe next-step metadata.

**Acceptance Scenarios**:

1. **Given** a model omits `database`, **When** the server rejects the call, **Then** recovery includes `missing_fields` and recommends reading `shardingsphere://databases`.
2. **Given** a model omits `execution_mode`, **When** it calls a side-effecting tool, **Then** recovery recommends `execution_mode=preview`.
3. **Given** a model sends mutating SQL to `execute_query`, **When** the server rejects it, **Then** recovery recommends `execute_update` preview and preserves the user approval boundary.
4. **Given** a model uses an unknown tool or resource, **When** the server rejects it, **Then** recovery recommends reading `shardingsphere://capabilities`.
5. **Given** a model uses an unavailable `plan_id`, **When** apply or validation fails, **Then** recovery recommends replanning in the current session.

---

### User Story 4 - Model follows short task paths and examples (Priority: P2)

As a model or model integrator, I want compact examples and short first-use paths so that I can use metadata inspection, safe SQL, and encrypt or mask workflow without reading long design documents.

**Why this priority**: The server already exposes prompts and descriptors. Small examples and short paths help models converge faster without adding new runtime complexity.

**Independent Test**: Inspect README, prompt templates, and descriptors to confirm each common path has a short sequence and each complex tool has a compact output example.

**Acceptance Scenarios**:

1. **Given** a model needs metadata, **When** it reads README or prompt guidance, **Then** it finds a path of no more than five steps.
2. **Given** a model needs safe SQL execution, **When** it reads README or prompt guidance, **Then** it finds a preview-before-execute path of no more than five steps.
3. **Given** a model needs encrypt or mask planning, **When** it reads README or prompt guidance, **Then** it finds a plan-preview-validate path of no more than six steps.
4. **Given** a model inspects complex tool descriptors, **When** it reads examples, **Then** the examples are small, static, and secret-free.

---

### User Story 5 - Maintainers catch model-surface regressions early (Priority: P2)

As a maintainer, I want lightweight descriptor lint and a capabilities contract test so that model-facing metadata does not silently regress.

**Why this priority**: The MCP surface can remain protocol-valid while becoming worse for models. Minimal deterministic tests should catch obvious regressions without requiring real model credentials.

**Independent Test**: Run descriptor lint and a capabilities contract test locally without real-model services.

**Acceptance Scenarios**:

1. **Given** a descriptor has an empty or placeholder description, **When** descriptor lint runs, **Then** it fails with the descriptor identifier.
2. **Given** a side-effecting tool lacks approval or side-effect metadata, **When** descriptor lint runs, **Then** it fails.
3. **Given** `shardingsphere://capabilities` is read in a test fixture, **When** the contract test runs, **Then** it asserts core sections exist without locking large runtime payloads.

---

### Edge Cases

- A model reads an old PRD that lists tools no longer exposed by descriptors.
- `execute_update` preview returns normalized SQL, but the user later asks to modify the SQL before execution.
- A recovery response cannot safely infer a missing value.
- A workflow plan exists in another MCP session.
- A result list is large enough that pagination hints matter.
- Prompt text and descriptor text drift over time.
- A future public config or file resource would require roots or permission-boundary metadata.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: README, descriptor, and capabilities documentation MUST describe the same current public MCP tools and resource-first metadata path.
- **FR-002**: Historical PRD, technical design, or previous spec documents that retain obsolete tool lists SHOULD be marked as historical design, current contract, or future planning.
- **FR-003**: Model-facing guidance SHOULD describe three short first-use paths: metadata inspection, safe SQL execution, and encrypt or mask workflow.
- **FR-004**: `next_actions` SHOULD be the primary structured field for follow-up guidance.
- **FR-005**: Each structured next action SHOULD include `action_kind`, `reason`, and `requires_user_approval`.
- **FR-006**: Tool-call next actions SHOULD include `target_tool` and `required_arguments`.
- **FR-007**: Ask-user next actions SHOULD include `required_inputs`.
- **FR-008**: The project SHOULD converge on one preferred field name for the single recommended next tool and avoid adding more equivalent names.
- **FR-009**: `execute_update` preview MUST return reusable `suggested_arguments` that include normalized SQL and the execution mode needed for an approved follow-up.
- **FR-010**: `execute_update` preview MUST state that user approval is required before execution.
- **FR-011**: `apply_workflow` preview SHOULD use the same next-action vocabulary as SQL preview where practical.
- **FR-012**: Missing `database` recovery MUST include missing field metadata and a safe resource to read first.
- **FR-013**: Missing `execution_mode` recovery MUST recommend `execution_mode=preview`.
- **FR-014**: Wrong SQL tool recovery MUST preserve preview and user approval semantics.
- **FR-015**: Unsupported tool and unsupported resource recovery SHOULD recommend `shardingsphere://capabilities`.
- **FR-016**: Unavailable workflow plan recovery SHOULD recommend replanning in the current MCP session.
- **FR-017**: Complex tool descriptors or documentation SHOULD include compact output examples for `execute_update` preview,
  `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow` preview, and `validate_workflow`.
- **FR-018**: Examples MUST be static, small, secret-free, and free of production database names or environment-specific paths.
- **FR-019**: Descriptor lint SHOULD reject empty descriptions, placeholder descriptions, missing side-effect metadata for side-effecting tools,
  missing enum value sets, missing core output schema fields, and broken navigation references.
- **FR-020**: A lightweight capabilities contract test SHOULD assert the presence of resources, resource templates, tools, prompts,
  completion targets, navigation, protocol availability, and fingerprints.
- **FR-021**: Capabilities contract tests MUST avoid locking large runtime data or requiring real model services.
- **FR-022**: The LLM usability suite SHOULD add minimal scenarios for side-effect SQL preview and plan-to-apply-preview-to-validate workflow order while remaining opt-in.
- **FR-023**: Completion ranking MAY prefer candidates with stronger supplied context, such as database/schema for tables and database/schema/table for columns.
- **FR-024**: Completion ranking MUST NOT use vector search, model calls, cross-session memory, or user behavior learning in this increment.
- **FR-025**: Metadata responses MAY expose a lightweight freshness hint such as `metadata_fingerprint` or `loaded_at`.
- **FR-026**: Documentation SHOULD cover common first-run failures: Java version, missing JDBC driver, HTTP token, STDIO stdout logging, and empty tool or resource discovery.
- **FR-027**: Encrypt and mask workflow documentation MUST state that workflow tools do not migrate existing data, backfill data, or approve production changes for the user.
- **FR-028**: Requirement analysis and resulting specification updates MUST be completed without branch creation or branch switching.

### Key Entities

- **Current MCP Surface**: The protocol-visible set of public resources, resource templates, tools, prompts, completions, navigation entries, annotations, and schemas.
- **Next Action**: A structured model-facing recommendation that tells the model whether to call a tool, read a resource, ask the user, or stop.
- **Recovery Envelope**: Structured metadata attached to recoverable errors that tells a model how to retry safely.
- **Capabilities Core Contract**: A small deterministic assertion set for `shardingsphere://capabilities`, focused on section presence and shape rather than large payload snapshots.
- **Compact Example**: A small static JSON shape used to teach complex tool output without leaking secrets or environment details.
- **Workflow Plan Summary**: A current-session summary of workflow plan identity, kind, status, update time, artifact summary, and next action if a lightweight workflow query resource is implemented.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A reviewer can compare README public tool lists with descriptor-backed capabilities and find no current-surface mismatch.
- **SC-002**: Common mistake tests prove structured recovery for missing database, missing execution mode, wrong SQL tool, unknown resource or tool, and unavailable plan id.
- **SC-003**: SQL preview responses include reusable suggested arguments and explicit user-approval guidance.
- **SC-004**: Descriptor lint catches at least empty descriptions, placeholder descriptions, side-effect metadata omissions, missing enum values,
  missing core output fields, and broken navigation references.
- **SC-005**: A capabilities contract test runs without real-model credentials and verifies the core model-facing sections.
- **SC-006**: README or prompt guidance contains short paths for metadata inspection, safe SQL execution, and encrypt or mask workflow.
- **SC-007**: This Spec Kit package is created on the existing branch with no branch creation or branch switching.

## Assumptions

- The descriptor catalog remains the source of truth for model-facing MCP metadata.
- HTTP and STDIO transports continue to expose the same public model-facing surface.
- Workflow `plan_id` remains session-scoped and non-durable unless a separate durability requirement is approved later.
- Existing opt-in LLM E2E infrastructure can be extended without affecting default CI.
- Current bearer token behavior remains an admission gate, not a full authorization system.
