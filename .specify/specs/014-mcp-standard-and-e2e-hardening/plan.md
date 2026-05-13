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

# Implementation Plan: MCP Standard and E2E Hardening

**Branch**: `001-shardingsphere-mcp` | **Date**: 2026-05-13 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `.specify/specs/014-mcp-standard-and-e2e-hardening/spec.md`
**Note**: This plan is maintained manually. Do not run branch-changing Speckit commands.

## Summary

Close standards and evidence gaps found by the latest mcp-builder review.
The work is split into descriptor semantics, output contract validation, distribution release evidence, and E2E running-mode evidence.

The first implementation slice should fix the highest-risk semantic mismatch: planning tools must not advertise read-only or idempotent behavior while saving workflow state.
The second slice should add evidence gates for real packaged startup paths and release manifest validation.

## Technical Context

**Language/Version**: Java 21 for MCP modules and E2E runtime tests.
**Primary Dependencies**: MCP Java SDK, embedded Tomcat, Jackson, JUnit 5, Mockito, Testcontainers where enabled.
**Storage**: In-memory workflow session state and external test databases for PR-gated E2E running modes.
**Testing**: Module-scoped Maven tests, PR-gated MCP E2E tests under `test/e2e/mcp`, golden contract snapshots, release validation checks.
**Target Platform**: ShardingSphere MCP standalone runtime with HTTP and STDIO transports, packaged distribution, Docker entrypoint, and Proxy-oriented workflow behavior.
**Project Type**: Java backend runtime, descriptor catalog, transport adapter, distribution package, and E2E test suite.
**Performance Goals**: Keep every MCP E2E PR job within its explicit timeout budget.
Split running modes across jobs so the total wall-clock time is bounded by the slowest required job rather than a sequential sum.
**Constraints**: No branch switching, no generated `target/` edits, no broad rewrites, no prose-only completion evidence.
**Scale/Scope**: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, `mcp/bootstrap`, `distribution/mcp`, and `test/e2e/mcp`.

## Constitution Check

*GATE: Must pass before implementation. Re-check after design decisions and before code changes.*

- **Proxy-first logical abstraction**: Pass with scope note. Workflow behavior and side-effecting operations must stay Proxy-oriented; H2 remains only deterministic test infrastructure.
- **Explicit operator control**: Pass. Side-effecting tools must preserve preview, approval, and selected execution mode.
- **Minimal safe automation**: Pass. This package adds evidence and standardization; it does not authorize migration, backfill, or rollback orchestration.
- **Deterministic naming and transparent changes**: Pass. Tool naming changes use the `database_gateway_` preferred prefix and do not retain old generic aliases.
- **Complete verification before completion**: Pass. Each requirement has command, artifact, or contract evidence before closure.
- **Repository rules**: Pass. `AGENTS.md` and `CODE_OF_CONDUCT.md` remain binding; implementation requires scoped checks.

## Project Structure

### Documentation

```text
.specify/specs/014-mcp-standard-and-e2e-hardening/
|-- spec.md
|-- plan.md
|-- tasks.md
`-- checklists/
    `-- requirements.md
```

### Source Code

```text
mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/
mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/
mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/
mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/
mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/
distribution/mcp/
test/e2e/mcp/
```

**Structure Decision**: Requirement management lives in `.specify/specs/014-mcp-standard-and-e2e-hardening/`.
Implementation remains in existing MCP modules; no new runtime module is required.

## Phase 0: Research and Evidence Inventory

- Confirm the current MCP protocol version target used by bootstrap and descriptor contract tests.
- Inventory tool descriptors, annotations, output schemas, and public names.
- Inventory E2E running modes, PR path filters, enablement flags, current command coverage, and current duration budgets.
- Inventory distribution startup paths and release manifest validation gaps.
- Compare this package with `.specify/specs/013-mcp-protocol-field-standardization/` to avoid duplicate implementation.

## Phase 1: Design

- Define the `database_gateway_` preferred tool naming policy with no old-name compatibility aliases.
- Define planning-tool annotation semantics and test expectations.
- Define complete `execution.taskSupport` cleanup because MCP Tasks are experimental and out of scope.
- Define structured-output-only cleanup policy and schema validation boundary.
- Define packaged distribution, Docker, server manifest, and registry package exit gates.
- Define the PR-gated E2E evidence matrix for H2, MySQL, STDIO, distribution, remote HTTP, Docker or registry smoke, and live LLM running modes.

### Confirmed Design Decisions

- Public MCP tool names use the `database_gateway_` prefix.
  Old generic names are removed without aliases because compatibility is explicitly out of scope.
- `execution.taskSupport` is removed completely.
  The MCP Tasks protocol remains out of scope because the current implementation only had descriptor and adapter fragments.
- Tool-level `response_format=markdown` is not supported.
  The MCP contract remains JSON `structuredContent`; LLM harness prompts may still ask models to avoid Markdown in final JSON answers.
- Tools declaring `outputSchema` must validate successful `structuredContent` before transport exposure.
  Error responses stay in the existing MCP error envelope and do not need to match success schemas.
- PR E2E is path-gated by MCP-related paths and split by running mode so runtime is controlled by the slowest job, not by one serial job.

## Phase 2: Implementation Slices

