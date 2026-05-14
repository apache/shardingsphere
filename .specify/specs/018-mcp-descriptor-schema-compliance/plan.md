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

# Implementation Plan: MCP Descriptor Schema Compliance

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-14 | **Spec**: [spec.md](./spec.md)
**Input**: Follow-up package for non-annotation MCP descriptor field compliance.
**Note**: This plan is maintained manually because branch-changing Speckit commands are forbidden for this worktree.

## Summary

Align SDK-supported non-annotation MCP descriptor fields with MCP Specification `2025-11-25` while keeping ShardingSphere descriptor-only metadata behind explicit boundaries.
The implementation must not reopen package 017 and must not change behavior for descriptors that omit the newly supported fields.

## Technical Context

**Language/Version**: Java 21 for MCP modules.
**Primary Dependencies**: MCP Java SDK `1.1.2`, SnakeYAML descriptor loading, Jackson-based SDK serialization.
**Storage**: No storage changes.
**Testing**: JUnit 5 and scoped Maven tests.
**Target Platform**: ShardingSphere MCP runtime over STDIO and Streamable HTTP.
**Project Type**: Java backend MCP server modules.
**Performance Goals**: Descriptor validation remains startup-time only and must not affect request hot paths.
**Constraints**: No branch switch, no generated `target/` edits, no compatibility shims unless explicitly requested.
**Scale/Scope**: Descriptor API models, YAML DTOs, raw validation, catalog validation, catalog payload output, SDK factories, and README guidance for official non-annotation fields.

## Constitution Check

- **Source-backed contract**: MCP `2025-11-25` schema pages define the target fields.
- **Boundary clarity**: API models own official protocol fields, support owns descriptor loading and validation, bootstrap owns SDK conversion.
- **Minimal safe automation**: No new runtime automation or data migration is introduced.
- **Deterministic naming and transparent changes**: New fields and stricter validation are recorded in spec and tasks before implementation.
- **Complete verification before completion**: Tasks require scoped tests, Checkstyle, Spotless, static searches, and diff checks.

## API and Interface Boundary

- `mcp/api` owns the stable descriptor contract exposed inside ShardingSphere MCP modules.
- `mcp/support` owns developer-authored YAML parsing, raw-key validation, semantic validation, and descriptor catalog payloads.
- `mcp/bootstrap` owns conversion into MCP Java SDK schema objects.
- ShardingSphere extension/runtime metadata remains separate from official MCP fields and must not be encoded under `annotations`.

## Project Structure

### Documentation

```text
.specify/specs/018-mcp-descriptor-schema-compliance/
|-- spec.md
|-- plan.md
|-- tasks.md
|-- field-inventory.md
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
```

## Adjacent Package Ownership

- Package 017 owns `Annotations` and `ToolAnnotations` only.
- Package 018 owns official non-annotation descriptor fields such as resource `size`, `icons`, tool `execution`, and `_meta` boundary cleanup.
- MCP Java SDK `1.1.2` supports fixed `Resource.size` but does not expose `icons` on `Resource`, `ResourceTemplate`, or `Tool`, and does not expose `Tool.execution`.
- Existing 013 through 016 packages own broader protocol field standardization and E2E gap triage. If this package discovers output-schema or transport drift, cross-link instead of expanding scope.

## Implementation Strategy

1. Reconfirm MCP `2025-11-25` source facts for selected descriptor fields and MCP Java SDK support.
2. Inventory current YAML validator rejections and current SDK/payload omissions.
3. Implement fixed `Resource.size`, because it is supported by both MCP schema and MCP Java SDK `1.1.2`.
4. Record `icons` and `Tool.execution` as deferred SDK limitations.
5. Add raw and semantic validation at the YAML descriptor boundary.
6. Update payload and SDK mapping to emit `Resource.size` through official surfaces.
7. Keep descriptors that omit `size` behaviorally unchanged.
8. Add focused tests for valid, omitted, and invalid `size` cases.

## Complexity Tracking

- **SDK surface may lag the protocol**: If MCP Java SDK `1.1.2` lacks a field, document the limitation and avoid fake protocol output.
- **`meta` versus `_meta` naming ambiguity**: Treat this as a boundary decision, not a string replacement. Existing descriptor metadata behavior must be protected by tests before any rename.
- **Icon validation scope**: Icon shape validation is deferred until the SDK exposes official icon fields. Security validation for icon URI schemes may be added only if it stays small and source-backed.
- **Non-annotation behavior must not leak into package 017**: Keep tasks, tests, and docs isolated under this package.
