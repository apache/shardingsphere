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

# Requirements: MCP Encrypt/Mask Scorecard 100

## Source

This is the repo-visible handoff for `.specify/specs/019-mcp-encrypt-mask-scorecard-100/`.

Canonical Speckit files:

- `.specify/specs/019-mcp-encrypt-mask-scorecard-100/spec.md`
- `.specify/specs/019-mcp-encrypt-mask-scorecard-100/plan.md`
- `.specify/specs/019-mcp-encrypt-mask-scorecard-100/source-map.md`
- `.specify/specs/019-mcp-encrypt-mask-scorecard-100/scorecard.md`
- `.specify/specs/019-mcp-encrypt-mask-scorecard-100/tasks.md`
- `.specify/specs/019-mcp-encrypt-mask-scorecard-100/checklists/requirements.md`

## Goal

Use Speckit to manage the current task of raising every agreed MCP score dimension to **100/100** without switching branches.

This package supersedes the current planning baseline only. Older Speckit packages remain historical or reusable evidence, but they do not automatically close this checkpoint.

## Branch Constraint

- Current branch remains `001-shardingsphere-mcp`.
- Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Preserve unrelated dirty files in the working tree.

## Scope

- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/features/encrypt`
- `mcp/features/mask`
- `mcp/bootstrap`
- `mcp/server.json`
- `distribution/mcp`
- `test/e2e/mcp`

Functional completeness is limited to encrypt and mask workflows for this checkpoint.

Out of scope unless the user explicitly reopens it:

- Sharding rules
- Readwrite-splitting rules
- Shadow rules
- Traffic governance rules
- Mode governance
- Observability
- Historical data migration
- Backfill
- Rollback orchestration
- Audit persistence

## Current Baseline

- Overall: **84/100**
- `shardingsphere-mcp`: **86/100**
- `test/e2e/mcp`: **80/100**

Active dimensions:

- MCP protocol conformity: `88/100`
- Encrypt/mask functional completeness: `87/100`
- Workflow safety and correctness: `88/100`
- AI usability and MCP ergonomics: `90/100`
- Architecture cleanliness: `84/100`
- Implementation elegance: `83/100`
- Code cleanliness: `78/100`
- Unit test coverage and quality: `85/100`
- E2E and contract coverage: `81/100`
- Security and risk control: `87/100`
- Documentation and governance: `84/100`
- Operations and distribution maturity: `72/100`

## Markdown Decision

Markdown is **not** required for MCP tool results.

The current target is:

- Use official MCP `structuredContent` for machine-readable tool results.
- Include serialized JSON text fallback for compatibility.
- Use Markdown where it is useful for prompts or human-readable reports.
- Do not reduce the score only because a database gateway tool does not return Markdown.

## Completion Rule

Completion requires:

- Every active dimension is `100/100`.
- Every below-100 baseline gap has a completed task and evidence.
- Every claimed 100 dimension has command, artifact, contract, or official-source evidence.
- Relevant scoped Maven tests pass.
- Checkstyle and Spotless pass for touched Java modules.
- The current git branch remains unchanged.
- Official MCP source mapping is current.
- MCP Java SDK behavior is verified against declared dependency version or local SDK source.
- mcp-builder evaluation contains ten read-only, independent, complex, realistic, verifiable, and stable questions.

## Current Status

- Speckit package `019-mcp-encrypt-mask-scorecard-100` created manually on the current branch.
- Current score baseline, target model, source map, tasks, checklist, and final evidence have been recorded.
- Phase 2 protocol evidence tasks `T010` through `T013` are complete.
- Focused bootstrap test evidence passed with exit code `0`: `30` tests run, `0` failures, `0` errors, `0` skipped.
- Final scoped Maven, Checkstyle, Spotless, E2E, distribution, and LLM verification commands passed and are recorded in
  `.specify/specs/019-mcp-encrypt-mask-scorecard-100/e2e-llm-operations-evidence.md`.
- Production implementation and evidence updates for the encrypt/mask scope are complete under this package.
- All 12 active score dimensions have moved to `100/100` under this package.
