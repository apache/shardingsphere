# Pre-Implementation Analysis: MCP Product Quality 100

## Scope

This analysis converts the 80 independent 100-point scoring dimensions into a practical implementation order for the MCP
and MCP E2E modules.

No branch switch is required. This file records analysis only and does not change production behavior.

## Compliance Guardrails

- Repository branch must remain `001-shardingsphere-mcp`.
- Generated files under `target/` are evidence only and must not be edited.
- `CODE_OF_CONDUCT.md` is the first coding-standard reference. Relevant controls include readability, cleanliness,
  consistency, simplicity, abstraction, excellence, build checks, Checkstyle, Spotless, and coverage discipline.
- Local variable style follows `CODE_OF_CONDUCT.md` when it conflicts with other instructions.
- MCP compatibility can yield to clarity because the MCP contract is not released yet, but every public contract change must
  be covered by explicit evidence.
- Live LLM E2E remains opt-in for default PR CI. Deterministic contract failures are blocking.
- Dockerized Ollama with `qwen3:1.7b` is the mandatory live model baseline.

## Executive Decision

Start with the deterministic P0 and scoring-observability gaps before doing any broad refactor.

The highest-value path is:

1. Fix stale deterministic descriptor assertions with semantic contract assertions.
2. Separate native model tool calls from harness recovery in trace data and score output.
3. Replace fixed readiness sleep in the LLM client with bounded polling and structured failure detail.
4. Re-run scoped MCP core and MCP E2E checks.
5. Only then consider contract snapshots, SQL safety expansion, or class extraction.

## Analysis Matrix

### A1. Stale Tool Descriptor Assertion

**Evidence**

- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/ToolHandlerRegistryTest.java` asserts descriptor
  field counts `[6, 5, 6, 3, 1]`.
- Current descriptors are `[6, 5, 7, 4, 1]`.
- `execute_update` now exposes `execution_mode`, `approved_by_user`, `max_rows`, and `timeout_ms`.
- `apply_workflow` now exposes `execution_mode`, `approved_steps`, and `approved_by_user`.
- Scoped command failed:

```shell
./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true -Dtest=ToolHandlerRegistryTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

Exit code: `1`.

**Conclusion**

The test is stale. The production descriptor shape is intentional and more explicit than the old assertion.

**Recommendation**

Replace field-count assertions with semantic assertions:

- Tool names and order.
- Required flags for `database`, `schema`, `sql`, and `execution_mode`.
- `execution_mode` enum values for `execute_update` and `apply_workflow`.
- `approved_by_user` type and optionality.
- Safety fields such as `max_rows`, `timeout_ms`, and `approved_steps`.

**Scoring Impact**

- Raises deterministic test health.
- Improves contract clarity.
- Reduces brittle test maintenance risk.

### A2. Native Tool Call vs Harness Recovery

**Evidence**

- `LLMMCPConversationRunner.processTextActionCompletion` executes `execute_query` when the model writes expected SQL in text.
- The recovered action is currently recorded through `createTraceRecord`, which marks it as a normal `tool_call`.
- `MCPInteractionTraceRecord` has no source or origin field.
- `LLMUsabilityMetricCalculator` and `LLMUsabilityScorecard` do not expose native tool-call rate or harness recovery rate.

**Conclusion**

The current harness can over-credit model tool-use ability. It is useful for robustness, but it must not be mixed with native
model behavior.

**Recommendation**

Add trace-level action origin and score it separately.

Recommended origin values:

- `model_tool_call`: the model emitted a real tool call.
- `harness_text_recovery`: the harness converted a text answer into a tool execution.
- `harness_argument_normalization`: the harness normalized placeholders or workflow arguments before execution.
- `protocol_bridge`: deterministic protocol actions such as resource or prompt bridge calls.

Required scorecard changes:

- Add `nativeToolCallRate`.
- Add `harnessRecoveryRate`.
- Keep existing task success, but full-score gates must require native execution for required tool actions.
- Keep harness recovery visible as resilience evidence, not model capability evidence.

**Scoring Impact**

- Raises LLM-use honesty.
- Improves auditability.
- Prevents hidden harness behavior from inflating model naturalness and tool-use scores.

