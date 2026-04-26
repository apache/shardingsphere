# Tasks: MCP Runtime Pluginization Completion

**Input**: Design documents from `/.specify/specs/005-mcp-runtime-pluginization/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`
**Tests**: Add or update module-scoped tests for `mcp/bootstrap`, related `mcp/core` registry checks, and `distribution/mcp` packaging verification.

**Organization**: Tasks are grouped by user story so runtime boundary cleanup, official bundle preservation, and extension onboarding can land in reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Analysis Gates

以下 4 个问题必须先在规格层说清楚，再进入实现：

- 真插件化是否包含热插拔，还是仅限启动期 classpath 发现
- 默认 feature 集合到底由 `bootstrap` 负责还是由 `distribution` 负责
- `plugins/` 的职责边界是否只承接用户追加的 driver / feature jar
- bootstrap 测试与 distribution 验证各自要为哪一层责任背书

---

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Inventory all bootstrap-side default feature coupling in `mcp/bootstrap/pom.xml`, `mcp/README.md`, `mcp/README_ZH.md`, and bootstrap tests under `mcp/bootstrap/src/test/java/`.
- [ ] T002 Inventory packaged-runtime responsibilities in `distribution/mcp/pom.xml` and `distribution/mcp/src/main/bin/start.sh`, including current library copy layout and extension classpath behavior.
- [ ] T003 [P] Decide the final spec wording for startup-only plugin discovery, official default bundle ownership, and operator extension path under `/.specify/specs/005-mcp-runtime-pluginization/`.

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Remove main-scope feature defaults from `mcp/bootstrap/pom.xml` so bootstrap no longer declares `shardingsphere-mcp-feature-encrypt` and `shardingsphere-mcp-feature-mask` as intrinsic runtime dependencies.
- [ ] T005 [P] Update `distribution/mcp/pom.xml` so the official packaged runtime explicitly includes encrypt and mask feature jars.
- [ ] T006 [P] Decide whether `mcp/bootstrap/pom.xml` should keep encrypt and mask in `test` scope temporarily, and document that choice in review notes to avoid reintroducing runtime coupling by accident.

**Checkpoint**: The build graph now expresses that bootstrap publishes classpath surfaces, while distribution owns the official default feature bundle.

---

## Phase 3: User Story 1 - Publish feature surface from the startup classpath only (Priority: P1)

**Goal**: Make bootstrap publish whatever valid feature handlers are present on the startup classpath and nothing more.
**Independent Test**: Run bootstrap-level tests with different classpath compositions and verify the published MCP surface changes only with installed feature jars.

### Tests for User Story 1

- [ ] T007 [P] [US1] Refactor `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/tool/MCPToolSpecificationFactoryTest.java` so it no longer treats encrypt and mask as intrinsic bootstrap guarantees.
- [ ] T008 [P] [US1] Refactor `mcp/bootstrap/src/test/java/org/apache/shardingsphere/mcp/bootstrap/transport/server/stdio/StdioTransportIntegrationTest.java` and related HTTP discovery tests so they validate classpath-driven subsets instead of one hard-coded default feature set.
- [ ] T009 [P] [US1] Add a minimal fixture feature under bootstrap test scope, including `META-INF/services` registration, to prove an arbitrary SPI-registered feature can be published without bootstrap implementation knowledge.

### Implementation for User Story 1

- [ ] T010 [US1] Update any bootstrap runtime assembly path that still relies on feature presence through direct build-time dependency assumptions rather than through runtime SPI discovery.
- [ ] T011 [US1] Ensure `mcp/core` and bootstrap registry validation still reject missing, duplicate, or ambiguous handler registration when feature jars are discovered from classpath only.
- [ ] T012 [US1] Document in code-level review notes or Javadoc where appropriate that discovery changes require restart because handler loading is startup-time only.

**Checkpoint**: Bootstrap behavior is now defined by startup classpath contents, not by compile-time default feature ownership.

---

## Phase 4: User Story 2 - Make the official distribution own the default feature bundle (Priority: P1)

**Goal**: Preserve current packaged behavior while moving default feature selection out of bootstrap.
**Independent Test**: Package the MCP distribution and verify the runtime still publishes encrypt and mask by default.

### Tests for User Story 2

