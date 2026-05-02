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

# Tasks: Unify MCP Handler Contracts

**Input**: `specs/001-mcp-handler-unification/spec.md` and `specs/001-mcp-handler-unification/plan.md`  
**Constraint**: Do not switch branches. Do not perform implementation bulk edits or file deletions until explicitly confirmed.

## Phase 0: Safety Gate

- [x] T001 Confirm implementation approval before deleting old interfaces or doing wide renames.
- [x] T002 Confirm branch is still `001-shardingsphere-mcp` with `git branch --show-current`.
- [x] T003 Confirm no generated `target/` paths are included in edit scope.
- [x] T004 Re-read `AGENTS.md` and `CODE_OF_CONDUCT.md` immediately before coding.

## Phase 1: API Contracts

- [x] T005 Add `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/handler/MCPHandlerContext.java`.
- [x] T006 Add `mcp/core/src/main/java/org/apache/shardingsphere/mcp/context/MCPServiceHandlerContext.java`.
- [x] T007 Rename `MCPResourceContribution` to `MCPResourceHandler<T extends MCPHandlerContext>`.
- [x] T008 Add `Class<T> getContextType()` and generic `handle(T, MCPUriVariables)` to `MCPResourceHandler`.
- [x] T009 Rename `MCPToolContribution` to `MCPToolHandler<T extends MCPHandlerContext>`.
- [x] T010 Add `Class<T> getContextType()` and generic `handle(T, MCPToolCall)` to `MCPToolHandler`.
- [x] T011 Rename `MCPContributionProvider` to `MCPHandlerProvider`.
- [x] T012 Rename provider methods to `getResourceHandlers()` and `getToolHandlers()`.
- [x] T013 Update API unit tests for renamed provider and generic handler contracts.
- [x] T013a Keep `MCPResourceHandler` in `org.apache.shardingsphere.mcp.api.resource`.
- [x] T013b Keep `MCPToolHandler` in `org.apache.shardingsphere.mcp.api.tool`.
- [x] T013c Keep `MCPHandlerProvider` in `org.apache.shardingsphere.mcp.api.spi`.

## Phase 2: Context Integration

- [x] T014 Update `MCPDatabaseHandlerContext` to extend `MCPHandlerContext`.
- [x] T015 Update `MCPWorkflowHandlerContext` to extend `MCPHandlerContext`.
- [x] T016 Update `MCPRequestScope` to implement `MCPServiceHandlerContext`, `MCPDatabaseHandlerContext`, and `MCPWorkflowHandlerContext`.
- [x] T017 Ensure no handler receives nullable context values.
- [x] T018 Keep request-scope creation behavior unchanged in `MCPToolController` and `MCPResourceController`; do not optimize service-level dispatch in this refactor.

## Phase 3: Core Loader and Registry

- [x] T019 Rename `MCPContributionLoader` to `MCPHandlerLoader`.
- [x] T020 Move loader package from `org.apache.shardingsphere.mcp.contribution` to `org.apache.shardingsphere.mcp.handler`.
- [x] T021 Move built-in provider package from `org.apache.shardingsphere.mcp.contribution.core` to `org.apache.shardingsphere.mcp.handler.core`.
- [x] T022 Update loader methods to `loadToolHandlers()` and `loadResourceHandlers()`.
- [x] T023 Update loader null-collection and null-handler validation messages to use handler terminology.
- [x] T024 Update `ResourceHandlerRegistry` to store `MCPResourceHandler<?>`.
- [x] T025 Update `ResourceHandlerRegistry` to validate `getContextType()` is non-null and exactly supported during registration.
- [x] T026 Update `ResourceHandlerRegistry` to dispatch by resolved `MCPHandlerContext` instead of deleted subinterfaces.
- [x] T027 Keep the generic cast for resource handler dispatch inside a small covered helper method.
- [x] T028 Update `ToolHandlerRegistry` to store `MCPToolHandler<?>`.
- [x] T029 Update `ToolHandlerRegistry` to validate `getContextType()` is non-null and exactly supported during registration.
- [x] T030 Update `ToolHandlerRegistry` to dispatch by resolved `MCPHandlerContext` instead of deleted subinterfaces.
- [x] T031 Keep the generic cast for tool handler dispatch inside a small covered helper method.
- [x] T032 Preserve required tool argument validation behavior.
- [x] T033 Preserve duplicate and overlapping registration validation behavior.
- [x] T033a Use `IllegalArgumentException` for unsupported handler context type validation errors.
- [x] T033b Include handler class and context type in unsupported context type error messages.

## Phase 4: Built-in Core Handlers

- [x] T034 Update `ServerCapabilitiesHandler` to implement `MCPResourceHandler<MCPServiceHandlerContext>`.
- [x] T035 Update `DatabaseCapabilitiesHandler` and `MetadataResourceHandler` to implement `MCPResourceHandler<MCPDatabaseHandlerContext>`.
- [x] T036 Update `ExecuteSQLToolHandler` and `SearchMetadataToolHandler` to implement `MCPToolHandler<MCPDatabaseHandlerContext>`.
- [x] T037 Update `WorkflowValidationToolHandler` and `WorkflowExecutionToolHandler` to implement `MCPToolHandler<MCPWorkflowHandlerContext>`.
- [x] T038 Update core handler factory classes to return handler collections.
- [x] T039 Rename `CoreContributionProvider` to `CoreHandlerProvider`.
- [x] T040 Update core provider and core provider tests to `MCPHandlerProvider`.
- [x] T041 Update core service discovery file to `META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPHandlerProvider`.
- [x] T042 Update core service discovery file content to `org.apache.shardingsphere.mcp.handler.core.CoreHandlerProvider`.

