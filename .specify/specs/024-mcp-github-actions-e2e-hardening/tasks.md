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

# Tasks: MCP LLM Docker E2E Environment Hardening

**Input**: `.specify/specs/024-mcp-github-actions-e2e-hardening/spec.md`, `plan.md`, and `source-map.md`
**Branch Rule**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.
**Current Rule**: Do not implement code before the user gives an explicit implementation command.

## Phase 1 - Speckit Baseline

- [x] T001 Confirm the active branch is `001-shardingsphere-mcp` without switching branches.
- [x] T002 Re-read `AGENTS.md` supplied in the thread and `CODE_OF_CONDUCT.md`.
- [x] T003 Re-scope existing `.specify/specs/024-mcp-github-actions-e2e-hardening` to the accepted five LLM Docker E2E environment issues.
- [x] T004 Update `specs/014-mcp-github-actions-e2e-hardening/requirements.md` as the lightweight requirement mirror.
- [x] T005 Record destructive Docker cleanup as confirmation-gated.
- [x] T005A Add source-driven reanalysis for GitHub path filters, required checks, Docker Buildx cache, and Docker prune semantics.
- [x] T005B Add doubt-driven reanalysis findings for broad path triggers, cache export failure risk, and helper dry-run verification.
- [x] T005C Add mcp-builder review notes for MCP E2E score-evidence preservation.
- [x] T005D Add reanalysis for GitHub changed-file path-filter limits and Buildx single-platform local loading.
- [x] T005E Add reanalysis for Docker GHA cache API v2 compatibility and Buildx diagnostics.
- [x] T005F Clarify the dedicated LLM workflow files as the only non-module path exception for PR triggers.

## Phase 2 - Workflow Trigger Hardening

- [ ] T006 Keep `pull_request` triggers in `.github/workflows/mcp-llm-e2e.yml`, scoped to MCP-related paths plus the dedicated workflow file itself.
- [ ] T007 Keep `pull_request` triggers in `.github/workflows/mcp-llm-usability-e2e.yml`, scoped to MCP-related paths plus the dedicated workflow file itself.
- [ ] T008 Preserve `workflow_dispatch` and weekday `schedule` in both workflows.
- [ ] T009 Document that LLM workflows are non-required merge checks and should not hide failures with success-only reporting.
- [ ] T010 Verify `rg -n "pull_request|paths:" .github/workflows/mcp-llm*.yml` shows MCP-scoped PR filters.
- [ ] T010A Remove root and aggregator-only path triggers such as `pom.xml`, `distribution/pom.xml`, `test/e2e/pom.xml`, and Speckit-only paths.
- [ ] T010B Verify the LLM workflows do not contain broad root, aggregator, or Speckit-only path triggers.
- [ ] T010C Document `workflow_dispatch` as the fallback for unusually large PRs affected by GitHub path-filter changed-file limits.

## Phase 3 - Docker Build Fail-Fast And Cache

- [ ] T011 Add Docker preflight before Maven install in `.github/workflows/mcp-llm-e2e.yml`.
- [ ] T012 Add Docker preflight before Maven install in `.github/workflows/mcp-llm-usability-e2e.yml`.
- [ ] T013 Move LLM runtime image build before Maven install in both workflows.
- [ ] T014 Add Docker Buildx/GHA cache to the smoke workflow image build.
- [ ] T015 Add Docker Buildx/GHA cache to the usability workflow image build.
- [ ] T016 Ensure cached builds still load `apache/shardingsphere-mcp-llm-runtime:local` into Docker for Testcontainers.
- [ ] T016A Use explicit Buildx `context`, `file`, `build-args`, `tags`, and `load` inputs equivalent to the current Docker CLI build.
- [ ] T016B Configure cache export so cache-service failures do not fail a successful image build.
- [ ] T016C Reuse existing repository Docker action major versions unless a separate dependency-update decision is made.
- [ ] T016D Do not configure multi-platform output for the locally loaded LLM runtime image.
- [ ] T016E Print `docker buildx version` after setup for GitHub Cache service API v2 diagnostics.

## Phase 4 - Local Architecture-Aware Build Helper

- [ ] T017 Add `test/e2e/mcp/src/test/resources/docker/llm-runtime/build-local.sh`.
- [ ] T018 Map `x86_64` and `amd64` to the pinned linux/amd64 base digest.
- [ ] T019 Map `arm64` and `aarch64` to the pinned linux/arm64 base digest.
- [ ] T020 Fail unsupported architectures before Docker build.
- [ ] T021 Keep the default output image tag as `apache/shardingsphere-mcp-llm-runtime:local`.
- [ ] T021A Add a dry-run or print mode so helper verification can run without downloading the model.
- [ ] T021B Keep the helper POSIX `sh` compatible and include the ASF license header.
- [ ] T021C Align README invocation with file mode: direct executable invocation or explicit `sh`.

## Phase 5 - Documentation And Cleanup Guidance

- [ ] T022 Update `mcp/README.md` with the local build helper and MCP-scoped PR plus manual plus scheduled workflow policy.
- [ ] T023 Update `mcp/README_ZH.md` with the same guidance.
- [ ] T024 Add safe Docker cleanup guidance: inspect first, prune dangling images/build cache only by default.
- [ ] T025 Document Docker volume pruning as higher risk and excluded unless explicitly confirmed.

## Phase 6 - Verification And Review

- [ ] T026 Run focused LLM configuration/runtime tests.
- [ ] T027 Run scoped Checkstyle/Spotless for touched MCP/E2E modules.
- [ ] T028 Run helper syntax validation and dry-run or print-mode validation appropriate for the current machine.
- [ ] T028A Verify helper execution style matches README instructions.
- [ ] T029 Run `docker system df` before any cleanup decision.
- [ ] T030 Use `mcp-builder` to review MCP E2E design and implementation reasonableness.
- [ ] T031 Run final code review and report commands with exit codes.

## Cleanup Execution Gate

- [ ] T032 Ask for explicit confirmation before pruning Docker resources.
- [ ] T033 If confirmed, prune only agreed resource classes and report before/after `docker system df`.
