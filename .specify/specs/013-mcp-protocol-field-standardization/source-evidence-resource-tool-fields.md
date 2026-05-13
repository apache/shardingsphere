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

# Source Evidence: MCP Protocol Fields

## Source Set

- Official MCP protocol: `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- Official MCP protocol: `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- Official MCP protocol: `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- Official MCP protocol: `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion`
- Official MCP schema: `https://modelcontextprotocol.io/specification/2025-11-25/schema`
- Official MCP schema source: `https://github.com/modelcontextprotocol/modelcontextprotocol/blob/main/schema/2025-11-25/schema.ts`
- Official MCP general fields: `https://modelcontextprotocol.io/specification/2025-11-25/basic#general-fields`
- Local dependency: `mcp/bootstrap/pom.xml` currently uses MCP Java SDK `1.1.0`.
- Local SDK source: `~/.m2/repository/io/modelcontextprotocol/sdk/mcp-core/1.1.0/mcp-core-1.1.0-sources.jar`.
- External SDK source inspected from Maven Central: MCP Java SDK `1.1.2` and `2.0.0-M2` source jars.
- Local production source: `mcp/api`, `mcp/support`, and `mcp/bootstrap`.
- Local tests: `mcp/api/src/test`, `mcp/support/src/test`, and `mcp/bootstrap/src/test`.

## Official Baseline

### Resource

Official Resource fields in MCP `2025-11-25`:

- `uri`: Unique readable resource identifier.
- `name`: Programmatic or logical identifier. It can be used as display fallback when `title` is absent.
- `title`: Optional human-readable display name for UI and end-user contexts.
- `description`: Optional model/client hint describing what the resource represents.
- `icons`: Optional UI icons; each icon is an `Icon` object.
- `mimeType`: Optional MIME type of the resource.
- `annotations`: Optional client hints for audience, priority, and last modification time.
- `size`: Optional raw resource content size in bytes before base64 encoding or tokenization.
- `meta`: Java/YAML name for MCP wire `_meta`; reserved extension metadata.

### ResourceTemplate

Official ResourceTemplate fields in MCP `2025-11-25`:

- `uriTemplate`: URI template used to produce concrete resource URIs.
- `name`: Programmatic or logical identifier. It can be used as display fallback when `title` is absent.
- `title`: Optional human-readable display name for UI and end-user contexts.
- `description`: Optional model/client hint describing what the template produces.
- `icons`: Optional UI icons.
- `mimeType`: Optional MIME type of resources produced by the template when they share the same type.
- `annotations`: Optional client hints for audience, priority, and last modification time.
- `meta`: Java/YAML name for MCP wire `_meta`; reserved extension metadata.

### Annotations

Official `annotations` fields:

- `audience`: Intended audience list. Valid values are `user` and `assistant`.
- `priority`: Importance from `0.0` to `1.0`; `1.0` means effectively required and `0.0` means optional.
- `lastModified`: ISO 8601 timestamp indicating when the resource was last modified.

The current `MCPResourceAnnotations` shape is protocol-aligned, but implementation should rename or generalize it as shared `MCPAnnotations`.
The official object is reused beyond Resource, so its purpose is client/model display and context-prioritization guidance, not ShardingSphere control metadata.

### Icon

Official `Icon` fields:

- `src`: Required URI pointing to an icon resource.
- `mimeType`: Optional MIME type override.
- `sizes`: Optional display sizes in `WxH` or `any` form.
- `theme`: Optional `light` or `dark` theme hint.

MCP `icons` can be attached to Resource, ResourceTemplate, Tool, Prompt, and Implementation objects.
For ShardingSphere descriptors, icon validation should reject unsafe URI schemes and allow only HTTPS or `data:` sources.

### Tool

Official Tool fields in MCP `2025-11-25`:

