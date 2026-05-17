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

# Implementation Plan: MCP CI E2E And Release Consolidation

**Branch**: `001-shardingsphere-mcp`
**Date**: 2026-05-17
**Spec**: `.specify/specs/026-mcp-ci-e2e-release-consolidation/spec.md`
**Input**: New non-conflicting design package for MCP GitHub Actions, E2E, and release validation.
**Note**: This plan is documentation-only until the user gives an explicit implementation command.

## Summary

Move MCP CI toward one clear target state:

1. Replace workflow-invoked smoke tests with complete MySQL-backed E2E suites.
2. Consolidate distribution validation into one package-centered E2E path.
3. Keep JDK 21 CI focused on Java 21 MCP build/runtime behavior.
4. Strengthen release workflow with post-push GHCR and MCP Registry validation.

## Technical Context

**Language/Version**: Java 21 for MCP subchain; GitHub Actions YAML; Bash for workflow validation helpers if needed.
**Primary Dependencies**: Maven, Maven Toolchains, Docker, GitHub Actions, GHCR, MCP Java SDK, MySQL Testcontainers/service containers.
**Testing**: JUnit 5 E2E tests under `test/e2e/mcp`, workflow static checks, Docker image runtime checks.
**Target Platform**: GitHub-hosted Ubuntu runners and release workflow publishing to GHCR.
**Project Type**: Java backend/runtime plus CI/release workflows.
**Performance Goals**: Keep each workflow job within repository GitHub Action timeout policy; avoid adding duplicate CI work already covered by Required Check.
**Constraints**: No branch switching, no implementation in this design round, no editing generated `target/`, no duplicate Checkstyle/RAT/Spotless in JDK 21 CI while Required Check covers MCP.
**Scale/Scope**: MCP modules, MCP E2E module, MCP distribution, JDK 21 CI workflow, LLM E2E workflows, and MCP release workflow.

## Constitution Check

- **Proxy-first scope**: Pass. The runtime E2E target is MySQL-backed MCP/Proxy behavior, not H2-only local shortcuts.
- **Explicit operator control**: Pass. This design changes CI validation only and does not add runtime mutation behavior.
- **Minimal safe automation**: Pass. Release validation pulls and runs published artifacts, but does not introduce destructive operations.
- **Deterministic naming and transparent changes**: Pass. Workflow/job/suite names must be explicit and unique.
- **Complete verification before completion**: Pass. Target validation spans build, runtime, package, container, and release artifact layers.

## Source-Driven Constraints

- MCP official transports define stdio and Streamable HTTP as standard transports. Complete E2E must cover both where MCP runtime is validated.
- GitHub Actions official workflow syntax supports service containers, job timeouts, and workflow path filters; workflow design must stay inside those primitives.
- GitHub Container Registry official docs support pulling by tag and digest; release validation must inspect or pull the published artifact after push.
- Maven Toolchains official docs explain that a build can select a JDK independently from the JRE running Maven; Java 21 MCP subchain needs explicit Java 21 validation.

## Structure Decision

### Documentation

