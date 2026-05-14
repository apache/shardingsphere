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

# Implementation Plan: MCP Scorecard Perfect 100

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-11 | **Spec**: `spec.md`
**Input**: Feature specification from `.specify/specs/012-mcp-scorecard-perfect-100/spec.md`
**Note**: Maintained manually because branch-changing Speckit scripts are forbidden by the user request.

## Summary

Convert the latest MCP and MCP E2E score review into a strict all-dimensions-100 requirement package.
The implementation path is scorecard-driven: every gap below 100 becomes a bounded task with an explicit exit gate and evidence requirement.

The 2026-05-13 update reopens the package for source-driven official MCP standard alignment.
Previous 100-point evidence is historical until it proves that MCP production and E2E surfaces follow official MCP protocol semantics without ShardingSphere-specific protocol invention.

## Technical Context

**Language/Version**: Java 21 for MCP subchain; Markdown for Speckit artifacts.
**Primary Dependencies**: ShardingSphere MCP modules, MCP Java SDK `1.1.2`, embedded Tomcat `11.0.18`, Jackson `2.16.1`, H2, optional MySQL/Testcontainers, optional Dockerized Ollama LLM lane.
**Storage**: None for the specification; runtime evidence may use H2, MySQL, artifacts under `target/`, and distribution package outputs.
**Testing**: JUnit 5, Mockito, Hamcrest, module-scoped Maven tests, Checkstyle, Spotless, optional LLM E2E profile.
**Target Platform**: ShardingSphere-Proxy-focused MCP runtime, HTTP and STDIO transports, packaged distribution.
**Project Type**: Java backend/runtime feature plus E2E verification and model-facing contract documentation.
**Performance Goals**: Each performance-related score must have measured or bounded evidence before reaching 100.
**Constraints**: No branch switch; no branch-changing Speckit command; locked dimensions; no average-based completion; no prose-only 100 score; no non-standard MCP protocol behavior.
**Scale/Scope**: MCP production modules plus MCP E2E module and distribution/runtime evidence.

## Source-Driven MCP Standard Gate

Authoritative sources for protocol behavior:

- MCP Specification 2025-11-25 Lifecycle: `https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle`
- MCP Specification 2025-11-25 Transports: `https://modelcontextprotocol.io/specification/2025-11-25/basic/transports`
- MCP Specification 2025-11-25 Authorization: `https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization`
- MCP Specification 2025-11-25 Tools: `https://modelcontextprotocol.io/specification/2025-11-25/server/tools`
- MCP Specification 2025-11-25 Resources: `https://modelcontextprotocol.io/specification/2025-11-25/server/resources`
- MCP Specification 2025-11-25 Prompts: `https://modelcontextprotocol.io/specification/2025-11-25/server/prompts`
- MCP Specification 2025-11-25 Completion: `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion`
- MCP Specification 2025-11-25 Pagination: `https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/pagination`
- MCP Security Best Practices: `https://modelcontextprotocol.io/docs/tutorials/security/security_best_practices`
- MCP Java SDK server documentation: `https://java.sdk.modelcontextprotocol.io/latest/server/`

Version rule:

- Official MCP Specification controls protocol semantics.
- MCP Java SDK documentation controls implementation style only after API compatibility is verified against SDK `1.1.2` or local dependency source.
- Any newer SDK documentation that conflicts with SDK `1.1.2` must be treated as a migration note, not an implementation requirement.

Implementation rules:

- Use standard MCP lifecycle, capability negotiation, JSON-RPC message shapes, stdio, and Streamable HTTP behavior.
- Use official MCP `tools/list`, `tools/call`, `resources/list`, `resources/read`, `resources/templates/list`, `prompts/list`, `prompts/get`, and `completion/complete` request/response semantics.
- Use official `inputSchema`, `outputSchema`, `structuredContent`, text compatibility output, annotations, `isError`, and JSON-RPC error separation.
- Use opaque `cursor` and `nextCursor` for official MCP list pagination.
- Treat `shardingsphere://...` resources and ShardingSphere-specific fields as application resources or structured payload, not protocol extensions.
- For remote HTTP security, satisfy MCP Streamable HTTP `Origin` validation, localhost binding expectations, authentication,
  protected resource metadata, scoped bearer-token validation, and token-passthrough prohibition.
