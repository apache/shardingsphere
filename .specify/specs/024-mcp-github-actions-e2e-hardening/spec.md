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

# Feature Specification: MCP GitHub Actions E2E Hardening

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-17
**Status**: Draft
**Input**: User requested a Speckit requirement package for MCP GitHub Actions consolidation and coverage improvement. Do not switch branches. Do not write workflow or test implementation code yet.

## Branch Constraint

- Work must remain on the existing `001-shardingsphere-mcp` branch.
- Speckit branch creation or branch switching commands are forbidden for this requirement package.
- This round is documentation-only; workflow, production, test, distribution, and E2E implementation changes require a later explicit implementation command.
- Existing worktree changes are out of scope and must not be reverted or reformatted by this package.

## Goal

Reduce duplicated MCP GitHub Actions configuration while preserving the current E2E gates, then add targeted coverage for the currently thin areas: scoped JDK 21 quality checks, container HTTP runtime smoke, release image pull-back validation, release manifest inspection, and broader LLM usability topology coverage.

The package separates fast PR gates, heavier scheduled gates, and release-only publication gates so that coverage improves without turning every MCP PR into an unstable or overly expensive pipeline.

## Clarifications

### Session 2026-05-17

- The current branch already contains four MCP-related workflow files relative to `master`: `jdk21-subchain-ci.yml`, `mcp-build.yml`, `mcp-llm-e2e.yml`, and `mcp-llm-usability-e2e.yml`.
- Existing dedicated LLM workflows share nearly identical setup, Docker runtime image build, Maven invocation shape, and artifact upload logic; they differ mainly by suite selector and schedule.
- Existing distribution and container gates both depend on a packaged MCP distribution and can share the packaging step.
- Release publishing must remain a separate workflow because it needs release event semantics, package write permissions, OIDC login, image push, and MCP Registry publication.
- LLM usability coverage is valuable but heavier and more variable than smoke coverage; it must be split between PR-critical and scheduled coverage.
- This specification records the design target only. It does not authorize code changes.

## User Scenarios & Testing

### User Story 1 - Maintainers consolidate LLM E2E workflows (Priority: P1)

As an MCP CI maintainer, I want the smoke and usability LLM suites to share one workflow definition so that image digest, Docker build, artifact upload, and Maven setup changes cannot drift between two near-duplicate files.

**Independent Test**: Review the future workflow diff and verify one LLM E2E workflow still runs `LLMSmokeE2ETest` and `LLMUsabilitySuiteE2ETest` with separate suite identities, artifacts, schedules, and failure reporting.

**Acceptance Scenarios**:

1. Given an MCP PR touches LLM E2E code or MCP tool/resource behavior, when LLM smoke is required, then the consolidated workflow runs the smoke suite without requiring a second duplicated workflow file.
2. Given scheduled usability validation is required, when the schedule fires, then the consolidated workflow runs the usability suite with its own artifact name and report path.
3. Given both suites are represented by a matrix, when workflow parallelism is configured, then `max-parallel` is set to `1` to avoid concurrent local LLM runtime contention.
4. Given a suite class is renamed or missing, when Maven runs with `-Dsurefire.failIfNoSpecifiedTests=true`, then the workflow fails without an additional shell-only selector existence check.
5. Given artifacts are uploaded after failure, when either suite fails, then `test/e2e/mcp/target/llm-e2e` and `test/e2e/mcp/target/surefire-reports` remain available with suite-specific artifact names.

### User Story 2 - Maintainers consolidate distribution and container E2E packaging (Priority: P1)

As an MCP CI maintainer, I want distribution smoke and container smoke to share the packaged distribution build so that CI does not package the same MCP distribution twice before testing closely related runtime surfaces.

**Independent Test**: Review the future `jdk21-subchain-ci.yml` and verify one distribution/container job packages `distribution/mcp` once, validates registry metadata, runs packaged distribution smoke, builds the local Docker image, and runs container smoke.

**Acceptance Scenarios**:

1. Given the distribution/container job starts, when `distribution/mcp` is packaged, then subsequent packaged and container smoke steps reuse the same packaged artifact.
2. Given registry metadata validation fails, when the job reaches the validation step, then Docker image build and container smoke do not run.
3. Given packaged distribution smoke fails, when the job reports failure, then the failed step identifies whether HTTP/STDIO packaged runtime or plugin discovery failed.
4. Given Docker image smoke fails, when the job reports failure, then packaged distribution validation remains separately visible in the step history.
5. Given the combined job uses a longer runtime budget, when timeout is configured, then it remains at or below the repository maximum of 60 minutes.

