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

# Feature Specification: MCP Natural Language Markdown Resources

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-17
**Status**: Draft
**Input**: User agreed to store suitable MCP natural-language content in Markdown files and requested a Speckit requirement package.
The package must list what should change, why it should change, which natural-language content must not move to Markdown, and why.
User explicitly requested not to switch branches.

## Branch Constraint

- Work must remain on the existing `001-shardingsphere-mcp` branch.
- Speckit branch creation or branch switching commands are forbidden for this requirement package.
- This round is documentation-only; production, test, descriptor, distribution, and runtime behavior changes require a later explicit implementation command.
- Existing worktree changes are out of scope and must not be reverted, reformatted, staged, or otherwise changed by this package.

## Goal

Separate MCP model-facing authored prose from Java behavior constants so reviewers can read, edit, and translate instruction-like content as resources.
Structured descriptors, runtime diagnostics, and machine-readable payload contracts must stay in their existing structured homes.

The first target is the MCP server-level instruction text currently embedded as a transport constant.
That text is prompt-like operational guidance for MCP clients, not protocol behavior.
It should be owned by a Markdown resource and loaded by the MCP bootstrap layer when the server exposes instructions.

This package also classifies adjacent MCP natural-language content so future work does not move every English string into Markdown blindly.
Markdown is the approved home for paragraph or list style prompt/instruction content aimed at model behavior.
YAML descriptors remain the approved home for tool/resource/schema metadata.
Runtime errors, validation messages, logs, enum values, JSON payload keys, and short machine contract labels must remain outside Markdown.
A separate structured message catalog or descriptor model would be needed to externalize those categories.

## Clarifications

### Session 2026-05-17

- User agreed to use Markdown files for suitable MCP natural-language content.
- User asked for the concrete change list, reasons for each change, and a separate list of natural-language content that must not move to Markdown.
- User requested a new Speckit spec and explicitly forbade branch switching.
- Speckit's standard feature creation script is not used in this round because it creates and checks out a new branch.
- Current MCP resources already include descriptor YAML files under `META-INF/shardingsphere-mcp/mcp-descriptors`.
- Current MCP resources already include prompt Markdown files under `META-INF/shardingsphere-mcp/prompts`.
- Current MCP support code already has loader patterns for descriptor YAML and prompt Markdown resources.
- User confirmed the next design step must cover all phases, not only the first server-instruction migration slice.
- User accepted `mcp/bootstrap/src/main/resources/META-INF/shardingsphere-mcp/instructions/server-instructions.md` as the server instruction resource path.
- Existing MCP prompt Markdown resources include ASF license headers, so model-facing Markdown loading must separate source headers from delivered prompt/instruction content.
- The Java constant layer should stay limited to behavior constants, protocol names, resource paths, or fallback-free loading concerns.
- Broader model-facing workflow guidance must be classified across phases, but structured response payloads must not be converted to free-form Markdown.

## User Scenarios and Testing

### User Story 1 - Maintainers Review Server Instructions as Authored Content (Priority: P1)

As an MCP maintainer, I want server-level instructions to live in an authored Markdown resource so that prompt-like behavior guidance can be reviewed without editing transport constants.

**Independent Test**: Inspect the MCP bootstrap resources and server initialization.
Verify the server instructions are loaded from one Markdown file and the delivered instruction text matches that file.

**Acceptance Scenarios**:

1. Given a reviewer searches MCP bootstrap resources, when they open the server instruction resource, then the full server-level instruction prose is visible as Markdown.
2. Given the MCP server starts, when the server exposes its instructions to a client, then the exposed instruction text matches the authored Markdown resource.
3. Given the instruction resource is missing or empty after implementation, when the server starts or instruction loading is validated, then the failure is visible.
   It must not silently fall back to stale Java prose.
4. Given transport constants are inspected after implementation, when reviewers read them, then they contain only behavior constants, keys, or resource identifiers and not long prompt prose.

### User Story 2 - Maintainers Classify Natural Language by Correct Storage (Priority: P1)

As an MCP maintainer, I want a clear classification of natural-language content so that prompt prose, descriptor metadata, runtime diagnostics, and machine contracts do not get mixed together.

**Independent Test**: Review the future implementation plan and source map; verify each identified natural-language category has one approved storage location and a reason.

**Acceptance Scenarios**:

1. Given a string is paragraph or list style model-facing instruction prose, when it is reviewed, then Markdown is the preferred storage format.
2. Given a string describes a tool, resource, prompt, argument, JSON Schema field, or descriptor entry, when it is reviewed, then it remains structured.
   It stays in the descriptor or schema resource that owns that field.
