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

**Decision**: Focus requirements on readability, responsibility boundaries, contract discoverability, legacy cleanup, and E2E clarity.

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
Keep internal-only fallback parsing only when a focused test proves it is still needed during implementation.

**Rationale**: The user confirmed compatibility does not need to be preserved.
The cleanest contract should win when readability, elegance, and consistency improve.

**Rejected Alternative**: Preserve aliases behind compatibility adapters.
That would keep transitional design in the product surface and weaken the clarity goal.

## Decision 7: Keep E2E Tests Scenario-Centered

**Decision**: Split the LLM conversation runner and add assertion helpers only where they make scenarios read like product behavior.

**Rationale**: E2E code should tell what behavior is protected. It should not require each test to reconstruct protocol payload mechanics.

**Rejected Alternative**: Build a generic test automation framework. The E2E module needs a smaller harness, not a larger one.

## Evidence Inputs

The requirement inventory is based on static review of these areas:

- `mcp/core` statement classification, metadata search, workflow execution, and error conversion.
- `mcp/support` descriptor catalog and loader.
- `mcp/bootstrap` tool and completion specification factories.
- `mcp/features/encrypt` and `mcp/features/mask` workflow planning services and tests.
- `test/e2e/mcp` LLM conversation runner, runtime fixtures, production smoke tests, HTTP contract tests, and usability metrics.

## Open Questions for Implementation

- Should statement classification use existing ShardingSphere parser metadata directly, or stay lightweight with documented lexical scope?
- Which E2E test classes are stable enough to split first without increasing flakiness?
- Can descriptor linting cover the public contract drift without requiring a full MCP runtime startup?
