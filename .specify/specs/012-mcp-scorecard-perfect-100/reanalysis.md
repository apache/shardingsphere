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

# Reanalysis: MCP Scorecard Perfect 100

## Purpose

This document resolves the follow-up analysis before implementation.
It classifies every below-100 dimension by the real work needed to reach 100.

Rules:

- Dimensions are locked for this checkpoint.
- No branch switch is allowed.
- Existing project PR gates are not changed.
- Every dimension must reach `100/100`.
- No waiver is allowed.
- Missing evidence is an open risk and keeps the dimension below 100.

## Executive Conclusion

The requirements are clear enough to implement.
No additional user confirmation is needed before starting work.

The remaining work is not one large rewrite.
It is a sequence of evidence-backed closures:

1. Establish the evidence ledger and open-risk format.
2. Revalidate historical evidence from `011-mcp-llm-product-quality-100`.
3. Close evidence-only gaps without code changes.
4. Close high-impact code and E2E stability gaps in small slices.
5. Re-score only after each dimension has passing evidence.

## Latest Reanalysis After Default-Lane Closure

The default code and E2E lane has current evidence, so this reanalysis does not reopen that implementation slice.
The remaining blockers are evidence gates for strict 100-point scoring.

### What No Longer Needs Reanalysis

- Production code readability for `MCPToolSpecificationFactory` and `MCPErrorConverter`: current refactoring evidence exists.
- Concrete handler test replacement: current tests cover concrete behavior instead of interface-only contracts.
- Default H2 HTTP E2E and local golden contracts: current clean test evidence exists.
- Checkstyle and Spotless for the selected MCP and MCP E2E modules: current evidence exists.

These items can support score improvement, but they still do not allow every dimension to become 100 because the perfect gate requires all remaining evidence.

### What Must Still Be Reanalyzed Before Any 100 Claim

#### RA-001 Live LLM Evidence

- Blocking dimensions: E-M01, E-M02, E-M08, E-M09, E-M11, E-M13, E-M15, E-M16.
- Current state: The `llm-e2e` Maven profile exists, but the current checkpoint has no live run artifact recorded.
- Why below 100: Historical LLM output cannot prove current model behavior, and harness recovery must be separated from native tool use.
- Minimum evidence command:

```bash
./mvnw -pl test/e2e/mcp -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest,LLMUsabilitySuiteE2ETest test -B -ntp
```

- Completion requirement: `scorecard.json`, `scenario-results.json`, `summary.md`, and interaction traces show full score, native tool calls, no approval violation, and bounded round trips.

#### RA-002 STDIO Runtime Evidence

- Blocking dimensions: P-M07, P-M13, P-M14, E-M07, E-M08, E-M14, E-M15.
- Current state: STDIO support and tests exist, but default E2E disables `mcp.e2e.production.stdio.enabled`.
- Why below 100: HTTP evidence cannot prove STDIO framing, process lifecycle, or stderr/stdout separation.
- Minimum evidence command:

```bash
MCP_STDIO_TESTS=ProductionH2CapabilityDiscoveryE2ETest,ProductionH2MetadataResourceE2ETest,ProductionH2SQLExecutionE2ETest
MCP_STDIO_TESTS=$MCP_STDIO_TESTS,ProductionH2AiNativeInteractionE2ETest,ProductionMultiDatabaseE2ETest
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.production.stdio.enabled=true -Dtest="$MCP_STDIO_TESTS" test -B -ntp
```

- Completion requirement: STDIO cases pass without protocol-frame contamination and failures include process stderr diagnostics.

#### RA-003 MySQL Runtime Evidence

- Blocking dimensions: P-M14, E-M06, E-M08, E-M09, E-M14, E-M15.
- Current state: MySQL Testcontainers support exists, but `mcp.e2e.production.mysql.enabled` is disabled by default.
- Why below 100: H2 proves deterministic behavior, not MySQL driver, dialect, connection, transaction, and metadata behavior.
- Minimum evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.production.mysql.enabled=true -Dmcp.e2e.production.stdio.enabled=true \
  -Dtest=ProductionMySQLRuntimeSmokeE2ETest test -B -ntp
