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

# Feature Specification: MCP Contract and E2E Gap Triage

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-14
**Status**: Evidence-Mapped Draft
**Input**: User requests that the mcp-builder findings for `shardingsphere-mcp` and `test/e2e/mcp` be managed through Speckit, with no branch switching.

## Goal

Turn the latest MCP contract, functional, implementation, and E2E-test findings into a traceable requirements package before any implementation work starts.

This package is an intake and requirement-triage layer.
It records what is non-standard, missing, insufficiently tested, or over-tested, then assigns each gap to a clear requirement and future verification gate.
It does not implement fixes, change CI, or mark earlier scorecards complete.

## Source Baseline

Implementation tasks derived from this package must use the repository-selected MCP protocol baseline as the source of truth.
Official MCP `latest` currently resolves to MCP Specification `2025-11-25`.
If the repository protocol target changes, this package must be updated before coding.
Registry metadata is tracked separately: the official MCP Registry currently uses a `server.json` schema dated `2025-12-11`.
That registry schema date is not treated as protocol-baseline drift.

Official source pages used for this draft:

- Lifecycle and initialized notification:
  https://modelcontextprotocol.io/specification/2025-11-25/basic/lifecycle
- Streamable HTTP transport, `Accept`, `Origin`, session, DELETE, and protocol-version behavior:
  https://modelcontextprotocol.io/specification/2025-11-25/basic/transports
- Tool descriptors, input/output schemas, structured content, ResourceLink, and tool error channels:
  https://modelcontextprotocol.io/specification/2025-11-25/server/tools
- Resources, resource templates, subscriptions, and resource errors:
  https://modelcontextprotocol.io/specification/2025-11-25/server/resources
- Authorization:
  https://modelcontextprotocol.io/specification/2025-11-25/basic/authorization
- Elicitation, including form mode and URL mode:
  https://modelcontextprotocol.io/specification/2025-11-25/client/elicitation
- Completion:
  https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/completion
- Prompts:
  https://modelcontextprotocol.io/specification/2025-11-25/server/prompts
- Logging:
  https://modelcontextprotocol.io/specification/2025-11-25/server/utilities/logging
- Cancellation and progress:
  https://modelcontextprotocol.io/specification/2025-11-25/basic/utilities/cancellation
  https://modelcontextprotocol.io/specification/2025-11-25/basic/utilities/progress
- Tasks:
  https://modelcontextprotocol.io/specification/2025-11-25/basic/utilities/tasks
- Roots and sampling:
  https://modelcontextprotocol.io/specification/2025-11-25/client/roots
  https://modelcontextprotocol.io/specification/2025-11-25/client/sampling
- Registry and `server.json`:
  https://modelcontextprotocol.io/registry/about

Relevant existing packages:

- `.specify/specs/012-mcp-scorecard-perfect-100/`: scorecard and complete OAuth validation gate.
- `.specify/specs/013-mcp-protocol-field-standardization/`: descriptor field naming and canonical public enum casing.
- `.specify/specs/014-mcp-standard-and-e2e-hardening/`: accepted descriptor semantics, output-schema validation, distribution, and PR E2E hardening work.
- `.specify/specs/015-mcp-protocol-api-generalization/`: protocol/domain boundary, completion generalization, unsupported-target errors, ResourceLink ownership, and planner-schema cleanup.
- `e2e-test-disposition.md`: class-by-class E2E disposition and missing-test target map for this package.
- `finding-ledger.md`: mcp-builder finding IDs, owner package, affected paths, and closure evidence state.
- `source-path-evidence.md`: official-source baseline, current code evidence, and exact target layers for each finding.
- `tasks.md`: ordered implementation tasks, verification gates, adjacent owner handoffs, and recommended first slice.

