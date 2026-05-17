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

# Doubt Review: MCP LLM Docker E2E Environment Hardening

## Cycle 1 Claim

The 024 Speckit plan is sufficient to implement the accepted five LLM Docker E2E environment fixes without changing MCP behavior or score evidence.

## Review Contract

- LLM workflows run for MCP-related PRs only.
- LLM workflows remain non-required for merge, but failures stay visible.
- Docker-owned LLM runtime evidence remains unchanged.
- Docker cache improves cost but does not weaken checksum verification.
- Docker cleanup remains confirmation-gated.

## Findings And Classification

### Finding 1: Root and aggregator path filters would still trigger non-MCP PRs

Classification: Valid + actionable.

Reason:
GitHub `paths` filters are OR-matched across changed files.
Keeping `pom.xml`, `distribution/pom.xml`, `test/e2e/pom.xml`, or Speckit-only paths means the LLM lane can run even when no MCP module changed.

Action:
Update requirements and tasks to remove those broad path triggers.

### Finding 2: Path-filtered workflows can block merge if configured as required

Classification: Valid + actionable.

Reason:
The workflow cannot guarantee non-blocking merge from YAML alone.
If a skipped path-filtered workflow is required, GitHub can leave it pending and block merge.

Action:
Document that the LLM workflows must not be branch-protection or ruleset required checks.
Do not use `continue-on-error`.

### Finding 3: Buildx cache inputs were underspecified

Classification: Valid + actionable.

Reason:
The plan said "add Docker Buildx/GHA cache" but did not require explicit `context`, `file`, and `load`.
Without `load`, Testcontainers cannot rely on the local image tag.
Without explicit context/file, the action can diverge from the current Docker CLI build semantics.

Action:
Require explicit Buildx inputs and local image loading.

### Finding 4: Cache export failure should not fail score evidence

Classification: Valid + actionable.

Reason:
The Docker image build is score-critical, but cache export is only an optimization.
A cache API failure should not fail an otherwise valid E2E image build.

Action:
Require cache export to be non-fatal, for example with `ignore-error=true`.

### Finding 5: Helper validation could force a large model download

Classification: Valid + actionable.

Reason:
Testing a helper script by building the image can force a large GGUF download even when only architecture selection needs verification.

Action:
Require dry-run or print mode.

### Finding 6: GitHub changed-file limits can miss MCP path matches in unusually large PRs

Classification: Valid trade-off.

Reason:
GitHub evaluates path filters from a generated changed-file list and documents a 300-file limit.
A very large PR can miss an MCP path if it is outside the first 300 files returned to the filter.

Action:
Keep the MCP-scoped PR filter because it is still the right default, but document manual dispatch as the fallback for unusually large PRs.

### Finding 7: Buildx `load` should stay single-platform

Classification: Valid + actionable.

Reason:
The LLM E2E image must be available in the local Docker daemon for Testcontainers.
Docker documents `--load` as a single-platform output, while multi-platform images belong to registry push or image stores that support manifest lists.

Action:
Require the LLM runtime image build to avoid multi-platform `platforms` configuration when using `load: true`.

### Finding 8: GHA cache API v2 failures can look like generic Docker build failures

Classification: Valid + actionable.

Reason:
Docker's GitHub Actions cache backend now requires GitHub Cache service API v2 support.
GitHub-hosted runners using Docker actions should be current, but a future self-hosted runner or stale Buildx/BuildKit stack can fail with a legacy-cache error.

Action:
Require `docker buildx version` logging after setup so maintainers can diagnose cache API v2 compatibility without rerunning with extra debug steps.

### Finding 9: Helper invocation can drift from file mode

Classification: Valid + actionable.

Reason:
The local helper lives under test resources, and a newly added script may not be executable unless the implementation explicitly sets file mode.
If README shows direct execution but the file is not executable, local reproduction fails before Docker even starts.

Action:
Require POSIX `sh` compatibility, ASF license header, and either executable file mode or README instructions that invoke the helper through `sh`.

### Finding 10: Dedicated workflow-file triggers could conflict with "MCP module only" wording

Classification: Valid + actionable.

Reason:
The path list should exclude broad non-module triggers, but removing the dedicated LLM workflow files would make workflow-only changes unvalidated by the lane they modify.
This is a narrow lane-infrastructure exception, not a general non-module trigger.

Action:
Document that only the dedicated LLM workflow files may trigger outside MCP module paths; root POM, aggregator POM, and Speckit-only changes remain excluded.

## mcp-builder Review

The changes remain outside MCP tool/resource schema and do not alter protocol behavior.
The plan preserves score evidence by keeping LLM failures visible and preserving artifact upload.
The main MCP quality risk was accidentally hiding failures through `continue-on-error`; the plan now rejects that path explicitly.

## Stop Condition

After two cycles, no additional design issue requires a new user decision before implementation.
