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

# Scorecard: MCP Scorecard Perfect 100

## Rule

Every item below has target score **100/100**.
The feature is incomplete while any item is below 100 or lacks evidence.

## Active Standard-First Reassessment

The 2026-05-11 production and E2E tables remain historical evidence for the previous checkpoint.
The 2026-05-13 user requirement reopens completion with a stricter source-driven gate.
The 2026-05-14 user requirement reopens HTTP authorization again for complete OAuth token validation:

- Official MCP Specification `2025-11-25` is the protocol source of truth.
- MCP Java SDK `1.1.2` is the detected implementation API and must be verified before code changes.
- ShardingSphere-specific payloads may support product workflows but cannot replace MCP discovery, transport, authorization, pagination, or error semantics.
- A dimension cannot be marked `100/100` unless it passes both the historical evidence gate and the active standard-first gate.

| Active Dimension | Current | Target | Closing Evidence |
|------------------|--------:|-------:|------------------|
| MCP protocol standard conformity | 100 | 100 | Closed by `EV-033` through `EV-035`: HTTP authorization now validates OAuth resource-server tokens with RFC 7662 introspection while preserving deployment-level bearer compatibility. |
| Tool and resource model quality | 100 | 100 | Closed by `EV-027`, `EV-029`, and golden contracts for tools, resources, resource templates, prompts, and completion. |
| Functional completeness | 100 | 100 | Closed by `EV-033` and `EV-034`: complete OAuth validation covers active, issuer, audience/resource, time-window, scope, failure, and valid-token paths. |
| Implementation elegance | 100 | 100 | Closed by isolating authorization metadata into HTTP bootstrap classes and keeping domain catalog guidance in descriptor payload builders. |
| Architecture cleanliness | 100 | 100 | Closed by `EV-027` and `EV-031`: official protocol discovery remains separate from ShardingSphere domain catalog resources. |
| Code cleanliness | 100 | 100 | Closed by consistent official method terminology and `EV-031` style gates. |
| Security and risk control | 100 | 100 | Closed by fail-closed OAuth introspection, RFC 6750 `invalid_token` and `insufficient_scope` challenges, and SDK duplicate static-token bypass in OAuth mode. |
| Error recovery and diagnostics | 100 | 100 | Closed by `EV-027`: JSON-RPC protocol errors stay separate from tool results, and recovery guidance now points to official discovery first. |
| Test coverage | 100 | 100 | Closed by `OAuthTokenValidatorTest`, `HttpOAuthTokenIntrospectorTest`, `HttpBearerAuthorizationHandlerTest`, and OAuth/static HTTP E2E evidence in `EV-033` and `EV-034`. |
| E2E realism and LLM evaluation | 100 | 100 | Closed by `EV-029`, `EV-030`, and historical opt-in runtime evidence retained through current standard-gate verification. |
| Operations and distribution maturity | 100 | 100 | Closed by documented `oauthIntrospection` config, secret placeholders, mutual exclusion with `accessToken`, and validated loopback fixture support. |
| Documentation and governance | 100 | 100 | Closed by README/README_ZH, protocol matrix, source map, scorecard, tasks, and evidence ledger updates tied to passing commands. |

Active status: **closed for complete OAuth token validation**.
The previous 12/12 standard-first evidence remains valid, and the 2026-05-14 OAuth resource-server validation scope is closed by `EV-033` through `EV-035`.

## Active OAuth Validation Reassessment

| Affected Dimension | Current | Target | Gap To Close For 100 |
|--------------------|--------:|-------:|----------------------|
| MCP protocol standard conformity | 100 | 100 | Closed by OAuth resource-server token validation using official MCP authorization plus RFC 7662/RFC 8707 semantics. |
| Functional completeness | 100 | 100 | Closed by a token validator that proves active token, issuer, resource/audience, time window, and required scopes. |
| Security and risk control | 100 | 100 | Closed by fail-closed OAuth validation and correct 401/403 challenge behavior. |
| Test coverage | 100 | 100 | Closed by branch-specific unit and E2E tests for invalid token classes and successful validation. |
| Operations and distribution maturity | 100 | 100 | Closed by documented and validated OAuth introspection configuration, secret placeholders, startup diagnostics, and migration behavior. |
| Documentation and governance | 100 | 100 | Closed by Speckit, README, protocol matrix, and evidence ledger updates after implementation evidence. |

