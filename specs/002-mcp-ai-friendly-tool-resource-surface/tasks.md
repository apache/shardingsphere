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

# Tasks: AI-Friendly MCP Tool and Resource Surface

**Input**: `specs/002-mcp-ai-friendly-tool-resource-surface/spec.md` and `specs/002-mcp-ai-friendly-tool-resource-surface/plan.md`  
**Constraint**: Do not switch branches. Implementation was explicitly confirmed before edits.

## Phase 0: Safety and Grounding

- [x] T001 Confirm implementation approval before API-breaking MCP descriptor changes.
- [x] T002 Confirm branch remains `001-shardingsphere-mcp`.
- [x] T003 Confirm no generated `target/` paths are in edit scope.
- [x] T004 Re-read `AGENTS.md` and `CODE_OF_CONDUCT.md` immediately before coding.
- [x] T005 Verify MCP Java SDK `1.1.0` builder APIs for `Tool.outputSchema`, `Tool.annotations`, `Resource.meta`, `ResourceTemplate.meta`, prompts, and completions.
- [x] T006 Confirm descriptor file location strategy: module-local descriptors, centralized descriptor module, or hybrid.
- [x] T007 Confirm YAML descriptor schema and file naming convention.
- [x] T008 Confirm final SQL tool names, provisionally `execute_query` and `execute_update`.

## Phase 1: Descriptor Schema and Catalog Model

- [x] T009 Add descriptor model for resources.
- [x] T010 Add descriptor model for resource template parameters.
- [x] T011 Add descriptor model for tools.
- [x] T012 Add descriptor model for structured input object properties.
- [x] T013 Add descriptor model for output schemas.
- [x] T014 Add descriptor model for related resources and related tools.
- [x] T015 Add descriptor model for examples.
- [x] T016 Add descriptor model for side-effect metadata.
- [x] T017 Add descriptor model for workflow guidance.
- [x] T018 Add descriptor model for prompt and completion requirements as deferred entries.
- [x] T019 Add descriptor catalog aggregate model for `shardingsphere://capabilities`.
- [x] T020 Add public API Javadocs for descriptor model classes.

## Phase 2: External YAML Descriptor Loading

- [x] T021 Add core descriptor YAML files with ASF license headers.
- [x] T022 Add encrypt descriptor YAML files with ASF license headers.
- [x] T023 Add mask descriptor YAML files with ASF license headers.
- [x] T024 Add descriptor parser using the existing repository YAML or structured parsing conventions.
- [x] T025 Add descriptor catalog loader that discovers descriptor files from MCP modules.
- [x] T026 Add descriptor validation for missing names, titles, descriptions, MIME types, and duplicate identifiers.
- [x] T027 Add descriptor validation for placeholder descriptions and URI-only descriptions.
- [x] T028 Add descriptor validation for missing URI template parameter metadata.
- [x] T029 Add descriptor validation for known object parameters without declared properties.
- [x] T030 Add descriptor validation for side-effecting tools without side-effect metadata.
- [x] T031 Add descriptor loader tests for core, encrypt, and mask descriptors.

## Phase 3: Resource Descriptor API Migration

- [x] T032 Add direct resource descriptor method to `MCPResourceHandler`.
- [x] T033 Remove reliance on `getUriPattern()` as the protocol metadata source.
- [x] T034 Update `ServerCapabilitiesHandler` to return a resource descriptor.
- [x] T035 Update `DatabaseCapabilitiesHandler` to return a resource descriptor.
- [x] T036 Update `MetadataResourceHandler` and core metadata resources to return descriptors.
- [x] T037 Update encrypt resource handlers to return descriptors.
- [x] T038 Update mask resource handlers to return descriptors.
- [x] T039 Update E2E fixture resource handlers to return descriptors.
- [x] T040 Update `ResourceHandlerRegistry` to register by descriptor URI or URI template.
- [x] T041 Keep duplicate and overlapping URI validation using descriptor URI patterns.
- [x] T042 Update resource registry tests for descriptor registration.

