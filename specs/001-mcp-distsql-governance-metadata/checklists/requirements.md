# Requirements Checklist: MCP DistSQL-backed Governance Metadata Resources

**Purpose**: Validate that the feature remains limited to existing DistSQL-backed MCP governance metadata resources.
**Created**: 2026-06-14
**Feature**: `specs/001-mcp-distsql-governance-metadata/spec.md`

## Scope Control

- [x] CHK001 The feature is read-only.
- [x] CHK002 Every included capability is backed by an existing documented DistSQL statement.
- [x] CHK003 New DistSQL syntax is explicitly out of scope.
- [x] CHK004 Raw `SHOW` passthrough is explicitly out of scope.
- [x] CHK005 Physical datasource probing and credential retrieval are explicitly out of scope.
- [x] CHK006 Migration source storage units are excluded unless a future requirement explicitly scopes migration MCP capabilities.

## Resource Coverage

- [x] CHK007 Storage unit list resource is specified.
- [x] CHK008 Storage unit detail resource is specified.
- [x] CHK009 Storage unit used-by rules resource is specified.
- [x] CHK010 Single table list resource is specified.
- [x] CHK011 Single table detail resource is specified.
- [x] CHK012 Default single table storage unit resource is specified.
- [x] CHK013 Metadata search support for `storage_unit` is specified.
- [x] CHK014 SQL recovery guidance for supported metadata introspection statements is specified.

## Security and Compatibility

- [x] CHK015 Sensitive storage unit properties must be redacted or omitted.
- [x] CHK016 Resource URI variables must be validated before DistSQL execution.
- [x] CHK017 The plan avoids opening a second physical datasource connection path.
- [x] CHK018 Existing SQL safety validation remains intact.
- [x] CHK019 Older Proxy unsupported-DistSQL behavior is acknowledged.
- [x] CHK020 Explicit database path parameters are required instead of relying on connection-selected database state.
- [x] CHK021 Storage units must not be added to `MCPMetadataQueryFacade` as JDBC metadata.
- [x] CHK022 A narrow `mcp/support` object type change is allowed only if metadata search argument parsing requires it.
- [x] CHK023 Storage unit detail lookup defaults to list-plus-exact-match instead of `LIKE`.
- [x] CHK024 The default single table storage unit URI avoids collision with `{table}`.
- [x] CHK025 Resource template variables follow existing core naming conventions, including `{storageUnit}` and `{table}`.
- [x] CHK026 Resource kind, parent, navigation and recovery helper behavior must be updated for the new resource families if those helpers are used.

## Testability

- [x] CHK027 Acceptance criteria are measurable through resource reads, search calls or recovery payload assertions.
- [x] CHK028 Unit tests are required for success, empty result, not-found, failure and redaction cases.
- [x] CHK029 Workflow hint tests are required if feature planning responses change.
- [x] CHK030 Verification commands are listed in the plan.

## Clarification Audit

- [x] CHK031 No blocking user clarification remains before starting implementation.
- [x] CHK032 Non-blocking defaults are documented in `research.md` and `plan.md`.
