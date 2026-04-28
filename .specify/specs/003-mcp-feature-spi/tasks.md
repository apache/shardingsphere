# Tasks: MCP Feature SPI Simplification

**Input**: Design documents from `/.specify/specs/003-mcp-feature-spi/`
**Prerequisites**: `plan.md`, `spec.md`, `research.md`, `data-model.md`, `quickstart.md`, `contracts/feature-spi.md`
**Tests**: Add or update module-scoped tests for `mcp/features/spi`, `mcp/core`, `mcp/features/encrypt`, `mcp/features/mask`, and any newly added shared module.

**Organization**: Tasks are grouped by user story so the simplification can land in small, reviewable slices.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Analysis Gates

以下问题必须在实际搬迁前落成明确答案：

- `mcp/features/spi` 当前每个 package family 的 ownership 是什么
- 哪些类型是 shared API 而不是 SPI
- 哪些 concrete helper 真正被多个 feature 共享
- 哪些 implementation 是 core-private
- 为了避免 feature 反向依赖 core，`mcp/api` 的边界应如何定义
- 当前共享 concrete helper 家族如何拆分，才能在不引入 `mcp/features/support` 的前提下完成归位

---

## Phase 1: Setup and classification

- [ ] T001 Produce a type-classification matrix for all production classes under `mcp/features/spi/src/main/java/`, mapping each family to `pure-spi`, `shared-api`, `feature-support`, `core-runtime`, or `feature-owned`, and explicitly flag any SPI signature that currently exposes a concrete implementation type.
- [ ] T002 Update `mcp/pom.xml` and create `mcp/api/pom.xml` because the classification requires a dedicated shared API module.
- [ ] T003 [P] Record the decomposition targets for shared concrete helper families and explicitly verify that no `mcp/features/support` module is introduced.

---

## Phase 2: Foundational extraction

- [ ] T004 Move shared non-SPI contracts out of `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/` into `mcp/api/src/main/java/org/apache/shardingsphere/mcp/`, covering descriptor, request/response, protocol, metadata-model, and shared exception families identified by T001.
- [ ] T005 [P] Move core-private runtime helpers out of `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/` into `mcp/core/src/main/java/org/apache/shardingsphere/mcp/`, covering registry-only utilities, URI parsing, and core-owned store/runtime implementation families identified by T001.
- [ ] T006 Keep only interface-only seams in `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/`, rewrite their imports so they point at `mcp/api` contracts where needed, and eliminate signatures that return or accept concrete helpers such as workflow store implementations.
- [ ] T007 [P] Update `mcp/core/pom.xml`, `mcp/features/spi/pom.xml`, `mcp/features/encrypt/pom.xml`, and `mcp/features/mask/pom.xml` so the dependency graph matches the new contract boundaries.

**Checkpoint**: `mcp/features/spi` compiles as a pure SPI module and no longer carries shared DTO/runtime baggage.

---

## Phase 3: User Story 1 - Make the SPI boundary obvious (Priority: P1)

**Goal**: Ensure `mcp/features/spi` is recognizable as a pure SPI boundary by inspection.
**Independent Test**: Inspect the module and confirm no concrete runtime helper classes remain.

### Tests for User Story 1

- [ ] T008 [P] [US1] Add or update boundary tests in `mcp/features/spi/src/test/java/` to assert that remaining exported types are interface-level contracts and minimal supporting enums/annotations only.
- [ ] T009 [P] [US1] Add a lightweight architectural guard test or verification search that fails if new concrete classes are added under forbidden `mcp/features/spi` package families such as `tool/service`, `tool/model/workflow`, or `resource/uri`.

### Implementation for User Story 1

- [ ] T010 [US1] Remove or relocate concrete classes from `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/tool/service/`, `tool/model/workflow/`, `tool/request/`, `tool/response/`, `protocol/response/`, and any other non-SPI package families identified by T001.
- [ ] T011 [US1] Update package structure and Javadocs in `mcp/features/spi/src/main/java/org/apache/shardingsphere/mcp/` so the remaining module clearly documents itself as a pure SPI boundary.

**Checkpoint**: A reviewer can open `mcp/features/spi` and see only true SPI contracts plus minimal signature-owned types.

---

## Phase 4: User Story 2 - Place shared classes without creating circular dependencies (Priority: P1)

**Goal**: Give every former `mcp/features/spi` class family a correct home without creating feature-to-core reverse dependency.
**Independent Test**: The classification matrix is fully implemented in code, and the resulting dependency graph remains acyclic and ownership-driven.

