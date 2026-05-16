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

# Tasks: MCP Configuration Input Validation Alignment

**Input**: `.specify/specs/021-mcp-validation-framework-alignment/spec.md`, `plan.md`, and `scope-map.md`  
**Branch Rule**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.
**Scope Rule**: Only YAML configuration and direct counterpart configuration input validation are in scope.

## Phase 1 - Governance And Baseline

- [ ] T001 Confirm the active branch is `001-shardingsphere-mcp` before implementation work starts.
- [ ] T002 Re-scan MCP bootstrap configuration validation code and update `scope-map.md` if source lines or candidates changed.
- [ ] T003 Record the final migration disposition for every candidate in `scope-map.md`.

## Phase 2 - Direct YAML Configuration Constraint Migration

- [ ] T004 Add or verify launch YAML DTO constraints for root configuration, transport, and runtime database collection requirements.
- [ ] T005 Add or verify transport YAML DTO constraints for required HTTP and STDIO sections.
- [ ] T006 Add or verify HTTP transport YAML DTO constraints for host, port, endpoint path, and OAuth cache TTL.
- [ ] T007 Add STDIO DTO validation before swapping.
- [ ] T008 Add or verify runtime database YAML DTO constraints for database type, JDBC URL, username, password, and driver class name.
- [ ] T009 Remove duplicate direct structural checks from configuration swappers after each equivalent validation test is present.

## Phase 3 - Configuration DTO Adjustments

- [ ] T010 Add typed map-value validation or a focused custom validator for `runtimeDatabases`.
- [ ] T011 Preserve existing external YAML map shape for runtime database entries.
- [ ] T012 Preserve unsupported runtime database property rejection if DTO typing changes the current map handling.

## Phase 4 - Counterpart Configuration Input Validation

- [ ] T013 Add class-level validation for exactly one enabled transport in `MCPLaunchConfiguration`.
- [ ] T014 Add resolved HTTP configuration validation for remote access, loopback binding, allowed origins, and authorization requirements.
- [ ] T015 Add resolved HTTP configuration validation for bearer token and OAuth introspection conflicts.
- [ ] T016 Add resolved authorization server validation for required HTTPS URLs.
- [ ] T017 Add OAuth introspection consistency validation for endpoint, client ID, client secret, cache TTL, and expected issuer.
- [ ] T018 Retain or centralize `RuntimeDatabaseConfiguration` constructor input validation for non-YAML configuration input.

## Phase 5 - Explicit Non-Scope Guardrails

- [ ] T019 Confirm descriptor catalog and descriptor YAML validation are not changed as part of this requirement.
- [ ] T020 Confirm registry metadata validation is not changed as part of this requirement.
- [ ] T021 Confirm HTTP header, session, request authorization, and transport lifecycle validation are not changed as part of this requirement.

## Phase 6 - Tests

- [ ] T022 Add invalid-input tests for every direct YAML configuration migration.
- [ ] T023 Add invalid and valid neighboring-case tests for every class-level counterpart configuration constraint.
- [ ] T024 Add YAML compatibility tests for runtime database DTO shape adjustments.
- [ ] T025 Update existing configuration swapper and launch configuration tests to assert the new validation layer.

## Phase 7 - Verification And Handoff

- [ ] T026 Run module-scoped MCP tests for touched modules.
- [ ] T027 Run module-scoped Checkstyle for touched modules.
- [ ] T028 Run module-scoped Spotless checks for touched modules.
- [ ] T029 Confirm no branch switch occurred during the work.
- [ ] T030 Report commands, exit codes, changed files, retained out-of-scope checks, and remaining risks.
