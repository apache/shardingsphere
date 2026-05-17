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

# Tasks: MCP Registry Release Validation

**Input**: `.specify/specs/022-mcp-registry-release-validation/spec.md`, `plan.md`, and `source-map.md`  
**Branch Rule**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.  
**Scope Rule**: Static release metadata validation only; no dynamic image inspection in this slice.

## Phase 1 - Governance And Baseline

- [x] T001 Confirm the active branch is `001-shardingsphere-mcp`.
- [x] T002 Re-read `AGENTS.md` and `CODE_OF_CONDUCT.md`.
- [x] T003 Record official MCP Registry, OCI, Maven Exec Plugin, and Spec Kit sources.
- [x] T004 Re-scan current `mcp/registry` command, tests, Dockerfile, and workflows.

## Phase 2 - Speckit Traceability

- [x] T005 Create this Speckit requirement package without branch-switching commands.
- [x] T006 Mirror the concise requirement summary under `specs/013-mcp-registry-release-validation/requirements.md`.

## Phase 3 - Static Dockerfile Validation

- [x] T007 Add optional `--dockerfile-path` parsing to `MCPRegistryMetadataCommand`.
- [x] T008 Add static validation for `ARG MCP_SERVER_NAME=<server.json name>`.
- [x] T009 Add static validation for `io.modelcontextprotocol.server.name="${MCP_SERVER_NAME}"`.
- [x] T010 Add static validation for `ARG MCP_IMAGE_VERSION=unknown`.
- [x] T011 Add static validation for `org.opencontainers.image.version="${MCP_IMAGE_VERSION}"`.
- [x] T012 Keep validation disabled when `--dockerfile-path` is not supplied, preserving existing command behavior.

## Phase 4 - Tests

- [x] T013 Add a valid Dockerfile metadata scenario.
- [x] T014 Add missing `--dockerfile-path` value coverage.
- [x] T015 Add mismatched `MCP_SERVER_NAME` coverage.
- [x] T016 Add missing MCP Registry ownership label coverage.
- [x] T017 Add missing image version argument coverage.
- [x] T018 Add missing OCI image version label coverage.
- [x] T019 Add source metadata validation for real `mcp/server.json` plus `distribution/mcp/Dockerfile`.

## Phase 5 - Workflow Integration

- [x] T020 Replace JDK 21 subchain shell `grep` Dockerfile checks with `MCPRegistryMetadataCommand --dockerfile-path`.
- [x] T021 Add Dockerfile validation to release metadata preparation.
- [x] T022 Add Dockerfile validation to release metadata validation.
- [x] T023 Confirm dynamic image label inspection remains absent.

## Phase 6 - Verification And Handoff

- [x] T024 Run `mcp/registry` unit tests.
- [x] T025 Run module-scoped Spotless.
- [x] T026 Run module-scoped Checkstyle.
- [x] T027 Run static release metadata validation against the real Dockerfile.
- [x] T028 Run mcp-builder review of MCP design and implementation reasonableness.
- [x] T029 Run doubt-driven self-review and classify findings.
- [x] T030 Confirm no unresolved questions require user confirmation before final handoff.
