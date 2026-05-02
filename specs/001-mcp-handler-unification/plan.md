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

# Implementation Plan: Unify MCP Handler Contracts

**Branch Constraint**: Stay on `001-shardingsphere-mcp`; do not switch or create branches.  
**Spec**: `specs/001-mcp-handler-unification/spec.md`  
**Scope**: MCP API contract refactor across API, support, core, features, tests, documentation, and service discovery metadata.

## Technical Context

- **Language/runtime**: Java, MCP subchain integrated with JDK 21.
- **Primary modules**: `mcp/api`, `mcp/support`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, `test/e2e/mcp`.
- **Current extension path**: `MCPContributionProvider` is discovered by `ShardingSphereServiceLoader`; it returns resource and tool contributions; registries validate and dispatch them.
- **Target extension path**: `MCPHandlerProvider` is discovered by `ShardingSphereServiceLoader`; it returns resource and tool handlers; registries use `getContextType()` to select the request-scope context view and call a single generic `handle` method.
- **Generated paths**: Ignore everything under `target/`.

## Reanalysis Findings

### Loader and Package Naming

The current loader and built-in provider live under `org.apache.shardingsphere.mcp.contribution`. Keeping this package after renaming the public API to handlers would leave the same conceptual mismatch in the core implementation.

Implementation decision:

- Rename `MCPContributionLoader` to `MCPHandlerLoader`.
- Move the loader package from `org.apache.shardingsphere.mcp.contribution` to `org.apache.shardingsphere.mcp.handler`.
- Rename `CoreContributionProvider` to `CoreHandlerProvider`.
- Move `CoreContributionProvider`, `CoreResourceHandlers`, and `CoreToolHandlers` from `org.apache.shardingsphere.mcp.contribution.core` to `org.apache.shardingsphere.mcp.handler.core`.
- Remove source references to `org.apache.shardingsphere.mcp.contribution` outside migration/spec documents.
- Rename touched contribution-named tests to handler terminology, including `MCPHandlerProviderTest`, `MCPHandlerLoaderTest`, and `CoreHandlerProviderTest`.

### Service Context Weight

`MCPToolController` and `MCPResourceController` already create `MCPRequestScope` before registry dispatch. Making `MCPRequestScope` implement `MCPServiceHandlerContext` does not add a new request-scope creation path in the first implementation.

Implementation decision:

- Keep `MCPRequestScope` as the concrete object for service, database, and workflow handler contexts.
- Do not optimize service-level handler dispatch to avoid request-scope creation in this refactor.
- Record request-scope lightweight optimization as out of scope unless a later performance requirement targets it.

### Context Type Validation

`getContextType()` is explicit metadata and should fail early if it is missing or unsupported.

Implementation decision:

- Validate `getContextType()` during handler registration.
- Reject `null` context type.
- Accept exact context types only: `MCPServiceHandlerContext.class`, `MCPDatabaseHandlerContext.class`, and `MCPWorkflowHandlerContext.class`.
- Resolve context through a small helper and keep any unchecked generic cast local to that helper.
- Throw `IllegalArgumentException` for unsupported context type registration errors.
- Include handler class and context type in unsupported-context error messages, for example `Unsupported handler context type `%s` for `%s`.`.

### API Package Placement

The API should keep resource and tool concepts in their existing semantic packages while putting shared handler context types in a small shared package.

Implementation decision:

- Place `MCPHandlerContext` in `org.apache.shardingsphere.mcp.api.handler`.
- Place `MCPServiceHandlerContext` in `org.apache.shardingsphere.mcp.context`.
- Keep `MCPResourceHandler` in `org.apache.shardingsphere.mcp.api.resource`.
- Keep `MCPToolHandler` in `org.apache.shardingsphere.mcp.api.tool`.
- Keep `MCPHandlerProvider` in `org.apache.shardingsphere.mcp.api.spi`.

### SPI and Fixture Synchronization

The source service discovery files that must be renamed are:

- `mcp/core/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider`
- `mcp/features/encrypt/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider`
- `mcp/features/mask/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider`

The E2E packaged plugin fixture also writes the provider service entry dynamically in `PackagedDistributionPluginFixtureSupport`.

Implementation decision:

- Rename all source service files to `META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPHandlerProvider`.
- Update service file contents to provider implementation classes with handler naming, for example `CoreHandlerProvider`.
- Update E2E fixture service entry constants and provider class names.
- Ignore stale service files under `target/`; they must be regenerated by Maven, not edited.

### Static Mock Scope

