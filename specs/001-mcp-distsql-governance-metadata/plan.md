# Implementation Plan: MCP DistSQL-backed Governance Metadata Resources

## Technical Context

- Problem: MCP cannot answer storage-unit-oriented data source questions even though Proxy has dedicated DistSQL for the required governance metadata.
- Scope: add read-only MCP resources, metadata search support, recovery guidance and workflow resource hints for existing DistSQL-backed metadata.
- Out of scope: new DistSQL grammar, raw `SHOW` passthrough, physical datasource inspection, write operations and structured algorithm property templates.
- Primary modules for future implementation: `mcp/core`.
  A narrow `mcp/support` change may be needed for shared metadata object type parsing.
  Limited updates may be needed in `mcp/features/readwrite-splitting`, `mcp/features/shadow` and `mcp/features/sharding` if workflow guidance changes.
- Dependencies: existing MCP resource handler registry, core descriptor YAML, `database_gateway_search_metadata`, SQL safety recovery payloads and MCP query facade.

## Compliance Check

- [x] Current phase is documentation and planning only; no Java or YAML implementation changes are included.
- [x] Current branch remains unchanged; do not switch or create branches for this task.
- [x] Every planned capability is backed by existing documented DistSQL.
- [x] The SQL execution tool remains constrained; raw metadata introspection stays blocked.
- [x] Resource design follows MCP's read-only resource model for contextual data.
- [x] Sensitive storage unit fields must be redacted or omitted.
- [x] Future implementation must use mocks for query facade tests and follow repository unit test rules.
- [x] Future implementation must run scoped MCP tests first, then Spotless and Checkstyle.

## Phase 0: Research

Status: complete for planning.

Completed findings:

- MCP resources are the correct primitive for read-only context.
- Existing DistSQL supports all included metadata.
- Current MCP descriptors and handlers do not expose storage units or single table mappings.
- Current SQL safety behavior intentionally blocks `SHOW`, so resources and recovery guidance are the safe path.

Remaining research before coding:

- Inspect existing `MetadataResourceHandler` payload conventions for list, detail, empty and recovery responses.
- Inspect existing `SearchMetadataToolHandler` object type validation and URI factory patterns.
- Inspect whether `storage_unit` search requires a narrow `mcp/support` enum change without treating storage units as JDBC metadata.
- Inspect existing feature workflow response structures before adding `resources_to_read` hints.

## Phase 1: Design

### Resource Templates

Planned templates:

- `shardingsphere://databases/{database}/storage-units`
- `shardingsphere://databases/{database}/storage-units/{storageUnit}`
- `shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules`
- `shardingsphere://databases/{database}/single-tables`
- `shardingsphere://databases/{database}/single-tables/{table}`
- `shardingsphere://databases/{database}/single-table/default-storage-unit`

### Backing DistSQL

- Storage unit list: `SHOW STORAGE UNITS FROM databaseName`
- Storage unit detail: `SHOW STORAGE UNITS FROM databaseName`, followed by exact name matching.
  Use `LIKE` only if escaping behavior is explicitly verified.
- Storage unit usage: `SHOW RULES USED STORAGE UNIT storageUnitName FROM databaseName`
- Single table list: `SHOW SINGLE TABLES FROM databaseName`
- Single table detail: `SHOW SINGLE TABLE tableName FROM databaseName`
- Default single table storage unit: `SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM databaseName`

### Search and Recovery

- Add `storage_unit` to `database_gateway_search_metadata` object type handling.
- Return resource URIs that point to the new storage unit detail resources.
- Map rejected metadata introspection SQL for supported statements to resource hints where practical.
- Keep storage unit search backed by DistSQL governance metadata, not by `MCPMetadataQueryFacade` JDBC metadata.

### Security and Payload Policy

- Do not expose credentials, passwords, tokens or secret-like attributes.
- Preserve non-sensitive DistSQL columns.
- Keep Proxy storage unit metadata separate from backend physical datasource metadata.

### Rollback Plan

- Descriptor/resource changes can be reverted without data migration.
- No database schema or Proxy DistSQL change is planned.
- If a resource causes compatibility issues, disable the new resource registration and object type while preserving existing SQL safety behavior.

## Phase 2: Task Planning

Implement in independently reviewable increments:

1. Add storage unit list/detail resources and tests.
2. Add storage unit used-by resource and tests.
3. Add single table and default single table storage unit resources and tests.
4. Add metadata search support and recovery guidance tests.
5. Add workflow `resources_to_read` hints for readwrite-splitting, shadow and sharding planning where missing storage unit names are handled.
6. Update user-facing MCP documentation.

## Verification Strategy

Suggested scoped verification after implementation:

- `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/features/readwrite-splitting,mcp/features/shadow,mcp/features/sharding -DskipITs -Dspotless.skip=true test`
- `./mvnw spotless:apply -Pcheck -T1C`
- `./mvnw checkstyle:check -Pcheck -T1C`

If implementation touches only `mcp/core`, skip feature module tests unless workflow guidance changes are also made.

## MCP Builder Review Notes

- Use resources for read-only metadata and keep tools focused.
- Keep URI names descriptive and consistent with existing `shardingsphere://databases/...` patterns.
- Keep returned data structured and stable for programmatic clients.
- Add actionable errors for not-found, unsupported DistSQL and query failures.
- Avoid raw external execution surfaces and validate URI variables before building DistSQL.

## Doubt-driven Review Notes

Claim: The next implementation should be a core MCP resource expansion backed only by existing DistSQL, without widening SQL execution.

Adversarial checks:

- Could single table resources belong to a separate feature module? Current evidence says no dedicated MCP single table module exists, and the capability is generic governance metadata.
- Could used-by rows be de-duplicated for a cleaner payload? The spec preserves DistSQL output because changing row multiplicity may hide current Proxy behavior.
- Could `LIKE` support cause false detail matches? Detail resources require exact matching after query.
- Could sensitive attributes leak through JSON-like `other_attributes`? The plan requires explicit redaction and tests before exposure.
- Could workflow hints expand scope beyond resources? Hints only point clients to new resources and do not add new rule semantics.
- Could `storage_unit` search pollute JDBC metadata capability contracts? The updated plan restricts any `mcp/support` change to parsing/shared type support and keeps collection DistSQL-backed.
- Could storage unit detail use `LIKE` incorrectly? The updated plan defaults to list-plus-exact-match and treats `LIKE` as optional only after escaping is verified.
- Could resource template variables drift from existing core URI conventions?
  The updated plan uses `{storageUnit}` and `{table}`, and implementation tasks must update resource kind or navigation helper logic for these new resource families.

Result: no blocking issue found. The plan is ready for implementation once code changes are authorized.

## Clarification Audit

Question asked before ending: is there any issue that must be confirmed with the user before implementation can start?

Answer: no blocking question remains.

Non-blocking choices already defaulted:

- Implement in `mcp/core` first.
- Exclude migration source storage units.
- Preserve duplicate used-by rows.
- Redact secret-like storage unit attributes.
- Keep raw `SHOW` rejected.
- Keep storage unit search out of the JDBC metadata facade.