3. Given a string is an exception, validation error, HTTP error, or log message, when it is reviewed, then it remains in code or a future message catalog rather than Markdown.
4. Given a string is a status, enum value, JSON key, URI, tool name, prompt name, or protocol-facing token, when it is reviewed, then it remains part of the machine-readable contract.
5. Given an algorithm recommendation or property description is structured metadata, when it is reviewed, then it remains in YAML or another structured data model rather than Markdown.

### User Story 3 - MCP Client Guidance Stays Semantically Stable (Priority: P1)

As an MCP client integrator, I want server instruction migration to preserve the existing guidance semantics so that clients do not lose discovery, completion, safety, or recovery expectations.

**Independent Test**: Compare the authored Markdown resource with the current server instruction content and verify the same client guidance is present after migration.

**Acceptance Scenarios**:

1. Given server instructions are migrated, when the content is reviewed, then it still tells clients to use official MCP discovery methods first.
2. Given server instructions are migrated, when completion guidance is reviewed, then it still describes completion support for operation IDs and known argument names.
3. Given server instructions are migrated, when safety guidance is reviewed, then it still distinguishes read-only SQL from updates and requires preview or approval before side-effecting operations.
4. Given server instructions are migrated, when recovery guidance is reviewed, then it still tells clients to continue from structured `next_actions` or `recovery.next_actions` entries.
5. Given optional resource catalog guidance exists, when instructions are reviewed, then it remains clear without duplicating descriptor-owned resource definitions.

### User Story 4 - Reviewers Avoid Moving Unsuitable Natural Language to Markdown (Priority: P2)

As a reviewer, I want explicit exclusions for natural-language strings that should not become Markdown resources.
The codebase must keep structured data queryable, testable, and close to its owning contract.

**Independent Test**: Run source searches for descriptor descriptions, diagnostic messages, JSON keys, and workflow payload labels.
Verify excluded categories remain outside Markdown and have documented ownership.

**Acceptance Scenarios**:

1. Given descriptor YAML contains titles or descriptions, when Markdown migration is reviewed, then those fields remain in descriptor YAML to preserve field-level ownership and validation.
2. Given existing prompt templates already live in Markdown, when the migration is reviewed, then they are not duplicated into a new Markdown location.
3. Given runtime diagnostics contain human-readable English, when the migration is reviewed, then they remain with code paths or a future message catalog.
   They are operational messages, not authored prompt content.
4. Given structured JSON payloads contain explanatory labels, when the migration is reviewed, then the payload shape remains machine-readable and deterministic.
5. Given workflow intent or keyword matching text drives classification behavior, when the migration is reviewed, then it remains deterministic.
   It stays in code or moves to a structured dictionary format, not Markdown prose.

## Edge Cases

- A Markdown resource may include exact tool names, URI schemes, JSON field names, and enum-like tokens; migration must preserve those tokens exactly.
- Source Markdown files may need ASF license headers, but delivered MCP instructions and prompt text must not include those license headers as model guidance.
- Header stripping must be narrow: remove only a leading ASF HTML comment block from Markdown resources after matching ASF license markers.
  Arbitrary comments and descriptor YAML content must not be stripped by the Markdown loader.
- Markdown resources must not introduce YAML front matter in this package; descriptor metadata remains in descriptor YAML.
- Server instructions must remain static server-level guidance; runtime database metadata and generated capability inventories stay in resources or structured payloads.
- The server instruction Markdown file is an internal classpath resource, not a new MCP `resources/list` entry.
- Server instructions are stable for a created server instance; this package does not add hot reload.
- A missing, unreadable, or blank server instruction resource must not silently produce incomplete client guidance.
- Descriptor YAML descriptions are natural language but must remain co-located with their descriptor fields.
- Existing prompt Markdown files are already in the correct storage family and must not be duplicated.
- Short diagnostic messages may be English prose but must not be treated as prompt resources.
- Structured workflow payloads may include user-visible labels, but their schema and keys are part of the client contract.
- Future localization or message-catalog work may externalize diagnostic strings, but that is a separate design from Markdown prompt resources.
- Generated directories such as `target/` must not be edited or treated as source resources.

## Requirements

### Functional Requirements

- **MNR-FR-001**: This package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing Speckit commands.
- **MNR-FR-002**: This package MUST NOT modify production code, tests, descriptors, distribution files, generated files, or runtime behavior.
  Those changes require an explicit later implementation command.
- **MNR-FR-003**: Existing unrelated worktree changes MUST NOT be reverted, reformatted, staged, or otherwise changed by this package.
- **MNR-FR-004**: MCP server-level instruction prose MUST be treated as authored model-facing content and SHOULD be moved from a Java transport constant to a Markdown resource.
- **MNR-FR-005**: The server instruction Markdown resource MUST preserve the current guidance about official MCP discovery and completion.
  It MUST also preserve optional resource catalog use, SQL read-only/update separation, preview or approval for side effects, and next-action recovery.
