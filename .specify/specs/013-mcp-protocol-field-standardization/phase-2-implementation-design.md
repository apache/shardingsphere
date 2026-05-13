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

# Phase 2 Implementation Design: MCP Descriptor Foundation

## Purpose

Phase 2 freezes the implementation boundary before any Java production code changes.
The goal is to make official MCP descriptor types explicit in `mcp/api` and move ShardingSphere-only runtime or guidance data to typed support owners.

This design is source-driven by official MCP schema version `2025-11-25`:

- `https://modelcontextprotocol.io/specification/2025-11-25/schema`
- `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion`
- `https://modelcontextprotocol.io/specification/2025-11-25/basic/index`

## Boundary Decisions

### `mcp/api`

`mcp/api` owns official ShardingSphere MCP-facing DTOs only.
It must not depend on YAML classes, support loaders, runtime registries, catalog builders, SDK classes, or transport adapters.

Target public descriptor DTOs:

- `org.apache.shardingsphere.mcp.api.common.descriptor.MCPIcon`
- `org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations`
- `org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor`
- `org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceTemplateDescriptor`
- `org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor`
- `org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations`
- `org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolExecution`
- `org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolTaskSupport`
- `org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor`
- `org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor`

Current `MCPResourceAnnotations` should become the shared official `MCPAnnotations` shape because the official `Annotations` object is reused by resources,
resource templates, content blocks, and resource links.

### `mcp/support`

`mcp/support` owns descriptor loading, YAML models, validation, catalog payload building, and internal registries.
It may depend on `mcp/api` descriptor DTOs, but official descriptor DTOs should not depend on support.

Target support-owned internal types:

- `MCPUriVariableDescriptor`: URI template variable validation and optional derived metadata source.
- `MCPPromptTemplateBinding`: prompt name to classpath template binding.
- `MCPCompletionTargetDescriptor`: completion registry entry aligned to PromptReference or ResourceTemplateReference, not an official descriptor object.
- `MCPReferenceNavigationDescriptor`: relationship graph entry replacing ambiguous `from` and `to`.
- `MCPShardingSphereMetadataKeys`: constants for exposed `org.apache.shardingsphere/` metadata keys.
- `MCPToolRuntimeDescriptor`: typed internal owner for workflow role, approval behavior, direct-result behavior if retained internally, and safety policy.

### `mcp/core`

`mcp/core` owns dispatch and runtime safety.
It must not infer routing, argument validation, approval, or workflow state from non-standard public descriptor fields.

Required direction:

- Resource routing consumes split fixed resource and resource template route data.
- Tool argument validation consumes `inputSchema` or a compiled internal argument contract derived from it.
- Preview and explicit approval remain server-side runtime policy, not public `meta` behavior.

### `mcp/bootstrap`

`mcp/bootstrap` owns SDK and wire adaptation only.
It may adapt official descriptor DTOs to MCP Java SDK `1.1.2`, and it may fill SDK gaps without changing the public descriptor contract.

Required adapter behavior:

- Java/YAML `meta` serializes to MCP wire `_meta`.
- SDK-supported fields map directly.
- Official fields missing from SDK `1.1.2`, including `icons` and `tool.execution`, are added by the local adapter.
- SDK `ToolAnnotations.returnDirect` is not populated from public descriptor annotations.
- Adapter feasibility must be proven through actual MCP list-response serialization seams, not only by standalone DTO or `ObjectMapper` serialization.

## Doubt-Driven Hardening Decisions

### SDK Adapter Feasibility Gate

The adapter is a required implementation gate, not a follow-up detail.
Before relying on SDK gap handling, implementation must prove one concrete wire strategy with tests.
The gate runs after upgrading the MCP Java SDK dependency to `1.1.2`, because the feasibility result is only meaningful against the selected implementation dependency.

Required proof:

- Serialize representative `resources/list`, `resources/templates/list`, `tools/list`, and `prompts/list` outputs through the bootstrap path that will serve clients.
- Prove official `icons` can appear at descriptor top level.
- Prove official `tool.execution.taskSupport` can appear at descriptor top level.
- Prove Java/YAML `meta` becomes wire `_meta`.
- Prove public ToolAnnotations never serialize SDK `returnDirect`.

The feasibility gate may use representative official descriptor fixtures or minimal DTOs before the full YAML/catalog migration exists.
The later transport mapping tests must still prove the final API DTOs reach the same wire shape.

Allowed strategies:

