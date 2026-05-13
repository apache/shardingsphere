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

# Data Model: MCP Protocol Field Standardization

## Official Descriptor Object

Represents one object whose top-level fields are defined by MCP `2025-11-25`.

Validation rules:

- Unknown top-level fields are rejected.
- Validation runs while descriptors are loaded and fails before descriptor publication or transport mapping.
- Java/YAML property name is `meta`; wire-level MCP JSON may serialize it as `_meta`.
- `name` is the stable programmatic identifier and `title` is the human-readable label.
- `icons` is modeled for every official object type that supports it, even when empty.
- Official resource, resource template, tool, prompt, and prompt argument DTOs are owned by `mcp/api`.
- YAML loading, validation, prompt template binding, completion targets, navigation, and runtime-control descriptors are owned by `mcp/support`.
- Internal source YAML sections use the `internal` prefix and are not official descriptor collections.

## MCPResourceDescriptor

Fixed resource descriptor.

Fields:

- `uri`
- `name`
- `title`
- `description`
- `icons`
- `mimeType`
- `annotations`
- `size`
- `meta`

Validation rules:

- `uri` must not contain URI template variables.
- `uriTemplate` is forbidden.
- `parameters`, `resourceKind`, `objectScope`, `feature`, `relatedTools`, `relatedResources`, and `useBefore` are forbidden as top-level fields.

## MCPResourceTemplateDescriptor

Templated resource descriptor.

Fields:

- `uriTemplate`
- `name`
- `title`
- `description`
- `icons`
- `mimeType`
- `annotations`
- `meta`

Validation rules:

- `uriTemplate` must contain at least one template variable.
- URI variable descriptions are internal validation data or exposed as `meta.org.apache.shardingsphere/uri-variables`.
- Fixed `uri` is forbidden.

## MCPUriVariableDescriptor

Internal or derived metadata for URI template variables.

Fields:

- `name`
- `title`
- `description`
- `required`
- `scope`

Validation rules:

- Every variable in `uriTemplate` must have one descriptor when URI variable metadata is present.
- No non-template variable may be declared.
- Exposed form must live under `meta.org.apache.shardingsphere/uri-variables`.

## MCPToolDescriptor

Official tool descriptor.

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

Validation rules:

- `inputSchema` must be a JSON Schema object.
- Root `inputSchema.type` must be `object`.
- When `$schema` is absent, JSON Schema 2020-12 is the effective dialect.
- Runtime validation enforces only the documented compiled subset derived from `inputSchema`.
- Unsupported JSON Schema keywords may remain client-facing guidance but must not be described as server-enforced.
- `fields` and `inputFields` are forbidden in public YAML and catalog output.
- `outputSchema`, when present, must be a JSON Schema object.
- Root `outputSchema.type` must be `object` when `outputSchema` is present.
- When `outputSchema` is present, tool `structuredContent` must conform before the result is exposed to clients.
- Tool names must satisfy the local fail-fast name policy.
- Safety hints belong in annotations or internal configuration according to the official meaning of each annotation.

## MCPToolAnnotations

Official tool behavior hints.

Fields:

- `title`
- `readOnlyHint`
- `destructiveHint`
- `idempotentHint`
- `openWorldHint`

Validation rules:

- `returnDirect` is forbidden.
- Absent booleans retain official default semantics and should remain absent on the wire unless explicitly configured.
- Hints do not authorize execution and cannot replace server-side safety checks.

## MCPToolExecution

Official tool execution metadata.

Fields:

- `taskSupport`

Validation rules:

- Allowed values are the official MCP values for task support.
- The absent value means `forbidden`.
- SDK `1.1.2` may not emit this field directly, so bootstrap must adapt without changing the descriptor model.

## MCPPromptDescriptor

Official prompt descriptor.

Fields:

- `icons`
- `name`
- `title`
- `description`
- `arguments`
- `meta`

Validation rules:

- `templateResource` is forbidden as a top-level field.
- Prompt template binding is internal.

## MCPPromptArgumentDescriptor

Official prompt argument descriptor.

Fields:

- `name`
- `title`
- `description`
- `required`

Validation rules:

- Completion hints are forbidden as top-level prompt argument fields.

## MCPPromptTemplateBinding

Internal binding between prompt descriptor and prompt template resource.

Fields:

- `promptName`
- `templateResource`

Validation rules:

- `promptName` must reference an existing prompt descriptor.
- `templateResource` must resolve to an existing classpath resource.
- Binding is not exposed as an official prompt top-level field.

## MCPCompletionTargetDescriptor

Internal completion registry entry under `internalCompletionTargets`.

Fields:

- `referenceType`
- `reference`
- `arguments`
- `maxValues`
- `meta`

Validation rules:

- Registry is internal or ShardingSphere-scoped catalog metadata.
- It is not an official descriptor object.
- References map to official PromptReference or ResourceTemplateReference semantics.
- Exposed metadata must use namespaced keys.

## MCPReferenceNavigationDescriptor

Internal relationship graph for model guidance under `internalReferenceNavigation`.

Fields:

- `sourceRef`
- `targetRef`
- `requiredArguments`
- `carriedArguments`
- `description`

Validation rules:

- Replaces ambiguous `from` and `to` naming.
- It is not an official descriptor object.
- Exposed form must be ShardingSphere-scoped metadata or capabilities guidance.

## MCPToolArgumentContract

Internal runtime argument contract derived from a tool `inputSchema`.

Fields:

- `toolName`
- `requiredArguments`
- `requiredTextArguments`
- `enumArguments`

Validation rules:

- It is derived from `inputSchema` and not exposed as an official descriptor field.
- It preserves required argument checks and blank text checks from the current runtime behavior.
- It preserves the specialized missing `execution_mode` exception and preview suggestion.

## MCPToolRuntimeDescriptor

Internal runtime policy under `internalToolRuntime`.

Fields:

- `toolName`
- `workflowRole`
- `approvalPolicy`
- `safetyPolicy`

Validation rules:

- It is not exposed as tool `meta`.
- It drives runtime safety decisions together with core runtime policy.
- Direct-result behavior is not carried forward unless implementation proves an existing runtime behavior requires it.
- Any direct-result behavior must use a new internal name and must not serialize as `returnDirect`.

## MCPIcon

Official icon descriptor shared by supported MCP object types.

Fields:

- `src`
- `mimeType`
- `sizes`
- `theme`

Validation rules:

- `src` is required when an icon entry exists.
- `src` must use `https` or `data` in source descriptors.
- `sizes` entries must be `any` or `WxH`.
- `theme` must be `light` or `dark` when present.

## ShardingSphere Extension Metadata

Namespaced metadata exposed to MCP clients or models.

Rules:

- Exposed keys must begin with `org.apache.shardingsphere/`.
- `org.apache.shardingsphere/` is the only ShardingSphere extension namespace.
- Exposed values are read-only and derived.
- Internal typed configuration is the source of truth when the same concept is needed internally.

Allowed examples:

- `org.apache.shardingsphere/resource-kind`
- `org.apache.shardingsphere/object-scope`
- `org.apache.shardingsphere/feature`
- `org.apache.shardingsphere/uri-variables`
- `org.apache.shardingsphere/related-tools`
- `org.apache.shardingsphere/related-resource-uris`

Internal-only examples:

- `workflowRole`
- `templateResource`
- safety execution decisions
- completion source wiring
