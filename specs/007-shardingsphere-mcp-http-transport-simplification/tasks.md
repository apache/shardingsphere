# Tasks: ShardingSphere MCP HTTP Transport Simplification After SDK Reuse

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Reuse the `006` HTTP zero-loss regression matrix for initialize protocol header, follow-up protocol fallback,
missing/blank `Accept`, loopback `Origin`, `DELETE` cleanup, shutdown cleanup, and classpath-driver behavior.

**Organization**: Tasks are grouped by user story so structural simplification, SDK-boundary clarity,
and zero-loss verification remain reviewable.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Structure Inventory)

**Purpose**: Freeze the simplification target before editing code.

- [X] T001 Add transport simplification scope and keep/inline principles to `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/spec.md`
- [X] T002 [P] Record SDK-native vs SDK-hook vs ShardingSphere-owned decisions in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/research.md`

---

## Phase 2: Foundational (Zero-Loss Guardrail)

**Purpose**: Reuse the `006` regression matrix as the mandatory safety net for simplification work.

**CRITICAL**: No structure simplification is complete without this regression path.

- [X] T003 Capture the simplification verification plan in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/plan.md`
- [X] T004 [P] Reconfirm the HTTP zero-loss regression suite references in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/tasks.md`

**Checkpoint**: The simplification effort is tied to the existing no-loss verification matrix.

---

## Phase 3: User Story 1 - HTTP transport 的总复杂度继续下降 (Priority: P1)

**Goal**: Remove one-off helper files that do not carry a stable concept boundary.

**Independent Test**: Production behavior remains unchanged while the number of single-use helper files drops.

### Tests for User Story 1

- [X] T005 [P] [US1] Reuse HTTP regressions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`

### Implementation for User Story 1

- [X] T006 [US1] Merge or inline one-off request/response/protocol helper logic in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java`
- [X] T007 [P] [US1] Remove or repurpose `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/DefaultAcceptHeaderRequestWrapper.java`
- [X] T008 [P] [US1] Remove or repurpose `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/InitializeProtocolHeaderResponseWrapper.java`
- [X] T009 [P] [US1] Remove or repurpose `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpInitializeRequestUtils.java`

**Checkpoint**: The servlet/helper cluster is simpler overall, not just split differently.

---

## Phase 4: User Story 2 - SDK 与本地 glue 的边界进一步清晰 (Priority: P1)

**Goal**: Keep only meaningful standalone seams and document why each remaining local class exists.

**Independent Test**: Every remaining production type in the HTTP transport package can be justified as SDK core wiring,
standalone local policy, or ShardingSphere-owned glue.

### Tests for User Story 2

- [X] T010 [P] [US2] Reuse validator and lifecycle regressions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidatorTest.java`

### Implementation for User Story 2

- [X] T011 [US2] Keep only justified standalone local policy types in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/LoopbackOriginSecurityValidator.java`
- [X] T012 [P] [US2] Record that missing/blank `Accept` still requires a local shim under current SDK in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/research.md`
- [X] T013 [P] [US2] Reconcile servlet ownership comments and local glue boundaries in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [X] T014 [P] [US2] Reconcile detailed HTTP keep/inline rules in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

**Checkpoint**: Remaining standalone classes have explicit seam value; everything else is merged to the simplest safe place.

---

## Phase 5: User Story 3 - 简化后继续零损失 (Priority: P1)

**Goal**: Prove that structural simplification did not change runtime behavior.

**Independent Test**: The `006` HTTP no-loss matrix still passes after helper consolidation.

### Tests for User Story 3

- [X] T015 [P] [US3] Verify HTTP runtime regressions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`
- [X] T016 [P] [US3] Verify classpath-driver regressions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/ProductionMetadataDiscoveryIntegrationTest.java`
- [X] T017 [P] [US3] Verify execute-query and shutdown regressions in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/ProductionExecuteQueryIntegrationTest.java`

### Implementation for User Story 3

- [X] T018 [US3] Ensure protocol/session contract remains unchanged in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidator.java`
- [X] T019 [P] [US3] Ensure managed-session cleanup remains unchanged in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/ManagedSessionRegistry.java`
- [X] T020 [P] [US3] Ensure session close semantics remain unchanged in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPSessionCloser.java`

**Checkpoint**: Structure is simpler and the old behavior is still intact.

---

## Phase 6: Polish & Verification

**Purpose**: Final consistency and quality closure.

- [X] T021 [P] Run scoped HTTP simplification verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [X] T022 [P] Align final simplification notes in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/007-shardingsphere-mcp-http-transport-simplification/spec.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup and blocks all code changes.
- **User Stories (Phase 3-5)**: Depend on the zero-loss guardrail from Phase 2.
- **Polish (Phase 6)**: Runs after the selected simplification slice is complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice because it reduces total structural complexity.
- **User Story 2 (P1)**: Depends on US1 simplification direction being clear enough to justify remaining standalone seams.
- **User Story 3 (P1)**: Depends on US1/US2 so that the final behavior check matches the intended simplified structure.

### Parallel Opportunities

- `T001` and `T002` can run in parallel while shaping the follow-up.
- `T007`, `T008`, and `T009` can run in parallel if the final simplification direction is fixed.
- `T012`, `T013`, and `T014` can run in parallel as documentation alignment work.
- `T015`, `T016`, and `T017` can run in parallel as verification slices.
- `T019` and `T020` can run in parallel if implementation touches those lifecycle helpers.

## Implementation Strategy

### MVP First

1. Freeze the keep / inline / retain matrix.
2. Collapse one-off helpers back to the simplest safe location.
3. Keep only meaningful standalone seams.
4. Re-run the `006` no-loss verification matrix.

### Incremental Delivery

1. Confirm what can still be internalized to SDK and what cannot.
2. Remove helper fragmentation that does not reduce total complexity.
3. Preserve all runtime and protocol glue that still belongs to ShardingSphere.
4. Update docs so future refactors start from the clarified boundary.

## Notes

- This task list is a post-`006` simplification follow-up, not a second protocol redesign.
- The optimization target is total readability, not “fewest lines in servlet”.
- If a helper cannot justify its own file via reuse or seam value, it should default to merge.
