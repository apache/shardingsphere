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

# Feature Specification: MCP CI E2E And Release Consolidation

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-17
**Status**: Draft
**Input**: User requested a new non-conflicting Spec Kit package for the agreed MCP GitHub Actions, E2E, and release validation design.

## Branch Constraint

- Work MUST stay on the existing `001-shardingsphere-mcp` branch.
- Branch creation, branch switching, committing, pushing, and staging are out of scope for this specification package.
- This package is documentation-only until the user gives an explicit implementation command.
- This package intentionally does not modify `.specify/specs/024-mcp-github-actions-e2e-hardening/`.

## Goal

Consolidate MCP GitHub Actions and MCP E2E coverage so the target state is easier to reason about:

1. Full E2E suites replace standalone smoke targets.
2. Real MCP E2E uses MySQL-backed runtimes, not H2-backed production E2E.
3. Distribution validation becomes one complete distribution E2E path.
4. JDK 21 CI validates Java 21 MCP behavior without duplicating Required Check style gates.
5. Release workflow validates the artifact after it is published to GHCR and the MCP Registry.

## User Scenarios & Testing

### User Story 1 - Replace Smoke With Complete MySQL E2E (Priority: P1)

As an MCP maintainer, I want CI to run complete MySQL-backed HTTP and STDIO E2E suites instead of standalone smoke suites, so smoke-only coverage cannot hide real transport or backend gaps.

**Why this priority**: The user explicitly rejects smoke as an E2E target state. Smoke can only disappear after its topology coverage is absorbed into a complete E2E suite.

**Independent Test**: Inspect MCP E2E workflow selectors and test suites; no workflow invokes smoke-only MCP or LLM E2E classes, and MySQL HTTP plus MySQL STDIO scenarios cover the previous smoke topology.

**Acceptance Scenarios**:

1. **Given** an existing smoke case covers MySQL HTTP, **When** implementation finishes, **Then** an equivalent or stronger MySQL HTTP full E2E scenario exists outside a smoke-only suite.
2. **Given** an existing smoke case covers MySQL STDIO, **When** implementation finishes, **Then** an equivalent or stronger MySQL STDIO full E2E scenario exists outside a smoke-only suite.
3. **Given** an existing smoke case covers H2 HTTP or H2 STDIO, **When** implementation finishes, **Then** it is either removed from real E2E or moved to a lightweight unit/integration layer that is not treated as production E2E evidence.
4. **Given** LLM usability covers the needed topology, **When** LLM workflows run, **Then** the workflow invokes the complete usability suite rather than `LLMSmokeE2ETest`.
5. **Given** LLM smoke has been absorbed, **When** CI target state is evaluated, **Then** no standalone LLM smoke workflow entry remains.
6. **Given** smoke classes remain temporarily during migration, **When** CI target state is evaluated, **Then** they are not workflow entry points and have an explicit deletion or rename task.

---

### User Story 2 - Build One Complete Distribution E2E (Priority: P1)

As a release maintainer, I want a single complete distribution E2E path that packages MCP once and validates packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO.

**Why this priority**: Splitting distribution smoke and container smoke makes it unclear which artifact was actually validated.

**Independent Test**: Inspect `jdk21-subchain-ci.yml` and release validation steps; distribution packaging is shared, and a single distribution E2E suite validates the package and locally built container image end to end.

**Acceptance Scenarios**:

1. **Given** `distribution/mcp` is packaged, **When** distribution E2E runs, **Then** the same package home is used for packaged HTTP and packaged STDIO tests.
2. **Given** a plugin fixture is present, **When** the packaged distribution starts, **Then** plugin discovery is validated from the packaged layout.
3. **Given** a local Docker image is built from the packaged distribution, **When** distribution E2E runs, **Then** container HTTP and container STDIO are both validated.
4. **Given** a distribution test needs a backend database, **When** it is real E2E, **Then** it uses MySQL rather than H2.
5. **Given** the workflow completes, **When** maintainers inspect job names, **Then** there is no separate distribution smoke or STDIO container smoke job.

---

### User Story 3 - Keep JDK 21 CI Focused (Priority: P1)

As a CI maintainer, I want JDK 21 CI to prove Java 21 MCP compilation, tests, distribution, and runtime behavior without duplicating repo-wide style checks already owned by Required Check.

**Why this priority**: MCP uses a Java 21 subchain while the broader repository still has non-JDK21 CI. The value is Java 21 runtime/build validation, not duplicate Checkstyle/RAT/Spotless work.

