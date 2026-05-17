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

# Feature Specification: MCP Registry Release Validation

**Feature Branch**: `001-shardingsphere-mcp`  
**Created**: 2026-05-17  
**Status**: Draft  
**Input**: Keep MCP Registry publication metadata validation in the dedicated `mcp/registry` module and add a static Dockerfile metadata gate.
Do not switch branches.

## Branch Constraint

- Work must remain on the existing `001-shardingsphere-mcp` branch.
- Speckit branch creation or branch switching commands are forbidden for this requirement package.
- This specification is manually maintained under `.specify/specs/022-mcp-registry-release-validation/` and mirrored by `specs/013-mcp-registry-release-validation/requirements.md`.

## Clarifications

### Session 2026-05-17

- Scope includes release-time validation for `mcp/server.json` and `distribution/mcp/Dockerfile`.
- Scope excludes dynamic post-build image inspection in this slice.
- Scope excludes moving release validation back into production runtime modules.
- The static Dockerfile gate must be runnable in GitHub Actions before container image push.

## User Scenarios & Testing

### User Story 1 - Registry publication metadata stays outside runtime code (Priority: P1)

As a release maintainer, I want MCP Registry metadata preparation and validation to live in a release-check module, so production MCP runtime code does not contain publication-only command logic.

**Independent Test**: Run `MCPRegistryMetadataCommand` from `mcp/registry` through Maven `exec:java` and verify it validates `mcp/server.json` without requiring packaged distribution runtime classes.

**Acceptance Scenarios**:

1. Given the source `mcp/server.json`, when validation runs in snapshot mode, then the command accepts the current snapshot metadata.
2. Given release version and OCI identifier arguments, when preparation runs, then `server.json` version and package metadata are rewritten consistently.
3. Given missing release arguments outside validation-only mode, when preparation runs, then the command fails before writing metadata.

### User Story 2 - Dockerfile publication labels are statically guarded (Priority: P1)

As a release maintainer, I want CI to fail before image push when the Dockerfile cannot produce MCP Registry ownership metadata or ShardingSphere OCI version metadata.

**Independent Test**: Run `MCPRegistryMetadataCommand --validate-only --dockerfile-path distribution/mcp/Dockerfile` and verify the command checks Dockerfile metadata against `mcp/server.json`.

**Acceptance Scenarios**:

1. Given `mcp/server.json` name `io.github.apache/shardingsphere-mcp`, when Dockerfile validation runs, then `ARG MCP_SERVER_NAME=io.github.apache/shardingsphere-mcp` is required.
2. Given a Dockerfile label block, when validation runs, then `io.modelcontextprotocol.server.name="${MCP_SERVER_NAME}"` is required.
3. Given ShardingSphere release image metadata conventions, when validation runs, then `ARG MCP_IMAGE_VERSION=unknown` and `org.opencontainers.image.version="${MCP_IMAGE_VERSION}"` are required.

### User Story 3 - Reviewable release scope (Priority: P2)

As a reviewer, I want release-validation requirements and sources to be traceable without changing runtime behavior or adding unrequested image inspection.

**Independent Test**: Compare the implementation and workflows against this specification and verify every release gate maps to one requirement and one scoped verification command.

**Acceptance Scenarios**:

1. Given the GitHub Actions release workflow, when metadata validation runs, then the Java command validates both `server.json` and the Dockerfile before registry publication.
2. Given the JDK 21 subchain CI workflow, when distribution validation runs, then shell `grep` checks are replaced by the same Java validator used by release.
3. Given a request not to do build-after real image label validation, when implementation is inspected, then no `docker inspect`, `imagetools inspect`, or registry pull-based label gate is added.

## Requirements

### Functional Requirements

- **FR-001**: The work MUST stay on branch `001-shardingsphere-mcp`; branch switching, branch creation, and Speckit commands that change branches MUST NOT be used.
- **FR-002**: MCP Registry metadata preparation and validation MUST remain in `mcp/registry`, not in MCP bootstrap/runtime production modules.
- **FR-003**: The release command MUST continue to validate the official `server.json` schema URL, fixed ShardingSphere MCP server name, concrete version, OCI identifier, package version, transports, HTTP URL, and required environment variables.
- **FR-004**: The release command MUST accept an optional Dockerfile path argument for static Dockerfile metadata validation.
- **FR-005**: Dockerfile validation MUST require the Dockerfile `MCP_SERVER_NAME` build argument to equal the validated `server.json` server name.
- **FR-006**: Dockerfile validation MUST require the MCP Registry ownership label `io.modelcontextprotocol.server.name` to be sourced from `${MCP_SERVER_NAME}`.
- **FR-007**: Dockerfile validation MUST require the ShardingSphere image version build argument `MCP_IMAGE_VERSION` to default to `unknown`.
- **FR-008**: Dockerfile validation MUST require the OCI image version label `org.opencontainers.image.version` to be sourced from `${MCP_IMAGE_VERSION}`.
- **FR-009**: Static validation MUST fail with actionable error messages before Docker image push when any required Dockerfile metadata is missing or mismatched.
- **FR-010**: This slice MUST NOT add dynamic post-build image label inspection.
- **FR-011**: GitHub Actions MUST invoke the same Java validator instead of duplicating Dockerfile metadata checks in shell.
- **FR-012**: Tests MUST cover the valid Dockerfile path and each required Dockerfile metadata failure path.
- **FR-013**: Implementation MUST avoid broad refactors, dependency upgrades, generated-directory edits, and unrelated MCP behavior changes.

### Key Entities

- **MCP Registry Metadata Command**: The release-check CLI that prepares and validates `mcp/server.json`.
- **Dockerfile Metadata Gate**: Static validation of Dockerfile build arguments and labels required for MCP Registry ownership and OCI version metadata.
- **MCP Server Name**: The official registry name `io.github.apache/shardingsphere-mcp`.
- **OCI Identifier**: The `ghcr.io/apache/shardingsphere-mcp:<tag>` package identifier written into `server.json`.

## Scope

### In Scope

- `mcp/registry` command-line validation.
- `mcp/server.json` publication metadata shape.
- `distribution/mcp/Dockerfile` static metadata checks.
- GitHub Actions release and JDK 21 subchain validation steps.
- Targeted unit tests for command behavior and Dockerfile metadata failures.

### Out of Scope

- Branch switching, branch creation, commits, pushes, or destructive git operations.
- Dynamic post-build Docker image inspection.
- MCP runtime launch behavior, transports, tool/resource/prompt catalogs, and e2e runtime behavior.
- Dependency upgrades or module restructuring beyond the existing `mcp/registry` boundary.

## Success Criteria

- **SC-001**: `MCPRegistryMetadataCommand` validates both source `server.json` and Dockerfile metadata when `--dockerfile-path` is supplied.
- **SC-002**: Release and CI workflows use the Java validator for Dockerfile metadata instead of ad hoc shell checks.
- **SC-003**: Unit tests reject missing or mismatched Dockerfile server name and image version metadata.
- **SC-004**: Scoped `mcp/registry` tests, Spotless, Checkstyle, and static release validation pass.
