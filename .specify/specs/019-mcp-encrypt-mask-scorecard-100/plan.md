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

# Implementation Plan: MCP Encrypt/Mask Scorecard 100

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-15 | **Spec**: `.specify/specs/019-mcp-encrypt-mask-scorecard-100/spec.md`
**Input**: Feature specification from `.specify/specs/019-mcp-encrypt-mask-scorecard-100/spec.md`
**Note**: This package is manually maintained because branch-changing Speckit commands are forbidden for this task.

## Summary

Drive the agreed MCP production and E2E score dimensions from the current 84/100 reassessment to 100/100 with evidence-backed tasks.
Functional completeness is limited to encrypt and mask workflows.
Markdown is explicitly not a mandatory tool-result format; MCP structured content plus JSON text fallback is the protocol requirement.

## Technical Context

**Language/Version**: Java 21 for the MCP subchain.
**Primary Dependencies**: MCP Java SDK `1.1.2`, embedded Tomcat for `mcp/bootstrap`, ShardingSphere Proxy runtime, JUnit 5, Mockito.
**Storage**: No new persistent storage; workflow context remains runtime/session scoped unless a later task proves otherwise.
**Testing**: Module-scoped Maven unit tests, Checkstyle, Spotless, H2 E2E, MySQL opt-in E2E, STDIO E2E, distribution smoke, and LLM evaluation.
**Target Platform**: ShardingSphere-Proxy MCP runtime over Streamable HTTP and STDIO.
**Project Type**: Java service modules plus E2E harness.
**Performance Goals**: Keep default MCP unit and H2 E2E lanes bounded; record performance budgets before claiming performance/resource-use 100.
**Constraints**: No branch switch, no generated `target/` edits, no destructive git commands, no score 100 without evidence, no Markdown-as-required-tool-output rule.
**Scale/Scope**: Encrypt and mask V1 workflows for single logical database/table/column scenarios, with metadata, algorithms, rules, plan/apply/validate, and recovery paths.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- **Proxy-first logical abstraction**: Pass. Scope remains ShardingSphere-Proxy and logical database workflows.
- **Explicit operator control**: Pass. Every schema/rule-changing path must keep preview and approval gates.
- **Minimal safe automation**: Pass. No data migration, backfill, rollback orchestration, or audit persistence is added by this package.
- **Deterministic naming and transparent changes**: Pass. Encrypt derived column naming remains deterministic and must be covered by existing or new tests.
- **Complete verification before completion**: Pass. Score dimensions were kept below 100 until four-layer validation evidence and relevant E2E lanes became current.
- **Repository rules**: Pass. Implementation tasks satisfy `CODE_OF_CONDUCT.md` readability, cleanliness, consistency, simplicity, abstraction, build, and unit-test standards through scoped evidence.

## Project Structure

### Documentation (this feature)

```text
.specify/specs/019-mcp-encrypt-mask-scorecard-100/
|-- checklists/
|   `-- requirements.md
|-- plan.md
|-- scorecard.md
|-- source-map.md
|-- spec.md
`-- tasks.md

specs/010-mcp-encrypt-mask-scorecard-100/
`-- requirements.md
```

### Source Code (repository root)

```text
mcp/api/
mcp/support/
mcp/core/
mcp/features/encrypt/
mcp/features/mask/
mcp/bootstrap/
mcp/server.json
distribution/mcp/
test/e2e/mcp/
```

**Structure Decision**: Canonical Speckit state lives under `.specify/specs/019-mcp-encrypt-mask-scorecard-100`.
The repo-visible handoff lives under `specs/010-mcp-encrypt-mask-scorecard-100/requirements.md`.
Current root-level specs already stop at `009`.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | Not applicable | Not applicable |
