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

# MCP GitHub Actions E2E Hardening Requirements

Canonical Speckit package: `.specify/specs/024-mcp-github-actions-e2e-hardening/`
Current branch: `001-shardingsphere-mcp`
Branch constraint: do not switch or create branches for this work.

## Goal

Consolidate duplicated MCP GitHub Actions where safe, preserve current E2E intent, and add targeted coverage for scoped quality checks, container HTTP runtime, release image pull-back validation, multi-arch manifest validation, and scheduled LLM usability topology expansion.

## User Constraints

- Do not switch branches.
- Use Speckit to manage the requirement.
- Do not edit workflow, production, test, E2E, distribution, Dockerfile, or generated files in this round.
- Preserve unrelated worktree changes.
- Keep release publication workflow separate from PR CI.

## Functional Requirements

- Consolidate `mcp-llm-e2e.yml` and `mcp-llm-usability-e2e.yml` into one workflow or reusable workflow family unless implementation evidence shows consolidation increases risk.
- Preserve independent suite identity for `LLMSmokeE2ETest` and `LLMUsabilitySuiteE2ETest`.
- Share LLM setup, local runtime image build, Maven invocation conventions, and artifact upload logic.
- If matrix is used for LLM suites, set `max-parallel: 1`.
- Remove redundant selector existence shell checks and rely on Maven `-Dsurefire.failIfNoSpecifiedTests=true`.
- Consolidate distribution and container smoke into one job that packages `distribution/mcp` once and keeps validation, packaged smoke, image build, and container smoke as separate named steps.
- Keep the consolidated distribution/container job timeout at or below 60 minutes.
- Add a scoped MCP quality gate for Checkstyle, Spotless, license/RAT, or equivalent `-Pcheck` behavior.
- Keep release workflow separate because it owns release triggers, package write permissions, OIDC registry authentication, image push, and MCP Registry publication.
- Validate the published GHCR image after push by pulling the published tag or digest and running at least one smoke test.
- Inspect the published image manifest and verify `linux/amd64` and `linux/arm64` entries.
- Record manifest-only arm64 validation as residual risk when true arm64 runtime smoke is unavailable.
- Pin or integrity-check the MCP Publisher download, or document why an unpinned download remains acceptable.
- Add container HTTP smoke covering session initialization, `tools/list`, `shardingsphere://capabilities`, metadata search, and read-only SQL execution.
- Keep PR-critical LLM coverage focused on smoke.
- Use scheduled LLM usability for H2 HTTP full usability, H2 STDIO core usability, and MySQL HTTP core usability.
- Keep MySQL STDIO full usability outside PR-critical scope unless timing and stability evidence justify adding it.

## Verification Requirements

- Validate changed workflow YAML syntax.
- Run scoped Maven verification for MCP-related workflow/test changes.
- Report all command exit codes.
- Preserve LLM and distribution/container artifacts on failure.
- Confirm no unrelated existing worktree changes were modified.