- Add SDK-gap adapters only in bootstrap/integration boundaries, with source citation, tests, and a tracked migration path.

## Constitution Check

*GATE: Must pass before implementation tasks begin. Re-check after design updates.*

- Proxy-first logical abstraction: PASS. Scope remains ShardingSphere-Proxy-focused MCP runtime.
- Explicit operator control: PASS. Safety and approval gates are explicit score dimensions.
- Minimal safe automation: PASS. No migration, backfill, rollback orchestration, or destructive automation is introduced by this specification.
- Deterministic naming and transparent changes: PASS. Any future implementation must preserve deterministic workflow outputs.
- Complete verification before completion: PASS. Every dimension requires evidence before 100 can be claimed.
- Repository-level instructions: PASS. `AGENTS.md` and `CODE_OF_CONDUCT.md` govern implementation and verification.
- Official MCP standard alignment: PASS. `EV-026` through `EV-032` close the 2026-05-13 source mapping, protocol evidence, security, E2E, mcp-builder, and style/test gates.

## Project Structure

### Documentation

```text
.specify/specs/012-mcp-scorecard-perfect-100/
|-- spec.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- scorecard.md
|-- source-driven-mcp-standard-map.md
|-- quickstart.md
|-- tasks.md
`-- checklists/
    `-- requirements.md

specs/008-mcp-scorecard-perfect-100/
`-- requirements.md
```

### Source Code

```text
mcp/api/
mcp/support/
mcp/core/
mcp/features/encrypt/
mcp/features/mask/
mcp/bootstrap/
distribution/mcp/
test/e2e/mcp/
```

**Structure Decision**: The canonical Speckit package lives under `.specify/specs/012-mcp-scorecard-perfect-100/`.
The repo-visible summary lives under `specs/008-mcp-scorecard-perfect-100/`.

## Complexity Tracking

No constitution violation is required for this specification.
Implementation work should reduce complexity rather than add broad abstractions.
Any compatibility adapter for SDK `1.1.2` must be isolated and documented because protocol semantics cannot move into ShardingSphere-specific wrappers.

## Phase 0: Research

Research decisions are recorded in `research.md` and `source-driven-mcp-standard-map.md`.
The main decisions are to create a Speckit package, use all-dimensions-full-score semantics, require evidence for every 100 claim,
and make the official MCP Specification the only source for protocol semantics.

## Phase 1: Design

Design artifacts are:

- `data-model.md`: score dimension, score gap, exit gate, evidence record, and open risk model.
- `scorecard.md`: current scores, target scores, gap reasons, and 100-point exit gates.
- `source-driven-mcp-standard-map.md`: official MCP source map, stack detection, and non-standard behavior rejection criteria.
- `reanalysis.md`: historical evidence, reachability, gap type, and priority analysis.
- `quickstart.md`: how to read, validate, and later close the requirement package.

## Phase 2: Task Planning

`tasks.md` groups implementation work by:

- Score governance and evidence model.
- MCP production module gap closure.
- MCP E2E gap closure.
- Cross-cutting verification and release readiness.
- Source-driven MCP standard alignment, including protocol, SDK, security, and E2E evaluation evidence.

## Verification Strategy

Documentation-only updates for this package require:

- `git status --short --branch`
- `find .specify/specs/012-mcp-scorecard-perfect-100 specs/008-mcp-scorecard-perfect-100 -type f | sort`
- `rg -n "git switch|git checkout|branch-changing|100/100|Target" .specify/specs/012-mcp-scorecard-perfect-100 specs/008-mcp-scorecard-perfect-100`
- `STANDARD_PATTERN="source-driven|official MCP|modelcontextprotocol|non-standard|protocol invention|cursor|nextCursor|structuredContent|outputSchema"`
- `rg -n "$STANDARD_PATTERN" .specify/specs/012-mcp-scorecard-perfect-100 specs/008-mcp-scorecard-perfect-100`

The current implementation slice that changes Java modules has run the relevant scoped Maven tests, Checkstyle, and Spotless gates:

- `EV-029`: golden contract plus production H2 official-discovery E2E tests.
- `EV-031`: scoped Checkstyle and Spotless for touched MCP and E2E modules.
- `EV-032`: focused unit and E2E authorization/discovery/evaluation tests.