- [ ] T013 [P] [US2] Add or refactor packaged-runtime verification for `distribution/mcp` so it asserts encrypt and mask jars are intentionally included in the official runtime layout.
- [ ] T014 [P] [US2] Add or refactor startup verification that the packaged runtime still publishes encrypt and mask surfaces by default after packaging responsibility moves.

### Implementation for User Story 2

- [ ] T015 [US2] Keep `distribution/mcp/src/main/bin/start.sh` aligned with the packaged classpath contract after dependency ownership moves.
- [ ] T016 [US2] Review whether packaged runtime layout creation in `distribution/mcp/pom.xml` needs additional comments or directory preparation changes to keep official libs and operator-added extension jars distinct.

**Checkpoint**: The official distribution still behaves the same for users, but the ownership line between library and packaged product is explicit.

---

## Phase 5: User Story 3 - Add external features through a clear startup extension contract (Priority: P2)

**Goal**: Make operator-facing feature installation explicit without adding a dynamic plugin subsystem.
**Independent Test**: Place a valid feature jar in the documented extension location before startup and verify that discovery exposes it; place it after startup and verify that no live reload occurs.

### Tests for User Story 3

- [ ] T017 [P] [US3] Add coverage for extension-path discovery behavior if an existing integration harness can validate jars present before startup versus after startup.
- [ ] T018 [P] [US3] Add or refactor negative-path verification for invalid external SPI registration so startup fails clearly instead of publishing partial surfaces.

### Implementation for User Story 3

- [ ] T019 [US3] Confirm and document `plugins/` as the operator extension path in `mcp/README.md`, `mcp/README_ZH.md`, and packaged runtime guidance.
- [ ] T020 [US3] State explicitly in documentation that adding or removing external feature jars requires process restart and is outside the scope of hot reload.

**Checkpoint**: Operators have a concrete, minimal plugin installation contract that matches the actual runtime behavior.

---

## Phase 6: User Story 4 - Split tests and documentation by responsibility (Priority: P2)

**Goal**: Make it obvious which layer guarantees discovery, which layer guarantees packaged defaults, and how feature authors should extend the runtime.
**Independent Test**: Review docs and tests and verify each one speaks for a single boundary only.

### Tests for User Story 4

- [ ] T021 [P] [US4] Review bootstrap, core, and distribution test names and assertions so they each describe one boundary and do not mix bootstrap discovery with packaged-default claims.

### Implementation for User Story 4

- [ ] T022 [US4] Rewrite the "add a new MCP feature" guidance in `mcp/README.md` and `mcp/README_ZH.md` so it points to distribution packaging or runtime classpath extension instead of bootstrap dependency wiring.
- [ ] T023 [US4] Add a concise compatibility note describing the behavior difference between consuming `shardingsphere-mcp-bootstrap` directly and using the official packaged distribution.
- [ ] T024 [US4] Cross-link `/.specify/specs/003-mcp-feature-spi/` and `/.specify/specs/005-mcp-runtime-pluginization/` so reviewers can distinguish "module ownership solved in 003" from "runtime/plugin packaging solved in 005".

**Checkpoint**: Reviewer and maintainer onboarding now matches the actual runtime architecture.

---

## Phase 7: Polish & Cross-Cutting Concerns

- [ ] T025 [P] Run scoped verification for `mcp/bootstrap`, affected `mcp/core` tests, and `distribution/mcp` packaging after the dependency move.
- [ ] T026 Fix any Checkstyle or Spotless issues in touched modules and ensure no transitional comments or compatibility wording reintroduces the old bootstrap-owned model.
- [ ] T027 Perform a final pass on filenames, task wording, and review notes so the pluginization narrative is consistent across spec, plan, tasks, README, and code.

## Dependencies & Execution Order

- Phase 1 must complete before code changes so the runtime contract is explicit.
- Phase 2 blocks all user stories because responsibility transfer in the build graph is the prerequisite for everything else.
- US1 and US2 should land first because they establish the actual runtime and packaging boundary.
- US3 depends on US1 and US2 because operator extension guidance only makes sense after the base contract is corrected.
- US4 depends on the earlier slices because docs and tests should describe the final boundary, not an intermediate state.
- Polish runs last after the desired slices are in place.

## Implementation Strategy

- MVP first: finish Phase 2 plus US1 and US2 so bootstrap and distribution no longer share confused ownership.
- Increment 2: complete US3 so external feature installation has an explicit startup contract.
- Increment 3: complete US4 and Phase 7 so documentation, tests, and review expectations line up with the final architecture.
