# Feature Specification: ShardingSphere MCP Resource Metadata

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-23
**Status**: Draft
**Input**: User description: "Clarify the ShardingSphere-specific MCP resource metadata requirements. Rename the internal resource extension model and YAML
authoring block to ShardingSphere-owned metadata, expose the metadata through MCP `_meta`, and do not preserve compatibility aliases."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Discover ShardingSphere Resource Metadata Through MCP (Priority: P1)

As an MCP client or model, I want ShardingSphere-specific resource semantics to be exposed through the official MCP resource `_meta` field so that I can discover
resource kind, object scope, URI variables, related tools, and related resources without depending on a non-standard catalog shape.

**Why this priority**: This is the protocol correctness requirement. ShardingSphere resource semantics are useful only if clients can discover them through
standard MCP `resources/list` and `resources/templates/list` responses.

**Independent Test**: Call official MCP resource discovery for fixed resources and resource templates, then verify ShardingSphere metadata appears under `_meta`
with `org.apache.shardingsphere/` namespaced keys.

**Acceptance Scenarios**:

1. **Given** a fixed resource descriptor with ShardingSphere metadata, **When** an MCP client calls `resources/list`, **Then** the resource response includes
   `_meta` entries for the ShardingSphere metadata.
2. **Given** a templated resource descriptor with URI variables, **When** an MCP client calls `resources/templates/list`, **Then** the resource template response
   includes `_meta` entries describing the URI variables.
3. **Given** a client that only understands standard MCP resource discovery, **When** it inspects ShardingSphere resources, **Then** it can find ShardingSphere
   metadata without reading `shardingsphere://capabilities` first.

---

### User Story 2 - Preserve Typed Internal Metadata for Runtime Decisions (Priority: P1)

As a ShardingSphere MCP runtime developer, I want ShardingSphere resource metadata to remain available as a typed internal model so that handlers can safely decide
response mode, object scope, empty-state recovery, and large-result guidance without parsing raw metadata maps.

**Why this priority**: Runtime behavior already depends on resource kind and object scope. Moving data to `_meta` must not force fragile string-map parsing throughout the runtime.

**Independent Test**: Execute metadata resource handlers for list and detail resources, then verify their existing behavior remains driven by typed metadata rather than ad hoc `_meta` map reads.

**Acceptance Scenarios**:

1. **Given** a detail resource such as `shardingsphere://databases/{database}`, **When** the handler reads metadata for one logical database, **Then** it returns
   detail-shaped payload fields including `response_mode`, `found`, and `object_scope`.
2. **Given** a list resource that returns no items, **When** the handler builds an empty response, **Then** it uses the ShardingSphere object scope to create recovery guidance.
3. **Given** a broad metadata list response, **When** the result exceeds the large-result threshold, **Then** the handler uses the object scope to create search
   guidance for the relevant metadata type.

---

### User Story 3 - Use Intent-Revealing ShardingSphere Naming (Priority: P2)

As a maintainer, I want the internal type name to make its ownership and purpose obvious so that readers do not confuse ShardingSphere-specific resource metadata with a generic MCP extension point.

**Why this priority**: The current `MCPResourceExtensionDescriptor` name hides ownership and invites protocol-level ambiguity. Naming should follow the repository's
readability and consistency standards.

**Independent Test**: Inspect the public and support-layer API names after the change and verify that the ShardingSphere-owned resource metadata type is named `ShardingSphereMCPResourceMetadata`.

**Acceptance Scenarios**:

1. **Given** a maintainer reads descriptor support code, **When** they see the resource metadata type, **Then** the name identifies it as ShardingSphere-owned MCP resource metadata.
2. **Given** YAML uses the ShardingSphere-owned authoring block, **When** descriptors are loaded, **Then** the Java model name and YAML key both avoid the old
   generic `extension` terminology.

---

### Edge Cases

