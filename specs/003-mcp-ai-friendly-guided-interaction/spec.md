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

# Feature Specification: AI-Friendly MCP Lightweight Requirements

**Feature Branch**: `001-shardingsphere-mcp` (current branch, not changed)
**Created**: 2026-05-04
**Status**: Active requirements organization
**Input**: User request to use Spec Kit to organize the remaining improvements that make `shardingsphere-mcp` native, convenient,
comfortable, and clear for large models, without over-design and without switching branches.

## Process Constraints

- Stay on the current branch. Do not run branch-changing Spec Kit scripts, `git switch`, or `git checkout`.
- Treat `requirements.md` in this directory and `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md` as the lightweight active requirements baseline.
- Preserve side-effect safety: update-capable tools and workflow apply paths must support preview first and keep explicit user approval before execution.
- Prefer current descriptor, resource, tool, prompt, completion, workflow, and recovery mechanisms.
- Do not edit generated paths such as `target/`.
- Do not introduce a broad tool matrix, planner, graph traversal engine, vector search, cross-session memory, model-call ranking, RBAC platform, benchmark leaderboard, or hidden execution shortcut.
- Historical PRD/design/spec drafts may remain as trace material, but they must not override the lightweight active scope.

## Current Baseline

`shardingsphere-mcp` already exposes:

- descriptor-backed tools, resources, prompts, completions, and capability fingerprints;
- `shardingsphere://capabilities` as the aggregated model-visible surface;
- resource-first logical metadata for databases, schemas, tables, columns, indexes, views, and sequences;
- `search_metadata` with metadata search results and derived resource navigation hints;
- separated `execute_query` and `execute_update` paths for read-only SQL and side-effecting SQL;
- encrypt and mask workflow planning, preview/apply, and validation;
- structured `next_actions`, recovery hints, and opt-in LLM usability coverage.

The next increment should reduce model guessing on top of this surface, not rebuild it.

## Active Scope

### P0: Must Do

- Make the current public surface unambiguous across README, descriptors, capabilities, and active requirements.
- Improve server instructions so the model starts from capabilities and the public resources/tools, not from historical design names.
- Converge successful outputs, preview outputs, workflow outputs, and recoverable errors on a clear `next_actions` vocabulary.
- Ensure `search_metadata` returns directly readable resource URIs when they can be safely derived.
- Align core tool output schemas with actual payloads.
- Expand structured recovery for the most common model mistakes.
- Add only lightweight deterministic regression guards for descriptor clarity and capabilities shape.

### P1: Should Do After P0

- Add lightweight resource response navigation such as `self_uri`, `parent_uri`, `count`, and `next_resources` where safe.
- Add compact static examples for complex outputs.
- Improve completion with prefix-first plus contains fallback and context-scoped candidate ordering.
- Expose encrypt and mask algorithm property templates through algorithm resources.
- Clarify `approved_steps` and approval-related arguments with reusable preview output.

### P2: May Do Later

- Improve startup success hints and first-use MCP client configuration docs.
- Add concise troubleshooting for Java version, JDBC driver, token, STDIO logging, empty public surface, and workflow topology mistakes.
- Add a small opt-in LLM usability set for preview-first SQL, metadata search to detail resource, and plan-to-apply-to-validate order.
- Normalize count and pagination wording on large resource lists.

## User Scenarios and Testing

### User Story 1: Model Discovers the Current Surface (Priority: P0)

A model can read server instructions or capabilities and understand the real current resources, tools, prompts, and completions without seeing obsolete tool-matrix expectations.

**Why this priority**: Discovery mistakes happen before any useful MCP call. The first surface must be truthful and compact.

**Independent Test**: Compare README public lists, descriptor catalog identifiers, and `shardingsphere://capabilities` sections for consistency.

**Acceptance Scenarios**:

1. **Given** the model initializes the server, **When** it reads instructions, **Then** it is directed to `shardingsphere://capabilities` and the resource-first path.
2. **Given** the model reads current docs, **When** it looks for public tools, **Then** it sees the existing small tool set rather than historical `list_*` or `describe_*` matrices.
3. **Given** a historical PRD or design document is retained, **When** it mentions early tool names, **Then** it is clearly marked as non-current or historical.

### User Story 2: Model Continues Safely From Outputs (Priority: P0)

After a successful call, preview, workflow plan, or recoverable error, a model receives the next safe MCP step and reusable arguments when the server can know them.

**Why this priority**: Model comfort comes from not reconstructing `plan_id`, SQL, execution mode, resources, and approval requirements from prose.

**Independent Test**: Representative tool and workflow tests assert the same next-action shape across success, preview, and recovery paths.

**Acceptance Scenarios**:

