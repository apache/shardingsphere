# Feature Specification: MCP Request-Scoped Metadata Lifecycle

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-18
**Status**: Draft
**Input**: User description: "In `shardingsphere-mcp`, fully remove the legacy global metadata-cache design, decouple capability from metadata, keep external functionality unchanged, keep capability as a process-level cache, and make all other metadata load lazily into a request-scoped metadata context that accumulates during a single MCP request and is released when that request ends."

## Clarifications

### Session 2026-04-18

- This work does not preserve internal compatibility with the old metadata-cache architecture.
- Legacy global metadata-catalog, refresh, and compatibility paths should be removed rather than retained behind adapters.
- Process-level caching for non-capability metadata is out of scope.
- Request-level metadata reuse is required.
- Metadata loaded during one request should accumulate in that request-scoped metadata context and remain reusable until the request ends.
- Metadata loaded for one request must not leak into later requests.
- Capability resolution must be decoupled from metadata lifecycle and held in a process-level capability cache.
- A request means one MCP call, such as one tool call or one resource read, and request-scoped metadata does not span multiple MCP calls.
- Externally visible field ordering must remain unchanged.
- Within one request, metadata already loaded into the request-scoped metadata context is reused, while metadata not yet loaded may be loaded later in that same request based on the database state visible at access time.
- The preferred term for non-capability metadata state is `Request-Scoped Metadata Context`.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Read fresh metadata on every new request (Priority: P1)

As an MCP caller, I want each new metadata request to reflect the current database state so that schema changes made outside the MCP process become visible without restart or manual refresh.

**Why this priority**: Eliminating stale cross-request metadata is the main reason for the refactor; if this does not hold, the redesign misses its core value.

**Independent Test**: Complete one metadata request, change the database schema outside MCP, then issue a second request and verify that the second request sees the new schema state without depending on a process restart or explicit refresh operation.

**Acceptance Scenarios**:

1. **Given** one request has already completed, **When** an external DDL change adds or removes a table before the next request begins, **Then** the next request sees the current table set rather than a stale snapshot from the earlier request.
2. **Given** an external DDL change modifies columns between two requests, **When** the later request reads that table metadata, **Then** it sees the latest column definition without relying on process-wide cached non-capability metadata.

---

### User Story 2 - Reuse metadata within a single request only (Priority: P1)

As an MCP caller, I want metadata fetched earlier in the same request to be reused later in that request so that repeated lookups do not reload the same database objects unnecessarily.

**Why this priority**: The redesign removes long-lived caches, so single-request reuse is the key mechanism that preserves efficiency without reintroducing stale global state.

**Independent Test**: Execute one request flow that reads the same logical database, schema, or table metadata more than once and verify that the request reuses the already loaded metadata while still allowing newly requested objects to be added into the same request context.

**Acceptance Scenarios**:

1. **Given** a request has already loaded metadata for a table, **When** a later step in the same request asks for that same table metadata again, **Then** the request reuses the metadata already held in the request context instead of reloading it.
2. **Given** a request has loaded metadata for one schema, **When** a later step in the same request accesses another schema or table, **Then** the request context expands to include the new metadata while preserving the metadata already loaded earlier in that request.
3. **Given** a request ends successfully or with failure, **When** request handling completes, **Then** the accumulated metadata context is released with the request lifecycle.

---

### User Story 3 - Keep capability available without global metadata state (Priority: P1)

As an MCP caller, I want capability-related behavior to keep working through a process-level capability cache even when no request-scoped metadata has been loaded so that capability checks remain available under the new architecture.

**Why this priority**: Capability is still needed by execution, validation, and resource discovery paths, so the refactor cannot make capability depend on request-scoped metadata warming or startup preloading.

**Independent Test**: Start the runtime without metadata preloading, verify that process-level capability is available before any request-scoped metadata has been loaded, and verify that capability use does not require a preloaded metadata snapshot.

**Acceptance Scenarios**:

1. **Given** a fresh request with an empty request-scoped metadata context, **When** the caller asks for database capability, **Then** the system returns the correct capability from the process-level capability cache without requiring a preloaded metadata snapshot.
2. **Given** a request resolves capability first and metadata later, **When** later steps load metadata for concrete objects, **Then** capability behavior remains consistent and independent from metadata accumulation.

---

### User Story 4 - Preserve external feature behavior through the refactor (Priority: P2)

As an MCP caller or operator, I want the same functional behavior from metadata, capability, planning, validation, and execution paths after the refactor so that the architecture change does not alter how the feature works from the outside.

**Why this priority**: The user explicitly wants a full internal cleanup without changing the effective feature behavior.

**Independent Test**: Run existing capability, metadata, planning, validation, and execution flows against the same database state before and after the refactor and verify that externally visible behavior remains functionally equivalent.

**Acceptance Scenarios**:

1. **Given** the same database state and the same MCP request input, **When** the refactored implementation handles the request, **Then** the externally visible result remains functionally equivalent to the previous implementation.
2. **Given** existing MCP workflows that depend on metadata and capability, **When** they run on the refactored implementation, **Then** they continue to complete without requiring callers to learn a new interaction pattern.

---

### Edge Cases

