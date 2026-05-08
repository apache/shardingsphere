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

# Requirements Checklist: MCP Design Clarity Cleanup

## Branch and Scope Gate

- [x] DCC-FR-001: Current branch is `001-shardingsphere-mcp`; no branch switch or branch creation occurred.
- [x] No production or E2E behavior change is made before the requirement package is reviewed.
- [x] No generated `target/` file is modified.
- [x] Unrelated dirty files are preserved.

## Core Clarity Gate

- [x] DCC-FR-002: Statement classification is narrowed or split with explicit supported scope.
- [x] DCC-FR-003: Descriptor loading, swapping, validation, legacy rejection, and model contract construction are separated.
- [x] DCC-FR-006: Error recovery no longer depends on exact exception message prefixes.
- [x] DCC-FR-007: Metadata search responsibilities are independently readable and testable.
- [x] DCC-FR-008: Completion candidate production is outside bootstrap transport adapters.

## Contract and Transport Gate

- [x] DCC-FR-004: Bootstrap transport has no hard-coded feature planning tool names or feature-specific workflow argument names.
- [x] DCC-FR-005: Common public payload contracts use typed models or centralized factories/constants.
- [x] DCC-FR-009: Feature planning services have simple testable construction paths.
- [x] DCC-FR-010: Legacy aliases are removed from public model-facing contracts unless an internal-only fallback is justified.
- [x] DCC-FR-011: Static and constructor mocking in touched tests follows repository guidance or documents an exception.
- [x] DCC-FR-019: API descriptor/value classes do not own legacy compatibility parsing, hidden meta cleanup, or transitional alias mapping.
- [x] DCC-FR-020: Common descriptor/value construction uses named factories/helpers instead of ambiguous long positional constructors.
- [x] DCC-FR-021: Public helpers do not hide failure behind ambiguous sentinel values.
- [x] DCC-FR-022: Recovery payloads use canonical action vocabulary at every public level.

## E2E Clarity Gate

- [x] DCC-FR-012: LLM E2E conversation runner responsibilities are split or simplified.
- [x] DCC-FR-013: Common E2E payload assertions use named helpers instead of repeated nested casts.
- [x] DCC-FR-014: Broad smoke tests are grouped by product surface.
- [x] DCC-FR-015: Runtime fixtures separate generic setup from LLM scenario data and wait/retry behavior.

## Compatibility and Simplicity Gate

- [x] DCC-FR-016: Public behavior changes are intentional and traceable through code names, contract tests, and focused assertions.
- [x] DCC-FR-017: New abstractions are justified by reduced responsibility mixing, meaningful duplication removal, or public contract discoverability.
- [x] DCC-FR-018: No unclear production or test code is accepted because JAVADOC, comments, README text, or handoff prose explains it.
- [x] No DI framework, planner framework, vector/semantic search, live LLM default CI gate, RBAC, or approval-token system is introduced.

## 100-Point Score Gate

- [x] Production MCP score reaches 70/70 in `scorecard.md`.
- [x] MCP E2E score reaches 30/30 in `scorecard.md`.
- [x] Design, readability, and convenience gates from `scorecard.md` all pass.
- [x] Final assessment records the whole MCP and MCP E2E scope as 100/100.

## Verification Gate

- [x] `git diff --check` passes.
- [x] Scoped Maven tests pass for touched MCP modules.
- [x] Scoped Checkstyle with `-Pcheck` passes for touched Java modules.
- [x] Scoped E2E tests pass when `test/e2e/mcp` behavior changes.
- [x] Final handoff reports commands, exit codes, branch status, and remaining compatibility risks.
