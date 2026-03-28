# Tasks: ShardingSphere MCP HTTP SDK Reuse Without Capability Loss

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/006-shardingsphere-mcp-http-sdk-reuse/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Include explicit zero-loss regressions for initialize protocol header, follow-up protocol fallback,
loopback `Origin`, lowercase headers, missing/blank `Accept`, `DELETE` cleanup, and HTTP runtime classpath-driver behavior.

**Organization**: Tasks are grouped by user story so SDK-native transport reuse, contract preservation,
and runtime compatibility can be landed in reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Design Alignment)

**Purpose**: Make the SDK-reuse boundary explicit before changing implementation.

- [x] T001 Add HTTP SDK reuse notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [x] T002 [P] Add HTTP SDK reuse and zero-loss notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Freeze the zero-loss regression matrix and prepare the SDK security hook seam.

**CRITICAL**: No story work should be considered complete until this phase is done.

- [x] T003 Extend loopback-origin and protocol validation coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidatorTest.java`
- [x] T004 [P] Add missing/blank `Accept` compatibility coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`
- [x] T005 [P] Add shutdown cleanup coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`
- [x] T006 [P] Add HTTP runtime classpath-driver regression coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/ProductionMetadataDiscoveryIntegrationTest.java`

**Checkpoint**: The no-loss matrix is explicit before implementation refactoring starts.

---

## Phase 3: User Story 1 - 能交给官方 SDK 的 HTTP transport 逻辑尽量交回 SDK (Priority: P1) MVP

**Goal**: 让 SDK 承担原生 transport mechanics，把 loopback `Origin` 下沉到官方 security hook。

**Independent Test**: initialize / follow-up / GET / DELETE 继续通过，且 loopback `Origin`
边界改由 SDK `securityValidator` 或等价官方 hook 承接。

### Tests for User Story 1

- [x] T007 [P] [US1] Add SDK-security-hook focused integration coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`

### Implementation for User Story 1

- [x] T008 [P] [US1] Introduce SDK-oriented loopback origin validator in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java`
- [x] T009 [P] [US1] Slim request validator to session/protocol and compatibility responsibilities in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidator.java`
- [x] T010 [US1] Refactor servlet to prefer SDK-native transport mechanics and drop redundant empty context configuration in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java`

**Checkpoint**: SDK-native transport behavior is the default path, and local-mode security is anchored on the official hook.

---

## Phase 4: User Story 2 - 对外 HTTP contract 与现有兼容行为零损失 (Priority: P1)

**Goal**: 在 SDK 复用后保持 initialize header、follow-up protocol fallback、lowercase headers、
missing/blank `Accept` 和 `DELETE` 相关行为不退化。

**Independent Test**: 当前 HTTP integration smoke 和新增 compatibility regressions 全部通过。

### Tests for User Story 2

- [x] T011 [P] [US2] Add initialize-header and protocol-fallback coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`
- [x] T012 [P] [US2] Add lowercase-header and post-delete regression coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`

### Implementation for User Story 2

- [x] T013 [P] [US2] Preserve initialize `MCP-Protocol-Version` response header behavior in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java`
- [x] T014 [P] [US2] Preserve follow-up protocol fallback and session validation contract in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPRequestValidator.java`
- [x] T015 [US2] Preserve missing/blank `Accept` compatibility with the thinnest possible shim in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java`

**Checkpoint**: Public HTTP contract and existing compatibility behaviors remain intact after SDK reuse.

---

## Phase 5: User Story 3 - ShardingSphere runtime lifecycle 与 driver/classloader 兼容性不退化 (Priority: P2)

**Goal**: 保留 managed session cleanup，并只在验证通过后才调整 classloader bridge。

**Independent Test**: `DELETE` / shutdown cleanup 继续生效，HTTP runtime 在 classpath-driver
场景下不退化。

### Tests for User Story 3

- [x] T016 [P] [US3] Add managed-session cleanup verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpRuntimeIntegrationTest.java`
- [x] T017 [P] [US3] Add classpath-driver HTTP execute-query regression in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/ProductionExecuteQueryIntegrationTest.java`

### Implementation for User Story 3

- [x] T018 [P] [US3] Keep managed session lifecycle bridge aligned with SDK transport cleanup in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/ManagedSessionRegistry.java`
- [x] T019 [P] [US3] Keep session resource closer aligned with HTTP transport shutdown semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/MCPSessionCloser.java`
- [x] T020 [US3] Remove or retain `serviceWithApplicationClassLoader` based on classpath-driver regression evidence in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/http/StreamableHttpMCPServlet.java`

**Checkpoint**: The refactor preserves ShardingSphere resource cleanup and runtime compatibility.

---

## Phase 6: Polish & Verification

**Purpose**: Close final quality and consistency gaps.

- [x] T021 [P] Run bootstrap HTTP transport verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [x] T022 [P] Reconcile final HTTP transport wording in `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all story work.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and recommended MVP because it delivers the main SDK reuse seam.
- **User Story 2 (P1)**: Depends on US1 seam changes being stable enough to preserve existing contract and compatibility behavior.
- **User Story 3 (P2)**: Depends on US1/US2 transport boundary stabilization so runtime compatibility can be judged accurately.

### Parallel Opportunities

- `T003`, `T004`, `T005`, and `T006` can run in parallel during the regression-freeze phase.
- `T008`, `T009`, and `T010` can run in parallel after the foundational regressions are in place.
- `T011` and `T012` can run in parallel for US2 coverage.
- `T016` and `T017` can run in parallel for US3 regressions.
- `T018` and `T019` can run in parallel because they touch disjoint lifecycle helpers.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 to move loopback `Origin` onto the SDK hook and slim duplicated transport logic.
3. Verify no public contract drift before touching compatibility shims.

### Incremental Delivery

1. Freeze the zero-loss regression matrix.
2. Move `Origin` validation onto SDK `securityValidator`.
3. Refactor servlet and validator to keep only ShardingSphere-specific glue.
4. Preserve or re-home compatibility shims for `Accept` and classloader only when needed.
5. Re-run HTTP transport regressions and scoped style checks.

## Notes

- This task list is about HTTP transport seam alignment, not MCP protocol redesign.
- If any current behavior cannot be cleanly expressed with the SDK hook surface, keep the smallest possible compatibility layer.
- `T020` is intentionally evidence-driven: classloader glue should only be deleted if the new regressions prove there is no capability loss.
