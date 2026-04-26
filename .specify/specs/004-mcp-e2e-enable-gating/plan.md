# Implementation Plan: MCP E2E Property-Based Enablement

**Branch**: `[001-shardingsphere-mcp]` | **Date**: 2026-04-19 | **Spec**: `/.specify/specs/004-mcp-e2e-enable-gating/spec.md`
**Input**: Feature specification from `/.specify/specs/004-mcp-e2e-enable-gating/spec.md`

## Summary

This change standardizes `test/e2e/mcp` test selection around one module-local property model:

- load default lane switches from `test/e2e/mcp/src/test/resources/env/e2e-env.properties`;
- allow JVM `-D` values to override those defaults;
- keep only `contract` and `production.h2` enabled by default;
- require explicit enablement for `production.mysql`, `production.stdio`, `proxy.workflow`, `distribution`, and `llm`;
- remove legacy top-level lane switches such as `mcp.distribution.smoke.enabled` and `mcp.llm.e2e.enabled` in the same delivery;
- update MCP-related GitHub workflows so heavy lanes are always selected explicitly from command lines.

The implementation should reuse the existing ShardingSphere E2E style of shared property loading plus `isEnabled()` or condition-helper gating, rather than introducing a new Maven profile or preserving one-off gate styles inside individual test classes.

## Technical Context

**Language/Version**: Java 17  
**Primary Dependencies**:
- `test/e2e/env` shared property loading support, especially `EnvironmentPropertiesLoader`
- JUnit 5 conditional execution support used by existing E2E modules
- MCP E2E runtime helpers under `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/support/`
- GitHub Actions workflows for MCP subchain, MCP build, and LLM suites
**Storage**:
- Module-local `env/e2e-env.properties`
- JVM system properties passed through Maven commands
- Existing lane-specific auxiliary properties such as `mcp.distribution.home` and `mcp.llm.*`
**Testing**:
- JUnit 5
- Scoped Maven test runs for `test/e2e/mcp`
- Workflow-equivalent targeted Maven commands for distribution and LLM suites
- Relevant scoped style and compile verification for touched files
**Target Platform**:
- Local developer runs on macOS and Linux
- GitHub Actions on `ubuntu-latest`
- Optional Docker-backed, packaged-distribution, and model-runtime environments for heavy lanes
**Project Type**: Java test-governance and workflow-selection refactor inside an existing multi-module repository
**Performance Goals**:
- Default local MCP E2E runs should avoid Docker, packaged distributions, and external model runtimes
- Enabling one heavy lane should not implicitly enable unrelated heavy lanes
- CI commands should make heavy-lane participation obvious from the Maven invocation itself
**Constraints**:
- No new Maven profile for MCP E2E lane selection
- No branch switching during this workstream
- Keep the current MCP E2E suites; only change how they are selected
- Remove legacy lane-selection properties in the same delivery rather than preserving a compatibility layer
- Preserve existing non-lane auxiliary settings such as `e2e.timezone`, `mcp.distribution.home`, and `mcp.llm.*`
**Scale/Scope**:
- `test/e2e/mcp` test configuration, runtime test classes, distribution smoke test, and LLM suites
- MCP-related GitHub workflow commands that currently rely on defaults or legacy lane properties
- Does not change MCP runtime business behavior or test assertions beyond execution selection

## Constitution Check

*GATE: Passes for this planning stage; no blocking constitution conflict identified.*

- **Specification-driven delivery**
  - This work is moving from reviewed `spec.md` into `plan.md` and `tasks.md` before implementation, which satisfies the constitution requirement that planning precede coding.
- **Product-boundary safety**
  - The feature only changes repository test selection and workflow commands. It does not alter MCP Proxy-first behavior, operator control, or workflow semantics.
- **Minimal and traceable change**
  - The intended change is limited to test configuration, conditional execution, and CI commands while reusing existing repository E2E patterns instead of introducing a new execution model.
- **Repository governance**
  - The plan stays within `AGENTS.md` and `CODE_OF_CONDUCT.md` expectations: reuse existing infrastructure, avoid duplicate configuration models, prefer scoped verification, and keep the change set reviewable.

## Project Structure

### Documentation (this feature)

```text
specs/004-mcp-e2e-enable-gating/
|-- spec.md
|-- research.md
|-- plan.md
|-- tasks.md
`-- checklists/
    `-- requirements.md
```

### Source Code (repository root)

```text
test/e2e/env/src/test/java/org/apache/shardingsphere/test/e2e/env/runtime/
`-- EnvironmentPropertiesLoader.java

test/e2e/mcp/src/test/resources/
`-- env/e2e-env.properties

test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/
|-- runtime/programmatic/
|-- runtime/production/
|-- llm/config/
|-- llm/suite/smoke/
|-- llm/suite/usability/
`-- support/

.github/workflows/
|-- jdk17-subchain-ci.yml
|-- mcp-build.yml
|-- mcp-llm-e2e.yml
`-- mcp-llm-usability-e2e.yml
```

**Structure Decision**:
Keep the shared lane model inside `test/e2e/mcp` as a dedicated MCP E2E configuration and condition layer, and let runtime tests, distribution smoke, and LLM suites delegate to that layer. Reuse `test/e2e/env` only as the shared property-loading mechanism, not as a reason to reshape MCP into the SQL E2E scenario matrix.

## Design Focus

### Shared configuration entry point

- Create one MCP E2E configuration owner that loads `env/e2e-env.properties` and overlays JVM system properties.
- Publish explicit predicates for:
  - `contract`
  - `production.h2`
  - `production.mysql`
  - `production.stdio`
  - `proxy.workflow`
  - `distribution`
  - `llm`
- Keep `e2e.timezone` untouched in the same module-local property file.

### Lane composition

- Treat `contract` and `production.h2` as the default baseline lanes.
- Treat MySQL, STDIO, proxy workflow, distribution, and LLM as opt-in lanes.
- Model cross-axis classes explicitly, especially the STDIO plus MySQL smoke case, so it runs only when both relevant heavy-lane decisions are enabled.

### Legacy gate removal

- Remove `@EnabledIfSystemProperty(named = "mcp.distribution.smoke.enabled", ...)`.
- Remove `mcp.llm.e2e.enabled` as the top-level LLM selector.
- Preserve lane-specific non-selector inputs such as `mcp.distribution.home` and `mcp.llm.base-url`.

### CI alignment

- Let the default MCP module test steps rely on the baseline defaults.
- Update distribution and LLM workflows to pass the new explicit lane-enable properties.
- Keep heavy-lane selection visible from workflow command lines.

## Implementation Slices

### Slice 1 - Shared MCP E2E lane model

- Add the MCP-specific configuration owner and shared lane predicates.
- Populate `env/e2e-env.properties` with the baseline-versus-opt-in defaults.

### Slice 2 - Runtime test-class adoption

- Move programmatic and H2 HTTP suites onto the baseline lanes.
- Move MySQL, STDIO, and proxy workflow suites onto explicit heavy-lane predicates, including composite handling where transport and backend combine.

### Slice 3 - Distribution and LLM normalization

- Replace distribution and LLM legacy selectors with the shared MCP lane model.
- Keep distribution-home and LLM runtime settings as separate auxiliary inputs.

### Slice 4 - Workflow and verification alignment

- Update all affected MCP workflows to use the explicit lane-enable switches.
- Verify default baseline behavior and representative heavy-lane commands.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| None | N/A | N/A |
