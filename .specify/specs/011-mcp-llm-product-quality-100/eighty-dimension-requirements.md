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

# Eighty-Dimension Requirements: MCP Product Quality 100

## Purpose

This document turns the 80 independent review dimensions into Speckit requirements for the production MCP modules and the MCP E2E module.

Each item is an independent 100-point gate. Scores are not additive, weighted, or averaged when deciding whether the target has been reached.

Per-item implementation routes, evidence requirements, and current status are tracked in `eighty-dimension-traceability.md`.

## Branch And Scope Rules

- Work MUST remain on branch `001-shardingsphere-mcp`.
- Speckit branch creation scripts MUST NOT be run for this requirement sweep.
- `git switch`, `git checkout`, and any branch-changing command are forbidden for this sweep.
- In-scope production modules are `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features`, and `mcp/bootstrap`.
- In-scope E2E module is `test/e2e/mcp`.
- Generated directories such as `target/` are out of scope.
- A final 100 score MUST NOT be claimed until both MCP and MCP E2E pass every applicable requirement in this document.

## Latest Baseline

- Baseline date: 2026-05-09.
- Baseline branch: `001-shardingsphere-mcp`.
- Latest independent score average for MCP production modules: `78/100`.
- Latest independent score average for MCP E2E module: `76/100`.
- Current blocking evidence: `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true -Dtest=ToolHandlerRegistryTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp` exits with code `1`.
- Immediate blocker: `ToolHandlerRegistryTest` asserts stale field counts `[6, 5, 6, 3, 1]` while current descriptors expose `[6, 5, 7, 4, 1]`.
- LLM scoring risk: text recovery in `LLMMCPConversationRunner` can execute `execute_query` when the model wrote expected SQL text instead of making a native tool call.
- E2E stability risk: `LLMChatModelClient` readiness polling uses fixed `Thread.sleep(2000L)`.

## Global Full-Score Gates

- **Gate G1**: All required scoped unit tests, MCP E2E tests, Checkstyle, Spotless, and `git diff --check` pass with recorded exit codes.
- **Gate G2**: Contract tests assert names, field semantics, required flags, enum values, output schema, recovery shape, and safety semantics instead of brittle field counts.
- **Gate G3**: LLM scorecards separate native model behavior from harness recovery behavior.
- **Gate G4**: Default CI stays deterministic and external-paid-provider free; live LLM gates stay opt-in unless explicitly selected.
- **Gate G5**: Every safety, approval, recovery, diagnostic, and public contract requirement has executable evidence.

## User-Confirmed Recommended Defaults

- Every MPQ-080 item is mandatory for MCP production and MCP E2E unless an exception is explicitly recorded and approved.
- Automated evidence is required for a 100 score. Manual review is supporting evidence only.
- Backward compatibility can yield to clarity because the MCP contract is unreleased.
- The mandatory live model remains Dockerized Ollama `qwen3:1.7b`.
- Native model tool calls and harness recovery behavior are separate score dimensions.
- Extended LLM scenarios do not fail the suite for model-performance misses.
- Extended LLM scenarios do fail for deterministic harness, safety, contract, artifact, score-shape, and secret-leakage failures.
- Live LLM E2E stays opt-in for default PR CI.
- H2 and MySQL are mandatory dialect/runtime evidence.
- PostgreSQL and openGauss are optional evidence unless a touched change targets them.
- Standalone MCP runtime is mandatory evidence.
- Cluster or registry-governance compatibility is a recorded risk unless a touched change targets it.
- Refactoring is allowed in small reviewable slices when it reduces reading cost, contract drift, or test ambiguity.
- Final delivery includes code, tests, Speckit, scorecard, and usage or rollback documentation when behavior changes require handoff.

## P0 Product Requirements

- **MPQ-P0-001**: Fix stale MCP core contract tests so the current descriptor contract can be verified by semantic assertions.
- **MPQ-P0-002**: Re-run the JDK 21 MCP subchain after the blocking unit test is fixed and record command evidence.
- **MPQ-P0-003**: Split LLM native tool-call scoring from text-recovery scoring before using LLM usability scores as product evidence.
- **MPQ-P0-004**: Add hard assertions for unapproved side effects, malformed scorecards, missing artifacts, secret leakage, and contract drift.
- **MPQ-P0-005**: Treat every item in the 80-dimension list as independently required for both production MCP and MCP E2E unless a written exception is approved.

## Eighty Independent 100-Point Requirements

### Product, Clarity, And Safety

