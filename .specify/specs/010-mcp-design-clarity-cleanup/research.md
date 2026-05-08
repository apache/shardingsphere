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

# Research: MCP Design Clarity Cleanup

## Decision 1: Create This Speckit Package Manually

**Decision**: Create `.specify/specs/010-mcp-design-clarity-cleanup` without running branch-creation scripts.

**Rationale**: The user explicitly forbids branch switching. The existing repository keeps Speckit packages under `.specify/specs`.
Manual creation preserves the workflow without violating the branch constraint.

**Rejected Alternative**: Run a standard Spec Kit feature command and let it create or switch branches. This violates the user constraint.

## Decision 2: Treat This as a Clarity Cleanup, Not a Product Expansion

**Decision**: Focus requirements on readability, responsibility boundaries, contract discoverability, legacy deletion, and E2E clarity.

**Rationale**: The evaluated issues are over-design, transitional design, inconsistent style, and unclear code. Adding new MCP capabilities would increase scope and make the cleanup harder to verify.

**Rejected Alternative**: Add a new agent, planner, semantic search, broad schema registry, or live LLM gate. These are explicitly outside a clarity-first cleanup.

## Decision 3: Prefer Small Named Helpers Over Broad Architecture

**Decision**: Split only when a helper has a clear responsibility such as tokenization, target extraction, descriptor validation, payload construction, or E2E assertion.

**Rationale**: The user values clarity over abstraction. A helper is justified only when it removes a mixed responsibility or duplicated contract knowledge.

**Rejected Alternative**: Introduce a DI framework, plugin framework, general SQL analysis framework, or full DTO graph for every response. That would replace one form of complexity with another.

## Decision 4: Make Bootstrap Transport Descriptor-Driven

**Decision**: Bootstrap transport adapters should not know encrypt/mask planning tool names or feature-specific argument names.

**Rationale**: Transport should translate MCP protocol requests and responses. Feature semantics belong to descriptors, core services, or feature modules.

**Rejected Alternative**: Keep adding tool-name branches in bootstrap for every workflow. This scales poorly and makes feature changes risky.

## Decision 5: Centralize Only High-Frequency Public Payload Contracts

**Decision**: Add typed models or centralized factories/constants only for recurring public contracts such as `next_actions`, recovery hints, resource hints, and workflow apply payloads.

**Rationale**: These fields are model-facing and appear across descriptors, tools, resources, tests, and docs. Centralizing them reduces typo risk and review cost.

**Rejected Alternative**: Convert every response map into a deep typed model hierarchy. That would overfit the current shape and slow small changes.

## Decision 6: Remove Legacy Aliases from Public Contracts

**Decision**: Remove legacy aliases and transitional fields from public model-facing contracts.
Do not preserve compatibility adapters for unpublished descriptor or payload shapes.

**Rationale**: The user confirmed compatibility does not need to be preserved.
The cleanest contract should win when readability, elegance, and consistency improve.

**Rejected Alternative**: Preserve aliases behind compatibility adapters or internal fallback parsing.
That would keep transitional design in the product surface and weaken the clarity goal.

## Decision 7: Keep E2E Tests Scenario-Centered

**Decision**: Split the LLM conversation runner and add assertion helpers only where they make scenarios read like product behavior.

**Rationale**: E2E code should tell what behavior is protected. It should not require each test to reconstruct protocol payload mechanics.

**Rejected Alternative**: Build a generic test automation framework. The E2E module needs a smaller harness, not a larger one.

## Decision 8: Let Code Explain the Design

**Decision**: Requirements for the 100/100 target must be satisfied by code names, types, factories, helpers, and tests rather than JAVADOC, ordinary comments, README text, or handoff prose.

**Rationale**: The user explicitly wants clarity to come from code. Repository style also says code that needs comments should be extracted into small methods with meaningful names.

**Rejected Alternative**: Keep unclear constructors, compatibility logic, or mixed responsibilities and explain them with comments. That would preserve the readability problem.

## Decision 9: Score the Whole MCP Surface, Not an API Slice

**Decision**: The scorecard must cover production MCP modules plus `test/e2e/mcp`.
The `mcp/api` cleanup is treated as completed evidence inside the production
score, not as the final module score.

**Rationale**: The user clarified that the target is the whole MCP module and
the MCP end-to-end tests. The highest-value cleanup points were outside
`mcp/api`: descriptor loading, completion, metadata search, statement
classification, and E2E harness code.

**Rejected Alternative**: Keep an API-only perfect result and list other modules
as a vague backlog. That hides the actual work and misrepresents the
module score.

## Evidence Inputs

The requirement inventory is based on static review of these areas:

- `mcp/core` statement classification, metadata search, workflow execution, and error conversion.
- `mcp/support` descriptor catalog and loader.
- `mcp/bootstrap` tool and completion specification factories.
- `mcp/features/encrypt` and `mcp/features/mask` workflow planning services and tests.
- `test/e2e/mcp` LLM conversation runner, runtime fixtures, production smoke tests, HTTP contract tests, and usability metrics.

## Final Findings For 100/100

1. `MCPDescriptorCatalogLoader` and `MCPDescriptorCatalog` are split by
   descriptor lifecycle and payload construction.
2. `MCPCompletionSpecificationFactory` no longer owns candidate production.
3. E2E payload assertions are named in helpers before scenario assertions reach
   into nested payloads.
4. `LLMMCPConversationRunner` is split from the MCP tool bridge, safety
   validator, final-answer validator, interaction coverage, and artifact state.
5. `SearchMetadataToolService` orchestrates collaborators for collection,
   matching, paging, and resource URI derivation.
6. `StatementClassifier` orchestrates scanner, structure resolver, class
   resolver, and target resolver helpers.

## Closed Implementation Questions

- Statement classification stays lightweight. Its scope is encoded by
  `SQLStatementScanner`, `SQLStatementStructureResolver`,
  `SQLStatementClassResolver`, `SQLStatementTargetResolver`, and focused tests.
- The H2 production smoke tests were split first because they exercise real
  runtime paths without requiring external services.
- Descriptor linting and contract tests cover public contract drift without
  requiring a full MCP runtime startup.
- A generic E2E wait/retry helper was rejected because the remaining loops have
  different return types, retry intervals, and failure semantics.
