# Contract: Production Runtime Acceptance

## Purpose

Define the minimum end-to-end acceptance behavior that must hold before this follow-up can claim the packaged MCP runtime fulfills the PRD product intent rather than only the transport shell.

## Acceptance Scope

The acceptance path for this follow-up covers:

- packaged startup
- MCP initialize and session negotiation
- real metadata discovery
- real SQL execution
- metadata refresh visibility
- session close
- MCP host registration expectations

## Packaged Startup Contract

- `bin/start.sh` MUST fail fast when production provider configuration is incomplete or invalid.
- A successful packaged startup MUST mean:
  - the HTTP listener is ready
  - a real metadata provider is ready
  - a real execution adapter is ready
- Successful startup MUST NOT depend on empty metadata/runtime defaults.

## Metadata Discovery Acceptance Contract

After successful initialize, the packaged runtime MUST support at least one acceptance slice where:

1. `list_databases` returns a non-empty logical database list.
2. `get_capabilities(database)` returns a database capability consistent with runtime facts.
3. `list_tables` or `search_metadata` returns real metadata objects.
4. `describe_table` returns real column information.

If the selected runtime scope contains real logical databases, empty success responses are acceptance failures.

## SQL Execution Acceptance Contract

At least one acceptance slice MUST prove:

1. `execute_query(database, "SELECT ...")` succeeds against the real execution path.
2. One DML scenario succeeds when allowed by capability, or returns `unsupported` when not allowed.
3. Transaction control remains bound to a single logical database.
4. `SAVEPOINT` either succeeds consistently with capability or returns `unsupported`.

Backend-native behaviors MUST still map to MCP unified result and error surfaces.

## Refresh Visibility Acceptance Contract

When a selected acceptance slice includes DDL or DCL:

1. The committing session sees the change immediately after commit.
2. Global visibility converges within 60 seconds.
3. Session close does not preserve resumable transactional state.

## Session Close Acceptance Contract

- `DELETE /mcp` MUST close the session.
- Closing a session with an active transaction MUST roll back uncommitted work.
- A closed session id MUST be treated as invalid on follow-up requests.

## MCP Host Registration Contract

- MCP hosts discover this server through explicit registration, not through automatic network scanning.
- After the host is configured with the MCP HTTP endpoint and completes `initialize`, it MUST automatically discover non-empty tools and resources from the server.
- Production acceptance MUST include at least one metadata tool call and one SQL tool call initiated through a registered host or an equivalent protocol-level client.

## Failure Conditions

The follow-up is not accepted if any of the following remains true:

- the packaged runtime can still start successfully with empty metadata/runtime defaults
- discovery works only with fixture-injected metadata
- `execute_query` still succeeds only against an in-memory runtime model
- production docs describe only transport reachability and not runtime readiness