- `name`: Unique identifier used to call the tool.
- `title`: Optional human-readable display name for UI and end-user contexts.
- `description`: Optional human-readable description and model hint for available functionality.
- `icons`: Optional UI icons.
- `inputSchema`: Required JSON Schema object defining expected call arguments.
- `execution`: Optional execution properties.
- `outputSchema`: Optional JSON Schema object defining `structuredContent` returned by `CallToolResult`.
- `annotations`: Optional tool-behavior hints.
- `meta`: Java/YAML name for MCP wire `_meta`; reserved extension metadata.

### ToolAnnotations

Official `ToolAnnotations` fields:

- `title`: Optional human-readable title hint for the tool.
- `readOnlyHint`: True when the tool does not modify its environment.
- `destructiveHint`: True when the tool may perform destructive updates; meaningful only when `readOnlyHint` is false.
- `idempotentHint`: True when repeated calls with the same arguments have no additional effect; meaningful only when `readOnlyHint` is false.
- `openWorldHint`: True when the tool may interact with external entities outside a closed domain.

All tool annotations are untrusted hints. Clients must not make security-sensitive tool-use decisions solely from these fields.
Official default semantics are `readOnlyHint=false`, `destructiveHint=true`, `idempotentHint=false`, and `openWorldHint=true` when absent.

### ToolExecution

Official `ToolExecution` fields:

- `taskSupport`: Whether the tool supports task-augmented execution; valid values are `forbidden`, `optional`, and `required`.

When `execution` or `taskSupport` is absent, the effective task support is `forbidden`.

### JSON Schema in Tools

MCP uses JSON Schema throughout protocol messages.
If `$schema` is absent, the default dialect is JSON Schema 2020-12.
Implementations must support 2020-12 and should handle explicitly declared supported dialects.

Tool `inputSchema` and `outputSchema` are both JSON Schema objects.
For ShardingSphere descriptors, the root schema must be an object because tool arguments and structured content are JSON objects.
If `outputSchema` is provided, servers must return `structuredContent` conforming to that schema.

### Prompt

Official Prompt fields in MCP `2025-11-25`:

- `icons`: Optional UI icons.
- `name`: Unique identifier for the prompt.
- `title`: Optional human-readable display name for UI and end-user contexts.
- `description`: Optional human-readable description.
- `arguments`: Optional list of prompt arguments.
- `meta`: Java/YAML name for MCP wire `_meta`; reserved extension metadata.

Prompt descriptors are official MCP objects and should therefore be represented by public DTOs in `mcp/api`.
Prompt template file paths such as `templateResource` are ShardingSphere implementation bindings, not official prompt fields.

### PromptArgument

Official PromptArgument fields in MCP `2025-11-25`:

- `name`: Programmatic argument identifier.
- `title`: Optional human-readable display name for UI and end-user contexts.
- `description`: Optional human-readable argument description.
- `required`: Whether the argument must be provided.

Completion hints are not official PromptArgument fields.

### Completion

Official completion protocol is the `completion/complete` request and `CompleteResult` response.
It is not a descriptor object and it does not define descriptor-level `completionTargets`.

Official completion request fields:

- `ref`: A `PromptReference` or `ResourceTemplateReference`.
- `argument`: The argument name and current value being completed.
- `context`: Optional previously resolved argument values.

Official completion response fields:

- `completion.values`: Returned suggestions, with at most 100 values.
- `completion.total`: Optional total available suggestions.
- `completion.hasMore`: Whether additional suggestions exist.

The schema source names the resource-side reference `ResourceTemplateReference`, even though its wire `type` is `ref/resource` and its `uri` can hold a URI or URI template.
ShardingSphere `completionTargets` should remain an internal registry or ShardingSphere catalog metadata, not an official MCP descriptor field.

### Meta

Official wire metadata field name is `_meta`.
Java and YAML use `meta` in this feature to follow Java naming conventions, while bootstrap maps to `_meta`.

Official key rules require namespaced prefixes to use reverse-DNS style labels followed by `/`.
Prefixes where the second label is `modelcontextprotocol` or `mcp` are reserved for MCP.
The chosen ShardingSphere prefix `org.apache.shardingsphere/` follows this rule and is not MCP-reserved.

### Name and Title

`name` and `title` must not be merged.