- **MNR-FR-006**: After implementation, Java constants in the transport area SHOULD contain only behavior constants, protocol names, resource paths, or loader identifiers, not long prompt prose.
- **MNR-FR-007**: The instruction loader MUST fail deterministically or surface a clear startup/configuration problem if the required Markdown resource is missing or blank.
- **MNR-FR-007A**: Model-facing Markdown loaders MUST remove leading ASF HTML comment headers before exposing prompt or instruction text to MCP clients.
- **MNR-FR-007B**: Existing prompt Markdown templates MUST be checked for source-header exposure if implementation changes model-facing Markdown loading.
- **MNR-FR-007C**: Future implementation MUST verify the instruction Markdown resource is included in bootstrap and MCP distribution packaging.
- **MNR-FR-007D**: Shared Markdown loading MUST NOT use path-only global caching because resource visibility can depend on class loader context.
- **MNR-FR-007E**: Shared Markdown loading MUST keep prompt placeholder extraction and rendering semantics unchanged.
- **MNR-FR-007F**: Server instruction Markdown MUST NOT embed runtime metadata, descriptor inventories, generated capability payloads, or prompt bodies.
- **MNR-FR-007G**: Server instruction Markdown MUST NOT add a new MCP resource URI or appear in `resources/list` in this package.
- **MNR-FR-007H**: Server instruction loading MUST be stable per server instance and MUST NOT introduce hot reload behavior in this package.
- **MNR-FR-008**: Existing prompt templates that already live in Markdown MUST remain canonical in their current prompt resource location unless a separate prompt reorganization package is approved.
- **MNR-FR-009**: Descriptor-owned natural-language fields MUST remain in structured descriptor YAML or schema resources.
  This includes tool titles, tool descriptions, prompt descriptions, resource descriptions, argument descriptions, and JSON Schema descriptions.
- **MNR-FR-010**: Runtime exceptions, validation messages, HTTP errors, and log messages MUST NOT be moved to Markdown resources in this package.
- **MNR-FR-011**: Protocol-facing identifiers, JSON keys, enum values, tool names, prompt names, resource URI templates, and operation IDs MUST remain contract data.
  Short status labels also MUST remain machine-readable contract data and MUST NOT be moved to Markdown.
- **MNR-FR-012**: Algorithm recommendation metadata, algorithm property descriptions, and feature capability descriptions SHOULD remain structured.
  If they are consumed as data, they SHOULD remain in YAML or another structured resource format rather than Markdown.
- **MNR-FR-013**: Workflow intent keywords, aliases, and matching dictionaries SHOULD remain in code or move to structured dictionary resources rather than Markdown prose.
- **MNR-FR-014**: The implementation plan MUST cover all migration phases: inventory, resource loading, server instructions, broader classification, exclusion protection, and verification.
- **MNR-FR-015**: Future implementation MUST include verification that delivered server instructions match the authored Markdown resource.
- **MNR-FR-016**: Future implementation MUST include source searches or review evidence showing that excluded natural-language categories were not moved into Markdown by mistake.
- **MNR-FR-017**: Future implementation MUST preserve Apache license headers for new source-controlled resource and checklist files where repository convention requires them.
- **MNR-FR-018**: Future implementation MUST report scoped verification commands and exit codes, including resource loading tests and any relevant style checks for touched modules.

### Concrete Change Candidates

- **MNR-CC-001**: Move the server-level instruction content currently owned by `MCPTransportConstants.SERVER_INSTRUCTIONS` into a Markdown resource.
  Reason: it is long model-facing instruction prose and should be reviewed as authored content.
- **MNR-CC-002**: Keep a Java constant only for the server instruction resource path if needed by the loader.
  Reason: resource lookup identifiers are behavior/configuration constants, not authored prose.
- **MNR-CC-003**: Add or reuse a resource loader for required Markdown instruction content. Reason: the server must load one canonical instruction text and fail visibly if it is unavailable.
- **MNR-CC-004**: Add tests around server instruction loading or server factory initialization. Reason: migration must prove the exposed instructions match the resource text.
- **MNR-CC-005**: Document the classification rule in the future implementation evidence or source map. Reason: reviewers need to know why descriptor descriptions and diagnostics were not moved.
- **MNR-CC-006**: Classify broader model-facing workflow guidance before moving it. Reason: some content is authored prose, while structured payload data must stay machine-readable.

### Natural Language That Must Not Move To Markdown In This Package