1. **Given** an update preview succeeds, **When** the output is returned, **Then** the next action keeps `execution_mode=execute` behind explicit user approval.
2. **Given** a workflow plan is ready, **When** the output is returned, **Then** it points to `apply_workflow` preview with the current `plan_id`.
3. **Given** the server cannot safely know a value, **When** it builds next action metadata, **Then** it asks the model to ask the user instead of fabricating arguments.

### User Story 3: Model Searches and Reads Metadata (Priority: P0)

A model can search metadata and then read the matched resource without manually constructing URI patterns.

**Why this priority**: URI construction is a common small failure that can be avoided by returning safe, descriptor-backed links.

**Independent Test**: `search_metadata` tests assert `resource_uri`, `parent_resource_uri`, `next_resource_uris`, and derivation status for supported object types.

**Acceptance Scenarios**:

1. **Given** a table search hit can be mapped to a detail resource, **When** results are returned, **Then** the item includes a readable `resource_uri`.
2. **Given** a parent schema or database URI can be derived, **When** results are returned, **Then** the item includes `parent_resource_uri`.
3. **Given** a URI cannot be derived safely, **When** results are returned, **Then** no guessed URI is emitted and the derivation reason is visible.

### User Story 4: Model Repairs Common Errors (Priority: P0)

Recoverable mistakes produce structured recovery instead of prose-only failure.

**Why this priority**: The highest-frequency model errors are small and local; structured repair avoids needless user interruption.

**Independent Test**: Error conversion tests assert recovery for missing `database`, missing `execution_mode`, wrong SQL tool, unknown public identifier, and stale workflow `plan_id`.

**Acceptance Scenarios**:

1. **Given** `database` is missing, **When** validation fails, **Then** recovery recommends reading `shardingsphere://databases`.
2. **Given** side-effecting SQL is sent to `execute_query`, **When** validation fails, **Then** recovery recommends `execute_update` preview.
3. **Given** a workflow `plan_id` is stale or outside the current session, **When** apply or validation fails, **Then** recovery recommends current-session completion or replanning.

### User Story 5: Model Fills Arguments With Less Guessing (Priority: P1)

Completion, examples, and algorithm resources give a model enough local information to fill safe arguments without inventing hidden values.

**Why this priority**: These are small improvements that remove friction after the P0 safety path is stable.

**Independent Test**: Completion tests and resource handler tests assert context-scoped candidates, contains fallback, and algorithm property templates.

**Acceptance Scenarios**:

1. **Given** prefix completion has no match, **When** a safe contains match exists, **Then** completion can return it deterministically.
2. **Given** database/schema/table context is supplied, **When** table or column completion runs, **Then** candidates prefer that context.
3. **Given** encrypt or mask algorithms are listed, **When** the resource response is returned, **Then** it includes required properties,
   optional properties, defaults, secret flags, and capability hints when known.

### User Story 6: First-Time MCP Connection Is Clear (Priority: P2)

A user or model setting up MCP can see the endpoint, client configuration shape, token requirement, and common failure causes without reading source code.

**Why this priority**: First-use failures are not core runtime behavior, but they strongly affect whether the MCP feels native.

**Independent Test**: Documentation review and startup-output tests where applicable.

**Acceptance Scenarios**:

1. **Given** HTTP mode starts, **When** startup succeeds, **Then** endpoint, config path, log path, token hint, and runtime database count are clear.
2. **Given** STDIO mode starts, **When** logs are emitted, **Then** stdout remains reserved for MCP protocol and diagnostics use stderr or files.
3. **Given** a client fails to connect, **When** the user reads troubleshooting, **Then** the common Java, driver, token, STDIO, and empty-surface causes are covered.

## Edge Cases

- The server cannot safely infer a URI, next tool argument, or missing value; it must omit guesses and ask the model to ask the user.
- A historical design document contains obsolete public tool names; it must be labeled so models do not treat it as current contract.
- Descriptor and README public lists drift; lightweight checks must catch the obvious mismatch.
- Completion has no prefix match; contains fallback must remain deterministic and bounded.
- A resource list is large; count and pagination fields must clearly say whether more data exists.
- A side-effecting call omits `execution_mode`; it must recover to preview and not execute.
- A workflow plan is session-scoped; stale or cross-session IDs must recover by completion or replanning.
- STDIO mode must not write human logs to stdout.

## Requirements

### Functional Requirements

