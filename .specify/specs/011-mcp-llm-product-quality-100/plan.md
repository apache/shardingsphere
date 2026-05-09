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

# Implementation Plan: MCP LLM Product Quality 100

## Objective

Move the full MCP production and E2E scope from strict product-quality score **78/100** to **100/100**.

The implementation order optimizes for model-facing proof first, then code boundaries that prevent regressions, then operational hardening.

## Current Baseline

- Branch: `001-shardingsphere-mcp`
- Previous design-clarity score: `100/100`
- New strict model/product score: `78/100`
- Main gap: real LLM experience is not proven by enough natural tasks, contract drift protection, recovery metrics, and runtime diagnostics.
- Mandatory live LLM gate: Dockerized Ollama with model id `qwen3:1.7b`.
- MCP E2E must prepare the Dockerized Ollama environment and pull the model automatically.

## Non-Negotiable Constraints

- Do not switch branches.
- Do not use documentation, JavaDoc, or comments as the fix for unclear behavior.
- Do not add broad frameworks just to increase score.
- Do not make live LLM tests mandatory in default CI.
- Do not accept another live model as the required 100-point gate unless the user changes the decision.
- Do not require a paid external API key or cloud model for the mandatory 100-point gate.
- Do not retain compatibility aliases; the MCP contract is unreleased.

## Execution Strategy

1. Lock the strict scorecard and natural-scenario gate before implementation.
2. Replace scripted LLM prompts with natural tasks and measurable assertions.
3. Add golden contract protection for all model-facing public payloads.
4. Make recovery categories and next actions easier for models to follow.
5. Harden safety boundaries around SQL and workflows.
6. Split LLM scenarios into core blocking gates and extended scored gates.
7. Keep deterministic extended-scenario assertions hard while recording model-performance misses as scores.
8. Reduce code hotspots only where they block contract stability or reading cost.
9. Add runtime and packaged diagnostic coverage.
10. Update the final score only after verification evidence passes.

## Phase Plan

### Phase 0 - Governance and Baseline

- Confirm branch and no switch operations.
- Record current 78/100 strict baseline.
- Record weighted score model and target gates.
- Mark live LLM tests as opt-in for default CI while making the full 100-point gate self-preparing through Dockerized Ollama.

### Phase 1 - Natural LLM Usability Gate

- Convert usability scenarios from scripted first-call prompts to natural user intents.
- Keep a separate protocol contract suite for explicit first-call requirements.
- Track task success, first-action accuracy, invalid-call rate, round trips, recovery success, and approval violations.
- Add a score gate that fails below full target.
- Add extended scenario scoring that does not block for model-performance misses.
- Keep deterministic extended checks as hard JUnit assertions.

### Phase 2 - Model-First Surface and Contract Snapshots

- Add compact capability summary for first-hop model use.
- Generate model-facing bridge tool definitions from production descriptors or shared contracts.
- Add golden snapshots for capabilities, tools, resources, prompts, completions, errors, and workflow outputs.
- Add schema drift tests for canonical field vocabulary.

### Phase 3 - Recovery and Safety

- Refine recovery categories into stable model-facing classes.
- Ensure each recovery response has one primary next path.
- Expand missing-context, bad-resource, bad-enum, SQL mismatch, and workflow context recovery tests.
- Harden SQL and workflow preview-first approval gates.

### Phase 4 - Code Boundary Hardening

- Keep `MCPErrorConverter` from becoming a policy sink by extracting stable recovery families only where needed.
- Keep catalog payload construction readable through model contract builders or focused factories.
- Replace recurring raw map payload assembly with typed builders only when it reduces drift or duplication.

### Phase 5 - Operations and Release Readiness

- Add safe runtime diagnostic categories for missing driver, authentication failure, connection timeout, invalid configuration, and transport validation failure.
- Add packaged HTTP and STDIO diagnostic smoke tests where practical.
- Confirm no secret leakage in diagnostic payloads or LLM artifacts.

### Phase 6 - Verification and Final Score

- Run branch check.
- Run scoped unit tests.
- Run scoped MCP E2E.
- Run Checkstyle and `git diff --check`.
- Run opt-in live LLM suite after E2E starts Dockerized Ollama and pulls `qwen3:1.7b`.
- Update `scorecard.md` from 78/100 to 100/100 only when all gates pass.

## Verification Commands

Documentation-only planning update:

```bash
git branch --show-current
git diff --check
```

Implementation slices:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features,mcp/bootstrap -am -DskipITs -Dspotless.skip=true test
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipITs -DskipTests checkstyle:check
```

Live LLM gate remains opt-in for default CI, but the 100-point model is fixed.
The implementation must prepare Dockerized Ollama and pull `qwen3:1.7b` before running this command:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=LLMUsabilitySuiteE2ETest \
  -Dmcp.e2e.llm.enabled=true -Dmcp.llm.base-url=http://127.0.0.1:11434/v1 -Dmcp.llm.provider=openai-compatible \
  -Dmcp.llm.model=qwen3:1.7b -Dmcp.llm.api-key=ollama -Dsurefire.failIfNoSpecifiedTests=false test
```
