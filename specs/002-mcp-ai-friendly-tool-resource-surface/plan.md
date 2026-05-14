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

# Implementation Plan: AI-Friendly MCP Tool and Resource Surface

**Branch Constraint**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.  
**Spec**: `specs/002-mcp-ai-friendly-tool-resource-surface/spec.md`  
**Scope**: MCP API descriptors, external descriptor catalog, transport discovery metadata, core and feature tool/resource surfaces, workflow guidance, recoverable errors, tests, and docs.

## Technical Context

- **Language/runtime**: Java, MCP subchain integrated with JDK 21.
- **MCP Java SDK**: `io.modelcontextprotocol.sdk` version `1.1.0` in `mcp/bootstrap/pom.xml`.
- **SDK capability finding**: local SDK classes expose tool output schema, annotations, and metadata fields.
  They also expose resource and resource-template title, description, annotations, and metadata fields.
- **Primary modules**: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`, `test/e2e/mcp`.
- **Descriptor source**: external YAML descriptor files are the planned canonical source.
- **Capability strategy**: selected Option A; `shardingsphere://capabilities` publishes a full generated descriptor catalog.
- **SQL tool split**: use `execute_query` for read-only SQL and `execute_update` for update-capable SQL unless a later naming review changes this before coding.

## Compliance Checklist

- Re-read `AGENTS.md` and `CODE_OF_CONDUCT.md` immediately before implementation.
- Use `rg`, `./mvnw`, and `apply_patch` for inspection, verification, and manual edits.
- Do not run `git switch`, `git checkout`, `git reset --hard`, or branch-creating Spec Kit commands.
- Do not edit generated `target/` files.
- Treat descriptor YAML files as source files and include ASF license headers.
- Keep comments and descriptor text in English.
- Keep public API Javadocs clear and comprehensive for descriptor and schema contracts.
- Avoid inline fully qualified class names.
- Keep variable declarations near first use and follow project style when implementation begins.
- Default to Mockito for tests and keep static or constructor mocking aligned with project rules.

## Design Overview

### Descriptor Catalog

Create external YAML descriptor files for the MCP model-facing surface.
The catalog should describe resources, resource templates, tools, workflows, output schemas, side effects, related resources, related tools, and prompt/completion requirements.

Planned placement:

```text
mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/
mcp/support/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/
```

Feature modules may own feature-local descriptor files under their own resources:

```text
mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/
mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/
```

The runtime should load and validate these descriptors through the same service-loader-friendly path used by handlers.
Standard MCP discovery responses and `shardingsphere://capabilities` must be generated from this shared descriptor source.

### Resource Descriptor Contract

Add a direct descriptor requirement to `MCPResourceHandler`.
The target contract is API-breaking and does not need default-method compatibility.

Conceptual shape:

```java
MCPResourceDescriptor getResourceDescriptor();
```

`MCPResourceDescriptor` must cover:

- URI or URI template
- name
- title
- description
- MIME type
- variable metadata
- annotations
- related tools
- examples
- model-facing metadata

After migration, resource publishing must not synthesize business descriptions from URI strings.

### Tool Descriptor Enrichment

Extend the existing `MCPToolDescriptor` model to cover:

- related resources
- related tools
- output schema
- annotations
- examples
- workflow role
- side-effect metadata
- model-facing metadata

Use MCP SDK `Tool.outputSchema`, `Tool.annotations`, and `Tool.meta` when creating transport-level tool specifications.

### Structured Input Schema

Extend `MCPToolValueDefinition` so object fields can describe named properties.
Open-ended objects remain allowed only for plugin-defined algorithm property maps where keys depend on runtime-visible algorithms.

Known object fields that must become structured:

- `structured_intent_evidence`
- `user_overrides`
- encrypt primary algorithm properties
- encrypt assisted-query algorithm properties
- encrypt LIKE-query algorithm properties
- mask primary algorithm properties

### Output Schema

Create model-facing output definitions for:

- `search_metadata`
- `execute_query`
- `execute_update`
- `plan_encrypt_rule`
- `plan_mask_rule`
- `apply_workflow`
- `validate_workflow`

Transport should publish these through MCP SDK `Tool.outputSchema`.
Tool results should continue returning `structuredContent`.

### Workflow Guidance

Workflow planning and execution responses must include stable navigation fields:

- `status`
- `next_actions`
- `recommended_next_tool`
- `resources_to_read`
- `requires_user_approval`
- `plan_id`

The response builder should be the single place that normalizes these fields for encrypt and mask planning tools.
Apply and validation responses should also produce next-action guidance.

### Capability Catalog

`shardingsphere://capabilities` becomes the full generated operating map for models.
It must include:

- runtime topology and limitations
- resources and resource templates
- tools
- workflow graph
- side-effect summaries
- prompt requirements
- completion requirements
- compact identifiers for quick scanning

The full catalog must be generated from the same descriptor source as standard MCP discovery to avoid duplicated truth.

### SQL Tool Split

Split the current SQL execution surface into:

- `execute_query`: read-only SQL only; model-facing description and annotations must say it is read-only.
- `execute_update`: update-capable SQL; model-facing description and annotations must say it may mutate data, metadata, rules, or physical structures.

