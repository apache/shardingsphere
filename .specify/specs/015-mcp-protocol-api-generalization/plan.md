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

# Implementation Plan: MCP Protocol API Generalization

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-13 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `.specify/specs/015-mcp-protocol-api-generalization/spec.md`
**Note**: This plan is maintained manually because branch-changing Speckit commands are forbidden for this package.

## Summary

Generalize the MCP protocol layer so official MCP APIs remain protocol-shaped and ShardingSphere-specific guidance remains domain-owned.
The implementation will keep stable MCP `2025-11-25` as the protocol baseline.
It makes discovery standards-first, moves completion and ResourceLink behavior behind descriptor/SPI contracts, and separates protocol errors from business payload errors.

## Technical Context

**Language/Version**: Java 21 for `mcp` modules.
**Primary Dependencies**: MCP Java SDK `1.1.2`, ShardingSphere MCP api/support/core/bootstrap modules, ShardingSphere Proxy runtime metadata.
**Storage**: No persistent storage change expected.
Workflow session state remains existing in-memory/session-scoped behavior unless a later implementation plan says otherwise.
**Testing**: JUnit 5, Mockito, module-scoped Maven tests, descriptor contract tests, bootstrap serialization tests.
**Target Platform**: ShardingSphere-Proxy MCP runtime over STDIO and Streamable HTTP.
**Project Type**: Java backend protocol integration.
**Performance Goals**: Completion and discovery must avoid additional network round trips beyond existing metadata/resource calls.
Default metadata and catalog payload sizes must remain bounded for model context use.
**Constraints**: No branch switching, no compatibility aliases unless separately requested, no draft MCP implementation in this package, no weakening of preview or approval safeguards.
**Scale/Scope**: Current MCP modules plus descriptors under `mcp/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/`; no broad changes to non-MCP modules.

## Constitution Check

*GATE: Must pass before implementation tasks begin. Re-check after design changes that affect workflow behavior.*

- **Proxy-first logical abstraction**: PASS.
  Requirements target MCP surfaces backed by ShardingSphere-Proxy logical metadata.
- **Explicit operator control**: PASS.
  The package preserves preview and explicit approval for side-effecting workflows.
- **Minimal safe automation**: PASS.
  No migration, backfill, rollback orchestration, or audit persistence is added.
- **Deterministic naming and transparent changes**: PASS.
  No generated object naming behavior is changed by this package.
- **Complete verification before completion**: PASS.
  Tasks require contract, descriptor, protocol-error, and module-scoped verification before implementation can close.
- **Repository governance**: PASS.
  `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md` remain binding.

## Source Evidence

Official MCP `2025-11-25` baseline:

- MCP uses JSON-RPC result and error responses: https://modelcontextprotocol.io/specification/2025-11-25/basic
- Initialization negotiates protocol and capabilities: https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle
- Tool descriptors, `structuredContent`, `outputSchema`, ResourceLink, and tool error behavior are defined by the tools spec: https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- Resources and resource contents are defined by the resources spec: https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- Prompt list and prompt get behavior are defined by the prompts spec: https://modelcontextprotocol.io/specification/2025-11-25/server/prompts
- MCP list pagination uses `cursor` and `nextCursor`: https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/pagination

Local findings that drive implementation:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogPayloadBuilder.java` emits custom catalog sections.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPModelFirstContractPayloadBuilder.java` labels custom catalog sections as `protocol_fields`.
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/completion/MCPCompletionService.java` hardcodes completion argument and feature routing.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogValidator.java` hardcodes tool-specific validation branches.
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/MCPResourceController.java` converts resource failures into successful resource payloads.
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/MCPToolController.java` converts unsupported tool exceptions into normal response payloads before transport mapping.
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/protocol/response/MCPItemsResponse.java` uses application pagination fields.
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPTransportPayloadUtils.java` recursively scans payload maps to emit ResourceLinks.
- `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml` contains encryption-oriented mask planner evidence fields.

## Project Structure

### Documentation

```text
.specify/specs/015-mcp-protocol-api-generalization/
|-- spec.md
|-- plan.md
|-- tasks.md
`-- checklists/
    `-- requirements.md
```

### Source Code

```text
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/
mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/
mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/
mcp/features/*/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
mcp/*/src/test/java/
```

**Structure Decision**: Keep protocol API generalization inside existing MCP modules.
`mcp/api` owns stable contracts, `mcp/support` owns descriptors and validation, `mcp/core` owns runtime dispatch, and `mcp/bootstrap` owns SDK/wire adapters.

## Implementation Strategy

1. Freeze the official MCP `2025-11-25` source baseline and local evidence before code changes.
2. Define explicit protocol/domain ownership rules for catalog fields, business payload fields, error channels, pagination, and ResourceLinks.
3. Introduce provider or descriptor-driven dispatch for completion and ResourceLink generation before deleting hardcoded branches.
4. Move tool-specific descriptor validation out of the generic validator and into descriptor schemas, fixtures, or feature-owned validators.
5. Align resource and tool unsupported cases with protocol errors while preserving actionable tool execution errors for supported tool calls.
6. Clean feature planner schemas and prompt names after the protocol boundary is protected by tests.
7. Run scoped MCP module tests and Checkstyle/Spotless gates for touched modules.

## Complexity Tracking

No constitution violation is expected.

- **Protocol error mapping may require SDK integration seams**:
  Current controllers return `MCPResponse` payloads before bootstrap mapping.
  Mitigation is a typed dispatch result or exception path with bootstrap tests.
- **Completion provider extraction may touch core and support modules**:
  Current completion logic directly queries metadata and workflow sessions.
  Mitigation is to start with provider interfaces and migrate existing providers one by one.
- **Capabilities resource may be depended on by models**:
  Current server instructions recommend reading it first.
  Mitigation is to keep the resource, relabel it as catalog guidance, and keep official list methods authoritative.
- **ResourceLink emission may change output ordering**:
  Current recursive scanning implicitly orders links by discovered fields.
  Mitigation is to define explicit provider priority and preserve documented ordering in tests.
