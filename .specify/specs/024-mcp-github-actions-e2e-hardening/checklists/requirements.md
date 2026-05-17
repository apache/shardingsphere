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

# Requirements Checklist: MCP GitHub Actions E2E Hardening

**Purpose**: Validate the Speckit package before any workflow or test implementation starts.
**Created**: 2026-05-17
**Package**: `.specify/specs/024-mcp-github-actions-e2e-hardening/`

## Governance

- [x] Active branch confirmed as `001-shardingsphere-mcp` without switching.
- [x] Package created manually without branch-changing Speckit commands.
- [x] Documentation-only work performed before implementation authorization.
- [x] Existing worktree changes preserved and not reverted.
- [x] Repository rules from `AGENTS.md` and `CODE_OF_CONDUCT.md` considered.
- [x] GitHub Action naming, timeout, unique job, and matrix parallelism requirements captured.

## Requirement Quality

- [x] LLM workflow consolidation requirement keeps both smoke and usability suite identities.
- [x] LLM workflow matrix parallelism risk is captured with `max-parallel: 1`.
- [x] Redundant selector existence shell checks are identified for removal in favor of Maven `failIfNoSpecifiedTests`.
- [x] Distribution/container consolidation requirement preserves step-level failure locality.
- [x] Scoped MCP quality gate requirement covers style/static/license risk.
- [x] Release workflow independence is preserved.
- [x] Published image pull-back smoke is required for release validation.
- [x] Multi-arch manifest inspection is required for `linux/amd64` and `linux/arm64`.
- [x] Arm64 runtime smoke limitation is recorded as residual risk when no runner is available.
- [x] MCP Publisher pinning or integrity-check requirement is captured.
- [x] Container HTTP smoke coverage is specified.
- [x] LLM usability topology expansion is split between PR-critical and scheduled gates.

## Testability

- [x] Future YAML syntax validation is required.
- [x] Future scoped Maven verification commands and exit codes are required.
- [x] LLM artifact preservation is required for both smoke and usability failures.
- [x] Distribution/container artifacts and logs remain visible by named steps.
- [x] Release pull-back smoke must use the pushed tag or digest rather than the local pre-push image.
- [x] Container HTTP smoke has concrete session, tool, resource, metadata, and SQL checks.
- [x] Scheduled LLM usability has concrete H2 HTTP, H2 STDIO core, and MySQL HTTP core targets.

## Confirmed Decisions Before Code

- [x] Do not switch branches.
- [x] Do not edit workflow or test code in this round.
- [x] Consolidate the two LLM workflows unless later evidence shows higher risk than duplication.
- [x] Consolidate distribution and container packaging unless later timing evidence requires separate jobs.
- [x] Keep release publication workflow separate.
- [x] Add coverage through layered PR, schedule, and release gates rather than one overloaded PR gate.