**Independent Test**: Inspect `required-check.yml`, root reactor module inclusion, and `jdk21-subchain-ci.yml`; Required Check owns Checkstyle, Spotless, and RAT, while JDK 21 CI owns MCP Java 21 build and E2E.

**Acceptance Scenarios**:

1. **Given** `required-check.yml` runs, **When** the repository contains MCP modules in the default reactor, **Then** Checkstyle, Spotless, and RAT are already covered by Required Check.
2. **Given** JDK 21 CI runs, **When** MCP subchain is built, **Then** it uses Java 21 and validates MCP-specific runtime and distribution paths.
3. **Given** a style gate is considered for JDK 21 CI, **When** Required Check already covers the same files, **Then** the gate is not added unless a new uncovered path is identified.
4. **Given** a Java 21-only MCP dependency or compiler setting changes, **When** Required Check passes, **Then** JDK 21 CI is still required because Required Check does not prove Java 21 subchain runtime behavior.

---

### User Story 4 - Verify Published GHCR Artifacts (Priority: P1)

As a release maintainer, I want the release workflow to validate the published GHCR image and MCP Registry publication, not only the local build artifact before push.

**Why this priority**: A local image can pass while the pushed tag, digest, manifest, or registry metadata is broken.

**Independent Test**: Inspect `.github/workflows/mcp-build.yml`; after push, the workflow pulls the published image by tag or digest, verifies the manifest platforms, runs an MCP runtime check, and validates MCP Registry publication inputs.

**Acceptance Scenarios**:

1. **Given** a release image is pushed to `ghcr.io/apache/shardingsphere-mcp`, **When** post-push validation runs, **Then** the workflow pulls the published image by the release tag or resolved digest.
2. **Given** the release builds linux/amd64 and linux/arm64 images, **When** manifest validation runs, **Then** both platforms are present.
3. **Given** the pulled published image starts, **When** MCP validation runs, **Then** it validates published runtime behavior rather than only local build behavior.
4. **Given** `mcp-publisher` is downloaded, **When** release workflow runs, **Then** the download is pinned or integrity-checked rather than blindly using an unverified latest archive.
5. **Given** MCP Registry publish completes, **When** evidence is reviewed, **Then** the workflow records which server identifier and image reference were published.
6. **Given** the release runner is linux/amd64, **When** published runtime validation runs, **Then** native runtime validation proves the pulled amd64 image while manifest inspection proves that the arm64 image was published.

---

### User Story 5 - Preserve Release And Distribution Boundaries (Priority: P2)

As a maintainer, I want local distribution E2E and release validation to share helpers where useful while keeping their artifact targets distinct.

**Why this priority**: The two workflows have overlapping commands but different failure meanings.

**Independent Test**: Inspect the design and tasks; local distribution E2E validates local package/image output, while release workflow validates the published GHCR image and Registry publication.

**Acceptance Scenarios**:

1. **Given** distribution E2E runs in PR CI, **When** it validates MCP runtime, **Then** it uses local artifacts from the current commit.
2. **Given** release workflow runs after release publication, **When** it validates MCP runtime, **Then** it uses the pushed GHCR tag or digest.
3. **Given** common scripts are introduced, **When** they are used by both workflows, **Then** their inputs make the artifact target explicit.
4. **Given** release-only credentials are unavailable in PR CI, **When** distribution E2E runs, **Then** it does not require GHCR write or MCP Registry publish permissions.

## Edge Cases

- Do not delete smoke coverage before a full MySQL HTTP/STDIO replacement exists.
- Do not treat H2-backed production E2E as real E2E evidence; H2 may remain only for lightweight local/unit integration or sample configuration if not used as the production E2E backend.
- Do not leave `mcp.e2e.production.h2.enabled=true` as an implied production E2E default after H2 is reclassified; either rename the flag, change the default, or document it as lightweight-only.
- Do not add duplicate JDK 21 style gates while Required Check still covers MCP files without path filters.
- Do not collapse release validation into distribution E2E; release must validate the published GHCR artifact.
- Do not make LLM E2E non-required by hiding failures; non-required status belongs to branch protection, not success-only workflow behavior.
- Do not edit generated `target/` content.
- Do not change MCP protocol behavior as part of this CI/E2E consolidation unless an implementation task explicitly requires it.

## Requirements

### Functional Requirements

