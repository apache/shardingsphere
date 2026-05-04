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

# Implementation Plan: AI-Friendly MCP Lightweight Requirements

**Branch**: `001-shardingsphere-mcp` (current branch, not changed)
**Date**: 2026-05-04
**Spec**: `specs/003-mcp-ai-friendly-guided-interaction/spec.md`
**Input**: Lightweight requirements from `requirements.md`
**Note**: This plan is maintained manually because branch-changing Spec Kit commands are forbidden for this request.

## Summary

Organize the next MCP usability increment around small model-facing improvements.
The work should make the current MCP surface easier for large models to discover, call, continue, and recover from without introducing
a planner, broad tool matrix, graph engine, vector search, cross-session memory, or default-CI real-model benchmark.

The active requirement source is:

- `specs/003-mcp-ai-friendly-guided-interaction/spec.md`
- `specs/003-mcp-ai-friendly-guided-interaction/requirements.md`
- `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md`

## Technical Context

**Language/Version**: Java 21 MCP subchain.
**Primary Dependencies**: Existing MCP Java SDK integration, descriptor catalog, MCP bootstrap transport, workflow session context, and ShardingSphere MCP resources/tools.
**Storage**: Existing session-scoped workflow state only; no new durable memory.
**Testing**: Documentation-only changes use `git diff --check`.
Later code changes should use module-scoped JUnit 5/Mockito tests, descriptor lint, focused capabilities contract checks,
and opt-in LLM usability tests only when explicitly enabled.
**Target Platform**: ShardingSphere-Proxy MCP runtime over HTTP and STDIO.
**Project Type**: Java backend protocol surface and model-facing metadata.
**Performance Goals**: Completion and navigation changes remain deterministic, in-memory, and bounded by existing pagination or max-result limits.
**Constraints**: No branch switching, no generated-path edits, no hidden execution defaults, no broad planner or benchmark system, and no default-CI dependency on live model services.
**Scale/Scope**: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/bootstrap`, `mcp/features/encrypt`, `mcp/features/mask`,
`mcp/README.md`, `mcp/README_ZH.md`, `docs/mcp`, and focused MCP tests when implementation starts.

## Constitution Check

- **Readability and cleanliness**: Pass. Requirements are grouped by P0/P1/P2 and remove stale broad scope.
- **Simplicity**: Pass. The plan explicitly excludes planner, graph, memory, vector, benchmark, and RBAC systems.
- **Explicit operator control**: Pass. Preview-first and user approval boundaries remain mandatory for side effects.
- **Deterministic verification**: Pass. Default verification uses lightweight deterministic checks; real-model usage remains opt-in.
- **Repository rules**: Pass. This task does not switch branches or edit generated files, and implementation tasks must keep scoped Maven/checkstyle verification.

## Project Structure

### Documentation

```text
specs/003-mcp-ai-friendly-guided-interaction/
|-- spec.md
|-- requirements.md
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
`-- tasks.md

docs/mcp/
`-- ShardingSphere-MCP-AI-Friendly-Requirements.md
```

### Source Code For Later Implementation

```text
mcp/api/
mcp/support/
mcp/core/
mcp/bootstrap/
mcp/features/encrypt/
mcp/features/mask/
mcp/README.md
mcp/README_ZH.md
test/e2e/mcp/
```

**Structure Decision**: Reuse existing MCP modules. Do not introduce a new module for this increment.

## Requirement Slices

### Slice 0: Requirements Alignment

- Keep Spec Kit `spec.md` aligned with the lightweight active requirements.
- Keep Spec Kit `requirements.md` synchronized with the current `docs/mcp` requirement baseline.
- Mark over-designed historical material as trace-only when it remains in the repository.
- Verify documentation diffs with `git diff --check`.

### Slice 1: P0 Public Surface Clarity

- Make server instructions point models to `shardingsphere://capabilities` and resource-first discovery.
- Check README, descriptor identifiers, and capabilities for current public surface consistency.
- Label obsolete tool-matrix material as non-current.
- Add a lightweight capabilities shape check rather than a broad golden transcript suite.

### Slice 2: P0 Next Actions and Safe Continuation

- Standardize `next_actions` as the primary guidance field.
- Keep existing compatibility fields only where already present.
- Ensure preview outputs return reusable arguments for execute/apply steps when safe.
- Preserve user approval requirements for all side-effecting continuation paths.

### Slice 3: P0 Metadata URI Navigation

- Ensure `search_metadata` returns descriptor-backed detail resource URIs when safe.
- Include parent and next-resource hints when derivable.
- Return derivation status and reason instead of guessed URIs when the mapping is uncertain.

### Slice 4: P0 Schema and Recovery Accuracy

- Reconcile core output schemas with actual payloads for search, SQL, workflow planning, apply, and validation.
- Extend structured recovery for missing `database`, missing `execution_mode`, wrong SQL tool, unknown public identifier, and stale workflow `plan_id`.
- Add descriptor lint for obvious model-facing regressions.

### Slice 5: P1 Lightweight Comfort Enhancements

- Add safe `self_uri`, `parent_uri`, `count`, and `next_resources` to resource responses where useful.
- Add compact static examples for complex tool outputs.
- Add deterministic prefix-first plus contains fallback completion behavior.
- Expose encrypt and mask algorithm property templates through algorithm resources.
- Clarify approval arguments such as `approved_steps`.

### Slice 6: P2 First-Use and Opt-In Usability

- Improve HTTP and STDIO startup hints.
- Add first-use client configuration and troubleshooting docs.
- Add a few opt-in LLM usability scenarios for preview-first SQL, metadata search to detail resource, and workflow order.
- Normalize count and pagination wording on large list responses.

## Complexity Tracking

No complexity exception is justified.
Any later proposal for a new planner, graph engine, vector retrieval, cross-session memory, default-CI real-model suite,
benchmark leaderboard, RBAC platform, or hidden execution shortcut must reopen this section with a concrete benefit and scoped alternative analysis.

## Verification Plan

For this requirements-only pass:

```bash
git diff --check
```

For later implementation, prefer narrow module checks, for example:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

If descriptor lint, capabilities contract checks, or opt-in LLM usability tests are added, record the exact scoped command and keep live model services out of default CI.
