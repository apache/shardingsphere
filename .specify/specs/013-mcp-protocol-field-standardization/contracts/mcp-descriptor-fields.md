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

# Contract: MCP Descriptor Fields

## Standard

The descriptor field contract is locked to official MCP `2025-11-25`.
Java and YAML use `meta`; MCP wire JSON may serialize that field as `_meta`.
Implementation uses MCP Java SDK `1.1.2` plus local adapter support for official fields not directly represented by that SDK.
MCP Java SDK `2.0.0-M2` is not part of this contract.

## Allowed Top-Level Fields

### Resource

- `uri`
- `name`
- `title`
- `description`
- `icons`
- `mimeType`
- `annotations`
- `size`
- `meta`

### ResourceTemplate

- `uriTemplate`
- `name`
- `title`
- `description`
- `icons`
- `mimeType`
- `annotations`
- `meta`

### Tool

- `icons`
- `name`
- `title`
- `description`
- `inputSchema`
- `execution`
- `outputSchema`
- `annotations`
- `meta`

### ToolAnnotations

- `title`
- `readOnlyHint`
- `destructiveHint`
- `idempotentHint`
- `openWorldHint`

### ToolExecution

- `taskSupport`

### Prompt

- `icons`
- `name`
- `title`
- `description`
- `arguments`
- `meta`

### PromptArgument

- `name`
- `title`
- `description`
- `required`

### Icon

- `src`
- `mimeType`
- `sizes`
- `theme`

### Annotations

- `audience`
- `priority`
- `lastModified`

## Banned Current Descriptor Fields

These fields must not appear as official descriptor top-level fields:

- `parameters`
- `resourceKind`
- `objectScope`
- `feature`
- `relatedTools`
- `relatedResources`
- `useBefore`
- `fields`
- `inputFields`
- `returnDirect`
- `templateResource`
- prompt argument `completion`
- descriptor-level `completionTargets`
- descriptor-level `resourceNavigation`
- descriptor-level `protocolAvailability`
- descriptor-level `fingerprints`
- descriptor-level `modelFacingSchemas`
- descriptor-level `payload_contract`

## Internal YAML Sections

The only non-descriptor top-level YAML sections allowed for ShardingSphere internal wiring are:

- `internalPromptTemplateBindings`
- `internalCompletionTargets`
- `internalReferenceNavigation`
- `internalToolRuntime`

Unprefixed legacy sections such as `completionTargets` and `resourceNavigation` are invalid in source descriptor YAML.
They may appear only as old-field negative test fixtures.

## Metadata Contract

Exposed non-official metadata must use namespaced keys:

```yaml
meta:
  org.apache.shardingsphere/resource-kind: list
  org.apache.shardingsphere/object-scope: logical-table
```

Source descriptor YAML accepts only ShardingSphere extension metadata under the `org.apache.shardingsphere/` namespace.
MCP-reserved `_meta` keys are a wire/runtime concern and must not be used as a loophole for un-namespaced source descriptor metadata.
The only ShardingSphere extension metadata namespace is `org.apache.shardingsphere/`.

## Validation Contract

Descriptor validation runs during descriptor loading and fails before descriptor publication or transport mapping.
The validator must reject:

- unknown top-level fields on official descriptor objects;
- unknown source YAML keys before they are silently ignored by typed binding;
- banned legacy fields listed in this contract;
- `uriTemplate` on fixed resources and `uri` on resource templates;
- resource templates whose `uriTemplate` contains no template variables;
- tool `fields`, catalog `inputFields`, and annotation `returnDirect`;
- unprefixed internal YAML sections such as `completionTargets` and `resourceNavigation`;
- tool names that are blank, longer than 128 characters, duplicated by exact case-sensitive match, or outside the local allowed character set;
- icon `src` values that do not use `https` or `data`, invalid icon `sizes`, or invalid icon `theme`;
- un-namespaced exposed extension metadata;
- invalid official enum values, including annotation audience and tool execution `taskSupport`;
- invalid JSON Schema structure for tool `inputSchema` and `outputSchema`, including non-object root schemas;
- claims of runtime `inputSchema` enforcement outside the documented compiled subset;
- descriptor-level `completionTargets` and `resourceNavigation` if they appear inside official descriptor objects.

When a tool descriptor declares `outputSchema`, tool execution must validate returned `structuredContent` against that schema before exposing the result.
Descriptors that cannot provide conforming `structuredContent` should omit `outputSchema`.

## Field Disposition

- Resource `parameters` becomes internal URI variable validation data or `meta.org.apache.shardingsphere/uri-variables`.
- Resource `resourceKind`, `objectScope`, and `feature` become derived namespaced metadata when model-facing.
- Resource `relatedTools`, `relatedResources`, and `useBefore` become relationship metadata or capabilities guidance.
- Tool `fields` becomes official `inputSchema`.
- Tool annotation `returnDirect` is deleted.
- Prompt `templateResource` becomes internal prompt-template binding.
- Prompt argument `completion` becomes internal completion registry or namespaced metadata.
- `completionTargets` becomes `internalCompletionTargets`.
  When mapped to official completion semantics, references are PromptReference or ResourceTemplateReference.
- `resourceNavigation` becomes `internalReferenceNavigation` backed by `MCPReferenceNavigationDescriptor`.
- Tool runtime data such as workflow role, approval policy, and any direct-result behavior becomes `internalToolRuntime`.

## Example Resource

```yaml
resources:
  - uri: shardingsphere://capabilities
    name: capabilities
    title: Capabilities
    description: Read the ShardingSphere MCP capability catalog.
    mimeType: application/json
    annotations:
      audience:
        - assistant
      priority: 1.0
```

## Example Resource Template

```yaml
resourceTemplates:
  - uriTemplate: shardingsphere://databases/{database}/schemas/{schema}/tables
    name: schema-tables
    title: Schema Tables
    description: List logical tables visible in one schema.
    mimeType: application/json
    meta:
      org.apache.shardingsphere/uri-variables:
        - name: database
          title: Database
          description: ShardingSphere logical database name.
          required: true
          scope: logical-database
```

## Example Tool

```yaml
tools:
  - name: search_metadata
    title: Search Metadata
    description: Search logical metadata.
    inputSchema:
      type: object
      properties:
        database:
          type: string
          description: ShardingSphere logical database name.
      required:
        - database
      additionalProperties: false
    outputSchema:
      type: object
      properties:
        response_mode:
          type: string
    annotations:
      readOnlyHint: true
      destructiveHint: false
      idempotentHint: true
      openWorldHint: false
```

## Example Prompt

```yaml
prompts:
  - name: inspect_metadata
    title: Inspect Metadata
    description: Guide metadata inspection.
    arguments:
      - name: database
        title: Database
        description: ShardingSphere logical database name.
        required: false
```
