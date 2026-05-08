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

# Scorecard: MCP Design Clarity Cleanup

## Scope

This scorecard covers the whole MCP implementation area, not only `mcp/api`.

- Production modules: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features`, and
  `mcp/bootstrap`.
- End-to-end module: `test/e2e/mcp`.
- Out of scope: release packaging and user documentation, except where they
  directly expose unclear MCP behavior.

## Current Full-Scope Score

Score timestamp: after the final code cleanup and focused verification in the
working tree. The score covers the full production MCP surface and
`test/e2e/mcp`.

- Overall score: **100 / 100**
- Production MCP modules: **70 / 70**
- MCP E2E module: **30 / 30**

Category view:

- Design: **100 / 100**
- Readability: **100 / 100**
- Convenience: **100 / 100**

The final shape reaches 100 by code boundaries, helper names, construction
paths, and tests. A generic E2E retry framework was rejected because the
remaining readiness loops have different lifecycles and exception semantics.

## Production Score

- API and public contract purity: **10 / 10**
  - Canonical descriptor state is now clean.
  - Legacy descriptor compatibility has been removed.
  - Named factories make value-definition intent visible at call sites.
- Error recovery and next-action contract: **8 / 8**
  - Typed MCP exceptions replaced message-prefix recovery detection.
  - Recovery payload keys are more stable and test-covered.
- Descriptor loading and catalog boundary: **10 / 10**
  - `MCPDescriptorCatalogLoader` now orchestrates YAML loading, descriptor
    swapping, catalog validation, and catalog return.
  - `MCPDescriptorCatalogYamlLoader` owns classpath/YAML reading.
  - `MCPDescriptorCatalogYamlSwapper` owns YAML-to-descriptor conversion.
  - `MCPDescriptorCatalogValidator` owns descriptor contract validation.
  - `MCPDescriptorCatalogPayloadBuilder` owns model-facing payload and
    fingerprint construction.
- Transport and completion boundary: **10 / 10**
  - `MCPCompletionService` owns candidate production, ranking, and metadata
    lookup.
  - Bootstrap completion code only adapts MCP SDK request and response objects.
- Metadata search pipeline: **8 / 8**
  - `SearchMetadataToolService` now orchestrates validation, type resolution,
    collection, and paging.
  - `MetadataSearchCollector` owns metadata traversal and hit construction.
  - `MetadataSearchMatcher` owns match explanations.
  - `MetadataSearchPaginator` owns ordering, page tokens, and search context.
  - `MetadataSearchResourceUriFactory` owns derived resource URIs.
- Statement classification boundary: **8 / 8**
  - `StatementClassifier` now owns high-level policy and result assembly.
  - `SQLStatementScanner` owns lexical scanning.
  - `SQLStatementStructureResolver` owns CTE and main-statement structure.
  - `SQLStatementClassResolver` owns statement-class mapping.
  - `SQLStatementTargetResolver` owns target object extraction.
- Feature planning testability: **6 / 6**
  - Encrypt and mask workflow planning services now expose constructor-based
    collaborator wiring.
  - Touched planning tests no longer replace ordinary collaborators through
    field reflection.
- Production verification shape: **10 / 10**
  - Focused unit tests, full production MCP module tests, and scoped
    Checkstyle passed after the final cleanup.

Production subtotal: **70 / 70**

## E2E Score

- Scenario harness boundary: **7 / 7**
  - `LLMMCPConversationRunner` keeps the conversation loop.
  - `LLMMCPActionExecutor`, `LLMMCPSafetyValidator`,
    `LLMMCPFinalAnswerValidator`, `LLMMCPConversationArtifacts`, and
    `LLMMCPInteractionCoverage` own the surrounding responsibilities.
- Payload assertion clarity: **7 / 7**
  - `MCPPayloadAssertions` centralizes repeated payload item, map-list, and
    tool-definition assertions for the touched H2 scenarios.
  - LLM JSON and tool definition helpers keep model-facing payload shape out of
    the runner loop.
- Production smoke grouping: **5 / 5**
  - H2 production smoke coverage is split by product surface: capability
    discovery, metadata resources, SQL execution, and AI-native interaction.
- Runtime fixtures, wait, and retry helpers: **5 / 5**
  - H2 runtime setup is now a shared fixture base for product-surface classes.
  - Distinct readiness loops remain local where their exception semantics are
    materially different.
- LLM opt-in, artifact, and scoring clarity: **6 / 6**
  - Live LLM execution remains opt-in.
  - Artifact state, final-answer validation, tool-call safety, and tool bridge
    are separately readable.

E2E subtotal: **30 / 30**

## 100-Point Gates

### Passed For 100

- `MCPDescriptorCatalogLoader` is split into named phases whose public methods
  read as the descriptor lifecycle: load YAML, swap to descriptors, validate,
  assemble catalog.
- `MCPDescriptorCatalog` is an immutable holder, not the owner of large
  model-facing payload construction.
- Completion candidate production can be tested without MCP transport
  bootstrap.
- Recurring MCP payload contracts are named once at the production boundary and
  consumed by tests through helper methods.
- `SearchMetadataToolService` exposes the product operation while smaller
  collaborators handle collection, matching, paging, and resource URI assembly.
- `StatementClassifier` becomes an orchestration class around clearly named SQL
  splitting and policy components.
- `test/e2e/mcp` scenario tests assert product behavior through named helpers,
  with repeated raw casts confined to thin helper boundaries in the touched
  paths.
- H2 production smoke scenarios are split by product surface.
- LLM conversation E2E separates conversation loop, MCP tool bridge,
  validation, artifact writing, and scoring.
- Scoped checks pass for the touched MCP production modules and `test/e2e/mcp`.

### Explicit Non-Gates

- Do not chase score through Javadocs or comments. Required Javadocs stay only
  because the project rules require them; code shape carries the explanation.
- Do not keep legacy descriptor compatibility. The unpublished contract should
  collapse to the final target shape.
- Do not introduce a generic framework just to reduce file length. Extract only
  boundaries that reduce concrete reading cost.

## Recommended Work Order

1. Completion service boundary cleanup.
   - Score impact: **+2**
   - Reason: it removes bootstrap transport pressure from candidate selection.
2. E2E payload assertion helpers.
   - Score impact: **+3**
   - Reason: this immediately raises readability across the largest test files.
3. Split H2 production smoke scenarios by product surface.
   - Score impact: **+2**
   - Reason: failures will name broken behavior instead of forcing map-level
     debugging.
4. Split LLM conversation runner responsibilities.
   - Score impact: **+2**
   - Reason: the opt-in path is important but should not be a monolith.
5. Metadata search internal pipeline cleanup.
   - Score impact: **+1**
   - Reason: good return on clarity with modest behavior risk.
6. Statement classifier cleanup.
   - Score impact: **+1**
   - Reason: SQL structure and target extraction are explicit review units.
7. Final scoped verification and score re-audit.
   - Score impact: **+1**
   - Reason: 100 requires proof, not just a cleaner shape.

## Required Evidence

Each implementation slice must provide scoped command evidence.
Use narrower test commands when possible.

```bash
git branch --show-current
git diff --check
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features,mcp/bootstrap -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipITs -DskipTests checkstyle:check
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true test
```

The final handoff must state which gates passed, which commands ran, and
whether any intentional public contract break remains.

### Evidence For Final 100-Point State

```bash
git branch --show-current
./mvnw -pl mcp/core -am -DskipITs -Dspotless.skip=true -Dtest=SearchMetadataToolServiceTest,StatementClassifierTest -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl mcp/core -am -Pcheck -DskipITs -DskipTests checkstyle:check
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=LLMMCPConversationRunnerTest -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true -Dtest=EncryptWorkflowPlanningServiceTest,MaskWorkflowPlanningServiceTest -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true -Dtest=ProductionH2CapabilityDiscoveryE2ETest,ProductionH2MetadataResourceE2ETest,ProductionH2SQLExecutionE2ETest,ProductionH2AiNativeInteractionE2ETest -Dsurefire.failIfNoSpecifiedTests=false test
```

All listed commands passed in the final implementation path. The final handoff
also records the full production MCP module test, combined Checkstyle, E2E
module test, `git diff --check`, and final branch check.

### Rejected Extra Abstraction

- A generic E2E wait/retry framework was not added because MySQL readiness,
  packaged HTTP readiness, and LLM model readiness have different return types,
  retry intervals, and failure semantics.
- More DTO layers for every response map were not added because the cleaned
  helper boundaries already make the recurring payload contracts discoverable.
