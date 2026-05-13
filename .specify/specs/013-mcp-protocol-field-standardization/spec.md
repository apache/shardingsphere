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

# Feature Specification: MCP Protocol Field Standardization

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-12
**Status**: Clarified
**Input**: User requests a Speckit requirement package that standardizes MCP resource, tool, prompt, completion, navigation, annotations, icons, and meta fields against the official MCP protocol.
The package must be created without switching branches.

## Goal

Standardize the ShardingSphere MCP descriptor and catalog surface so official MCP objects expose only official MCP fields.
ShardingSphere-specific guidance, diagnostics, and workflow hints must have explicit boundaries.

The target is a clean protocol contract, not backward compatibility.
Existing non-standard top-level descriptor fields MUST be removed, moved into namespaced `meta`, or converted into internal typed configuration.

The reference protocol is locked to official MCP schema version `2025-11-25`. The requirement MUST NOT follow a moving `latest` target during implementation.

## Clarifications

### Session 2026-05-12

- Q: Should the protocol standard be locked to MCP `2025-11-25`? -> A: Yes. The feature targets MCP `2025-11-25` exactly and does not follow a moving latest version.
- Q: Should descriptor standardization include ShardingSphere business payload refactoring? -> A: No.
  Existing business payload fields are out of scope except where they are incorrectly documented as MCP descriptor fields.
- Q: Should old descriptor fields remain accepted for backward compatibility? -> A: No. Backward compatibility is completely out of scope; old non-standard descriptor fields are rejected or removed.
- Q: What is the default ShardingSphere extension metadata exposure policy? -> A: Use minimal exposure.
  Runtime control information remains internal typed configuration.
  Only read-only derived guidance useful to MCP clients or models may be exposed under namespaced `meta`.
- Q: Which MCP Java SDK version should be used? -> A: Upgrade the stable 1.x line to MCP Java SDK `1.1.2`; do not target `2.0.0-M2` for this work.
- Q: How should official fields missing from SDK `1.1.2` be handled? -> A: Keep the descriptor model aligned to MCP `2025-11-25` and fill SDK gaps with a bootstrap protocol adapter.
- Q: What namespace should ShardingSphere extension metadata use? -> A: `org.apache.shardingsphere/` is the only ShardingSphere extension namespace.
- Q: Should descriptor-adjacent sections be covered together? -> A: Yes.
  Cover resource, resource template, tool, prompt, completion registry, navigation registry, and catalog payload surfaces in one implementation.
- Q: Where should protocol contract validation happen? -> A: Enforce it at descriptor load and validation time with fail-fast errors, not as a late transport fallback.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or any Speckit script that changes the current branch.
- Java and YAML source models MUST use `meta` as the property name. Wire-level MCP JSON may still serialize that property as `_meta` through the SDK or transport adapter.
- Public MCP descriptor objects MUST NOT keep or accept non-standard top-level fields for compatibility.
- ShardingSphere extension metadata MUST use reverse-DNS namespaced keys under `meta`.
- Source descriptor YAML MUST NOT use MCP-reserved `_meta` keys as a substitute for ShardingSphere namespaced metadata.
- Runtime decisions that affect safety or execution MUST be driven by typed internal configuration, not loosely interpreted public `meta`.
- ShardingSphere extension metadata MUST follow a minimal exposure policy: expose only read-only derived client or model guidance, and keep runtime control data internal.
- Business payload fields may remain ShardingSphere-specific, but they MUST NOT be documented or validated as official MCP descriptor fields.
- Repository rules from `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md` remain binding.

## User Scenarios & Testing

### User Story 1 - Client Receives Official MCP Descriptor Shapes (Priority: P1)

An MCP client listing resources, tools, and prompts should receive descriptor objects whose top-level fields match the official MCP schema.
The client should not see ShardingSphere-only fields mixed into official objects.

**Why this priority**: Descriptor drift is the root problem.
If official objects still carry custom top-level fields, protocol clients and reviewers cannot distinguish MCP semantics from ShardingSphere guidance.

**Independent Test**: Read the descriptor catalog output.
Verify every resource, resource template, tool, prompt, and prompt argument object uses only the allowed official top-level fields for its MCP object type.

**Acceptance Scenarios**:

