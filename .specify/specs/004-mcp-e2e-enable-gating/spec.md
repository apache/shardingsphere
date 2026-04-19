# Feature Specification: MCP E2E Property-Based Enablement

**Feature Branch**: `[001-shardingsphere-mcp]`
**Created**: 2026-04-19
**Status**: Draft
**Input**: User description: "For `test/e2e/mcp`, do not introduce new Maven profiles for lane selection. Reuse the enablement style used by other ShardingSphere E2E modules: control defaults from `env/*.properties`, allow `-D` overrides, and gate heavy suites through explicit enable switches."

## Clarifications

### Session 2026-04-19

- This feature is about MCP E2E enablement and execution selection, not about changing MCP business behavior or deleting existing E2E coverage.
- No new Maven profile should be introduced to choose MCP E2E subsets.
- MCP E2E should reuse the existing ShardingSphere E2E pattern of configuration-file defaults plus JVM system-property overrides.
- The MCP module may use module-local configuration and helper classes, but the overall mechanism should match other E2E modules by driving execution through `isEnabled()` or shared condition helpers rather than scattered one-off gating styles.
- Default local execution should keep a lightweight always-on baseline and move heavier suites behind explicit enable switches.
- The lightweight baseline should include contract-level MCP coverage and lightweight H2-backed production coverage.
- Heavy suites that depend on Docker, child-process startup, packaged distribution artifacts, or external model runtimes should be opt-in.
- Docker availability, packaged distribution availability, and model-runtime availability remain secondary runtime guards; they should not be the primary lane-selection mechanism.
- Distribution and LLM suites may keep specialized configuration inputs, but their top-level execution gate should align with the same module-level enablement scheme.
- The documented MCP E2E lane-switch family should use a unified `mcp.e2e.<lane>.enabled` naming style.
- The initial documented lane defaults should be:
  - `mcp.e2e.contract.enabled=true`
  - `mcp.e2e.production.h2.enabled=true`
  - `mcp.e2e.production.mysql.enabled=false`
  - `mcp.e2e.production.stdio.enabled=false`
  - `mcp.e2e.proxy.workflow.enabled=false`
  - `mcp.e2e.distribution.enabled=false`
  - `mcp.e2e.llm.enabled=false`
- If the implementation needs to read an existing legacy LLM-specific switch during transition, that compatibility path should be temporary and should not become the primary documented MCP E2E naming model.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Keep the default MCP E2E run lightweight and deterministic (Priority: P1)

As an MCP contributor, I want the default `test/e2e/mcp` run to execute only the lightweight baseline lanes so that local feedback stays fast and does not depend on Docker, packaged artifacts, or external model infrastructure.

**Why this priority**: The current pain point is that routine local runs spend most of their time in heavy suites that are not always needed for day-to-day development.

**Independent Test**: Run the MCP E2E module with no lane-specific overrides and verify that only the contract and lightweight H2-backed baseline suites execute, while heavy suites remain disabled.

**Acceptance Scenarios**:

1. **Given** the default MCP E2E configuration, **When** a contributor runs the module tests without extra enable overrides, **Then** only the lightweight baseline lanes execute.
2. **Given** the default MCP E2E configuration, **When** Docker, packaged distribution artifacts, or an LLM runtime are unavailable, **Then** the default run still remains valid because those heavy lanes are disabled.
3. **Given** the default MCP E2E configuration, **When** a lightweight baseline test fails, **Then** the failure reflects always-on baseline MCP behavior rather than an unintentionally enabled heavy dependency.

---

### User Story 2 - Enable only the heavy MCP lane that matters for the current change (Priority: P1)

As an MCP maintainer, I want to turn on a specific heavy lane through an explicit enable switch so that I can verify the exact risk area I changed without paying for unrelated heavy suites.

**Why this priority**: The main value of property-based gating is selective confidence. Without independent lane switches, heavy suites remain bundled and local iteration stays slow.

**Independent Test**: Run the module multiple times with different lane-specific property overrides and verify that each override enables only the intended heavy lane or lane group.

**Acceptance Scenarios**:

1. **Given** only the MySQL-backed production lane is explicitly enabled, **When** the module tests run, **Then** the MySQL-backed production suite executes without forcing STDIO, proxy workflow, distribution, or LLM lanes to run.
2. **Given** only the STDIO production lane is explicitly enabled, **When** the module tests run, **Then** STDIO suites execute without forcing MySQL, proxy workflow, distribution, or LLM lanes to run.
3. **Given** multiple explicit lane-enable overrides, **When** the module tests run, **Then** the selected lanes execute together and unselected heavy lanes remain disabled.

