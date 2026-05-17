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

# MCP LLM Docker E2E Environment Hardening Requirements

Canonical Speckit package: `.specify/specs/024-mcp-github-actions-e2e-hardening/`
Current branch: `001-shardingsphere-mcp`
Branch constraint: do not switch or create branches for this work.

## Goal

Harden the MCP LLM E2E Docker environment so score-closing LLM validation stays Docker-owned, reproducible, cheaper to rerun, and safer to operate locally and in GitHub Actions.

## User Constraints

- Do not switch branches.
- Use Speckit to manage the requirement.
- Do not implement code in this Speckit-only round.
- Preserve unrelated worktree changes.
- Keep `llama.cpp` plus `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
- Treat Docker volume pruning as confirmation-gated.

## Functional Requirements

- Retain `pull_request` triggers in `.github/workflows/mcp-llm-e2e.yml` and `.github/workflows/mcp-llm-usability-e2e.yml`, scoped to MCP-related paths plus the dedicated workflow file itself as the only lane-infrastructure exception.
- Remove root and aggregator-only path triggers such as `pom.xml`, `distribution/pom.xml`, `test/e2e/pom.xml`, and Speckit-only paths from the LLM workflows.
- Retain `workflow_dispatch` and weekday scheduled execution for both dedicated LLM workflows.
- Update `mcp/README.md` and `mcp/README_ZH.md` so the LLM lane is documented as MCP-scoped PR plus manual plus scheduled, and as a non-required merge check.
- Document `workflow_dispatch` as the fallback for unusually large PRs affected by GitHub path-filter changed-file limits.
- Keep LLM workflow failures visible; do not turn failing LLM evidence into success-only reporting merely to avoid blocking merge.
- Add a local LLM runtime image build helper that chooses the pinned `llama.cpp` base digest by CPU architecture.
- Build and tag `apache/shardingsphere-mcp-llm-runtime:local` by default.
- Fail fast on unsupported local architectures before Docker build starts.
- Add dry-run or print-mode validation to the local helper so architecture selection can be checked without downloading the model.
- Keep the helper POSIX `sh` compatible, include the ASF license header, and align README invocation with the committed file mode.
- Add Docker Buildx or equivalent BuildKit/GHA cache to LLM runtime image builds in GitHub Actions.
- Use explicit Buildx `context`, `file`, `build-args`, `tags`, and `load` inputs equivalent to the current Docker CLI build.
- Reuse existing repository Docker action major versions unless a separate dependency-update decision is made.
- Do not configure multi-platform output for the locally loaded LLM runtime image.
- Print `docker buildx version` after setup for GitHub Cache service API v2 diagnostics.
- Treat cache export as an optimization; cache-service export failure should not fail an otherwise valid image build.
- Keep model revision and checksum verification in the Dockerfile as the integrity boundary.
- Ensure cached CI builds still load the local image into the Docker daemon for Testcontainers.
- Run Docker preflight and LLM runtime image build before Maven dependency installation in the LLM workflows.
- Print Docker version and `docker system df` as preflight evidence.
- Document a safe Docker cleanup path that checks `docker system df` before cleanup.
- Limit default cleanup guidance to dangling images and build cache.
- Exclude Docker volume pruning from default cleanup guidance unless explicitly confirmed.
- Preserve LLM artifacts on failure.
- Use `mcp-builder` review when implementation touches MCP or MCP E2E files.
- Report verification commands and exit codes after implementation.

## Verification Requirements

- `rg -n "pull_request|paths:" .github/workflows/mcp-llm*.yml` shows MCP-scoped PR filters.
- Reverse search confirms no broad root, aggregator, or Speckit-only path triggers remain in the LLM workflows.
- Local helper builds or dry-validates the correct digest for the host architecture.
- Workflow YAML shows explicit BuildKit/GHA cache inputs and local image loading.
- Workflow step order shows Docker preflight and image build before Maven install.
- Focused LLM configuration/runtime tests pass.
- Scoped Checkstyle/Spotless for touched modules pass.
- README and README_ZH include local build and cleanup guidance.