- A resource or template has both raw `meta` entries and typed ShardingSphere metadata for the same `org.apache.shardingsphere/` key.
- A templated resource contains URI variables but the typed metadata omits one variable description.
- A typed metadata block is absent for a resource that has no ShardingSphere-specific semantics.
- Existing capability catalog payloads and official MCP discovery expose conflicting ShardingSphere metadata for the same resource.
- A client reads Java SDK objects where the Java accessor is named `meta()` but the serialized MCP field is `_meta`.
- A descriptor still uses the old `extension` authoring key after the no-compatibility migration.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST expose ShardingSphere-specific MCP resource metadata through the official MCP `_meta` field for both fixed resources and resource templates.
- **FR-002**: The system MUST use `org.apache.shardingsphere/` namespaced metadata keys for all ShardingSphere-specific `_meta` entries.
- **FR-003**: The system MUST retain a typed internal representation named `ShardingSphereMCPResourceMetadata` for ShardingSphere resource semantics.
- **FR-004**: The typed metadata MUST cover resource URI or URI template, URI variables, resource kind, object scope, feature, related tools, related resources, and use-before guidance.
- **FR-005**: Descriptor loading MUST detect duplicate ownership before merging typed ShardingSphere resource metadata into descriptor `meta`.
- **FR-006**: Descriptor loading MUST fail fast when raw `meta` contains a key from the fixed typed-owned ShardingSphere resource metadata key set, even if values match.
- **FR-007**: Resource template validation MUST continue to require every URI template variable to have a required typed metadata description.
- **FR-008**: Runtime metadata handlers MUST continue using typed ShardingSphere metadata for response-shape and recovery decisions instead of relying on raw `_meta` map parsing.
- **FR-009**: The ShardingSphere capability catalog MUST remain consistent with official MCP discovery metadata for every shared resource and resource template.
- **FR-010**: The change MUST preserve existing resource URIs, resource template URI templates, tool names, prompt names, and MCP protocol method compatibility.
- **FR-011**: Descriptor authoring MUST rename the old `extension` key to `shardingSphereMetadata` and MUST reject the old key after the migration.
- **FR-012**: ShardingSphere capability catalog resource and resource-template entries MUST use `_meta` for resource metadata to match the official MCP protocol shape.
- **FR-013**: Java method, field, and local variable names SHOULD use `meta` instead of `_meta` while preserving `_meta` in serialized protocol payloads.
- **FR-014**: E2E baseline contracts MUST assert `_meta` for resource and resource-template metadata when the capability catalog mirrors MCP discovery.

### Key Entities *(include if feature involves data)*

- **ShardingSphereMCPResourceMetadata**: ShardingSphere-owned typed model for resource semantics. It maps one fixed resource URI or resource template URI template
  to typed metadata fields and can be converted to MCP `_meta` entries.
- **YAML `shardingSphereMetadata` Block**: Descriptor authoring block for ShardingSphere resource metadata. It replaces the old generic `extension` block without
  backward-compatible aliases.
- **MCPResourceDescriptor**: Internal descriptor for MCP resources and resource templates. It carries protocol-facing fields plus metadata that the MCP Java SDK serializes as `_meta`.
- **MCPShardingSphereMetadataKeys**: Namespaced key registry for ShardingSphere MCP metadata fields under the `org.apache.shardingsphere/` prefix.
- **MCP Descriptor Catalog**: Loaded descriptor set that contains resources, resource templates, tools, prompts, completions, navigation, runtime descriptors, and ShardingSphere resource metadata.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every resource with ShardingSphere resource metadata exposes the same metadata through official MCP discovery `_meta`.
- **SC-002**: Every resource template with URI variables exposes variable metadata through official MCP discovery `_meta`.
- **SC-003**: No resource metadata field is available only through `shardingsphere://capabilities` when the same resource is listed by official MCP discovery.
- **SC-004**: Existing metadata resource handler behavior for list resources, detail resources, empty states, and large-result guidance remains unchanged.
- **SC-005**: The old generic type name is no longer used for the ShardingSphere resource metadata model after implementation.
- **SC-006**: No descriptor authoring surface in `mcp` or `test/e2e/mcp` uses the old `extension` key after implementation.
- **SC-007**: Capability catalog fixed-resource and resource-template entries use `_meta` instead of `meta` for metadata-bearing entries.
- **SC-008**: Contract e2e verification runs with MCP contract checks enabled and fails if `_meta` is missing from discovery or baseline projections.

## Confirmed Decisions

- No compatibility alias for the old `extension` authoring key.
- Java code uses `meta` naming for fields, methods, and locals; serialized MCP payloads use `_meta`.
- Duplicate ownership policy is fail-fast for typed-owned ShardingSphere resource metadata keys.
- Scope includes both `mcp` and `test/e2e/mcp`.
- External cross-model review uses Codex CLI.

## Assumptions

- The MCP Java SDK's Java accessor name `meta()` is acceptable because it serializes to the protocol field `_meta`.
- This specification covers descriptor and metadata-contract changes only. It does not introduce new MCP tools, prompts, transports, resource URIs, or workflow behavior.
- No branch switch, commit, push, or destructive cleanup is part of this specification task.