This package does not supersede those packages.
It is the current issue inventory and requirement index.
When an item is already owned by 012, 013, 014, or 015, future tasks should update that owner instead of duplicating implementation.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Speckit files are maintained manually because the standard feature creation flow can change branches.
- Existing dirty worktree changes are preserved and must not be reverted.
- This requirement pass changes documentation only.
- Implementation tasks must follow `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md`.
- No issue may be closed by prose alone; closure requires a command, contract snapshot, code review artifact, or E2E evidence.
- Side-effecting MCP workflows must preserve explicit preview and operator approval boundaries.

## User Scenarios and Testing

### User Story 1 - MCP Clients Get Standard Transport and Security Behavior (Priority: P1)

A generic MCP client should be able to initialize and use the server without ShardingSphere-specific HTTP or authorization surprises.
Transport headers, session lifecycle, OAuth/token handling, origin policy, and secret collection must match the selected MCP baseline and fail closed.

**Why this priority**: Protocol and security drift can make the server unsafe or unusable for standards-compliant clients.

**Independent Test**: Run Streamable HTTP and STDIO contract tests that exercise initialize, initialized notification, required headers, token failures,
origin handling, session cleanup, and representative tool calls.

**Acceptance Scenarios**:

1. **Given** a Streamable HTTP client omits or sends an unsupported `Accept` header, **When** it calls an MCP endpoint, **Then** the server responds
   according to the selected MCP baseline rather than silently accepting an invalid negotiation.
2. **Given** remote HTTP is enabled, **When** the request carries a non-loopback or untrusted `Origin`, **Then** the origin policy rejects it unless an
   explicit allowlist permits it.
3. **Given** a client needs credentials or secrets, **When** the server guides the user, **Then** secrets are collected through approved configuration, OAuth,
   environment, or out-of-band flows rather than an unsafe form-elicitation contract.
4. **Given** a client completes `initialize`, **When** the initialized notification is required by the selected lifecycle, **Then** E2E helpers and product documentation perform or document that step.

---

### User Story 2 - Tool Contracts Are Enforced, Not Merely Described (Priority: P1)

An MCP client should be able to trust `inputSchema`, `outputSchema`, enum values, required fields, and resource URI templates.
Invalid arguments should fail consistently before they reach business logic, and structured results should validate against the declared contract.

**Why this priority**: MCP clients use schemas for planning and self-correction; schema drift leads to invalid calls, brittle clients, and false E2E confidence.

**Independent Test**: Generate descriptor snapshots and run tool-call tests with valid and invalid arguments for representative metadata, SQL, workflow, encrypt, and mask tools.

**Acceptance Scenarios**:

1. **Given** a tool declares an enum such as `object_types`, **When** a client sends a value using the wrong canonical case or unsupported value, **Then**
   validation fails with actionable recovery guidance.
2. **Given** a tool declares `outputSchema`, **When** the handler returns `structuredContent`, **Then** the payload validates against required fields,
   object boundaries, array item schemas, and `additionalProperties` policy.
3. **Given** a resource template contains URI variables, **When** clients pass encoded names with spaces, slashes, or reserved characters, **Then** the server decodes and validates them consistently.

---

### User Story 3 - E2E Tests Prove User-Visible Product Paths (Priority: P1)

A reviewer should see E2E evidence for the protocol paths and product features that real MCP clients use.
The default H2 path is useful, but it cannot be the only signal for release readiness.

**Why this priority**: Missing E2E coverage can hide regressions in remote HTTP, STDIO, session isolation, distribution startup, MySQL, and live model usage.

**Independent Test**: Open the MCP E2E matrix and verify each required running mode has a command, enablement flag, runtime budget, artifact, and pass/fail expectation.

**Acceptance Scenarios**:

1. **Given** MCP-related paths change, **When** PR validation runs, **Then** the required HTTP, STDIO, MySQL, distribution, remote security, and selected LLM usability paths have explicit evidence.
2. **Given** a session is deleted or expires, **When** the client retries tools, resources, or transactions under that session, **Then** state is cleaned up and failures are deterministic.
3. **Given** workflow plans are session-scoped, **When** two clients create or complete plans concurrently, **Then** plan IDs, completions, and approval state remain isolated.

---