- What happens when a single request repeatedly asks for the same database object through different internal paths?
- What happens when one request incrementally touches multiple schemas or tables and only some of them were loaded earlier in that request?
- What happens when external DDL changes occur while one request is still in progress?
- What happens when capability is needed before any metadata object has been loaded for the request?
- What happens when request handling fails after partially loading metadata into the request context?
- What happens when a request needs metadata for one object that fails to load while other metadata in the same request is already available?
- What happens when metadata-dependent workflows run back to back and the second request must not observe context from the first request?
- What happens when output payload construction must preserve the exact pre-refactor field order even though metadata is now obtained through a request-scoped metadata context?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST remove the legacy process-wide metadata snapshot behavior for MCP metadata handling.
- **FR-002**: The system MUST NOT rely on startup-time full metadata preloading for normal metadata operations.
- **FR-003**: The system MUST treat one MCP call as one request for request-scoped metadata lifecycle purposes.
- **FR-004**: The system MUST scope non-capability metadata reuse to the lifecycle of a single request.
- **FR-005**: The system MUST begin each request with an empty Request-Scoped Metadata Context.
- **FR-006**: The system MUST load metadata lazily, only when the active request needs that metadata.
- **FR-007**: The system MUST store metadata loaded during a request inside that request's Request-Scoped Metadata Context so later steps in the same request can reuse it.
- **FR-008**: The system MUST allow the Request-Scoped Metadata Context to grow incrementally as the request touches additional metadata objects.
- **FR-009**: The system MUST reuse metadata already loaded in the current Request-Scoped Metadata Context when later steps request the same metadata scope again.
- **FR-010**: The system MUST release the accumulated Request-Scoped Metadata Context when request handling ends, regardless of whether the request succeeds or fails.
- **FR-011**: The system MUST prevent metadata loaded for one request from being visible to any later request.
- **FR-012**: The system MUST decouple capability resolution from metadata lifecycle so capability does not depend on request-scoped metadata loading.
- **FR-013**: The system MUST keep capability in a process-level capability cache rather than in the Request-Scoped Metadata Context.
- **FR-014**: The system MUST make capability available even when the current request has not yet loaded any object metadata.
- **FR-015**: The system MUST preserve the current external functional behavior of MCP metadata, capability, planning, validation, and execution flows.
- **FR-016**: The system MUST preserve existing externally visible field ordering in response payloads.
- **FR-017**: The system MUST remove legacy compatibility layers that exist only to preserve the old global metadata-cache architecture.
- **FR-018**: The system MUST avoid keeping obsolete metadata-refresh flows whose sole purpose was to repair a long-lived global metadata snapshot.
- **FR-019**: The system MUST keep repeated metadata access within one request efficient by reusing request-scoped metadata instead of reloading identical metadata scopes.
- **FR-020**: The system MUST ensure that metadata observed by a new request reflects the current database state visible at the time that request executes, rather than state retained from prior requests.
- **FR-021**: The system MUST keep caller-facing interactions functionally unchanged even though internal metadata state management is rewritten.
- **FR-022**: Within one request, metadata scopes already loaded into the Request-Scoped Metadata Context MUST continue to be reused rather than reloaded after later database changes.
- **FR-023**: Within one request, metadata scopes not yet loaded MAY be loaded later in that same request based on the database state visible at the time those scopes are first accessed.

### Key Entities *(include if feature involves data)*

- **Request-Scoped Metadata Context**: The per-request in-memory scope that starts empty, accumulates non-capability metadata as needed, supports reuse within the same request, and is destroyed when the request ends.
- **Metadata Scope**: A concrete metadata unit requested by MCP during a request, such as database, schema, table, view, column, index, or sequence information.
- **Runtime Database Profile**: The stable database identity information needed to resolve capability independently from request-scoped metadata accumulation.
- **Process-Level Capability Cache**: The long-lived in-process store for capability descriptors that is independent from per-request metadata accumulation.
- **Capability Descriptor**: The database capability result used by MCP execution and discovery paths without depending on request-scoped metadata accumulation.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: After an external schema change between two requests, the later request sees the updated metadata without requiring runtime restart or manual metadata refresh.
- **SC-002**: Within a single request, repeated access to the same metadata scope does not trigger duplicate metadata loading for that scope.
- **SC-003**: Metadata accumulated during one request is not visible to a later request after the earlier request has ended.
- **SC-004**: Capability-related MCP behavior remains available from the process-level capability cache before any request-scoped metadata has been loaded.
- **SC-005**: Existing caller-visible MCP flows that depend on metadata or capability remain functionally equivalent after the refactor.
- **SC-006**: Response payloads that existed before the refactor preserve their externally visible field order after the refactor.

## Assumptions

- A "request" means one MCP operation handled end to end, such as one tool call or one resource read, including all internal steps that share the same request lifecycle.
- The database can change outside the MCP process between requests, and the new architecture must treat that as a normal case rather than an exceptional refresh scenario.
- The refactor is allowed to replace internal architecture completely as long as externally visible feature behavior remains functionally unchanged.

## Out of Scope

- Process-level metadata caching for non-capability metadata.
- Session-level metadata reuse across multiple requests.
- Preserving internal compatibility with the old global metadata-cache architecture.
- Adding a manual metadata refresh workflow solely to compensate for long-lived metadata caching.
- Changing public MCP tool names, resource names, or response semantics as part of this refactor.