- Use SDK objects directly where they already represent MCP `2025-11-25`.
- Use a bootstrap-only protocol payload adapter for fields the SDK model cannot represent.
- Keep the adapter isolated in `mcp/bootstrap`; do not add SDK dependencies to `mcp/api` or `mcp/support`.

Forbidden strategies:

- Do not place official fields such as `icons` or `execution` under `meta`.
- Do not describe SDK omissions as protocol omissions.
- Do not reintroduce old descriptor names to work around SDK limits.

### Strict YAML Loading Gate

Descriptor YAML validation must happen before unknown fields can be silently dropped.
The loader may use ShardingSphere `YamlEngine`, but implementation must prove the chosen path rejects unknown properties.

Required proof:

- Unknown top-level catalog sections fail with the descriptor resource name in the error.
- Unknown fields inside Resource, ResourceTemplate, Tool, ToolAnnotations, Prompt, PromptArgument, and internal sections fail.
- Old names such as `parameters`, `fields`, `returnDirect`, and `templateResource` fail before descriptor publication.

Acceptable implementation strategies:

- Parse raw YAML to a map, validate allowed keys recursively, then bind to typed YAML objects.
- Or keep direct POJO binding only if tests prove the constructor path fails for unknown properties in every descriptor object.

### Runtime Argument Validation

Runtime validation should not depend on public `fields` after the tool descriptor moves to `inputSchema`.
The first implementation should compile a small internal argument contract from `inputSchema` instead of adding a full JSON Schema runtime dependency.
This means the full `inputSchema` is the official client-facing schema, while only an explicitly documented subset is server-enforced at runtime.

The compiled contract must preserve current safety behavior:

- Required arguments are enforced.
- Required text arguments are rejected when blank.
- `execution_mode` keeps its specialized missing-argument exception and preview suggestion.
- Enum-like values used by existing tools remain available for targeted runtime checks.

The public `inputSchema` remains the official client-facing contract.
The internal argument contract is derived support/core data and must not be emitted as a descriptor field.
Unsupported JSON Schema keywords may remain in `inputSchema` for client guidance, but the implementation must not claim they are server-enforced unless the compiled contract supports and tests them.

### Output Schema Conformance

If a tool descriptor declares `outputSchema`, the runtime must validate returned `structuredContent` against that schema before the result is exposed to clients.
If the first implementation does not add a schema validator capable of enforcing the declared output schema, descriptors must not declare `outputSchema` beyond shapes that the runtime can verify.

Rules:

- `outputSchema` is optional.
- When present, it must have root `type: object`.
- Tool results that include `structuredContent` must conform to the declared `outputSchema`.
- Business payload fields remain ShardingSphere-specific, but they must satisfy the declared schema when a schema is present.
- Text-only tool results should omit `outputSchema` unless they also provide conforming `structuredContent`.

### Internal YAML Section Names

Internal source YAML sections must be visibly internal so reviewers do not confuse them with MCP descriptor sections.

Target top-level source YAML sections:

```yaml
resources: []
resourceTemplates: []
tools: []
prompts: []
internalPromptTemplateBindings: []
internalCompletionTargets: []
internalReferenceNavigation: []
internalToolRuntime: []
```

Rules:

- `resources`, `resourceTemplates`, `tools`, and `prompts` are official descriptor collections.
- `internalPromptTemplateBindings` owns prompt template resource paths.
- `internalCompletionTargets` maps to the MCP completion utility but is not an MCP descriptor section.
- `internalReferenceNavigation` owns ShardingSphere relationship guidance.
- `internalToolRuntime` owns workflow role, approval, and safety runtime policy.

### Metadata Layers

There are three distinct metadata layers:

- Java and YAML descriptor models use `meta`.
- MCP wire JSON uses `_meta`.
- ShardingSphere capability resources are business payloads, not official descriptor objects.

Rules:

- Only the bootstrap adapter maps `meta` to `_meta`.
- Source descriptor YAML accepts only `org.apache.shardingsphere/` extension metadata keys.
- MCP-reserved `_meta` keys are generated or passed through only at the wire/runtime layer when the MCP protocol explicitly defines them for that payload.
- Capability resource payload fields may remain ShardingSphere business fields, but documentation must not call them official MCP descriptor fields.

### `returnDirect` Disposition

`returnDirect` is deleted from public ToolAnnotations, descriptor YAML, and catalog output.
It must not be carried forward as a compatibility field.

