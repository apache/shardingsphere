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

# Evidence Ledger: MCP Scorecard Perfect 100

## Rule

This ledger records current evidence only.
Historical evidence from `011-mcp-llm-product-quality-100` must be revalidated before reuse.

Statuses:

- `current`: Evidence was produced for this checkpoint.
- `stale`: Evidence exists historically but must be rerun.
- `insufficient`: Evidence does not prove the target.
- `rerun-needed`: Evidence is likely useful but not current.
- `open-risk`: Evidence is missing and the dimension stays below 100.

## Current Evidence

### EV-001

- Dimension: Production code readability.
- Status: `current`.
- Evidence: Extracted `MCPToolElicitationHandler` from `MCPToolSpecificationFactory`.
- Result: Reduced mixed abstraction in bootstrap tool specification creation.

### EV-002

- Dimension: Production test quality.
- Status: `current`.
- Evidence:
  `./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest=MCPToolSpecificationFactoryTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp`
- Result: exit `0`, `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`.

### EV-003

- Dimension: E2E diagnostics.
- Status: `current`.
- Evidence: Added scenario-level failure details to LLM usability summary output.
- Result: Summary now includes failure type, round trips, invalid calls, native tool-call coverage, harness recovery, and message.

### EV-004

- Dimension: E2E test quality.
- Status: `current`.
- Evidence:
  `./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilityReportWriterTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp`
- Result: exit `0`, `Tests run: 1, Failures: 0, Errors: 0, Skipped: 0`.

### EV-005

- Dimension: Production diagnostics and readability.
- Status: `current`.
- Evidence: Split `MCPErrorConverter` recovery payload construction into bounded package-private factories:
  `MCPRecoveryPayloadFactory`, `MCPRecoveryPayloadSupport`, `MCPBasicRecoveryPayloadFactory`, `MCPSQLRecoveryPayloadFactory`,
  and `MCPWorkflowRecoveryPayloadFactory`.
- Result: `MCPErrorConverter` now owns error mapping while recovery payloads are grouped by basic, SQL, and workflow concerns.

### EV-006

- Dimension: Production recovery behavior.
- Status: `current`.
- Evidence:
  `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true -Dtest=MCPErrorConverterTest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp`
- Result: exit `0`, `Tests run: 39, Failures: 0, Errors: 0, Skipped: 0`.

### EV-007

- Dimension: Production concrete surface coverage.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true \
  -Dtest=MCPErrorConverterTest,CoreResourceHandlerSurfaceTest,SearchMetadataToolHandlerTest,WorkflowExecutionToolHandlerTest,WorkflowValidationToolHandlerTest \
  test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`, `Tests run: 96, Failures: 0, Errors: 0, Skipped: 0`.

### EV-008

- Dimension: E2E contract drift and runtime negative-state coverage.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dtest=HttpTransportGoldenContractE2ETest,ProductionMultiDatabaseE2ETest \
  test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`, `Tests run: 8, Failures: 0, Errors: 0, Skipped: 0`.

### EV-009

- Dimension: Final MCP plus E2E regression coverage.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipITs -Dspotless.skip=true clean test -B -ntp
```

- Result: exit `0`; all seven selected modules `SUCCESS`.
- Counts: `mcp/api` `13`, `mcp/support` `278`, `mcp/core` `418`, `mcp/features/encrypt` `69`, `mcp/features/mask` `52`, `mcp/bootstrap` `170`.
- E2E counts: `test/e2e/mcp` `240` tests with `14` skipped.
- Duration: total `03:22 min`; `test/e2e/mcp` `03:01 min`.

### EV-010

- Dimension: MCP feature module regression coverage.
- Status: `current`.
- Evidence:
  `./mvnw -pl mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true test -B -ntp`
- Result: exit `0`; `shardingsphere-mcp-feature-encrypt` and `shardingsphere-mcp-feature-mask` both `SUCCESS`.
- Counts: encrypt `69` tests, mask `52` tests; the `-am` dependency chain also passed.

### EV-011

- Dimension: Final style gates.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipTests -DskipITs -Dspotless.skip=true -Pcheck checkstyle:check -B -ntp
```

- Result: exit `0`, `0 Checkstyle violations` for selected MCP, MCP E2E, and MCP feature modules.