### A3. LLM Readiness Polling

**Evidence**

- `LLMChatModelClient.waitUntilReady` retries with fixed `Thread.sleep(2000L)`.
- Startup time varies across local Docker, CI Docker, model pull, and CPU-only inference.
- Failure output depends on the last exception string and does not expose retry count or timing.

**Conclusion**

The current readiness loop is simple but contributes to E2E runtime variance and weak diagnostics.

**Recommendation**

Introduce a small bounded polling helper in the MCP E2E support layer.

Minimum behavior:

- Deadline-based polling.
- Configurable initial interval and maximum interval.
- Structured last failure message.
- Attempt count and elapsed time in the final error.
- No change to the opt-in live LLM gate.

First target:

- `test/e2e/mcp/.../LLMChatModelClient.java`.

Optional later target:

- `test/e2e/mcp/.../MySQLRuntimeTestSupport.java`, which also uses fixed sleep for JDBC readiness.

**Scoring Impact**

- Raises E2E stability.
- Improves diagnosability.
- Reduces wasted waiting while preserving bounded startup behavior.

### A4. H2 and MySQL Runtime Coverage

**Evidence**

- `LLMSmokeE2ETest` covers H2 HTTP, MySQL HTTP, H2 STDIO, and MySQL STDIO when LLM E2E is enabled.
- `LLMUsabilitySuiteE2ETest` currently runs the usability baseline on H2.
- `ProductionH2SQLExecutionE2ETest` covers SQL execution, update safety, timeout, multi-statement rejection, savepoints,
  transactional DDL metadata refresh, and close rollback.
- `ProductionMySQLRuntimeSmokeE2ETest` covers MySQL capabilities, resources, metadata search, query, update, unsupported
  sequence rejection, rollback, and close rollback.
- E2E defaults keep MySQL, STDIO, distribution, and LLM disabled unless enabled by properties or profiles.

**Conclusion**

H2 deterministic coverage is strong. MySQL deterministic smoke exists but is opt-in because it requires Docker. LLM smoke
covers both H2 and MySQL, while deep usability scoring is currently H2 only.

**Recommendation**

Keep the recommended baseline:

- H2 deterministic E2E as always-on contract evidence.
- MySQL deterministic smoke as required release evidence when Docker is available.
- H2 and MySQL LLM smoke for provider/runtime compatibility.
- H2 usability full-score gate for natural model behavior.

Do not add MySQL full usability scoring until the native/recovery scoring split is complete and runtime cost is measured.

**Scoring Impact**

- Preserves practical CI cost.
- Maintains MySQL evidence.
- Avoids multiplying flaky live-model scenarios before the scoring model is honest.

### A5. Large-Class Refactor Candidates

**Evidence**

- `MCPErrorConverter` is a large recovery-policy class.
- `MCPDescriptorCatalogPayloadBuilder` is a large descriptor-catalog builder.
- `MCPDescriptorCatalogValidator` is a large semantic validator.
- `LLMMCPConversationRunner` mixes conversation flow, prompt shaping, action normalization, text recovery, and trace creation.
- `LLMUsabilityMetricCalculator` mixes metric calculation and narrative scorecard detail construction.

**Conclusion**

Several classes are large, but most are internally coherent. A broad split before P0 fixes would increase review surface without
improving the immediate failure.

**Recommendation**

Do not perform a broad refactor first.

Extract only when a required change touches the same responsibility repeatedly:

- Extract text recovery from `LLMMCPConversationRunner` if adding origin-aware recovery requires non-trivial branching.
- Extract score components from `LLMUsabilityMetricCalculator` only if native/recovery metrics make the method harder to read.
- Defer `MCPErrorConverter`, `MCPDescriptorCatalogPayloadBuilder`, and `MCPDescriptorCatalogValidator` splitting unless adding
  new recovery families or catalog sections.

**Scoring Impact**

- Protects architecture clarity without speculative churn.
- Keeps review focused on behavior and evidence.

### A6. Contract Test Strategy

**Evidence**

