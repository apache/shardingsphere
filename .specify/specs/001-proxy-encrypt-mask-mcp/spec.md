# Feature Specification: ShardingSphere-Proxy Encrypt and Mask MCP V1

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-17
**Status**: Draft
**Input**: User description: "Build a ShardingSphere-Proxy MCP workflow that can interpret natural language to configure encryption or masking for a table column, recommend algorithms, generate optional physical DDL, create rules, and validate the result."

## Clarifications

### Session 2026-04-17

- Scope is limited to `ShardingSphere-Proxy`.
- MCP runtime connects to `ShardingSphere-Proxy` rather than directly to underlying physical storage.
- V1 delivers encryption and masking together rather than staging masking into a later release.
- The user-facing model is always a logical view. Before encryption, logical view may equal physical view; after encryption, logical view remains primary and may differ from physical storage.
- Logical metadata validation is based on Proxy's logical view rather than direct physical metadata scanning.
- Every run MUST start with a global step list for confirmation.
- The workflow MUST support two execution styles: do everything in one guided flow, or advance one step at a time.
- Step-by-step mode should remain simple for operators; confirmed workflow context is retained server-side rather than requiring users to resend it every turn.
- Default derived physical column names are `*_cipher`, `*_assisted_query`, and `*_like_query`.
- Algorithm recommendation MUST come from natural language intent plus follow-up questions; if critical inputs are missing, the system MUST keep asking.
- Sample data is optional and should only be used after user approval when metadata and naming are insufficient.
- Rule resources and algorithm resources should be queryable from MCP.
- Rollback is not required in V1.
- Audit persistence is not required in V1.
- V1 supports create and alter flows for encrypt rules.
- V1 supports create, alter, and drop flows for mask rules.
- Physical DDL generation is allowed, but execution mode is operator-selected:
  - auto-generate and auto-execute,
  - auto-generate, review, then AI executes after approval,
  - auto-generate for operator manual execution only.
- V1 only establishes rules and prepares DDL; it does not process or migrate existing data.
- Generated physical column types should follow ShardingSphere default type strategy; MCP should not invent its own type mapping.
- Existing physical columns are not automatically reused; the default strategy is to generate a new set and resolve conflicts safely.
- If generated names conflict, the system should auto-rename using numeric suffixes and return the final names to the user.
- Recommendation should include built-in algorithms and currently installed custom SPI algorithms available in Proxy.
- `SHOW ... ALGORITHM PLUGINS` is the discovery baseline, but encrypt capability recommendation may be enriched by SPI metadata and instance probing because plugin rows do not expose decrypt/equality/like capability flags.
- Secret algorithm properties should be collected after algorithm selection, retained in session context, and masked in review output.
- The workflow should generate index recommendations or DDL when derived query-supporting columns need them.
- Validation must cover DDL, rules, logical metadata, and SQL executability.
- Mask workflows are rule-first in V1 and do not require physical DDL unless the approved plan explicitly includes it.
- Encrypt drop workflows are deferred and not included in V1.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Turn natural language into a safe rule plan (Priority: P1)

As a database operator, I want to describe an encryption or masking goal in natural language and receive a clarified, reviewable plan so that I can configure ShardingSphere-Proxy without manually assembling DistSQL and physical column strategy.

**Why this priority**: Without a reliable planning and clarification flow, the rest of the feature becomes unsafe and difficult to adopt.

**Independent Test**: Submit a natural language request with incomplete context and verify that the workflow asks follow-up questions, produces a complete step list, recommends algorithms, and generates a reviewable plan without executing changes.

**Acceptance Scenarios**:

1. **Given** a natural language request that names a database, table, and column but does not describe query behavior, **When** the workflow analyzes it, **Then** it asks follow-up questions needed to choose encryption or masking algorithms safely.
2. **Given** a fully clarified request, **When** the workflow prepares the plan, **Then** it returns a global step list, recommended algorithm strategy, naming proposal, and execution options before any schema or rule change occurs.
3. **Given** a request that only requires result masking, **When** the workflow classifies intent, **Then** it produces a mask-focused plan rather than an encryption plan.

---

### User Story 2 - Apply optional DDL and full rule lifecycle changes under operator-selected execution mode (Priority: P1)

As a database operator, I want control over whether generated physical DDL is executed automatically, executed after review, or left for manual execution so that I can match the workflow to my operational constraints.

