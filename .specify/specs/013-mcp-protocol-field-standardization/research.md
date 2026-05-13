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

# Research: MCP Protocol Field Standardization

## Decision: Lock the standard to MCP 2025-11-25

**Rationale**: The user explicitly confirmed that the protocol standard must be locked to `2025-11-25`.
This prevents implementation drift from a moving `latest` target and gives validators a fixed allowed-field set.

**Alternatives considered**:

- Follow latest official schema dynamically: rejected because new protocol releases would silently change field expectations.
- Follow current MCP Java SDK shape: rejected because SDK version is an implementation constraint, not the protocol standard.

## Decision: Upgrade MCP Java SDK to 1.1.2 and treat remaining gaps as adapter concerns

**Rationale**: Current MCP Java SDK `1.1.0` supports several descriptor fields but its local schema source does not expose every required `2025-11-25` field, such as `icons` and tool `execution`.
Maven metadata shows stable `1.1.2` is available on the 1.x line, while `2.0.0-M2` is a milestone.
Source inspection of both `1.1.2` and `2.0.0-M2` still shows no direct support for Resource or Tool `icons` or Tool `execution`.
The dependency should therefore move to stable SDK `1.1.2`, and bootstrap transport should adapt any remaining official field gaps without weakening the descriptor contract.

**Alternatives considered**:

- Lower the descriptor model to SDK `1.1.0`: rejected because it would preserve a stale protocol shape.
- Upgrade to SDK `2.0.0-M2`: rejected because it is a milestone and does not remove the need for local adapter support.
- Stay on SDK `1.1.0`: rejected because stable `1.1.2` is available and should be the baseline for this implementation.

## Decision: Split fixed resources and resource templates

**Rationale**: Current `MCPResourceDescriptor` uses `uriTemplate` for both fixed and templated resources and relies on `isTemplated()`.
Official MCP models fixed resources and resource templates as separate shapes with different identity fields.
Splitting them removes ambiguous naming and makes validation precise.

**Alternatives considered**:

- Keep one descriptor with `isTemplated()`: rejected because it preserves the ambiguous `uriTemplate` dual use.
- Rename `uriTemplate` to `uriPattern`: rejected because it still does not match official MCP fields.

## Decision: Replace public tool field DSL with inputSchema

**Rationale**: Current YAML uses `fields` and catalog output uses `inputFields`.
Official MCP tool descriptors use `inputSchema`.
Keeping a public DSL creates a parallel schema language and forces catalog builders to translate field names that clients should never see.

**Alternatives considered**:

- Keep `fields` as YAML shorthand: rejected because compatibility is out of scope and descriptor YAML should be standard.
- Keep `MCPToolValueDefinition` as an internal helper only: acceptable if it does not appear as public descriptor YAML or catalog output.

## Decision: Use minimal metadata exposure

**Rationale**: The user confirmed minimal exposure.
Runtime control information such as workflow role, prompt template path, safety decisions, and completion source wiring should remain typed internal configuration.
Only read-only derived guidance that helps clients or models may be exposed under namespaced `meta`.
The only ShardingSphere extension namespace is `org.apache.shardingsphere/`.

**Alternatives considered**:

- Expose all ShardingSphere guidance in `meta`: rejected because it enlarges the public contract and leaks implementation wiring.
- Hide all ShardingSphere guidance: rejected because clients and models would lose useful navigation and relationship hints.

## Decision: Keep business payload cleanup out of scope

**Rationale**: The user confirmed descriptor standardization should not become a broad business payload refactor.
Fields such as `response_mode`, `next_actions`, `recovery`, resource hints, and workflow guidance remain ShardingSphere payload fields.
The only exception is when they are incorrectly documented as official descriptor fields.

**Alternatives considered**:

- Standardize all payload fields now: rejected due to scope expansion.
- Ignore payload references entirely: rejected because capabilities and catalog text must not mislabel custom payload fields as MCP protocol fields.

## Decision: Reject old descriptor fields instead of migrating them