The MCP loader and registry tests currently use direct `mockStatic` for `ShardingSphereServiceLoader`. Project rules prefer `AutoMockExtension` with `@StaticMockSettings` for touched static mocks.

Implementation decision:

- Convert touched loader and registry tests to `@ExtendWith(AutoMockExtension.class)` and `@StaticMockSettings(ShardingSphereServiceLoader.class)` where they still need service loader mocking.
- Stub static calls directly with `when(ShardingSphereServiceLoader.getServiceInstances(...)).thenReturn(...)`.
- Do not broaden this refactor to unrelated bootstrap/controller tests that also use static or construction mocking.
- If isolated-classloader registry tests cannot work with `AutoMockExtension`, document the reason in the implementation notes and keep `mockStatic` wrapped in try-with-resources for those tests only.

## Compliance Checklist

- Re-read `AGENTS.md` and `CODE_OF_CONDUCT.md` before implementation.
- Use `rg`, `./mvnw`, and `apply_patch` for inspection, verification, and manual edits.
- Do not edit generated `target/` files.
- Do not run `git switch`, `git checkout`, `git reset --hard`, or any branch-changing Spec Kit command.
- Do not delete files or perform bulk edits without explicit confirmation because the implementation requires deleting old interfaces and wide renames.
- Keep public API Javadocs clear and self-explanatory.
- Avoid inline fully qualified class names.
- Keep method parameters and return values non-null.
- Use mocks by default in tests and preserve existing test naming rules.

## Design Overview

### Handler Context

Add a new API marker:

```java
package org.apache.shardingsphere.mcp.api.handler;

public interface MCPHandlerContext {
}
```

Define service-level context:

```java
package org.apache.shardingsphere.mcp.api.handler;

public interface MCPServiceHandlerContext extends MCPHandlerContext {
}
```

Update existing contexts:

```java
public interface MCPDatabaseHandlerContext extends MCPHandlerContext {
    ...
}

public interface MCPWorkflowHandlerContext extends MCPHandlerContext {
    ...
}
```

Update `MCPRequestScope` to implement all three concrete context interfaces. The first implementation keeps `MCPServiceHandlerContext` empty.

### Resource Handler

Replace:

```java
MCPResourceContribution
ServerResourceHandler
DatabaseResourceHandler
```

with:

```java
package org.apache.shardingsphere.mcp.api.resource;

public interface MCPResourceHandler<T extends MCPHandlerContext> {
    
    Class<T> getContextType();
    
    String getUriPattern();
    
    MCPResponse handle(T handlerContext, MCPUriVariables uriVariables);
}
```

### Tool Handler

Replace:

```java
MCPToolContribution
ServerToolHandler
DatabaseToolHandler
WorkflowToolHandler
```

with:

```java
package org.apache.shardingsphere.mcp.api.tool;

public interface MCPToolHandler<T extends MCPHandlerContext> {
    
    Class<T> getContextType();
    
    MCPToolDescriptor getToolDescriptor();
    
    MCPResponse handle(T handlerContext, MCPToolCall toolCall);
}
```

### Provider and Loader

Rename:

- `MCPContributionProvider` -> `MCPHandlerProvider`
- `getResourceContributions()` -> `getResourceHandlers()`
- `getToolContributions()` -> `getToolHandlers()`
- `MCPContributionLoader` -> `MCPHandlerLoader`
- `org.apache.shardingsphere.mcp.contribution` -> `org.apache.shardingsphere.mcp.handler`
- `org.apache.shardingsphere.mcp.contribution.core` -> `org.apache.shardingsphere.mcp.handler.core`
- `MCPContributionProviderTest` -> `MCPHandlerProviderTest`
- `MCPContributionLoaderTest` -> `MCPHandlerLoaderTest`
- `CoreContributionProviderTest` -> `CoreHandlerProviderTest`

Service discovery files must move from:

```text
META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider
```

to:

```text
META-INF/services/org.apache.shardingsphere.mcp.api.spi.MCPHandlerProvider
```

### Registry Dispatch

Resource and tool registries should:

1. Validate required URI patterns or tool descriptors.
2. Validate duplicate and overlapping registrations as they do today.
3. Validate `getContextType()` is one of the supported context contracts.
4. Resolve context from `MCPRequestScope` by context type.
5. Invoke `handler.handle(context, request)`.

No registry dispatch branch should use the deleted context-specific handler interfaces.

Unsupported context type validation should use `IllegalArgumentException`, matching existing registry validation style.

## Implementation Phases

### Phase 1: API and Context Contracts