**Why this priority**: DDL and rule changes can affect production-like environments; operator control is mandatory.

**Independent Test**: Run encrypt create/alter requests and mask create/alter/drop requests through each execution mode and verify that the workflow respects the selected mode, shows generated SQL and DistSQL, and never processes data migration.

**Acceptance Scenarios**:

1. **Given** a clarified encryption request that requires new derived columns, **When** the operator selects auto-execute mode, **Then** the workflow generates the physical DDL, executes it, applies the rule change, and reports progress step by step.
2. **Given** the same request, **When** the operator selects review-first mode, **Then** the workflow displays the generated SQL and DistSQL for review and only executes after explicit approval.
3. **Given** the same request, **When** the operator selects manual mode, **Then** the workflow generates SQL and DistSQL, stops before execution, and returns what the operator must run manually.
4. **Given** a V1 workflow, **When** schema and rules are applied, **Then** no existing data is backfilled, migrated, or transformed automatically.
5. **Given** an existing encrypt rule, **When** the operator requests modification, **Then** the workflow generates the appropriate DistSQL, follows the selected execution mode, and reports the lifecycle action explicitly.
6. **Given** an existing mask rule, **When** the operator requests modification or deletion, **Then** the workflow generates the appropriate DistSQL, follows the selected execution mode, and reports the lifecycle action explicitly.

---

### User Story 3 - Preserve logical view semantics while handling physical naming conflicts (Priority: P1)

As a database operator, I want the system to preserve logical view semantics and resolve physical naming conflicts automatically so that my logical column contract remains stable even when the storage-side column layout changes.

**Why this priority**: Logical view continuity is fundamental to ShardingSphere encryption behavior, and naming conflicts are common in brownfield environments.

**Independent Test**: Configure a column whose default derived names collide with existing physical columns and verify that the workflow creates a new generated set, auto-renames with numeric suffixes, returns the final names, and still presents the logical column as the user-facing contract.

**Acceptance Scenarios**:

1. **Given** a logical column whose default `*_cipher` derived name is already taken, **When** the plan is generated, **Then** the workflow assigns a safe alternative physical name and reports the final chosen name to the operator.
2. **Given** an encryption rule is applied successfully, **When** the operator inspects the result, **Then** the logical column remains the primary user-facing reference even though physical storage uses derived columns.
3. **Given** a request that does not require new physical columns, **When** the plan is generated, **Then** the workflow may produce a rule-only path while still treating the logical view as primary.
4. **Given** a physical table already contains columns matching the default generated naming pattern, **When** the workflow prepares a new encryption layout, **Then** it does not automatically reuse those columns and instead generates a new safe naming set.

---

### User Story 4 - Inspect and validate the resulting configuration end to end (Priority: P2)

As a database operator or developer, I want to inspect existing rules and validate the final state after execution so that I can trust the workflow result without manually correlating multiple tools.

**Why this priority**: Operational trust depends on complete inspection and verification, especially because V1 omits rollback and audit persistence.

**Independent Test**: After a rule workflow completes, verify that the system can report the resulting DDL state, rule state, logical metadata state, and logical-SQL executability in one summary.

**Acceptance Scenarios**:

1. **Given** a completed encryption or masking change, **When** validation runs, **Then** it checks physical DDL state, rule state, logical metadata state, and SQL executability against the logical view.
2. **Given** an existing logical database, **When** the operator asks to inspect current rules or algorithms, **Then** the workflow returns the relevant encrypt rules, mask rules, and algorithm plugin information without requiring manual DistSQL composition.
3. **Given** validation finds a mismatch between generated plan and observed metadata or SQL executability result, **When** the summary is returned, **Then** the mismatch is explicitly called out rather than silently ignored.

---

### User Story 5 - Recommend supporting indexes for query-oriented derived columns (Priority: P2)

As a database operator, I want the workflow to generate index recommendations or DDL for query-oriented derived columns so that the final configuration is operationally useful and not just syntactically complete.

**Why this priority**: Assisted query and like query columns are often incomplete without index planning, and operators should not need a second manual design pass for obvious support structures.

**Independent Test**: Submit a request that requires assisted query or like query columns and verify that the plan includes relevant index recommendations or index DDL alongside the main DDL and rule change.

**Acceptance Scenarios**:

1. **Given** a request that enables assisted query or like query derived columns, **When** the plan is generated, **Then** the workflow includes index recommendations or explicit index DDL for those columns.
2. **Given** the operator chooses review or manual mode, **When** DDL artifacts are returned, **Then** the index-related DDL is visible alongside column DDL and rule DistSQL.

