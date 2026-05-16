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

# Feature Specification: MCP Configuration Input Validation Alignment

**Feature Branch**: `001-shardingsphere-mcp`  
**Created**: 2026-05-16  
**Status**: Draft  
**Input**: Move MCP YAML configuration and corresponding configuration object input validation that is suitable for the validation framework into validation-driven constraints.
Limit scope to MCP bootstrap configuration models and their direct runtime configuration counterparts.
Do not switch branches.

## Branch Constraint

- Work must remain on the existing `001-shardingsphere-mcp` branch.
- Speckit branch creation or branch switching commands are forbidden for this requirement package.
- This specification is manually maintained under `.specify/specs/021-mcp-validation-framework-alignment/` and mirrored by `specs/012-mcp-validation-framework-alignment/requirements.md`.

## Clarifications

### Session 2026-05-16

- Scope includes directly movable validation checks and checks that are movable after adding custom constraints or adjusting configuration DTOs.
- Scope excludes implementation in this Speckit pass; the output of this pass is requirement, plan, task, and scope mapping documentation.
- Scope is limited to `mcp/bootstrap` YAML configuration DTOs, `mcp/bootstrap` configuration objects, and the direct runtime database configuration object created from YAML.
- A counterpart configuration class is in scope when it is created directly by an MCP YAML configuration swapper, even if the class lives outside `mcp/bootstrap`.
- Descriptor catalog, registry metadata, tool/resource/prompt schemas, runtime request validation, and transport header validation are outside this requirement package.

## User Scenarios & Testing

### User Story 1 - Consistent YAML configuration validation before swapping (Priority: P1)

As an MCP configuration maintainer, I want invalid YAML configuration input to be rejected before swappers construct `MCPLaunchConfiguration` and its nested configuration objects.
Errors should be reported consistently, and swappers should stay focused on conversion and placeholder resolution.

**Independent Test**: Feed invalid launch, transport, HTTP, STDIO, OAuth introspection, and runtime database YAML DTOs into the existing validation entry points.
Verify deterministic validation errors before the corresponding configuration object is used.

**Acceptance Scenarios**:

1. Given a missing launch or transport section, when YAML configuration is loaded, then validation reports the missing configuration property.
2. Given an HTTP transport with a blank host, invalid port, or endpoint path without a leading slash, when YAML validation runs, then validation reports the violated property.
3. Given a runtime database entry with missing required fields, when YAML validation runs, then validation reports the invalid runtime database input.

### User Story 2 - Configuration object input invariants are validated centrally (Priority: P1)

As an MCP runtime integrator, I want cross-field rules on launch, HTTP, OAuth introspection, and runtime database configuration objects to be centralized as configuration input validation.
This keeps launch-time guards consistent for YAML-originated and programmatic configuration input.

**Independent Test**: Construct invalid counterpart configuration objects or resolved YAML-derived configurations and validate them through the same configuration validation path.
Assert each failure is reported before transport startup.

**Acceptance Scenarios**:

1. Given both HTTP and STDIO transports enabled, when launch configuration input is validated, then validation rejects the ambiguous transport mode.
2. Given remote HTTP access without allowed origins or authorization, when HTTP configuration input is validated, then validation rejects the unsafe configuration.
3. Given both bearer token and OAuth introspection configured, when HTTP configuration input is validated, then validation rejects the conflicting authorization configuration.

### User Story 3 - Reviewable boundary for configuration-only scope (Priority: P2)

As a release reviewer, I want a traceable map that only includes YAML configuration and counterpart configuration input checks.
The map should explicitly exclude descriptor, registry, catalog, request, and runtime state validation.

**Independent Test**: Compare the implemented migration against `scope-map.md`; every listed candidate is either moved, explicitly retained with rationale, or deferred with a reason.

**Acceptance Scenarios**:

1. Given a migrated direct validation, when the corresponding swapper is inspected, then duplicate structural checks are removed while equivalent framework validation and tests exist.
2. Given a custom or DTO-adjustment validation, when implementation is inspected, then the external YAML shape is unchanged unless the specification explicitly records a compatible DTO adjustment.
3. Given descriptor catalog, registry metadata, tool schemas, HTTP headers, or session state checks, when implementation is inspected, then they are not part of this migration.

## Requirements

### Functional Requirements

- **FR-001**: The work MUST stay on branch `001-shardingsphere-mcp`; branch switching, branch creation, and Speckit commands that change branches MUST NOT be used.
- **FR-002**: Scope MUST be limited to YAML configuration DTOs under `mcp/bootstrap/.../config/yaml/config`, their swappers, and direct counterpart configuration classes.
- **FR-003**: Direct counterpart configuration classes in scope are launch, HTTP transport, STDIO transport, OAuth introspection, and runtime database configuration classes.
  YAML conversion ownership, not package location alone, determines whether a counterpart configuration class is in scope.
