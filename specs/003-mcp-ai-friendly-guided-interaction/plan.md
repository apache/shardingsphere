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

# Implementation Plan: AI-Friendly MCP Experience Hardening

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-04 | **Spec**: `specs/003-mcp-ai-friendly-guided-interaction/spec.md`
**Input**: Feature specification from `specs/003-mcp-ai-friendly-guided-interaction/spec.md`
**Note**: Spec Kit structure is maintained manually because branch-changing Spec Kit scripts are forbidden for this request.

## Summary

Harden the already implemented MCP prompt, completion, resource, tool, workflow, and preview surface for large-model use.
The plan now includes the full P0, P1, and P2 model-comfort scope: protocol regression guards, real-model proof, explicit side-effect modes,
clarification, completion diagnostics, descriptor lint, fingerprints, unified next-action metadata, descriptor-owned navigation, prompt stop conditions,
transport contracts, naming, pagination, sampling, progress, logging, roots, examples, and model-confusion tests.

## Technical Context

**Language/Version**: Java 21 MCP subchain.
**Primary Dependencies**: MCP Java SDK, ShardingSphere MCP descriptor catalog, MCP bootstrap transport, workflow session context, E2E MCP interaction framework.
**Storage**: Existing session-scoped workflow state only; no new durable plan storage.
**Testing**: JUnit 5, Mockito, module-scoped Maven tests, deterministic transcript golden tests, descriptor lint tests, transport contract tests,
model-confusion tests, opt-in real-model E2E.
**Target Platform**: ShardingSphere-Proxy MCP runtime.
**Project Type**: Java backend protocol surface and model-facing metadata.
**Performance Goals**: Completion ranking remains in-memory, deterministic, and bounded by existing max result limits.
**Constraints**: No branch switching, no generated-path edits, no default-CI real-model calls, no weakened approval path, no hidden execution defaults.
**Scale/Scope**: MCP API/support/core/bootstrap, encrypt/mask descriptors, README, and `test/e2e/mcp`.

## Constitution Check

- **Proxy-first logical abstraction**: Pass. Transcript, completion, recovery, and navigation operate on logical MCP resources and Proxy-visible metadata.
- **Explicit operator control**: Pass. Real-model E2E, recovery, and `execute_update` validation must preserve preview and approval boundaries for side effects.
- **Minimal safe automation**: Pass. No central planner, graph engine, rollback dry-run, migration, or hidden execution is introduced.
- **Deterministic naming and transparent changes**: Pass. Golden tests and completion ranking are deterministic.
- **Complete verification before completion**: Pass. Requirements include deterministic tests, opt-in real-model E2E, descriptor lint, transport contracts, and navigation validation.
- **Repository rules**: Pass. Implementation must follow `AGENTS.md`, `CODE_OF_CONDUCT.md`, scoped Maven checks, and ASF headers.

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
```

### Source Code

```text
mcp/api/
mcp/support/
mcp/core/
mcp/bootstrap/
mcp/features/encrypt/
mcp/features/mask/
test/e2e/mcp/
mcp/README.md
mcp/README_ZH.md
```

**Structure Decision**: Extend existing MCP modules and E2E framework. Do not introduce a new module for this increment.

## Phase Plan

### Phase 0: Documentation and Baseline Correction

- Update README statements that still describe prompt and completion support as deferred.
- Document current prompt, completion, preview, explicit update execution mode, structured clarification, and opt-in real-model E2E behavior.

### Phase 1: P0 Protocol Golden Guard

- Add normalized capture helpers for public MCP protocol payloads.
- Add small golden fixtures for model-facing protocol surfaces.
- Add tests that fail on missing descriptions, schemas, prompts, completions, preview enums, annotations, safety metadata, fingerprints, and explicit side-effect requirements.

### Phase 2: P0 Opt-In Real-Model E2E

- Extend or reuse MCP conversation scenarios for real-model runs.
- Assert trace shape instead of exact prose.
- Redact credentials and record provider, model, descriptor fingerprint, prompt fingerprint, navigation fingerprint, scenario ID, and failure classification.
- Keep real-model execution outside default CI.

### Phase 3: P0 Explicit Side-Effect and Clarification

- Require explicit `execution_mode` for `execute_update` and recover missing mode to preview.
- Prefer native MCP elicitation for user-only missing input when SDK support exists.
- Add structured fallback fields for pending questions, missing arguments, and ask-user status.
- Add completion diagnostic metadata for ranking source and missing context.

### Phase 4: P1 Descriptor Quality and Capability Fingerprints

- Add descriptor lint for descriptions, schemas, enum values, annotations, safety hints, prompt references, completion targets, navigation, and examples.
- Add deterministic fingerprints for descriptor catalog, prompt set, navigation metadata, and model-facing schema set.
- Record fingerprints in golden transcripts and real-model E2E reports.

### Phase 5: P1 Unified Next Actions and Prompt Stop Conditions

- Standardize next-action fields across tool outputs, resource outputs, prompts, and recovery.
- Add prompt stop conditions and ask-user conditions.
- Validate that suggested arguments contain only known or user-supplied values.

### Phase 6: P1 Descriptor-Owned Navigation and Transport Contracts

- Move navigation ownership into descriptor YAML or equivalent descriptor input.
- Add prompt retrieval, completion, capabilities, navigation, and explicit side-effect mode transport contract tests.
- Validate all navigation endpoints resolve to public resources, prompts, or tools.

### Phase 7: P2 Deterministic Completion Ranking

- Implement exact-prefix, context, plan-recency, and feature-reference ranking.
- Keep returned values directly reusable.
- Avoid cross-session history, embeddings, model calls, and user behavior learning.

### Phase 8: P2 Structured Recovery and Error Taxonomy

- Standardize recovery fields for common recoverable failures.
- Preserve preview and approval for wrong-tool SQL recovery.
- Recommend replanning for unavailable workflow plans.
- Expand categories for parse errors, unsupported statements, multiple statements, missing database, unsafe SQL, invalid enum, unsupported resource, and stale plan.

### Phase 9: P2 Ergonomics, Examples, and Confusion Tests

- Audit tool and resource naming for read-only, preview, and side-effect distinction.
- Normalize pagination fields for large result surfaces.
- Add native or structured progress metadata for long-running workflows when supported.
- Add sampling and logging only where stable SDK support and concrete workflow need exist.
- Add roots or permission-boundary metadata for future file or config resources.
- Require prompt argument coverage by completion, resource, or user-provided-only documentation.
- Add compact output examples and model-confusion tests.

## Complexity Tracking

No constitution violation is currently justified.
Any later proposal for a graph engine, central planner, real-model default CI, cross-session completion memory, semantic ranking, or hidden execution shortcut
must reopen this section with explicit rationale.
