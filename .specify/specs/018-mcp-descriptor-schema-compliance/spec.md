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

# Feature Specification: MCP Descriptor Schema Compliance

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-14
**Status**: Draft
**Input**: Follow-up analysis found that non-annotation MCP descriptor fields need their own compliance package instead of expanding package 017.

## Goal

Align ShardingSphere MCP descriptor input, validation, payload output, and SDK mapping with MCP Specification `2025-11-25` for official descriptor fields outside annotation semantics.

Package 017 owns only `Annotations` and `ToolAnnotations`.
This package owns broader descriptor fields such as resource `size`, `icons`, tool `execution`, and any `_meta` boundary cleanup needed for schema-exact output.
MCP Java SDK `1.1.2` currently supports only a subset of those official fields, so this implementation slice only enables fixed `Resource.size`.

## Source Baseline

Implementation tasks derived from this package MUST use MCP Specification `2025-11-25` as the source of truth unless the repository protocol target changes first.
Official source pages used for this requirement package:

- Schema reference for `Resource`, `ResourceTemplate`, `Tool`, `ToolExecution`, `Icon`, and `_meta`:
  https://modelcontextprotocol.io/specification/2025-11-25/schema
- Tool descriptor and tool result semantics:
  https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- Resource and resource template semantics:
  https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- General `_meta` and JSON Schema semantics:
  https://modelcontextprotocol.io/specification/2025-11-25/basic

Source-driven facts locked by this package:

- `Resource` may expose official fields including `icons`, `mimeType`, annotations, and `size`; SDK `1.1.2` exposes `size` but not `icons`.
- `ResourceTemplate` may expose official fields including `icons`, `mimeType`, and annotations, but not `size`; SDK `1.1.2` does not expose `icons`.
- `Tool` may expose official fields including `icons`, `inputSchema`, `outputSchema`, annotations, and `execution`; SDK `1.1.2` exposes neither `icons` nor `execution`.
- `ToolExecution.taskSupport` is optional and defaults to `forbidden` when absent.
- `_meta` is the official MCP extension metadata field. ShardingSphere descriptor-only metadata must not leak into official descriptor fields accidentally.
- JSON Schemas without `$schema` default to JSON Schema 2020-12.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Existing dirty worktree changes are preserved and must not be reverted.
- Package 017 remains the annotation-compliance package and MUST NOT be reopened for non-annotation descriptor fields.
- ShardingSphere descriptor YAML remains developer-authored and is not end-user runtime configuration.
- Official MCP fields must be modeled at the API/support/bootstrap boundaries, not hidden inside annotation objects.
- Descriptor safety policy may be stricter than MCP optionality, but stricter rules must be documented as ShardingSphere policy.

## User Scenarios and Testing

### User Story 1 - Official Resource Descriptor Fields Are Accepted (Priority: P1)

A descriptor maintainer can use official MCP resource fields without raw YAML validation rejecting them as unknown.

**Independent Test**: Load fixed resource descriptors containing `size`.
Verify validation accepts valid values, rejects malformed values, and preserves absent-field omission.

**Acceptance Scenarios**:

1. **Given** a fixed resource descriptor contains a valid `size`, **When** descriptor validation runs, **Then** validation succeeds and output preserves the value.
2. **Given** a resource descriptor omits `size`, **When** output is built, **Then** no default value is emitted.
3. **Given** a resource template descriptor contains `size`, **When** raw validation runs, **Then** validation fails because templates do not support `size`.

### User Story 2 - Official Tool Descriptor Fields Are Deferred by the SDK Boundary (Priority: P1)

A descriptor maintainer can see which official MCP tool fields are deferred because the current SDK does not expose them.

**Independent Test**: Verify `icons` and `execution.taskSupport` remain documented as SDK limitations and are not emitted through metadata or annotations.

**Acceptance Scenarios**:

1. **Given** the local SDK is `1.1.2`, **When** tool descriptor support is inventoried, **Then** `icons` and `execution` are recorded as unsupported by the SDK boundary.
2. **Given** a tool descriptor omits `execution`, **When** output is built, **Then** existing output remains unchanged.
3. **Given** future SDK support is available, **When** this package is extended, **Then** `execution.taskSupport` must accept only `forbidden`, `optional`, or `required`.