```

- Completion requirement: HTTP and STDIO MySQL cases pass or produce bounded Docker readiness diagnostics.

#### RA-004 Packaged Distribution And Plugin Evidence

- Blocking dimensions: P-M13, P-M14, E-M08, E-M14, E-M15.
- Current state: packaged distribution smoke and plugin discovery tests exist, but `mcp.e2e.distribution.enabled` is disabled by default.
- Why below 100: module tests do not prove the assembled distribution layout, scripts, lib/plugins separation, or packaged STDIO/HTTP startup.
- Minimum evidence commands:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package -B -ntp
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.distribution.enabled=true \
  -Dtest=PackagedDistributionSmokeE2ETest,PackagedDistributionPluginDiscoveryE2ETest test -B -ntp
```

- Completion requirement: packaged HTTP and STDIO smoke pass, plugin discovery proves external feature loading, and runtime status stays secret-safe.

#### RA-005 Performance And Resource Budgets

- Blocking dimensions: P-M12, E-M09, E-M13.
- Current state: default clean E2E duration is recorded, but no explicit budget exists for descriptor generation, metadata lookup, request scope, SQL execution, live LLM, or opt-in lanes.
- Why below 100: elapsed time without an agreed threshold is diagnostic, not a pass/fail score gate.
- Minimum closure: define budgets first, then record measured command output or lightweight performance smokes for each budget.
- Completion requirement: each budget has a threshold, a command, a result, and a decision recorded in `evidence-ledger.md`.

#### RA-006 Safety Boundary Evidence

- Blocking dimensions: P-M10, E-M11.
- Current state: approval, SQL classification, HTTP auth, and secret redaction have tests, but abuse/rate/external-model assumptions are not fully mapped.
- Why below 100: safety cannot be inferred from happy-path E2E or default contract tests.
- Minimum closure: create a safety matrix that maps approval, auth, prompt injection, model-boundary, SQL classifier, and redaction assumptions to tests or artifacts.
- Completion requirement: no safety assumption remains prose-only.

#### RA-007 Protocol Conformance Evidence

- Blocking dimensions: P-M07, E-M07.
- Current state: local golden and contract tests are strong.
- Why below 100: the scorecard asks for conformance-style evidence across initialize, session, tools, resources, prompts, completions, HTTP, and STDIO.
- Minimum closure: record a protocol matrix that maps each behavior to a passing test or artifact, including STDIO evidence from RA-002.
- Completion requirement: every protocol surface has current evidence and no unsupported behavior is silently accepted.

#### RA-008 Decoupling And Extensibility Evidence

- Blocking dimensions: P-M06, P-M11, E-M06, E-M12.
- Current state: SPI and plugin paths exist; default code cleanup reduced some local coupling.
- Why below 100: static registries, context boundaries, and scenario/golden fixture friction still need either proof or a small follow-up refactor.
- Minimum closure: prove low-friction feature and scenario extension with tests, or reduce the specific coupling found during that proof.
- Completion requirement: extension path is backed by tests and does not require manual synchronization across unexplained surfaces.

## Reanalysis Decision

No new user confirmation is needed.
The next implementation slice should start with RA-002, RA-003, and RA-004.
They are evidence-first and can raise realism, compatibility, protocol, and distribution confidence without changing PR gates.
RA-005 should run in parallel only after budgets are defined, otherwise it will produce numbers without a scoring decision.

## Historical 100 Versus Current Baseline

`011-mcp-llm-product-quality-100` is historical checkpoint evidence.
It cannot override this package because the latest review uses a stricter independent-dimension model.

Reanalysis:

- The historical package may contain valid commands and artifacts.
- Each historical command must be rerun or matched to a still-valid artifact before it counts here.
- Historical live LLM results are candidate evidence, not current evidence.
- Historical claims cannot mark a dimension 100 without current review linkage.

Action:

- Add an evidence ledger before changing scores.
- For each historical command, record whether it is still runnable, still relevant, and still sufficient.

## No-Waiver Reachability

The no-waiver rule does not block the feature, but it changes how boundary items are handled.

Allowed:

- Keep a dimension below 100 until missing evidence is produced.
- Add manual or opt-in evidence outside the existing PR gate.
- Improve code or tests to remove the gap.

Not allowed:

- Mark a dimension 100 because a limitation is declared acceptable.
- Remove a dimension without an explicit user request.
- Treat project V1 scope as a scoring bypass.

Boundary-sensitive dimensions must be closed with evidence that the boundary is enforced safely.
If such evidence is not possible yet, the dimension stays below 100.

