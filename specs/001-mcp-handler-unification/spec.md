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

# Feature Specification: Unify MCP Handler Contracts

**Feature Branch**: `001-shardingsphere-mcp`  
**Created**: 2026-05-02  
**Status**: Draft  
**Input**: User request to normalize MCP resource and tool extension contracts without switching branches.

## Process Constraints

- The current branch must remain unchanged. Do not run `git switch`, `git checkout`, or any Spec Kit command that creates or changes branches.
- This specification follows the Spec Kit requirements format manually because the repository does not currently contain `.specify` or `specs` scaffolding.
- The scope is requirement clarification only. Implementation, deletion, renaming, and test updates are separate follow-up work.

## Confirmed Decisions

- Rename `MCPContributionProvider` to `MCPHandlerProvider` together with the resource and tool handler contract renames.
- Use explicit context type declaration on each handler via `Class<T> getContextType()`.
- Let `MCPRequestScope` implement `MCPServiceHandlerContext`, `MCPDatabaseContext`, and `MCPWorkflowContext`, so dispatch can pass the same request-scope instance through the required context interface.
- Delete the old context-specific handler subinterfaces directly. Do not keep deprecated compatibility aliases.
- Place `MCPHandlerContext` and `MCPServiceHandlerContext` in `org.apache.shardingsphere.mcp.api.handler`.
- Keep `MCPResourceHandler` in `org.apache.shardingsphere.mcp.api.resource` and `MCPToolHandler` in `org.apache.shardingsphere.mcp.api.tool`.
- Keep the renamed provider SPI in `org.apache.shardingsphere.mcp.api.spi` as `MCPHandlerProvider`.
- Use `IllegalArgumentException` for unsupported handler context type registration errors.
- Physically delete the old context-specific handler interface files rather than keeping empty or deprecated compatibility shells.
- Place `MCPHandlerLoader` in `org.apache.shardingsphere.mcp.handler`.
- Place built-in handler provider/factory classes in `org.apache.shardingsphere.mcp.handler.core`.
- Rename affected test classes to handler terminology, including `MCPHandlerProviderTest`, `MCPHandlerLoaderTest`, and `CoreHandlerProviderTest`.

## Summary

The MCP extension API currently splits the concept of an extension-provided operation across `MCPResourceContribution`, `MCPToolContribution`, and multiple context-specific handler subinterfaces. The target design is to make handler the primary concept, add a generic handler context marker, and remove context-specific handler subinterfaces.

## Problem Statement

The current API has naming and abstraction friction:

- `MCPResourceContribution` and `MCPToolContribution` describe extension entries but implementations behave as runtime handlers.
- `ServerResourceHandler`, `DatabaseResourceHandler`, `ServerToolHandler`, `DatabaseToolHandler`, and `WorkflowToolHandler` exist mainly to vary the context parameter.
- `ResourceHandlerRegistry` and `ToolHandlerRegistry` must use `instanceof` checks to dispatch by handler subtype.
- Provider and loader names use contribution terminology while README guidance asks feature authors to implement handler contracts.

This makes extension authoring noisier than needed and spreads handler context selection across interface hierarchy and registry dispatch code.

## Goals

- Replace contribution terminology with handler terminology for resource and tool extension contracts.
- Use one generic resource handler interface and one generic tool handler interface.
- Represent service, database, and workflow execution capabilities through a shared `MCPHandlerContext` marker.
- Remove runtime `instanceof` dispatch against context-specific handler subinterfaces.
- Resolve handler context selection through explicit `getContextType()` metadata.
- Keep URI pattern validation, tool descriptor validation, duplicate detection, and required argument validation behavior unchanged.

## Non-Goals

- Do not change MCP protocol request or response payloads.
- Do not change resource URI patterns, tool names, descriptors, or feature capabilities.
- Do not change database metadata, workflow planning, SQL execution, or transport behavior.
- Do not introduce nullable handler context parameters.
- Do not keep backward-compatible alias interfaces.