- **MPQ-080-001 Requirement understanding**: MCP and E2E reach 100 when the target user, model client, maintainer, and operator goals are explicit in specs and tests.
- **MPQ-080-002 Problem modeling**: MCP and E2E reach 100 when model discovery, SQL execution, workflow planning, approval, recovery, and diagnostics are modeled as separate concerns.
- **MPQ-080-003 Acceptance alignment**: MCP and E2E reach 100 when every acceptance scenario maps to executable unit, contract, E2E, or live LLM evidence.
- **MPQ-080-004 Goal alignment**: MCP and E2E reach 100 when all changes improve the MCP product surface rather than adding unrelated cleanup.
- **MPQ-080-005 User value**: MCP and E2E reach 100 when natural user tasks can be completed through MCP without scripted first-call hints.
- **MPQ-080-006 Result correctness**: MCP and E2E reach 100 when successful responses are validated against deterministic expected payloads or verified database state.
- **MPQ-080-007 Clarity**: MCP and E2E reach 100 when public payloads and test artifacts make the safe next action obvious without reading code.
- **MPQ-080-008 Explainability**: MCP and E2E reach 100 when every recovery, approval, and diagnostic response includes machine-readable codes plus human-readable guidance.
- **MPQ-080-009 Uncertainty handling**: MCP and E2E reach 100 when ambiguous database, schema, SQL, workflow, or runtime states ask for clarification or fail safe.
- **MPQ-080-010 Evidence sufficiency**: MCP and E2E reach 100 when scoring changes cite passing commands, artifacts, and contract snapshots.
- **MPQ-080-011 Verifiability**: MCP and E2E reach 100 when a reviewer can reproduce the score with documented commands and no manual inspection.
- **MPQ-080-012 Risk identification**: MCP and E2E reach 100 when security, compatibility, runtime, LLM variance, and CI-cost risks are tracked in tests or scorecard notes.
- **MPQ-080-013 Impact analysis**: MCP and E2E reach 100 when changes state the affected MCP layer, data-flow step, and downstream runtime or E2E impact.
- **MPQ-080-014 Boundary coverage**: MCP and E2E reach 100 when missing context, invalid enum, bad resource, SQL mismatch, stale workflow, and runtime failure boundaries are covered.
- **MPQ-080-015 Robustness**: MCP and E2E reach 100 when transient runtime or model-service readiness failures have bounded retries and useful failure artifacts.
- **MPQ-080-016 Error handling**: MCP and E2E reach 100 when every expected error class has a stable category, message, recovery path, and focused test.
- **MPQ-080-017 Security**: MCP and E2E reach 100 when banned SQL, unapproved side effects, token checks, origin checks, and unsafe workflow paths fail closed.
- **MPQ-080-018 Privacy**: MCP and E2E reach 100 when credentials, bearer tokens, raw environment values, and stack traces are redacted from diagnostics and artifacts.
- **MPQ-080-019 Consistency**: MCP and E2E reach 100 when vocabulary, field naming, response mode, status, and next-action shapes are consistent across tools and resources.
- **MPQ-080-020 Documentation and handoff**: MCP and E2E reach 100 when README, Speckit, scorecard, and tasks explain usage, limits, verification, and rollback.

### Large-Model Product Experience

- **MPQ-080-021 LLM friendliness**: MCP and E2E reach 100 when models can identify the safest first action from capabilities, descriptors, completions, and next actions.
- **MPQ-080-022 Naturalness**: MCP and E2E reach 100 when natural user prompts work without artificial tool-order instructions.
- **MPQ-080-023 Controllability**: MCP and E2E reach 100 when side effects require preview and explicit approval, and tests prove bypass attempts fail.
- **MPQ-080-024 Context management**: MCP and E2E reach 100 when database, schema, table, column, and workflow context can be discovered, completed, and recovered.
- **MPQ-080-025 Output stability**: MCP and E2E reach 100 when public payload schemas are snapshot protected and live LLM artifacts use deterministic metadata.
- **MPQ-080-026 Tool-call friendliness**: MCP and E2E reach 100 when tool definitions are generated from production descriptors or a shared contract source.
- **MPQ-080-027 Model safety**: MCP and E2E reach 100 when LLM scenarios cannot execute side effects unless explicit approval is part of the scenario.
- **MPQ-080-028 Prompt-injection defense**: MCP and E2E reach 100 when malicious or conflicting prompts cannot bypass SQL, workflow, approval, or resource boundaries.
- **MPQ-080-029 Evaluation-set quality**: MCP and E2E reach 100 when core and extended scenarios cover natural metadata, SQL, workflow, recovery, diagnostics, and safety.
- **MPQ-080-030 Model regression detection**: MCP and E2E reach 100 when scorecards detect drops in task success, native tool-call rate, recovery, and approval safety.
- **MPQ-080-031 Human handoff**: MCP and E2E reach 100 when responses clearly identify when a human approval, operator action, or manual workflow is required.
- **MPQ-080-032 Cost and latency**: MCP and E2E reach 100 when live LLM gates are opt-in, bounded, locally runnable, and do not slow default CI.