- **MCE-FR-001**: The specification MUST live in a new non-conflicting Spec Kit package and MUST NOT overwrite `.specify/specs/024-mcp-github-actions-e2e-hardening/`.
- **MCE-FR-002**: Future implementation MUST keep all work on `001-shardingsphere-mcp` unless the user explicitly changes that instruction.
- **MCE-FR-003**: Future implementation MUST replace workflow-invoked MCP smoke targets with complete E2E suites.
- **MCE-FR-004**: Future implementation MUST absorb existing smoke topology coverage before deleting or de-targeting smoke tests.
- **MCE-FR-005**: Real MCP E2E MUST use MySQL-backed runtime coverage for HTTP and STDIO.
- **MCE-FR-006**: H2-backed production E2E MUST be removed from real E2E workflows or reclassified as lightweight non-production validation.
- **MCE-FR-007**: LLM usability E2E MUST cover the topology currently protected by LLM smoke before LLM smoke is removed from workflow invocation.
- **MCE-FR-008**: The final LLM E2E workflow target MUST NOT include a standalone smoke workflow entry; it SHOULD use one complete usability suite unless a documented scheduling or resource boundary requires a second non-smoke workflow.
- **MCE-FR-009**: Distribution E2E MUST package `distribution/mcp` once and validate packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO from that artifact set.
- **MCE-FR-010**: Distribution E2E MUST use MySQL for real backend validation.
- **MCE-FR-011**: JDK 21 CI MUST validate Java 21 MCP subchain compilation, tests, distribution packaging, and MCP runtime behavior.
- **MCE-FR-012**: JDK 21 CI MUST NOT duplicate Checkstyle, Spotless, or RAT gates while Required Check already covers the MCP modules and files.
- **MCE-FR-013**: Required Check coverage for MCP MUST be re-verified before deciding not to add a style gate.
- **MCE-FR-014**: Release workflow MUST validate published GHCR image references after push, not only local pre-push artifacts.
- **MCE-FR-015**: Release workflow MUST verify the multi-platform manifest contains linux/amd64 and linux/arm64 when both are pushed.
- **MCE-FR-016**: Release workflow MUST run a published-image MCP runtime validation after pulling the pushed tag or digest on the native runner platform.
- **MCE-FR-017**: Release workflow MUST pin or integrity-check the MCP Publisher artifact download.
- **MCE-FR-018**: Release workflow MUST keep evidence for version, image tag, digest or manifest, and MCP Registry server identifier.
- **MCE-FR-019**: Workflow names and job names MUST remain unique and compatible with repository GitHub Action standards.
- **MCE-FR-020**: Implementation touching `mcp`, `test/e2e/mcp`, or `distribution/mcp` MUST receive an MCP builder design review before final handoff.
- **MCE-FR-021**: Implementation MUST report verification commands with exit codes.

### Key Entities

- **Full MySQL E2E Suite**: Complete MCP runtime scenarios using MySQL and covering HTTP plus STDIO transports.
- **Smoke Topology Coverage**: Backend and transport combinations currently protected only by smoke-named tests.
- **Complete Distribution E2E**: A single artifact-centered validation path for packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO.
- **JDK 21 Subchain CI**: CI lane proving Java 21 MCP build/runtime behavior.
- **Required Check**: Repo-wide Checkstyle, Spotless, and RAT workflow.
- **GHCR Published Artifact**: The pushed `ghcr.io/apache/shardingsphere-mcp:<version>` image tag and its digest or manifest.

## Success Criteria

### Measurable Outcomes

- **MCE-SC-001**: No MCP E2E workflow invokes a smoke-only test class as the final target state.
- **MCE-SC-002**: MySQL HTTP and MySQL STDIO scenarios cover every previously workflow-protected smoke topology.
- **MCE-SC-003**: H2-backed production E2E tests are not used as CI evidence for real MCP E2E.
- **MCE-SC-004**: Distribution E2E uses one packaged artifact set and validates packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO.
- **MCE-SC-005**: JDK 21 CI contains no duplicate Checkstyle, Spotless, or RAT gate when Required Check coverage is confirmed.
- **MCE-SC-006**: Release workflow validates the published GHCR image after push by tag or digest and confirms linux/amd64 plus linux/arm64 manifest entries.
- **MCE-SC-007**: Release workflow validates or pins the MCP Publisher binary source.
- **MCE-SC-008**: All implementation verification commands and exit codes are reported.
- **MCE-SC-009**: Any remaining H2 validation is clearly classified outside real production E2E evidence.

## Assumptions

- Current branch remains `001-shardingsphere-mcp`.
- MySQL is the accepted real E2E backend for this design.
- Required Check remains repo-wide and not path-filtered.
- GHCR means GitHub Container Registry, hosted at `ghcr.io`.
- The release workflow may reuse distribution E2E helpers, but it must validate the published artifact rather than the local one.