### User Story 3 - Reviewers get scoped MCP quality gates (Priority: P1)

As a reviewer, I want the MCP subchain to have a scoped quality gate in GitHub Actions so that Java 21 compilation and E2E success cannot hide Checkstyle, Spotless, RAT, or related `-Pcheck` failures.

**Independent Test**: Trigger an MCP-only workflow change and verify a scoped Maven quality command runs for MCP-related modules without requiring a full repository build.

**Acceptance Scenarios**:

1. Given MCP source, test, distribution, or workflow files change, when the JDK 21 subchain workflow runs, then a scoped quality job validates style/static gates for touched MCP modules.
2. Given the quality job is scoped, when it runs, then it avoids full-repository `clean install` unless explicitly requested by a later implementation decision.
3. Given a license header or style issue exists in MCP files, when the quality job runs, then the workflow fails before handoff.
4. Given the workflow contains Maven commands, when implementation is reviewed, then any use of `-Dspotless.skip=true` is limited to non-style jobs and is not the only quality signal.

### User Story 4 - Release maintainers verify the published MCP image (Priority: P1)

As an MCP release maintainer, I want the release workflow to validate the image that was actually pushed so that a locally built distribution cannot pass while the GHCR tag or multi-arch manifest is broken.

**Independent Test**: Run a release or manual release workflow in a safe environment and verify it inspects the published image manifest and pulls the published tag for at least one runtime smoke test.

**Acceptance Scenarios**:

1. Given the release workflow pushes `ghcr.io/apache/shardingsphere-mcp:<version>`, when push succeeds, then the workflow pulls the same published tag before final publication completion.
2. Given the image is published as multi-arch, when manifest inspection runs, then `linux/amd64` and `linux/arm64` entries are verified.
3. Given the current runner is `ubuntu-latest`, when pull-back smoke runs, then at least the amd64 image is launched and smoke-tested through a supported transport.
4. Given arm64 runtime execution is unavailable, when release evidence is collected, then manifest presence is verified and the missing arm64 runtime smoke is explicitly recorded as residual risk.
5. Given MCP Publisher is downloaded during release, when the implementation is reviewed, then the publisher version and archive integrity are pinned or otherwise justified by release policy.

### User Story 5 - Runtime coverage includes container HTTP (Priority: P2)

As an MCP runtime maintainer, I want the Docker image to be smoke-tested over HTTP as well as STDIO so that the container's remote-usable transport is covered before release.

**Independent Test**: Build the local MCP Docker image, start it with HTTP transport enabled, initialize an MCP session, list tools, read capabilities, search metadata, and execute a read-only SQL query.

**Acceptance Scenarios**:

1. Given a local MCP Docker image exists, when the container HTTP smoke starts, then it exposes the configured Streamable HTTP endpoint.
2. Given the HTTP endpoint is reachable, when the test initializes a session, then the MCP runtime returns a valid session response.
3. Given the session is initialized, when the test lists tools and reads `shardingsphere://capabilities`, then core database gateway tools and capabilities are present.
4. Given the fixture database is available, when the test calls `database_gateway_search_metadata` and `database_gateway_execute_query`, then metadata and read-only query results are returned.
5. Given the container exits early or logs unsafe diagnostics, when the test fails, then the failure includes actionable runtime logs without exposing configured secrets.

### User Story 6 - LLM usability coverage expands without slowing every PR (Priority: P2)

As an MCP product maintainer, I want LLM usability coverage to include additional runtime topologies on a schedule while keeping PR feedback focused on the fastest score-closing checks.

**Independent Test**: Review future LLM workflow configuration and verify PR-triggered smoke remains short, while scheduled usability covers H2 HTTP, H2 STDIO core, and MySQL HTTP core scenarios.

**Acceptance Scenarios**:

1. Given a normal MCP PR, when LLM validation is required, then the smoke suite remains the PR-critical gate.
2. Given scheduled usability validation runs, when H2 HTTP full usability executes, then core and extended usability scenarios are evaluated.
3. Given scheduled usability validation runs, when H2 STDIO core executes, then the model is evaluated against STDIO for the core scenarios.
4. Given scheduled usability validation runs, when MySQL HTTP core executes, then the model is evaluated against a real MySQL backend for core scenarios.
5. Given MySQL STDIO full usability is too expensive or unstable, when coverage is reviewed, then it remains out of PR-critical scope unless later evidence justifies adding it.

## Edge Cases

- The consolidated LLM workflow must not start two local LLM server containers concurrently on the same runner unless resource isolation is explicitly designed.
- A failed scheduled LLM usability run must keep evidence artifacts even if the model runtime fails readiness checks.
- Distribution/container consolidation must preserve step-level failure localization despite sharing one job.
- Release pull-back smoke must use the pushed image tag or digest, not the local pre-push image.
- Manifest inspection alone is not a substitute for amd64 runtime smoke on the published image.
- Workflow matrix use must satisfy the repository rule requiring `max-parallel: 20` or lower; LLM matrix should use `max-parallel: 1`.
- Generated directories such as `target/` must not be edited or committed.

## Requirements

### Functional Requirements

- **MGA-FR-001**: The work MUST stay on branch `001-shardingsphere-mcp`; branch switching, branch creation, and branch-changing Speckit commands MUST NOT be used.
- **MGA-FR-002**: This package MUST NOT modify GitHub Actions, production code, tests, distribution files, or generated files before an explicit later implementation command.
- **MGA-FR-003**: Existing unrelated worktree changes MUST NOT be reverted, reformatted, staged, or otherwise changed by this package.
- **MGA-FR-004**: The LLM smoke and LLM usability workflows MUST be consolidated into one workflow or one reusable workflow family unless implementation evidence shows the consolidation increases instability or hides failures.
- **MGA-FR-005**: The consolidated LLM workflow MUST preserve independent execution identity for `LLMSmokeE2ETest` and `LLMUsabilitySuiteE2ETest`.
- **MGA-FR-006**: The consolidated LLM workflow MUST use shared setup, local LLM runtime image build, Maven invocation conventions, and artifact upload paths.
- **MGA-FR-007**: If the LLM workflow uses a matrix, it MUST set `max-parallel` to `1` unless a later resource-isolation design proves safe parallel execution.
- **MGA-FR-008**: The LLM workflow MUST rely on Maven `-Dtest=... -Dsurefire.failIfNoSpecifiedTests=true` for selector existence and MUST NOT require separate `test -f` selector checks.
- **MGA-FR-009**: Distribution smoke and container smoke SHOULD be consolidated into one job that packages `distribution/mcp` once and reuses that artifact for packaged runtime and Docker image validation.
- **MGA-FR-010**: The consolidated distribution/container job MUST keep registry metadata validation, packaged distribution smoke, plugin discovery, Docker image build, and container smoke as distinct named steps.
- **MGA-FR-011**: The consolidated distribution/container job timeout MUST remain at or below 60 minutes.
- **MGA-FR-012**: The JDK 21 subchain workflow MUST include a scoped MCP quality gate that covers Checkstyle, Spotless, license/RAT, or equivalent `-Pcheck` behavior for MCP-related modules.
- **MGA-FR-013**: Non-quality E2E jobs MAY keep `-Dspotless.skip=true`, but the workflow set MUST include at least one scoped style/static quality signal for MCP changes.
- **MGA-FR-014**: The release workflow MUST remain separate from PR CI because it owns release triggers, package write permissions, OIDC registry authentication, image push, and MCP Registry publication.
- **MGA-FR-015**: The release workflow SHOULD validate the published image after push by pulling the published tag or digest and running at least one smoke test on the pulled image.
- **MGA-FR-016**: The release workflow SHOULD inspect the published image manifest and verify `linux/amd64` and `linux/arm64` platform entries.
- **MGA-FR-017**: If true arm64 runtime smoke is not available, the release evidence MUST record manifest-only arm64 validation as a residual risk.
- **MGA-FR-018**: The release workflow SHOULD pin or integrity-check the MCP Publisher download, or document why an unpinned `latest` download remains acceptable.
- **MGA-FR-019**: Container smoke coverage MUST include STDIO and SHOULD add HTTP transport smoke for the MCP Docker image.
- **MGA-FR-020**: Container HTTP smoke MUST cover session initialization, `tools/list`, `shardingsphere://capabilities`, metadata search, and read-only SQL execution.
- **MGA-FR-021**: LLM PR-critical coverage MUST keep a short smoke gate that includes at least H2 HTTP, MySQL HTTP, H2 STDIO, and MySQL STDIO smoke scenarios unless later timing evidence requires a documented reduction.
- **MGA-FR-022**: Scheduled LLM usability SHOULD cover H2 HTTP full usability, H2 STDIO core usability, and MySQL HTTP core usability.
- **MGA-FR-023**: MySQL STDIO full usability SHOULD remain outside PR-critical scope unless timing, stability, and value evidence justify adding it.
- **MGA-FR-024**: Any workflow matrix introduced by implementation MUST satisfy repository GitHub Action standards for job timeout, unique job names, and `max-parallel`.
- **MGA-FR-025**: Future implementation MUST preserve artifact upload for LLM and distribution/container failures.
- **MGA-FR-026**: Future implementation MUST report verification commands and exit codes, including YAML syntax checks and scoped Maven checks.

