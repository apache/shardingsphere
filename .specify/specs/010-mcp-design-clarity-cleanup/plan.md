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

# Implementation Plan: MCP Design Clarity Cleanup

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-08 | **Spec**: `spec.md`
**Input**: Feature specification from `.specify/specs/010-mcp-design-clarity-cleanup/spec.md`
**Note**: This Speckit package was created manually because branch-switching Spec Kit scripts are forbidden for this task.

## Summary

Clean up `shardingsphere-mcp` and `test/e2e/mcp` by reducing mixed responsibilities, removing transitional compatibility behavior, and centralizing public MCP payload contracts.
The cleanup also simplifies E2E scenario support.
The work must favor clarity over abstraction: small helpers, explicit boundaries, and focused tests are preferred over new frameworks.

## Technical Context

**Language/Version**: Java 21 MCP subchain
**Primary Dependencies**: ShardingSphere MCP API/support/core/features/bootstrap, MCP Java SDK, embedded Tomcat, JUnit 5, Mockito
**Storage**: Not applicable; no schema, registry, or data migration changes are required
**Testing**: Module-scoped Maven tests, descriptor/contract tests, E2E tests where touched, Checkstyle with `-Pcheck`
**Target Platform**: ShardingSphere MCP in STDIO and Streamable HTTP modes; E2E runtime tests under `test/e2e/mcp`
**Project Type**: Java backend module and E2E test harness cleanup
**Performance Goals**: Preserve current runtime behavior; avoid adding allocations or parsing work on hot paths without tests
**Constraints**: Do not switch branches; do not modify generated `target/`; do not add live LLM tests to default CI; document and test intentional public MCP contract changes
**Scale/Scope**: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features`, `mcp/bootstrap`, and `test/e2e/mcp`

## Constitution Check

*GATE: Must pass before implementation and be re-checked before final handoff.*

- Repository instructions from `AGENTS.md` and `CODE_OF_CONDUCT.md` must be followed.
- Keep code readable, consistent, simple, and at one abstraction level.
- Preserve Proxy-first logical abstraction and explicit operator control for workflow surfaces.
- Do not introduce data migration, rollback orchestration, approval-token systems, or broader automation.
- Keep changes traceable and minimal; every refactor must have a readability or contract-safety reason.
- Use `rg`, `./mvnw`, and `apply_patch` as primary tools.
- Do not run `git switch`, `git checkout`, branch creation scripts, destructive git commands, or generated-file edits.
- Java changes must satisfy scoped tests and Checkstyle/Spotless requirements for touched modules.
- Test changes must use public APIs, focused assertions, repository mocking rules, and deterministic fixtures.

## Project Structure

### Documentation

```text
.specify/specs/010-mcp-design-clarity-cleanup/
|-- spec.md
|-- plan.md
|-- research.md
|-- tasks.md
`-- checklists/
    `-- requirements.md

specs/006-mcp-design-clarity-cleanup/
`-- requirements.md
```

### Source Code

```text
mcp/api/src/main/java/
mcp/support/src/main/java/
mcp/core/src/main/java/
mcp/features/encrypt/src/main/java/
mcp/features/mask/src/main/java/
mcp/bootstrap/src/main/java/
mcp/**/src/test/java/
test/e2e/mcp/src/test/java/
```

**Structure Decision**: Keep the full Speckit package under `.specify/specs/010-mcp-design-clarity-cleanup` and publish a concise repo-visible handoff under `specs/006-mcp-design-clarity-cleanup`.

## Phase 0: Requirement Baseline

Deliverables:

- `spec.md` records user stories, functional requirements, success criteria, and non-goals.
- `research.md` records design decisions and rejected over-abstraction.
- `tasks.md` records implementable cleanup tasks grouped by user story.
- `checklists/requirements.md` records the acceptance gate.
- `specs/006-mcp-design-clarity-cleanup/requirements.md` summarizes the final backlog for repo-visible handoff.

Exit criteria:

- Every known finding maps to a requirement or non-goal.
- No branch switch occurred.
- No production or test code was changed during requirement capture.

## Implementation Order

1. Remove bootstrap transport coupling to feature-specific planning tools and workflow argument names.
2. Clean public payload contracts and delete legacy aliases from model-facing output.
3. Split descriptor loading/catalog and structured recovery responsibilities.
4. Narrow or split statement classification and metadata/completion responsibilities.
5. Clean the MCP E2E runner, payload assertion helpers, smoke test grouping, and fixtures.

## Phase 1: Core Responsibility Boundaries

Affected areas:

- `StatementClassifier`
- `MCPDescriptorCatalog`
- `MCPDescriptorCatalogLoader`
- `MCPErrorConverter`
- `SearchMetadataToolService`
- `MCPCompletionSpecificationFactory`

Work items:

- Split or narrow statement classification responsibilities.
- Separate descriptor IO, conversion, validation, legacy cleanup, and model-facing catalog payload construction.
- Replace message-prefix recovery decisions with structured recovery hints.
- Split metadata search by collection, ranking, pagination, and response assembly.
- Move completion candidate production out of bootstrap.

Exit criteria:

- Each touched class has one primary responsibility.
- Focused tests prove behavior is unchanged.
- Public contract changes are intentional, documented, and verified.

## Phase 2: Contract and Transport Cleanup

Affected areas:

- MCP response payload builders.
- Descriptor schemas and descriptor validation.
- Bootstrap transport adapters.
- Feature workflow planning services.

Work items:

- Centralize common payload keys or introduce small typed payload models.
- Remove feature-specific workflow knowledge from bootstrap transport code.
- Add simple injectable construction paths for feature planning services.
- Remove legacy aliases from public contracts and keep internal-only fallbacks only when explicitly justified.

Exit criteria:

- Contributors can find canonical public field names without reading unrelated services.
- Bootstrap code only translates protocol requests and responses.
- Tests avoid normal collaborator replacement through field reflection.

## Phase 3: E2E Clarity Cleanup

Affected areas:

- LLM conversation runner.
- MCP E2E assertion code.
- Production smoke tests.
- Runtime fixture support.
- Wait/retry helpers.

Work items:

- Split or simplify the LLM conversation runner by model loop, tool bridge, final-answer verifier, safety validator, and artifact collector.
- Add named assertion helpers for common MCP payload shapes.
- Split broad smoke tests by product surface.
- Separate generic runtime setup from LLM scenario fixtures.
- Centralize manual wait/retry loops.

Exit criteria:

- E2E scenario tests read as product behavior, not harness plumbing.
- Adding a new scenario does not require duplicating MCP schema construction or raw nested map casts.

## Verification Plan

Recommended commands by change type:

```bash
git branch --show-current
git diff --check
./mvnw -pl mcp -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp -am -Pcheck checkstyle:check
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true test
```

Use narrower `-Dtest=...` commands when a task touches only a small class set. Documentation-only changes require `git diff --check` and branch verification, not Maven.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None planned | The feature exists to reduce complexity | New frameworks and broad rewrites are out of scope |

## Rollback Notes

- Contract centralization can be rolled back by restoring old payload builders while keeping tests as documentation.
- Transport cleanup can be rolled back by reintroducing adapter-local mapping only if the simpler descriptor-driven path cannot express a required behavior.
- E2E harness cleanup can be rolled back per helper class without affecting runtime modules.
- No data migration, registry mutation, or schema change is introduced.
