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

# Feature Specification: MCP Annotations Protocol Compliance

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-14
**Status**: Completed
**Input**: User requests that MCP resource and tool annotation requirements be managed through Speckit without switching branches.

## Goal

Make the ShardingSphere MCP annotation model strictly align with the selected MCP protocol baseline while preserving ShardingSphere descriptor safety policy.

This package exists because MCP defines two different annotation shapes:

- `Annotations` for resources, resource templates, resource links, embedded resources, and content blocks.
- `ToolAnnotations` for tool behavior hints.

They are not interchangeable and must not be collapsed into one Java model or one YAML DTO.
The implementation must preserve MCP optional-field semantics while still allowing ShardingSphere descriptors to require explicit public-tool safety hints.

## Source Baseline

Implementation tasks derived from this package MUST use MCP Specification `2025-11-25` as the source of truth unless the repository protocol target changes first.
Official source pages used for this requirement package:

- Schema reference for `Annotations`, `Role`, `Resource`, `ResourceTemplate`, `ResourceLink`, `Tool`, and `ToolAnnotations`:
  https://modelcontextprotocol.io/specification/2025-11-25/schema
- Tool descriptor and tool result semantics:
  https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- Resource and resource template semantics:
  https://modelcontextprotocol.io/specification/2025-11-25/server/resources

Source-driven facts locked by this package:

- `Annotations.audience` is optional and contains only MCP `Role` values: `user` and `assistant`.
- `Annotations.priority` is optional. A value of `1.0` means most important, and `0.0` means least important.
- `Annotations.lastModified` is optional and should be an ISO 8601 formatted string.
- `Tool.annotations` is optional and uses `ToolAnnotations`.
- `ToolAnnotations.readOnlyHint` defaults to `false` when absent.
- `ToolAnnotations.destructiveHint` defaults to `true` when absent and is meaningful only when `readOnlyHint == false`.
- `ToolAnnotations.idempotentHint` defaults to `false` when absent and is meaningful only when `readOnlyHint == false`.
- `ToolAnnotations.openWorldHint` defaults to `true` when absent.
- Tool annotation values are hints for clients and do not replace server-side authorization, approval, or runtime safety checks.
- Primitive tool hint fields still need annotation-presence semantics in the Java model or swapper output.
  `MCPToolAnnotations.EMPTY` and an explicitly declared tool annotation whose booleans happen to equal MCP defaults are not equivalent.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Speckit files are maintained manually because the standard feature creation flow can change branches.
- Existing dirty worktree changes are preserved and must not be reverted.
- This package started as a documentation-only requirement pass; implementation and verification are now recorded in `tasks.md`.
- Implementation tasks must follow `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md`.
- MCP protocol optional-field semantics must be preserved. Descriptor safety policy may be stricter, but it must be documented as ShardingSphere policy.
- `MCPResourceAnnotations` and `MCPToolAnnotations` must remain separate public concepts.

## User Scenarios and Testing

### User Story 1 - Resource Annotations Preserve MCP Optional Semantics (Priority: P1)

A maintainer can describe MCP resources and resource templates without accidentally emitting fields that were not declared.

**Why this priority**: `priority` being absent is not the same as `priority: 0.0`. Losing presence semantics changes how clients can rank resources.

**Independent Test**: Load descriptors with no resource annotations, with audience only, with priority `0.0`, with priority `1.0`, and with lastModified.
Verify the resulting MCP resource output omits absent fields and includes explicitly declared fields.

**Acceptance Scenarios**:

1. **Given** a resource descriptor omits `annotations`, **When** `resources/list` is built, **Then** the MCP resource omits `annotations`.
2. **Given** a resource descriptor explicitly sets `priority: 0.0`, **When** the MCP resource is built, **Then** `priority: 0.0` is emitted.
3. **Given** a resource descriptor omits priority, **When** the MCP resource is built, **Then** no default priority is emitted.
4. **Given** a resource descriptor declares `audience`, **When** the descriptor is loaded, **Then** every audience value is an MCP role.

---

### User Story 2 - Tool Annotations Use MCP Defaults Without Wrapper Booleans (Priority: P1)

A maintainer can reason about tool hints using MCP default values without carrying nullable boolean wrapper types through the Java API model.

**Why this priority**: MCP defines defaults for all four tool boolean hints.
Primitive booleans can represent the effective protocol value if descriptor loading keeps track of explicit public-tool policy.