### Tests for User Story 2

- [ ] T012 [P] [US2] Add module-scoped tests for `mcp/api` if introduced, covering shared descriptor, request/response, metadata-model, and exception contracts after extraction.
- [ ] T013 [P] [US2] Add or update compile-scope tests and dependency assertions for `mcp/core`, `mcp/features/encrypt`, and `mcp/features/mask` so reverse dependency on core implementation packages is rejected and no `mcp/features/support` module is introduced as a shortcut.

### Implementation for User Story 2

- [ ] T014 [US2] Move shared API families into `mcp/api/src/main/java/org/apache/shardingsphere/mcp/` and update all imports from `mcp/core`, `mcp/features/spi`, `mcp/features/encrypt`, and `mcp/features/mask`.
- [ ] T015 [US2] Decompose truly shared concrete helper families so that shared contracts move to `mcp/api` or SPI, while concrete implementations move to `mcp/core/src/main/java/` or the owning feature module rather than to a new support module.
- [ ] T016 [US2] Move feature-owned concrete helpers out of `mcp/features/spi` and into `mcp/features/encrypt/src/main/java/` or `mcp/features/mask/src/main/java/` based on T001 ownership decisions.
- [ ] T017 [US2] Move core-private implementations out of `mcp/features/spi` into `mcp/core/src/main/java/` and keep feature-facing access through interface-only seams where necessary.

**Checkpoint**: Every former `mcp/features/spi` type family has a final home, and no move relies on hidden reverse dependency to core.

---

## Phase 5: User Story 3 - Keep feature modules extensible without relying on core internals (Priority: P2)

**Goal**: Ensure encrypt and mask continue to extend MCP through pure SPI and shared contracts only.
**Independent Test**: Encrypt and mask compile and run without importing core-private implementation packages.

### Tests for User Story 3

- [ ] T018 [P] [US3] Update module-scoped tests in `mcp/features/encrypt/src/test/java/` and `mcp/features/mask/src/test/java/` to use extracted shared API contracts and to avoid direct core implementation imports.
- [ ] T019 [P] [US3] Update runtime discovery tests in `mcp/core/src/test/java/` and `mcp/bootstrap/src/test/java/` to confirm handler discovery still works after the contract/runtime split.

### Implementation for User Story 3

- [ ] T020 [US3] Rewrite encrypt imports and constructors in `mcp/features/encrypt/src/main/java/` so they consume pure SPI interfaces and `mcp/api` only.
- [ ] T021 [US3] Rewrite mask imports and constructors in `mcp/features/mask/src/main/java/` so they consume pure SPI interfaces and `mcp/api` only.
- [ ] T022 [US3] Update core adapter code in `mcp/core/src/main/java/` so any feature-facing runtime seam remains interface-only from the feature perspective.

**Checkpoint**: Feature modules remain extensible and operational without leaning on core implementation internals.

---

## Phase 6: Polish and guardrails

- [ ] T023 [P] Add documentation updates to `mcp/README.md`, `mcp/README_ZH.md`, and relevant module READMEs explaining the new ownership rules: pure SPI, shared API, optional feature support, core runtime, feature-owned code.
- [ ] T024 Add an architectural note or enforcement check that future contributors must not place new concrete helper code under `mcp/features/spi`.
- [ ] T025 Run module-scoped tests and style checks for every touched module and fix resulting issues.

## Dependencies and execution order

- Classification must happen before any module move.
- Shared API extraction and core-private extraction are foundational.
- Shared concrete helper decomposition must finish without introducing `mcp/features/support`.
- Feature cleanup depends on the shared API and core-private moves being settled first.
- Polish runs last after dependency cleanup is stable.

## Parallel execution examples

- After T001, T002 and T003 can proceed in parallel because `mcp/api` setup and decomposition-target recording touch different files.
- After T004 starts, T005 can proceed in parallel because shared API families and core-private runtime families have disjoint destinations.
- T020 and T021 can run in parallel because encrypt and mask have separate write scopes once the shared module boundaries are in place.

## Implementation strategy

- MVP first: classify all current `mcp/features/spi` type families and extract shared API plus core-private runtime families.
- Increment 2: resolve any remaining shared concrete helper families through decomposition into core-private or feature-owned implementations.
- Increment 3: clean feature dependencies, harden tests, and add guardrails so `mcp/features/spi` stays pure over time.
