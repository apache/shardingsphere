---
description: "Task list template for feature implementation"
---

# Tasks: [FEATURE NAME]

**Input**: Design documents from `/specs/[###-feature-name]/`
**Prerequisites**: `plan.md` (required), `spec.md` (required for user stories), `research.md`, `data-model.md`, `contracts/`
**Tests**: Optional unless explicitly requested in the feature specification.

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel
- **[Story]**: Which user story this task belongs to
- Include exact file paths in descriptions

## Phase 1: Setup (Shared Infrastructure)

- [ ] T001 Create project structure per implementation plan
- [ ] T002 Initialize dependencies and baseline tooling
- [ ] T003 [P] Configure formatting and verification

---

## Phase 2: Foundational (Blocking Prerequisites)

- [ ] T004 Establish shared infrastructure required by all user stories
- [ ] T005 [P] Add supporting models or metadata contracts
- [ ] T006 [P] Add validation and error handling infrastructure

**Checkpoint**: Foundation ready; user story work can now begin.

---

## Phase 3: User Story 1 - [Title] (Priority: P1)

**Goal**: [What this story delivers]
**Independent Test**: [How to verify this story on its own]

### Tests for User Story 1 (optional)

- [ ] T010 [P] [US1] Add contract or integration test

### Implementation for User Story 1

- [ ] T011 [P] [US1] Add data model or metadata support
- [ ] T012 [US1] Implement service or handler logic
- [ ] T013 [US1] Wire endpoint, tool, or feature entry point

**Checkpoint**: User Story 1 should be independently functional and testable.

---

## Phase N: Polish & Cross-Cutting Concerns

- [ ] TXXX [P] Documentation updates
- [ ] TXXX Code cleanup and refactoring
- [ ] TXXX Additional tests if requested
- [ ] TXXX Validation against quickstart or operational guide

## Dependencies & Execution Order

- Setup before foundational work.
- Foundational work blocks all user stories.
- User stories may proceed in parallel after foundational work is complete.
- Polish depends on all desired user stories being complete.