**Independent Test**: Construct `MCPToolAnnotations.EMPTY`, load a descriptor with explicit tool hints, and build `tools/list`.
Verify primitive values match MCP defaults and explicit descriptor values override them.

**Acceptance Scenarios**:

1. **Given** `MCPToolAnnotations.EMPTY`, **When** a test fixture uses it, **Then** its effective values are `readOnlyHint=false`,
   `destructiveHint=true`, `idempotentHint=false`, and `openWorldHint=true`.
2. **Given** a production public tool descriptor, **When** it is loaded from YAML, **Then** the raw descriptor must explicitly contain all four boolean hint keys.
3. **Given** a read-only public tool, **When** its annotations are validated, **Then** `readOnlyHint=true` and `destructiveHint=false` are required by ShardingSphere descriptor policy.
4. **Given** a side-effecting public tool, **When** its annotations are validated, **Then** `readOnlyHint=false` and an explicit destructive/additive decision are required.

---

### User Story 3 - Descriptor Validation Separates MCP Schema From ShardingSphere Policy (Priority: P1)

A descriptor author gets precise validation errors that distinguish official MCP schema violations from ShardingSphere safety-policy violations.

**Why this priority**: MCP allows omitted annotations, but ShardingSphere production tool descriptors require explicit safety hints. Mixing the two rules makes future protocol upgrades confusing.

**Independent Test**: Run descriptor validation against valid resources, invalid resource annotations, valid public tools,
test fixtures using empty tool annotations, and production tools missing required safety hints.

**Acceptance Scenarios**:

1. **Given** a resource descriptor contains `annotations: {}`, **When** raw YAML validation runs, **Then** validation fails because empty annotation maps should be omitted.
2. **Given** a resource descriptor contains `audience: [model]`, **When** validation runs, **Then** validation fails with the path of the invalid value.
3. **Given** a resource descriptor contains `priority: 1.2`, **When** validation runs, **Then** validation fails because priority is outside the MCP range.
4. **Given** a production tool omits `openWorldHint`, **When** descriptor validation runs, **Then** validation fails as a ShardingSphere public-tool policy violation.

---

### User Story 4 - MCP Output Contains Only Official Annotation Fields (Priority: P2)

A generic MCP client sees only official MCP annotation fields in `resources/list`, `resources/templates/list`, and `tools/list`.

**Why this priority**: Internal descriptor metadata such as workflow runtime, side-effect scope, or ShardingSphere catalog hints must not leak into protocol annotations.

**Independent Test**: Build official list responses and inspect serialized annotations.
Verify resources use only `audience`, `priority`, and `lastModified`; tools use only `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, and `openWorldHint`.

**Acceptance Scenarios**:

1. **Given** a resource has extension metadata, **When** MCP resource output is built, **Then** extension metadata is not placed under `annotations`.
2. **Given** a tool has runtime metadata, **When** MCP tool output is built, **Then** runtime metadata is not placed under `annotations`.
3. **Given** an annotation object is empty after filtering absent values, **When** output is built, **Then** `annotations` is omitted.

## Edge Cases

- `priority: 0.0` is a valid explicit value and must not be treated as empty.
- Missing priority is not equivalent to `0.0`.
- `Double.NaN`, `Infinity`, and `-Infinity` are never valid MCP JSON values and must never be emitted.
- `lastModified` may be absent, but when present it must parse as an ISO 8601 instant or offset date-time acceptable to Java time parsing.
- Tool hints are not security boundaries. Runtime approval and validation remain required even when annotations say a tool is non-destructive.
- Test fixtures may use empty tool annotations when the descriptor is not exposed to a production MCP client.
- Loaded descriptor YAML from test resources should satisfy the same raw annotation-shape rules when it participates in catalog loading.
  Programmatic unit-test fixture objects may use `MCPToolAnnotations.EMPTY`.

## Requirements

### Functional Requirements

- **MAC-FR-001**: This package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands or scripts.
- **MAC-FR-002**: `MCPResourceAnnotations` MUST model only MCP `Annotations` fields: `audience`, `priority`, and `lastModified`.
- **MAC-FR-003**: `MCPToolAnnotations` MUST model only MCP `ToolAnnotations` fields: `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, and `openWorldHint`.
- **MAC-FR-004**: Resource and tool annotation Java models MUST remain separate and MUST NOT be merged into a shared public annotation model.
- **MAC-FR-005**: Resource priority MUST preserve presence semantics. The implementation MUST distinguish absent priority from explicit `0.0`.
- **MAC-FR-006**: Tool boolean hints SHOULD use primitive boolean fields in the Java API model and MUST use MCP default effective values when absent.
- **MAC-FR-006a**: Tool annotations MUST preserve annotation-object presence separately from primitive boolean values.
  `MCPToolAnnotations.EMPTY` MUST remain distinguishable from an explicitly declared annotation object whose values equal MCP defaults.