1. Correct planning-tool annotations and add descriptor tests.
2. Add or update golden contract tests for tool names, annotations, and removed unsupported execution metadata.
3. Add output schema conformance tests and runtime validation where needed.
4. Add packaged script startup and release manifest validation tests.
5. Add Docker entrypoint or equivalent registry package smoke evidence.
6. Add PR-gated E2E running-mode evidence matrix and commands for MySQL, STDIO, distribution, remote HTTP, Docker or registry smoke, and live LLM.
7. Remove `response_format` and Markdown response formatting from requirements, descriptors, tests, and docs while validating structured JSON contracts first.

## Verification Strategy

- Documentation-only requirement changes: inspect files and run no Maven command.
- Descriptor or catalog changes: run scoped MCP module tests plus Checkstyle or Spotless gates for touched modules.
- Bootstrap transport changes: run bootstrap tests and HTTP contract E2E.
- Distribution changes: build `distribution/mcp` and run packaged HTTP/STDIO smokes.
- E2E running-mode changes: run every MCP E2E running mode in PR when MCP-related paths change; split jobs by running mode for time control.
- Release manifest changes: run a release validation test that rejects snapshot metadata.

## PR-Gated E2E Evidence Matrix

The PR path filter is active for `mcp/**`, `distribution/mcp/**`, `distribution/pom.xml`, `test/e2e/mcp/**`, `test/e2e/pom.xml`,
MCP Speckit packages, and MCP workflow or script files.

- Default H2 HTTP, remote HTTP contract/security, and default MCP tests:
  `mcp-default-e2e` runs `./mvnw -pl mcp,test/e2e/mcp -am install ...`.
  It uses the default contract and H2 flags, has a 15m timeout, and records Surefire reports.
- H2 STDIO production runtime:
  `mcp-h2-stdio-e2e` runs `ProductionH2*E2ETest` and `ProductionMultiDatabaseE2ETest`.
  It enables `mcp.e2e.production.stdio.enabled=true`, disables H2 HTTP, has a 15m timeout, and records Surefire reports.
- MySQL HTTP and MySQL STDIO runtime, plus Proxy workflow coverage:
  `mcp-mysql-e2e` runs `ProductionMySQLRuntimeSmokeE2ETest` and `HttpProductionProxy*WorkflowE2ETest`.
  It enables Docker, MySQL, and STDIO, has a 15m timeout, and records Surefire reports plus Testcontainers logs.
- Packaged distribution HTTP/STDIO and registry metadata shape:
  `mcp-distribution-e2e` packages `distribution/mcp` and runs packaged distribution smoke tests.
  It enables `mcp.e2e.distribution.enabled=true`, has a 15m timeout, and records the packaged home plus Surefire reports.
- Docker / OCI-style STDIO smoke:
  `mcp-stdio-container-e2e` runs `mcp-stdio-smoke.py shardingsphere-mcp-ci:local`.
  It enables Docker setup, has a 15m timeout, and proves container initialize/list-tools behavior.
- Live LLM smoke:
  `mcp-llm-e2e.yml` runs `LLMSmokeE2ETest` with Ollama Docker and `mcp.e2e.llm.enabled=true`.
  It has a 60m timeout and uploads `test/e2e/mcp/target/llm-e2e`, Surefire reports, and Ollama logs.
- Live LLM usability suite:
  `mcp-llm-usability-e2e.yml` runs `LLMUsabilitySuiteE2ETest` with Ollama Docker and `mcp.e2e.llm.enabled=true`.
  It has a 60m timeout and uploads LLM score reports, Surefire reports, and Ollama logs.
- Release registry publication:
  `mcp-build.yml` prepares and validates `mcp/server.json`, pushes the Docker image, and publishes registry metadata.
  It runs on release or manual dispatch, has a 60m timeout, and records prepared metadata plus image tags.

### Current Duration Evidence

- Default MCP plus MCP E2E: 3m22s.
- H2 STDIO production E2E: 8m25s.
- MySQL HTTP plus STDIO E2E: 2m55s.
- Distribution assembly: 15.463s.
- Packaged HTTP/STDIO/plugin smoke: 6.124s.
- Live LLM smoke plus usability: 32m12s total.
  Local slow-class evidence: `LLMUsabilitySuiteE2ETest` 1269.581s, `LLMSmokeE2ETest` 622.645s,
  `ProductionH2MetadataResourceE2ETest` 204.897s, `ProductionMySQLRuntimeSmokeE2ETest` 172.396s,
  `ProductionH2SQLExecutionE2ETest` 144.497s.

### Implementation Verification Evidence

- MCP core/support/bootstrap/features scoped unit tests passed with exit code 0.
- MCP E2E harness/support tests passed with exit code 0.
- Programmatic HTTP contract, recovery, transaction, metadata discovery, and golden contract E2E passed with exit code 0.
- GitHub workflow YAML parsing and MCP release metadata script smoke checks passed with exit code 0.
- Scoped MCP Checkstyle, Spotless apply, and Spotless check passed with exit code 0.
- Reverse searches found no legacy public tool names in MCP implementation or E2E paths.
- Reverse searches found `execution.taskSupport` only in Speckit cleanup requirements, not implementation paths.
- Reverse searches found `response_format=markdown` only in Speckit negative requirements; implementation has a descriptor guard test only.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | Not applicable | Not applicable |
