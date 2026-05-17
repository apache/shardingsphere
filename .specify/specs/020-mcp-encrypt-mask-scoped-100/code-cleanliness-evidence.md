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

# Code Cleanliness Evidence: MCP Encrypt/Mask Scoped Scorecard 100

## Reflection

- Search command: `rg "getDeclaredMethod|setAccessible|invoke\\(" mcp test/e2e/mcp`.
- No MCP test invokes private methods by reflection and no `setAccessible` usage is present.
- The remaining bounded reflection is field injection through `Plugins.getMemberAccessor()` or public-method invocation in registry tests. That matches the repository rule for unavoidable field access and does not expand public production APIs for tests.

## Mocking

- Search command: `rg "mockStatic|mockConstruction" mcp test/e2e/mcp`.
- Direct constructor mocking remains in `MCPRuntimeLauncherTest` because the public behavior is server selection, startup failure cleanup, and startup log generation; adding a new factory seam only for the test would over-design the launcher.
- Direct static mocking remains only where the owning test uses try-with-resources or a cleanup method. No touched test added static mocking.
- `OllamaLLMRuntimeSupportTest` keeps one local `mockStatic(MySQLRuntimeTestSupport.class)` because importing the shared AutoMock extension into `test/e2e/mcp` only for Docker availability probing would broaden the module boundary.

## String Assertions

- `MCPPromptSpecificationFactoryTest` no longer uses broad `containsString`; it now checks rendered prompt lines through exact line membership.
- Touched encrypt/mask handler tests assert structured `resources_to_read`, `next_actions.tool_name`, `next_actions.arguments`, and absence of the stale `required_arguments` alias.
- Remaining `containsString` assertions are limited to free-form text, HTTP challenge headers, logs, or documentation snippets where structured JSON parsing is not the tested contract.

## Checkstyle Suppressions

- Search command: `rg "CHECKSTYLE:OFF|CHECKSTYLE:ON" mcp test/e2e/mcp`.
- Remaining suppressions are local exception-boundary cases: CLI `main` methods that surface `IOException`, recovery/validation paths that convert runtime failures into model-facing payloads, and optional metadata synchronization where the next validation layer reports the failure.
- No new Checkstyle suppression was added in this implementation.

## Closure Mapping

- T060: private-method reflection is absent; field-only member access is bounded and documented.
- T061: direct static/constructor mocking was reviewed and only retained where migration would add broader infrastructure or reduce readability.
- T062: touched payload and prompt assertions now prefer structured or exact-line assertions.
- T063: remaining Checkstyle suppressions are documented as boundary/error-conversion cases.
- T064: final style gate is required before score closure.
