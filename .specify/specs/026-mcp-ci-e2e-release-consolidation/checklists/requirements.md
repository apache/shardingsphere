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

# Requirements Checklist: MCP CI E2E And Release Consolidation

## Scope Safety

- [x] New `.specify/specs/026-mcp-ci-e2e-release-consolidation/` package exists.
- [x] Existing `.specify/specs/024-mcp-github-actions-e2e-hardening/` is not modified by this package.
- [x] New `specs/015-mcp-ci-e2e-release-consolidation/` mirror exists.
- [x] Existing `specs/014-mcp-github-actions-e2e-hardening/` is not modified by this package.
- [x] No implementation code or workflow changes are included in this design-only round.

## Requirement Quality

- [x] Requirements have no unresolved clarification markers.
- [x] Requirements describe final behavior rather than implementation guesses.
- [x] Smoke removal has a coverage-preservation prerequisite.
- [x] H2 cleanup preserves possible non-production test or demo usage.
- [x] Distribution E2E and release workflow have distinct artifact targets.
- [x] JDK 21 CI and Required Check have distinct responsibilities.

## Coverage Expectations

- [x] MySQL HTTP E2E target is required.
- [x] MySQL STDIO E2E target is required.
- [x] LLM usability must become a superset before LLM smoke is removed.
- [x] Complete distribution E2E includes packaged HTTP, packaged STDIO, plugin discovery, container HTTP, and container STDIO.
- [x] Release validation includes published GHCR image pull, manifest inspection, runtime validation, and MCP Publisher integrity.

## Source And Review

- [x] Official MCP transport documentation is recorded.
- [x] Official GitHub Actions documentation is recorded.
- [x] Official GHCR documentation is recorded.
- [x] Official Maven Toolchains documentation is recorded.
- [x] Doubt review is recorded.
- [x] Follow-up reanalysis is recorded.
- [x] MCP builder review gate is recorded for future implementation touching MCP paths.

## Final Design Check

- [x] No open confirmation questions remain for this design package.
- [x] Next action is implementation planning or coding only after explicit user instruction.
