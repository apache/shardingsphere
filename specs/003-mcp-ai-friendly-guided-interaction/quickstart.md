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

# Quickstart: AI-Friendly MCP Lightweight Requirements

This quickstart explains how to read and validate the active requirements package.
It is not an implementation script and must not switch branches.

## Prerequisites

- `git branch --show-current` reports the existing branch, `001-shardingsphere-mcp`.
- No `git switch`, `git checkout`, or branch-changing Spec Kit script is run.
- Requirements stay scoped to model-facing clarity, safe continuation, and lightweight regression protection.
- `shardingsphere://capabilities` is treated as the sole current public-surface fact source.
- Legacy compatibility shims, old tool names, old recommendation fields, and implicit execution defaults are not preserved.
- Side-effecting SQL and workflow apply paths remain preview-first and user-approved.
- Live-model usability scenarios remain opt-in and outside default CI.

## Requirement Documents

Use these files as the active requirement set:

- `docs/mcp/ShardingSphere-MCP-AI-Friendly-Requirements.md`
- `specs/003-mcp-ai-friendly-guided-interaction/spec.md`
- `specs/003-mcp-ai-friendly-guided-interaction/requirements.md`
- `specs/003-mcp-ai-friendly-guided-interaction/plan.md`
- `specs/003-mcp-ai-friendly-guided-interaction/research.md`
- `specs/003-mcp-ai-friendly-guided-interaction/data-model.md`
- `specs/003-mcp-ai-friendly-guided-interaction/tasks.md`
- `specs/003-mcp-ai-friendly-guided-interaction/quickstart.md`

## Scenario 1: Confirm Active Scope

1. Read `spec.md`.
2. Confirm P0 focuses on public surface clarity, `next_actions`, search URI hints, output schema accuracy, recovery, and lightweight guards.
3. Confirm P0 remains locked to those six categories.
4. Confirm P1 and P2 are sequenced after P0.
5. Confirm out-of-scope items exclude broad tool matrices, compatibility shims, planner, graph engine, vector search, cross-session memory, RBAC, benchmark systems, and hidden execution shortcuts.

Expected result:

- The requirement set has one clear lightweight scope.

## Scenario 2: Public Surface Clarity

1. Compare README public lists, descriptor identifiers, and `shardingsphere://capabilities`.
2. Check that current resources, tools, prompts, and completion targets are not contradicted by active docs.
3. Check that historical PRD or design docs are labeled when they mention non-current tool matrices.
4. Check that no old tool-name compatibility entry point remains in the active contract.

Expected result:

- A model can discover the current MCP surface without chasing historical tool names.

## Scenario 3: Safe Continuation

1. Inspect representative successful outputs, preview outputs, workflow outputs, and recoverable errors.
2. Confirm `next_actions` is the primary guidance shape.
3. Confirm side-effecting next steps require preview and explicit user approval.
4. Confirm reusable arguments are present only when the server can know them safely.
5. Confirm `recommended_next_tool` and `suggested_next_tool` are removed from the active contract.

Expected result:

- A model can continue without reconstructing SQL, `plan_id`, execution mode, or approval state from prose.

## Scenario 4: Metadata Search Navigation

1. Search for database, schema, table, column, index, view, and sequence metadata.
2. Confirm results include `resource_uri`, `parent_resource_uri`, and `next_resource_uris` only when descriptor-backed derivation is safe.
3. Confirm unsafe derivation returns status and reason instead of guessed URIs.

Expected result:

- A model can move from search results to readable resources without manual URI construction.

## Scenario 5: Schema and Recovery Accuracy

1. Compare output schemas with actual payloads for the seven core tools in `requirements.md`.
2. Check enum casing, required fields, branch shapes, and nested object names.
3. Trigger or review recovery for missing `database`, missing `execution_mode`, wrong SQL tool, unknown public identifier, and stale workflow `plan_id`.
4. Confirm missing `execution_mode` is rejected before recovery recommends `execution_mode=preview`.

Expected result:

- Models can parse outputs and repair common mistakes through structured fields.

## Scenario 6: Lightweight Regression Guards

1. Add descriptor lint only for obvious model-facing drift.
2. Add capabilities shape checks for public sections without locking large snapshots.
3. Add focused unit or contract assertions for touched behavior.

Expected result:

- The public MCP contract is protected without a large golden transcript suite or default live-model dependency.

## Scenario 7: P1 and P2 Triage

1. Take P1 only after P0 is stable.
2. Prefer resource navigation fields, compact examples, deterministic completion, algorithm property templates, and `approved_steps` clarity.
3. Take P2 for startup hints, troubleshooting, pagination wording, and a small opt-in usability set.
4. Re-check that no P1/P2 item introduces a new compatibility shim, planner, graph engine, vector search, memory, RBAC platform, or default-CI live-model suite.

Expected result:

- Later improvements stay useful, small, and independently deliverable.

## Verification Commands

For requirements-only changes:

```bash
git branch --show-current
git diff --check
```

For later implementation, use scoped module checks rather than the full reactor by default:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/bootstrap,mcp/features/encrypt,mcp/features/mask -am -Pcheck -DskipTests checkstyle:check
```
