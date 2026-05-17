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

# Implementation Plan: MCP LLM Docker E2E Environment Hardening

**Branch**: `001-shardingsphere-mcp`
**Date**: 2026-05-17
**Spec**: `.specify/specs/024-mcp-github-actions-e2e-hardening/spec.md`
**Note**: This plan is documentation-only until the user gives an explicit implementation command.

## Summary

Apply five small environment-hardening changes around the Docker-owned LLM E2E lane without changing the LLM model, MCP server behavior, or scorecard semantics.

The implementation should change only workflow/docs/helper surfaces needed for reproducibility and operability:

1. Dedicated LLM workflows run on MCP-scoped PRs, manual dispatch, and schedule, while remaining non-required for merge.
2. A local helper selects the right pinned base digest by CPU architecture.
3. CI image builds use BuildKit/GHA cache.
4. Docker preflight and runtime image build run before Maven install.
5. Docker cleanup guidance is documented with confirmation boundaries.

## Technical Context

**Language/Version**: Java 21 for MCP E2E tests; shell for local Docker helper.
**Primary Dependencies**: Docker, Docker BuildKit/Buildx, Testcontainers, MCP Java SDK `1.1.2`, Maven.
**Model Runtime**: Docker-owned `llama.cpp` server.
**Model**: `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M`.
**Testing**: Focused LLM configuration/runtime unit tests, workflow static checks, scoped Checkstyle/Spotless.
**Constraints**: No branch switching, no model/runtime replacement, no Docker volume pruning without explicit confirmation.

## Design Decisions

- Keep two dedicated LLM workflow files for now. The current request targets trigger/cache/order hardening, not workflow consolidation.
- Keep `pull_request` triggers in both dedicated LLM workflows, but scope them to MCP-related paths only.
- Remove broad root and aggregator-only path triggers from the LLM workflows because GitHub `paths` entries are OR-matched. Root `pom.xml`, `distribution/pom.xml`, `test/e2e/pom.xml`, and Speckit-only paths must not start the LLM lane by themselves.
- Treat the dedicated LLM workflow files themselves as the only non-module path exception, because changing the lane infrastructure should run the lane it changes.
- Document manual dispatch as the fallback for unusually large PRs that hit GitHub's changed-file path-filter limits.
- Treat merge non-blocking behavior as repository required-check configuration plus documentation. Do not make failing LLM evidence look successful in the workflow.
- Use Docker Buildx with `load: true` so Testcontainers can start the locally tagged image.
- Keep the LLM runtime image build single-platform when using `load: true`; do not copy the multi-platform push pattern from release image workflows.
- Use explicit Buildx `context` and `file` values equivalent to the current Docker CLI build command.
- Follow existing repository Docker action major versions, currently `docker/setup-buildx-action@v3` and `docker/build-push-action@v6`, unless a separate dependency-update decision is made.
- Print `docker buildx version` after Buildx setup so GitHub Cache service API v2 compatibility is visible in workflow logs.
- Cache Docker layers through GHA cache while keeping Dockerfile checksum verification as the model integrity boundary.
- Treat cache export as an optimization; cache export failures should not fail an otherwise valid image build.
- Use a helper script instead of asking local users to copy an amd64-only CI build command.
- Give the helper a dry-run or print mode so architecture selection can be validated without downloading the model.
- Keep the helper POSIX `sh` compatible and make the README invocation match the committed file mode: either executable direct invocation or explicit `sh`.
- Put Docker preflight and image build before Maven install for faster failure on Docker daemon, disk, registry, and model-download issues.
- Document cleanup rather than silently running prune commands in implementation. Actual prune remains confirmation-gated.

## Implementation Phases

### Phase 1 - Workflow Trigger And Step Order

1. Keep `pull_request` blocks in `.github/workflows/mcp-llm-e2e.yml`, scoped to MCP-related paths plus the dedicated workflow file itself.
2. Keep `pull_request` blocks in `.github/workflows/mcp-llm-usability-e2e.yml`, scoped to MCP-related paths plus the dedicated workflow file itself.
3. Remove root and aggregator-only path triggers from both LLM workflows.
4. Keep `workflow_dispatch` and weekday `schedule`.
5. Document that these workflows must not be configured as required merge checks.
6. Document `workflow_dispatch` as the large-PR path-filter fallback.
7. Add Docker preflight before Maven dependency install.
8. Move LLM runtime image build before Maven dependency install.