### User Story 4 - Test Coverage Is Signal-Rich and Maintainable (Priority: P2)

Maintainers should know which tests are necessary, which are missing,
and which existing tests can be preserved as support coverage or reduced because they duplicate lower-level behavior.

**Why this priority**: A large E2E suite that mixes product proof with helper-unit checks becomes expensive without improving release confidence.

**Independent Test**: Classify every MCP E2E class as product-path E2E, protocol contract E2E,
fixture support unit, model usability, governance artifact, or reduction/downscope candidate.

**Acceptance Scenarios**:

1. **Given** a test only asserts pure helper string construction or static fixture behavior, **When** it does not start MCP or cross a real transport boundary,
   **Then** it is preserved as support coverage until a narrower unit-test or harness module exists.
2. **Given** a golden contract snapshot duplicates descriptor unit tests, **When** it adds no client-visible transport evidence, **Then** it is reduced to one
   canonical contract gate.
3. **Given** an E2E assertion checks exact error prose, **When** the stable contract is an error code or structured recovery field, **Then** the test asserts
   the stable contract rather than volatile wording.

---

### User Story 5 - Functional Scope Is Explicit (Priority: P2)

Users and reviewers should know which ShardingSphere features and MCP optional capabilities are implemented, intentionally unsupported, or future work.
Missing features should not look accidentally supported.

**Why this priority**: MCP clients and product reviewers need a reliable capability surface and roadmap, especially for optional MCP features and ShardingSphere governance features.

**Independent Test**: Compare the advertised capabilities, resource catalog, prompts, and docs against the implemented handlers and tests.

**Acceptance Scenarios**:

1. **Given** optional MCP features such as logging, progress, cancellation, roots, sampling, subscriptions, or resource `listChanged` are not implemented,
   **When** clients inspect capabilities, **Then** the server does not advertise them and docs classify them as unsupported or future work.
2. **Given** ShardingSphere feature areas such as sharding, readwrite-splitting, shadow, traffic, DB discovery, mode governance, and observability are not
   exposed through MCP, **When** users inspect docs, **Then** their status is explicitly scoped instead of implied by the product name.

## Issue Inventory

### P0 - Non-Standard or Safety-Sensitive Gaps

- **MCE-P0-001 Secret-safe elicitation**: Any path that asks for passwords, tokens, or connection secrets through MCP elicitation forms must be removed, replaced, or explicitly proven safe.
  Closure evidence: descriptor review, transport test, and docs proving approved secret channels.
  Form mode must not request secrets; sensitive interactions use URL mode, OAuth, environment, or out-of-band configuration.
- **MCE-P0-002 Strict Streamable HTTP negotiation**: Missing or unsupported `Accept` behavior must match the selected MCP baseline.
  Tests must not bless server-side defaulting if the baseline requires explicit negotiation.
- **MCE-P0-003 Remote HTTP origin policy**: Loopback-only origin checks are insufficient for remote authenticated deployments.
  Remote mode needs an explicit allowlist, documented deployment default, and negative E2E coverage.
- **MCE-P0-004 Authorization fail-closed gate**: OAuth or bearer-token validation must reject inactive, expired, wrong-issuer, wrong-audience/resource,
  insufficient-scope, and introspection-failure cases without token passthrough.
  If this remains owned by `.specify/specs/012-mcp-scorecard-perfect-100/`, this package only tracks linkage.

### P1 - Contract and Implementation Gaps

- **MCE-P1-001 Input-schema enforcement**: Tool arguments must be validated against declared schemas, including required fields, enum values, type mismatches, and unknown-field policy.
- **MCE-P1-002 Output-schema strictness**: Declared `outputSchema` must cover required success fields, array item shape, nested object shape, and additional-property policy.
  Tools that cannot meet this must omit the schema until they can.
