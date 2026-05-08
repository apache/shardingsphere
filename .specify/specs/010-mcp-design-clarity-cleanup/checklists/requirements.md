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

- [ ] DCC-FR-001: Current branch is `001-shardingsphere-mcp`; no branch switch or branch creation occurred.
- [ ] No production or E2E behavior change is made before the requirement package is reviewed.
- [ ] No generated `target/` file is modified.
- [ ] Unrelated dirty files are preserved.

## Core Clarity Gate

- [ ] DCC-FR-002: Statement classification is narrowed or split with explicit supported scope.
- [ ] DCC-FR-003: Descriptor loading, swapping, validation, legacy cleanup, and model contract construction are separated.
- [ ] DCC-FR-006: Error recovery no longer depends on exact exception message prefixes.
- [ ] DCC-FR-007: Metadata search responsibilities are independently readable and testable.
- [ ] DCC-FR-008: Completion candidate production is outside bootstrap transport adapters.

## Contract and Transport Gate

- [ ] DCC-FR-004: Bootstrap transport has no hard-coded feature planning tool names or feature-specific workflow argument names.
- [ ] DCC-FR-005: Common public payload contracts use typed models or centralized factories/constants.
- [ ] DCC-FR-009: Feature planning services have simple testable construction paths.
- [ ] DCC-FR-010: Legacy aliases are removed from public model-facing contracts unless an internal-only fallback is justified.
- [ ] DCC-FR-011: Static and constructor mocking in touched tests follows repository guidance or documents an exception.

## E2E Clarity Gate

- [ ] DCC-FR-012: LLM E2E conversation runner responsibilities are split or simplified.
- [ ] DCC-FR-013: Common E2E payload assertions use named helpers instead of repeated nested casts.
- [ ] DCC-FR-014: Broad smoke tests are grouped by product surface.
- [ ] DCC-FR-015: Runtime fixtures separate generic setup from LLM scenario data and wait/retry behavior.

## Compatibility and Simplicity Gate

- [ ] DCC-FR-016: Public behavior changes are intentional, documented, and tested when they improve readability and contract clarity.
- [ ] DCC-FR-017: New abstractions are justified by reduced responsibility mixing, meaningful duplication removal, or public contract discoverability.
- [ ] No DI framework, planner framework, vector/semantic search, live LLM default CI gate, RBAC, or approval-token system is introduced.

## Verification Gate

- [ ] `git diff --check` passes.
- [ ] Scoped Maven tests pass for touched MCP modules.
- [ ] Scoped Checkstyle with `-Pcheck` passes for touched Java modules.
- [ ] Scoped E2E tests pass when `test/e2e/mcp` behavior changes.
- [ ] Final handoff reports commands, exit codes, branch status, and remaining compatibility risks.