### EV-012

- Dimension: Final format gate.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,mcp/bootstrap,test/e2e/mcp \
  -DskipTests -DskipITs -Pcheck spotless:check -B -ntp
```

- Result: exit `0`, all selected MCP, MCP E2E, and MCP feature modules clean.

### EV-013

- Dimension: Production test readability.
- Status: `current`.
- Evidence: Extracted duplicated workflow handler test setup into `WorkflowHandlerTestFixture`.
- Result: `WorkflowExecutionToolHandlerTest` and `WorkflowValidationToolHandlerTest` now share one focused fixture while keeping concrete handler coverage.

### EV-014

- Dimension: E2E readiness diagnostics.
- Status: `current`.
- Evidence: Added bounded STDIO stderr diagnostics, Docker readiness diagnostics, and packaged distribution missing-home diagnostics.
- Evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dtest=MySQLRuntimeTestSupportTest,AbstractProcessMCPStdioInteractionClientTest,PackagedDistributionTestSupportTest \
  test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`, `Tests run: 9, Failures: 0, Errors: 0, Skipped: 0`.

### EV-015

- Dimension: STDIO runtime evidence.
- Status: `current`.
- Evidence command:

```bash
MCP_STDIO_TESTS=ProductionH2CapabilityDiscoveryE2ETest,ProductionH2MetadataResourceE2ETest,ProductionH2SQLExecutionE2ETest
MCP_STDIO_TESTS=$MCP_STDIO_TESTS,ProductionH2AiNativeInteractionE2ETest,ProductionMultiDatabaseE2ETest
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.production.stdio.enabled=true -Dtest="$MCP_STDIO_TESTS" test -B -ntp
```

- Result: exit `0`, `Tests run: 84, Failures: 0, Errors: 0, Skipped: 0`, total `08:25 min`.
- Note: `ProductionMultiDatabaseE2ETest` now asserts HTTP preserves `RuntimeDatabaseConnectionException` while STDIO preserves the process stderr diagnostic.

### EV-016

- Dimension: MySQL runtime evidence.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.production.mysql.enabled=true -Dmcp.e2e.production.stdio.enabled=true \
  -Dtest=ProductionMySQLRuntimeSmokeE2ETest test -B -ntp
```

- Result: exit `0`, `Tests run: 22, Failures: 0, Errors: 0, Skipped: 0`, total `02:55 min`.
- Runtime evidence: Testcontainers launched Docker-backed `mysql:8.0.36` and covered HTTP plus STDIO MySQL runtime behavior.

### EV-017

- Dimension: Packaged distribution assembly evidence.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl distribution/mcp -am -DskipTests package -B -ntp
```

- Result: exit `0`, all `50` reactor modules `SUCCESS`, total `15.463 s`.
- Runtime evidence: `distribution/mcp/target/apache-shardingsphere-mcp-5.5.4-SNAPSHOT` was assembled with `data`, `plugins`, and `logs` runtime directories.

### EV-018

- Dimension: Packaged distribution and plugin runtime evidence.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dmcp.e2e.distribution.enabled=true \
  -Dtest=PackagedDistributionSmokeE2ETest,PackagedDistributionPluginDiscoveryE2ETest test -B -ntp
```

- Result: exit `0`, `Tests run: 3, Failures: 0, Errors: 0, Skipped: 0`, total `6.124 s`.
- Runtime evidence: packaged HTTP smoke, packaged STDIO smoke, and packaged plugin discovery all passed with secret-safe runtime status assertions.

### EV-019

- Dimension: Live LLM usability evidence.
- Status: `current`.
- Evidence command:

```bash
MCP_LLM_RUN_ID=ra001-final-20260512015143 \
./mvnw -pl test/e2e/mcp -am -Pllm-e2e -DskipITs -Dspotless.skip=true \
  -Dtest=LLMSmokeE2ETest,LLMUsabilitySuiteE2ETest test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`, `Tests run: 5, Failures: 0, Errors: 0, Skipped: 0`, total `32:12 min`.
- Runtime evidence: `LLMSmokeE2ETest` passed `4` tests and `LLMUsabilitySuiteE2ETest` passed `1` test with Docker-backed Ollama.
- Artifact evidence:
  `test/e2e/mcp/target/llm-e2e/ra001-final-20260512015143/llm-usability-h2/core/scorecard.json`
  and
  `test/e2e/mcp/target/llm-e2e/ra001-final-20260512015143/llm-usability-h2/extended/scorecard.json`.
- Scorecard result: core and extended suites both reported `overallScore=100`, `fullScore=true`,
  `nativeToolCallRate=1`, `invalidCallRate=0`, `approvalViolationRate=0`, and `harnessRecoveryRate=0`.

### EV-020

- Dimension: Live LLM harness correctness and runtime stability.
- Status: `current`.
- Evidence: Reused one Docker-backed Ollama runtime across live LLM test classes, required tool calls until scenario coverage is complete,
  and made the extended LLM scorecard fail the Maven run unless it reaches full score.
- Evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dtest=LLMMCPConversationRunnerTest,LLMUsabilityScenarioCatalogTest,LLMUsabilityMetricCalculatorTest,LLMUsabilityReportWriterTest \
  test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`, `Tests run: 66, Failures: 0, Errors: 0, Skipped: 0`.