### Code And Architecture

- **MPQ-080-033 Code readability**: MCP and E2E reach 100 when classes expose intent through names and small methods, not comments or debugging.
- **MPQ-080-034 Architecture clarity**: MCP and E2E reach 100 when API, support, core, feature, bootstrap, and E2E boundaries stay explicit.
- **MPQ-080-035 Decoupling**: MCP and E2E reach 100 when production descriptors, bridge tools, scorecards, and test harnesses avoid duplicated schema logic.
- **MPQ-080-036 Cohesion**: MCP and E2E reach 100 when recovery, descriptor construction, workflow execution, LLM orchestration, and diagnostics have focused owners.
- **MPQ-080-037 Maintainability**: MCP and E2E reach 100 when large policy sinks are split only where it reduces reading cost or contract drift.
- **MPQ-080-038 Extensibility**: MCP and E2E reach 100 when new tools, resources, prompts, completions, and feature workflows can be added through stable extension points.
- **MPQ-080-039 Complexity control**: MCP and E2E reach 100 when long orchestration classes have bounded responsibilities and explicit extraction criteria.
- **MPQ-080-040 Abstraction quality**: MCP and E2E reach 100 when abstractions remove real duplication and do not hide simple one-off payload logic.
- **MPQ-080-041 Dependency governance**: MCP and E2E reach 100 when SDK, Tomcat, Jackson, JDBC, Docker, and LLM dependencies stay scoped and justified.
- **MPQ-080-042 API design**: MCP and E2E reach 100 when public descriptor, payload, exception, and runtime APIs are compact, stable, and testable.
- **MPQ-080-043 Configuration design**: MCP and E2E reach 100 when defaults are safe, opt-ins are explicit, and invalid configuration has safe diagnostics.
- **MPQ-080-044 Data consistency**: MCP and E2E reach 100 when database metadata, workflow plans, approval steps, and runtime artifacts stay coherent across calls.
- **MPQ-080-045 Concurrency safety**: MCP and E2E reach 100 when session, workflow, runtime, and artifact operations are safe under parallel tests or documented as isolated.
- **MPQ-080-046 Test sufficiency**: MCP and E2E reach 100 when every public production method and every model-facing contract has focused test coverage.
- **MPQ-080-047 Test quality**: MCP and E2E reach 100 when tests use precise assertions, avoid brittle counts, avoid weak `containsString` where exact assertions are practical, and stay repeatable.
- **MPQ-080-048 Regression risk**: MCP and E2E reach 100 when contract drift, safety regressions, SQL-classification changes, and LLM harness changes fail fast.

### Engineering And Operations

- **MPQ-080-049 Performance**: MCP and E2E reach 100 when normal MCP requests avoid unnecessary scanning, waits, or expensive model-harness work.
- **MPQ-080-050 Resource usage**: MCP and E2E reach 100 when runtime and tests close sessions, clients, containers, files, and database resources deterministically.
- **MPQ-080-051 Cost**: MCP and E2E reach 100 when mandatory gates avoid paid model providers and heavy live tests remain explicitly selected.
- **MPQ-080-052 Observability**: MCP and E2E reach 100 when traces record tool name, resource URI, execution mode, approval state, classification, latency, and error category.
- **MPQ-080-053 Operability**: MCP and E2E reach 100 when packaged HTTP and STDIO startup and failure modes have safe operator diagnostics.
- **MPQ-080-054 Stability**: MCP and E2E reach 100 when default tests are deterministic and live LLM variance is isolated from deterministic assertions.
- **MPQ-080-055 Fault recovery**: MCP and E2E reach 100 when recoverable errors guide the model through exactly one primary next path.
- **MPQ-080-056 Gray release and rollback**: MCP and E2E reach 100 when risky behavior has opt-in gates, disable paths, and documented rollback.
- **MPQ-080-057 Release readiness**: MCP and E2E reach 100 when JDK 21 subchain CI, distribution smoke, Docker smoke, and scoped quality gates pass.
- **MPQ-080-058 Reproducibility**: MCP and E2E reach 100 when scorecards record model, model digest where available, prompt hash, descriptor hash, run id, and command evidence.
- **MPQ-080-059 Auditability**: MCP and E2E reach 100 when tool calls, recoveries, approvals, diagnostics, and score decisions are traceable without leaking secrets.
- **MPQ-080-060 Ownership**: MCP and E2E reach 100 when every contract family and E2E suite has clear source ownership and update rules.

### Long-Term Quality