The update-capable tool must have stricter approval wording and output fields that summarize what ran.

### Recoverable Errors

Introduce structured recoverable error payloads for:

- missing arguments
- invalid enum values
- invalid object fields
- unsupported tool names
- unsupported resource URIs
- missing workflow plans
- stale workflow plans
- unsafe side-effect attempts

Errors must be safe to show users and must not leak secret algorithm properties.

### Prompts and Completions

Prompts and completions are recorded in descriptors but are not implemented in phase 1.
The descriptor format should leave room for later prompt and completion entries so the next phase does not need another catalog redesign.

## Implementation Phases

### Phase 0: Discovery and SDK Grounding

- Verify MCP SDK `1.1.0` builder APIs for tool output schema, tool annotations, resource annotations, resource template metadata, prompts, and completions.
- Decide final YAML schema names and file locations.
- Confirm final tool names `execute_query` and `execute_update`.
- Confirm descriptor loading path does not create module dependency cycles.

### Phase 1: Descriptor Model and Loader

- Add API descriptor classes for resources, resource parameters, tools, output schema, workflow guidance, related entries, examples, and side effects.
- Add descriptor YAML parser and validator in the proper MCP layer.
- Add catalog loading that merges core, encrypt, and mask descriptor files.
- Add descriptor validation errors with clear source-file and entry identifiers.

### Phase 2: Resource Discovery Migration

- Add direct resource descriptor method to `MCPResourceHandler`.
- Update core, encrypt, mask, and E2E fixture resource handlers.
- Update resource registry and bootstrap resource specification factory to use descriptors.
- Publish resource and resource template `title`, `description`, `annotations`, and `meta`.
- Remove URI-generated placeholder descriptions.

### Phase 3: Tool Descriptor Enrichment

- Extend `MCPToolDescriptor` and `MCPToolValueDefinition`.
- Update core, workflow, encrypt, mask, and E2E fixture tool handlers.
- Publish tool output schema, annotations, relationships, and metadata through bootstrap.
- Convert known object parameters from open-ended objects to structured schemas.

### Phase 4: SQL Tool Split

- Refactor current SQL execution handler into read-only query and update-capable handlers.
- Enforce read-only validation for `execute_query`.
- Add explicit side-effect metadata and result schema for `execute_update`.
- Update capability catalog, README examples, and tests for the split.

### Phase 5: Workflow Guidance and Output Contracts

- Normalize planning status values.
- Add stable next-action fields to encrypt and mask planning responses.
- Add next-action fields to apply and validation responses.
- Publish output schemas for planning, apply, validation, metadata search, query, and update tools.

### Phase 6: Capability Catalog

- Replace `shardingsphere://capabilities` payload with the full generated descriptor catalog.
- Keep compact identifiers inside the catalog for quick scanning.
- Add runtime topology and workflow limitations.
- Ensure the catalog and standard discovery endpoints are generated from the same descriptor data.

### Phase 7: Recoverable Errors

- Add structured recovery metadata to MCP error responses.
- Cover missing fields, invalid enums, invalid object fields, unsupported resources, unsupported tools, and workflow state errors.
- Ensure errors do not expose secret values.

### Phase 8: Documentation

- Update `mcp/README.md` and `mcp/README_ZH.md`.
- Document the descriptor YAML authoring contract for new MCP features.
- Document the normal model path for inspect, plan, apply, and validate.
- Record prompt and completion requirements as deferred work.

### Phase 9: Verification

- Run targeted unit tests for touched MCP modules.
- Run module-scoped Checkstyle and Spotless.
- Run protocol golden tests for discovery and capability catalog outputs.
- Run search checks for generated resource descriptions, open-ended known object schemas, and stale SQL tool names.

## Risk and Mitigation

- **Descriptor drift**: Generate all discovery outputs and the capability catalog from one descriptor source.
- **Large capability payload**: Keep compact identifiers, group entries by feature, and consider future pagination only if catalog size becomes a real issue.
- **SDK field mismatch**: Verify SDK `1.1.0` APIs before implementation and keep fallback metadata only if a field is missing.
- **SQL side effects**: Split query and update tools early so model-facing safety semantics are not bolted on late.
- **Workflow inconsistency**: Centralize response guidance builders for planning, apply, and validation responses.
- **Secret leakage**: Keep masking and recovery error tests around algorithm properties and generated SQL payloads.

## Verification Commands

Planned scoped verification after implementation:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck checkstyle:check
```

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck spotless:check
```

Planned search checks:

```bash
rg -n "ShardingSphere MCP resource:|ShardingSphere MCP resource template:" mcp test/e2e/mcp --glob '!target/**'
```

```bash
rg -n "additionalProperties\", true|additionalProperties\\(\\)" mcp --glob '!target/**'
```

```bash
rg -n "execute_query|execute_update" mcp test/e2e/mcp --glob '!target/**'
```

## Open Implementation Decisions

- Confirm whether YAML descriptor files should live in each module or a centralized MCP descriptor module.
- Confirm whether generated README snippets should be produced by a build helper or checked manually against golden tests.
- Confirm exact enum names for workflow `status` and `next_actions.action_kind`.