---

### Edge Cases

- What happens when the request omits `database` and the workflow cannot rely on `USE` semantics?
- What happens when the target logical table or column cannot be resolved from logical metadata?
- What happens when a column already has encrypt or mask rules and the new request conflicts with them?
- What happens when default derived names collide with multiple existing physical objects?
- What happens when generated names must avoid existing columns that appear reusable but are intentionally not reused?
- What happens when the chosen algorithm does not support the required decrypt, equality-filter, or like-query capability?
- What happens when a custom encrypt SPI algorithm is discoverable from plugins but capability probing is incomplete or unavailable?
- What happens when the operator chooses manual DDL mode but expects the workflow to continue with automatic validation?
- What happens when metadata is ambiguous and the operator refuses sample data access?
- What happens when storage units or physical DDL permissions are unavailable?
- What happens when the request is a mask-rule deletion that does not require DDL but still requires validation and summary?
- What happens when index creation is recommended but the operator declines index DDL execution?
- What happens when the user requests encrypt-rule deletion in V1 even though that lifecycle action is deferred?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST only target ShardingSphere-Proxy in V1.
- **FR-001A**: The system MUST execute workflow planning, rule inspection, rule application, and validation against ShardingSphere-Proxy rather than directly against underlying physical storage.
- **FR-002**: The system MUST require explicit logical database context before planning or execution.
- **FR-003**: The system MUST present a logical view to the operator before and after encryption changes.
- **FR-004**: The system MUST support both encryption intents and masking intents from natural language input.
- **FR-005**: The system MUST detect missing critical inputs and ask follow-up questions before recommending algorithms or generating execution artifacts.
- **FR-006**: The system MUST present a global step list before any execution activity.
- **FR-007**: The system MUST support an all-at-once guided flow and a one-step-at-a-time flow.
- **FR-008**: The system MUST recommend encryption algorithms based on recoverability, equality-filter needs, and like-query needs.
- **FR-009**: The system MUST recommend masking algorithms based on field semantics and display behavior requirements.
- **FR-010**: The system MUST NOT inspect sample data unless the operator explicitly allows it.
- **FR-011**: The system MUST expose existing encrypt rules for the selected logical database.
- **FR-012**: The system MUST expose existing mask rules for the selected logical database.
- **FR-013**: The system MUST expose available encrypt algorithm plugins, including installed custom SPI implementations visible to Proxy.
- **FR-014**: The system MUST expose available mask algorithm plugins, including installed custom SPI implementations visible to Proxy.
- **FR-015**: The system MUST use built-in and currently installed custom SPI algorithms as the recommendation pool for encrypt and mask planning.
- **FR-015A**: The system MUST treat `SHOW ... ALGORITHM PLUGINS` as the discovery baseline and MUST allow encrypt capability recommendation to be enriched by SPI metadata or instance probing when available.
- **FR-016**: The system MUST default derived physical column names to `*_cipher`, `*_assisted_query`, and `*_like_query`.
- **FR-017**: The system MUST NOT automatically reuse pre-existing physical columns merely because they match generated naming patterns.
- **FR-018**: The system MUST auto-resolve naming conflicts with numeric suffixes and MUST report the final names back to the operator.
- **FR-019**: The system MUST generate physical DDL when derived columns are required and DDL is part of the approved path.
- **FR-020**: The system MUST use ShardingSphere default type strategy when generating physical derived column definitions.
- **FR-021**: The system MUST support three DDL execution modes: auto-execute, review-then-execute, and manual-only.
- **FR-022**: The system MUST preview generated SQL and DistSQL before execution in any mode that includes operator review.
- **FR-023**: The system MUST allow rule-only workflows when no physical DDL is needed.
- **FR-024**: The system MUST support create and alter flows for encrypt rules after operator confirmation.
- **FR-024A**: The system MUST support create, alter, and drop flows for mask rules after operator confirmation.
- **FR-025**: The system MUST NOT backfill, migrate, or transform existing data in V1.
- **FR-026**: The system MUST NOT require rollback support in V1.
- **FR-027**: The system MUST NOT require audit persistence in V1.
- **FR-028**: The system MUST report progress at each step during guided execution.
- **FR-029**: The system MUST validate physical DDL state after execution when DDL is in scope.
- **FR-030**: The system MUST validate rule state after execution.
- **FR-031**: The system MUST validate logical metadata state after execution based on Proxy's logical view.
- **FR-032**: The system MUST validate SQL executability from the logical view without requiring migrated data or result-correctness checks against real payloads.
- **FR-033**: The system MUST generate index recommendations or index DDL for derived query-supporting columns when appropriate.
- **FR-034**: The system MUST return an execution summary that includes target objects, algorithms, final generated names, chosen execution mode, executed or generated SQL/DistSQL, index recommendations or index DDL, and validation results.
- **FR-035**: The system MUST clearly distinguish between plan generation, operator review, execution, and validation states.
- **FR-036**: The system MUST stop before execution when the operator chooses manual-only mode, while still returning the artifacts needed for later validation.
- **FR-037**: The system MUST allow the operator to review and override proposed names before execution where such override is compatible with safety checks.
- **FR-038**: The system MUST surface rule or algorithm conflicts explicitly instead of silently falling back to unsupported behavior.
- **FR-039**: The system MUST keep the logical column as the primary user-facing identifier even when physical derived columns are introduced.
- **FR-040**: The system MUST support rule-only deletion for mask drop flows in V1.
- **FR-041**: The system MUST allow mask workflows to complete without physical DDL when the approved plan is rule-only.
- **FR-042**: The system MUST retain confirmed workflow context server-side for step-by-step mode so operators do not need to resend previously confirmed inputs each turn.
- **FR-043**: The system MUST collect secret algorithm properties after algorithm selection, keep them in runtime context, and mask them in review or summary output.
- **FR-044**: The system MUST reject or clearly defer encrypt drop requests in V1 instead of silently generating unsupported workflows.

