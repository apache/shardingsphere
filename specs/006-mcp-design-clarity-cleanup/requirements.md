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

# Requirements: MCP Design Clarity Cleanup

## 1. Purpose

This is the repo-visible handoff for `.specify/specs/010-mcp-design-clarity-cleanup/`.

The goal is to turn the current review findings for `shardingsphere-mcp` and `test/e2e/mcp` into an implementation-ready cleanup backlog.
The guiding rule is clarity first: remove mixed responsibilities and transitional behavior without introducing broad new abstractions.

## 2. Branch Constraint

- Current branch remains `001-shardingsphere-mcp`.
- Do not run `git switch`, `git checkout`, branch creation scripts, or Spec Kit commands that switch or create branches.
- Preserve unrelated dirty files in the working tree.

## 3. In-Scope Areas

- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/features/encrypt`
- `mcp/features/mask`
- `mcp/bootstrap`
- `test/e2e/mcp`

## 4. P0 Requirements: Core Clarity

- **DCC-P0-01 Statement classification boundary**: Narrow `StatementClassifier` to MCP safety/routing needs.
  Otherwise split tokenizer, CTE handling, and target-object extraction into explicit helpers with documented supported scope.
- **DCC-P0-02 Descriptor loader boundary**: Separate descriptor YAML IO, descriptor swapping, validation, legacy cleanup, and model-facing catalog payload construction.
- **DCC-P0-03 Bootstrap transport boundary**: Remove hard-coded encrypt/mask planning tool names and feature-specific workflow argument names from bootstrap transport.
- **DCC-P0-04 Public payload contract ownership**: Centralize or lightly type recurring public contracts such as `next_actions`, recovery hints, resource hints, and workflow apply payloads.
- **DCC-P0-05 Structured recovery**: Replace exception-message-prefix recovery decisions with error codes, structured exception data, or dedicated recovery providers.
- **DCC-P0-06 Legacy cleanup**: Remove legacy aliases and transitional fields from public model-facing contracts unless an internal-only fallback is explicitly justified.

## 5. Implementation Order

1. Remove bootstrap transport coupling to feature-specific planning tools and workflow argument names.
2. Clean public payload contracts and delete legacy aliases from model-facing output.
3. Split descriptor loading/catalog and structured recovery responsibilities.
4. Narrow or split statement classification and metadata/completion responsibilities.
5. Clean the MCP E2E runner, payload assertion helpers, smoke test grouping, and fixtures.

## 6. P1 Requirements: Service and Feature Testability

- **DCC-P1-01 Metadata search split**: Make metadata collection, ranking, pagination, and response assembly independently readable and testable.
- **DCC-P1-02 Completion ownership**: Move completion candidate production into core/support logic; keep bootstrap as protocol translation.
- **DCC-P1-03 Feature planner construction**: Add simple testable construction paths for encrypt and mask workflow planning services.
  Normal collaborator replacement should not require field reflection.
- **DCC-P1-04 Mocking alignment**: For touched tests, align static and constructor mocking with repository guidance or document why the standard extension cannot be used.

## 7. P2 Requirements: E2E Readability

- **DCC-P2-01 LLM runner split**: Split or simplify the LLM conversation runner by model loop, tool bridge, final-answer verification, safety validation, and artifact collection.
- **DCC-P2-02 E2E assertion helpers**: Replace repeated nested `Map<String, Object>` casts with named assertion helpers for common MCP payload shapes.
- **DCC-P2-03 Smoke test grouping**: Split broad smoke tests by product surface, such as metadata resources, tool schema, SQL execution, workflow guidance, and transport contracts.
- **DCC-P2-04 Fixture boundaries**: Separate generic H2/MySQL runtime setup from LLM scenario fixtures and repeated wait/retry behavior.

## 8. Non-Goals

- Do not add a DI framework, planner framework, semantic/vector search, cross-session memory, RBAC, approval-token system, rollback orchestration, or live LLM default CI gate.
- Do not change MCP product behavior just to make classes smaller.
- Do not rewrite the entire MCP module or E2E suite in one pass.

## 9. Acceptance Gates

- Bootstrap transport does not contain feature tool-name or feature argument-name branches.
- Descriptor loading no longer has one class responsible for IO, legacy fallback, validation policy, and model guidance construction.
- Statement classification has explicit responsibility boundaries and focused tests.
- Common model-facing payloads have discoverable contract ownership.
- E2E scenario tests avoid repeated raw nested casts for common payload shapes.
- Every implementation slice documents and tests intentional public behavior or contract changes.
- Final handoff includes branch status, commands with exit codes, scoped test evidence, Checkstyle status for touched Java modules, and remaining risks.

## 10. Verification Plan

Documentation-only changes:

```bash
git branch --show-current
git diff --check
```

Java changes:

```bash
./mvnw -pl mcp -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp -am -Pcheck checkstyle:check
```

E2E changes:

```bash
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true test
```

Use narrower `-Dtest=...` runs when only one small class set is touched.