## Phase 5: Feature Modules

- [x] T043 Update encrypt resource handlers to implement `MCPResourceHandler<MCPDatabaseHandlerContext>`.
- [x] T044 Update encrypt workflow tool handler to implement `MCPToolHandler<MCPWorkflowHandlerContext>`.
- [x] T045 Update `EncryptFeatureProvider` and tests to `MCPHandlerProvider`.
- [x] T046 Update encrypt service discovery file to the renamed provider SPI.
- [x] T047 Update mask resource handlers to implement `MCPResourceHandler<MCPDatabaseHandlerContext>`.
- [x] T048 Update mask workflow tool handler to implement `MCPToolHandler<MCPWorkflowHandlerContext>`.
- [x] T049 Update `MaskFeatureProvider` and tests to `MCPHandlerProvider`.
- [x] T050 Update mask service discovery file to the renamed provider SPI.

## Phase 6: E2E Fixtures and Documentation

- [x] T051 Update MCP E2E plugin fixture provider and handlers to the new handler contracts.
- [x] T052 Rename `PluginFixtureContributionProvider` to handler terminology.
- [x] T053 Update packaged distribution fixture support to write the renamed provider service entry.
- [x] T054 Update packaged distribution fixture support class list to use the renamed provider class.
- [x] T055 Update `mcp/README.md` authoring guidance.
- [x] T056 Update `mcp/README_ZH.md` authoring guidance.

## Phase 7: Remove Old Contracts

- [x] T057 Delete `ServerResourceHandler`.
- [x] T058 Delete `DatabaseResourceHandler`.
- [x] T059 Delete `ServerToolHandler`.
- [x] T060 Delete `DatabaseToolHandler`.
- [x] T061 Delete `WorkflowToolHandler`.
- [x] T062 Delete old provider service files after new service files are added.
- [x] T063 Confirm no source package remains under `mcp/core/src/main/java/org/apache/shardingsphere/mcp/contribution`.
- [x] T063a Confirm deleted context-specific handler interfaces were not replaced by empty or deprecated compatibility shells.

## Phase 8: Tests

- [x] T064 Update `MCPHandlerProviderTest`.
- [x] T065 Update `MCPHandlerLoaderTest`.
- [x] T066 Update `ResourceHandlerRegistryTest`.
- [x] T067 Update `ToolHandlerRegistryTest`.
- [x] T068 Update `ResourceHandlerTest`.
- [x] T069 Update core workflow tool handler tests.
- [x] T070 Update encrypt and mask feature provider tests.
- [x] T071 Update MCP E2E fixture tests.
- [x] T072 Convert touched service-loader static mock tests to `AutoMockExtension` and `@StaticMockSettings(ShardingSphereServiceLoader.class)` where compatible.
- [x] T073 If isolated-classloader registry tests cannot use `AutoMockExtension`, document the reason and keep direct `mockStatic` only in those tests with try-with-resources.
  - Note: `mcp/core` does not expose the test infra extension on its test classpath, and registry tests need scoped static mocks so normal registry static initialization can still use real service loading. Direct `mockStatic` remains limited to try-with-resources in loader/registry tests.
- [x] T073a Rename `MCPContributionProviderTest` to `MCPHandlerProviderTest`.
- [x] T073b Rename `MCPContributionLoaderTest` to `MCPHandlerLoaderTest`.
- [x] T073c Rename `CoreContributionProviderTest` to `CoreHandlerProviderTest`.

## Phase 9: Verification

- [x] T074 Run scoped MCP unit tests:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

- [x] T075 Run scoped Checkstyle:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck checkstyle:check
```

- [x] T076 Run scoped Spotless check:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck spotless:check
```

- [x] T077 Run old-name search outside generated paths:

```bash
rg -n "MCPResourceContribution|MCPToolContribution|MCPContributionProvider|ServerResourceHandler|DatabaseResourceHandler|ServerToolHandler|DatabaseToolHandler|WorkflowToolHandler" mcp test/e2e/mcp --glob '!target/**'
```

- [x] T078 Run service entry search outside generated paths:

```bash
rg -n "org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider" mcp test/e2e/mcp --glob '!target/**'
```

## Dependencies

- T005-T012 must complete before most implementation updates.
- T014-T016 must complete before registry dispatch can compile.
- T024-T033 must complete before handler behavior tests can be finalized.
- T041, T046, T050, and T053 must complete before E2E packaging verification.
- T057-T063 should be last among implementation edits to reduce intermediate compile breaks.

## Completion Criteria

- All old contribution and context-specific handler contracts are gone outside migration notes and this spec directory.
- All provider, loader, registry, feature, fixture, and documentation references use handler terminology.
- Unit tests, Checkstyle, Spotless, and search verification pass or have documented blockers.
- No branch switch occurred during the workflow.
