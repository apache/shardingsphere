# Research: MCP DistSQL-backed Governance Metadata Resources

## Source-driven Findings

### MCP resource shape

- Official MCP resource documentation says resources let servers expose contextual data, including database schemas or application-specific information, identified by URI.
- Official MCP resource documentation also defines resource templates for parameterized resources and standard not-found/internal error handling.
- Design implication: storage unit and single table metadata should be exposed as read-only MCP resources and resource templates, not by widening the SQL execution tool.

Source:

- `https://modelcontextprotocol.io/specification/2025-06-18/server/resources`

### MCP tool boundary

- Official MCP tool documentation describes tools as model-invoked operations for querying external systems or performing computations.
- ShardingSphere already has `database_gateway_search_metadata` as a controlled metadata discovery tool.
- Design implication: the search tool can gain a `storage_unit` object type, but raw `SHOW` execution should remain blocked by the SQL safety validator.

Source:

- `https://modelcontextprotocol.io/specification/2025-06-18/server/tools`

### ShardingSphere DistSQL backing

The required read-only data is already available through documented Proxy DistSQL:

- `SHOW STORAGE UNITS [FROM databaseName] [LIKE pattern]`
- `SHOW RULES USED STORAGE UNIT storageUnitName [FROM databaseName]`
- `SHOW SINGLE TABLE tableName [FROM databaseName]`
- `SHOW SINGLE TABLES [LIKE pattern] [FROM databaseName]`
- `SHOW DEFAULT SINGLE TABLE STORAGE UNIT [FROM databaseName]`

Sources:

- `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/rql/storage-unit-query/show-storage-units.en.md`
- `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/ral/show-rules-used-storage-unit.en.md`
- `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/single-table/show-single-table.en.md`
- `docs/document/content/user-manual/shardingsphere-proxy/distsql/syntax/rql/rule-query/single-table/show-deafult-single-table-storage-unit.en.md`

### Existing MCP gap

- Core MCP metadata resources cover logical database metadata but not storage units.
- `database_gateway_search_metadata` does not include `storage_unit` in its object type set.
- Metadata introspection SQL is rejected by `SQLStatementSafetyValidator`, so missing resources cannot be worked around safely through `database_gateway_execute_query`.
- Readwrite-splitting, shadow and sharding workflows already need storage unit names in their rule planning inputs.

Sources:

- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-core.yaml`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/SQLStatementSafetyValidator.java`
- `mcp/features/readwrite-splitting/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-readwrite-splitting.yaml`
- `mcp/features/shadow/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-shadow.yaml`
- `mcp/features/sharding/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors/mcp-descriptor-sharding.yaml`

## Decisions

### Decision 1: Use resources for governance metadata

Expose storage units, storage unit usage, single table mappings and default single table storage unit as MCP resources.

Rationale:

- These are read-only contextual metadata.
- They map directly to stable Proxy DistSQL.
- The MCP resource model fits URI-addressable metadata better than a model-controlled raw SQL tool.

### Decision 2: Keep raw `SHOW` blocked

Do not relax `database_gateway_execute_query` safety validation for `SHOW`, `DESC` or `DESCRIBE`.

Rationale:

- The current safety model deliberately routes metadata introspection through MCP resources.
- Opening raw `SHOW` would create an unbounded surface and weaken recovery guidance.

### Decision 3: Place first implementation in core MCP

Prefer implementing the new resources in `mcp/core` unless implementation evidence shows a stronger module boundary.

Rationale:

- Storage units and single table mappings are generic Proxy governance metadata.
- No existing dedicated MCP feature module owns single table resources.
- Search and metadata-introspection recovery already live in core MCP.

### Decision 4: Use existing query facade

Use the existing MCP query facade or equivalent workflow query infrastructure to execute DistSQL.

Rationale:

- It avoids a second connection path.
- It keeps behavior aligned with existing feature inspection services.
- It preserves Proxy-mode semantics instead of probing backend physical databases.

### Decision 5: Redact sensitive storage unit fields

Return stable storage unit metadata while redacting or omitting secret-like values from `other_attributes` and any future secret-bearing fields.

Rationale:

- The user need is discovery and validation, not credential retrieval.
- MCP resources may be loaded into LLM context, so secret minimization is part of the resource contract.

## Risks and Mitigations

- Risk: DistSQL identifier quoting is mishandled for database, storage unit or table names.
  Mitigation: use existing ShardingSphere SQL/DistSQL utility patterns instead of ad hoc string concatenation.

