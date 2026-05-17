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

# Research: MCP LLM Docker E2E Environment Hardening

## Official Sources

- GitHub Actions workflow syntax: `https://docs.github.com/en/actions/reference/workflows-and-actions/workflow-syntax`
- GitHub status checks: `https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/collaborating-on-repositories-with-code-quality-features/about-status-checks`
- Docker Build GitHub Actions cache: `https://docs.docker.com/build/cache/backends/gha/`
- Docker build-push-action inputs: `https://github.com/docker/build-push-action`
- Docker system prune: `https://docs.docker.com/reference/cli/docker/system/prune/`

## Source-Driven Findings

### GitHub Actions Path Filters

GitHub documents that when `branches` and `paths` are both defined, both filters must match.
Within `paths`, at least one changed path matching the include patterns can trigger the workflow.
GitHub also documents path-filter diff limits: if more than 1,000 commits are pushed or diff generation times out, the workflow always runs; changed-file evaluation is limited to 300 files, and a match outside the first 300 files can be missed.

Implication for this spec:

- Keep `pull_request` for the LLM workflows.
- Use MCP-scoped paths plus the dedicated LLM workflow file itself as the only lane-infrastructure exception.
- Do not keep broad root or aggregator paths such as `pom.xml`, `distribution/pom.xml`, or `test/e2e/pom.xml`, because those would start LLM workflows even when no MCP module path changed.
- Do not keep Speckit-only paths as LLM triggers if the user requirement is "no MCP module change, no LLM workflow".
- Document manual dispatch as the fallback for unusually large PRs where GitHub's changed-file limit misses an MCP path.

### Required Checks And Skipped Workflows

GitHub documents that a path-filtered workflow can remain pending if skipped and required.
GitHub status-check documentation also says required checks must pass before protected-branch merge.

Implication for this spec:

- The workflow should fail honestly when LLM tests fail.
- The non-blocking merge behavior must be handled by keeping the LLM workflow out of required branch protection or ruleset checks.
- Do not use `continue-on-error` as the mechanism for non-blocking merge; that would weaken the quality signal and make artifacts misleading.

### Docker Buildx Cache

Docker documents the `gha` cache backend for GitHub Actions workflows and notes it needs a non-default Buildx builder.
The Docker build-push-action exposes `context`, `file`, `cache-from`, `cache-to`, and `load`.
The `load` input maps to Docker output and is required here because Testcontainers starts the locally tagged image.
The repository already uses `docker/setup-buildx-action@v3` and `docker/build-push-action@v6` in `.github/workflows/mcp-build.yml`.
Docker documents `--load` as loading the single-platform build result to `docker images`; the default Docker image store does not support loading multi-platform images.
Docker documents that, as of April 15, 2025, only GitHub Cache service API v2 is supported.
For GHA cache compatibility, Docker lists minimum versions: Docker Buildx `v0.21.0`, BuildKit `v0.20.0`, Docker Compose `v2.33.1`, and Docker Engine `v28.0.0` when using the Docker driver with containerd image store enabled.
Docker also notes that GitHub-hosted runners using `docker/build-push-action` are already up to date, while self-hosted runners may need explicit updates.

Implication for this spec:

- Add `docker/setup-buildx-action`.
- Use `docker/build-push-action` with explicit `context` and `file`.
- Set `load: true`.
- Do not configure a multi-platform `platforms` list for this LLM runtime image build. The score-closing image must be a local single-platform image loaded for Testcontainers, not a pushed manifest list.
- Use `cache-from: type=gha` and `cache-to: type=gha,mode=max,scope=mcp-llm-runtime,ignore-error=true`.
- Add a lightweight Buildx version diagnostic after setup so cache API v2 compatibility is visible in failure evidence.
- Treat cache import/export as best-effort on fork PRs or permission-limited runs; image build and checksum verification remain the score evidence.
- Keep checksum verification in the Dockerfile; cache is an optimization, not the integrity boundary.
- Follow the repository's existing Docker action major versions unless a separate dependency-update decision is made.

### Docker Cleanup

Docker documents that `docker system prune` removes unused containers, networks, dangling images, and build cache by default, and volumes only when the `--volumes` option is used.

Implication for this spec:

- Local cleanup must be confirmation-gated.
- Default guidance should prefer narrower commands for dangling images and build cache.
- Volume pruning must remain excluded unless explicitly confirmed.

## Reanalysis Decisions

- Update implementation plan to remove broad root, aggregator, and Speckit-only path triggers.
- Require explicit Buildx `context`, `file`, `load`, and cache inputs.
- Require cache export failure to be non-fatal.
- Require implementation to follow existing Docker action major versions rather than opportunistically upgrading them.
- Require local helper dry-run or print mode for low-cost verification.
- Document GitHub path-filter changed-file limits and manual dispatch fallback.
- Require the LLM runtime image build to stay single-platform when using `load: true`.
- Record Docker GHA cache API v2 compatibility and Buildx diagnostics as implementation requirements.
- Require local helper invocation style to match file mode and keep the helper POSIX `sh` compatible.
- Clarify that dedicated LLM workflow file changes are allowed triggers because they change the MCP LLM lane itself; all other non-module-only changes remain excluded.