---

### User Story 3 - Reuse the same enablement style as other ShardingSphere E2E modules (Priority: P1)

As a repository maintainer, I want MCP E2E enablement to follow the same configuration and condition pattern used by other ShardingSphere E2E modules so that contributors do not have to learn a one-off execution model for MCP.

**Why this priority**: Repository consistency matters as much as raw runtime savings. A special MCP-only gating model would add maintenance cost and reviewer friction.

**Independent Test**: Review the MCP E2E configuration entry point and verify that it uses `env/*.properties` defaults, system-property overrides, and explicit `isEnabled()` or shared-condition checks consistent with existing E2E modules.

**Acceptance Scenarios**:

1. **Given** the MCP E2E module configuration, **When** a reviewer inspects how default values are loaded, **Then** they find a configuration-file-first model with JVM system properties overriding file defaults.
2. **Given** MCP E2E test classes, **When** a reviewer inspects lane gating, **Then** they find lane execution controlled through `isEnabled()` or shared helper logic rather than a new Maven profile or scattered special-case patterns.
3. **Given** another maintainer later adds a new heavy MCP lane, **When** they follow the established pattern, **Then** they can add the lane by extending the shared MCP E2E configuration and condition model instead of inventing a new mechanism.

---

### User Story 4 - Let CI and specialized jobs opt into the right MCP lanes explicitly (Priority: P2)

As a CI maintainer, I want workflow jobs to opt into MCP heavy lanes through explicit property overrides so that each job states exactly what it is validating and avoids hidden default coupling.

**Why this priority**: CI readability and determinism are important. Release or nightly jobs should be explicit about which heavy lanes they turn on.

**Independent Test**: Inspect CI commands for MCP subchain, packaged distribution validation, and LLM jobs and verify that heavy-lane participation is selected by explicit property overrides instead of always-on module defaults or new Maven profiles.

**Acceptance Scenarios**:

1. **Given** a CI job that validates packaged distribution behavior, **When** it runs, **Then** it explicitly enables the distribution lane rather than depending on a default always-on distribution suite.
2. **Given** a CI or nightly job that validates model-driven MCP behavior, **When** it runs, **Then** it explicitly enables the LLM lane while still supplying any required model runtime settings separately.
3. **Given** a contributor reads a workflow file, **When** they inspect the Maven command, **Then** they can tell which heavy MCP lanes are intended to run from the passed property overrides alone.

---

### Edge Cases