- **MCE-P1-003 Canonical enum casing**: `object_types` and similar enums need one canonical public casing across descriptor schema, recovery payloads, E2E clients, scripts, and docs.
- **MCE-P1-004 Lifecycle initialized evidence**: HTTP and STDIO helpers must prove the full initialize lifecycle expected by the selected protocol baseline.
- **MCE-P1-005 Positive completion coverage**: Completion tests must include successful database, schema, table, column, algorithm, and workflow-plan candidates, not only empty or error cases.
- **MCE-P1-006 Resource URI encoding boundaries**: URI templates and handlers already have unit-level encoding evidence,
  but product E2E must still cover encoded names, missing variables, reserved characters, and unsupported values.
- **MCE-P1-007 Session and transaction isolation**: Multi-session transaction, workflow-plan, completion, and DELETE cleanup behavior need deterministic tests.
- **MCE-P1-008 Registry manifest schema**: `mcp/server.json` validation must cover the official registry schema,
  package transports, release versions, OCI identifiers, and publication-time rewrite behavior.
  The registry schema date `2025-12-11` is separate from the MCP protocol baseline `2025-11-25`.

### P2 - Missing Functional Scope Decisions

- **MCE-P2-001 Optional MCP capabilities**: Logging, progress, cancellation, roots, sampling, subscriptions, and resource `listChanged` must be explicitly
  unsupported, implemented, or assigned to future packages.
- **MCE-P2-002 ShardingSphere feature breadth**: Sharding, readwrite-splitting, shadow, traffic, database-discovery, mode governance, and observability
  must be documented as supported, unsupported, or future MCP scope.
- **MCE-P2-003 Prompt/resource catalog clarity**: Prompt and resource names must distinguish official MCP objects from ShardingSphere product guidance and catalogs.
- **MCE-P2-004 Error recovery stability**: Tests should assert structured error codes and recovery actions rather than volatile display text.

### P2 - E2E Suite Rationalization

- **MCE-E2E-001 Missing required tests**: Add or confirm tests for strict Accept negotiation, remote origin allowlists, full initialize lifecycle,
  input-schema rejection, URI encoding, positive completion, session deletion, transaction cleanup, and registry manifest schema.
- **MCE-E2E-002 PR gate visibility**: Every opt-in running mode must have visible PR or release-gate evidence, skip reasons, and runtime budgets.
- **MCE-E2E-003 Over-specific golden snapshots**: Reduce snapshots that duplicate unit-level descriptor checks without proving transport-visible behavior.
- **MCE-E2E-004 Helper-only tests in E2E**: Keep pure helper, fixture, path-construction,
  and report-writing tests as support coverage until a narrower harness-support module exists.
- **MCE-E2E-005 Brittle prose assertions**: Replace exact display-message assertions with stable codes, fields, and machine-readable recovery actions.
- **MCE-E2E-006 Duplicated protocol-path coverage**: Keep one canonical transport contract for shared behavior.
  Add per-transport tests only where HTTP, STDIO, or distribution behavior can actually diverge.

## Requirements

### Functional Requirements

- **MCE-FR-001**: The requirement package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands or scripts.
- **MCE-FR-002**: Each recorded issue MUST have a severity, owner package, affected path,
  official source, expected behavior, and closure evidence before implementation tasks are created.