## MCP Production Module Dimensions

| Dimension | Current | Target | Gap To Close For 100 |
|-----------|--------:|-------:|----------------------|
| Model-use friendliness | 100 | 100 | Closed by `EV-019`, `EV-020`, `EV-022`, and model-first descriptor tests. |
| Natural interaction quality | 100 | 100 | Closed by guided flows, approval-first workflow behavior, and live LLM scorecards in `EV-019` and `EV-020`. |
| Clarity | 100 | 100 | Closed by compact capability contracts, lane docs, and the evidence artifacts in `EV-024`. |
| Code readability | 100 | 100 | Closed by bounded recovery factories, tool elicitation separation, and the small safety limiter slice. |
| Architecture clarity | 100 | 100 | Closed by request-scope tests, transport/session tests, protocol matrix, and safety boundary documentation. |
| Decoupling | 100 | 100 | Closed by bounded SPI registry tests, packaged plugin discovery, and `protocol-evidence-matrix.md`. |
| Protocol correctness | 100 | 100 | Closed by production protocol tests, golden E2E contracts, STDIO/MySQL/package evidence, and `EV-023`. |
| Stability | 100 | 100 | Closed by session cleanup, runtime negative-state, LLM guardrail, and Docker/readiness diagnostics evidence. |
| Diagnostics | 100 | 100 | Closed by recovery factory tests, LLM artifact summaries, and compact `rate_limited` recovery guidance. |
| Safety | 100 | 100 | Closed by approval/auth/SQL-classifier evidence plus session-scope tool-call limiting in `EV-021` and `EV-022`. |
| Extensibility | 100 | 100 | Closed by handler SPI tests, package plugin smoke, workflow fixtures, and descriptor contract coverage. |
| Performance and resource use | 100 | 100 | Closed by `MCPPerformanceBudgetSmokeTest` and E2E lane budgets in `performance-budget.md`. |
| Configuration and distribution | 100 | 100 | Closed by distribution assembly, packaged runtime smoke, MySQL, STDIO, and lane matrix evidence. |
| Compatibility | 100 | 100 | Closed by Java 21 module chain, Proxy-only runtime evidence, Docker-backed MySQL, STDIO, and package smokes. |
| Test quality | 100 | 100 | Closed by targeted tests, final Checkstyle, final Spotless, and no open-risk evidence gaps. |

## MCP E2E Module Dimensions

| Dimension | Current | Target | Gap To Close For 100 |
|-----------|--------:|-------:|----------------------|
| Model-use friendliness | 100 | 100 | Closed by repeatable live LLM smoke/usability scorecards and enforced full-score guardrails. |
| Natural interaction quality | 100 | 100 | Closed by metadata, SQL, workflow, recovery, and approval scenarios in the LLM suites. |
| Clarity | 100 | 100 | Closed by the E2E lane matrix, protocol matrix, and performance-budget evidence. |
| Code readability | 100 | 100 | Closed by LLM diagnostics extraction, runtime support diagnostics, and focused fixture tests. |
| Architecture clarity | 100 | 100 | Closed by separated runtime fixture, LLM harness, packaged distribution, and production client evidence. |
| Decoupling | 100 | 100 | Closed by Docker/Ollama/MySQL/STDIO/package fixture boundaries and configuration evidence. |
| Protocol correctness | 100 | 100 | Closed by golden contracts, HTTP contracts, STDIO runtime evidence, and `protocol-evidence-matrix.md`. |
| End-to-end realism | 100 | 100 | Closed by MySQL, STDIO, packaged runtime, plugin discovery, and live LLM evidence. |
| Stability | 100 | 100 | Closed by bounded diagnostics for Docker readiness, STDIO stderr, package startup, and model readiness. |
| Diagnostics | 100 | 100 | Closed by reviewer-ready LLM artifacts and runtime failure diagnostics. |
| Safety | 100 | 100 | Closed by HTTP safety E2E, approval E2E, session-scope rate guard, and external-model boundary evidence. |
| Extensibility | 100 | 100 | Closed by golden contract drift tests, descriptor coverage, plugin fixture, and model-contract assertions. |
| Performance and resource use | 100 | 100 | Closed by command-duration budgets in `performance-budget.md` and current lane evidence. |
| Configuration and distribution | 100 | 100 | Closed by complete default, opt-in, MySQL, STDIO, distribution, package, and LLM commands. |
| Compatibility | 100 | 100 | Closed by Docker, Ollama, Java 21, MySQL 8.0.36, STDIO, HTTP, and model-provider evidence. |
| Test quality | 100 | 100 | Closed by strengthened assertions, golden drift checks, current LLM scorecards, and style gates. |

