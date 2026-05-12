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

# Tasks: MCP Scorecard Perfect 100

**Input**: `.specify/specs/012-mcp-scorecard-perfect-100/spec.md`, `plan.md`, `scorecard.md`, `reanalysis.md`, `evidence-ledger.md`
**Prerequisites**: Latest independent review baseline and repository rules.
**Tests**: Required for any Java or E2E implementation that claims a score improvement.

**Constraint**: Do not run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.

## Phase 1: Score Governance

- [x] T001 Add evidence columns or linked records to `scorecard.md` before marking any dimension as 100.
- [x] T002 Define the exact command or artifact required for each production module dimension below 100.
- [x] T003 Define the exact command or artifact required for each MCP E2E dimension below 100.
- [x] T004 Record open-risk format for missing evidence; open risks keep dimensions below 100.
- [x] T005 Revalidate historical evidence from `011-mcp-llm-product-quality-100` before reusing it.

## Phase 2: MCP Production Module Gaps

- [x] T010 [P] Improve model-use friendliness by reducing descriptor duplication and adding zero-guessing discovery evidence.
- [x] T011 [P] Improve natural interaction by hiding low-level SQL, DistSQL, and artifact concepts where guided paths can carry intent.
- [x] T012 [P] Improve clarity by adding compact first-read documentation and model-first summaries.
- [x] T013 Refactor oversized production classes while preserving public model-facing contracts.
- [x] T014 Reduce static registry and hardcoded context coupling, or document a bounded extension gate with tests.
- [x] T015 Add protocol conformance-style evidence for HTTP, STDIO, sessions, tools, resources, prompts, and completions.
- [x] T016 Add stability tests for SQL scanner, runtime cleanup, session/workflow lifecycle, and negative runtime states.
- [x] T017 Strengthen diagnostics without allowing recovery builders to become unbounded policy sinks.
- [x] T018 Strengthen safety evidence for approval, authentication, audit assumptions, and SQL classification.
- [x] T019 Add performance measurements or budgets for descriptor generation, metadata lookup, request scope creation, and SQL execution.
- [x] T020 Improve distribution and compatibility evidence for Java 21, Proxy-only scope, dialect coverage, and packaged runtime.
- [x] T021 Run scoped production tests, Checkstyle, Spotless, and coverage/Jacoco where needed before any production dimension reaches 100.

## Phase 3: MCP E2E Gaps

- [x] T030 [P] Make live LLM usability evidence repeatable, artifact-backed, and separated from harness recovery.
- [x] T031 [P] Expand natural-language scenario coverage for metadata, read-only SQL, workflow preview, and recovery.
- [x] T032 [P] Add a single reader-facing E2E lane matrix that explains default and opt-in coverage.
- [x] T033 Refactor large LLM conversation and runtime support classes into smaller testable components.
- [x] T034 Isolate Docker, Ollama, MySQL, STDIO, and distribution assumptions behind fixture configuration.
- [x] T035 Add or justify conformance-style protocol evidence for E2E contracts.
- [x] T036 Record mandatory MySQL, STDIO, distribution, packaged runtime, and LLM evidence for the perfect gate.
- [x] T037 Remove or bound flake sources from polling, Docker readiness, model readiness, and packaged runtime startup.
- [x] T038 Improve artifact and trace diagnostics so failed E2E runs produce reviewer-ready summaries.
- [x] T039 Add safety evidence for per-user auth, abuse, rate, and external-model assumptions.
- [x] T040 Define time and resource budgets for full E2E and LLM lanes, then prove they pass.
- [x] T041 Strengthen E2E assertions and golden contract drift checks before any E2E dimension reaches 100.

## Phase 4: Final Verification

- [x] T050 Update `scorecard.md` with evidence for every dimension.
- [x] T051 Confirm all production dimensions are 100/100.
- [x] T052 Confirm all E2E dimensions are 100/100.
- [x] T053 Run the narrowest full MCP production plus E2E command set required by the evidence model.
- [x] T054 Run relevant Checkstyle and Spotless gates for touched modules.
- [x] T055 Update `quickstart.md` and repo-visible `specs/008-mcp-scorecard-perfect-100/requirements.md` with final verification.
- [x] T056 Verify current git branch is still `001-shardingsphere-mcp`.

## Dependencies

- Phase 1 blocks all score updates.
- Production and E2E implementation phases may proceed in parallel if they touch disjoint files.
- Final verification depends on all dimensions having evidence.
