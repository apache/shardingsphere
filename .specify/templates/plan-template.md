# Implementation Plan: [FEATURE]

**Branch**: `[###-feature-name]` | **Date**: [DATE] | **Spec**: [link]
**Input**: Feature specification from `/specs/[###-feature-name]/spec.md`
**Note**: This template is filled in by the `/speckit.plan` command.

## Summary

[Extract from feature spec: primary requirement and technical approach]

## Technical Context

**Language/Version**: [e.g. Java 17 or NEEDS CLARIFICATION]
**Primary Dependencies**: [e.g. ShardingSphere Proxy, DistSQL, MCP runtime]
**Storage**: [if applicable]
**Testing**: [e.g. JUnit 5, Mockito, module-scoped Maven tests]
**Target Platform**: [e.g. Linux server, Proxy runtime]
**Project Type**: [e.g. Java service, backend feature]
**Performance Goals**: [domain-specific metric or NEEDS CLARIFICATION]
**Constraints**: [domain-specific constraints]
**Scale/Scope**: [domain-specific scope]

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

[Gates determined by `.specify/memory/constitution.md`]

## Project Structure

### Documentation (this feature)

```text
specs/[###-feature]/
|-- plan.md
|-- research.md
|-- data-model.md
|-- quickstart.md
|-- contracts/
`-- tasks.md
```

### Source Code (repository root)

```text
[Document the relevant repository paths for this feature]
```

**Structure Decision**: [Document the selected structure and reference the real directories]

## Complexity Tracking

> Fill only if Constitution Check has violations that must be justified.

| Violation | Why Needed | Simpler Alternative Rejected Because |
|-----------|------------|-------------------------------------|
| [example] | [reason]   | [reason]                            |