- **MAC-FR-007**: YAML raw validation MUST preserve explicit-key policy for loaded descriptor tools before primitive defaults are applied.
- **MAC-FR-008**: Production public tool descriptors MUST explicitly declare `readOnlyHint`, `destructiveHint`, `idempotentHint`, and `openWorldHint`.
- **MAC-FR-009**: Test fixtures and internal non-public descriptors MAY use `MCPToolAnnotations.EMPTY` when they do not produce a production `tools/list` surface.
- **MAC-FR-010**: Resource annotations MAY be absent for ordinary resources when no audience, priority, or last-modified signal is needed.
- **MAC-FR-011**: Raw descriptor validation MUST reject empty annotation maps that contain no MCP fields.
- **MAC-FR-012**: Raw descriptor validation MUST reject resource annotation keys outside `audience`, `priority`, and `lastModified`.
- **MAC-FR-013**: Raw descriptor validation MUST reject tool annotation keys outside `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, and `openWorldHint`.
- **MAC-FR-014**: Resource audience values MUST be limited to MCP roles `user` and `assistant`.
- **MAC-FR-015**: Resource priority MUST be finite and within `0.0` through `1.0` when present.
- **MAC-FR-016**: Resource `lastModified` MUST be an ISO 8601 timestamp with an explicit offset or UTC marker when present.
- **MAC-FR-017**: Descriptor validation MUST reject contradictory tool hints, including `readOnlyHint=true` with `destructiveHint=true`.
- **MAC-FR-018**: MCP resource and resource-template output MUST omit annotations when the effective resource annotation object is empty.
- **MAC-FR-019**: MCP resource output MUST pass `null` or omit priority when priority is absent; it MUST NOT pass `NaN` or default `0.0`.
- **MAC-FR-020**: MCP tool output MUST not include ShardingSphere runtime or extension metadata under `annotations`.

### Key Entities

- **MCPResourceAnnotations**: Java API model for MCP `Annotations`, scoped to resources and resource templates in current descriptors.
- **MCPToolAnnotations**: Java API model for MCP `ToolAnnotations`, scoped to tools.
- **YamlMCPResourceAnnotations**: YAML DTO for resource annotation input before validation and swapping.
- **YamlMCPToolAnnotations**: YAML DTO for tool annotation input before validation and swapping.
- **Descriptor raw annotation map**: SnakeYAML map used for explicit key, empty-map, and presence validation before default values are applied.

## Success Criteria

### Measurable Outcomes

- **SC-001**: No production MCP resource or tool emits annotation fields outside the selected MCP schema.
- **SC-002**: All production public tools declare the four MCP boolean hints explicitly in descriptor YAML.
- **SC-003**: Tests prove absent resource priority and explicit `priority: 0.0` are serialized differently.
- **SC-004**: Tests prove invalid audience, invalid priority, invalid lastModified, empty annotation maps, and contradictory tool hints fail before server startup.

## Assumptions

- The repository protocol baseline remains MCP `2025-11-25` for this package.
- The MCP Java SDK may still use boxed constructor arguments internally; ShardingSphere API models can use primitive fields while adapters preserve optional wire semantics.
- Descriptor YAML is developer-authored, not end-user runtime configuration.

## Completion State

- Annotation API models, YAML DTOs, raw descriptor validation, semantic catalog validation, payload output, SDK mapping, descriptors, and README guidance are implemented under this package.
- Scoped unit tests, Checkstyle, Spotless, static searches, and touched-file `git diff --check` evidence are recorded in `tasks.md`.
- Broader MCP descriptor schema alignment for non-annotation fields such as `icons`, `size`, and `execution` is outside this package.
  It is tracked separately by `.specify/specs/018-mcp-descriptor-schema-compliance/`.