- Resulting guardrail: a green live LLM Maven run now requires both core and extended scorecards to be `100/100`.

### EV-021

- Dimension: Production and E2E safety boundary.
- Status: `current`.
- Evidence: Added `MCPClientSafetyPolicy`, `MCPToolCallLimiter`, `MCPToolCallLimitExceededException`, `rate_limited` error conversion,
  recovery guidance, and `security_hints.client_safety_policy` in `shardingsphere://capabilities`.
- Result: The enforced boundary is MCP-session scoped. Every tool call is counted before dispatch, invalid calls are counted, exhausted sessions return
  `rate_limited`, and the production runtime states that external model calls are outside the MCP server.

### EV-022

- Dimension: Production safety, protocol surface, and performance budget tests.
- Status: `current`.
- Evidence command:

```bash
MCP_SCORE_TESTS=MCPToolDescriptorTest,MCPClientSafetyPolicyTest,MCPModelFirstContractPayloadBuilderTest
MCP_SCORE_TESTS=$MCP_SCORE_TESTS,MCPToolCallLimiterTest,MCPToolControllerTest,MCPErrorConverterTest
MCP_SCORE_TESTS=$MCP_SCORE_TESTS,ServerCapabilitiesHandlerTest,ToolHandlerRegistryTest,MCPPerformanceBudgetSmokeTest
./mvnw -pl mcp/api,mcp/support,mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest="$MCP_SCORE_TESTS" \
  test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`; `mcp/api` `1` test, `mcp/support` `7` tests, and `mcp/core` `66` tests all passed.
- Budget result: `MCPPerformanceBudgetSmokeTest` ran `4` tests in `0.196 s`, covering descriptor generation, request-scope creation, metadata search,
  and SQL classification budgets.

### EV-023

- Dimension: E2E capability surface safety and compatibility.
- Status: `current`.
- Evidence command:

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dtest=ProductionH2CapabilityDiscoveryE2ETest,ProductionH2RuntimeSmokeE2ETest \
  test -Dsurefire.failIfNoSpecifiedTests=false -B -ntp
```

- Result: exit `0`, `Tests run: 7, Failures: 0, Errors: 0, Skipped: 0`.

### EV-024

- Dimension: Protocol, historical, safety, and performance traceability.
- Status: `current`.
- Evidence artifacts:
  `protocol-evidence-matrix.md`, `historical-evidence-map.md`, `safety-boundary.md`, and `performance-budget.md`.
- Result: Historical `011` evidence is mapped only through current `012` commands, protocol rows have production and E2E evidence, safety has an enforced
  session-scope boundary, and E2E lane budgets are explicit.

### EV-025

- Dimension: Final style gates for MCP API/support/core chain.
- Status: `current`.
- Evidence commands:

```bash
./mvnw -pl mcp/support,mcp/core -am -DskipTests -DskipITs -Dspotless.skip=true -Pcheck checkstyle:check -B -ntp
./mvnw -pl mcp/support,mcp/core -am -DskipTests -DskipITs -Pcheck spotless:check -B -ntp
```

- Result: both commands exited `0`; `mcp/api`, `mcp/support`, and `mcp/core` were Checkstyle and Spotless clean.

## Open Risks

None for this checkpoint.