- `name` is the stable programmatic or logical identifier used for lookup, invocation, references, and fallback display.
- `title` is the optional end-user/UI label optimized for readability.
- For tools, official display precedence is `title`, then `annotations.title`, then `name`.

## Current Local Implementation Evidence

### Resource Descriptor

`mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/MCPResourceDescriptor.java` currently contains:

- Official-like fields: `uriTemplate`, `name`, `title`, `description`, `mimeType`, `annotations`, `meta`.
- Local non-official fields: `parameters`, `resourceKind`, `objectScope`, `feature`, `relatedTools`, `relatedResources`, `useBefore`.
- A conflation rule: `isTemplated()` treats any `uriTemplate` containing `{` as a ResourceTemplate and everything else as a fixed Resource.

Current implication:

- Fixed `Resource.uri` is represented by the local field `uriTemplate`.
- Templated `ResourceTemplate.uriTemplate` is represented by the same local field.
- This is the main naming ambiguity to remove.

### Resource Wire Mapping

`mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactory.java` maps:

- Non-templated descriptor to SDK `McpSchema.Resource` with `uri`, `name`, `title`, `description`, `mimeType`, `annotations`, and `meta`.
- Templated descriptor to SDK `McpSchema.ResourceTemplate` with `uriTemplate`, `name`, `title`, `description`, `mimeType`, `annotations`, and `meta`.
- Local fields such as `resourceKind`, `objectScope`, `feature`, `relatedTools`, `relatedResources`, `useBefore`, and `parameters` are injected into un-namespaced `meta`.

Current implication:

- Wire-level resource and resource-template mapping mostly follows SDK `1.1.0`.
- Local metadata injection does not follow MCP `2025-11-25` `_meta` key namespace guidance.
- SDK `1.1.0` does not expose official `icons` on Resource or ResourceTemplate.

### Resource Catalog Payload

`mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java` currently emits:

- Standard-like fields: `uri` or `uriTemplate`, `name`, `title`, `description`, `mimeType`, `annotations`, `meta`.
- Non-standard top-level fields: `parameters`, `resourceKind`, `objectScope`, `feature`, `relatedTools`, `relatedResources`, `useBefore`, and `payload_contract`.

Current implication:

- The catalog payload currently exposes ShardingSphere descriptor extensions as if they were descriptor fields.
- Future catalog output must either remove them from official descriptor objects or expose useful derived guidance through namespaced `meta`.

### Resource Tests

`mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/MCPResourceSpecificationFactoryTest.java` proves:

- Fixed resources are expected to expose SDK `resource().uri()`, `name()`, `title()`, `description()`, and `mimeType()`.
- Resource templates are expected to expose SDK `resourceTemplate().uriTemplate()`, `name()`, `title()`, `description()`, and `mimeType()`.
- Current tests expect template `parameters` to appear in SDK `_meta`.

`mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoaderTest.java` proves:

- Current descriptors intentionally load and validate local fields such as `resourceKind`, `objectScope`, and `relatedTools`.
- Those tests describe current implementation behavior, not official MCP field semantics.

### Tool Descriptor

`mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/MCPToolDescriptor.java` currently contains:

- Official-like fields: `name`, `title`, `description`, `outputSchema`, `annotations`, `meta`.
- Local non-official field: `fields`.
- Conversion method: `toInputSchema()` builds the official `inputSchema` JSON Schema object from `fields`.

Current implication:

- `fields` is an internal schema-building DSL, not an official MCP Tool field.
- Public YAML and catalog output should use `inputSchema` directly.

### Tool Wire Mapping

`mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactory.java` maps:

- `name`, `title`, `description`, generated `inputSchema`, `outputSchema`, `annotations`, and `meta` to SDK `McpSchema.Tool`.
- Local `MCPToolAnnotations.returnDirect` into SDK `ToolAnnotations.returnDirect`.

Current implication:

- Core Tool mapping follows SDK `1.1.0` for supported fields.
- SDK `1.1.0` does not expose official `icons` or `execution`.
- SDK `1.1.0` exposes `returnDirect`, but official MCP `2025-11-25` `ToolAnnotations` does not.