If implementation proves an existing runtime delivery behavior is still required, it must be modeled as internal tool runtime data with a new explicit name.
It must not serialize to SDK `ToolAnnotations.returnDirect`, and it must not influence safety approval decisions.

### Prompt Scope Guard

Only Prompt and PromptArgument descriptor DTOs move to `mcp/api`.
Prompt rendering, template loading, `GetPromptResult`, `PromptMessage`, and classpath resource access remain support/bootstrap concerns.

### Tool Name Validation Policy

MCP `2025-11-25` defines Tool Name guidance as SHOULD-level protocol guidance.
ShardingSphere standardization treats it as a local fail-fast descriptor policy:

- Tool names must be 1 to 128 characters.
- Tool names are case-sensitive.
- Tool names may contain only ASCII letters, digits, underscore, hyphen, and dot.
- Tool names must remain unique within the server.

This policy is stricter than MCP's wire requirement, but it keeps generated descriptors deterministic and portable.

## Public Descriptor DTO Design

### `MCPIcon`

Fields:

- `src`
- `mimeType`
- `sizes`
- `theme`

Rules:

- `src` is required when an icon is present.
- Source descriptors allow only `https` and `data` icon sources.
- `theme` accepts only `light` or `dark` when present.
- `sizes` is a list of official icon size strings such as `48x48`, `96x96`, or `any`.

### `MCPAnnotations`

Fields:

- `audience`
- `priority`
- `lastModified`

Rules:

- `audience` values are `user` and `assistant`.
- `priority` is within `0.0` to `1.0`.
- `lastModified` is an ISO 8601 formatted string.
- This object is client/model usage guidance, not ShardingSphere runtime control.

### `MCPResourceDescriptor`

Fixed resource fields:

- `uri`
- `name`
- `title`
- `description`
- `icons`
- `mimeType`
- `annotations`
- `size`
- `meta`

Rules:

- This DTO represents only official fixed Resource.
- It has no `uriTemplate` and no `isTemplated()`.
- It has no custom top-level fields such as `parameters`, `resourceKind`, `objectScope`, `relatedTools`, or `useBefore`.

### `MCPResourceTemplateDescriptor`

Resource template fields:

- `uriTemplate`
- `name`
- `title`
- `description`
- `icons`
- `mimeType`
- `annotations`
- `meta`

Rules:

- This DTO represents only official ResourceTemplate.
- It has no fixed `uri` and no `size`.
- URI variable descriptions are support-owned data and may be exposed only as derived namespaced `meta`.

### `MCPToolDescriptor`

Fields:

- `icons`
- `name`
- `title`
- `description`
- `inputSchema`
- `execution`
- `outputSchema`
- `annotations`
- `meta`

Rules:

- `inputSchema` is a required JSON Schema object and replaces public `fields`.
- Root `inputSchema.type` must be `object`.
- When `$schema` is absent, JSON Schema 2020-12 is the effective dialect.
- `toInputSchema()` is removed from the public DTO.
- `outputSchema` remains an optional JSON Schema object and must have root `type: object` when present.
- Runtime argument validation enforces only the documented compiled subset derived from `inputSchema`.
- When `outputSchema` is present, `structuredContent` must be validated against it before exposure.
- Public `meta` is read-only extension metadata, not runtime policy.

### `MCPToolAnnotations`

Fields:

- `title`
- `readOnlyHint`
- `destructiveHint`
- `idempotentHint`
- `openWorldHint`

Rules:

- `returnDirect` is removed.
- Official absent-value defaults are retained by leaving unset hints absent on the wire.
- Annotations are hints and do not authorize tool execution.
- Runtime safety continues through typed internal configuration.

### `MCPToolExecution`

Fields:

- `taskSupport`

Rules:

- `taskSupport` uses `MCPToolTaskSupport`.
- Supported values are `FORBIDDEN`, `OPTIONAL`, and `REQUIRED` in Java.
- YAML and wire values use official lower-case strings: `forbidden`, `optional`, and `required`.
- The absent value is equivalent to official default `forbidden`; descriptors should omit `execution` when no task support is needed.

### `MCPPromptDescriptor`

Fields:

- `icons`
- `name`
- `title`
- `description`
- `arguments`
- `meta`

Rules:

- Prompt descriptors move to `mcp/api` because Prompt is an official MCP descriptor object.
- `templateResource` is removed from the public Prompt DTO.
- Prompt template lookup is owned by support through `MCPPromptTemplateBinding`.

