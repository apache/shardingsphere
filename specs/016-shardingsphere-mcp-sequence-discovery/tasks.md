# Tasks: ShardingSphere MCP Sequence Discovery

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/016-shardingsphere-mcp-sequence-discovery/`  
**Prerequisites**: plan.md (required), spec.md (required)

**Tests**: Add and update dedicated core/bootstrap/E2E tests for sequence capability, resource discovery, and unified metadata search.

**Organization**: Tasks are grouped by sequence delivery slice so capability, metadata loading, discovery surface, and docs can be reviewed independently.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`)
- Every task includes an exact file path
- No unrelated cleanup tasks may be added

## Phase 1: Setup (Spec Freeze)

**Purpose**: Freeze the sequence scope before code changes.

- [X] T001 Add sequence scope, constraints, and acceptance criteria to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/016-shardingsphere-mcp-sequence-discovery/spec.md`
- [X] T002 [P] Freeze implementation and verification strategy in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/016-shardingsphere-mcp-sequence-discovery/plan.md`
- [X] T003 [P] Freeze executable tasks in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/016-shardingsphere-mcp-sequence-discovery/tasks.md`

---

## Phase 2: User Story 1 - 通过 resource 发现 sequence (Priority: P1)

**Goal**: Expose sequence list/detail through resources and real database capabilities.

**Independent Test**: resource handler/controller tests and production HTTP metadata integration verify sequence resources and capability.

### Tests for User Story 1

- [X] T004 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/ResourceTestDataFactory.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerTest.java`
  to cover sequence-capable resource fixtures and sequence list/detail handlers
- [X] T005 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/handler/ResourceHandlerRegistryTest.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/resource/MCPResourceControllerTest.java`
  to cover new sequence URIs and unsupported sequence resources
- [X] T006 [P] [US1] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/H2RuntimeTestSupport.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/ProductionMetadataDiscoveryIntegrationTest.java`
  to verify H2 runtime exposes sequence resources and `SEQUENCE` capability

### Implementation for User Story 1

- [X] T007 [US1] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/MCPSequenceMetadata.java`
  and extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/model/MCPSchemaMetadata.java`
  to carry sequence metadata
- [X] T008 [US1] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/DatabaseCapabilityOption.java`,
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/MCPDatabaseCapabilityProvider.java`,
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/capability/database/dialect/H2DatabaseCapabilityOption.java`
  so H2 declares `SEQUENCE`
- [X] T009 [US1] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/jdbc/MCPJdbcMetadataLoader.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/metadata/query/MetadataQueryService.java`
  to load/query H2 sequences and reject unsupported sequence resources
- [X] T010 [P] [US1] Add `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/SequencesHandler.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/resource/handler/metadata/SequenceHandler.java`,
  and register them in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/resources/META-INF/services/org.apache.shardingsphere.mcp.resource.handler.ResourceHandler`

---

## Phase 3: User Story 2 - 通过 `search_metadata` 搜索 sequence (Priority: P1)

**Goal**: Make `sequence` part of the public unified metadata search surface.

**Independent Test**: tool controller, search service, production HTTP, and E2E tests verify `order_seq` can be searched.

### Tests for User Story 2

- [X] T011 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolServiceTest.java`
  to cover `sequence` searches and default search-surface inclusion
- [X] T012 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/tool/MCPToolControllerTest.java`
  to cover `sequence` object-type acceptance and end-to-end metadata search payloads
- [X] T013 [P] [US2] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/test/java/org/apache/shardingsphere/mcp/jdbc/H2RuntimeTestSupport.java`,
  `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/ProductionMetadataDiscoveryIntegrationTest.java`,
  and `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/MetadataDiscoveryE2ETest.java`
  to verify `order_seq` through HTTP and E2E discovery flows

### Implementation for User Story 2

- [X] T014 [US2] Extend `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolHandler.java`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/src/main/java/org/apache/shardingsphere/mcp/tool/handler/metadata/SearchMetadataToolService.java`
  to treat `sequence` as a supported public object type

---

## Phase 4: Contract & Docs Alignment

**Purpose**: Keep the public sequence surface truthful.

- [X] T015 [P] Update `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/mcp-domain-contract.md`
  to include sequence resources and `search_metadata.object_types=sequence`
- [X] T016 [P] Update `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md`
  to reflect sequence as the first expanded metadata object
- [X] T017 [P] Update `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
  and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`
  to document sequence resources and sequence search support

---

## Phase 5: Polish & Verification

**Purpose**: Final consistency and scope guard.

- [X] T018 [P] Run scoped core verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/core/pom.xml`
- [X] T019 [P] Run scoped bootstrap verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [X] T020 [P] Run scoped E2E verification in `/Users/zhangliang/IdeaProjects/shardingsphere/test/e2e/mcp/pom.xml`
- [X] T021 [P] Run scoped style checks for touched MCP modules
- [X] T022 [P] Reconcile final implementation notes and status in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/016-shardingsphere-mcp-sequence-discovery/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Completed first to freeze scope.
- **User Story 1 (Phase 2)**: Goes first because resource/capability/model must exist before search can use sequence.
- **User Story 2 (Phase 3)**: Depends on metadata loading and query service support from Phase 2.
- **Contract & Docs (Phase 4)**: Can start once public surface is settled, before final verification.
- **Polish (Phase 5)**: Runs after selected implementation slices are complete.

### Parallel Opportunities

- `T004`, `T005`, and `T006` can run in parallel with `T007`/`T008` once the sequence model shape is fixed.
- `T011`, `T012`, and `T013` can run in parallel with `T014`.
- `T015`, `T016`, and `T017` can run in parallel as doc alignment.

## Notes

- 这轮的评审重点是“sequence 是否真实进入统一 discovery surface”，不是“是否已经做完所有扩展对象”。
- 任何 auth、remote 放开、或多对象泛化框架的想法，都必须拆到后续 follow-up。