## Phase 4: Resource Transport Publishing

- [x] T043 Update `MCPResourceSpecificationFactory` to publish descriptor `name`, `title`, `description`, `mimeType`, `annotations`, and `meta`.
- [x] T044 Publish resource template parameter metadata in `ResourceTemplate.meta`.
- [x] T045 Remove generated placeholder descriptions from resource publishing.
- [x] T046 Add protocol golden tests for `resources/list`.
- [x] T047 Add protocol golden tests for `resources/templates/list`.
- [x] T048 Add search verification for removed placeholder descriptions.

## Phase 5: Tool Descriptor Enrichment

- [x] T049 Extend `MCPToolDescriptor` with related resources.
- [x] T050 Extend `MCPToolDescriptor` with related tools.
- [x] T051 Extend `MCPToolDescriptor` with output schema metadata.
- [x] T052 Extend `MCPToolDescriptor` with tool annotations.
- [x] T053 Extend `MCPToolDescriptor` with examples and model-facing metadata.
- [x] T054 Extend `MCPToolValueDefinition` to support structured object properties.
- [x] T055 Update `MCPToolSpecificationFactory` to publish `outputSchema`, `annotations`, and `meta`.
- [x] T056 Update tool descriptor validation for missing or placeholder model-facing metadata.
- [x] T057 Update tool descriptor tests.
- [x] T058 Add protocol golden tests for `tools/list`.

## Phase 6: Core Tool Migration

- [x] T059 Update `search_metadata` description, related resources, structured input schema, and output schema.
- [x] T060 Split current SQL execution into read-only `execute_query`.
- [x] T061 Add update-capable `execute_update`.
- [x] T062 Enforce read-only statement validation for `execute_query`.
- [x] T063 Add side-effect metadata for `execute_update`.
- [x] T064 Add output schemas for `execute_query` and `execute_update`.
- [x] T065 Update `ToolHandlerRegistry` tests for split SQL tools.
- [x] T066 Update SQL execution handler tests for read-only and update-capable paths.

## Phase 7: Workflow Tool Descriptor Migration

- [x] T067 Update shared workflow planning input field descriptions.
- [x] T068 Structure `structured_intent_evidence` schema.
- [x] T069 Structure `user_overrides` schemas.
- [x] T070 Structure encrypt algorithm property schemas where keys are known.
- [x] T071 Keep plugin-defined algorithm property maps open only where runtime algorithm requirements define keys.
- [x] T072 Update `plan_encrypt_rule` descriptor with related encrypt resources and follow-up tools.
- [x] T073 Update `plan_mask_rule` descriptor with related mask resources and follow-up tools.
- [x] T074 Add output schemas for `plan_encrypt_rule` and `plan_mask_rule`.
- [x] T075 Update `apply_workflow` descriptor with side-effect metadata and output schema.
- [x] T076 Update `validate_workflow` descriptor with output schema and recovery guidance.
- [x] T077 Update workflow descriptor tests.

## Phase 8: Workflow Guidance Responses

- [x] T078 Define stable workflow `status` values.
- [x] T079 Define `next_actions` entry schema.
- [x] T080 Add `recommended_next_tool` where one next tool is preferred.
- [x] T081 Add `resources_to_read` where more context should be loaded.
- [x] T082 Add `requires_user_approval` to side-effecting follow-up guidance.
- [x] T083 Update encrypt planning response builder.
- [x] T084 Update mask planning response builder.
- [x] T085 Update apply workflow response builder.
- [x] T086 Update validation workflow response builder.
- [x] T087 Add workflow response contract tests.

## Phase 9: Full Capability Catalog

