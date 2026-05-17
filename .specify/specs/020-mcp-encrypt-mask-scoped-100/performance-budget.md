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

# Performance Budget: MCP Encrypt/Mask Scoped Scorecard 100

## Budgets

- Descriptor and capability generation: 100 iterations within 5 seconds.
- Request-scope creation: 200 iterations within 5 seconds.
- Metadata search: 100 iterations within 5 seconds.
- Workflow planning payload generation: 1000 iterations within 5 seconds.
- Workflow `plan_id` completion: 1000 iterations within 5 seconds.
- SQL classifier: 1000 iterations within 5 seconds.
- Default MCP E2E lane: must finish without hangs; target remains below 60 seconds for background unit-style runs where infrastructure is not started.
- Distribution smoke and Docker/Testcontainers lanes: opt-in evidence, recorded separately from the default unit lane because they depend on local Docker runtime startup.
- LLM score lane: must avoid heavyweight runtime images; score-closing CI must use a project-owned local Docker image built from `llama.cpp` server with prepackaged `ggml-org/Qwen3-1.7B-GGUF:Q4_K_M` model evidence.

## Owning Tests

- `MCPPerformanceBudgetSmokeTest#assertDescriptorGenerationBudget`
- `MCPPerformanceBudgetSmokeTest#assertRequestScopeCreationBudget`
- `MCPPerformanceBudgetSmokeTest#assertMetadataSearchBudget`
- `MCPPerformanceBudgetSmokeTest#assertWorkflowPlanPayloadBudget`
- `MCPPerformanceBudgetSmokeTest#assertWorkflowPlanIdCompletionBudget`
- `MCPPerformanceBudgetSmokeTest#assertSQLClassifierBudget`

## Reliability Guardrails

- Workflow apply must not execute side effects during preview.
- HTTP session cleanup must close the target session without deleting other sessions.
- Docker-owned LLM lanes must start their own `llama.cpp` server runtime and must not count external debug endpoints as score evidence.
- MySQL/Proxy product-path lanes must wait for database readiness before attempting JDBC or Proxy workflow calls.

## Verification Commands

```bash
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true \
  -Dtest=MCPPerformanceBudgetSmokeTest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

```bash
./mvnw -pl test/e2e/mcp -DskipITs -Dspotless.skip=true \
  -Dsurefire.failIfNoSpecifiedTests=false test
```

Distribution smoke remains opt-in:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true \
  -Dtest=PackagedDistributionSmokeE2ETest \
  -Dsurefire.failIfNoSpecifiedTests=false test
```