## Gap Type Legend

- `CODE`: production or E2E code must change.
- `TEST`: deterministic unit, integration, E2E, or golden tests must be added or strengthened.
- `CONTRACT`: model-facing schema, descriptor, prompt, or contract snapshot must be clarified.
- `EVIDENCE`: code may already be enough, but current proof is missing or stale.
- `PERF`: measurable time, resource, or flake budget is missing.
- `OPS`: packaging, runtime, configuration, or reproduction evidence is missing.
- `BOUNDARY`: a project scope or V1 limitation must be proven safe, not bypassed.

## Production Module Reanalysis

### P-M01 Model-use friendliness

- Current score: `90/100`.
- Gap types: `CONTRACT`, `TEST`, `EVIDENCE`.
- Analysis: Descriptor density and duplicate override shapes increase model choice cost.
- Required closure: Simplify or normalize model-facing fields and prove zero-guessing discovery.
- First gate: Contract test for compact capability path plus model-facing discovery scenario.

### P-M02 Natural interaction quality

- Current score: `88/100`.
- Gap types: `CONTRACT`, `CODE`, `TEST`.
- Analysis: Some flows still expose SQL, DistSQL, and artifact concepts too early.
- Required closure: Add guided wording or next-action paths that preserve natural intent.
- First gate: Natural task scenarios prove safe preview and query flows without scripted tool hints.

### P-M03 Clarity

- Current score: `91/100`.
- Gap types: `CONTRACT`, `EVIDENCE`.
- Analysis: Documentation exists, but first-read and first-call paths are dense.
- Required closure: Add compact entry points for readers and models.
- First gate: Contract or doc check that the compact path is present and linked.

### P-M04 Code readability

- Current score: `84/100`.
- Gap types: `CODE`, `TEST`.
- Analysis: Several classes carry too many responsibilities for a 100 score.
- Required closure: Split only where it reduces reading cost and preserves behavior.
- First gate: Refactor one largest policy or transport class with focused tests.

### P-M05 Architecture clarity

- Current score: `88/100`.
- Gap types: `CODE`, `TEST`.
- Analysis: Request-scope assembly and transport duties are understandable but not crisp enough.
- Required closure: Move construction and transport concerns behind named boundaries.
- First gate: Tests prove runtime behavior is unchanged after boundary extraction.

### P-M06 Decoupling

- Current score: `84/100`.
- Gap types: `CODE`, `TEST`, `BOUNDARY`.
- Analysis: Static registries and hardcoded context types reduce extension flexibility.
- Required closure: Reduce coupling or prove the bounded extension model with tests.
- First gate: Handler registration and context resolution tests cover extension behavior.

### P-M07 Protocol correctness

- Current score: `90/100`.
- Gap types: `TEST`, `EVIDENCE`.
- Analysis: Local MCP contracts are strong, but external conformance evidence is absent.
- Required closure: Add conformance-style local tests or record official compatibility evidence.
- First gate: A protocol matrix covers initialize, session, tools, resources, prompts, and completions.

### P-M08 Stability

- Current score: `86/100`.
- Gap types: `TEST`, `CODE`, `OPS`.
- Analysis: Session and runtime cleanup are guarded, but edge cases need stronger negative proof.
- Required closure: Add bounded tests for lifecycle, cleanup, workflow, and SQL scanner edges.
- First gate: Negative lifecycle and SQL-safety tests pass in scoped Maven commands.

### P-M09 Diagnostics

- Current score: `92/100`.
- Gap types: `CODE`, `CONTRACT`, `TEST`.
- Analysis: Diagnostics are useful, but centralized recovery policy can grow too large.
- Required closure: Bound recovery families and prove responses remain compact and actionable.
- First gate: Golden recovery tests verify category, action count, and secret-free payloads.

### P-M10 Safety

- Current score: `88/100`.
- Gap types: `TEST`, `BOUNDARY`, `OPS`.
- Analysis: Approval and SQL safety exist, but audit, auth, and classifier boundaries need proof.
- Required closure: Prove safety boundaries or add missing safety tests.
- First gate: Approval, auth, SQL classifier, and secret-redaction tests pass together.

### P-M11 Extensibility

