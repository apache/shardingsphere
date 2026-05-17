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

# Feature Specification: MCP LLM Docker E2E Environment Hardening

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-17
**Status**: Draft
**Input**: User accepted fixing all five remaining Docker LLM E2E environment issues and requested a new Speckit spec without switching branches.

## Branch Constraint

- Work must stay on the existing `001-shardingsphere-mcp` branch.
- Branch creation, branch switching, and branch-changing Speckit commands are forbidden.
- This Speckit package is documentation-only; implementation requires the next explicit coding command.
- Existing unrelated worktree changes must not be reverted, reformatted, staged, or committed by this package.

## Goal

Harden the MCP LLM E2E Docker environment so that score-closing LLM validation remains reproducible, cheaper to run, and safer to operate locally and in GitHub Actions.

The five accepted fixes are:

1. Align LLM workflow triggers with the MCP-scoped, non-required PR lane policy.
2. Provide a local architecture-aware LLM runtime image build entry point.
3. Add Docker build cache for the server-plus-model image in GitHub Actions.
4. Move Docker image build and Docker preflight before Maven dependency/test work so infrastructure failures fail fast.
5. Add a safe Docker disk cleanup path with explicit confirmation for destructive cleanup.

## User Scenarios & Testing

### User Story 1 - Maintainers run LLM validation for MCP PRs without blocking merge (Priority: P1)

As an MCP maintainer, I want LLM E2E workflows to run for PRs that touch MCP-related paths, while remaining a non-required check so an unfinished LLM lane does not block merge.

**Independent Test**: Inspect `.github/workflows/mcp-llm-e2e.yml` and `.github/workflows/mcp-llm-usability-e2e.yml`; both workflows contain MCP-scoped `pull_request` path filters, both retain `workflow_dispatch` and `schedule`, and documentation says these checks must not be configured as required merge checks.

**Acceptance Scenarios**:

1. Given a PR touches MCP-related paths, when workflow triggers are evaluated, then the dedicated LLM workflows are started by `pull_request`.
2. Given a PR does not touch MCP-related paths, when workflow triggers are evaluated, then the dedicated LLM workflows are not started.
3. Given an LLM workflow is still running, when maintainers evaluate merge readiness, then the workflow is visible as a non-required signal and does not block merge by itself.
4. Given a maintainer wants score evidence outside PR timing, when `workflow_dispatch` is used, then the LLM workflow can still be run manually.
5. Given nightly evidence is needed, when the weekday schedule fires, then the LLM workflow still runs.
6. Given README documents the LLM lane, when reviewers read it, then it says MCP-scoped PR plus manual plus scheduled, non-required for merge.
7. Given only root or aggregator files such as `pom.xml`, `distribution/pom.xml`, `test/e2e/pom.xml`, or Speckit documents change, when workflow triggers are evaluated, then the dedicated LLM workflows are not started unless an MCP module path or the dedicated LLM workflow infrastructure changed.
8. Given a very large PR exceeds GitHub's path-filter changed-file limit, when maintainers need LLM evidence, then the README directs them to use `workflow_dispatch` as the fallback.

### User Story 2 - Developers build the LLM runtime image on different local CPU architectures (Priority: P1)

As a developer on either amd64 or Apple Silicon, I want one local build entry point that selects the right pinned `llama.cpp` base image digest, so I do not accidentally pull the wrong platform or copy CI-only commands.

**Independent Test**: Run the local build helper on the current machine and verify it chooses the expected base digest and tags `apache/shardingsphere-mcp-llm-runtime:local`.

**Acceptance Scenarios**:

1. Given `uname -m` returns `x86_64` or `amd64`, when the helper builds the image, then it uses the pinned linux/amd64 base digest.
2. Given `uname -m` returns `arm64` or `aarch64`, when the helper builds the image, then it uses the pinned linux/arm64 base digest.
3. Given an unsupported architecture is detected, when the helper starts, then it fails before Docker build with an actionable message.
4. Given the image is built, when Maven LLM E2E starts, then it uses the local prepackaged image and does not download the model during Maven execution.
5. Given a developer only wants to validate architecture selection, when the helper runs in dry-run or print mode, then it reports the selected digest without starting Docker build.