- Risk: `LIKE` filtering is mistaken for exact detail lookup.
  Mitigation: detail resources must exact-match the returned `name` or `table_name` after query.

- Risk: an older Proxy target does not support one of the backing DistSQL statements.
  Mitigation: return an explicit unsupported-capability response and keep recovery guidance actionable.

- Risk: storage unit metadata leaks credentials through `other_attributes`.
  Mitigation: define and test redaction before exposing payloads.

- Risk: resources become too large for databases with many storage units or single tables.
  Mitigation: preserve existing MCP resource conventions and prefer search/filtering when broad reads are large.

## Clarification Status

No blocking user clarification is required before implementation planning.

Non-blocking defaults:

- Use explicit database path parameters in every resource, even when DistSQL has an optional `FROM databaseName`.
- Preserve DistSQL row shape where practical.
- Preserve duplicate used-by rows unless implementation evidence proves duplicates are accidental and existing MCP conventions require de-duplication.
- Exclude migration source storage units from this feature because they were not part of the reported MCP data source information gap.

## Reanalysis Notes

### R1: Metadata search must not pollute JDBC metadata contracts

Finding:

- `database_gateway_search_metadata` currently depends on `MCPMetadataQueryFacade`.
  That facade represents logical JDBC-style metadata such as databases, schemas, tables, views, columns, indexes and sequences.
- Storage units are Proxy governance metadata queried through DistSQL, not JDBC metadata.
- Adding `storage_unit` search must not force storage units into `MCPMetadataQueryFacade` as if they were physical or JDBC metadata.

Decision:

- Keep storage unit querying behind the existing direct query facade or a focused governance metadata query helper.
- If `SupportedMCPMetadataObjectType` needs a `STORAGE_UNIT` enum value for argument parsing, do not treat that as JDBC metadata support.
- Update search collection so storage unit hits come from the DistSQL-backed governance metadata path.

### R2: Search scope may include `mcp/support`

Finding:

- The object type enum used by metadata search lives in `mcp/support`.
- The first implementation plan that named only `mcp/core` was too narrow for the search object type slice.

Decision:

- Keep production behavior in `mcp/core` where possible.
- Allow a narrow `mcp/support` change only for shared object type parsing or capability contracts required by `storage_unit` search.

### R3: Detail lookup should prefer exact filtering over `LIKE`

Finding:

- `SHOW STORAGE UNITS` supports `LIKE`, but `%` and `_` are pattern characters.
- Using `LIKE` for a detail resource can create escaping complexity and false positives.

Decision:

- Default storage unit detail lookup to `SHOW STORAGE UNITS FROM databaseName` followed by exact identifier comparison.
- Use `LIKE` only for optional list/search narrowing after escaping behavior is explicitly verified.

### R4: Single table default URI should avoid path-template ambiguity

Finding:

- `shardingsphere://databases/{database}/single-tables/{table}` consumes one variable path segment.
- A default resource under `single-tables/default-storage-unit` could be ambiguous with a table named `default-storage-unit`.

Decision:

- Keep the default resource as `shardingsphere://databases/{database}/single-table/default-storage-unit`.
- The singular `single-table` segment is intentional to avoid overlap with the table-name template.

### R5: URI variable names should follow existing core conventions

Finding:

- Existing core metadata resource templates use short variable names such as `{database}`, `{schema}`, `{table}`, `{column}`, `{index}` and `{sequence}`.
- Using a long-form table name variable for single table resources would diverge from the established table template convention.
- Storage units do not have an existing URI variable, but `{storageUnit}` matches the existing short variable naming style.
- Existing resource hint and recovery helper logic derives requested tokens and resource kinds from known URI variable names and path segments, so new resource families need explicit coverage.

Decision:

- Use `shardingsphere://databases/{database}/storage-units/{storageUnit}` for storage unit detail resources.
- Use `shardingsphere://databases/{database}/single-tables/{table}` for single table detail resources.
- Include implementation tasks to update resource kind, navigation, parent or recovery helper logic for `storage-units`, `single-tables` and `single-table/default-storage-unit` paths.

### R6: No new blocking question remains

Repeated self-question:

- Is there another issue worth reanalyzing before ending this planning step?

Answer:

- No blocking issue remains.
- Remaining choices are implementation details already captured by tasks and can be resolved during Slice 0 inspection.
