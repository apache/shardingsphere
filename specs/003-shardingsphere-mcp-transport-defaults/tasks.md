# Tasks: ShardingSphere MCP Transport Default Realignment

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/003-shardingsphere-mcp-transport-defaults/`
**Prerequisites**: plan.md (required), spec.md (required for user stories)

**Tests**: Include explicit transport launch-matrix tests because the change is primarily about default behavior and configuration correctness.

**Organization**: Tasks are grouped by user story so the default-profile change can land before the documentation sweep.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Shared Documentation and Baseline)

**Purpose**: Make the transport-default change explicit before touching implementation.

- [ ] T001 Add transport-default rollout notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T002 [P] Add default-profile correction notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Flip the packaged default safely and expose effective runtime state.

**CRITICAL**: No story work should be considered complete until these foundational tasks are done.

- [ ] T003 Update packaged transport defaults to `stdio=true` and `http=false` in `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp.yaml`
- [ ] T004 [P] Emit effective transport-state diagnostics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncher.java`
- [ ] T005 [P] Extend transport-switch parsing tests in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/McpConfigurationLoaderTest.java`

**Checkpoint**: Default configuration and launcher diagnostics are aligned.

---

## Phase 3: User Story 1 - 默认启动走 STDIO (Priority: P1) MVP

**Goal**: 让默认发行配置成为 `stdio only`，并证明 HTTP 不会在默认 profile 下被启动。

**Independent Test**: 使用默认 `mcp.yaml` 完成 STDIO smoke，并确认未创建 HTTP listener。

### Tests for User Story 1

- [ ] T006 [P] [US1] Add default-stdio launch coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StdioTransportIntegrationTest.java`
- [ ] T007 [P] [US1] Add launcher matrix test for `stdio only` and `both false` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncherTest.java`

### Implementation for User Story 1

- [ ] T008 [US1] Keep `both false` fail-fast and default `stdio only` behavior explicit in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncher.java`

**Checkpoint**: Default packaged runtime is stdio-first and fail-fast still holds.

---

## Phase 4: User Story 2 - HTTP 保持可显式启用 (Priority: P2)

**Goal**: 默认值切换后，HTTP opt-in 与双开模式继续可用。

**Independent Test**: 显式启用 HTTP 后通过 `/mcp` 完成 initialize、follow-up 与 close-session。

### Tests for User Story 2

- [ ] T009 [P] [US2] Add launch coverage for `http only` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/server/StreamableHttpRuntimeIntegrationTest.java`
- [ ] T010 [P] [US2] Add launcher matrix coverage for `dual enabled` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncherTest.java`

### Implementation for User Story 2

- [ ] T011 [US2] Preserve simultaneous STDIO and HTTP launch wiring in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/lifecycle/McpRuntimeLauncher.java`

**Checkpoint**: HTTP opt-in and dual-transport launch remain stable after the default flip.

---

## Phase 5: User Story 3 - 双协议都可用且文档一致 (Priority: P3)

**Goal**: 收敛 README、quickstart 与设计文档，让默认值与推荐使用方式一致。

**Independent Test**: 审阅者仅按文档即可完成 `stdio only` 默认启动与 `http only` 显式启动。

### Documentation for User Story 3

- [ ] T012 [P] [US3] Update operator and quickstart docs in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
- [ ] T013 [P] [US3] Update Chinese operator and quickstart docs in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`
- [ ] T014 [P] [US3] Update transport quickstart defaults in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/quickstart.md`

**Checkpoint**: Packaged default, opt-in HTTP path, and dual-enable path are all documented consistently.

---

## Phase 6: Polish & Verification

**Purpose**: Close quality and consistency gaps.

- [ ] T015 [P] Run bootstrap scoped verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T016 [P] Run distribution config verification against `/Users/zhangliang/IdeaProjects/shardingsphere/distribution/mcp/src/main/resources/conf/mcp.yaml`
- [ ] T017 [P] Reconcile final wording across `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md` and `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 and blocks all story work.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and recommended MVP because it delivers the new default profile.
- **User Story 2 (P2)**: Depends on the default flip but is otherwise parallel to docs after tests are in place.
- **User Story 3 (P3)**: Should land after US1/US2 so docs reflect the real supported matrix.

### Parallel Opportunities

- `T004` and `T005` can run in parallel after `T003`.
- `T006` and `T007` can run in parallel for US1.
- `T009` and `T010` can run in parallel for US2.
- `T012`, `T013`, and `T014` can run in parallel for documentation.