- **FR-004**: Existing MCP YAML validation entry points MUST remain the source of truth for YAML DTO validation, including nested DTO validation through `@Valid`.
- **FR-005**: Directly duplicated null, blank, range, and pattern checks MUST be migrated from swappers into configuration validation only after equivalent constraints and tests are present.
- **FR-006**: Direct migration scope MUST match `scope-map.md`.
  It includes launch transport presence, HTTP host/port/path, STDIO section presence, OAuth cache TTL, runtime database required fields, and runtime database collection presence.
- **FR-007**: Custom-constraint or DTO-adjustment scope MUST match `scope-map.md`.
  It includes typed runtime database map validation, exactly-one transport mode, HTTP remote-access safety, allowed origins, authorization requirements, and OAuth introspection consistency.
- **FR-008**: Counterpart configuration validation MUST cover direct construction inputs that are equivalent to YAML-derived required fields.
- **FR-009**: DTO adjustments MUST preserve the existing external YAML shape and public behavior unless a later implementation plan records a compatibility-safe transition.
- **FR-010**: Checks that depend on resolved environment placeholders MUST run after resolution or on counterpart configuration objects.
  Raw YAML constraints MUST NOT reject values that become valid only after placeholder resolution.
- **FR-011**: Descriptor catalog, registry metadata, tool/resource/prompt catalog, runtime request, HTTP header, session, and lifecycle validation MUST remain out of scope.
- **FR-012**: Error messages and property paths SHOULD become more framework-consistent, but tests MUST document any intentional message or path changes.
- **FR-013**: Each migrated production path MUST have scoped tests proving invalid input fails at the configuration validation layer and valid input still swaps or launches successfully.
- **FR-014**: Implementation MUST avoid broad refactors, dependency upgrades, generated-directory edits, and unrelated MCP behavior changes.

### Key Entities

- **YAML Configuration DTO**: A YAML-backed MCP bootstrap configuration object validated before swapper conversion.
- **Counterpart Configuration Object**: The runtime configuration object created from YAML and used by launcher or transport startup code.
- **Bean Validation Constraint**: A built-in or custom Jakarta validation rule attached to DTO fields, type use elements, map keys, or class-level invariants.
- **Swapper**: Conversion logic from YAML DTOs to runtime MCP configuration objects; after migration, swappers should not duplicate structural configuration validation.
- **Configuration Validator**: Validation attached to YAML DTOs or counterpart configuration objects before the MCP runtime starts.
- **Scope Map**: The traceability document that assigns every reviewed candidate to direct migration, custom/DTO migration, or intentional retention.

## Scope

### In Scope

- Structural YAML configuration validation that is deterministic from MCP bootstrap configuration DTO contents.
- Counterpart configuration object validation for launch and HTTP transport input invariants.
- Direct migration candidates using built-in constraints such as `@NotNull`, `@NotBlank`, `@NotEmpty`, `@Min`, `@Max`, `@Pattern`, `@Valid`, and type-use constraints.
- Custom constraints or class-level validators for DTO-local invariants that involve two or more fields.
- DTO adjustments or validation groups when a YAML configuration shape currently prevents nested validation.
- Targeted tests for migrated validations and valid configuration paths.

### Out of Scope

- Branch switching, branch creation, commits, pushes, or destructive git operations.
- MCP SDK upgrades, dependency upgrades, or unrelated module restructuring.
- Configuration file path resolution; it is loader input validation, not YAML configuration model or counterpart configuration validation.
- Descriptor catalog validation, registry metadata validation, tool/resource/prompt schema validation, and catalog-level semantics.
- Dynamic tool argument JSON-schema validation.
- HTTP header parsing, request/session state, transport lifecycle state, and request security checks that require runtime context.

## Success Criteria

- **SC-001**: Every candidate listed in `scope-map.md` has an implementation disposition: migrated directly, migrated through custom/DTO support, retained in domain validation, or explicitly deferred.
- **SC-002**: Swappers no longer contain duplicated structural validation for migrated configuration checks.
- **SC-003**: Validation failures are observable through the existing MCP YAML or counterpart configuration validation paths with deterministic property paths.
- **SC-004**: Existing valid MCP launch configuration examples continue to pass.
- **SC-005**: Scoped MCP tests, Checkstyle, and Spotless checks pass for every touched module during implementation.