### User Story 3 - GitHub Actions avoids repeated large model downloads when possible (Priority: P1)

As a CI maintainer, I want Docker build cache around the LLM runtime image so repeated runs do not always download the 1.28GB GGUF model layer from scratch.

**Independent Test**: Inspect the workflow and verify Docker Buildx/GHA cache is configured for the LLM runtime image build.

**Acceptance Scenarios**:

1. Given a cold GitHub runner, when the LLM runtime image builds, then the workflow still downloads and verifies the pinned GGUF file through Docker build.
2. Given a warm cache exists, when the same Dockerfile, base digest, model URL, and checksum are reused, then Docker build can restore cached layers.
3. Given the model checksum changes, when Docker build runs, then the cache is invalidated and the checksum-protected layer is rebuilt.
4. Given the local image is needed for Testcontainers, when the build completes, then the image is loaded into the local Docker daemon with the expected tag.
5. Given GitHub Actions cache export is unavailable or throttled, when the image itself builds successfully, then cache export failure does not fail the score run.
6. Given Buildx is used with `load: true`, when the workflow builds the LLM runtime image, then it does not request a multi-platform output.
7. Given the runner has an outdated Buildx/BuildKit stack, when the LLM runtime image build tries to use GHA cache, then the workflow evidence shows the Buildx version so maintainers can identify GitHub Cache API v2 incompatibility.

### User Story 4 - CI fails fast on Docker/model infrastructure problems (Priority: P1)

As a CI maintainer, I want Docker preflight and LLM image build to run before Maven install work, so GHCR, Hugging Face, Docker disk, or Docker daemon problems are reported early.

**Independent Test**: Inspect workflow step order and verify Docker preflight and image build precede `Build MCP E2E Test Dependencies`.

**Acceptance Scenarios**:

1. Given Docker is unavailable, when the workflow starts, then a Docker preflight step fails before Maven dependency installation.
2. Given the base image or model download is unavailable, when the workflow runs, then the Docker image build step fails before Maven installation.
3. Given Docker disk pressure exists, when `docker system df` is printed, then the failure evidence includes disk usage before the expensive steps.
4. Given Docker image build succeeds, when Maven install runs, then later LLM tests can rely on an already built score-closing image.

### User Story 5 - Developers have a safe Docker disk cleanup path (Priority: P2)

As a developer running LLM E2E locally, I want documented low-risk cleanup commands and explicit confirmation boundaries so model/build-cache disk usage can be recovered without deleting useful database volumes accidentally.

**Independent Test**: Review README and Speckit tasks; low-risk dangling image/build-cache cleanup is separated from volume cleanup, and volume cleanup requires explicit confirmation.

**Acceptance Scenarios**:

1. Given Docker disk usage is high, when the developer follows the documented preflight, then `docker system df` is checked before cleanup.
2. Given dangling images or build cache are reclaimable, when the low-risk cleanup is explicitly requested, then only dangling images and build cache are pruned.
3. Given local volumes are reclaimable, when cleanup is considered, then volume pruning is documented as higher risk and is not part of the default cleanup.
4. Given cleanup has run, when evidence is reported, then before/after `docker system df` output is recorded.

## Edge Cases

