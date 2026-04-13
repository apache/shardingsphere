# Research: ShardingSphere MCP V1

## Decision 1: Keep MCP in the main repository and the default module graph

- **Decision**: Introduce `mcp/`, `distribution/mcp`, and `test/e2e/mcp` in the main repository as regular default reactor modules.
- **Rationale**: This keeps MCP aligned with JDBC, Proxy, and agent while still localizing Java 17 compilation and runtime constraints inside the MCP subchain.
- **Alternatives considered**:
  - Keep a dedicated `mcp` profile: rejected because it special-cases MCP module management relative to the rest of the repository.
  - Build MCP in a separate repository: rejected because version alignment, release flow, and internal integration would become harder to maintain.

## Decision 2: Use a three-chain layout for runtime, distribution, and E2E

- **Decision**: Split implementation into `mcp/core`, `mcp/bootstrap`, `distribution/mcp`, and `test/e2e/mcp`.
- **Rationale**: The design documents already separate protocol/runtime code,
  release packaging, and end-to-end verification; keeping these as distinct
  chains minimizes coupling and supports independent verification.
- **Alternatives considered**:
  - Put distribution logic inside `mcp/bootstrap`: rejected because packaging concerns should align with existing `distribution/*` conventions.
  - Reuse a generic E2E module: rejected because MCP is a new access surface, not just another operation test slice.

## Decision 3: Isolate the MCP chain on Java 17 with Maven Toolchains

- **Decision**: Build the MCP chain on Java 17 only, with module-local compiler settings and a dedicated `mcp` CI lane.
- **Rationale**: The standalone MCP runtime is implemented and packaged as a Java 17 subchain, while the rest of the repository still uses Java 8 as its default baseline.
- **Alternatives considered**:
  - Upgrade the whole repository to Java 17: rejected as out of scope and too disruptive.
  - Compile the MCP chain against Java 8 and shade newer runtime behavior: rejected because it complicates the standalone HTTP listener and packaging path.

## Decision 4: Standardize on a bootstrap-scoped MCP SDK transport runtime

- **Decision**: Keep `mcp/core` on repository-owned domain DTOs and session facades, but use the MCP Java SDK plus embedded Tomcat only inside `mcp/bootstrap` for the Streamable HTTP runtime.
- **Rationale**: This reduces protocol boilerplate, keeps the SDK boundary out of `mcp/core`, and lets Jackson remain aligned to the repository-managed 2.16.1 version through Maven dependency mediation.
- **Alternatives considered**:
  - Keep a fully custom HTTP runtime: rejected because it leaves more protocol and SSE lifecycle code for the repository to maintain.
  - Let `mcp/core` depend on SDK types: rejected because it would couple ShardingSphere domain contracts to external protocol classes.

## Decision 5: Use Streamable HTTP plus STDIO, with stateful session semantics

- **Decision**: Expose MCP over Streamable HTTP at `/mcp` and support STDIO for local debugging; treat HTTP as a stateful session model rather than a stateless request model.
- **Rationale**: The PRD requires transaction and savepoint semantics, which makes per-session state mandatory; the detailed design also fixes `POST`, `GET`, and `DELETE` behavior on `/mcp`.
- **Alternatives considered**:
  - Stateless HTTP only: rejected because transaction and savepoint semantics would become inconsistent or impossible.
  - STDIO only: rejected because the product needs a deployable remote service entry point.

## Decision 6: Keep HTTP cluster semantics simple in V1

- **Decision**: Require sticky session routing and store session state in local memory only.
- **Rationale**: This is the smallest design that supports transactions and savepoints without adding distributed session coordination or failover recovery complexity.
- **Alternatives considered**:
  - Distributed session store in V1: rejected because it increases architecture and operational complexity beyond the approved scope.
  - Best-effort failover with partial session replay: rejected because it would create ambiguous transaction semantics and unclear rollback behavior.

## Decision 7: Model capability, session, audit, refresh visibility, and transaction matrix as first-class core domain concepts

- **Decision**: Implement a strong typed transaction matrix registry plus capability assembly, session state, audit recording, and metadata-refresh coordination in `mcp/core`.
- **Rationale**: These objects define the current product contract itself: supported objects, statement classes, transaction behavior, DDL / DCL visibility, and database-specific differences.
- **Alternatives considered**:
  - Store the transaction matrix only in YAML or JSON: rejected because the detailed design prefers Java strong typing for correctness and testability.
  - Compute everything dynamically from metadata only: rejected because fixed defaults and explicit V1 guarantees would become fragile.

## Decision 8: Keep the built-in runtime boundary minimal and rely on external network controls for exposed deployments

- **Decision**: Keep the built-in boundary minimal but not anonymous:
  use loopback defaults, local-mode `Origin` checks, session / protocol validation,
  and a shared bearer token admission gate for configured HTTP protection;
  leave broader exposed-endpoint governance to external network controls.
- **Rationale**: The standalone runtime still belongs behind a trusted network, gateway, or reverse proxy when exposed externally,
  but remote HTTP should no longer remain an anonymous entry point.
- **Alternatives considered**:
  - Introduce a new in-process enforcement SPI now: rejected because it expands the runtime contract beyond the implemented feature set and would misrepresent the shipped surface.
  - Omit boundary guidance entirely: rejected because operators still need explicit deployment expectations for exposed HTTP endpoints.

## Decision 9: Package MCP as a standalone distribution with first-class operations support

- **Decision**: Produce `shardingsphere-mcp-distribution` with scripts, configuration, Dockerfile, logs directory, and standalone release layout.
- **Rationale**: The MCP service must be deployable, governable, and releasable independent of Proxy and JDBC.
- **Alternatives considered**:
  - Piggyback on Proxy distribution assets: rejected because MCP is not a Proxy facade and has a different runtime shape.
  - Publish jars only: rejected because operations, startup, and containerization requirements are part of the approved design.

## Decision 10: Verify behavior through layered tests, including protocol and database matrix scenarios

- **Decision**: Plan for four test layers: unit, module integration, protocol integration, and E2E.
- **Rationale**: The feature spans protocol handling, session semantics, audit and refresh visibility, result mapping, and database capability differences; a single test layer would miss regressions.
- **Alternatives considered**:
  - Unit tests only: rejected because transport, session, and refresh boundaries need integration coverage.
  - E2E only: rejected because root-cause diagnosis and coverage quality would be too weak for core infrastructure code.
