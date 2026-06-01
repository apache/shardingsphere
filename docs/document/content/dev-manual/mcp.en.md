+++
pre = "<b>5.10. </b>"
title = "MCP"
weight = 10
chapter = true
+++

This chapter is for developers who want to extend ShardingSphere-MCP.
For installation and usage, see the [User Manual](../../user-manual/shardingsphere-mcp/). For the protocol surface, see the [Reference](../../reference/mcp/).

## Module structure

The MCP path is organized as `api + support + features + core + bootstrap`:

- `mcp/api`: public tool/resource handler contracts, descriptor types, protocol responses, and MCP protocol exceptions.
- `mcp/support`: database metadata, execution, capability, workflow contexts, models, facades, SPI, and reusable helpers.
- `mcp/features/encrypt`: Encrypt MCP feature.
- `mcp/features/mask`: Mask MCP feature.
- `mcp/core`: handler discovery, registry, request scope, session, SQL execution trace, metadata discovery, and runtime context.
- `mcp/bootstrap`: MCP Java SDK based bootstrap, HTTP/STDIO transport, configuration loading, and lifecycle management.
- `distribution/mcp`: standalone packaging, startup scripts, configuration, and Dockerfile.
- `test/e2e/mcp`: end-to-end contract validation.

`mcp/bootstrap` only publishes the aggregated protocol surface. It should not hard-code concrete feature business logic.

## Add a Feature Plugin

Recommended path for a new feature:

1. Create a module under `mcp/features/<feature>`.
2. Depend on `mcp/api`.
3. Depend on `mcp/support` when database metadata, SQL execution, or workflow support is needed.
4. Depend on `mcp/core` only when service-level handler context is needed.
5. Do not depend on `mcp/bootstrap`.
6. Implement `MCPHandlerProvider`.
7. Return the feature's handlers from `getToolHandlers()` and `getResourceHandlers()`.
8. If the feature owns workflow definitions, implement `MCPWorkflowDefinitionProvider` on the same provider.
9. Register `org.apache.shardingsphere.mcp.api.MCPHandlerProvider` under `src/main/resources/META-INF/services/`.
10. Add descriptors under `META-INF/shardingsphere-mcp/mcp-descriptors`.

If the feature should be shipped as an official default capability:

- Add it to `mcp/features/pom.xml`.
- Add it to `distribution/mcp/pom.xml`.

If the feature is optional, place the built jar under the distribution `plugins/` directory.

## Handler and Descriptor

When adding a public tool:

- Implement `MCPToolHandler<T extends MCPHandlerContext>`.
- Declare the context type.
- Declare the canonical tool name.
- Maintain input schema, output schema, annotations, related resources, follow-up tools, and side-effect notes in the descriptor.

When adding a public resource:

- Implement `MCPResourceHandler<T extends MCPHandlerContext>`.
- Declare the context type.
- Declare the canonical URI template.
- Maintain URI parameter meaning, object scope, MIME type, title, description, annotations, and relationship metadata in the descriptor.

When runtime code needs the descriptor for a handler, resolve it from `MCPDescriptorCatalogIndex` by canonical tool name or resource URI template.
Do not duplicate descriptor fields inside handlers.

## Context selection

- Use `MCPServiceHandlerContext` for service-level handlers.
- Use `MCPDatabaseHandlerContext` for database metadata or execution handlers.
- Use `MCPWorkflowHandlerContext` for workflow handlers.

## Naming and uniqueness

- Tool names and resource URI patterns must be globally unique.
- Duplicate handlers or descriptors are rejected at startup validation.
- Feature URIs should use the `shardingsphere://features/<feature>/...` namespace.
- Do not mix feature URIs with public metadata paths.

## Descriptor maintenance

Descriptors should explain how models should use the protocol surface, not merely repeat tool names or URIs.

Maintain:

- Clear field descriptions.
- JSON schema constraints.
- Output structure and examples.
- Safety annotations.
- Side-effect scope.
- Related resources.
- Next actions.
- Completion targets.
- Workflow recovery paths.

Tool annotations are only client hints. They do not replace runtime validation, SQL safety checks, user approval, or server-side authorization.