## Exit Gate Policy

- A score can move to 100 only after every listed gap for that dimension has passing evidence.
- Evidence can be a scoped Maven command, a golden contract snapshot, a live LLM artifact, or a packaged runtime smoke result.
- Waivers and exception records are not allowed.
- Missing evidence remains an open risk and keeps the dimension below 100.
- Average score is not a gate. The only completion gate is every dimension at 100.

## Current Completion Status

- MCP production dimensions at 100 for the 2026-05-11 historical checkpoint: **15/15**.
- MCP E2E dimensions at 100 for the 2026-05-11 historical checkpoint: **16/16**.
- Active standard-first dimensions at 100 for the 2026-05-13 gate: **12/12**.
- Active complete OAuth validation dimensions at 100 for the 2026-05-14 gate: **6/6**.
- Overall status: **closed for complete OAuth token validation**; the scoped 2026-05-13 standard-first gate remains historical evidence.

## Current Progress Evidence

This checkpoint now has current implementation evidence in `evidence-ledger.md`:

- `EV-005` to `EV-007`: production readability, diagnostics, and concrete surface tests.
- `EV-008`: E2E golden contract drift and runtime negative-state coverage.
- `EV-009`: final default-lane MCP plus MCP E2E regression command.
- `EV-010`: MCP feature module regression command.
- `EV-011` and `EV-012`: final Checkstyle and Spotless gates.

These items close the recommended default-lane implementation slice.
The next runtime evidence slice is also current:

- `EV-014`: E2E readiness diagnostics for STDIO stderr, Docker readiness, and packaged distribution missing-home cases.
- `EV-015`: STDIO runtime command, `84` tests with `0` skipped.
- `EV-016`: MySQL HTTP plus STDIO command, `22` tests with `0` skipped against Docker-backed `mysql:8.0.36`.
- `EV-017` and `EV-018`: packaged distribution assembly plus HTTP, STDIO, and plugin smoke commands.
- `EV-019`: live LLM smoke plus usability command, `5` tests with `0` skipped; core and extended scorecards both `100/100`.
- `EV-020`: live LLM harness guardrails for shared Ollama runtime, required tool-call coverage, and full-score extended-suite enforcement.
- `EV-033`: complete OAuth introspection unit and documentation contract command, `80` tests with `0` skipped.
- `EV-034`: OAuth introspection plus static bearer HTTP E2E command, `10` tests with `0` skipped.
- `EV-035`: final Checkstyle and Spotless gate for `mcp/support`, `mcp/core`, `mcp/bootstrap`, and `test/e2e/mcp`.

The final evidence slice closes the remaining performance-budget, safety-boundary, protocol-conformance,
historical revalidation, and decoupling gaps:

- `EV-021`: session-scope tool-call limiter, `rate_limited` recovery, and model-facing safety policy.
- `EV-022`: MCP API/support/core target tests plus production performance budget smoke.
- `EV-023`: E2E H2 capability surface smoke after the safety-policy payload change.
- `EV-024`: protocol, historical, safety, and performance traceability artifacts.
- `EV-025`: final Checkstyle and Spotless gates for the MCP API/support/core chain.

## Active Standard-First Evidence Closed

- `EV-026`: Source-driven official MCP map, stack detection, and standard decision record.
- `EV-027`: Protocol evidence matrix that proves official MCP discovery and payload semantics.
- `EV-028`: Security evidence for Streamable HTTP, authorization mode, origin handling, valid HTTPS authorization-server URIs, and token-passthrough rejection.
- `EV-029`: E2E evidence for official tools/resources/prompts/completion flows through HTTP plus historical STDIO, packaged runtime, and MySQL lanes.
- `EV-030`: mcp-builder evaluation XML with ten read-only, independent, complex, realistic, verifiable, and stable questions.
- `EV-031` and `EV-032`: Current scoped style, unit, golden, and H2 E2E verification for the standard-first implementation.