### Tool Catalog Payload

`mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java` currently emits:

- Standard-like fields: `name`, `title`, `description`, `outputSchema`, `annotations`, `meta`.
- Non-standard top-level field: `inputFields`.

Current implication:

- `inputFields` is a ShardingSphere presentation of the local DSL.
- It should be replaced by official `inputSchema` in model-facing descriptor output.

### Tool Tests

`mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java` proves:

- The generated SDK tool is expected to expose `name`, `title`, `description`, `inputSchema`, `outputSchema`, `annotations`, and `meta`.
- Current tests expect un-namespaced `relatedResources` in tool `meta`.

`mcp/api/src/test/java/org/apache/shardingsphere/mcp/api/tool/descriptor/MCPToolDescriptorTest.java` proves:

- `fields` currently exists to generate a JSON Schema object with `type`, `properties`, `required`, and `additionalProperties`.
- This confirms `fields` is an implementation helper for `inputSchema`, not a protocol field.

`mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtilsTest.java` proves:

- Tool results include `structuredContent`, text content, and optional `ResourceLink` content.
- Resource link metadata currently uses un-namespaced keys such as `resource_kind`, `purpose`, and `source_field`.
- These result payload/link hints are related MCP surfaces, but they are not Tool descriptor fields.

## SDK Evidence

The local MCP Java SDK source defines:

- `Resource`: `uri`, `name`, `title`, `description`, `mimeType`, `size`, `annotations`, `_meta`.
- `ResourceTemplate`: `uriTemplate`, `name`, `title`, `description`, `mimeType`, `annotations`, `_meta`.
- `Annotations`: `audience`, `priority`, `lastModified`.
- `Tool`: `name`, `title`, `description`, `inputSchema`, `outputSchema`, `annotations`, `_meta`.
- `ToolAnnotations`: `title`, `readOnlyHint`, `destructiveHint`, `idempotentHint`, `openWorldHint`, `returnDirect`.
- `CallToolResult`: `content`, `isError`, `structuredContent`, `_meta`.
- `ResourceLink`: `name`, `title`, `uri`, `description`, `mimeType`, `size`, `annotations`, `_meta`.

SDK gaps against official MCP `2025-11-25`:

- SDK Resource and ResourceTemplate lack `icons`.
- SDK Tool lacks `icons`.
- SDK Tool lacks `execution`.
- SDK ToolAnnotations has `returnDirect`, which is not official MCP `2025-11-25`.

Follow-up source inspection showed that MCP Java SDK `1.1.2` and `2.0.0-M2` still lack direct Resource or Tool `icons` and Tool `execution` support.
The confirmed implementation target is therefore stable SDK `1.1.2` plus local protocol adapter support, not SDK `2.0.0-M2`.

## Phase 1 Implementation Inventory

### Descriptor YAML Scope

Production descriptor YAML files that require migration:

- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`
- `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`
- `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`
- `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`

Test descriptor YAML files that require migration:

- `mcp/support/src/test/resources/META-INF/shardingsphere-mcp/descriptors/test-planning.yaml`

Current YAML drift counts:

```text
core.yaml:
  uriTemplate=19, parameters=16, resourceKind=19, objectScope=17, relatedTools=7, relatedResources=4
  useBefore=1, fields=3, returnDirect=3, completionTargets=1, resourceNavigation=1
support.yaml:
  uriTemplate=1, parameters=1, resourceKind=1, objectScope=1, relatedTools=5, relatedResources=2
  fields=2, returnDirect=2, templateResource=3, completionTargets=1, resourceNavigation=1
encrypt.yaml:
  uriTemplate=3, parameters=2, resourceKind=3, feature=3, relatedTools=4, relatedResources=2
  fields=1, returnDirect=1, templateResource=1, completionTargets=1, resourceNavigation=1
mask.yaml:
  uriTemplate=3, parameters=2, resourceKind=3, feature=3, relatedTools=4, relatedResources=2
  fields=1, returnDirect=1, templateResource=1, completionTargets=1, resourceNavigation=1
