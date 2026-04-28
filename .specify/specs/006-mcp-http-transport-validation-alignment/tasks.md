# Tasks: MCP HTTP Transport Validation Alignment

**Input**: Design documents from `/.specify/specs/006-mcp-http-transport-validation-alignment/`
**Prerequisites**: `spec.md`, `research.md`
**Tests**: Add or update validator unit tests, servlet/server transport tests, and focused HTTP transport integration coverage for MCP-native validation ownership.

**Organization**: Tasks are grouped by user story so MCP-native validator wiring, local constraint adaptation, MCP-owned request/session failures, and successful-flow preservation can be implemented and verified in reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Design Gates

以下 4 个问题必须在实现中保持一致，不能半途回退到 mixed-mode：

- 只有一个 MCP-facing `ServerTransportSecurityValidator`，不能再保留 servlet 手工 `validateHeaders(...)`
- `access token`、`origin`、`protocol version` 都要降为本地 header constraint，而不是继续让叶子类直接实现 MCP 接口
- `session` 缺失/不存在、`Accept` 校验、JSON-RPC 解析等错误要交还 MCP，而不是再维护一条 ShardingSphere request validator
- 旧的 `{"message": ...}` 自定义错误体契约不再保留，测试应改为围绕 status code 和 ownership 边界断言

## Code Cut Points

- **Primary production files**
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServer.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidator.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/AccessTokenSecurityValidator.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/LoopbackOriginSecurityValidator.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/CompositeServerTransportSecurityValidator.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/HttpTransportSecurityHeaderUtils.java`
- **Expected new production files**
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/TransportHeaderConstraint.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/TransportHeaderConstraintException.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/AccessTokenHeaderConstraint.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/LoopbackOriginHeaderConstraint.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ProtocolVersionHeaderConstraint.java`
  - `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactory.java`
- **Primary test files**
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidatorTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServerTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/AccessTokenSecurityValidatorTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/LoopbackOriginSecurityValidatorTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/CompositeServerTransportSecurityValidatorTest.java`
- **Expected new test files**
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/AccessTokenHeaderConstraintTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/LoopbackOriginHeaderConstraintTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ProtocolVersionHeaderConstraintTest.java`
  - `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactoryTest.java`

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Create the local validation abstraction files `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/TransportHeaderConstraint.java` and `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/TransportHeaderConstraintException.java`.
- [ ] T002 Create `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ServerTransportSecurityValidatorFactory.java` to own composite validator assembly from bind host, access token, and session manager inputs.
- [ ] T003 [P] Rename or replace the old leaf-validator test targets by creating `AccessTokenHeaderConstraintTest.java`, `LoopbackOriginHeaderConstraintTest.java`, `ProtocolVersionHeaderConstraintTest.java`, and `ServerTransportSecurityValidatorFactoryTest.java` under `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/`.

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Refactor `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/CompositeServerTransportSecurityValidator.java` so it consumes local `TransportHeaderConstraint` instances and maps `TransportHeaderConstraintException` to `ServerTransportSecurityException`.
- [ ] T005 [P] Replace `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/AccessTokenSecurityValidator.java` with `AccessTokenHeaderConstraint.java`, preserving bearer-token semantics while removing direct `ServerTransportSecurityValidator` coupling.
- [ ] T006 [P] Replace `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/LoopbackOriginSecurityValidator.java` with `LoopbackOriginHeaderConstraint.java`, preserving loopback-origin semantics while removing direct `ServerTransportSecurityValidator` coupling.
- [ ] T007 [P] Implement `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ProtocolVersionHeaderConstraint.java` so it validates `MCP-Protocol-Version` only for follow-up requests that reference an existing session through `MCPSessionManager`.
- [ ] T008 Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/CompositeServerTransportSecurityValidatorTest.java` and add `ServerTransportSecurityValidatorFactoryTest.java` to cover local-rule ordering, short-circuiting, exception mapping, and deterministic factory assembly order.

**Checkpoint**: One local constraint model exists, the MCP adapter boundary is explicit, and protocol-version logic is available as a local header constraint.

---

## Phase 3: User Story 1 - Use MCP's native validation hook as the only header-validation entry point (Priority: P1)

**Goal**: Remove the servlet-side manual header-validation path and let MCP invoke the registered composite validator automatically.
**Independent Test**: Build the servlet/server wiring and verify the MCP provider receives a real validator while no servlet helper still calls `validateHeaders(...)` manually.

### Tests for User Story 1

- [ ] T009 [P] [US1] Rewrite `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServletTest.java` so it verifies MCP-native validator registration or resulting servlet behavior without asserting a manual ShardingSphere JSON error body.
- [ ] T010 [P] [US1] Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServerTest.java` if needed so server startup expectations reflect factory-based validator wiring rather than the old manual pre-validation path.

