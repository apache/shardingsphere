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

# Implementation Plan: MCP AI-Native Perfect 100

## Technical Context

- Branch: `001-shardingsphere-mcp`.
- Constraint: do not switch branches.
- Target topology: ShardingSphere MCP in STDIO and Streamable HTTP modes.
- Target consumers: LLM-native MCP clients and operators using read-only diagnostics.
- Quality bar: no placeholders, no speculative scope expansion, deterministic verification.

## Constitutional Checklist

- AGENTS.md and `CODE_OF_CONDUCT.md` must be read before implementation.
- Keep changes scoped to MCP docs, descriptors, runtime contracts, tests, and packaging metadata.
- Use `rg`, `./mvnw`, and `apply_patch` as primary tools.
- Do not edit generated `target/` files.
- Do not run destructive git commands or switch branches.
- Do not add live LLM or production API calls to required verification.
- For Java changes, keep variables near first use, use `final` when retained, and satisfy Checkstyle/Spotless.
- For tests, use scoped methods and deterministic fixtures; default to mocks for external systems.

## Design Strategy

The plan uses a gate model instead of a numeric average. Any missing required gate keeps the score below `100`.

Implementation order:

1. Stabilize public contracts that affect model guessing.
2. Add comfort features that reduce repeated user clarification.
3. Add durable proof so future regressions are visible.

## Phase 0: Requirement Baseline

Deliverables:

- `spec.md`: user-visible feature scope and `100` definition.
- `requirements.md`: canonical requirement inventory.
- `research.md`: design decisions and rejected over-design.
- `data-model.md`: structured contract entities.
- `quickstart.md`: verification and scoring workflow.
- `tasks.md`: implementation backlog.
- `checklists/requirements.md`: acceptance checklist.

Exit criteria:

- The package defines what counts as `100`.
- Non-goals are explicit.
- Every known gap maps to one or more requirements.

## Phase 1: P0 Zero-Guessing Contract

Affected areas:

- MCP descriptor generation.
- Tool/resource response builders.
- Completion and workflow planning responses.
- Metadata list/search/detail responses.
- Structured error and recovery payloads.
- Unit and contract tests.

Work items:

- Add a shared response-mode vocabulary and validate every public surface against it.
- Normalize `next_actions` entries.
- Replace ambiguous large-result behavior with explicit pagination or search continuation.
- Route completion recovery to nearest known resources.
- Add deterministic single-schema auto-fill.
- Normalize not-found, empty-state, and ambiguous responses.
- Add contract tests for all P0 requirements.

Exit criteria:

- Every P0 requirement in `requirements.md` passes.
- A future reviewer cannot identify a known in-scope model-guessing gap.

## Phase 2: P1 Comfortable Native Use

Affected areas:

- Runtime/readiness resources.
- Workflow argument planner.
- Redaction helpers.
- Prompt and descriptor text.
- Local MCP smoke test harness.

Work items:

- Add secret-free readiness and runtime visibility.
- Add argument provenance to workflow plans.
- Standardize redaction categories, markers, and summaries.
- Add compact Chinese data-governance lexicon for planning hints.
- Align terminology across prompts, tools, resources, and docs.
- Add deterministic local MCP client smoke coverage.

Exit criteria:

- Every P1 requirement passes.
- The model can move from discovery to safe read-only action with minimal user clarification.

## Phase 3: P2 Proof and Polish

Affected areas:

- Descriptor linting.
- Distribution metadata.
- Recovery-safe tracing fields.
- Scorecard artifact.

Work items:

- Add optional correlation id support where runtime context already exists.
- Add MCP packaging metadata hints without changing packaging architecture.
- Add descriptor-authoring lint rules.
- Add maintained scorecard that records evidence for every gate.

Exit criteria:

- Every P2 requirement passes.
- The scorecard can justify `100` without relying on a live reviewer.

## Verification Plan

Run the narrowest meaningful checks for each implementation phase:

- Documentation-only edits: whitespace and path inspection.
- Descriptor/runtime changes: scoped MCP module unit tests.
- Contract changes: descriptor lint and contract tests.
- Java style changes: scoped Checkstyle/Spotless for touched modules.
- Packaging changes: package or resource inspection for the MCP distribution path.

Recommended commands after implementation:

```bash
git branch --show-current
git diff --check
./mvnw -pl mcp -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp -am -Pcheck checkstyle:check
```

If the actual MCP module path differs, replace `mcp` with the module that contains the touched MCP bootstrap or contract tests.

## Rollback Notes

The safest rollback is by phase:

- Revert descriptor/response contract changes if MCP clients reject the new shape.
- Disable new readiness or runtime resources if packaging compatibility is affected.
- Keep tests and docs adjusted only when the runtime contract remains active.

No data migration, registry mutation, or schema change is required by this design.

