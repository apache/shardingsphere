# Implementation Plan: MCP Request-Scoped Metadata Lifecycle

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-18 | **Spec**: `/.specify/specs/002-mcp-request-scoped-metadata/spec.md`
**Input**: Feature specification from `/.specify/specs/002-mcp-request-scoped-metadata/spec.md`

## Summary

This refactor replaces the current long-lived MCP metadata architecture with a split design:

- capability is resolved through a process-level capability cache;
- all other database metadata is held only in a `Request-Scoped Metadata Context`;
- one MCP call defines one request lifecycle;
- metadata is loaded lazily, accumulated inside the current request context, reused within that request, and released when the request ends.

The implementation must remove the legacy global metadata snapshot, its refresh-oriented repair flows, and compatibility-only wrappers while keeping externally visible behavior functionally unchanged, including response field ordering.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**:
- `mcp/core` MCP tool, resource, metadata, capability, and execution infrastructure
- `mcp/bootstrap` runtime wiring
- JDBC metadata access used by current MCP metadata loaders
- Existing ShardingSphere SPI capability option infrastructure
**Storage**:
- Process-level in-memory capability cache
- Request-scoped in-memory metadata context
- No process-level non-capability metadata storage
**Testing**:
- JUnit 5
- Mockito
- `./mvnw -pl mcp/core -am test`
- `./mvnw -pl mcp/bootstrap -am test`
- Scoped checkstyle and formatting verification for touched modules
**Target Platform**:
- ShardingSphere MCP runtime in STDIO and Streamable HTTP modes
**Project Type**: Java backend runtime refactor inside an existing repository
**Performance Goals**:
- Avoid stale cross-request metadata
- Preserve efficient repeated metadata access within one request
- Avoid forcing startup-time full metadata loading
**Constraints**:
- One MCP call equals one request metadata lifecycle
- Capability uses a process-level cache
- Non-capability metadata must not outlive the current request
- External behavior and response field order must remain stable
- Legacy global metadata snapshot and compatibility paths should be removed cleanly
**Scale/Scope**:
- Covers MCP metadata, capability, resource, workflow, and execution paths that currently depend on global metadata state
- Limited to internal architecture changes; no public feature expansion

## Constitution Check

*GATE: Passes for this planning stage; no blocking constitution conflict identified.*

- **Repository governance**
  - Work remains within existing ShardingSphere MCP architecture and repository rules from `AGENTS.md` and `CODE_OF_CONDUCT.md`.
- **Traceability and minimal change**
  - Caller-visible functionality stays stable while internal state management is simplified.
- **Safety**
  - Removing stale global metadata state reduces correctness risk for metadata discovery flows.

## Project Structure

### Documentation (this feature)

```text
specs/002-mcp-request-scoped-metadata/
|-- spec.md
|-- research.md
|-- plan.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

```text
mcp/
|-- bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/
|-- core/src/main/java/org/apache/shardingsphere/mcp/context/
|-- core/src/main/java/org/apache/shardingsphere/mcp/metadata/
|-- core/src/main/java/org/apache/shardingsphere/mcp/capability/database/
|-- core/src/main/java/org/apache/shardingsphere/mcp/resource/
|-- core/src/main/java/org/apache/shardingsphere/mcp/tool/
`-- core/src/test/java/org/apache/shardingsphere/mcp/
```

**Structure Decision**:
The refactor should stay centered in `mcp/core`, with `mcp/bootstrap` only updated where runtime wiring currently assumes startup-time metadata loading. Capability and request-scoped metadata lifecycle should be expressed through dedicated context and provider objects rather than through a shared global metadata catalog.

## Design Focus

### Architectural direction

- Introduce a dedicated process-level capability cache that is independent from request-scoped metadata state.
- Introduce a dedicated `Request-Scoped Metadata Context` object that starts empty per MCP call and accumulates metadata scopes on demand.
- Replace direct dependencies on long-lived metadata snapshots with request-bound metadata access.
- Remove refresh flows that exist only to keep a long-lived metadata snapshot synchronized.

### Behavior preservation

- Keep existing MCP tools and resources functionally equivalent.
- Preserve response payload field order.
- Keep capability semantics stable while changing only internal sourcing and lifecycle.

### Cleanup expectation

- Remove obsolete global metadata catalog usage rather than wrapping it.
- Remove metadata-refresh coordination that no longer has a purpose after the global snapshot is gone.
- Keep only the minimum stable profile information needed for capability resolution.

## Implementation Slices

### Slice 1 - Capability and metadata state split

- Define the boundary between process-level capability cache and request-scoped metadata context.
- Remove capability dependence on global metadata snapshot objects.

### Slice 2 - Request-scoped metadata loading

- Build the request metadata context lifecycle around one MCP call.
- Ensure metadata scopes are loaded lazily and reused only within the active request.

### Slice 3 - Call-path integration

- Update resource, metadata query, workflow, and execution paths to read metadata through the request-scoped metadata context.
- Preserve externally visible payload structure and field order.

### Slice 4 - Legacy cleanup and regression coverage

- Remove obsolete startup preload and refresh-only flows.
- Expand tests for request isolation, repeated in-request reuse, capability availability, and payload stability.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
