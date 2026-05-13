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

# Implementation Plan: MCP Protocol Field Standardization

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-12 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `.specify/specs/013-mcp-protocol-field-standardization/spec.md`
**Note**: This plan was created manually because the standard Speckit setup script can switch branches, which the user forbids.

## Summary

Standardize the MCP descriptor and catalog boundary to official MCP `2025-11-25` fields.
The implementation must split fixed resources from resource templates and replace custom tool `fields` with official `inputSchema`.
It must also internalize runtime-control metadata and move exposed ShardingSphere guidance to namespaced `meta`.
Business payload fields remain in scope only when they are incorrectly described as MCP descriptor fields.

The technical approach is a phased contract migration:

1. Upgrade MCP Java SDK dependencies to `1.1.2` and prove the bootstrap adapter strategy against actual MCP list-response serialization seams before relying on SDK gap fields.
2. Define the strict YAML-key validation strategy and the runtime schema-enforcement boundary before descriptor DTO implementation.
3. Introduce canonical descriptor data models, metadata-key constants, and typed internal owners.
4. Update YAML models, swappers, registries, validators, and catalog payload builders to publish only official top-level fields.
5. Adapt bootstrap transport factories to SDK capabilities without lowering the official descriptor contract.
6. Migrate descriptor YAML files and tests to the new canonical shape.
7. Verify descriptor contract output, validator rejection paths, transport wire output, schema conformance, and existing runtime safety behavior.

## Technical Context

**Language/Version**: Java 21 for the MCP subchain; Markdown Speckit artifacts for this plan.
**Primary Dependencies**: ShardingSphere MCP modules, MCP Java SDK `1.1.2`, embedded Tomcat for HTTP transport, Jackson through repository dependency management.
**Storage**: No persistent storage changes. Descriptor YAML files under `META-INF/shardingsphere-mcp/descriptors` are the source data.
**Testing**: JUnit 5, Mockito, module-scoped Maven tests, descriptor loader tests, bootstrap transport tests, and scoped Checkstyle with `-Pcheck`.
**Target Platform**: ShardingSphere-Proxy MCP runtime over STDIO and Streamable HTTP.
**Project Type**: Java service modules with descriptor-driven MCP protocol surface.
**Performance Goals**: Descriptor loading and validation remain startup-time work; no new request-path parsing or reflection is introduced.
**Constraints**: No branch switching, no backward compatibility for old descriptor fields, no business payload refactor, Java/YAML uses `meta`, official wire output uses `_meta`.
Strict YAML loading must reject unknown keys before they can be silently dropped, and runtime argument validation must use a compiled contract derived from `inputSchema`.
**Scale/Scope**: Four main descriptor YAML files plus test descriptors; MCP API/support/core/bootstrap and feature descriptor resources are in scope.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Proxy-first logical abstraction**: PASS. The feature changes protocol descriptor shape and does not change Proxy-first logical metadata behavior.
- **Explicit operator control**: PASS. The plan preserves preview and approval behavior and moves runtime-control metadata toward typed internal configuration.
- **Minimal safe automation**: PASS. No data migration, backfill, rollback orchestration, or side-effect automation is added.
- **Deterministic naming and transparent changes**: PASS. Descriptor field disposition is explicit and old non-standard fields are rejected rather than silently translated.
- **Complete verification before completion**: PASS. The plan requires descriptor contract tests, validator rejection tests, transport adapter tests, and runtime safety regression tests.
- **Repository governance**: PASS. Work remains on `001-shardingsphere-mcp` and follows `AGENTS.md` plus `CODE_OF_CONDUCT.md`.

## Project Structure

### Documentation (this feature)

```text
.specify/specs/013-mcp-protocol-field-standardization/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- tasks.md
|-- quickstart.md
|-- contracts/
|   `-- mcp-descriptor-fields.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

```text
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/common/descriptor/
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/prompt/descriptor/
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/
mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/
mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/
mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/
mcp/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
mcp/*/src/test/java/
```

**Structure Decision**: Keep descriptor domain models in `mcp/api` when they represent public MCP concepts.
Keep catalog loading, validation, prompt template bindings, completion registry, and navigation registry in `mcp/support`.
Keep SDK and wire adaptation in `mcp/bootstrap`.

## Phase 0 Research Decisions

Research outputs are captured in [research.md](./research.md).
Key decisions:

- Use official MCP `2025-11-25` as the fixed field standard.
- Upgrade MCP Java SDK to `1.1.2` and treat remaining SDK gaps as bootstrap adapter concerns.
- Do not use MCP Java SDK `2.0.0-M2` for this feature.
- Split fixed resources and resource templates in the descriptor model.
- Use official `inputSchema` directly instead of the current custom tool field DSL for public descriptor YAML.
- Apply minimal metadata exposure and derive any exposed metadata from typed internal configuration.
- Enforce descriptor contract validation during descriptor loading with fail-fast errors.