1. **Given** a fixed resource descriptor, **When** it is exposed to an MCP client, **Then** it uses `uri` instead of `uriTemplate` and contains no resource-specific custom top-level fields.
2. **Given** a tool descriptor, **When** it is exposed to an MCP client, **Then** it uses `inputSchema` instead of `fields` or `inputFields`.
3. **Given** a prompt descriptor, **When** it is exposed to an MCP client, **Then** it does not expose `templateResource` as a prompt top-level field.

---

### User Story 2 - Maintainer Knows Where Every Custom Field Belongs (Priority: P1)

A maintainer reviewing descriptors should be able to classify every non-official datum as internal configuration, namespaced `meta`, or business payload. No custom field should remain ambiguous.

**Why this priority**: The current surface mixes official fields, model guidance, runtime control hints, and payload contracts. A clean ownership rule prevents future field drift.

**Independent Test**: Review the field inventory and validator rules.
Each known custom field has exactly one target disposition: delete, internalize, move to namespaced `meta`, or keep as business payload.

**Acceptance Scenarios**:

1. **Given** a custom field such as `resourceKind`, **When** the field inventory is reviewed, **Then** it has a target location and is not allowed as a descriptor top-level field.
2. **Given** a runtime control hint such as `workflowRole`, **When** the implementation is planned, **Then** the requirement states its final location.
   It is internal typed configuration.
   Namespaced metadata may expose only derived read-only guidance and must not drive runtime control.
3. **Given** a business response field such as `next_actions`, **When** protocol standardization is reviewed, **Then** it is treated as ShardingSphere payload, not an MCP descriptor field.

---

### User Story 3 - Validator Prevents Protocol Drift (Priority: P1)

A contributor adding or changing an MCP descriptor should receive a deterministic validation failure when they introduce an unknown top-level descriptor field or un-namespaced extension metadata.

**Why this priority**: Naming standards cannot rely on reviewer memory. The catalog validator must keep the protocol boundary clean after this cleanup lands.

**Independent Test**: Add sample descriptors with banned top-level fields and un-namespaced `meta` keys in tests; verify validation rejects them with actionable messages.

**Acceptance Scenarios**:

1. **Given** a resource descriptor with top-level `parameters`, **When** the catalog is validated, **Then** validation fails and directs the contributor to the canonical URI variable location.
2. **Given** a tool annotation with `returnDirect`, **When** the catalog is validated, **Then** validation fails because it is not an official tool annotation.
3. **Given** a `meta` key without an allowed namespace, **When** the catalog is validated, **Then** validation fails before descriptor publication.

---

### User Story 4 - Runtime Safety Stays Explicit (Priority: P2)

An operator using side-effecting tools should keep the existing preview and approval protection while descriptor names become protocol-aligned.

**Why this priority**: Standardizing fields must not weaken the constitution's explicit operator control rule.

**Independent Test**: Verify destructive and workflow tools still declare clear behavior hints and still require preview plus explicit approval before side effects.

**Acceptance Scenarios**:

1. **Given** a destructive SQL or workflow tool, **When** its descriptor is standardized, **Then** official `ToolAnnotations` still describe the behavior.
   Required hints include `destructiveHint`, `readOnlyHint`, `idempotentHint`, and `openWorldHint`.
2. **Given** a tool requires approval, **When** runtime safety is evaluated, **Then** approval behavior is driven by typed internal configuration.
   Namespaced ShardingSphere metadata may expose derived information, but unofficial tool annotation fields must not drive approval.

### Edge Cases

- If the current MCP Java SDK model does not expose every MCP `2025-11-25` field required by this specification, that is an implementation-time protocol adaptation concern.
  It is not a runtime behavior requirement and MUST NOT lower the descriptor standard.
- Some ShardingSphere guidance is useful to models but not official MCP schema; it may remain in `shardingsphere://capabilities` or namespaced `meta`.
- Existing clients may rely on old custom top-level fields. Compatibility is out of scope for this feature.
- The standard Speckit feature creation script is not used because it switches branches.
- Documentation-only Speckit updates do not require Maven execution.
- MCP Java SDK dependency should move from `1.1.0` to `1.1.2` during implementation.
- MCP Java SDK `2.0.0-M2` is not the target because it is a milestone and does not remove the need for local adapter support for `icons` or tool `execution`.
- Descriptor contract validation happens at descriptor load time and MUST fail fast for old non-standard fields, unknown official-object fields, and invalid exposed metadata.

## Requirements

### Functional Requirements

