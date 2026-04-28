# Research: MCP HTTP Transport Validation Alignment

## Decision 1: Use MCP's `ServerTransportSecurityValidator` as the sole header-validation entry point

- **Decision**: Remove the servlet-side manual header-validation invocation and register one non-`NOOP` `ServerTransportSecurityValidator` with the MCP HTTP transport provider.
- **Rationale**:
  - MCP upstream already defines `ServerTransportSecurityValidator` as the transport-level pre-request header-validation hook.
  - Keeping both a manual servlet-side validation path and an MCP-native validation path creates mixed ownership, duplicated branching, and confusing error responsibility.
  - Aligning with the upstream hook is the least surprising architecture and matches the user's stated preference to use MCP's own validation framework.
- **Alternatives considered**:
  - Keep `NOOP` in the builder and continue manual servlet-side validation: rejected because it leaves the architecture split across two validation chains.
  - Let both the servlet and MCP perform validation for defense in depth: rejected because it duplicates behavior and makes error ownership harder to reason about.

## Decision 2: Keep MCP coupling only at the composite adapter boundary

- **Decision**: Only the composite adapter should implement `ServerTransportSecurityValidator`; leaf ShardingSphere constraints should use a local `TransportHeaderConstraint` abstraction.
- **Rationale**:
  - Access token, origin, and protocol-version checks are ShardingSphere-owned constraints, not MCP-owned extension points by themselves.
  - If each leaf constraint implements MCP's interface directly, the code suggests MCP is expected to call each leaf independently, which is not true.
  - A local constraint abstraction keeps leaf constraints reusable and makes the MCP-facing adapter the only place where upstream exception types and interfaces appear.
- **Alternatives considered**:
  - Let every leaf rule implement `ServerTransportSecurityValidator`: rejected because it leaks MCP coupling into components that MCP will never invoke directly.
  - Remove interfaces entirely and hard-code concrete validator classes in the composite: rejected because it weakens structure and makes later extensions less clear.

## Decision 3: Treat protocol-version consistency as a transport-header constraint, but only for existing sessions

- **Decision**: Move `MCP-Protocol-Version` follow-up validation into a local header constraint executed by the MCP validator adapter, but apply it only when the request refers to an existing session.
- **Rationale**:
  - Protocol-version consistency is a header-level precondition for valid follow-up requests, so it belongs closer to transport-header validation than to a separate servlet request validator.
  - The constraint should not steal ownership of missing-session outcomes. If the session is unknown, MCP's own request/session lifecycle should continue to produce the `404`.
  - This keeps the constraint focused on "existing-session protocol agreement" instead of mixing protocol validation with session lifecycle ownership.
- **Alternatives considered**:
  - Keep protocol-version validation in a ShardingSphere-specific request validator: rejected because it preserves a parallel front-running chain.
  - Validate protocol headers even when the session is absent: rejected because it would blur `400` protocol errors with `404` session lifecycle errors and reintroduce ownership overlap.

## Decision 4: Let MCP own request/session lifecycle errors and their rendering

- **Decision**: Remove the custom ShardingSphere request-validation layer and custom JSON error-envelope path for header-validation failures; let MCP own those failure branches after the header-rule adapter returns control.
- **Rationale**:
  - MCP upstream already handles missing session IDs, unknown sessions, malformed request bodies, Accept-header issues, and generic transport processing errors.
  - Preserving a ShardingSphere-specific JSON error body would require a second compatibility layer, which would keep the architecture in mixed mode.
  - The user explicitly accepted MCP-native error rendering instead of preserving the current custom `{"message": ...}` contract.
- **Alternatives considered**:
  - Preserve the old JSON error body by wrapping MCP-native errors in custom servlet code: rejected because it keeps ShardingSphere in the request-error ownership path.
  - Preserve `StreamableHttpMCPRequestValidator` only for a subset of cases: rejected because partial duplication is exactly the structural problem being removed.

## Decision 5: Keep only the servlet responsibilities that are truly transport-adaptation concerns

- **Decision**: Retain only protocol normalization, negotiated protocol-response header decoration, default Accept supplementation, and session-close coordination in `StreamableHttpMCPServlet`.
- **Rationale**:
  - These behaviors are not duplicates of MCP's own validation logic; they are ShardingSphere-specific compatibility and adaptation concerns layered around MCP transport.
  - They do not create a second validation framework and can coexist with MCP-native validator invocation cleanly.
  - Removing them would change successful protocol interoperability rather than just validation ownership.
- **Alternatives considered**:
  - Strip the servlet down to a bare delegate passthrough: rejected because ShardingSphere still needs initialize normalization and response decoration behavior.
  - Keep all current servlet helpers, including request validation: rejected because that preserves the mixed validation architecture.

## Decision 6: Rewrite tests around ownership boundaries instead of the old error body

- **Decision**: Update tests so they assert stable ownership boundaries and status codes, and stop depending on the current custom JSON message envelope for header-validation failures.
- **Rationale**:
  - The pre-existing tests encode the old architecture, especially the expectation that ShardingSphere always wraps failures as `{"message": ...}`.
  - Under the new MCP-native structure, the meaningful stable contract is who owns the error branch and what HTTP status code is returned.
  - Boundary-oriented tests are better documentation for future maintainers than body-shape assertions tied to a removed compatibility layer.
- **Alternatives considered**:
  - Keep the old body assertions and add compatibility shims: rejected because it would preserve a non-native error contract the user already agreed to drop.
  - Assert only successful flows and ignore failure branches: rejected because failure ownership is the main architectural reason for this refactor.

## Decision 7: Prefer complete responsibility transfer over partial migration

- **Decision**: Treat this change as a full validation-boundary transfer rather than a small transport tweak.
- **Rationale**:
  - The current risks came from partial overlap: some validation lived in ShardingSphere, some in MCP, and error rendering differed by path.
  - Moving only the token/origin checks while keeping protocol-version and custom error behavior in the servlet would recreate the same split under new names.
  - The user explicitly asked not to leave the design half-migrated.
- **Alternatives considered**:
  - Move only access token and origin into MCP while leaving protocol-version and JSON error handling in ShardingSphere: rejected because it is still mixed-mode validation.
  - Move only the builder wiring and keep all old supporting code temporarily: rejected because it maximizes confusion during transition and obscures final ownership.