- **MCE-FR-003**: P0 issues MUST be resolved or explicitly reassigned to an active Speckit owner before any final score or release-readiness claim.
- **MCE-FR-004**: Secret-bearing input MUST NOT be requested through unsafe MCP elicitation or stored in model-visible structured payloads.
- **MCE-FR-005**: Streamable HTTP header negotiation MUST follow the selected MCP baseline, including negative tests for missing or unsupported headers.
- **MCE-FR-006**: Remote HTTP origin policy MUST be explicit, configurable, documented, and negatively tested.
- **MCE-FR-007**: HTTP authorization MUST fail closed and MUST NOT pass raw bearer tokens to downstream databases or logs.
- **MCE-FR-008**: Tool `inputSchema` validation MUST be enforced consistently before business handlers use arguments.
- **MCE-FR-009**: Tool `outputSchema` validation MUST prove success payloads match declared schemas, or the schema MUST be removed until conformance exists.
- **MCE-FR-010**: Public enum values MUST have one canonical casing across descriptors, clients, scripts, tests, and recovery payloads.
- **MCE-FR-011**: Lifecycle tests MUST include the selected protocol's required initialize and initialized-notification behavior for HTTP and STDIO where applicable.
- **MCE-FR-012**: Completion coverage MUST include positive and negative cases for metadata, feature algorithms, and workflow plan IDs.
- **MCE-FR-013**: Resource URI handling MUST be covered for valid encoded values, invalid encoded values, missing variables, and unsupported resources.
- **MCE-FR-014**: Session and transaction cleanup MUST be tested across isolated clients, explicit session deletion, and failure recovery.
- **MCE-FR-015**: `mcp/server.json` validation MUST cover schema, release version hygiene, package transport mapping, and OCI publication metadata.
- **MCE-FR-016**: Optional MCP capabilities MUST be either implemented with tests or absent from advertised capabilities and documented as unsupported or future work.
- **MCE-FR-017**: ShardingSphere feature areas not exposed through MCP MUST be explicitly classified as unsupported or future scope.
- **MCE-FR-018**: E2E tests MUST be classified by value and disposition.
  Evidence values include protocol contract, product-path runtime, distribution/release, LLM usability, fixture unit, and governance artifact.
  Dispositions include keep-E2E, keep-support, keep-governance, reduce-candidate, and downscope candidate.
- **MCE-FR-019**: E2E tests that do not cross a transport, runtime, distribution, or model boundary SHOULD stay as support coverage until a narrower test module exists.
- **MCE-FR-020**: Tests MUST prefer stable structured fields, error codes, and recovery actions over exact human-display text.
- **MCE-FR-021**: Existing 012, 013, 014, and 015 requirements MUST be referenced rather than duplicated when they already own a gap.
- **MCE-FR-022**: Documentation-only Speckit work does not require Maven execution; future implementation work MUST run scoped tests and Checkstyle or Spotless gates for touched modules.

### Key Entities

- **Gap Record**: A single standards, function, implementation, or test-quality issue with severity, owner, affected paths, expected behavior, and evidence gate.
- **Owner Package**: The Speckit package that will own implementation tasks for a gap, such as 012, 013, 014, 015, or this package if no prior owner exists.
- **E2E Test Disposition**: The decision to keep, add, preserve as support, reduce, or downscope a test based on release evidence value.
  The class-level matrix lives in `e2e-test-disposition.md`.
- **Finding Ledger**: The owner map from each MCE finding to the Speckit package that owns implementation or tracking.
- **Protocol Contract Gate**: A repeatable assertion against MCP wire behavior, descriptor shape, schema validation, or lifecycle behavior.
- **Product Runtime Gate**: An E2E assertion that a real ShardingSphere MCP runtime, distribution, database, or LLM-facing flow behaves as shipped.

## Success Criteria

### Measurable Outcomes

- **MCE-SC-001**: Every issue in this package maps to an owner package and a closure evidence type.
- **MCE-SC-002**: P0 items have no unowned gaps before implementation starts.
- **MCE-SC-003**: The E2E suite has a documented disposition for every class under `test/e2e/mcp/src/test/java`.
- **MCE-SC-004**: Required missing E2E tests are listed with their target behavior, transport/runtime, and pass/fail contract.
- **MCE-SC-005**: Deletion/downscope candidates are listed with the lower-level test or artifact that preserves coverage.
- **MCE-SC-006**: No final quality, release, or scorecard claim is made without command, artifact, or contract evidence.

## Assumptions

- The current branch remains `001-shardingsphere-mcp`.
- This package starts requirements management only; implementation tasks will be created after ownership and evidence mapping are reviewed.
- Adjacent Speckit packages may already close some items; this package records linkage instead of redoing their work.
- Source-path evidence has been mapped for implementation planning, but no production implementation has started.
- H2 stays the fast deterministic default runtime, while MySQL, STDIO, distribution, remote HTTP, and LLM paths need explicit opt-in or CI evidence.