### Phase 2 - Build Cache

1. Add Docker Buildx setup to both LLM workflows.
2. Print `docker buildx version` after setup for cache API v2 diagnostics.
3. Replace or wrap the Docker build step with a BuildKit path that supports GHA cache, following existing repository Docker action major versions.
4. Ensure the resulting image is loaded as `apache/shardingsphere-mcp-llm-runtime:local`.
5. Set explicit `context`, `file`, `build-args`, `tags`, and `load` inputs.
6. Do not set a multi-platform `platforms` list for the locally loaded LLM runtime image.
7. Add `cache-from` and `cache-to` with a stable scope for this LLM runtime image.
8. Keep the existing pinned base digest and model checksum.
9. Prevent cache export failures from failing the build when the image itself is valid.

### Phase 3 - Local Build Helper And Docs

1. Add a local build helper under `test/e2e/mcp/src/test/resources/docker/llm-runtime/`.
2. Map `x86_64` and `amd64` to the linux/amd64 digest.
3. Map `arm64` and `aarch64` to the linux/arm64 digest.
4. Fail unsupported architectures with a short actionable error.
5. Support a dry-run or print mode for architecture-selection validation.
6. Add an ASF license header and POSIX `sh` syntax.
7. Make the helper executable if README uses direct execution; otherwise document `sh build-local.sh`.
8. Update `mcp/README.md` and `mcp/README_ZH.md` to use the helper.
9. Add safe cleanup guidance and clarify that volume pruning is not default.

### Phase 4 - Verification And Review

1. Verify both LLM workflows have MCP-scoped `pull_request` path filters and do not include broad root, aggregator, or Speckit-only paths.
2. Run focused LLM configuration/runtime tests.
3. Run scoped Checkstyle/Spotless for touched modules.
4. Run helper syntax validation and dry-run or print mode.
5. Verify helper execution style matches README instructions.
6. Use `mcp-builder` review because implementation touches MCP E2E/workflow docs.
7. Report commands, exit codes, and residual risk.

## Verification Commands

Planned commands after implementation:

```bash
rg -n "pull_request|paths:" .github/workflows/mcp-llm*.yml
```

```bash
rg -n "'pom.xml'|'distribution/pom.xml'|'test/e2e/pom.xml'|'.specify/specs/\\*mcp\\*/\\*\\*'" .github/workflows/mcp-llm*.yml
```

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=LLME2EConfigurationTest,LLMRuntimeSupportTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

```bash
./mvnw -pl mcp/core,mcp/bootstrap,test/e2e/mcp -am -Pcheck \
  -DskipTests -DskipITs checkstyle:check spotless:check
```

```bash
docker system df
```

## Risks And Mitigations

- GHA cache misses still download the GGUF model. This is expected and remains checksum-protected.
- Buildx cache adds workflow complexity. Keep the change local to the image build step and preserve artifact upload.
- GHA cache API v2 requires modern Buildx/BuildKit. GitHub-hosted runners should satisfy this through Docker actions, but version logging keeps self-hosted or future runner failures diagnosable.
- GitHub path filters are OR-based. Removing broad aggregator paths means a root-POM-only PR will not run this LLM lane; other repository CI remains responsible for non-MCP-only build coverage.
- GitHub changed-file path-filter limits can miss unusually large PR matches. Manual dispatch remains the fallback for those PRs.
- A path-filtered workflow configured as required can remain pending and block merge. The LLM workflows must remain non-required in branch protection/rulesets.
- Buildx `load: true` is a local single-platform output. The LLM runtime workflow must not use release-style multi-platform image configuration.
- Local helper introduces shell portability concerns. Keep it POSIX `sh` compatible.
- Docker cleanup can delete useful state. Document low-risk cleanup separately and require confirmation for volumes.