- Current score: `87/100`.
- Gap types: `CODE`, `CONTRACT`, `TEST`.
- Analysis: SPI and descriptors help, but new features still require multi-surface updates.
- Required closure: Reduce feature addition friction or prove the extension checklist is complete.
- First gate: Feature SPI or descriptor extension test shows a low-friction path.

### P-M12 Performance and resource use

- Current score: `82/100`.
- Gap types: `PERF`, `EVIDENCE`, `TEST`.
- Analysis: Limits exist, but no measured budget proves resource behavior.
- Required closure: Define and measure budgets for descriptor, metadata, request, and SQL paths.
- First gate: Performance smoke records time and memory-sensitive counts within budget.

### P-M13 Configuration and distribution

- Current score: `90/100`.
- Gap types: `OPS`, `TEST`, `EVIDENCE`.
- Analysis: Distribution exists, but full HTTP, STDIO, driver, and plugin evidence is not current.
- Required closure: Reproduce packaged runtime evidence without changing PR gates.
- First gate: Manual or opt-in distribution smoke records HTTP and STDIO results.

### P-M14 Compatibility

- Current score: `84/100`.
- Gap types: `BOUNDARY`, `OPS`, `TEST`.
- Analysis: Java 21, Proxy-only, V1 identifiers, and dialect coverage need explicit proof.
- Required closure: Show supported boundaries are safe and unsupported paths fail clearly.
- First gate: Compatibility matrix has commands or tests for each supported boundary.

### P-M15 Test quality

- Current score: `88/100`.
- Gap types: `TEST`, `EVIDENCE`.
- Analysis: Tests are broad, but style risks and coverage evidence remain.
- Required closure: Strengthen assertions and collect scoped coverage or equivalent proof.
- First gate: Scoped test, Checkstyle, Spotless, and coverage evidence are recorded.

## MCP E2E Module Reanalysis

### E-M01 Model-use friendliness

- Current score: `88/100`.
- Gap types: `EVIDENCE`, `TEST`.
- Analysis: Live LLM evidence exists historically, but repeatability for this checkpoint is not proven.
- Required closure: Rerun or refresh live LLM artifacts and separate native calls from harness help.
- First gate: Current live LLM scorecard has full score and trace evidence.

### E-M02 Natural interaction quality

- Current score: `86/100`.
- Gap types: `TEST`, `CONTRACT`.
- Analysis: Existing prompts are useful, but scenario diversity is still limited.
- Required closure: Add natural task variants without scripted first-call wording.
- First gate: Scenario catalog includes metadata, SQL, workflow, and recovery natural prompts.

### E-M03 Clarity

- Current score: `87/100`.
- Gap types: `OPS`, `CONTRACT`.
- Analysis: Default and opt-in lane behavior is discoverable but spread across files.
- Required closure: Add one lane matrix for default, manual, and opt-in evidence.
- First gate: Reader-facing matrix lists H2, MySQL, STDIO, distribution, and LLM lanes.

### E-M04 Code readability

- Current score: `84/100`.
- Gap types: `CODE`, `TEST`.
- Analysis: Conversation and runtime support classes are larger than ideal.
- Required closure: Split high-churn support logic into focused components.
- First gate: Refactor one support area and keep all related tests passing.

### E-M05 Architecture clarity

- Current score: `88/100`.
- Gap types: `CODE`, `OPS`.
- Analysis: Runtime fixtures, LLM harness, distribution support, and clients need clearer seams.
- Required closure: Clarify ownership and dependency direction between support layers.
- First gate: Support package structure and tests show each layer can be reasoned about alone.

### E-M06 Decoupling

- Current score: `82/100`.
- Gap types: `CODE`, `OPS`, `TEST`.
- Analysis: Docker, Ollama, MySQL, STDIO, and distribution assumptions are too embedded.
- Required closure: Move environment-specific choices behind fixture configuration.
- First gate: Tests can validate fixture decisions without launching every dependency.

### E-M07 Protocol correctness

- Current score: `91/100`.
- Gap types: `TEST`, `EVIDENCE`.
- Analysis: Golden and contract tests are strong, but external conformance evidence is absent.
- Required closure: Add conformance-style protocol matrix or refreshed contract proof.
- First gate: HTTP and STDIO contract evidence is current and mapped to protocol behavior.

### E-M08 End-to-end realism

