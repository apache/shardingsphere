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

# Architecture Cleanliness Evidence: Phase 5

## Scope

- Package: `019-mcp-encrypt-mask-scorecard-100`
- Date: 2026-05-16
- Branch constraint: no branch-changing command was used.
- Functional scope: encrypt and mask workflow completion only.

## Source-Driven Boundary

- Official MCP Tools `2025-11-25` defines tool `inputSchema` as JSON Schema and requires it to be a valid JSON Schema object.
- The local `MCPToolArgumentContract` remains a bounded execution guard, not a general JSON Schema engine.
- The local guard intentionally covers the parts that protect handlers before dispatch: required fields, primitive types, enum values, arrays, recursive objects, and `additionalProperties: false`.
- Full SQL grammar parsing remains owned by ShardingSphere parser modules.
  The MCP `StatementClassifier` stays a lightweight execution safety scanner for statement class, banned commands, and best-effort target extraction.

## T040 Nullable Return Review

- Refactored `SQLStatementTargetResolver#findCommonTableExpression` from a nullable return to `Optional<SQLCommonTableExpression>`.
- Kept tri-state `Boolean` returns in encrypt intent handling because `null` means "unknown / ask user", distinct from explicit `false`.
- Kept YAML and environment placeholder nulls where the framework represents absent optional configuration as `null`.
- Kept snapshot copy nulls where optional lifecycle timestamps and workflow state may be absent before the step exists.
- Kept response normalization methods that convert nullable external values into empty collections or strings at module edges.

## T041 Static and Constructor Mock Review

- Direct Mockito static and constructor mocks were searched across `mcp` and `test/e2e/mcp`.
- A trial migration of simple `mcp/core` static mocks to `AutoMockExtension` failed at test compilation because `mcp/core` does not expose `test-infra-framework` on its test classpath.
- Adding that test dependency only for style would widen module coupling, so the implementation keeps scoped `try-with-resources` mocks in `mcp/core`.
- Constructor mocks in bootstrap and SQL execution tests are retained because they require per-construction behavior, constructed-instance inspection,
  or failure injection that `AutoMockExtension` does not expose.
- Isolated classloader registry tests retain `try-with-resources` so the mocked service loader is scoped exactly to the class initializer under test.

## T042 Workflow Payload Field Boundary

- Added `WorkflowFieldNames` as the shared field-name vocabulary for encrypt/mask workflow planning arguments and payloads.
- Replaced duplicated field literals in request binding, plan/guidance/validation payloads, intent inference, workflow artifacts,
  descriptor validation, and encrypt/mask planning handlers.
- External MCP field names are unchanged.

## T043 Input Schema and SQL Scanner Tests

- Added nested `additionalProperties: false` coverage in `ToolHandlerRegistryTest` through `MCPToolArgumentContract`.
- Added `VALUES` derived-table coverage in `StatementClassifierTest` to pin the "do not invent a target object" boundary.
- Rechecked MCP Tools source for `inputSchema` and tool-result contracts instead of expanding the custom validator into speculative full JSON Schema support.

## T044 Historical Score Reconciliation

- Historical packages such as `012` and `016` remain evidence sources.
- This package is the active encrypt/mask checkpoint and does not inherit historical 100/100 closure without current commands, source evidence, and artifact links.

## Command Evidence

```bash
./mvnw -pl mcp/core,mcp/support,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true \
  -Dtest=<focused support/core test set> \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: focused support/core tests passed; `111` tests run with `0` failures, `0` errors, `0` skipped.
- Focused support/core test set: `MCPHandlerLoaderTest`, `WorkflowRuntimeDefinitionRegistryTest`, `MCPToolControllerTest`,
  `MCPResourceControllerTest`, `ToolHandlerRegistryTest`, `StatementClassifierTest`, `WorkflowRequestBinderTest`,
  `WorkflowPlanPayloadBuilderTest`, and `WorkflowGuidancePayloadBuilderTest`.

```bash
./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask -am -DskipITs -Dspotless.skip=true \
  -Dtest=WorkflowRequestBinderTest,WorkflowPlanPayloadBuilderTest,EncryptToolHandlerTest,MaskToolHandlerTest,EncryptWorkflowPlanningServiceTest,MaskWorkflowPlanningServiceTest \
  -Dsurefire.failIfNoSpecifiedTests=false test -B -ntp
```

- Exit code: `0`
- Result: focused support/encrypt/mask tests passed; `38` tests run with `0` failures, `0` errors, `0` skipped.

```bash
./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask -am -Pcheck -DskipTests -DskipITs checkstyle:check -B -ntp
./mvnw -pl mcp/core -Pcheck -DskipTests -DskipITs spotless:apply -B -ntp
./mvnw -pl mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask -Pcheck -DskipTests -DskipITs spotless:check -B -ntp
```

- Exit codes: `0`, `0`, `0`
- Result: scoped Checkstyle and Spotless gates passed after applying the repository's Java blank-line formatting in `ToolHandlerRegistryTest`.

## Non-Closure Notes

- Phase 5 evidence improves architecture, implementation elegance, code cleanliness, and unit-test quality.
- Scores still must not move to 100 until Phase 6 and Phase 7 gates are complete.
