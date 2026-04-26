# Research: MCP E2E Property-Based Enablement

## Decision 1: Shared configuration source

- **Decision**: Load MCP E2E lane defaults from `test/e2e/mcp/src/test/resources/env/e2e-env.properties` and overlay JVM system properties through the existing shared E2E property-loading mechanism.
- **Rationale**: This matches the established ShardingSphere E2E model, keeps defaults reviewable in one module-local file, and avoids introducing a separate profile matrix.
- **Alternatives considered**:
  - New Maven profiles per lane: rejected because the feature explicitly forbids new profiles and profiles would fragment selection logic.
  - Raw per-class `System.getProperty(...)` calls: rejected because that keeps the current one-off gate sprawl and duplicates configuration handling.

## Decision 2: Lane taxonomy and defaults

- **Decision**: Use the documented lane family:
  - `mcp.e2e.contract.enabled=true`
  - `mcp.e2e.production.h2.enabled=true`
  - `mcp.e2e.production.mysql.enabled=false`
  - `mcp.e2e.production.stdio.enabled=false`
  - `mcp.e2e.proxy.workflow.enabled=false`
  - `mcp.e2e.distribution.enabled=false`
  - `mcp.e2e.llm.enabled=false`
- **Rationale**: This preserves a useful always-on baseline while keeping Docker, packaged-distribution, and external-model suites opt-in.
- **Alternatives considered**:
  - Disable every lane by default: rejected because it would remove the always-on safety net for routine local MCP changes.
  - Keep some heavy lanes on by default: rejected because the current local runtime pain comes from hidden heavy-suite participation.

## Decision 3: Composite semantics for transport-plus-backend suites

- **Decision**: Treat `StdioProductionMySQLRuntimeSmokeE2ETest` as a composite case that should run only when both `mcp.e2e.production.mysql.enabled=true` and `mcp.e2e.production.stdio.enabled=true`.
- **Rationale**: The class validates both a heavy backend and a heavy transport. Requiring both lanes keeps each dimension independently selectable and prevents one switch from silently broadening scope.
- **Alternatives considered**:
  - Put the class under `production.mysql` only: rejected because it would unexpectedly trigger STDIO validation when the user asked only for MySQL-backed HTTP coverage.
  - Put the class under `production.stdio` only: rejected because it would unexpectedly require Docker and MySQL when the user asked only for STDIO H2 coverage.

## Decision 4: Gate style inside tests

- **Decision**: Make the top-level participation decision come from shared MCP E2E predicates exposed through `isEnabled()` methods or a shared condition helper, while keeping Docker and other environment assumptions only as secondary runtime guards.
- **Rationale**: This aligns MCP with other repository E2E modules and cleanly separates "should this suite participate?" from "is its external dependency available right now?".
- **Alternatives considered**:
  - Keep Docker or packaged-artifact detection as the primary selector: rejected because dependency detection should not implicitly decide suite scope.
  - Keep `@EnabledIfSystemProperty` or ad hoc LLM assumption messages per class: rejected because they perpetuate the current inconsistency.

## Decision 5: Legacy selector removal in the same delivery

- **Decision**: Replace `mcp.distribution.smoke.enabled` and `mcp.llm.e2e.enabled` with the unified lane family in the same feature delivery, while keeping `mcp.distribution.home` and the non-selector `mcp.llm.*` settings intact.
- **Rationale**: The user explicitly chose no compatibility period for legacy selectors. Keeping the old lane names alive would preserve two competing execution models.
- **Alternatives considered**:
  - Short-term dual-read compatibility: rejected because it weakens the cleanup goal and makes CI migration incomplete.
  - Rewrite all LLM settings into the new namespace: rejected because only the top-level enable switch needs normalization; the remaining LLM settings are lane-specific operational inputs, not selectors.

## Decision 6: CI command alignment

- **Decision**: Update MCP-related workflows so:
  - the general MCP module test steps rely on the new baseline defaults;
  - packaged-distribution workflows pass `-Dmcp.e2e.distribution.enabled=true`;
  - LLM workflows pass `-Dmcp.e2e.llm.enabled=true`.
- **Rationale**: This makes workflow intent obvious from the Maven invocation and keeps local and CI behavior on one selection model.
- **Alternatives considered**:
  - Keep CI using legacy selectors while local runs use the new model: rejected because it preserves two mental models and obscures which suites CI actually exercises.
  - Make CI enable all heavy lanes in the shared default command: rejected because it would recreate the same hidden heavy-lane coupling the feature is removing.