- **FR-001**: The current public surface MUST be consistent across README, descriptors, and `shardingsphere://capabilities`.
- **FR-002**: Server instructions MUST tell models to start from `shardingsphere://capabilities` and resource-first discovery.
- **FR-003**: Historical PRD/design/spec material MUST be marked as non-current when it mentions obsolete tool matrices or deferred capabilities.
- **FR-004**: `next_actions` MUST be the primary model-facing next-step field for successful outputs, previews, workflows, and recoverable errors.
- **FR-005**: Next actions MUST include action kind, reason, approval requirement, and the target tool or resource when known.
- **FR-006**: Preview outputs MUST include reusable execution or apply arguments when the server can know them safely.
- **FR-007**: `search_metadata` MUST return `resource_uri`, `parent_resource_uri`, and `next_resource_uris` when they are safely derivable from public resource templates.
- **FR-008**: URI derivation failures MUST expose derivation status and reason rather than guessed URIs.
- **FR-009**: Core output schemas MUST match actual payloads for `search_metadata`, `execute_query`, `execute_update`, `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow`, and `validate_workflow`.
- **FR-010**: Recovery MUST cover missing `database`, missing `execution_mode`, wrong SQL tool choice, unknown tool/resource, and stale or unavailable workflow `plan_id`.
- **FR-011**: Side-effecting recovery MUST preserve preview-first and user-approval requirements.
- **FR-012**: Regression guards MUST stay lightweight: descriptor lint, capabilities shape checks, and focused contract assertions, not a large golden transcript suite.
- **FR-013**: Resource responses SHOULD include `self_uri`, `parent_uri`, `count`, and `next_resources` where the values are safe and already public.
- **FR-014**: Complex outputs SHOULD include compact static examples that do not contain secrets, production identifiers, or environment-specific paths.
- **FR-015**: Completion SHOULD support deterministic prefix-first, contains fallback, context-scoped ordering, and current-session `plan_id` ordering.
- **FR-016**: Completion MUST NOT use vector search, model calls, cross-session history, or user behavior learning in this increment.
- **FR-017**: Encrypt and mask algorithm resources SHOULD expose required properties, optional properties, defaults, secret flags, and capability hints.
- **FR-018**: Approval-related tool arguments, including `approved_steps`, SHOULD be documented with allowed values and preview-to-execute reuse guidance.
- **FR-019**: Startup and client documentation SHOULD cover HTTP endpoint, STDIO behavior, token requirement, config paths,
  log paths, Java version, JDBC driver, and empty public surface troubleshooting.
- **FR-020**: Opt-in LLM usability additions MAY cover only a few high-value scenarios and MUST remain outside default CI.
- **FR-021**: The increment MUST NOT add a broad tool matrix, planner, graph engine, vector search, cross-session memory, RBAC platform, benchmark leaderboard, or hidden execution shortcut.

### Key Entities

- **Public Surface**: The model-visible set of tools, resources, resource templates, prompts, completion targets, descriptors, and capability fingerprints.
- **Next Action**: Structured guidance that tells the model to read a resource, call a tool, ask the user, retry with repaired arguments, or stop.
- **Recovery Envelope**: Structured error metadata that lets a model safely repair common failures.
- **Resource URI Derivation**: Safe mapping from metadata search results to public resource templates.
- **Output Schema Contract**: Descriptor-declared shape that must match the actual tool payload.
- **Algorithm Property Template**: Model-visible property requirements for encrypt and mask algorithms.
- **Startup Hint**: Runtime-visible connection and troubleshooting information for HTTP or STDIO mode.

## Success Criteria

- **SC-001**: A model can identify the current public MCP surface without relying on obsolete PRD or design tool names.
- **SC-002**: A model can continue from preview, workflow, and recoverable error payloads by following `next_actions`.
- **SC-003**: Metadata search results point to readable resources when URI derivation is safe.
- **SC-004**: Common model mistakes recover without guessed secrets, hidden physical objects, or side-effect execution.
- **SC-005**: Core output schemas are accurate enough for a model to parse returned payloads without extra project knowledge.
- **SC-006**: The requirements stay lightweight and avoid the explicitly excluded over-designed systems.

## Assumptions

- The existing descriptor catalog remains the source of truth for model-facing identifiers.
- Workflow `plan_id` remains session-scoped and non-durable.
- ShardingSphere-Proxy is the primary runtime topology for MCP workflows.
- HTTP and STDIO transports remain supported.
- P1 and P2 items are sequenced after P0 and may be split into independent future tasks.

## Out of Scope

- New persistent approval system, RBAC, tenant model, or external ticket workflow.
- New automatic planner, tool-matrix compatibility layer, graph traversal service, vector retrieval, or semantic ranking.
- Default-CI real-model E2E or broad model-confusion matrices.
- MCP-native sampling, progress, logging, or roots work unless a future SDK-supported requirement justifies it separately.
- Metadata freshness semantics, config environment variable interpolation, and current-session workflow listing resources.