### `MCPPromptArgumentDescriptor`

Fields:

- `name`
- `title`
- `description`
- `required`

Rules:

- Completion hints are not public prompt argument fields.
- Completion behavior is owned by the support completion registry.

## YAML Shape Design

Target top-level source YAML sections:

```yaml
resources: []
resourceTemplates: []
tools: []
prompts: []
internalPromptTemplateBindings: []
internalCompletionTargets: []
internalReferenceNavigation: []
internalToolRuntime: []
```

Rules:

- `resources` contains fixed resources with `uri`.
- `resourceTemplates` contains templated resources with `uriTemplate`.
- `tools` contains official tool descriptor fields, including `inputSchema`.
- `prompts` contains official prompt descriptor fields only.
- Internal sections are support-owned and must keep the `internal` prefix in YAML.
- `internalCompletionTargets` maps to the official completion utility and must not be exposed as a descriptor section.
- YAML loading must reject unknown fields instead of silently accepting old names.

## Metadata Design

`meta` is the Java and YAML property name.
MCP wire JSON uses `_meta` when emitted by the SDK or bootstrap adapter.

Allowed ShardingSphere exposed keys:

- `org.apache.shardingsphere/resource-kind`
- `org.apache.shardingsphere/object-scope`
- `org.apache.shardingsphere/feature`
- `org.apache.shardingsphere/uri-variables`
- `org.apache.shardingsphere/related-tools`
- `org.apache.shardingsphere/related-resource-uris`
- `org.apache.shardingsphere/use-before`
- `org.apache.shardingsphere/source-field`
- `org.apache.shardingsphere/purpose`

Rules:

- Exposed extension metadata must use `org.apache.shardingsphere/`.
- Public metadata is derived from internal typed state.
- Internal typed state is the source of truth for runtime decisions.
- Un-namespaced metadata keys are invalid unless an MCP schema explicitly reserves them.

## Validator Design

Validation runs in support during descriptor loading.
It must fail before catalog publication or bootstrap transport mapping.

Required validator checks:

- Official top-level field whitelist per descriptor object type.
- Old field rejection for `parameters`, `fields`, `inputFields`, `returnDirect`, and `templateResource`.
- Internal YAML sections must use the `internal` prefix.
- Split identity rules: fixed resources require `uri`; templates require `uriTemplate`.
- Tool name length, character set, case-sensitive uniqueness, and nonblank requirements.
- Metadata namespace rules for every exposed `meta` object.
- `MCPAnnotations.audience`, `priority`, and `lastModified` structure.
- `MCPIcon.src` scheme, `sizes`, and `theme`.
- `MCPToolExecution.taskSupport`.
- `inputSchema` and `outputSchema` root object structure.
- The supported runtime-enforced subset of `inputSchema`.
- Cross-reference integrity for prompt bindings, completion targets, navigation, and tool runtime entries.

## Phase 2 Execution Order

1. Upgrade MCP Java SDK dependencies in `mcp/bootstrap` to `1.1.2`.
2. Add bootstrap adapter feasibility tests for actual list-response serialization seams and SDK gap fields before relying on adapter behavior.
3. Define strict YAML loading behavior for raw unknown keys before typed binding.
4. Define the compiled internal tool argument contract derived from `inputSchema`.
5. Define the `outputSchema` conformance strategy.
6. Add shared common descriptor primitives in `mcp/api`.
7. Split fixed resource and resource template DTOs in `mcp/api`.
8. Move prompt and prompt argument DTOs from support to `mcp/api`.
9. Replace public tool `fields` with direct `inputSchema`.
10. Add tool execution and task support enum.
11. Add support-owned internal descriptors for URI variables, prompt bindings, navigation, completion, and tool runtime.
12. Add metadata key constants in support.
13. Update the catalog aggregate shape so it can carry split resources and internal registries.

## Phase 2 Stop Condition

Phase 2 is complete when the pre-implementation gates are satisfied and descriptor DTOs plus internal owner types compile with tests updated for constructor or getter changes.
It is not complete until SDK `1.1.2` is in use, adapter feasibility is proven through actual list-response serialization seams, strict YAML loading behavior is proven, the runtime schema boundary is documented, old public DTO fields no longer exist in `mcp/api`, and official prompt DTOs are no longer support-owned.

Runtime factories, YAML descriptor migration, catalog payload output, and SDK adapter details belong to later phases unless required for compilation.