test-planning.yaml:
  uriTemplate=1, resourceKind=1, fields=1, returnDirect=1
```

### Tests Locking Old Behavior

Existing tests that must be updated when the descriptor boundary changes:

- `MCPResourceDescriptorTest`: asserts `isTemplated()`, `getUriTemplate()`, and current meta/custom field behavior.
- `MCPToolDescriptorTest`: asserts `fields` to `inputSchema` conversion through `toInputSchema()`.
- `MCPDescriptorCatalogLoaderTest`: asserts `getUriTemplate()`, `getFields()`, `resourceKind`, `objectScope`, and `relatedTools`.
- `WorkflowToolDescriptorsTest`: asserts workflow tool `fields` and field-level schema fragments.
- `MCPResourceSpecificationFactoryTest`: asserts resources are split by `isTemplated()` and template `parameters` are in `_meta`.
- `MCPToolSpecificationFactoryTest`: asserts un-namespaced `relatedResources`, `workflowRole`, `fields`, and `returnDirect`.
- `MCPPromptSpecificationFactoryTest`: asserts prompt `templateResource` and un-namespaced `relatedTools` in prompt meta.
- `ToolHandlerRegistryTest`: asserts tool required arguments through `descriptor.getFields()`.
- `ServerCapabilitiesHandlerTest`: asserts `inputFields`, catalog extension sections, and `payload_contract`.
- `CoreResourceHandlerSurfaceTest`: asserts `getUriTemplate()` and capability catalog extension sections.
- `MCPTransportPayloadUtilsTest`: asserts un-namespaced ResourceLink metadata such as `source_field`.

### Source Hotspots

Source files whose current responsibilities cross the target boundary:

- `MCPResourceDescriptor`: fixed resource and resource template identity are conflated through `uriTemplate` and `isTemplated()`.
- `MCPToolDescriptor`: public contract exposes `fields` and builds official `inputSchema` internally.
- `ResourceHandlerRegistry`: routes resources through descriptor `uriTemplate`.
- `ToolHandlerRegistry`: validates arguments through descriptor `fields`.
- `MCPDescriptorCatalogYamlSwapper`: maps old YAML fields into public descriptors.
- `MCPDescriptorCatalogValidator`: validates old typed fields and old `fields` DSL.
- `MCPDescriptorCatalogPayloadBuilder`: emits old descriptor fields and catalog extension sections as model-facing output.
- `MCPResourceSpecificationFactory`: injects custom resource fields into un-namespaced `_meta`.
- `MCPToolSpecificationFactory`: maps `returnDirect` and generated `inputSchema`.
- `MCPToolElicitationHandler`: reads `workflowRole` from public meta and inspects descriptor `fields`.
- `MCPPromptSpecificationFactory`: exposes `templateResource` in prompt meta.
- `MCPTransportPayloadUtils`: emits un-namespaced result and ResourceLink metadata.

### SDK Upgrade Plan

Current `mcp/bootstrap/pom.xml` uses `mcp-java-sdk.version` `1.1.0`.
Implementation target is `1.1.2`.

The upgrade does not remove the protocol adapter requirement:

- SDK `1.1.2` still lacks direct support for descriptor `icons`.
- SDK `1.1.2` still lacks direct support for Tool `execution.taskSupport`.
- SDK `1.1.2` still exposes `returnDirect`, which must not be part of the public ShardingSphere descriptor contract.
- SDK `2.0.0-M2` remains rejected for this feature because it is a milestone and does not solve the missing official fields.

## Confirmed Field Meaning Matrix

### Resource and ResourceTemplate

| Field | Official meaning | Current local status | Standardization decision |
| --- | --- | --- | --- |
| `uri` | Concrete readable resource identifier. | Represented by `uriTemplate` when no template variable exists. | Add explicit fixed-resource `uri`; stop using `uriTemplate` for fixed resources. |
| `uriTemplate` | Template for constructing concrete resource URIs. | Used for both fixed and templated resources. | Keep only on ResourceTemplate. |
| `name` | Programmatic or logical identifier; display fallback. | Present and tested. | Keep; do not merge with `title`. |
| `title` | Human-readable UI/end-user label. | Present and tested. | Keep; do not merge with `name`. |
| `description` | Human-readable/model-facing description. | Present and tested. | Keep. |
| `icons` | Optional visual identifiers for UI. | Missing from descriptors and SDK `1.1.0`. | Add to protocol model; validate icon shape and adapt through bootstrap. |
| `mimeType` | Optional MIME type of resource content. | Present and tested. | Keep. |
| `annotations` | Client hints: audience, priority, lastModified. | Present and mapped. | Keep as shared `MCPAnnotations`. |
| `size` | Raw resource content size in bytes. | SDK supports fixed Resource; descriptor lacks it. | Add to fixed Resource; not ResourceTemplate. |
| `meta` | Extension metadata; wire name is `_meta`. | Present but keys are often un-namespaced. | Keep Java/YAML `meta`; namespace exposed non-official keys. |
| `parameters` | Not an official ResourceTemplate field. | Used for URI variable descriptions and exposed in `_meta`. | Make internal or expose as namespaced `meta` only when model-facing. |
| `resourceKind` | Not official. | Used as ShardingSphere classification. | Make internal or namespaced derived metadata. |
| `objectScope` | Not official. | Used as ShardingSphere classification. | Make internal or namespaced derived metadata. |
| `feature` | Not official. | Used as ShardingSphere classification. | Make internal or namespaced derived metadata. |
| `relatedTools` | Not official. | Used as guidance/relationship data. | Make internal or namespaced derived metadata. |
| `relatedResources` | Not official. | Used as guidance/relationship data. | Make internal or namespaced derived metadata. |
| `useBefore` | Not official. | Used as model guidance. | Make internal or namespaced derived metadata. |
| `payload_contract` | Not official. | Emitted by catalog builder. | Move out of official descriptor object or expose as namespaced metadata if still needed. |

### Tool

| Field | Official meaning | Current local status | Standardization decision |
| --- | --- | --- | --- |
| `name` | Unique callable tool identifier. | Present and tested. | Keep; do not merge with `title`. |
| `title` | Human-readable UI/end-user label. | Present and tested. | Keep; do not merge with `name`. |
| `description` | Optional human-readable/model-facing description. | Present and tested. | Keep. |
| `icons` | Optional visual identifiers for UI. | Missing from descriptors and SDK `1.1.0`. | Add to protocol model; validate icon shape and adapt through bootstrap. |
| `inputSchema` | Required JSON Schema object for call arguments. | Generated from local `fields`; tested. | Replace public `fields`/`inputFields` with direct root object schema. |
| `execution` | Execution properties such as `taskSupport`. | Missing from descriptors and SDK `1.1.0`. | Add to protocol model; adapt after upgrading to SDK `1.1.2`. |
| `outputSchema` | Optional JSON Schema object for `structuredContent`. | Present and tested for descriptor shape. | Keep; require root object schema and runtime `structuredContent` conformance when present. |
| `annotations` | Tool behavior hints. | Present and tested. | Keep official fields only. |
| `meta` | Extension metadata; wire name is `_meta`. | Present but keys can be un-namespaced. | Keep Java/YAML `meta`; namespace exposed non-official keys. |
| `fields` | Not official. | Local DSL for generating `inputSchema`. | Remove from public descriptor contract. |
| `inputFields` | Not official. | Catalog payload representation of `fields`. | Remove from model-facing descriptor output. |
| `returnDirect` | Not official in MCP `2025-11-25`. | Present in local and SDK annotations. | Delete from public descriptor model. |

### Prompt and PromptArgument

| Field | Official meaning | Current local status | Standardization decision |
| --- | --- | --- | --- |
| `icons` | Optional visual identifiers for UI. | Missing from prompt descriptor. | Add to protocol model; adapt after upgrading to SDK `1.1.2`. |
| `name` | Unique prompt or argument identifier. | Present and tested. | Keep; do not merge with `title`. |
| `title` | Human-readable UI/end-user label. | Present and tested. | Keep; do not merge with `name`. |
| `description` | Human-readable prompt or argument description. | Present and tested. | Keep. |
| `arguments` | Optional list of prompt arguments. | Present and tested. | Keep on Prompt only. |
| `required` | Whether a prompt argument must be provided. | Present and tested. | Keep on PromptArgument only. |
| `meta` | Extension metadata; wire name is `_meta`. | Prompt meta exists but can contain un-namespaced guidance. | Keep Java/YAML `meta`; namespace exposed non-official keys. |
| `templateResource` | Not official. | Public support descriptor and prompt meta expose it. | Move to internal `MCPPromptTemplateBinding`. |
| prompt argument `completion` | Not official. | Potential descriptor-adjacent hint. | Move to completion registry or namespaced metadata if exposed. |

### Completion and Navigation

| Field or section | Official meaning | Current local status | Standardization decision |
| --- | --- | --- | --- |
| `completion/complete` | Official utility request for argument suggestions. | Runtime utility is descriptor-adjacent. | Keep protocol behavior separate from descriptor fields. |
| `PromptReference` | Completion reference to a prompt by name. | Internal registry currently abstracts references. | Use as source meaning for prompt completion targets. |
| `ResourceTemplateReference` | Reference with wire type `ref/resource` and URI or URI template. | Internal registry abstracts references. | Use for resource-template completion targets. |
| `completionTargets` | Not an official descriptor section. | Present in YAML and catalog payload. | Keep internal or ShardingSphere-scoped catalog metadata. |
| `resourceNavigation` | Not an official descriptor section. | Present in YAML and catalog payload. | Keep internal or ShardingSphere-scoped catalog metadata. |

## Related But Not Descriptor Fields

These fields exist on related MCP surfaces and should not be confused with Resource or Tool descriptors:

- `CallToolResult.content`: Unstructured tool result content.
- `CallToolResult.structuredContent`: Structured JSON object returned by a tool.
- `CallToolResult.isError`: Tool execution error flag.
- `CallToolResult.meta`: Result metadata; wire name is `_meta`.
- `ResourceLink.name`, `title`, `uri`, `description`, `mimeType`, `size`, `annotations`, and `meta`: link content returned in tool results.
- `PromptMessage.role` and `PromptMessage.content`: prompt result message fields, not Prompt descriptor fields.
- `CompleteRequest.ref`, `argument`, and `context`: completion utility request fields, not descriptor fields.
- `CompleteResult.completion`: completion utility response payload, not descriptor metadata.
- ShardingSphere payload fields such as `response_mode`, `next_actions`, `resource_kind`, `purpose`, and `source_field`: business payload/link-hint fields, not descriptor fields.

## Final Confirmation

- `name` and `title` have distinct official semantics and must remain separate.
- The current `MCPResourceAnnotations` shape is protocol-aligned, but should become a shared annotations DTO.
- The current resource API conflates fixed resources and templates through `uriTemplate`; official MCP requires `uri` and `uriTemplate` to belong to different shapes.
- The current tool API exposes `fields`/`inputFields`; official MCP requires `inputSchema`.
- Tool `description` is optional, while `name` and `inputSchema` are required.
- Tool `inputSchema` and `outputSchema` must be JSON Schema objects; absent `$schema` means JSON Schema 2020-12.
- Tool annotations have default semantics when absent, but runtime safety must not depend on client interpretation of those hints.
- Current `meta` usage needs namespace cleanup when non-official data is exposed.
- SDK `1.1.2` is the implementation dependency target, but it is still not a sufficient source of truth for official MCP `2025-11-25`.
- Fields missing from SDK `1.1.2`, including `icons` and tool `execution`, remain adapter concerns.
- Prompt descriptors are official objects and belong in the public API descriptor boundary; `templateResource` remains support-owned internal binding.
- Completion and navigation registries are descriptor-adjacent ShardingSphere support data, not official MCP descriptor fields.
- Internal source YAML sections should use explicit `internal*` names so they cannot be mistaken for MCP descriptor sections.
