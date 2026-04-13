# Tasks: ShardingSphere MCP Remote HTTP Access Token

**Input**: Design documents from `/Users/zhangliang/IdeaProjects/shardingsphere/specs/020-shardingsphere-mcp-remote-http-access-token/`  
**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/

**Tests**: Include explicit bootstrap configuration, validator, and Streamable HTTP integration tests because this feature is a transport-boundary hardening change.

**Organization**: Tasks are grouped by user story so remote startup hardening, request admission gating, and local-mode compatibility can land in controlled slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story owns the task (`US1`, `US2`, `US3`)
- Every task includes an exact file path

## Phase 1: Setup (Product Contract Framing)

**Purpose**: 先把 remote HTTP 最小 admission gate 的产品边界说清楚，再进入实现。

- [ ] T001 Add remote HTTP access-token notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-PRD.md`
- [ ] T002 [P] Add transport-boundary hardening notes to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Technical-Design.md`
- [ ] T003 [P] Add detailed access-token wording to `/Users/zhangliang/IdeaProjects/shardingsphere/docs/mcp/ShardingSphere-MCP-Detailed-Design.md`
- [ ] T004 [P] Reconcile top-level MCP runtime boundary wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/spec.md`
- [ ] T005 [P] Reconcile Streamable HTTP contract wording in `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/streamable-http-contract.md`
- [ ] T006 [P] Reconcile deployment guidance in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README.md`
- [ ] T007 [P] Reconcile deployment guidance in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md`

---

## Phase 2: Foundational (Configuration and Admission Surface)

**Purpose**: 先把配置模型和 transport admission boundary 补齐，这会阻塞所有后续请求级行为。

**CRITICAL**: No user story work should be considered complete until this phase is done.

- [ ] T008 Extend HTTP transport config object in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/HttpTransportConfiguration.java`
- [ ] T009 [P] Extend YAML HTTP transport config in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/YamlHttpTransportConfiguration.java`
- [ ] T010 [P] Add `accessToken` swap and remote validation in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`
- [ ] T011 [P] Add config coverage for `accessToken` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java`
- [ ] T012 [P] Add YAML swapper coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapperTest.java`
- [ ] T013 [P] Add launch config coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlMCPTransportConfigurationSwapperTest.java`

**Checkpoint**: remote HTTP cannot start without an explicit shared token.

---

## Phase 3: User Story 1 - remote 场景必须显式配置共享访问密钥 (Priority: P1)

**Goal**: 把 “远程暴露意图” 升级成 “远程暴露且具备最小 admission gate”。

**Independent Test**: 非 loopback 场景缺失 token 启动失败，存在 token 启动成功。

### Tests for User Story 1

- [ ] T014 [P] [US1] Add blank-token startup rejection coverage to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java`
- [ ] T015 [P] [US1] Add loopback-without-token compatibility coverage to `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/config/loader/MCPConfigurationLoaderTest.java`

### Implementation for User Story 1

- [ ] T016 [US1] Finalize remote token-required validation in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/YamlHttpTransportConfigurationSwapper.java`

**Checkpoint**: anonymous remote startup is no longer allowed.

---

## Phase 4: User Story 2 - HTTP 请求必须带 Bearer Token 才能接入 runtime (Priority: P1)

**Goal**: 让 initialize 和后续 session 请求都经过统一的 admission gate。

**Independent Test**: initialize、tools/call、resources/read、DELETE
在无 token、错误 token、正确 token 下表现不同。

### Tests for User Story 2

- [ ] T017 [P] [US2] Add token-validator unit coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/AccessTokenSecurityValidatorTest.java`
- [ ] T018 [P] [US2] Add authentication-order coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java`
- [ ] T019 [P] [US2] Add initialize admission coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java`
- [ ] T020 [P] [US2] Add metadata-read admission coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMetadataDiscoveryIT.java`
- [ ] T021 [P] [US2] Add execute-query admission coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpExecuteQueryIT.java`

### Implementation for User Story 2

- [ ] T022 [US2] Add `AccessTokenSecurityValidator` in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/AccessTokenSecurityValidator.java`
- [ ] T023 [US2] Compose token and origin validation in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
- [ ] T024 [US2] Keep session/protocol validation focused in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidator.java`

**Checkpoint**: configured HTTP runtime no longer accepts anonymous requests.

---

## Phase 5: User Story 3 - loopback 本地调试保持低摩擦 (Priority: P2)

**Goal**: remote hardening 不破坏 local-first 开发体验。

**Independent Test**: loopback without token still works, loopback `Origin` guard still holds.

### Tests for User Story 3

- [ ] T025 [P] [US3] Preserve loopback origin coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/LoopbackOriginSecurityValidatorTest.java`
- [ ] T026 [P] [US3] Add loopback-with-configured-token coverage in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java`

### Implementation for User Story 3

- [ ] T027 [US3] Keep loopback local-mode behavior compatible in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
- [ ] T028 [US3] Preserve loopback `Origin` validator semantics in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/LoopbackOriginSecurityValidator.java`

**Checkpoint**: local debugging remains usable, while explicit local hardening stays possible.

---

## Phase 6: Polish & Verification

**Purpose**: 收口测试、文档和风格检查。

- [ ] T029 [P] Run bootstrap config verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T030 [P] Run validator verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T031 [P] Run HTTP integration verification in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T032 [P] Run scoped style checks in `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/bootstrap/pom.xml`
- [ ] T033 [P] Reconcile final wording across `/Users/zhangliang/IdeaProjects/shardingsphere/mcp/README_ZH.md` and `/Users/zhangliang/IdeaProjects/shardingsphere/specs/001-shardingsphere-mcp/contracts/streamable-http-contract.md`

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Starts immediately.
- **Foundational (Phase 2)**: Depends on Setup completion and blocks all user stories.
- **User Stories (Phase 3-5)**: Depend on Foundational completion.
- **Polish (Phase 6)**: Depends on the selected stories being complete.

### User Story Dependencies

- **User Story 1 (P1)**: First slice and required MVP because startup hardening closes the most visible anonymous-remote gap.
- **User Story 2 (P1)**: Depends on US1 config model being stable enough to enforce request admission.
- **User Story 3 (P2)**: Depends on US2 admission order being stable enough to verify local-mode compatibility.

### Parallel Opportunities

- `T002` to `T007` can run in parallel after `T001`.
- `T009` to `T013` can run in parallel after `T008`.
- `T017` to `T021` can run in parallel for US2.
- `T029` to `T033` can run in parallel during final verification.

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 and Phase 2.
2. Complete Phase 3 so remote startup cannot proceed without token.
3. Validate that remote exposure now requires an explicit shared secret.

### Incremental Delivery

1. 收紧 remote startup 配置边界。
2. 对整个 HTTP surface 加 admission gate。
3. 回归并保留 loopback 本地体验。
4. 对齐 README 与 top-level contract。

## Notes

- 这份任务列表刻意避免把本轮扩展成完整 auth platform。
- 最重要的成功信号不是“支持了更多认证方式”，
  而是 remote HTTP 不再是 anonymous entrypoint。
- 如果实现阶段想改成 Basic Auth、login issuance 或 auth SPI，
  必须重新开 feature，不应在本轮隐式扩 scope。
