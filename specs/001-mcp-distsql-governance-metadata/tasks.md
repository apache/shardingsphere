# Tasks: MCP DistSQL-backed Governance Metadata Resources

## How to Use This Task List

- Implement one slice at a time and keep each slice independently reviewable.
- Do not switch or create branches.
- Do not add new DistSQL grammar, raw `SHOW` passthrough, or physical datasource probing.
- If implementation creates a new public production type, add direct focused tests for that type.
- Prefer existing MCP query facade, resource handler, metadata search and recovery patterns before adding abstractions.
- Do not force storage units into `MCPMetadataQueryFacade`; they are DistSQL-backed governance metadata.

## Slice 0 - Preflight and Contracts

- [x] T001 Confirm current branch and dirty worktree before implementation; do not switch branches.
- [x] T002 Re-read `AGENTS.md`, `CODE_OF_CONDUCT.md`, this spec, `research.md`, and `plan.md`.
- [x] T003 Inspect `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/handler/core/CoreResourceHandlers.java`.
- [x] T004 Inspect `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/metadata/MetadataResourceHandler.java`.
- [x] T005 Inspect `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/CoreResourceHandlerSurfaceTest.java`.
- [x] T006 Inspect `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/metadata/MetadataResourceHandlerTest.java`.
- [x] T007 Inspect `WorkflowSQLUtils.formatDistSQLIdentifier` and reuse it for DistSQL identifier rendering.
- [x] T008 Decide whether a focused governance metadata query service is needed.
- [x] T009 Record the final production write scope before editing code.
- [x] T010 Inspect whether `SupportedMCPMetadataObjectType` needs a narrow `STORAGE_UNIT` addition in `mcp/support`.
- [x] T011 Confirm that any `mcp/support` change does not advertise storage units as JDBC metadata capability.
- [x] T012 Confirm resource template variables use `{storageUnit}` and `{table}` to match existing core URI conventions.

Definition of done:

- The implementation owner knows the exact first slice write set.
- The plan still excludes raw `SHOW`, new DistSQL grammar and physical datasource probing.

## Slice 1 - Storage Unit Query Foundation

- [x] T013 Add or choose a focused query component for governance metadata queries.
- [x] T014 Back `queryStorageUnits(database)` with `SHOW STORAGE UNITS FROM databaseName`.
- [x] T015 Back `queryStorageUnit(database, storageUnitName)` with storage unit query plus exact name matching.
- [x] T016 Avoid using `LIKE` for detail lookup unless escaping behavior is explicitly verified.
- [x] T017 Back `queryRulesUsedStorageUnit(database, storageUnitName)` with `SHOW RULES USED STORAGE UNIT storageUnitName FROM databaseName`.
- [x] T018 Use `WorkflowSQLUtils.formatDistSQLIdentifier` for database, storage unit and table identifiers.
- [x] T019 Preserve DistSQL result rows as structured maps unless redaction applies.
- [x] T020 Add unsupported-DistSQL handling that matches existing workflow query conventions.

Candidate files:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/...`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/...`

Definition of done:

- Storage unit DistSQL is centralized enough to avoid copy-paste SQL construction.
- Unsupported DistSQL and query failures have explicit behavior.

## Slice 2 - Storage Unit Resource Surface

- [x] T021 Register `shardingsphere://databases/{database}/storage-units`.
- [x] T022 Register `shardingsphere://databases/{database}/storage-units/{storageUnit}`.
- [x] T023 Register `shardingsphere://databases/{database}/storage-units/{storageUnit}/used-by-rules`.
- [x] T024 Add descriptors for the three resource templates in `mcp-descriptor-core.yaml`.
- [x] T025 Ensure list resource returns all storage unit rows for the database.
- [x] T026 Ensure detail resource returns only the exact matching storage unit.
- [x] T027 Ensure used-by resource returns `type` and `name` rows and preserves duplicate rows.
- [x] T028 Update resource kind, parent, next-resource or navigation helper logic for `storage-units` paths if implementation uses those helpers.
- [x] T029 Add resource surface tests proving the new URIs are registered and dispatchable.

Candidate files:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/handler/core/CoreResourceHandlers.java`
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/metadata/MetadataResourceHandler.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/CoreResourceHandlerSurfaceTest.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/MCPResourceControllerTest.java`

Definition of done:

- MCP clients can read storage unit list, detail and usage resources without using SQL execution.

## Slice 3 - Sensitive Field Redaction

