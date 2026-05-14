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

## Phase 5: Source-Driven Official MCP Standard Gate

- [x] T060 Record official MCP source mapping for lifecycle, transports, authorization, tools, resources, prompts, completion, pagination, errors, and security.
- [x] T061 Audit all public protocol surfaces and reject any ShardingSphere-only field or resource that is treated as MCP protocol semantics.
- [x] T062 Align official discovery and invocation flows to tools, resources, prompts, and completion MCP methods.
- [x] T063 Prove official MCP list pagination uses opaque `cursor` request values and `nextCursor` response values.
- [x] T064 Add or update tool output contracts so structured results use `outputSchema`, `structuredContent`, and text fallback where applicable.
- [x] T065 Separate JSON-RPC protocol errors from tool execution errors and prove tool execution errors use standard `isError` results.
- [x] T066 Verify MCP Java SDK `1.1.2` API compatibility for every SDK pattern before code changes; isolate any SDK-gap adapter in bootstrap or integration boundaries.
- [x] T067 Implement or document the scoped deployment-level HTTP authorization mode with protected resource metadata, header-only bearer token handling,
  resource-scoped bearer-token acceptance, and no token passthrough.
- [x] T068 Prove Streamable HTTP `Origin` validation, local binding expectations, and safe authentication behavior in contract or E2E tests.
- [x] T069 Add official resource template, prompt, and completion E2E coverage or keep related standard-first dimensions below 100.
- [x] T070 Create mcp-builder evaluation XML with ten read-only, independent, complex, realistic, verifiable, and stable questions.
- [x] T071 Update `protocol-evidence-matrix.md`, `evidence-ledger.md`, and `scorecard.md` only after each standard-first gate has command or artifact evidence.
- [x] T072 Run scoped Maven tests, Checkstyle, Spotless, and standard-focused contract commands for every touched MCP and E2E module.

## Phase 6: Complete OAuth Token Validation Gate

- [x] T080 Record official OAuth resource-server sources: MCP Authorization 2025-11-25, RFC 6750, RFC 7662, RFC 8707, and RFC 8414.
- [x] T081 Design the OAuth introspection configuration shape without branch switching, including introspection endpoint, issuer, protected resource, required scopes,
  client authentication secret handling, cache TTL, and fail-closed behavior.
- [x] T082 Replace static bearer-token authorization with a standard OAuth token validator while preserving local no-auth mode and avoiding token passthrough.
- [x] T083 Remove or bypass duplicate static-token enforcement in the SDK security-validator path after OAuth validation succeeds.
- [x] T084 Implement RFC 6750/MCP-compliant challenges: `401 invalid_token` for invalid or unverifiable tokens and `403 insufficient_scope` for active tokens missing required scopes.
- [x] T085 Add focused unit tests for missing bearer, malformed bearer, inactive token, expired token, not-yet-valid token, wrong issuer, wrong audience/resource,
  insufficient scope, introspection server error, malformed introspection response, and valid token.
- [x] T086 Add MCP E2E coverage with a local fake introspection endpoint and prove initialization plus session requests use validated OAuth tokens.
- [x] T087 Update README, README_ZH, protocol evidence, scorecard, evidence ledger, and mcp-builder artifacts only after passing implementation evidence exists.
- [x] T088 Run scoped Maven tests plus Checkstyle and Spotless for `mcp/bootstrap`, `mcp/core`, `mcp/support`, and `test/e2e/mcp`.

### Package 016 Cross-Link

- Package 016 T023 revalidated that inactive, expired, wrong issuer, introspection failure, challenge, and no-token-passthrough evidence remains owned here
  by T080-T088 and must not be duplicated inside package 016.

## Dependencies

- Phase 1 blocks all score updates.
- Production and E2E implementation phases may proceed in parallel if they touch disjoint files.
- Final verification depends on all dimensions having evidence.
- Phase 5 reopens completion and blocks any new perfect-100 claim until official MCP standard evidence is current.