- Official LLM tool definitions are generated from `ToolHandlerRegistry.getSupportedToolDescriptors()`.
- Protocol bridge schemas remain local to the E2E bridge and are separately tested.
- Descriptor catalog validation already checks model contract, tool/resource/prompt/completion shapes, navigation schema,
  next-action schema, banned public fields, and destructive-tool descriptors.
- No durable JSON golden snapshot fixtures were found for the public MCP descriptor surface.

**Conclusion**

Semantic contract tests exist and are valuable. Golden snapshots are not yet the primary protection.

**Recommendation**

Use a hybrid strategy:

- Fix the immediate core test with semantic assertions.
- Add snapshot or golden artifacts later for high-value public surfaces:
  - capability summary,
  - tool descriptors,
  - resource descriptors,
  - prompt descriptors,
  - completion descriptors,
  - recovery error payloads.
- Treat snapshots as contract-drift evidence, not as a replacement for semantic tests.

**Scoring Impact**

- Improves contract stability.
- Keeps tests explainable.
- Avoids snapshot churn for still-moving internal structure.

### A7. SQL Safety and Approval Matrix

**Evidence**

- H2 E2E covers read query execution, result truncation, unapproved update rejection, approved preview/update behavior,
  explain analyze, timeouts, multi-statement rejection, savepoints, transactional DDL refresh, and close rollback.
- MySQL smoke covers query/update/rollback paths.
- `execute_update` descriptor requires explicit `execution_mode` and exposes `approved_by_user`.
- `MCPErrorConverter` has recovery families for missing execution mode, invalid execution mode, approval required, multiple SQL,
  unsupported SQL, and banned SQL.

**Conclusion**

The safety baseline is strong. The next gaps are not obvious production failures; they are scoring and evidence gaps.

**Recommendation**

After P0 and native scoring:

- Add targeted tests only for uncovered high-risk safety claims.
- Prioritize prompt-injection style approval bypass, destructive SQL recovery messages, and dialect-specific MySQL behavior if
  evidence is weak.
- Avoid broad SQL classifier rewrites unless a failing test identifies a policy gap.

**Scoring Impact**

- Maintains security score.
- Keeps safety work evidence-driven.

### A8. Documentation and Rollback Evidence

**Evidence**

- `mcp/README.md` and `mcp/README_ZH.md` describe descriptors, fingerprints, workflow preview/approval, LLM lanes,
  Ollama `qwen3:1.7b`, and H2/MySQL smoke.
- Current docs include E2E controls and opt-in live LLM behavior.
- Scorecard documentation is now captured in Speckit, but generated run artifacts must be tied back to the 80 scoring gates.

**Conclusion**

Documentation is mostly present. The missing part is operational evidence mapping: how a reviewer confirms each mandatory score.

**Recommendation**

For final delivery:

- Update scorecard evidence with exact commands and artifact locations.
- Document how to disable or skip live LLM gates.
- Document rollback for any behavior-changing MCP contract update.
- Keep README changes focused on user-facing runtime behavior, not internal scoring mechanics.

**Scoring Impact**

- Raises reviewer confidence.
- Improves product readiness and maintainability.

## Recommended Implementation Order

1. P0 descriptor test repair in `mcp/core`.
2. Origin-aware trace data and scorecard fields for native tool calls versus harness recovery.
3. Bounded readiness polling for the LLM client.
4. Scoped deterministic verification:

```shell
./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true -Dtest=ToolHandlerRegistryTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

5. Scoped E2E verification for affected LLM support tests.
6. Contract snapshot or golden evidence, if needed after semantic tests are stable.
7. Documentation and scorecard evidence mapping.

## Deferred Items

- Broad class extraction for `MCPErrorConverter`, `MCPDescriptorCatalogPayloadBuilder`, or `MCPDescriptorCatalogValidator`.
- MySQL full usability scoring.
- PostgreSQL and openGauss runtime coverage unless the touched code affects those dialects.
- Default-PR live LLM enablement.

## Decision Log

- Recommended default choices from product clarification are accepted.
- Harness recovery remains allowed, but it must be scored separately.
- Deterministic failures block progress. Extended live LLM model misses do not fail the suite unless they expose deterministic
  contract or safety failures.
- The next implementation should be narrow and evidence-first.
