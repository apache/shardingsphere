# Research: MCP Request-Scoped Metadata Lifecycle

## Decision 1: Request boundary

- **Decision**: Treat one MCP call as one request for metadata lifecycle purposes.
- **Rationale**: This gives the Request-Scoped Metadata Context a clear, short-lived boundary that naturally matches tool calls and resource reads.
- **Alternatives considered**:
  - Session-scoped metadata reuse: rejected because it reintroduces stale cross-request state.
  - Process-scoped metadata reuse: rejected because it recreates the current stale-metadata problem.

## Decision 2: Capability lifecycle

- **Decision**: Keep capability in a process-level capability cache.
- **Rationale**: Capability is comparatively stable and should remain available before any request-scoped metadata has been loaded.
- **Alternatives considered**:
  - Request-scoped capability only: rejected because repeated resolution would add cost without solving a real freshness problem.
  - Capability derived from request-scoped metadata: rejected because it would keep capability coupled to metadata loading order.

## Decision 3: Metadata terminology

- **Decision**: Use `Request-Scoped Metadata Context` as the canonical term for non-capability metadata held during one request.
- **Rationale**: The object represents the working set for a request, not a reusable cross-request cache.
- **Alternatives considered**:
  - Request cache: rejected because it overemphasizes optimization instead of lifecycle and ownership.
  - Request-scoped loader: rejected because it describes loading behavior but not the accumulated in-memory state.

## Decision 4: In-request consistency semantics

- **Decision**: Reuse metadata scopes already loaded in the current Request-Scoped Metadata Context, and load previously unseen scopes on demand using the database state visible when they are first accessed.
- **Rationale**: This preserves request-local efficiency without forcing a costly whole-request metadata snapshot.
- **Alternatives considered**:
  - Full-request immutable metadata snapshot: rejected because it would require broader upfront loading and higher request cost.
  - Always reload every scope within a request: rejected because it defeats the point of request-local reuse.

## Decision 5: External behavior stability

- **Decision**: Preserve tool/resource behavior and response field order while allowing full internal architectural replacement.
- **Rationale**: The goal is internal cleanup without caller-visible semantic drift.
- **Alternatives considered**:
  - Allow caller-visible payload reshaping: rejected because it changes observable behavior.
  - Keep compatibility adapters around the old architecture: rejected because the user explicitly wants the old design removed cleanly.
