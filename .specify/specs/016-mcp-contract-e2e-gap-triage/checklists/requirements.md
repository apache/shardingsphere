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

# Specification Quality Checklist: MCP Contract and E2E Gap Triage

**Purpose**: Validate draft completeness before mcp-builder findings become implementation tasks.
**Created**: 2026-05-14
**Feature**: [spec.md](../spec.md)

## Branch and Scope

- [x] Current branch is `001-shardingsphere-mcp`.
- [x] No `git switch`, `git checkout`, branch creation script, or branch-changing Speckit command was run.
- [x] The initial requirements pass was documentation-only; implementation slices are now tracked in `tasks.md` with command evidence.
- [x] Existing unrelated dirty worktree changes are preserved.
- [x] The package records the user's instruction to manage the findings through Speckit.

## Content Quality

- [x] The draft separates standards gaps, functional gaps, implementation-contract gaps, missing tests, and over-testing candidates.
- [x] Hard constraints include no branch switching and no prose-only closure.
- [x] Adjacent Speckit owner packages 012, 013, 014, and 015 are recorded to avoid duplicate implementation.
- [x] A finding ledger records initial owner state for each MCE issue.
- [x] Requirements are written as observable behavior or evidence gates.
- [x] Every gap has been mapped to exact source paths and current line-level evidence.
- [x] Every gap has a final owner package after overlap review with 012, 013, 014, and 015.
- [x] Every P0 gap has a reviewed acceptance test target.

## Requirement Completeness

- [x] Secret-safe elicitation is covered.
- [x] Strict Streamable HTTP negotiation is covered.
- [x] Remote HTTP origin policy is covered.
- [x] Authorization fail-closed behavior is linked to the existing OAuth gate.
- [x] Input-schema and output-schema enforcement are covered.
- [x] Enum casing and resource URI encoding boundaries are covered.
- [x] Initialize lifecycle, completion, session, transaction, and DELETE cleanup evidence are covered.
- [x] `mcp/server.json` registry and publication validation is covered.
- [x] Optional MCP capability scope is covered.
- [x] ShardingSphere feature breadth scope is covered.
- [x] Missing E2E tests are listed.
- [x] E2E reduction, preservation, and downscope candidates are listed.
- [x] The E2E class-by-class disposition matrix has been created.
- [x] Missing tests have been split into exact unit, integration, E2E, and release-gate targets.

## Acceptance and Success Criteria

- [x] User scenarios cover transport/security, tool contracts, E2E realism, test maintainability, and explicit product scope.
- [x] Success criteria require measurable owner mapping and evidence gates.
- [x] Documentation-only creation does not require Maven execution.
- [x] Requirements are ready for task generation after source-path evidence and owner mapping are completed.
- [x] `tasks.md` has been generated with P0/P1/P2 implementation order and scoped verification gates.

## Readiness Decision

- [x] Ready as a starting requirements draft.
- [x] Ready for implementation planning.
- [x] Ready for implementation by task slice.

## Implementation Slice Evidence

- [x] T021/T025 strict HTTP Accept negotiation evidence recorded in `tasks.md`.
- [x] T022/T026 remote HTTP origin allowlist evidence recorded in `tasks.md`.
- [x] T020/T023/T024 secret-safe elicitation evidence recorded in `tasks.md`.
- [x] T040/T041/T042/T043/T044/T045 runtime lifecycle, URI, completion, and session-isolation evidence recorded in `tasks.md`.
- [x] T050/T051 MCP Registry metadata release-gate evidence recorded in `tasks.md`.