## User Scenarios and Tests

### Scenario 1: Feature author implements a service-level resource

Given a feature needs to expose a server-level resource that does not require database or workflow capabilities, when the author creates the handler, then they implement `MCPResourceHandler<MCPServiceHandlerContext>` with one `handle` method.

Acceptance checks:

- The handler exposes the same URI pattern as before.
- The handler receives a non-null `MCPServiceHandlerContext`.
- No `ServerResourceHandler` type is required.

### Scenario 2: Feature author implements a database-aware resource

Given a feature needs metadata or database capability access, when the author creates the resource handler, then they implement `MCPResourceHandler<MCPDatabaseContext>`.

Acceptance checks:

- The handler receives a non-null `MCPDatabaseContext`.
- Existing database-aware resource behavior remains unchanged.
- No `DatabaseResourceHandler` type is required.

### Scenario 3: Feature author implements a workflow-aware tool

Given a feature needs workflow session access, when the author creates the tool handler, then they implement `MCPToolHandler<MCPWorkflowContext>`.

Acceptance checks:

- The handler receives a non-null `MCPWorkflowContext`.
- Existing workflow validation and execution tools continue to use workflow context capabilities.
- No `WorkflowToolHandler` type is required.

### Scenario 4: Runtime dispatches handlers uniformly

Given a registered resource URI or tool name matches a handler, when registry dispatch executes, then it selects the handler, resolves the configured context, and calls the generic `handle` method.

Acceptance checks:

- Registry code does not dispatch through `instanceof Server*Handler`, `Database*Handler`, or `Workflow*Handler`.
- Registry dispatch resolves the handler context from `handler.getContextType()`.
- Unsupported context types fail with a clear exception during registration or dispatch.
- Existing duplicate URI pattern and duplicate tool name errors stay equivalent.

## Functional Requirements

- **FR-001**: Introduce `MCPHandlerContext` as an empty marker interface in the MCP API layer.
- **FR-001a**: `MCPHandlerContext` must be located in `org.apache.shardingsphere.mcp.api.handler`.
- **FR-002**: Update `MCPDatabaseContext` to extend `MCPHandlerContext`.
- **FR-003**: Update `MCPWorkflowContext` to extend `MCPHandlerContext`.
- **FR-004**: Introduce `MCPServiceHandlerContext` as the service-level context for handlers that do not need database or workflow capabilities.
- **FR-004a**: `MCPServiceHandlerContext` must be located in `org.apache.shardingsphere.mcp.api.handler`.
- **FR-005**: Update `MCPRequestScope` to implement `MCPServiceHandlerContext`, `MCPDatabaseContext`, and `MCPWorkflowContext`.
- **FR-006**: Rename `MCPResourceContribution` to `MCPResourceHandler<T extends MCPHandlerContext>`.
- **FR-006a**: `MCPResourceHandler` must remain in `org.apache.shardingsphere.mcp.api.resource`.
- **FR-007**: `MCPResourceHandler` must expose `Class<T> getContextType()`.
- **FR-008**: `MCPResourceHandler` must keep `String getUriPattern()`.
- **FR-009**: `MCPResourceHandler` must expose `MCPResponse handle(T handlerContext, MCPUriVariables uriVariables)`.
- **FR-010**: Rename `MCPToolContribution` to `MCPToolHandler<T extends MCPHandlerContext>`.
- **FR-010a**: `MCPToolHandler` must remain in `org.apache.shardingsphere.mcp.api.tool`.
- **FR-011**: `MCPToolHandler` must expose `Class<T> getContextType()`.
- **FR-012**: `MCPToolHandler` must keep `MCPToolDescriptor getToolDescriptor()`.
- **FR-013**: `MCPToolHandler` must expose `MCPResponse handle(T handlerContext, MCPToolCall toolCall)`.
- **FR-014**: Delete the context-specific handler subinterfaces: `ServerResourceHandler`, `DatabaseResourceHandler`, `ServerToolHandler`, `DatabaseToolHandler`, and `WorkflowToolHandler`.
- **FR-014a**: Delete the old context-specific handler interface files physically; do not keep deprecated shells.
- **FR-015**: Rename `MCPContributionProvider` to `MCPHandlerProvider`.
- **FR-015a**: `MCPHandlerProvider` must remain in `org.apache.shardingsphere.mcp.api.spi`.
- **FR-016**: Rename provider methods from contribution terminology to handler terminology, for example `getResourceHandlers()` and `getToolHandlers()`.
- **FR-017**: Rename loader methods and internal registry fields from contribution terminology to handler terminology.
- **FR-017a**: `MCPHandlerLoader` must be located in `org.apache.shardingsphere.mcp.handler`.
- **FR-017b**: Built-in handler provider/factory classes must be located in `org.apache.shardingsphere.mcp.handler.core`.
- **FR-018**: Preserve existing service loader discovery behavior through the renamed provider SPI.
- **FR-019**: Update all core, feature, database, workflow, E2E fixture, and unit test implementations to use the new handler interfaces.
- **FR-019a**: Rename affected test classes to handler terminology rather than keeping contribution terminology in test class names.
- **FR-020**: Update README guidance so feature authors are told to return handlers rather than contributions.
- **FR-021**: Registration validation must continue to reject missing or blank resource URI patterns.
- **FR-022**: Registration validation must continue to reject missing or blank tool names and missing tool descriptors.
- **FR-023**: Registration validation must continue to reject duplicate or overlapping resource URI patterns and duplicate tool names.
- **FR-024**: Handler dispatch must pass non-null handler context values and must not depend on nullable database context parameters.
- **FR-025**: Handler registration or dispatch must reject unsupported `getContextType()` values with `IllegalArgumentException` and a clear message that includes the handler class and context type.

