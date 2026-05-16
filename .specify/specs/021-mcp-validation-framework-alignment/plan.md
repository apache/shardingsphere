<!--
  Licensed to the Apache Software Foundation (ASF) under one or more
  contributor license agreements. See the NOTICE file distributed with
  this work for additional information regarding copyright ownership.
  The ASF licenses this file to You under the Apache License, Version 2.0
  (the "License"); you may not use this file except in compliance with
  the License. You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

  Unless required by applicable law or agreed to in writing, software
  distributed under the License is distributed on an "AS IS" BASIS,
  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  See the License for the specific language governing permissions and
  limitations under the License.
-->

# Implementation Plan: MCP Configuration Input Validation Alignment

**Branch**: `001-shardingsphere-mcp`  
**Spec**: `.specify/specs/021-mcp-validation-framework-alignment/spec.md`  
**Scope Map**: `.specify/specs/021-mcp-validation-framework-alignment/scope-map.md`  
**Input**: Move MCP YAML configuration and counterpart configuration input validations into validation-driven constraints.
Scope is limited to bootstrap configuration input.

## Technical Context

**Language/Version**: Java 21 MCP subchain  
**Primary Dependencies**: Jakarta Bean Validation / Hibernate Validator already used by MCP YAML validation, existing ShardingSphere YAML swappers and validators  
**Storage**: MCP YAML configuration files  
**Testing**: JUnit 5, Mockito where needed, module-scoped Maven test and style commands  
**Target Modules**: `mcp/bootstrap` and the direct `RuntimeDatabaseConfiguration` counterpart in `mcp/support`  
**Constraints**: No branch switching; no generated directory edits; no dependency upgrades; preserve external YAML shape  

## Framework Basis

- Project dependency detection: `mcp/pom.xml` uses `jakarta.validation-api` 2.0.2 and Hibernate Validator 6.2.5.Final.
- Hibernate Validator 6.2 is compatible with Jakarta Validation 2.0 and keeps the `javax.validation.*` package model.
- Bean Validation 2.0 supports built-in constraints such as `@NotEmpty`, `@NotBlank`, and `@PositiveOrZero`.
- Bean Validation 2.0 supports container element constraints and cascaded validation on generic container element types.
- Sources:
  - https://hibernate.org/validator/releases/6.2/
  - https://beanvalidation.org/2.0-jsr380/
  - https://beanvalidation.org/2.0/spec/

## Governance Check

- Use the existing branch `001-shardingsphere-mcp`; do not run `git switch`, `git checkout`, branch creation helpers, or Speckit branch commands.
- Keep changes incremental and module-scoped.
- Prefer built-in Bean Validation constraints before adding custom validators.
- Add custom constraints only for DTO-local invariants that cannot be expressed by built-in annotations.
- Keep descriptor, registry, request, and runtime state checks outside this configuration-only requirement.
- Keep post-placeholder checks on resolved counterpart configuration when raw YAML validation would lose required context.
- Treat direct YAML swapper output classes as in scope even when the class is outside `mcp/bootstrap`.
- Run scoped tests and style checks for touched modules during implementation.

## Project Structure

```text
.specify/specs/021-mcp-validation-framework-alignment/
|-- spec.md
|-- plan.md
|-- scope-map.md
|-- tasks.md
`-- checklists/
    `-- requirements.md

specs/012-mcp-validation-framework-alignment/
`-- requirements.md
```

## Source Areas

- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/config/`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/yaml/swapper/`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/config/`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/MCPRuntimeLauncher.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/metadata/jdbc/RuntimeDatabaseConfiguration.java`
- Corresponding `src/test/java` packages under the same modules.

## Execution Strategy

### Phase 1 - Baseline and Traceability

1. Confirm current branch remains `001-shardingsphere-mcp`.
2. Re-scan MCP validation-related code and align candidates with `scope-map.md`.
3. Convert the source map into a migration checklist for implementation.

### Phase 2 - Direct Configuration Constraint Migration

1. Add or verify built-in constraints on YAML configuration DTOs for required fields, blank strings, numeric ranges, path patterns, and non-empty collections.
2. Ensure existing validation entry points validate nested DTOs with `@Valid`.
3. Remove duplicate structural checks from configuration swappers only after tests cover the equivalent validation failure.

### Phase 3 - Configuration DTO Adjustments

1. Adjust `runtimeDatabases` typing or validation so each map entry can be validated as a runtime database configuration.
2. Preserve external YAML keys and map shape.
3. Keep unsupported-property handling explicit if DTO typing changes how unknown keys are detected.

### Phase 4 - Counterpart Configuration Input Validation

1. Centralize exactly-one transport validation for `MCPLaunchConfiguration`.
2. Centralize HTTP remote-access, allowed-origin, authorization, and OAuth introspection consistency validation for `HttpTransportConfiguration`.
3. Include required nested counterpart configuration objects and required resolved scalar fields for programmatic construction paths.
4. Keep checks that need resolved placeholders on counterpart configuration objects rather than raw YAML DTOs.

### Phase 5 - Explicitly Retained Outside Scope

1. Keep descriptor catalog, registry metadata, tool/resource/prompt, HTTP header, session, and runtime lifecycle checks outside this migration.
2. Document retained checks in `scope-map.md` when they could be mistaken for configuration input validation.

### Phase 6 - Verification

1. Run module-scoped tests for changed classes.
2. Run module-scoped Checkstyle and Spotless checks for touched modules.
3. Record any intentionally changed validation messages or property paths.

## Verification Bar

- Direct migrations require one invalid-input test per migrated configuration branch and one representative valid-input regression test.
- Custom/class-level configuration constraints require tests for both the rejected conflict and a valid neighboring case.
- DTO shape adjustments require YAML parsing coverage proving external keys remain compatible.
- Implementation completion requires scoped Maven commands with exit codes in the handoff.

## Risk Register

- **Message drift**: Bean Validation may change exception message text or property paths. Mitigate with focused assertions that document intended user-facing behavior.
- **Placeholder timing**: Raw YAML constraints can reject placeholders that would resolve later. Keep post-resolution blank checks outside raw DTO validation.
- **Over-migration**: Moving request, descriptor, or lifecycle checks into configuration validation can remove needed context. Keep these checks outside this requirement.
- **DTO compatibility**: Runtime database DTO typing can accidentally change YAML shape. Require parsing focused tests before removing old paths.