**Rationale**: Backward compatibility is completely out of scope. Validators should fail fast on old non-standard descriptor fields, and migration should update all in-repo YAML descriptors directly.
Validation is a descriptor loading concern: bad descriptors must fail before publication or transport mapping.

**Alternatives considered**:

- Accept old fields and translate them: rejected because it preserves ambiguity and weakens the cleanup.
- Warn but continue: rejected because future descriptor drift would remain possible.
- Validate only at transport output time: rejected because invalid descriptors would remain accepted internally and fail too late.

## Decision: Cover all descriptor-adjacent surfaces in one implementation

**Rationale**: The user confirmed the implementation should cover resources, resource templates, tools, prompts, completion registry, navigation registry, and catalog payload surfaces together.
This prevents a partial cleanup where resource and tool descriptors become standard but prompt, completion, navigation, or catalog sections keep ambiguous field semantics.

**Alternatives considered**:

- Implement only resource and tool first: rejected because the same descriptor drift exists on adjacent MCP surfaces.
- Leave completion and navigation untouched: rejected because they can still be mislabeled as official descriptor fields.

## Decision: Prove SDK adapter feasibility before relying on SDK gap handling

**Rationale**: MCP Java SDK `1.1.x` records do not directly expose every MCP `2025-11-25` descriptor field.
The implementation must therefore prove the chosen bootstrap wire strategy before DTO migration assumes `icons`, `tool.execution`, or `meta` to `_meta` mapping works.

**Alternatives considered**:

- Put missing official fields into `meta`: rejected because it would make official MCP fields look like ShardingSphere extensions.
- Lower the descriptor contract to SDK fields: rejected because the protocol standard is official MCP `2025-11-25`.
- Defer adapter proof until the end: rejected because a failed adapter would invalidate earlier DTO and YAML choices.

## Decision: Validate raw YAML keys or prove strict binding

**Rationale**: The current loader uses `YamlEngine.unmarshal`.
If typed binding ever skips unknown properties, the descriptor validator would not see old fields and fail-fast validation would be illusory.
The implementation must either validate raw YAML maps before binding or prove the direct binding path rejects unknown keys for every descriptor object.

**Alternatives considered**:

- Rely on later catalog validation only: rejected because unknown fields may already be lost.
- Allow old fields and warn: rejected because compatibility is out of scope.

## Decision: Compile runtime argument validation from inputSchema

**Rationale**: Core runtime currently validates required arguments from the public `fields` DSL.
After public descriptors move to official `inputSchema`, runtime should use a derived internal argument contract instead of reintroducing `fields` or adding a broad JSON Schema runtime dependency.

**Alternatives considered**:

- Keep `fields` internally inside the public DTO: rejected because it preserves the ambiguous contract.
- Add a full JSON Schema validator immediately: rejected as unnecessary for preserving current required-argument behavior.

## Decision: Enforce Resource and Tool schema details from official MCP docs

**Rationale**: A second source-driven pass over Resource and Tool showed that the previous design had the right field set but needed tighter validation semantics.
Tool `description` is optional, `inputSchema` is required, absent `$schema` means JSON Schema 2020-12, and `outputSchema` must constrain structured content when present.
Icons also need source-level validation because official guidance treats icon metadata as untrusted and requires consumers to reject unsafe schemes.

**Alternatives considered**:

- Treat schema details as transport-only behavior: rejected because bad descriptors should fail during descriptor loading.
- Accept any icon URI and rely on clients: rejected because source descriptors can avoid publishing unsafe icon sources.
- Keep `description` effectively required in tests: rejected because official Tool schema makes it optional.

## Decision: Prefix internal source YAML sections

**Rationale**: Names such as `completionTargets` and `resourceNavigation` can be mistaken for official descriptor sections.
Internal source sections therefore use explicit names: `internalPromptTemplateBindings`, `internalCompletionTargets`, `internalReferenceNavigation`, and `internalToolRuntime`.

**Alternatives considered**:

- Keep old section names and document them as internal: rejected because the field names remain ambiguous.
- Move all internal wiring out of YAML: rejected because descriptor-adjacent support data still needs source-controlled configuration.