- **MNR-NM-001**: Descriptor YAML titles, descriptions, argument descriptions, and JSON Schema descriptions must remain in descriptor resources.
  Reason: they are structured metadata owned by a specific descriptor field and validated with that descriptor.
- **MNR-NM-002**: Existing prompt templates already stored as Markdown must not be duplicated. Reason: they already have a resource home and duplicate prompt text creates drift risk.
- **MNR-NM-003**: Exception, validation, HTTP error, and log messages must remain outside Markdown.
  Reason: they are operational diagnostics tied to code paths, tests, and observability.
  If externalized later, a structured message catalog is the proper design.
- **MNR-NM-004**: JSON keys, status labels, operation IDs, tool names, prompt names, resource URI templates, and enum-like values must remain outside Markdown.
  Reason: they are machine contracts and client integration points.
- **MNR-NM-005**: Workflow response payload labels and next-action structures must remain structured.
  Reason: clients consume them deterministically; Markdown would make them harder to validate and parse.
- **MNR-NM-006**: Algorithm property templates, recommendation metadata, and capability maps must remain structured.
  Reason: they are data records that need field-level ownership, validation, and programmatic selection.
- **MNR-NM-007**: Intent recognition keywords and aliases must not become Markdown prose. Reason: they drive matching behavior and should remain deterministic dictionary or code data.

### Key Entities

- **Server Instruction Markdown Resource**: The canonical Markdown document containing server-level MCP client guidance.
- **Instruction Resource Loader**: The bootstrap mechanism that loads required instruction Markdown and exposes it to MCP server initialization.
- **Authored Model-Facing Prose**: Paragraph or list style natural language whose purpose is to guide model/client behavior.
- **Structured Descriptor Text**: Natural-language metadata that belongs to descriptor YAML or schema fields and remains queryable through descriptor loading.
- **Runtime Diagnostic Text**: Human-readable exceptions, validation errors, HTTP errors, and logs tied to operational code paths.
- **Machine Contract Text**: Identifiers, enum-like values, JSON keys, operation IDs, tool names, prompt names, status labels, and URI templates consumed by clients or tests.

## Scope

### In Scope

- Speckit requirement package for MCP natural-language resource ownership.
- Server-level MCP instruction prose as the first Markdown migration target.
- Full phased design for inventory, resource loading, classification, migration, exclusion protection, and verification.
- Classification of which MCP natural-language categories should use Markdown.
- Classification of which MCP natural-language categories must remain structured or code-owned.
- Future verification requirements for resource loading and semantic preservation.

### Out of Scope

- Production code, test, descriptor, distribution, or runtime changes in this round.
- Branch creation, branch switching, committing, pushing, or opening a pull request.
- Moving descriptor YAML descriptions into Markdown.
- Moving runtime diagnostics or logs into Markdown.
- Reorganizing existing prompt Markdown templates.
- Designing localization, i18n, or a diagnostic message catalog.
- Replacing structured workflow payloads with free-form Markdown.
- Changing MCP protocol behavior, tool names, resource URIs, JSON payload shapes, or client-visible contracts.

## Success Criteria

### Measurable Outcomes

- **MNR-SC-001**: Reviewers can find the server-level MCP instruction content in one Markdown resource without inspecting Java transport constants.
- **MNR-SC-002**: Automated verification proves the server instructions exposed at runtime match the authored Markdown resource.
- **MNR-SC-003**: No long server-level prompt prose remains in transport constants after implementation.
- **MNR-SC-004**: The current server instruction semantics around discovery, completion, safety, preview/approval, and next-action recovery are preserved.
- **MNR-SC-005**: Descriptor YAML descriptions and schema descriptions remain structured and are not duplicated into Markdown.
- **MNR-SC-006**: Runtime diagnostic messages, logs, JSON keys, operation IDs, tool names, prompt names, and resource URI templates remain outside Markdown.
- **MNR-SC-007**: Future implementation reports verification commands and exit codes for resource loading, semantic preservation, and style checks relevant to touched modules.
- **MNR-SC-008**: No unrelated existing worktree changes are modified by this package or its later implementation.

## Assumptions

- Existing prompt Markdown resources remain canonical for prompt templates.
- Existing descriptor YAML resources remain canonical for descriptor-owned metadata.
- The accepted server instruction resource path is `mcp/bootstrap/src/main/resources/META-INF/shardingsphere-mcp/instructions/server-instructions.md`.
- The first code-changing implementation slice may still start with server instructions, but the design package must cover all phases.
- Markdown resources are intended for authored model-facing prose, not structured payload contracts.
- The active worktree may already contain unrelated MCP changes, and this package must coexist with them without cleanup or reversal.
