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

- `mcp/api`: public MCP capability contracts, descriptor types, protocol responses, MCP protocol exceptions, and SPI entry points.
- `mcp/support`: database metadata, execution, capability, workflow contexts, models, facades, database/workflow SPIs, and reusable helpers.
- `mcp/features/encrypt`: Encrypt MCP feature.
- `mcp/features/mask`: Mask MCP feature.
- `mcp/core`: handler discovery, registry, request context, session, SQL execution trace, metadata discovery, and runtime context.
- `mcp/bootstrap`: MCP Java SDK based bootstrap, HTTP/STDIO transport, configuration loading, and lifecycle management.
- `distribution/mcp`: standalone packaging, startup scripts, configuration, and Dockerfile.
- `test/e2e/mcp`: end-to-end contract validation.

`mcp/bootstrap` only publishes the aggregated protocol surface. It should not hard-code concrete feature business logic.

Public server capability contracts are organized under `org.apache.shardingsphere.mcp.api.capability.<capability>`, while ServiceLoader entry interfaces live in
`org.apache.shardingsphere.mcp.spi`. Request contexts, sessions, transports, payloads, and exceptions are cross-capability or base-protocol contracts and stay outside capability packages.

## Add a Feature Plugin

Recommended path for a new feature:

1. Create a module under `mcp/features/<feature>`.
2. Depend on `mcp/api`.
3. Depend on `mcp/support` when database metadata, SQL execution, or workflow support is needed.
4. Do not depend on `mcp/core` or `mcp/bootstrap`; runtime implementations are not feature extension contracts.
5. Implement `MCPHandlerProvider`.
6. Return the feature's handlers from `getToolHandlers()` and `getResourceHandlers()`.
7. If the feature owns workflow definitions, implement `MCPWorkflowDefinitionProvider` on the same provider.
8. Register `org.apache.shardingsphere.mcp.spi.MCPHandlerProvider` under `src/main/resources/META-INF/services/`.
9. Add descriptors under `META-INF/shardingsphere-mcp/mcp-descriptors`.

If the feature should be shipped as an official default capability:

- Add it to `mcp/features/pom.xml`.
- Add it to `distribution/mcp/pom.xml`.

If the feature is optional, place the built jar under the distribution `plugins/` directory.

## Feature workflow template

Features that plan, preview, apply, and validate rule changes should use the Data Encryption MCP feature's rule workflow as the template.
The template implementation should satisfy:

- Keep feature business logic under `mcp/features/<feature>`; `mcp/support` and `mcp/core` should only host generic workflow, execution, redaction, descriptor, and runtime contracts.
- Handlers declare canonical tool names, context types, and workflow definitions; descriptors and prompts own the model-facing contract, and handlers should not duplicate descriptive fields.
- Read feature-owned resources, such as algorithms, rules, or existing configuration resources, before planning reviewable DistSQL artifacts.
- Expose only artifacts supported by the current feature in the output schema; do not keep unsupported fields as placeholders or make models depend on real physical table structure.
- Side-effecting execution must run preview first, then execute only user-approved `approved_steps`.
- Sensitive properties must be masked in model-facing plan, preview, execution, validation, recovery, and error outputs; the execution path uses original values from the controlled context.
- Validation should read Proxy-visible rule state or feature state, and should not use physical operations outside the current feature as acceptance conditions.
- Startup descriptor validation should cover tool/resource/prompt name uniqueness, schema fields, side-effect annotations, related resources,
  follow-up tools, completion targets, and workflow recovery paths.

## Handler and Descriptor

When adding a public tool:

- Implement `MCPToolHandler<T extends MCPRequestContext>`.
- Declare the context type.
- Declare the canonical tool name.
- `handle(...)` returns only a successful `MCPSuccessPayload`. For controlled failures such as invalid arguments, missing resources, query failure, timeout, or unsupported operations, throw the corresponding `ShardingSphereMCPException` subclass and let runtime convert it to an MCP tool error result. Unexpected runtime failures are sanitized as JSON-RPC internal errors.
- Maintain input schema, output schema, annotations, related resources, follow-up tools, and side-effect notes in the descriptor.

When adding a public resource:

- Implement `MCPResourceHandler<T extends MCPRequestContext>`.
- Declare the context type.
- Declare the canonical resource URI template. A fixed URI is also a URI template without variables.
- `handle(...)` returns only a successful `MCPSuccessPayload`. Do not build error payloads in handlers; throw the corresponding `ShardingSphereMCPException` subclass and let runtime convert it to an MCP resource read error. Unexpected runtime failures are sanitized as JSON-RPC internal errors.
- Maintain URI parameter meaning, object scope, MIME type, title, description, annotations, and relationship metadata in the descriptor.

When runtime code needs the descriptor for a handler, resolve it from `MCPDescriptorCatalogIndex` by canonical tool name or resource URI template.
Do not duplicate descriptor fields inside handlers.

## Context selection

- Use `MCPRequestContext` when a handler only needs the session identity or active transport. It exposes exactly `getSessionIdentity()` and `getActiveTransport()`.
- Use `MCPFeatureRequestContext` when a handler or completion provider needs database metadata, execution, or workflow capabilities.

`MCPSessionIdentity` contains the opaque session ID together with optional trusted `subject`, `source`, and `attributes`; read the ID through `getSessionIdentity().getSessionId()`. Attribution describes where a session came from and is not an authentication or authorization result.

`MCPFeatureRuntimeRequestContext` is the runtime-owned, per-request implementation. Handlers and completion providers depend only on context interfaces, not on the core implementation class.

Completion requests use a per-session 60-second fixed-window rate limit. The default is 600 requests per minute and can be changed with the
`shardingsphere.mcp.maxCompletionRequestsPerMinute` Java system property.

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
