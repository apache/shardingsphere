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

# Quickstart: MCP Protocol Field Standardization

## 1. Stay on the Current Branch

```bash
git status --short --branch
```

Expected branch:

```text
001-shardingsphere-mcp
```

Do not run Speckit scripts that create or switch branches.

## 2. Review the Contract

Read the required descriptor field contract:

```text
.specify/specs/013-mcp-protocol-field-standardization/contracts/mcp-descriptor-fields.md
```

Read the Phase 2 implementation boundary before changing Java source:

```text
.specify/specs/013-mcp-protocol-field-standardization/phase-2-implementation-design.md
```

Use the task list as the implementation entry point:

```text
.specify/specs/013-mcp-protocol-field-standardization/tasks.md
```

Confirm the implementation target:

- MCP schema version is locked to `2025-11-25`.
- MCP Java SDK dependency target is stable `1.1.2`.
- Old descriptor fields are not accepted for compatibility.
- ShardingSphere exposed metadata uses `org.apache.shardingsphere/`.
- Internal source YAML sections use the `internal` prefix.
- Descriptor validation fails during loading before transport mapping.
- Runtime argument validation uses an internal contract derived from `inputSchema`.
- Runtime enforcement covers only the documented compiled subset of `inputSchema`.
- Declared `outputSchema` requires conforming tool `structuredContent`.

## 3. Inspect Current Descriptor Drift

Use source YAML only. Do not edit generated `target/` descriptors.

```bash
rg -n "uriTemplate:|parameters:|resourceKind:|objectScope:|feature:|relatedTools:|relatedResources:|useBefore:|fields:|returnDirect:|templateResource:" mcp -g '*.yaml'
```

Expected before implementation: matches exist in source descriptors.
Expected after implementation: source descriptor YAML uses only standard descriptor fields or `internal*` registry sections.

## 4. Implement by Slice

Recommended order:

1. Upgrade MCP Java SDK dependencies to `1.1.2`.
2. Add adapter feasibility tests for SDK gap fields and wire `_meta` through actual MCP list-response serialization seams.
3. Define strict YAML key validation before typed binding.
4. Define the compiled internal argument contract derived from `inputSchema`.
5. Define `outputSchema` conformance for tool `structuredContent`.
6. Add shared descriptor primitives and metadata key constants.
7. Split fixed resources and resource templates.
8. Move official prompt descriptor DTOs to `mcp/api`.
9. Replace tool `fields` with official `inputSchema`.
10. Internalize prompt template binding and runtime-control metadata.
11. Add descriptor whitelist validation and metadata namespace rules.
12. Implement the proven adapter strategy.
13. Migrate YAML descriptors.
14. Update transport adapter and tests.

## 5. Run Focused Tests

Use targeted test runs during development:

```bash
./mvnw -pl mcp/api -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp/support -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true test
```

Add `-Dtest=ClassName -Dsurefire.failIfNoSpecifiedTests=false` for tighter loops.

## 6. Run Scoped Verification

Before handoff, run scoped module checks:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -Pcheck checkstyle:check
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true test
git diff --check
```

## 7. Handoff Expectations

Report:

- Branch status.
- Changed descriptor models and YAML files.
- Banned fields removed.
- Metadata keys exposed and internalized.
- SDK upgraded to `1.1.2` and adapter gaps covered.
- Commands run with exit codes.
- Remaining risks, if any.