- Current score: `90/100`.
- Gap types: `OPS`, `EVIDENCE`.
- Analysis: Realism exists through optional lanes, but current perfect-gate evidence is incomplete.
- Required closure: Record mandatory H2, MySQL, STDIO, distribution, and LLM evidence.
- First gate: Evidence ledger links each runtime lane to a passing command or artifact.

### E-M09 Stability

- Current score: `78/100`.
- Gap types: `CODE`, `TEST`, `PERF`, `OPS`.
- Analysis: This is the most important E2E gap.
- Required closure: Bound Docker, model readiness, polling, and packaged runtime startup.
- First gate: Readiness failures report attempts, elapsed time, last error, and timeout reason.

### E-M10 Diagnostics

- Current score: `90/100`.
- Gap types: `OPS`, `TEST`.
- Analysis: Artifacts exist, but failures still require manual interpretation.
- Required closure: Add reviewer-ready failure summaries for trace and artifact failures.
- First gate: A failing diagnostic fixture produces a concise actionable summary.

### E-M11 Safety

- Current score: `89/100`.
- Gap types: `TEST`, `BOUNDARY`.
- Analysis: Approval safety is tested, but abuse, rate, and external model assumptions need proof.
- Required closure: Add tests or documented evidence for each safety assumption.
- First gate: Safety matrix records approval, auth, prompt injection, and model-boundary behavior.

### E-M12 Extensibility

- Current score: `84/100`.
- Gap types: `CODE`, `CONTRACT`, `TEST`.
- Analysis: Adding scenarios touches catalog, golden contracts, fixtures, and assertions.
- Required closure: Reduce scenario addition friction or add a generator/checklist with tests.
- First gate: New scenario template passes validation with minimal file touches.

### E-M13 Performance and resource use

- Current score: `76/100`.
- Gap types: `PERF`, `OPS`, `EVIDENCE`.
- Analysis: This is the lowest score and must be handled early.
- Required closure: Define acceptable duration and resource budgets for each E2E lane.
- First gate: Full selected lane run records duration and stays within budget.

### E-M14 Configuration and distribution

- Current score: `85/100`.
- Gap types: `OPS`, `EVIDENCE`.
- Analysis: Reproduction exists but is too scattered for a perfect gate.
- Required closure: Centralize prerequisites and commands without changing PR gates.
- First gate: Quickstart or lane matrix provides complete manual reproduction commands.

### E-M15 Compatibility

- Current score: `83/100`.
- Gap types: `OPS`, `BOUNDARY`, `TEST`.
- Analysis: Java 21, Docker, Ollama, MySQL, STDIO, and provider assumptions need proof.
- Required closure: Prove compatibility boundaries or keep the dimension open.
- First gate: Compatibility matrix records supported versions and passing commands.

### E-M16 Test quality

- Current score: `89/100`.
- Gap types: `TEST`, `EVIDENCE`.
- Analysis: Contract tests are broad, but assertion precision and coverage evidence need work.
- Required closure: Strengthen brittle assertions and record drift/coverage evidence.
- First gate: Golden, model contract, and targeted E2E tests pass with precise assertions.

## Priority Order

### P0

1. Evidence ledger and open-risk format.
2. Historical evidence revalidation from `011`.
3. E2E stability.
4. E2E performance and resource budgets.
5. Production performance and resource budgets.
6. Production code readability and decoupling.

### P1

1. Model-use friendliness and natural interaction.
2. Protocol correctness evidence.
3. Configuration, distribution, and compatibility evidence.
4. Test quality evidence.
5. Diagnostics compactness and self-triage.

### P2

1. Reader-facing clarity polish.
2. Extensibility friction reduction.
3. Documentation synchronization after evidence is current.

## PR Gate Decision

This package must not change the project's existing PR gate.

Evidence can be collected through:

- Existing scoped Maven commands.
- Existing opt-in profiles.
- Manual local commands.
- Artifact review from live or packaged runs.

If a dimension depends on non-PR-gate evidence, it remains below 100 until that evidence is recorded.

## Implementation Start Recommendation

Start with score governance, not code.

First implementation slice:

1. Add evidence columns or a linked evidence ledger to `scorecard.md`.
2. Add open-risk fields for every dimension.
3. Map historical `011` commands to current dimensions.
4. Mark each historical evidence item as current, stale, insufficient, or rerun-needed.

Only after that should production or E2E code changes begin.
