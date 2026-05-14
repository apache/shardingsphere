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

# Implementation Plan: MCP Annotations Protocol Compliance

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-14 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `.specify/specs/017-mcp-annotations-protocol-compliance/spec.md`
**Note**: This plan is maintained manually because branch-changing Speckit commands are forbidden for this package.

## Summary

Align MCP resource and tool annotation models with MCP Specification `2025-11-25`.
The implementation must keep resource annotations and tool annotations separate, preserve optional resource priority semantics, use primitive tool booleans with MCP defaults, and add descriptor validation that distinguishes MCP schema rules from ShardingSphere production safety policy.

## Technical Context

**Language/Version**: Java 21 for MCP modules.
**Primary Dependencies**: MCP Java SDK `1.1.2`, SnakeYAML descriptor loading, Jackson-based SDK serialization.
**Storage**: No storage changes.
**Testing**: JUnit 5, Mockito where needed, scoped Maven tests.
**Target Platform**: ShardingSphere MCP runtime over STDIO and Streamable HTTP.
**Project Type**: Java backend MCP server modules.
**Performance Goals**: Descriptor validation remains startup-time only and must not affect request hot paths.
**Constraints**: No branch switch, no implementation before requirement acceptance, no generated `target/` edits, no compatibility shims unless explicitly requested.
**Scale/Scope**: Existing MCP descriptor catalog, resource/tool descriptor API, bootstrap resource/tool output factories, and related tests.

## Constitution Check

The package passes the constitution gate:

- **Proxy-first logical abstraction**: Annotation compliance improves MCP descriptors and does not change proxy topology or expose physical database details.
- **Explicit operator control**: Tool annotations are only hints; explicit preview and approval boundaries for side-effecting workflows remain required.
- **Minimal safe automation**: No new automation or data migration is introduced.
- **Deterministic naming and transparent changes**: Descriptor policy and defaults are documented in requirements and tasks.
- **Complete verification before completion**: Tasks require scoped tests, descriptor validation tests, Checkstyle, Spotless, and source-backed protocol checks.

Repository governance check:

- `AGENTS.md` requires planning before non-trivial changes, preserving dirty worktree changes, scoped verification, and no destructive commands.
- `CODE_OF_CONDUCT.md` requires readability, cleanliness, consistency, simplicity, and build/style verification.

## Project Structure

### Documentation

```text
.specify/specs/017-mcp-annotations-protocol-compliance/
|-- spec.md
|-- plan.md
|-- tasks.md
`-- checklists/
    `-- requirements.md
```

### Source Code

```text
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/resource/descriptor/
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/tool/descriptor/
mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/
mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/yaml/
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/resource/
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/
mcp/*/src/test/java/
test/e2e/mcp/src/test/java/
```

**Structure Decision**: Keep the protocol API models in `mcp/api`, descriptor loading and validation in `mcp/support`, and SDK wire conversion in `mcp/bootstrap`.
No new module is needed.

## Adjacent Package Ownership

- Package 013 owns broader protocol field standardization.
- Package 014 owns accepted E2E and output-schema hardening.
- Package 015 owns protocol/domain boundary cleanup.
- Package 017 owns annotation model semantics, annotation validation, primitive/default handling, and annotation output compliance only.

If an implementation task discovers broader descriptor-field or output-schema drift, it should cross-link to 013 or 014 instead of expanding this package.

## Implementation Strategy

1. Lock MCP `2025-11-25` schema facts for `Annotations`, `Role`, `Resource`, `ResourceTemplate`, `Tool`, and `ToolAnnotations`.
2. Update annotation API models so the Java types express MCP semantics without forcing nullable wrapper booleans for tool hints.
   Tool annotation object presence must remain explicit because primitive default values cannot distinguish `EMPTY` from a declared annotation object whose values equal MCP defaults.
3. Preserve resource `priority` presence semantics using an explicit presence flag or equivalent invariant that never emits absent priority as `0.0`.
4. Add raw YAML annotation validation before DTO defaults are applied.
5. Add semantic catalog validation for audience, priority, lastModified, and contradictory tool hints.
6. Update payload and SDK mapping so official MCP output contains only official annotation fields and omits empty annotations.
7. Add focused tests for every rule and run scoped module verification.

## Complexity Tracking

No constitution violation is expected.

- **Primitive tool booleans hide missing YAML keys and annotation object presence**:
  Raw SnakeYAML validation must enforce explicit loaded-descriptor keys before DTO defaults are applied.
  The Java API model or swapper must also preserve annotation-object presence so `MCPToolAnnotations.EMPTY` can omit output while explicitly declared default-valued annotations can still be emitted.
- **Primitive resource priority can lose optional semantics**:
  Implementation must use explicit presence or an equivalent safe invariant.
  Defaulting to `0.0` is forbidden.
- **MCP optional fields versus ShardingSphere policy**:
  Documentation and error messages must identify production-tool explicit hints as ShardingSphere descriptor policy, not MCP protocol requirement.
- **SDK constructors may still accept boxed values**:
  Adapters may box primitive values or pass `null` for absent priority at the MCP SDK boundary.