- [x] T030 Define storage unit redaction rules for secret-like keys and future secret-bearing fields.
- [x] T031 Redact or omit password, token, secret, credential and similar sensitive attributes.
- [x] T032 Avoid blanket-redacting non-secret fields merely because their names contain `key`.
- [x] T033 Preserve non-sensitive storage unit fields such as name, type, host, port, db and pool settings.
- [x] T034 Add tests for redacting nested `other_attributes` values when represented as JSON-like strings or maps.
- [x] T035 Add tests proving non-sensitive fields remain visible.

Candidate files:

- Same query/resource component introduced in Slice 1.
- New direct test class if the redaction component is public.

Definition of done:

- No sensitive storage unit values can enter MCP resource payloads in covered cases.

## Slice 4 - Single Table Resource Surface

- [x] T036 Back `querySingleTables(database)` with `SHOW SINGLE TABLES FROM databaseName`.
- [x] T037 Back `querySingleTable(database, tableName)` with `SHOW SINGLE TABLE tableName FROM databaseName`.
- [x] T038 Back `queryDefaultSingleTableStorageUnit(database)` with `SHOW DEFAULT SINGLE TABLE STORAGE UNIT FROM databaseName`.
- [x] T039 Register `shardingsphere://databases/{database}/single-tables`.
- [x] T040 Register `shardingsphere://databases/{database}/single-tables/{table}`.
- [x] T041 Register `shardingsphere://databases/{database}/single-table/default-storage-unit`.
- [x] T042 Keep the singular `single-table/default-storage-unit` path to avoid colliding with `{table}`.
- [x] T043 Update resource kind, parent, next-resource or navigation helper logic for `single-tables` and `single-table/default-storage-unit` paths if implementation uses those helpers.
- [x] T044 Add descriptors for the three single table resource templates in `mcp-descriptor-core.yaml`.
- [x] T045 Add tests for list, detail, empty result, not-found and query failure.

Candidate files:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/handler/core/CoreResourceHandlers.java`
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/metadata/MetadataResourceHandler.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/CoreResourceHandlerSurfaceTest.java`

Definition of done:

- MCP can answer table-to-storage-unit and default single table storage unit questions from resources.

## Slice 5 - Metadata Search Integration

- [x] T046 Add `storage_unit` to the supported object types in `database_gateway_search_metadata`.
- [x] T047 If required, add `STORAGE_UNIT` to `SupportedMCPMetadataObjectType` in `mcp/support`.
- [x] T048 Do not add storage unit query methods to `MCPMetadataQueryFacade`.
- [x] T049 Add storage unit search rows backed by the governance metadata query from Slice 1.
- [x] T050 Add storage unit resource URI derivation in `MetadataSearchResourceUriFactory`.
- [x] T051 Update descriptor input schema enum and output examples if present.
- [x] T052 Update `CoreToolDescriptorValidator` expectations if descriptor validation requires the new object type.
- [x] T053 Add tests for allowed `storage_unit` object type.
- [x] T054 Add tests for storage unit search result payload and resource URI hints.
- [x] T055 Add tests proving unrelated table or column hits are not returned for `object_types=["storage_unit"]`.

Candidate files:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolHandler.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/MetadataSearchResourceUriFactory.java`
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolHandlerTest.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolServiceTest.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/capability/SupportedMCPMetadataObjectType.java` only if parser support requires it.

Definition of done:

- Storage units are discoverable through the existing metadata search tool without treating them as JDBC metadata.

## Slice 6 - SQL Recovery Guidance

- [x] T056 Inspect how `SQLStatementSafetyValidator` reports `statement_type` for `SHOW STORAGE UNITS` and single table statements.
- [x] T057 Add recovery hints for `SHOW STORAGE UNITS` to point to storage unit resources and `storage_unit` search.
- [x] T058 Add recovery hints for `SHOW SINGLE TABLES` and `SHOW SINGLE TABLE` to point to single table resources.
- [x] T059 Add recovery hints for `SHOW DEFAULT SINGLE TABLE STORAGE UNIT` to point to the default resource.
- [x] T060 Use `{storageUnit}` and `{table}` consistently in recovery resource hint templates.
- [x] T061 Keep all metadata introspection SQL rejected by `database_gateway_execute_query`.
- [x] T062 Add recovery payload tests through the existing error converter or SQL tool handler tests.