### Implementation for User Story 1

- [ ] T011 [US1] Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServlet.java` to register the composite validator from `ServerTransportSecurityValidatorFactory` with `HttpServletStreamableServerTransportProvider.builder()` instead of `ServerTransportSecurityValidator.NOOP`.
- [ ] T012 [US1] Remove manual header-validation code from `StreamableHttpMCPServlet.java`, including the validator field used only for pre-validation, servlet-side header extraction for validation, and any helper methods dedicated only to the previous manual path.
- [ ] T013 [US1] Keep only transport-adaptation behaviors in `StreamableHttpMCPServlet.java`, including protocol normalization, Accept-header supplementation, negotiated protocol-response header decoration, and session-close coordination.

**Checkpoint**: Header validation enters through MCP's registered validator hook only, and the servlet no longer maintains a parallel pre-validation chain.

---

## Phase 4: User Story 2 - Adapt ShardingSphere-specific header constraints without coupling leaf constraints to MCP interfaces (Priority: P1)

**Goal**: Move token, origin, and protocol-version checks into local reusable header constraints behind one MCP-facing adapter.
**Independent Test**: Run dedicated constraint tests and composite-adapter tests to verify each constraint in isolation and the adapter as the only MCP-facing boundary.

### Tests for User Story 2

- [ ] T014 [P] [US2] Port the old `AccessTokenSecurityValidatorTest.java` scenarios into `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/AccessTokenHeaderConstraintTest.java`.
- [ ] T015 [P] [US2] Port the old `LoopbackOriginSecurityValidatorTest.java` scenarios into `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/LoopbackOriginHeaderConstraintTest.java`.
- [ ] T016 [P] [US2] Add `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/ProtocolVersionHeaderConstraintTest.java` to cover missing protocol header, mismatch, no-session-id bypass, and unknown-session bypass.

### Implementation for User Story 2

- [ ] T017 [US2] Rename or replace production files and imports so `StreamableHttpMCPServlet.java`, `CompositeServerTransportSecurityValidator.java`, and the new factory all use `AccessTokenHeaderConstraint`, `LoopbackOriginHeaderConstraint`, and `ProtocolVersionHeaderConstraint`.
- [ ] T018 [US2] Update `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/validator/HttpTransportSecurityHeaderUtils.java` only as needed to support the new local constraint API without reintroducing servlet-side validation helpers.
- [ ] T019 [US2] Delete the obsolete leaf validator classes and their tests once the new rule names, imports, and coverage are in place.

**Checkpoint**: ShardingSphere-specific header validation is expressed entirely as local constraints, and MCP coupling exists only in the composite adapter.

---

## Phase 5: User Story 3 - Keep MCP responsible for request/session lifecycle errors (Priority: P1)

**Goal**: Remove the parallel ShardingSphere request/session validator and let MCP own missing-session, malformed-request, and related lifecycle failures.
**Independent Test**: Trigger missing session IDs, unknown sessions, malformed requests, and bad Accept headers through HTTP transport tests and verify MCP-native ownership of those failures.

### Tests for User Story 3

- [ ] T020 [P] [US3] Delete or replace `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidatorTest.java` with focused coverage that now belongs either in `ProtocolVersionHeaderConstraintTest.java` or `StreamableHttpTransportIT.java`.
- [ ] T021 [P] [US3] Rewrite the failure assertions in `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java` so token/origin/protocol failures assert status codes and MCP-native ownership, while missing-session and malformed-request branches assert MCP-owned outcomes instead of the old JSON message envelope.

### Implementation for User Story 3

- [ ] T022 [US3] Delete `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPRequestValidator.java` and remove all references from `StreamableHttpMCPServlet.java`.
- [ ] T023 [US3] Simplify `StreamableHttpMCPServlet.java` request flow so GET/POST/DELETE delegate directly into MCP after servlet adaptation wrappers, without ShardingSphere-side session ID, session existence, or protocol-version pre-validation branches.
- [ ] T024 [US3] Remove servlet-side custom error-writing helpers that existed only for the previous pre-validation path, while preserving any helper still required for successful protocol adaptation.

**Checkpoint**: Request/session lifecycle errors are owned by MCP, not by a parallel ShardingSphere validator chain.

---

## Phase 6: User Story 4 - Preserve successful MCP HTTP workflows while accepting MCP-native error rendering (Priority: P2)

**Goal**: Keep successful initialize/follow-up/stream/delete behavior intact while adopting MCP-native error rendering for header-constraint failures.
**Independent Test**: Run focused HTTP transport integration scenarios to confirm successful workflows still pass and failure-path assertions now track status/ownership rather than the old error body.

### Tests for User Story 4

- [ ] T025 [P] [US4] Update `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpTransportIT.java` to keep successful initialize, authorized follow-up, stream opening, and delete assertions, while adding or adjusting protocol-mismatch coverage under the new header-constraint path.
- [ ] T026 [P] [US4] Review `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/StreamableHttpMCPServerTest.java` and any transport startup assertions so they no longer assume the removed ShardingSphere JSON error contract.

### Implementation for User Story 4

- [ ] T027 [US4] Verify and, if necessary, adjust `normalizeInitializeRequest(...)`, `withDefaultAcceptHeader(...)`, and `withInitializeProtocolHeader(...)` in `StreamableHttpMCPServlet.java` so they continue to support successful MCP interoperability after the validator migration.
- [ ] T028 [US4] Update any review-facing comments or Javadocs in the touched transport classes so they describe the new MCP-native ownership boundary instead of the old servlet-side pre-validation model.

**Checkpoint**: Successful transport workflows remain usable, and the test suite reflects the accepted shift to MCP-native error rendering.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T029 [P] Run `./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest=AccessTokenHeaderConstraintTest,LoopbackOriginHeaderConstraintTest,ProtocolVersionHeaderConstraintTest,ServerTransportSecurityValidatorFactoryTest,CompositeServerTransportSecurityValidatorTest,StreamableHttpMCPServletTest,StreamableHttpMCPServerTest,StreamableHttpTransportIT -Dsurefire.failIfNoSpecifiedTests=false test` and fix any failures.
- [ ] T030 [P] Run `./mvnw -pl mcp/bootstrap -Pcheck -DskipITs -DskipTests checkstyle:check spotless:check` after the refactor and resolve all style issues in touched files.
- [ ] T031 Remove dead files, obsolete imports, stale comments, and any transitional naming left from `*SecurityValidator` or `StreamableHttpMCPRequestValidator` once the new architecture is in place.
- [ ] T032 Verify with `rg` across `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/http/` that no production code still manually calls `validateHeaders(...)` or references `StreamableHttpMCPRequestValidator` after the refactor.

## Dependencies & Execution Order

- Phase 1 should finish before behavior changes so the local constraint model and factory boundary are explicit.
- Phase 2 blocks all user stories because the composite adapter and protocol-version constraint are the shared foundation.
- User Story 1 should land before User Story 3 because MCP-native validator registration is the prerequisite for deleting the manual pre-validation path.
- User Story 2 can proceed in parallel with the later part of User Story 1 once the foundational abstractions are in place.
- User Story 3 depends on User Story 1 and User Story 2 because request/session ownership can only be transferred after the new MCP-facing validator path is working.
- User Story 4 depends on the earlier slices because successful-flow preservation and IT rewriting should reflect the final ownership model.
- Polish runs last after all targeted story phases are complete.

## Implementation Strategy

- MVP first: finish Phase 2 plus User Story 1 so MCP becomes the sole header-validation entry point.
- Increment 2: complete User Story 2 so all ShardingSphere-owned checks live behind local header constraints and one composite adapter.
- Increment 3: complete User Story 3 to remove the parallel request validator and give request/session lifecycle failures back to MCP.
- Increment 4: finish User Story 4 and Phase 7 so success-path interoperability, test expectations, and cleanup all match the final MCP-native design.
