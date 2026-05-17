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

# Tasks: MCP CI E2E And Release Consolidation

**Input**: `.specify/specs/026-mcp-ci-e2e-release-consolidation/spec.md`, `plan.md`, `source-map.md`, `doubt-review.md`, and `implementation-review.md`
**Branch Rule**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.
**Current Rule**: User has authorized implementation; keep branch unchanged and do not stage or commit.

## Phase 1 - New Speckit Baseline

- [x] T001 Create new `.specify/specs/026-mcp-ci-e2e-release-consolidation/` package without modifying `.specify/specs/024-mcp-github-actions-e2e-hardening/`.
- [x] T002 Create new `specs/015-mcp-ci-e2e-release-consolidation/` mirror without modifying `specs/014-mcp-github-actions-e2e-hardening/`.
- [x] T003 Record branch, no-code, no-stage, no-commit, and no-generated-target constraints.
- [x] T004 Record official source constraints for MCP transports, GitHub Actions/GHCR, and Maven Toolchains.
- [x] T005 Record design doubt review and current confirmation status.
- [x] T005A Record reanalysis for release platform scope, H2 defaults, Required Check evidence, and LLM smoke workflow removal.

## Phase 2 - Inventory Before Implementation

- [x] T006 List workflow-invoked smoke tests in `.github/workflows/jdk21-subchain-ci.yml`, `.github/workflows/mcp-build.yml`, `.github/workflows/mcp-llm-e2e.yml`, and `.github/workflows/mcp-llm-usability-e2e.yml`.
- [x] T007 Map each smoke invocation to backend and transport topology.
- [x] T008 Map each topology to a full MySQL HTTP or MySQL STDIO target scenario.
- [x] T009 Identify H2-backed production E2E workflow invocations to remove or reclassify.
- [x] T010 Re-verify Required Check covers MCP modules through repo-wide Checkstyle, Spotless, and RAT.
- [x] T010A Identify `mcp.e2e.production.h2.enabled` defaults or documentation that would still imply H2 is production E2E evidence.

## Phase 3 - Full MySQL MCP E2E

- [x] T011 Add or extend MySQL HTTP full E2E scenarios for current smoke-only coverage.
- [x] T012 Add or extend MySQL STDIO full E2E scenarios for current smoke-only coverage.
- [x] T013 Remove H2-backed production E2E invocations from real E2E workflows.
- [x] T014 Replace smoke class selectors with full suite selectors.
- [x] T015 Delete or rename smoke classes once all topology coverage is absorbed.

## Phase 4 - LLM E2E Consolidation

- [x] T016 Extend `LLMUsabilitySuiteE2ETest` or its scenario catalog to cover MySQL HTTP topology currently protected by `LLMSmokeE2ETest`.
- [x] T017 Extend `LLMUsabilitySuiteE2ETest` or its scenario catalog to cover MySQL STDIO topology currently protected by `LLMSmokeE2ETest`.
- [x] T018 Remove `LLMSmokeE2ETest` from workflow invocation after usability coverage is a superset.
- [x] T019 Remove any standalone LLM smoke workflow entry after usability coverage is a superset.
- [x] T019A Collapse final LLM E2E workflow shape to one complete suite unless a documented scheduling or resource boundary requires a second non-smoke workflow.
- [x] T020 Preserve LLM artifacts and visible workflow failures.

## Phase 5 - Complete Distribution E2E

- [x] T021 Package `distribution/mcp` once in JDK 21 CI.
- [x] T022 Validate packaged HTTP runtime from the packaged home.
- [x] T023 Validate packaged STDIO runtime from the packaged home.
- [x] T024 Validate packaged plugin discovery.
- [x] T025 Build a local MCP container image from the packaged output.
- [x] T026 Validate container HTTP runtime.
- [x] T027 Validate container STDIO runtime.
- [x] T028 Use MySQL-backed runtime data for real distribution E2E.
- [x] T029 Remove separate distribution smoke and STDIO container smoke jobs after the complete job exists.

## Phase 6 - JDK 21 CI Boundaries

- [x] T030 Keep Java 21 MCP compile/test/distribution/runtime validation in `jdk21-subchain-ci.yml`.
- [x] T031 Do not add Checkstyle, Spotless, or RAT jobs to JDK 21 CI while Required Check coverage is confirmed.
- [x] T032 Keep job names unique and workflow timeout values compatible with repository standards.
- [x] T033 Document why Required Check covers style/license while JDK 21 CI covers Java 21 behavior.

## Phase 7 - Release Workflow Hardening

- [x] T034 Keep local package and pre-push validation in `.github/workflows/mcp-build.yml`.
- [x] T035 Capture or resolve the pushed GHCR image digest.
- [x] T036 Pull the published `ghcr.io/apache/shardingsphere-mcp:<version>` image by tag or digest.
- [x] T037 Inspect the pushed manifest and verify linux/amd64 plus linux/arm64 entries.
- [x] T038 Run MCP runtime validation against the pulled published image on the native runner platform.
- [x] T038A Keep arm64 validation as manifest validation unless QEMU-based arm64 runtime validation is explicitly accepted later.
- [x] T039 Pin or checksum-verify the `mcp-publisher` download.
- [x] T040 Record release evidence for version, tag, digest or manifest, and MCP Registry server identifier.

## Phase 8 - Verification And Reviews

- [x] T041 Run focused Maven tests for touched MCP E2E suites.
- [x] T042 Run scoped module Checkstyle/Spotless locally for touched modules.
- [x] T043 Run workflow selector/static checks.
- [x] T044 Run Docker package/image validation where touched.
- [x] T045 Run MCP builder review if implementation touches `mcp`, `test/e2e/mcp`, or `distribution/mcp`.
- [x] T046 Run final code review and report commands with exit codes.

## Dependencies And Execution Order

- T006-T010 must finish before deleting or de-targeting any smoke tests.
- T011-T014 must finish before T015.
- T016-T018 must finish before LLM smoke workflow removal is considered complete.
- T021-T028 must finish before T029.
- T034-T040 are release-only and must keep PR distribution E2E independent of publish credentials.
- T041-T046 close the implementation and cannot be skipped without reporting why.