- **MPS-FR-001**: The requirement package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands or scripts.
- **MPS-FR-002**: Fixed resource descriptors MUST expose only official fixed-resource top-level fields: `uri`, `name`, `title`, `description`, `icons`, `mimeType`, `annotations`, `size`, and `meta`.
- **MPS-FR-003**: Resource template descriptors MUST expose only official resource-template top-level fields.
  These fields are `uriTemplate`, `name`, `title`, `description`, `icons`, `mimeType`, `annotations`, and `meta`.
- **MPS-FR-004**: Fixed resources and resource templates MUST be modeled separately.
  The public descriptor model MUST NOT rely on `isTemplated()` or a single `uriTemplate` field to represent both official object types.
- **MPS-FR-005**: Resource descriptor custom top-level fields MUST be removed from official descriptor output.
  These include `parameters`, `resourceKind`, `objectScope`, `feature`, `relatedTools`, `relatedResources`, and `useBefore`.
- **MPS-FR-006**: Resource URI variable descriptions MUST either be internal-only validation data or exposed as `meta.org.apache.shardingsphere/uri-variables`.
  They MUST NOT be exposed as top-level `parameters`.
- **MPS-FR-007**: Tool descriptors MUST expose only official tool top-level fields: `icons`, `name`, `title`, `description`, `inputSchema`, `execution`, `outputSchema`, `annotations`, and `meta`.
- **MPS-FR-008**: Tool descriptors MUST use official JSON Schema under `inputSchema`. Public descriptor YAML and catalog output MUST NOT expose `fields` or `inputFields`.
- **MPS-FR-009**: Tool annotations MUST use only official tool annotation fields: `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, and `openWorldHint`.
- **MPS-FR-010**: `returnDirect` MUST be removed from tool annotations.
  If a direct-result behavior is later proven necessary, it MUST be represented as typed internal configuration with a new explicit name.
  Namespaced metadata may expose only read-only derived guidance and MUST NOT control direct-result behavior.
- **MPS-FR-011**: Prompt descriptors MUST expose only official prompt top-level fields: `icons`, `name`, `title`, `description`, `arguments`, and `meta`.
- **MPS-FR-012**: Prompt template paths MUST be internal prompt-template bindings. `templateResource` MUST NOT be exposed as a prompt top-level field.
- **MPS-FR-013**: Prompt arguments MUST expose only `name`, `title`, `description`, and `required`.
- **MPS-FR-014**: Completion target registration MUST be internal or ShardingSphere-scoped. `completionTargets` MUST NOT be represented as an official MCP descriptor field.
- **MPS-FR-015**: Resource navigation metadata MUST be internal or ShardingSphere-scoped. `resourceNavigation` MUST NOT be represented as an official MCP descriptor field.
- **MPS-FR-016**: Catalog extensions such as `protocolAvailability`, `fingerprints`, `modelFacingSchemas`, and `payload_contract` MUST NOT be described or validated as official MCP descriptor fields.
- **MPS-FR-017**: The capabilities resource may expose ShardingSphere business guidance, but it MUST label extension sections as ShardingSphere catalog metadata rather than MCP protocol fields.
- **MPS-FR-018**: All exposed ShardingSphere `meta` keys MUST use the prefix `org.apache.shardingsphere/`.
- **MPS-FR-019**: Safety and execution decisions MUST be driven by typed internal descriptors or runtime configuration, not by exposed `meta`.
- **MPS-FR-020**: Official `name` and `title` fields MUST remain separate for resources, resource templates, tools, prompts, and prompt arguments.
  `name` is the stable programmatic identifier; `title` is the human-readable display label.
- **MPS-FR-021**: All descriptor object types that support official `icons` MUST include descriptor model support for `icons`, even when current descriptors choose not to populate them.
- **MPS-FR-022**: The descriptor validator MUST reject unknown top-level fields for official MCP descriptor objects.
- **MPS-FR-023**: The descriptor validator MUST reject un-namespaced exposed source descriptor `meta` keys.
  MCP-reserved `_meta` keys are handled only at the wire/runtime layer when the MCP protocol explicitly defines them for that payload.
- **MPS-FR-024**: The transport adapter MUST preserve Java/YAML `meta` naming internally while serializing to the official wire-level `_meta` field where required by MCP.
- **MPS-FR-025**: Existing ShardingSphere business payload fields may remain business payload fields.
  Examples include `response_mode`, `next_actions`, `recovery`, resource hints, and workflow guidance.
  They MUST be excluded from descriptor-field standardization decisions.
- **MPS-FR-026**: Tests MUST cover descriptor serialization, descriptor validation, and representative YAML descriptors.
  Coverage includes resources, resource templates, tools, prompts, completion registry, and navigation registry.
- **MPS-FR-027**: The implementation MUST upgrade MCP Java SDK dependencies to stable version `1.1.2`.
- **MPS-FR-028**: The implementation MUST NOT depend on MCP Java SDK `2.0.0-M2` to satisfy this feature.
- **MPS-FR-029**: Official MCP fields missing from SDK `1.1.2` MUST be handled by the protocol adapter.
  This includes descriptor `icons` and tool `execution`, without changing the public descriptor contract.
- **MPS-FR-030**: Descriptor validation MUST run during descriptor loading and fail fast before descriptor publication or transport mapping.
- **MPS-FR-031**: The implementation MUST prove a bootstrap adapter strategy for SDK gap fields before relying on `icons`, tool `execution`, or `meta` to `_meta` mapping.
- **MPS-FR-032**: Descriptor YAML loading MUST reject unknown keys before typed binding can silently drop them.
- **MPS-FR-033**: Source YAML sections that are not official descriptor collections MUST use the `internal` prefix.
  Required names are `internalPromptTemplateBindings`, `internalCompletionTargets`, `internalReferenceNavigation`, and `internalToolRuntime`.
- **MPS-FR-034**: Runtime argument validation MUST use an internal argument contract derived from official `inputSchema`.
  Public `fields` or catalog `inputFields` MUST NOT remain runtime validation inputs.
- **MPS-FR-035**: Tool names MUST satisfy the local fail-fast naming policy: 1 to 128 characters, case-sensitive uniqueness, and ASCII letters, digits, `_`, `-`, or `.` only.
- **MPS-FR-036**: Prompt runtime objects such as prompt rendering, template loading, `PromptMessage`, and `GetPromptResult` MUST NOT move into `mcp/api`.
- **MPS-FR-037**: Tool `inputSchema` MUST be a JSON Schema object with root `type: object`.
  When `$schema` is absent, the effective dialect is JSON Schema 2020-12.
- **MPS-FR-038**: Tool `outputSchema`, when present, MUST be a JSON Schema object with root `type: object`.
- **MPS-FR-039**: Descriptor icons MUST validate `src`, `sizes`, and `theme`.
  Source descriptor icons allow only `https` and `data` URI sources.
- **MPS-FR-040**: Runtime argument validation MUST document and test the subset of `inputSchema` that is enforced by the internal argument contract.
  Unsupported JSON Schema keywords may remain client-facing guidance, but they MUST NOT be described as server-enforced behavior.
- **MPS-FR-041**: When a tool descriptor declares `outputSchema`, tool execution MUST validate returned `structuredContent` against that schema before exposing the result.
  Tools that cannot provide conforming `structuredContent` MUST omit `outputSchema`.

### Metadata Exposure Decision Criteria

ShardingSphere extension metadata follows a minimal exposure policy.

- Metadata MAY be exposed when it is read-only, derived from internal state, and helps MCP clients or models choose the next safe action.
  It may also help clients understand resource relationships, render diagnostics, or avoid guessing arguments.
- Metadata MUST remain internal when it controls execution, safety, workflow state transitions, permission-like decisions, prompt template loading, or implementation wiring.
- Exposed metadata increases client transparency and model guidance quality, but it also becomes a public contract that must be validated, documented, and regression-tested.
- Internal metadata reduces public contract size and future compatibility cost, but clients lose direct visibility into why a tool, resource, prompt, or completion behaves a certain way.
- If the same datum is needed both internally and externally, the internal typed configuration is the source of truth and the exposed metadata is a derived, namespaced view.
- Examples of metadata that may be exposed include `resource-kind`, `object-scope`, `feature`, `uri-variables`, `related-tools`, and `related-resource-uris`.
- Examples of metadata that must remain internal include `workflowRole`, `templateResource`, safety execution decisions, and completion source wiring.

### Key Entities

- **Official Descriptor Object**: A resource, resource template, tool, prompt, prompt argument, annotation, icon, or execution object whose top-level fields are defined by MCP.
- **ShardingSphere Extension Metadata**: Optional non-official data exposed under `meta.org.apache.shardingsphere/...` for model guidance, diagnostics, relationships, or product-specific hints.
- **Internal Runtime Configuration**: Typed non-public data that drives runtime behavior such as workflow role, safety policy, prompt template binding, or completion source selection.
- **Business Payload**: Tool or resource response content returned by ShardingSphere, such as workflow guidance, recovery actions, metadata lists, and SQL execution summaries.
- **Field Disposition**: The required target for a current field: official field, namespaced metadata, internal configuration, business payload, or deletion.
- **Protocol Adapter**: The boundary that translates Java/YAML descriptor models into SDK or wire-level MCP objects.

### Field Disposition Inventory

- **Resource `uriTemplate` for fixed resources**: Replace with official `uri`; keep `uriTemplate` only for resource templates.
- **Resource `parameters`**: Move to `meta.org.apache.shardingsphere/uri-variables` or keep internal for validation.
- **Resource `resourceKind`, `objectScope`, `feature`**: Move to namespaced ShardingSphere metadata when model-facing; otherwise keep internal.
- **Resource `relatedTools`, `relatedResources`, `useBefore`**: Move to namespaced relationship metadata or capabilities resource guidance.
- **Tool `fields` and catalog `inputFields`**: Replace with official `inputSchema`.
- **Tool annotation `returnDirect`**: Delete. Backward-compatible acceptance is out of scope.
- **Prompt `templateResource`**: Move to internal prompt-template binding.
- **Prompt argument `completion` hint**: Move to internal completion registry or namespaced metadata.
- **`completionTargets` section**: Keep as internal registry or ShardingSphere catalog metadata, not official descriptor data.
- **`resourceNavigation` section**: Keep as internal reference navigation or ShardingSphere catalog metadata, not official descriptor data.
- **ResourceLink meta `resource_kind`, `purpose`, `source_field`**: Rename to namespaced ShardingSphere metadata keys.
- **Elicitation meta `tool`, `plan_id`**: Rename to namespaced ShardingSphere metadata keys if exposed.

## Success Criteria

### Measurable Outcomes

- **MPS-SC-001**: Every official descriptor object emitted by the catalog contains only the top-level fields allowed for that MCP object type.
- **MPS-SC-002**: No descriptor YAML file contains banned custom top-level descriptor fields after migration.
- **MPS-SC-003**: All exposed non-official source descriptor metadata keys use `org.apache.shardingsphere/`.
- **MPS-SC-004**: Tool input contracts are represented by `inputSchema` in YAML, catalog payloads, and transport objects.
- **MPS-SC-005**: Fixed resources and resource templates are counted and validated as separate descriptor object types.
- **MPS-SC-006**: Prompt descriptors expose no top-level `templateResource`.
- **MPS-SC-007**: Tool annotations expose no `returnDirect`.
- **MPS-SC-008**: Descriptor validation tests fail for representative banned fields and pass for the standardized replacements.
- **MPS-SC-009**: Transport or SDK adaptation tests prove Java/YAML `meta` becomes MCP wire `_meta` where required without introducing Java fields named `_meta`.
- **MPS-SC-010**: Side-effecting workflow and SQL tools still require preview and explicit approval according to existing safety requirements after descriptor standardization.
- **MPS-SC-011**: MCP Java SDK dependencies resolve to `1.1.2` in the implementation branch.
- **MPS-SC-012**: Tests prove adapter handling for official fields that SDK `1.1.2` cannot represent directly.
- **MPS-SC-013**: Tests prove actual MCP list-response serialization includes adapter-filled official fields and wire `_meta`.
- **MPS-SC-014**: Tests prove runtime argument validation covers the documented compiled `inputSchema` subset and declared `outputSchema` validates tool `structuredContent`.

## Assumptions

- The official MCP 2025-11-25 schema is the locked standard for this requirement package.
- Backward compatibility for old descriptor field names is completely out of scope.
- SDK model gaps are implementation-time protocol adaptation constraints, not runtime behavior requirements and not a reason to keep non-standard descriptor names.
- SDK `1.1.2` is the stable dependency target for implementation; SDK `2.0.0-M2` is not required or preferred for this feature.
- Business response payloads are outside official descriptor schemas and may remain ShardingSphere-specific when clearly labeled.
- The standard Speckit branch creation flow is skipped to honor the user's no-branch-switch requirement.