- **MPQ-080-061 Compatibility**: MCP and E2E reach 100 when unreleased-contract cleanup is intentional and any remaining compatibility behavior is tested.
- **MPQ-080-062 Migration**: MCP and E2E reach 100 when public contract changes have explicit golden update and consumer migration notes.
- **MPQ-080-063 Long-term evolution**: MCP and E2E reach 100 when growth paths for tools, resources, workflows, diagnostics, and LLM scenarios are clear.
- **MPQ-080-064 Technical debt**: MCP and E2E reach 100 when stale aliases, brittle tests, unused helpers, and speculative abstractions are removed.
- **MPQ-080-065 Knowledge transfer**: MCP and E2E reach 100 when future contributors can update descriptors, contracts, scenarios, and diagnostics by following Speckit and tests.
- **MPQ-080-066 Compliance**: MCP and E2E reach 100 when ASF headers, Checkstyle, Spotless, unit-test rules, and license expectations are satisfied.
- **MPQ-080-067 Governance transparency**: MCP and E2E reach 100 when score changes and scope decisions are recorded in scorecard, tasks, or requirements.
- **MPQ-080-068 Domain fit**: MCP and E2E reach 100 when SQL, ShardingSphere rule workflows, governance metadata, and runtime diagnostics match real user workflows.
- **MPQ-080-069 Project style**: MCP and E2E reach 100 when naming, package layout, test style, assertions, and SPI usage follow ShardingSphere conventions.
- **MPQ-080-070 Internationalization and accessibility**: MCP and E2E reach 100 when model-facing English contracts remain clear and multilingual natural prompts are covered where selected.

### ShardingSphere-Specific Quality

- **MPQ-080-071 Module boundaries**: MCP and E2E reach 100 when `api`, `support`, `core`, `features`, `bootstrap`, and E2E each own their proper layer.
- **MPQ-080-072 SPI usage**: MCP and E2E reach 100 when extensions load through ShardingSphere SPI mechanisms and tests verify plugin discovery.
- **MPQ-080-073 SQL parsing-chain impact**: MCP and E2E reach 100 when classifier behavior is validated against parser expectations and metadata-introspection paths.
- **MPQ-080-074 Routing and rewrite impact**: MCP and E2E reach 100 when SQL and workflow tests prove routing, rewrite, and rule changes do not regress.
- **MPQ-080-075 Governance mode compatibility**: MCP and E2E reach 100 when standalone and cluster/governance assumptions are explicit or tested.
- **MPQ-080-076 Dialect compatibility**: MCP and E2E reach 100 when H2, MySQL, and documented target dialect behavior is covered or safely rejected.
- **MPQ-080-077 Checkstyle and Spotless compliance**: MCP and E2E reach 100 when scoped Checkstyle and Spotless pass after every touched change.
- **MPQ-080-078 Maven integration**: MCP and E2E reach 100 when the JDK 21 subchain, distribution module, and profile gates run from documented Maven commands.
- **MPQ-080-079 Community reviewability**: MCP and E2E reach 100 when changes are small enough to review and each PR explains behavior, tests, risk, and rollback.
- **MPQ-080-080 Rollback and compatibility notes**: MCP and E2E reach 100 when release notes or Speckit records explain how to disable, revert, or migrate the MCP behavior.

## Work Package Mapping

- **WP-A Gate repair**: MPQ-080-003, 006, 011, 046, 047, 048, 057, 077, 078.
- **WP-B Contract hardening**: MPQ-080-007, 008, 019, 025, 026, 042, 046, 047, 048, 060, 062.
- **WP-C LLM native scoring**: MPQ-080-021, 022, 023, 024, 025, 029, 030, 031, 032, 058.
- **WP-D Safety and recovery**: MPQ-080-009, 012, 014, 015, 016, 017, 023, 027, 028, 055.
- **WP-E Architecture cleanup**: MPQ-080-033, 034, 035, 036, 037, 038, 039, 040, 041, 064, 071, 072.
- **WP-F E2E reliability**: MPQ-080-010, 011, 029, 030, 046, 047, 050, 052, 054, 058, 059.
- **WP-G Operations readiness**: MPQ-080-018, 043, 049, 051, 052, 053, 056, 057, 066, 080.
- **WP-H ShardingSphere integration**: MPQ-080-068, 069, 071, 072, 073, 074, 075, 076, 078, 079.

## Final 100 Claim Rule

The scorecard may state `100/100` only after:

1. Every MPQ-080 item is marked satisfied for MCP production modules.
2. Every MPQ-080 item is marked satisfied for MCP E2E, or the item is explicitly marked not applicable with an approved reason.
3. Every MPQ-080 item in `eighty-dimension-traceability.md` has command or artifact evidence recorded in `scorecard.md`.
4. The blocking `ToolHandlerRegistryTest` failure is fixed by semantic assertions.
5. LLM E2E reports native tool-call metrics separately from harness recovery metrics.
6. Fixed-sleep readiness polling is replaced or justified with bounded deterministic behavior.
7. Verification commands and exit codes are recorded in `scorecard.md`.
