# Tasks: ShardingSphere MCP Sequence Dialect Matrix

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/017-shardingsphere-mcp-sequence-dialect-matrix/`  
**Prerequisites**: plan.md (required), spec.md (required)

**Tests**: Add and update dedicated capability/loader matrix tests for sequence support across supported database types.

## Phase 1: Setup (Spec Freeze)

- [x] T001 Add dialect-matrix scope and acceptance criteria to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/017-shardingsphere-mcp-sequence-dialect-matrix/spec.md`
- [x] T002 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/017-shardingsphere-mcp-sequence-dialect-matrix/plan.md`
- [x] T003 [P] Freeze executable tasks in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/017-shardingsphere-mcp-sequence-dialect-matrix/tasks.md`

## Phase 2: Capability Matrix

- [x] T004 Update sequence-supporting dialects in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/`
- [x] T005 Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProviderTest.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/SupportedDatabaseContractMatrixE2ETest.java`
  for the new sequence support matrix

## Phase 3: Metadata Loader Matrix

- [x] T006 Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java`
  with per-dialect sequence queries for supported databases
- [x] T007 Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoaderTest.java`
  to cover mocked sequence loading for all supported dialects

## Phase 4: Polish & Verification

- [x] T008 [P] Run scoped core verification
- [x] T009 [P] Run scoped E2E verification
- [x] T010 [P] Run scoped style checks
- [x] T011 [P] Reconcile final implementation notes/status in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/017-shardingsphere-mcp-sequence-dialect-matrix/spec.md`