- [x] T088 Replace `shardingsphere://capabilities` payload with the generated full descriptor catalog.
- [x] T089 Include compact tool and resource identifiers in the catalog.
- [x] T090 Include resources and resource templates in the catalog.
- [x] T091 Include tools and output schemas in the catalog.
- [x] T092 Include workflow graph relationships in the catalog.
- [x] T093 Include side-effect summaries in the catalog.
- [x] T094 Include prompt and completion requirements as deferred entries.
- [x] T095 Include runtime topology and ShardingSphere-Proxy workflow limitations.
- [x] T096 Add capability catalog golden tests.
- [x] T097 Add tests that verify catalog and standard discovery share the same descriptor source.

## Phase 10: Recoverable Errors

- [x] T098 Add recoverable error payload model.
- [x] T099 Add missing field recovery metadata.
- [x] T100 Add invalid enum recovery metadata.
- [x] T101 Add invalid object field recovery metadata.
- [x] T102 Add unsupported tool recovery metadata.
- [x] T103 Add unsupported resource URI recovery metadata with matching templates where possible.
- [x] T104 Add missing or stale workflow plan recovery metadata.
- [x] T105 Add unsafe side-effect attempt recovery metadata.
- [x] T106 Add secret leakage tests for recoverable errors.

## Phase 11: Documentation

- [x] T107 Update `mcp/README.md` for descriptor-driven model discovery.
- [x] T108 Update `mcp/README_ZH.md` for descriptor-driven model discovery.
- [x] T109 Document YAML descriptor authoring rules for new MCP features.
- [x] T110 Document the normal model path: inspect, read resources, plan, apply, validate.
- [x] T111 Document `execute_query` versus `execute_update`.
- [x] T112 Document prompts and completions as recorded but deferred requirements.
- [x] T113 Add documentation consistency checks or review notes.

## Phase 12: Verification

- [x] T114 Run scoped MCP unit tests:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

- [x] T115 Run scoped Checkstyle:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck checkstyle:check
```

- [x] T116 Run scoped Spotless:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck spotless:check
```

- [x] T117 Run placeholder description search:

```bash
rg -n "ShardingSphere MCP resource:|ShardingSphere MCP resource template:" mcp test/e2e/mcp --glob '!target/**'
```

- [x] T118 Run known object schema search:

```bash
rg -n "additionalProperties\", true|additionalProperties\\(\\)" mcp --glob '!target/**'
```

- [x] T119 Run SQL tool name and descriptor search:

```bash
rg -n "execute_query|execute_update" mcp test/e2e/mcp --glob '!target/**'
```

- [x] T120 Verify descriptor YAML files include ASF license headers.
- [x] T121 Verify no branch switch occurred during implementation.

## Dependencies

- T001-T008 must complete before implementation starts.
- T009-T020 must complete before descriptor YAML validation can be stable.
- T021-T031 must complete before resource and tool handlers depend on descriptor data.
- T032-T048 must complete before resource discovery golden tests can pass.
- T049-T058 must complete before enriched tool discovery can pass.
- T059-T066 should complete before workflow tool outputs are finalized because SQL side-effect semantics affect shared guidance.
- T067-T087 must complete before capability catalog finalization.
- T088-T097 depends on descriptors, resources, tools, and workflow relationships.
- T098-T106 can run after core descriptor and registry changes are available.
- T107-T113 should happen after protocol payload shapes are stable.
- T114-T121 are final verification gates.

## Completion Criteria

- MCP resource discovery exposes business-level descriptions, titles, annotations, variables, and metadata.
- MCP tool discovery exposes model-friendly descriptions, structured input schemas, output schemas, annotations, and relationships.
- `shardingsphere://capabilities` exposes the full generated model-facing catalog.
- SQL execution is split into read-only query and update-capable tools with clear side-effect semantics.
- Workflow responses expose stable next-action guidance.
- Recoverable errors include machine-readable repair hints.
- Prompts and completions are recorded in descriptors but not implemented in phase 1.
- Unit tests, protocol golden tests, Checkstyle, Spotless, and required searches pass or have documented blockers.
- No branch switch occurred.