- What happens when a lane-enable property is absent from both the module configuration file and JVM system properties?
- What happens when a heavy lane is enabled but its required runtime dependency, such as Docker or packaged distribution artifacts, is unavailable?
- What happens when one heavy lane is enabled and another lane with overlapping helper code remains disabled?
- What happens when a contributor enables the distribution lane without providing the packaged distribution home required for that run?
- What happens when the LLM lane is enabled but its model endpoint or credentials are still missing or invalid?
- What happens when future MCP lanes are added and need clear baseline-versus-opt-in defaults without reintroducing a profile matrix?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST load MCP E2E default settings from a module-local `env/*.properties` configuration file under `test/e2e/mcp`.
- **FR-002**: The system MUST allow JVM system properties to override MCP E2E default settings loaded from the module-local configuration file.
- **FR-003**: The system MUST NOT introduce a new Maven profile to select MCP E2E lanes.
- **FR-004**: The system MUST define explicit MCP E2E lane-enable switches for contract, production.h2, production.mysql, production.stdio, proxy.workflow, distribution, and llm lanes.
- **FR-005**: The system MUST keep the default MCP E2E baseline lightweight enough to run without Docker, packaged distribution artifacts, or external model infrastructure.
- **FR-006**: The default MCP E2E baseline MUST include contract-level MCP verification and lightweight H2-backed production verification.
- **FR-007**: The documented default lane-enable values MUST be `contract=true`, `production.h2=true`, `production.mysql=false`, `production.stdio=false`, `proxy.workflow=false`, `distribution=false`, and `llm=false`.
- **FR-008**: MySQL-backed production verification MUST be selectable independently from STDIO verification, proxy workflow verification, distribution verification, and LLM verification.
- **FR-009**: STDIO production verification MUST be selectable independently from MySQL-backed production verification, proxy workflow verification, distribution verification, and LLM verification.
- **FR-010**: Proxy workflow verification MUST be selectable independently from MySQL-backed production verification, STDIO verification, distribution verification, and LLM verification.
- **FR-011**: Packaged distribution verification MUST be selectable independently from other heavy MCP lanes.
- **FR-012**: LLM verification MUST be selectable independently from other heavy MCP lanes.
- **FR-013**: Disabled heavy lanes MUST remain non-blocking when their external dependencies are unavailable.
- **FR-014**: Enabled heavy lanes MAY still apply runtime dependency guards such as Docker availability checks, packaged-distribution availability checks, or model-runtime readiness checks.
- **FR-015**: The primary decision of whether a lane participates in the run MUST come from explicit MCP E2E enablement settings rather than from runtime dependency detection alone.
- **FR-016**: MCP E2E test classes MUST express lane selection through `isEnabled()` methods or shared condition helpers consistent with other ShardingSphere E2E modules.
- **FR-017**: MCP E2E configuration MUST remain understandable from one module-local configuration entry point plus a small number of shared enablement helpers.
- **FR-018**: MCP E2E default settings MUST preserve existing time-zone configuration support already defined for the module.
- **FR-019**: Distribution validation MAY keep additional artifact-location settings, but its top-level execution gate MUST align with the same MCP E2E enablement model as other lanes.
- **FR-020**: LLM validation MAY keep additional model endpoint, credential, timeout, and artifact settings, but its top-level execution gate MUST align with the same MCP E2E enablement model as other lanes.
- **FR-021**: CI and release-oriented jobs MUST be able to enable MCP heavy lanes entirely through explicit property overrides in Maven commands.
- **FR-022**: The lane-enable naming scheme MUST use the `mcp.e2e.<lane>.enabled` form for documented MCP E2E lane switches so reviewers can distinguish baseline defaults from opt-in lanes consistently.
- **FR-023**: The feature MUST preserve the existence of current MCP E2E suites; lane selection changes execution defaults rather than deleting coverage.
- **FR-024**: The feature MUST make it obvious to reviewers where new MCP E2E lanes should declare defaults and where they should attach their execution conditions.

### Key Entities *(include if feature involves data)*

- **MCP E2E Configuration Source**: The module-local MCP E2E property file plus JVM system-property overrides that together determine which lanes participate in one run.
- **Baseline Lane**: An always-on-by-default MCP E2E lane intended to provide fast, deterministic feedback without external runtime dependencies.
- **Opt-In Lane**: A heavier MCP E2E lane that requires an explicit enable decision because it depends on Docker, child-process startup, packaged artifacts, or external model infrastructure.
- **Lane Enable Switch**: A boolean MCP E2E setting that decides whether one lane or lane group should participate in a given run.
- **External Dependency Guard**: A secondary runtime check that verifies Docker, packaged artifacts, or model infrastructure for a lane that has already been enabled.
- **Shared MCP E2E Condition**: The helper logic that translates configuration values into actual test participation decisions.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A default MCP E2E module run executes the lightweight contract and H2-backed baseline lanes without executing MySQL-backed production, STDIO production, proxy workflow, distribution, or LLM lanes.
- **SC-002**: A contributor can enable any one heavy MCP lane through explicit property overrides without causing unrelated heavy lanes to run.
- **SC-003**: No new Maven profile is required in POMs or workflow commands to select MCP E2E lanes.
- **SC-004**: CI and release-oriented workflow commands state heavy MCP lane participation through explicit property overrides rather than through hidden module defaults.
- **SC-005**: A reviewer can identify the MCP E2E default-versus-opt-in lane model by inspecting one module-local configuration file and the associated enablement helpers.

## Assumptions

- The MCP E2E module should retain a small, always-on baseline rather than defaulting every lane to disabled.
- MySQL-backed production, STDIO production, proxy workflow, packaged distribution, and LLM validation are considered heavy or externally dependent lanes.
- The repository's existing E2E configuration-file and `isEnabled()` patterns are the target consistency baseline for MCP.
- Specialized LLM and distribution settings remain necessary, but they should sit underneath a common top-level lane-enable model.

## Out of Scope

- Removing existing MCP E2E suites from the repository.
- Redesigning MCP business behavior, transport behavior, or workflow assertions.
- Rewriting unrelated ShardingSphere E2E modules to use MCP-specific naming.
- Introducing a new Maven profile matrix for MCP lane selection.