- Workflow trigger changes must not remove `workflow_dispatch`; maintainers still need manual score evidence.
- PR path filters must not be so broad that non-MCP-only PRs start the LLM workflows.
- Dedicated LLM workflow file changes are the only non-module path exception because they directly change this MCP LLM lane's execution contract.
- Root and aggregator Maven files must not appear as standalone LLM workflow path triggers because GitHub `paths` entries are OR-matched.
- GitHub path-filter changed-file limits can miss unusually large PR matches; manual dispatch remains the documented fallback.
- The workflow must not hide failures with success-only reporting just to avoid merge blocking; the non-blocking behavior belongs to required-check configuration and documentation.
- Build cache must not weaken model integrity. The GGUF download remains pinned by revision and checksum.
- Build cache export failures should not turn a valid LLM image build into an E2E failure; cache is an optimization, not score evidence.
- GHA cache compatibility depends on modern Buildx/BuildKit support for GitHub Cache service API v2; the workflow should print the Buildx version after setup.
- Local architecture selection must not change the score-closing model, model quantization, server command, or artifact metadata contract.
- Local helper validation must not require downloading the GGUF model when only script behavior is being checked.
- Docker build cache must still load the image into the local daemon because Testcontainers starts `apache/shardingsphere-mcp-llm-runtime:local`.
- Buildx `load: true` must stay single-platform for the LLM runtime image build; multi-platform image publishing is outside this score lane.
- Docker cleanup must not prune volumes or running-container resources unless the user explicitly confirms that higher-risk operation.
- Generated directories such as `target/` must not be edited or committed.
- Existing LLM smoke/usability score behavior must not regress.

## Requirements

### Functional Requirements

- **MLD-FR-001**: The work MUST remain on branch `001-shardingsphere-mcp`; branch creation and switching MUST NOT be used.
- **MLD-FR-002**: The implementation MUST preserve the current score-closing model `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`, quantization `Q4_K_M`, model revision, model SHA-256, and Docker-owned runtime evidence.
- **MLD-FR-003**: The dedicated LLM E2E workflows MUST retain `pull_request` triggers scoped to MCP-related paths.
- **MLD-FR-004**: The dedicated LLM E2E workflows MUST NOT run for PRs that do not touch MCP-related paths.
- **MLD-FR-005**: The dedicated LLM E2E workflows MUST retain `workflow_dispatch` and weekday scheduled execution.
- **MLD-FR-006**: `mcp/README.md` and `mcp/README_ZH.md` MUST describe the LLM lane as MCP-scoped PR plus manual plus scheduled, and as a non-required merge check.
- **MLD-FR-007**: The LLM workflows SHOULD still fail visibly when their tests fail; they MUST NOT turn failing LLM evidence into success only to avoid blocking merge.
- **MLD-FR-008**: The LLM workflow path filters SHOULD include MCP module, MCP E2E, MCP distribution, and the dedicated LLM workflow files as a narrow lane-infrastructure exception.
- **MLD-FR-009**: The LLM workflow path filters MUST NOT include root or aggregator-only paths such as `pom.xml`, `distribution/pom.xml`, `test/e2e/pom.xml`, or Speckit-only documentation paths as standalone triggers.
- **MLD-FR-009A**: README guidance SHOULD document `workflow_dispatch` as the fallback when an unusually large PR is affected by GitHub path-filter changed-file limits.
- **MLD-FR-010**: A local LLM runtime image build helper SHOULD be added under the LLM runtime Docker resource path.
- **MLD-FR-011**: The local build helper MUST select the pinned `llama.cpp` base image digest from the current CPU architecture.
- **MLD-FR-012**: The local build helper MUST tag the image as `apache/shardingsphere-mcp-llm-runtime:local` by default.
- **MLD-FR-013**: Unsupported local architectures MUST fail with an actionable message before Docker build starts.
- **MLD-FR-014**: The local build helper SHOULD support a dry-run or print mode that validates architecture selection without starting Docker build.
- **MLD-FR-014A**: The local build helper MUST be POSIX `sh` compatible, include the ASF license header, and either be executable or be documented through `sh <script>`.
- **MLD-FR-015**: GitHub Actions LLM runtime image build MUST use Docker Buildx or an equivalent BuildKit path that supports GHA layer cache.
- **MLD-FR-016**: The cached image build MUST use explicit build context and Dockerfile inputs equivalent to the current `docker build` command.
- **MLD-FR-017**: The cached image build MUST still verify the model through the Dockerfile checksum and pinned revision.
- **MLD-FR-018**: The cached image build MUST load the local image into the GitHub runner Docker daemon before Maven LLM tests.
- **MLD-FR-019**: The cached image build MUST NOT configure multi-platform output when using local image loading for Testcontainers.
- **MLD-FR-020**: Cache export SHOULD ignore cache-export errors or otherwise prevent cache service failures from failing a successful image build.
- **MLD-FR-021**: The workflow SHOULD print `docker buildx version` after Buildx setup so GitHub Cache service API v2 compatibility is visible.
- **MLD-FR-022**: Docker preflight and LLM image build MUST run before Maven dependency installation in the LLM workflows.
- **MLD-FR-023**: Docker preflight SHOULD print Docker version and `docker system df` for actionable failure evidence.
- **MLD-FR-024**: Documentation MUST include a safe cleanup flow that checks `docker system df` before cleanup.
- **MLD-FR-025**: Low-risk cleanup guidance MUST be limited to dangling images and build cache unless the user explicitly confirms more destructive cleanup.
- **MLD-FR-026**: Volume pruning MUST be documented as higher risk and excluded from default cleanup guidance.
- **MLD-FR-027**: Future implementation MUST preserve LLM artifacts on failure.
- **MLD-FR-028**: Future implementation MUST run `mcp-builder` review if MCP or MCP E2E files are changed.
- **MLD-FR-029**: Future implementation MUST report verification commands and exit codes.