### Key Entities

- **LLM E2E Suite**: A named suite selector for `LLMSmokeE2ETest` or `LLMUsabilitySuiteE2ETest`, including schedule, Maven command, artifact name, and runtime resource expectations.
- **MCP Distribution/Container Gate**: A CI job that packages `distribution/mcp`, validates metadata, tests packaged runtime, builds a local image, and runs container smoke.
- **Scoped MCP Quality Gate**: A Maven quality check limited to MCP-related modules and workflows, intended to catch style/static/license failures missed by E2E-only commands.
- **Release Pull-Back Smoke**: A release-only verification that tests the image tag or digest after it is pushed to GHCR.
- **LLM Topology Coverage**: The matrix of model-facing validation across runtime backend and transport combinations, such as H2 HTTP, H2 STDIO, MySQL HTTP, and MySQL STDIO.

## Scope

### In Scope

- GitHub Actions design for MCP workflow consolidation.
- Requirements for LLM workflow consolidation.
- Requirements for distribution/container job consolidation.
- Requirements for scoped MCP quality gates.
- Requirements for published image pull-back and manifest inspection.
- Requirements for container HTTP smoke coverage.
- Requirements for scheduled LLM usability topology expansion.
- Documentation-only Speckit package creation.

### Out of Scope

- Editing `.github/workflows/**` in this round.
- Editing Java production, test, E2E, distribution, Dockerfile, or generated files in this round.
- Committing, pushing, creating a branch, or opening a pull request.
- Full-repository CI redesign unrelated to MCP.
- Removing existing E2E tests without a later implementation command and failure-impact analysis.
- Adding new external service dependencies beyond Docker/GHCR checks already implied by release validation.
- Guaranteeing true arm64 runtime smoke without an available arm64 runner.

## Success Criteria

### Measurable Outcomes

- **MGA-SC-001**: The future LLM workflow consolidation removes duplicate workflow setup while preserving both smoke and usability suite execution.
- **MGA-SC-002**: The future distribution/container consolidation packages `distribution/mcp` once for packaged and container smoke validation.
- **MGA-SC-003**: MCP PR validation includes at least one scoped quality gate that can fail on style/static/license issues before handoff.
- **MGA-SC-004**: Release validation proves the pushed GHCR image is pullable and runnable on at least amd64, and verifies the multi-arch manifest contains amd64 and arm64 entries.
- **MGA-SC-005**: Container HTTP smoke exercises the same published runtime surface that remote MCP clients are expected to use.
- **MGA-SC-006**: Scheduled LLM usability expands beyond H2 HTTP while PR-critical LLM feedback remains bounded.
- **MGA-SC-007**: Workflow YAML validation and scoped Maven verification commands are reported with exit codes after implementation.
- **MGA-SC-008**: No unrelated existing worktree changes are modified by this package or its later implementation.

## Assumptions

- `LLMSmokeE2ETest` remains the fastest model-facing confidence gate.
- `LLMUsabilitySuiteE2ETest` remains more expensive and belongs partly or fully in scheduled validation unless reviewers explicitly require it on PRs.
- Docker is available on the relevant GitHub-hosted Linux runners for MySQL, LLM runtime, and MCP image smoke tests.
- Release jobs can pull from GHCR after push using the same permissions model already used for image publication.
- A true arm64 runtime smoke depends on runner availability and may be deferred with explicit residual risk.
