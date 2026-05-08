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

# 100-Point Roadmap: MCP Design Clarity Cleanup

## Scope And Baseline

Scope:

- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/features`
- `mcp/bootstrap`
- `test/e2e/mcp`

Initial score: **85/100**.

- Production MCP: **62/70**
- MCP E2E: **23/30**

Current score after final cleanup: **100/100**.

- Production MCP: **70/70**
- MCP E2E: **30/30**

Target score: **100/100**.

The target is reached only through code shape, names, types, factories,
helpers, and tests. Comments and JAVADOC do not count as clarity fixes.

## Principles

- Prefer deletion over compatibility when the clearer contract is known.
- Prefer one obvious class responsibility over large "manager" classes.
- Prefer one named helper over repeated nested maps.
- Prefer focused tests over broad fixture assertions.
- Do not introduce a framework when a small named helper explains the code.

## Phase 1: Descriptor And Catalog Boundary

Score impact: **+3**
Status: **Done**

Primary files:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoader.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`

Recommended shape:

- Keep `MCPDescriptorCatalog` as an immutable holder.
- Extract classpath/YAML reading into a loader whose only job is to read
  descriptor documents.
- Extract YAML-to-descriptor conversion into a swapper.
- Extract descriptor lint rules into a validator.
- Extract model-facing catalog payload construction into a named payload
  builder.

Stop condition:

- A reader can trace descriptor lifecycle as load, swap, validate, assemble,
  build payload without reading unrelated rules.

Avoid:

- A generic descriptor framework.
- Reintroducing legacy descriptor compatibility.
- Moving confusion into comments.

## Phase 2: Completion Boundary

Score impact: **+2**
Status: **Done**

Primary file:

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactory.java`

Recommended shape:

- Move candidate production and ranking into a production-side service.
- Keep bootstrap responsible for MCP SDK request/response translation.
- Keep workflow-specific ranking data descriptor-driven or feature-owned.

Stop condition:

- Completion candidate behavior can be tested without starting MCP transport.

Avoid:

- Hard-coding feature resource URIs or workflow argument names in transport
  adapters.

## Phase 3: E2E Payload Assertions

Score impact: **+3**
Status: **Done**

Primary files:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ProductionH2RuntimeSmokeE2ETest.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPConversationRunnerTest.java`
- Shared E2E support under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp`

Recommended shape:

- Start with one named assertion helper for common MCP payload shapes.
- Move raw nested map reads into that helper.
- Split the helper only if it grows into unrelated product surfaces.

Stop condition:

- Scenario tests say what product behavior is expected before they reveal JSON
  transport shape.

Avoid:

- A broad test framework.
- Rebuilding MCP tool schemas by hand in every test.

## Phase 4: H2 Production Smoke Split

Score impact: **+2**
Status: **Done**

Primary file:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/runtime/production/ProductionH2RuntimeSmokeE2ETest.java`

Recommended shape:

- Split smoke scenarios by product surface:
  metadata resources, SQL tools, workflow guidance, and transport contracts.
- Keep shared runtime setup in fixtures, not in scenario classes.

Stop condition:

- A failing smoke class name identifies the broken product surface.

Avoid:

- Copying runtime setup into every split class.

## Phase 5: LLM E2E Runner Split

Score impact: **+2**
Status: **Done**

Primary file:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPConversationRunner.java`

Recommended shape:

- Separate conversation loop.
- Separate MCP tool bridge.
- Separate final-answer validation.
- Separate safety validation.
- Separate artifact writing and scoring.

Stop condition:

- A reader can understand the opt-in LLM flow without also understanding
  payload decoding, artifact formatting, and score calculation at the same
  time.

Avoid:

- Making live LLM execution part of default CI.

## Phase 6: Metadata Search Pipeline

Score impact: **+1**
Status: **Done**

Primary file:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java`

Recommended shape:

- Keep the public service as the product operation.
- Extract matching, paging, and payload assembly only where it lowers reading
  cost.

Stop condition:

- Adding a metadata object type does not require editing unrelated pagination
  or response-shape code.

## Phase 7: Statement Classification Boundary

Score impact: **+1**
Status: **Done**

Primary file:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/StatementClassifier.java`

Recommended shape:

- Keep `StatementClassifier` as orchestration.
- Make SQL splitting, lexical policy, parser-backed shape checks, and approval
  validation separately named.

Stop condition:

- SQL safety behavior is explicit enough to review without treating the class
  as a hidden SQL parser.

## Phase 8: Verification And Re-Score

Score impact: **+1**
Status: **Done**

Required evidence:

```bash
git branch --show-current
git diff --check
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features,mcp/bootstrap -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features,mcp/bootstrap,test/e2e/mcp -am -Pcheck -DskipITs -DskipTests checkstyle:check
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true test
```

Stop condition:

- `scorecard.md` records production MCP as 70/70, MCP E2E as 30/30, and whole
  scope as 100/100.