- Add `MCPHandlerContext`.
- Add `MCPServiceHandlerContext`.
- Rename resource and tool contribution interfaces to generic handler interfaces.
- Rename provider SPI and method names.
- Update `MCPDatabaseHandlerContext`, `MCPWorkflowHandlerContext`, and `MCPRequestScope`.
- Keep API class package placement aligned with the confirmed package decisions.

### Phase 2: Core Discovery and Dispatch

- Rename loader and loader tests.
- Update `ResourceHandlerRegistry` to store `MCPResourceHandler<?>`.
- Update `ToolHandlerRegistry` to store `MCPToolHandler<?>`.
- Add a small context resolution method in each registry or a shared helper if duplication becomes meaningful.
- Preserve existing validation messages where practical.

### Phase 3: Built-in Handlers and Feature Modules

- Update core resource handlers and tool handlers.
- Update encrypt and mask feature providers and handlers.
- Update service discovery metadata in core, encrypt, and mask modules.
- Update README and README_ZH authoring guidance.

### Phase 4: E2E Fixtures and Tests

- Update E2E plugin fixture provider and handlers.
- Update API, core, feature, and resource/tool registry tests.
- Search for old type names and old service discovery entries outside `target/`.
- Confirm old context-specific handler interface files were physically removed and not replaced by deprecated aliases.

### Phase 5: Verification

- Run narrow targeted test commands first.
- Run module-scoped Checkstyle and Spotless checks for touched MCP modules.
- Run search verification for deleted names and old contribution terminology.
- Escalate to broader MCP subchain build only if targeted runs pass and time allows.

## Context Resolution Contract

Supported mappings:

- `MCPServiceHandlerContext.class` -> request scope as `MCPServiceHandlerContext`
- `MCPDatabaseHandlerContext.class` -> request scope as `MCPDatabaseHandlerContext`
- `MCPWorkflowHandlerContext.class` -> request scope as `MCPWorkflowHandlerContext`

Supported mappings use exact class equality. Unsupported or null mappings must fail with a clear exception that includes the handler class and context type.

## Test Strategy

- API provider test: provider returns resource and tool handlers with context types.
- Loader tests: null handler collections, null handlers, unmodifiable output.
- Resource registry tests: missing URI pattern, duplicate URI pattern, overlapping URI pattern, unsupported context type, service context dispatch, database context dispatch.
- Tool registry tests: missing descriptor, missing tool name, duplicate tool name, required argument validation, unsupported context type, service/database/workflow context dispatch.
- Feature provider tests: encrypt/mask return expected resource URI patterns and tool names.
- Handler behavior tests: preserve existing assertions for metadata, capability, workflow validation, workflow execution, encrypt, and mask handlers.
- E2E fixture packaging test: generated plugin fixture service file uses `MCPHandlerProvider`.
- Static service loader tests touched by this refactor should prefer `AutoMockExtension` and `@StaticMockSettings`.

## Verification Commands

Prefer scoped commands. Candidate commands after implementation:

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -DskipITs -Dspotless.skip=true -Dsurefire.failIfNoSpecifiedTests=false test
```

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck checkstyle:check
```

```bash
./mvnw -pl mcp/api,mcp/support,mcp/core,mcp/features/encrypt,mcp/features/mask,test/e2e/mcp -Pcheck spotless:check
```

Search verification:

```bash
rg -n "MCPResourceContribution|MCPToolContribution|MCPContributionProvider|ServerResourceHandler|DatabaseResourceHandler|ServerToolHandler|DatabaseToolHandler|WorkflowToolHandler" mcp test/e2e/mcp --glob '!target/**'
```

## Risks and Mitigations

- **SPI rename can break service discovery**: update all `META-INF/services` entries and tests that generate plugin fixtures.
- **Generic casting can hide type errors**: validate `getContextType()` before dispatch and keep context resolution in a small, covered method.
- **Wide rename can miss docs or fixtures**: use `rg` search verification outside `target/`.
- **Checkstyle can flag generic helper methods**: keep helper methods small, avoid unnecessary locals, and keep declarations close to first use.
- **Behavioral regression in protocol exposure**: keep tool descriptors, resource URI patterns, and bootstrap specification factory behavior unchanged.
- **Request-scope cost remains for service handlers**: this matches current controller behavior and is intentionally left outside the first refactor.

## Rollback Notes

- Revert the handler API rename, provider SPI rename, registry dispatch changes, and service discovery metadata together.
- Protocol behavior should return to the previous contribution/subinterface dispatch model without config changes.
- Confirm rollback by running the same targeted MCP tests and old-name search in reverse.