Candidate files:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPSQLRecoveryPayloadFactory.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/SQLStatementSafetyValidator.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/MCPErrorConverterTest.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteQueryToolHandlerTest.java`

Definition of done:

- A user attempting supported `SHOW` metadata SQL receives resource-oriented recovery, not permission to execute raw SQL.

## Slice 7 - Workflow Guidance

- [x] T063 Add storage unit resource hints when readwrite-splitting planning lacks write/read storage unit names.
- [x] T064 Add storage unit resource hints when readwrite-splitting validation needs to confirm storage unit names.
- [x] T065 Add storage unit resource hints when shadow planning lacks source/shadow storage unit names.
- [x] T066 Add storage unit resource hints when shadow validation needs to confirm source/shadow storage unit names.
- [x] T067 Add storage unit resource hints when sharding planning lacks storage unit names.
- [x] T068 Keep workflow behavior advisory only; do not add storage unit creation or repair actions.
- [x] T069 Add focused tests for each changed workflow response.

Candidate files:

- `mcp/features/readwrite-splitting/src/main/java/.../tool/service/*WorkflowPlanningService.java`
- `mcp/features/readwrite-splitting/src/test/java/.../tool/service/*WorkflowPlanningServiceTest.java`
- `mcp/features/shadow/src/main/java/.../tool/service/*WorkflowPlanningService.java`
- `mcp/features/shadow/src/test/java/.../tool/service/*WorkflowPlanningServiceTest.java`
- `mcp/features/sharding/src/main/java/.../tool/service/*WorkflowPlanningService.java`
- `mcp/features/sharding/src/test/java/.../tool/service/*WorkflowPlanningServiceTest.java`

Definition of done:

- Missing or ambiguous storage unit workflow responses point clients to the storage unit resource instead of asking users to guess names first.

## Slice 8 - User Documentation

- [x] T070 Update MCP capabilities documentation with the new storage unit and single table resources.
- [x] T071 Document backing DistSQL and explicitly say raw `SHOW` remains blocked in the SQL tool.
- [x] T072 Document sensitive-field redaction behavior.
- [x] T073 Document non-goals: no new DistSQL, no physical datasource probing, no write operations.
- [x] T074 Add examples for reading storage units, used-by rules and single table mappings.

Candidate files:

- `docs/document/content/user-manual/shardingsphere-mcp/capabilities.en.md`
- `docs/document/content/user-manual/shardingsphere-mcp/capabilities.cn.md` if the English page has a Chinese counterpart.
- Feature-specific MCP docs only if workflow hints are changed.

Definition of done:

- Users can discover the new MCP resource path and understand what remains intentionally unsupported.

## Slice 9 - Verification and Handoff

- [x] T075 Run scoped core tests after core resource/search/recovery changes.
- [x] T076 Run scoped support tests if `mcp/support` changes.
- [x] T077 Run feature module tests if workflow guidance changes.
- [x] T078 Run Spotless after the final file-changing action.
- [x] T079 Run Checkstyle after Spotless.
- [x] T080 Confirm no branch switch occurred.
- [x] T081 Summarize changed files, executed commands, exit codes, remaining risks and rollback path.

Suggested commands:

- `./mvnw -pl mcp/core -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/support -DskipITs -Dspotless.skip=true test`
- `./mvnw -pl mcp/features/readwrite-splitting,mcp/features/shadow,mcp/features/sharding -DskipITs -Dspotless.skip=true test`
- `./mvnw spotless:apply -Pcheck -T1C`
- `./mvnw checkstyle:check -Pcheck -T1C`

## Dependencies and Parallelization

- Slice 0 must complete first.
- Slice 1 must complete before Slices 2, 3, 4 and 5.
- Slices 2 and 3 should be implemented together or back-to-back because resource payloads must be redacted before exposure.
- Slice 4 can proceed after Slice 1 and does not depend on Slice 2.
- Slice 5 depends on Slice 2 resource URIs.
- Slice 6 depends on the resource URIs from Slices 2 and 4.
- Slice 7 depends on storage unit list/detail resources from Slice 2.
- Slice 8 can start after resource URI names are stable.
- Slice 9 is final and must run after all selected implementation slices.

## Clarification Audit

No blocking question remains for task splitting.

Defaults carried forward:

- Start implementation in `mcp/core`.
- Allow a narrow `mcp/support` touch only for shared object type parsing if required.
- Exclude migration source storage units.
- Preserve duplicate used-by rows.
- Redact secret-like storage unit attributes.
- Keep raw metadata `SHOW` rejected.
- Keep storage units out of `MCPMetadataQueryFacade` JDBC metadata contracts.