### Key Entities

- **LLM Runtime Image**: The locally tagged Docker image containing `llama.cpp` server and the pinned Qwen3 GGUF model.
- **Local Build Helper**: A script that chooses the base image digest by architecture and builds the local score-closing image.
- **LLM Workflow Trigger Policy**: The rule that the dedicated LLM workflows run for MCP-scoped PRs, manual dispatch, and schedule, while remaining non-required for merge.
- **Docker Build Cache**: GitHub Actions BuildKit cache used to avoid repeated base/model layer downloads when inputs are unchanged.
- **Docker Cleanup Path**: A documented and confirmation-aware procedure for reclaiming dangling image and build cache disk usage.

## Scope

### In Scope

- `.github/workflows/mcp-llm-e2e.yml`
- `.github/workflows/mcp-llm-usability-e2e.yml`
- LLM runtime Docker resource documentation and helper script.
- `mcp/README.md` and `mcp/README_ZH.md`.
- Speckit documentation for requirements, plan, tasks, and source map.
- Verification of MCP-scoped PR trigger filters, helper behavior, workflow cache inputs, style checks, and focused LLM configuration/runtime tests.

### Out of Scope

- Changing the selected LLM model or quantization.
- Replacing `llama.cpp` with another serving runtime.
- Publishing a prebuilt LLM runtime image to a registry.
- Configuring LLM workflows as required merge checks.
- Pruning Docker volumes without explicit user confirmation.
- Editing generated `target/` content.
- Committing, pushing, staging, or switching branches.

## Success Criteria

- **MLD-SC-001**: Both LLM workflows retain `pull_request` triggers with MCP-scoped path filters and are documented as non-required merge checks.
- **MLD-SC-002**: The local build helper selects the correct digest for the host architecture and builds `apache/shardingsphere-mcp-llm-runtime:local`.
- **MLD-SC-003**: The local build helper can dry-run or print the selected digest without building the image.
- **MLD-SC-004**: The LLM workflows use BuildKit/GHA cache with explicit context/file inputs and still load the local image for Testcontainers.
- **MLD-SC-005**: Docker preflight and image build appear before Maven install in both LLM workflows.
- **MLD-SC-006**: README files document MCP-scoped PR plus manual plus scheduled LLM execution, local build helper usage, and safe Docker cleanup boundaries.
- **MLD-SC-007**: Focused LLM configuration/runtime tests pass after implementation.
- **MLD-SC-008**: Scoped Checkstyle/Spotless for touched MCP/E2E modules passes after implementation.
- **MLD-SC-009**: No unrelated existing worktree changes are reverted or reformatted.

## Assumptions

- GitHub-hosted Ubuntu LLM runners are linux/amd64.
- Local Apple Silicon developers need the linux/arm64 base digest to avoid unnecessary emulation or wrong-platform pulls.
- GHA cache improves repeated runs but cannot guarantee zero download on cache misses.
- Docker cleanup execution remains a separate confirmation-gated action because pruning deletes local Docker resources.