### Key Entities *(include if feature involves data)*

- **Database Context**: The logical database scope selected for planning, rule inspection, DDL generation, execution, and validation.
- **Column Intent**: The clarified description of what the operator wants for a target column, including encrypt versus mask, query behavior, recoverability, and display requirements.
- **Interaction Plan**: The ordered list of steps, follow-up questions, approvals, execution mode, and review checkpoints for a given request.
- **Derived Column Plan**: The proposed physical columns, default names, collision handling result, and final resolved names associated with a logical column.
- **Rule Plan**: The intended encrypt or mask rule configuration, including algorithms, logical-to-physical mappings, and generated DistSQL.
- **Index Plan**: The recommended or generated index definitions associated with assisted query or like query derived columns.
- **Validation Report**: The combined result of DDL, rule, logical metadata, and logical-SQL-executability verification for a completed workflow.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every request that lacks critical planning inputs triggers explicit follow-up questions before execution artifacts are produced.
- **SC-002**: Every completed plan includes a global step list, algorithm recommendation, naming outcome, DDL or rule preview, index recommendation when relevant, and validation strategy.
- **SC-003**: In manual-only mode, zero SQL or DistSQL statements are executed automatically.
- **SC-004**: In execution-enabled modes, post-run validation covers DDL state, rule state, logical metadata state, and logical-SQL executability.
- **SC-005**: Every automatic naming conflict resolution returns the final generated names in the operator-visible output.
- **SC-006**: No V1 workflow performs historical data migration or backfill.
- **SC-007**: Encrypt create/alter flows and mask create/alter/drop flows can each be planned and executed through the same review-first interaction model.
- **SC-008**: Physical derived column definitions always follow ShardingSphere default type strategy rather than MCP-specific type inference.

## Assumptions

- The target logical database already exists in ShardingSphere-Proxy and is reachable by the MCP runtime.
- Existing MCP capabilities will be extended to inspect rules, algorithms, and logical metadata needed by this workflow.
- Existing MCP capabilities will be extended so recommendation can see both built-in algorithms and installed custom SPI algorithms exposed by Proxy.
- Operators may have different permissions for DDL execution, so execution mode must remain explicit.
- V1 is optimized for single-database, single-table, single-column interactions even if future versions expand to broader scopes.
- Repository and product governance remain defined by `AGENTS.md`, `CODE_OF_CONDUCT.md`, and this feature-specific constitution.

## Out of Scope

- ShardingSphere-JDBC support.
- Historical data migration or backfill.
- Rollback orchestration.
- Audit persistence.
- Encrypt drop workflows.
- Bulk multi-database or multi-column workflows in a single request.
- Silent execution without operator awareness of the generated SQL and DistSQL.