### User Story 3 - Metadata Boundaries Are Schema-Exact (Priority: P2)

A generic MCP client sees official MCP descriptor fields under official names, while ShardingSphere-only implementation metadata remains clearly separated.

**Independent Test**: Build descriptor catalog payloads and SDK objects with official fields and ShardingSphere extension metadata.
Verify official fields are not placed under internal metadata and internal metadata is not emitted under annotations.

**Acceptance Scenarios**:

1. **Given** a descriptor has ShardingSphere extension metadata, **When** MCP output is built,
   **Then** extension metadata does not appear under `annotations`, `icons`, `execution`, or other official fields.
2. **Given** a descriptor has official `_meta`, **When** SDK mapping runs, **Then** it is passed through the MCP metadata boundary without renaming ambiguity.

## Edge Cases

- `size` must be non-negative when present.
- Empty official arrays such as `icons: []` should be rejected or omitted according to ShardingSphere descriptor policy when SDK support is added.
- Invalid icon URI schemes or invalid MIME types must not bypass validation if ShardingSphere decides to validate icon safety in a future SDK-supported slice.
- `execution.taskSupport` accepts only `forbidden`, `optional`, or `required`.
- Adding official fields must not change existing descriptors that omit those fields.

## Requirements

### Functional Requirements

- **MDS-FR-001**: This package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands or scripts.
- **MDS-FR-002**: Raw YAML validation MUST allow SDK-supported official MCP descriptor fields selected by this implementation slice and reject malformed values.
- **MDS-FR-003**: Resource descriptor models MUST preserve presence semantics for optional `size`.
- **MDS-FR-004**: Official fields unsupported by MCP Java SDK `1.1.2`, including `icons` and `Tool.execution`, MUST be documented as deferred instead of being emitted through metadata.
- **MDS-FR-005**: Future tool execution support MUST support only MCP `taskSupport` values `forbidden`, `optional`, and `required`.
- **MDS-FR-006**: SDK mapping MUST emit official descriptor fields through MCP SDK fields, not through annotations or ShardingSphere metadata.
- **MDS-FR-007**: Descriptor catalog payloads MUST use official field names for official MCP data and must keep ShardingSphere-only metadata separate.
- **MDS-FR-008**: Existing descriptor behavior MUST remain unchanged when descriptors omit the newly supported fields.
- **MDS-FR-009**: README guidance MUST distinguish official MCP descriptor fields from ShardingSphere descriptor-only metadata.

### Key Entities

- **MCPResourceDescriptor**: API model for official MCP resource and resource-template descriptor fields.
- **MCPToolDescriptor**: API model for official MCP tool descriptor fields.
- **YamlMCPResourceDescriptor**: YAML DTO for developer-authored resource descriptors before validation and swapping.
- **YamlMCPToolDescriptor**: YAML DTO for developer-authored tool descriptors before validation and swapping.
- **MCPDescriptorYamlKeyValidator**: Raw YAML boundary that enforces known official and ShardingSphere descriptor fields before defaults are applied.
- **Bootstrap specification factories**: SDK boundary where official descriptor fields are converted into MCP Java SDK schema objects.

## Success Criteria

- **SC-001**: Valid official MCP descriptor fields selected by this package are accepted by raw YAML validation.
- **SC-002**: Invalid official descriptor field values fail before server startup.
- **SC-003**: SDK mapping tests prove selected SDK-supported official fields are emitted through MCP SDK schema objects.
- **SC-004**: Existing descriptors that omit these fields produce the same payloads and SDK objects as before.
- **SC-005**: SDK-unsupported official fields are recorded as deferred and are not emitted through annotations or ShardingSphere-only metadata.

## Assumptions

- MCP Java SDK `1.1.2` exposes enough schema surface for selected fields; if not, adapters must document the SDK limitation instead of fabricating protocol output.
- Descriptor YAML is developer-authored, so strict validation is acceptable at startup time.
- Package 017 has already completed annotation-specific compliance.