## Phase 1 Design Outputs

- [data-model.md](./data-model.md) defines canonical descriptor entities, internal registries, and field disposition rules.
- [contracts/mcp-descriptor-fields.md](./contracts/mcp-descriptor-fields.md) defines allowed top-level fields and banned current fields.
- [phase-2-implementation-design.md](./phase-2-implementation-design.md) defines the pre-code implementation boundary for descriptor DTOs and internal owners.
- [quickstart.md](./quickstart.md) defines implementation and verification flow.

## Implementation Slices

1. **Pre-implementation gates**
   - Upgrade MCP Java SDK dependencies to stable `1.1.2`.
   - Prove the bootstrap adapter strategy with representative `resources/list`, `resources/templates/list`, `tools/list`, and `prompts/list` serialization seams before transport mapping relies on SDK gap fields.
   - Prove Java/YAML `meta` becomes wire `_meta` without exposing Java fields named `_meta`.
   - Define the strict raw YAML key validation strategy before typed binding.
   - Define the supported runtime-enforced `inputSchema` subset and the `outputSchema` conformance strategy.

2. **Descriptor model foundation**
   - Add shared `MCPIcon`, common annotations model, metadata key constants, and tool execution support.
   - Replace single resource descriptor semantics with separate fixed resource and resource template descriptors.
   - Move official prompt descriptor DTOs into `mcp/api` and keep prompt template bindings in `mcp/support`.
   - Keep Prompt rendering, PromptMessage creation, and template loading outside `mcp/api`.

3. **YAML and swapper migration**
   - Split YAML `resources` and `resourceTemplates`.
   - Replace tool `fields` with `inputSchema`.
   - Move prompt `templateResource` to internal prompt-template binding.
   - Rename internal YAML sections to `internalPromptTemplateBindings`, `internalCompletionTargets`, `internalReferenceNavigation`, and `internalToolRuntime`.
   - Keep completion targets and reference navigation as internal registries or ShardingSphere catalog metadata, not official descriptor objects.

4. **Validation and contract enforcement**
   - Add top-level field whitelist validation per MCP object type.
   - Validate raw YAML keys or prove strict POJO binding rejects unknown fields before descriptor publication.
   - Reject old non-standard fields and un-namespaced exposed `meta`.
   - Enforce local tool name policy for length, character set, case-sensitive uniqueness, and nonblank names.
   - Validate minimal metadata exposure rules.

5. **Catalog and capabilities output**
   - Emit official descriptor objects only.
   - Label ShardingSphere catalog sections as extension metadata or business payload guidance.
   - Remove `inputFields`, top-level resource `parameters`, prompt `templateResource`, and tool annotation `returnDirect`.

6. **Bootstrap transport adaptation**
   - Start with adapter feasibility tests that prove the chosen wire strategy through actual MCP list-response serialization seams, not standalone DTO serialization only.
   - Preserve Java/YAML `meta` internally and wire `_meta` externally.
   - Map official descriptor fields supported by SDK `1.1.2` directly.
   - Add local adapter handling for official fields missing in SDK `1.1.2`, including `icons` and tool `execution`.
   - Do not rely on SDK `2.0.0-M2` for this implementation.

7. **Descriptor YAML and tests**
   - Migrate main descriptor YAML files in core, support, encrypt, and mask modules.
   - Update test descriptors and loader/validator/transport tests.
   - Replace runtime `fields` validation with a compiled internal argument contract derived from `inputSchema`.
   - Validate tool `structuredContent` against `outputSchema` when `outputSchema` is declared.
   - Preserve side-effect preview and approval regression tests.

## Verification Plan

- `./mvnw -pl mcp/api -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/support -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -Pcheck checkstyle:check`
- `git diff --check`

Use narrower `-Dtest=...` runs during development, then run the scoped module gates before handoff.

## Complexity Tracking

No constitution violation is currently required.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | Not applicable | Not applicable |

## Post-Design Constitution Check

- **Proxy-first logical abstraction**: PASS. No change to database topology or feature workflow scope.
- **Explicit operator control**: PASS. Safety decisions move away from public `meta` and remain server-side.
- **Minimal safe automation**: PASS. Descriptor cleanup does not introduce execution automation.
- **Deterministic naming and transparent changes**: PASS. Field disposition and validation rules are explicit.
- **Complete verification before completion**: PASS. Verification plan covers descriptor contracts, validation, transport, and safety regressions.