```text
.specify/specs/026-mcp-ci-e2e-release-consolidation/
|-- spec.md
|-- plan.md
|-- source-map.md
|-- doubt-review.md
|-- reanalysis.md
|-- tasks.md
`-- checklists/
    `-- requirements.md

specs/015-mcp-ci-e2e-release-consolidation/
`-- requirements.md
```

### Future Implementation Paths

```text
.github/workflows/jdk21-subchain-ci.yml
.github/workflows/mcp-build.yml
.github/workflows/mcp-llm-e2e.yml
.github/workflows/mcp-llm-usability-e2e.yml
distribution/mcp/
mcp/
test/e2e/mcp/
```

## Design Decisions

### 1. Delete Smoke As A Target State, Not Before Coverage Exists

Smoke-only tests should not remain as final workflow targets. The implementation order must first move the useful topology coverage into full suites, then remove smoke invocation, then delete or rename the old smoke classes where appropriate.

### 2. Use MySQL For Real E2E

H2-backed production E2E is not accepted as real MCP E2E evidence. H2 may stay in unit tests, lightweight integration tests, or demo configuration if it is not the CI evidence for production runtime coverage.

### 3. Prefer One Complete LLM Usability E2E Target

If `LLMUsabilitySuiteE2ETest` can cover the current smoke topology, the separate `LLMSmokeE2ETest` workflow target should be removed. The final LLM E2E lane should be a complete usability suite, not smoke plus usability.

If a second LLM workflow remains for scheduling or resource isolation, it must still run a complete non-smoke target. The final state must not keep a standalone smoke workflow entry.

### 4. Build One Complete Distribution E2E

Distribution E2E should package `distribution/mcp` once and validate packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO from that artifact set. Separate distribution smoke and STDIO container smoke jobs should collapse into this one artifact-centered validation path.

### 5. Do Not Duplicate Required Check

Required Check already runs repo-wide Checkstyle, Spotless, and RAT. JDK 21 CI should not add duplicate style gates unless future inspection finds MCP files are no longer covered.

Current evidence: root `pom.xml` includes `mcp`, `test`, and `distribution`; `test/e2e/pom.xml` includes `mcp`; `distribution/pom.xml` includes `mcp`; `required-check.yml` runs Checkstyle, Spotless, and RAT without path filters.

### 6. Validate Published Release Artifacts

Release workflow should keep local pre-push validation but add post-push validation: pull the published GHCR tag or digest, inspect the manifest platforms, run a minimal MCP runtime validation from the published image, and pin or integrity-check `mcp-publisher`.

The native GitHub-hosted runner runtime check proves the pulled runner-platform image, normally linux/amd64. The linux/arm64 publish result should be verified through manifest inspection unless a later implementation explicitly accepts QEMU-based arm64 runtime cost.

## Implementation Phases

### Phase 1 - Inventory Current Coverage

1. List every workflow-invoked smoke test and its backend/transport topology.
2. Map each smoke topology to a target full E2E scenario.
3. Identify H2-backed production E2E invocations that must leave real E2E workflows.
4. Identify H2 enablement defaults that would still imply H2 is production E2E evidence.
5. Re-verify Required Check coverage for MCP modules and files.

### Phase 2 - Consolidate MySQL HTTP/STDIO E2E

1. Extend or create full MySQL HTTP E2E scenarios for any smoke-only coverage.
2. Extend or create full MySQL STDIO E2E scenarios for any smoke-only coverage.
3. Adjust workflow selectors to invoke full suites instead of smoke-only classes.
4. Remove or rename smoke classes after equivalent coverage exists.

### Phase 3 - Consolidate LLM E2E

1. Expand LLM usability scenarios to cover MySQL HTTP and MySQL STDIO topology.
2. Remove `LLMSmokeE2ETest` from workflow invocation after coverage is proven.
3. Remove any standalone LLM smoke workflow entry after coverage is proven.
4. Collapse the final LLM workflow design to one complete usability target unless scheduling or resource isolation requires a documented second non-smoke workflow.
5. Preserve LLM artifacts and visible failures.

### Phase 4 - Build Complete Distribution E2E

1. Package `distribution/mcp` once.
2. Use the packaged home for packaged HTTP, packaged STDIO, and plugin discovery.
3. Build a local container image from the packaged output.
4. Validate both container HTTP and container STDIO using MySQL-backed runtime data.
5. Replace separate smoke jobs with the complete distribution E2E job.

### Phase 5 - Strengthen Release Workflow

1. Keep local build, distribution validation, metadata generation, and Docker push.
2. Capture pushed image digest or inspect pushed manifest.
3. Pull the published GHCR image by tag or digest.
4. Validate linux/amd64 and linux/arm64 manifest entries.
5. Run MCP runtime validation against the native published image pulled on the runner.
6. Pin or integrity-check `mcp-publisher`.
7. Preserve release evidence in workflow output or artifacts.

### Phase 6 - Verification And Review

1. Run focused Maven tests for touched MCP/E2E suites.
2. Run workflow static checks for YAML selectors and job names.
3. Run scoped Checkstyle/Spotless only for touched modules as local verification, while not adding duplicate JDK 21 CI gates.
4. Run MCP builder review for changes touching `mcp`, `test/e2e/mcp`, or `distribution/mcp`.
5. Run final code review and report commands with exit codes.

## Planned Verification Commands

```bash
rg -n "Smoke|LLMSmoke|ProductionH2|ContainerStdioSmoke|PackagedDistributionSmoke" .github/workflows test/e2e/mcp/src/test/java
```

```bash
rg -n "checkstyle:check|spotless:check|apache-rat:check" .github/workflows/required-check.yml .github/workflows/jdk21-subchain-ci.yml
```

```bash
./mvnw -pl test/e2e/mcp -am test -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

```bash
./mvnw -pl distribution/mcp -am -DskipTests package -B -ntp
```

```bash
docker build -f distribution/mcp/Dockerfile -t shardingsphere-mcp-ci:local distribution/mcp/target
```

## Risks And Mitigations

- **Risk**: Deleting smoke too early loses topology coverage.
  **Mitigation**: Require a topology coverage map before removing any smoke invocation.
- **Risk**: H2 demo assets are confused with E2E evidence.
  **Mitigation**: Keep docs and workflow selectors explicit: MySQL is real E2E, H2 is not production E2E evidence.
- **Risk**: Distribution E2E becomes too large.
  **Mitigation**: Keep one job but organize suite internals by packaged HTTP, packaged STDIO, plugin, container HTTP, and container STDIO scenarios.
- **Risk**: Release workflow and PR CI share too much logic without clear artifact target.
  **Mitigation**: Common helpers must take explicit `local` or `published` artifact references.
- **Risk**: Release publisher download remains mutable.
  **Mitigation**: Pin version and checksum or use an equivalent integrity verification step.
- **Risk**: Published runtime validation is overstated for arm64.
  **Mitigation**: Treat native runtime validation and multi-platform manifest validation as separate claims; add QEMU arm64 runtime only if explicitly accepted later.
- **Risk**: H2 remains enabled by default and is mistaken for production E2E.
  **Mitigation**: Change, rename, or document the H2 flag so it is lightweight-only after reclassification.