## Key Entities

- **MCPHandlerContext**: Marker interface for all MCP runtime contexts used by handlers.
- **MCPServiceHandlerContext**: Context for handlers that only need service-level MCP request information.
- **MCPDatabaseContext**: Database-aware handler context with metadata, execution, query, and capability facades.
- **MCPWorkflowContext**: Workflow-aware handler context with workflow session access and database context access.
- **MCPResourceHandler**: Generic runtime handler for one resource URI pattern.
- **MCPToolHandler**: Generic runtime handler for one tool descriptor.
- **MCPHandlerProvider**: SPI provider that exposes handler collections for discovery.

## Compatibility and Migration Requirements

- This is an API-breaking refactor inside the new MCP module family and its test fixtures.
- All references to the deleted handler subinterfaces must be removed in the same change set.
- Deleted handler subinterface files must not be replaced by empty or deprecated aliases.
- Service loader metadata must be updated from the old provider SPI name to the renamed provider SPI name.
- All imports and package paths must avoid inline fully qualified class names.
- Generated paths such as `target/` must not be edited.
- Public API Javadocs must describe the generic context contract clearly.

## Verification Requirements

- Run targeted unit tests for MCP API, core, database, workflow, encrypt feature, mask feature, and MCP E2E support where implementations are touched.
- Run module-scoped Checkstyle and Spotless checks for touched MCP modules.
- Search verification must confirm no remaining references to deleted interfaces or contribution type names, except in migration notes or release notes if intentionally kept.
- If coverage is affected by registry or loader changes, run scoped Jacoco report or document why branch coverage is unchanged.

## Remaining Question

- Decide whether `MCPServiceHandlerContext` should expose request metadata later. The first implementation should keep it empty and rely on `MCPRequestScope` as the concrete request-scope context object.

## Success Criteria

- A feature author can implement any resource by implementing only `MCPResourceHandler<T>`.
- A feature author can implement any tool by implementing only `MCPToolHandler<T>`.
- Context choice is expressed through generic context type rather than handler subtype naming.
- Registry dispatch no longer contains subtype branches for server, database, or workflow handler interfaces.
- Existing MCP behavior remains unchanged from the protocol consumer perspective.
