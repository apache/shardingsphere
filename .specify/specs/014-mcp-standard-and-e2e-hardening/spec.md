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

# Feature Specification: MCP Standard and E2E Hardening

**Feature Branch**: `001-shardingsphere-mcp`
**Created**: 2026-05-13
**Status**: Clarified
**Input**: User requests that the latest mcp-builder review findings for the MCP modules and MCP E2E module be managed through Speckit without switching branches.

## Goal

Convert the latest MCP standards review into a traceable Speckit requirement package.
The package captures non-standard MCP behavior, protocol-contract drift, release-distribution gaps, and missing E2E evidence before implementation begins.

This feature is not a broad rewrite.
It defines small, reviewable improvements for the current MCP runtime, descriptors, distribution artifacts, and `test/e2e/mcp` coverage.

## Review Baseline

The 2026-05-13 mcp-builder review found these open gaps:

- Planning tools declare read-only and idempotent annotations while creating and saving workflow plan state.
- Public MCP tool names are generic and do not use the selected `database_gateway_` service prefix.
- Packaged distribution E2E starts Java directly and does not prove `bin/start.sh`, Docker entrypoint, or OCI package behavior.
- `mcp/server.json` still uses snapshot release metadata and needs release-time guardrails.
- Tool `execution.taskSupport` is modeled and emitted through adapters even though MCP Tasks are experimental and not fully implemented.
- Tool results expose machine-readable JSON, but operator-friendly response formatting is not yet defined.
- Output schema validation is not systematically applied to every tool's returned `structuredContent`.
- Default metadata pagination is larger than the mcp-builder context-budget recommendation.
- MySQL, STDIO, distribution, remote HTTP, and live LLM E2E running modes must run in PR when MCP-related paths change, with explicit runtime budgets.

## Clarifications

### Session 2026-05-13

- Q: Should old generic tool names remain as compatibility aliases? -> A: No.
  Compatibility is out of scope for this feature.
  Preferred names use the `database_gateway_` prefix and old generic names are removed rather than retained as aliases.
- Q: Is `execution.taskSupport` currently fully implemented? -> A: No.
  The current code has descriptor DTOs, YAML loading, and list-response adapter support for `execution.taskSupport`, but it does not expose the MCP `tasks` capability or implement task protocol methods.
  Because MCP Tasks are experimental and not in scope, this feature removes `execution.taskSupport` completely from production code paths, descriptor YAML, adapters, tests, and model-contract snapshots.
  It is not merely hidden from public descriptor output.
- Q: How should declared `outputSchema` be enforced? -> A: Use the recommended strict contract.
  Whenever a tool returns `structuredContent` and declares `outputSchema`, the returned structured payload must validate against that schema.
  If success and error payloads differ, the schema must describe an explicit success/error envelope or the tool must not return mismatched `structuredContent`.
- Q: Should tools support `response_format=markdown`? -> A: No.
  This feature keeps JSON `structuredContent` as the only authoritative machine contract and completely removes any `response_format` or Markdown-specific tool option, descriptor text, test fixture, and documentation.
  Existing MCP text content may continue to carry compact JSON text derived from the same payload, but Markdown response formatting must not remain as a supported or planned feature in this package.
- Q: Which E2E running modes should run in PR? -> A: All MCP E2E running modes.
  They should run only when the PR changes MCP-related paths, and should be split into bounded jobs so the wall-clock time is controlled.
  MCP-related paths include `mcp/**`, `distribution/mcp/**`, `test/e2e/mcp/**`, MCP Speckit packages, and CI files that control MCP E2E.
- Q: What does "E2E lane" mean? -> A: It is not an MCP official term.
  In this package it only meant an E2E running mode or test track: a named group of tests that share the same transport, database, packaging shape, or external dependency.
  To avoid ambiguity, this package uses "E2E running mode" instead of "lane" in requirements.

## Hard Constraints

- No task may run `git switch`, `git checkout`, branch creation scripts, or branch-changing Speckit commands.
- Speckit files are maintained manually because the standard feature creation flow can change branches.
- Existing dirty worktree changes are preserved and must not be reverted.
- Requirement work in this package changes documentation only.
- Implementation tasks must follow `AGENTS.md`, `CODE_OF_CONDUCT.md`, and `.specify/memory/constitution.md`.
- Side-effecting MCP behavior must preserve explicit operator preview and approval boundaries.
- No score or requirement may be marked complete without command, contract, or artifact evidence.

## User Scenarios and Testing

### User Story 1 - MCP Clients Receive Accurate Tool Semantics (Priority: P1)

An MCP client should be able to trust tool names, annotations, input schemas, output schemas, and optional execution metadata.
The client should not treat a state-changing workflow plan as a read-only idempotent operation.

**Why this priority**: MCP clients use descriptors and annotations to choose safe calls, cache behavior, retries, and human approval flows.

**Independent Test**: List tools through the MCP transport and verify every tool descriptor matches the actual runtime side effects and the chosen naming policy.

**Acceptance Scenarios**:

1. **Given** `database_gateway_plan_encrypt_rule` or `database_gateway_plan_mask_rule`, **When** the tool creates or saves a workflow plan, **Then** its annotations do not claim read-only or idempotent behavior.
2. **Given** a public ShardingSphere tool, **When** clients list tools, **Then** the preferred name uses the `database_gateway_` service prefix.
3. **Given** the current code has descriptor support for `execution.taskSupport`, **When** this feature is implemented, **Then** the related DTOs, YAML fields, adapter logic, fixtures, and tests are removed.
4. **Given** MCP Tasks are experimental, **When** this feature is reviewed, **Then** no production descriptor or model contract exposes `execution.taskSupport`.

---

### User Story 2 - Maintainers Can Prove Distribution and Release Readiness (Priority: P1)

A maintainer preparing MCP release artifacts should have E2E evidence that the assembled distribution, startup scripts, Docker entrypoint, and registry manifest work together.

**Why this priority**: A direct Java process smoke does not prove the user-facing distribution, Docker, or MCP registry package path.

**Independent Test**: Build `distribution/mcp`, start it through packaged scripts and Docker entrypoint paths, then initialize an MCP client and list tools/resources.

**Acceptance Scenarios**:

1. **Given** an assembled MCP distribution, **When** the packaged HTTP and STDIO smokes run, **Then** they start through `bin/start.sh` or the platform-equivalent packaged script.
2. **Given** the Docker image entrypoint, **When** `SHARDINGSPHERE_MCP_TRANSPORT=http` or `stdio` is configured, **Then** the process starts with the expected transport and exposes the MCP initialize flow.
3. **Given** a release candidate manifest, **When** release validation runs, **Then** `mcp/server.json` contains no `-SNAPSHOT` version or package identifier.
4. **Given** an OCI package entry in `mcp/server.json`, **When** the registry package smoke runs, **Then** the referenced artifact can initialize and expose the expected capability surface.

---

### User Story 3 - Reviewers See Complete E2E Evidence (Priority: P2)

A reviewer should see every MCP E2E running mode executed in PR whenever the PR changes MCP-related paths.

**Why this priority**: H2 default tests are valuable but do not prove all supported production paths or agent usability.

**Independent Test**: Open the E2E evidence matrix and verify every running mode has a PR job, path-change trigger, command, enablement flag, expected artifact, and pass/fail budget.

**Acceptance Scenarios**:

1. **Given** a PR changes MCP-related paths, **When** PR validation starts, **Then** default H2 HTTP, STDIO, MySQL, packaged distribution, Docker or registry smoke, remote HTTP, and live LLM running modes are scheduled.
2. **Given** a PR does not change MCP-related paths, **When** PR validation starts, **Then** MCP E2E jobs are skipped with an explicit path-filter reason.
3. **Given** live LLM tests run in PR, **When** artifacts are collected, **Then** they record native tool calls, approval boundaries, bounded scenario results, and elapsed time.
4. **Given** remote HTTP access is enabled in its PR job, **When** security evidence is evaluated, **Then** token, origin, protocol-version, session, and DELETE paths are tested in that mode.

---

### User Story 4 - Operators Get Stable Structured Outputs (Priority: P3)

An operator or model should receive stable JSON for automation without a second response format that can drift from `structuredContent`.

**Why this priority**: A single authoritative structured contract reduces ambiguity for MCP clients and keeps validation enforceable.

**Independent Test**: Call representative tools and verify JSON `structuredContent` remains stable, schema-conformant, and mirrored consistently in any text content.

**Acceptance Scenarios**:

1. **Given** a tool declares `outputSchema`, **When** it returns `structuredContent`, **Then** the payload validates against that schema before exposure.
2. **Given** a tool input schema is reviewed, **When** response-format fields are inspected, **Then** no `response_format` or Markdown-specific option is exposed.
3. **Given** metadata search has many results, **When** the client omits pagination settings, **Then** default page size stays within the selected context-budget limit.

## Edge Cases

- The selected service prefix is `database_gateway_`.
  It avoids the product-specific `shardingsphere_` prefix and avoids the implementation-specific `database_proxy_` prefix while still describing the server as a database entrypoint.
- Old generic tool names are not retained as compatibility aliases.
- The current code has descriptor and adapter support for `execution.taskSupport`, but no complete MCP Tasks capability.
  Because MCP Tasks are experimental and out of scope, `execution.taskSupport` support must be fully removed rather than hidden.
- If MCP Java SDK support for an official field is incomplete, the bootstrap adapter must own the gap and tests must prove the wire output.
- All MCP E2E running modes run in PR when MCP-related paths change.
  If Docker, MySQL, or LLM services are unavailable, the PR job must fail with actionable readiness diagnostics rather than silently skip the running mode.
- If a tool cannot guarantee schema-conformant `structuredContent`, it must omit `outputSchema` until conformance exists.
- `response_format` and Markdown response formatting are cleanup targets, not deferred features.
  No tool input schema, descriptor, test fixture, README, or Speckit task may keep them as supported or planned behavior.
  Structured JSON remains authoritative, and any text content must stay derived from the same payload.

## Requirements

### Functional Requirements

- **MSH-FR-001**: The requirement package MUST stay on branch `001-shardingsphere-mcp` and MUST NOT use branch-changing commands or scripts.
- **MSH-FR-002**: Planning tools that create, save, or mutate workflow plan state MUST NOT declare `readOnlyHint: true`.
- **MSH-FR-003**: Planning tools that generate new plan identifiers or non-repeatable session state MUST NOT declare `idempotentHint: true`.
- **MSH-FR-004**: Public ShardingSphere MCP tool names MUST use the `database_gateway_` service prefix for preferred names to avoid collisions in multi-server clients.
- **MSH-FR-005**: Old generic public tool names MUST NOT be retained as compatibility aliases.
- **MSH-FR-006**: Tool `execution.taskSupport` support MUST be removed from production DTOs, YAML descriptors, descriptor validation, descriptor catalog payloads, transport adapters, tests, and model-contract snapshots.
- **MSH-FR-006A**: The MCP `tasks` capability and task protocol methods MUST remain out of scope for this feature.
- **MSH-FR-007**: Tool `inputSchema` and `outputSchema` MUST remain official JSON Schema objects with root `type: object` when present.
- **MSH-FR-008**: Tool execution MUST validate returned `structuredContent` against declared `outputSchema` before exposing the result.
- **MSH-FR-009**: Tools that cannot validate returned `structuredContent` MUST omit `outputSchema`.
- **MSH-FR-010**: Tools MUST NOT keep or add `response_format` or Markdown-specific output options in this feature.
- **MSH-FR-011**: JSON `structuredContent` MUST remain the authoritative machine contract, and any text content MUST be derived from the same payload.
- **MSH-FR-012**: Metadata search default pagination MUST fit the agreed MCP context budget; the default page size SHOULD be no more than 50 unless justified by evidence.
- **MSH-FR-013**: Distribution E2E MUST include at least one script-level packaged startup path instead of only direct Java main invocation.
- **MSH-FR-014**: Distribution E2E MUST include Docker entrypoint behavior or a documented release-gate substitute with equivalent artifact evidence.
- **MSH-FR-015**: Release validation MUST fail if `mcp/server.json` contains snapshot versions or snapshot OCI package identifiers.
- **MSH-FR-016**: Release validation MUST prove every declared `mcp/server.json` package transport maps to a runnable artifact path.
- **MSH-FR-017**: PR validation MUST run all MCP E2E running modes when MCP-related paths change.
- **MSH-FR-018**: PR validation MUST skip MCP E2E running modes when no MCP-related path changed, and the skip reason MUST be visible in CI output.
- **MSH-FR-018A**: MySQL, STDIO, distribution, Docker or registry smoke, remote HTTP, and live LLM running modes MUST have repeatable PR commands, enablement flags, expected artifacts, and timeout budgets.
- **MSH-FR-018B**: MCP-related path filters MUST include `mcp/**`, `distribution/mcp/**`, `test/e2e/mcp/**`, `.specify/specs/*mcp*/**`, and CI workflow files that control MCP E2E.
- **MSH-FR-019**: Remote HTTP security coverage MUST include token, origin, protocol-version, session, and DELETE behavior when remote access is enabled.
- **MSH-FR-020**: Every standardization or E2E gap MUST map to at least one task and one measurable exit gate.
- **MSH-FR-021**: Documentation-only Speckit updates MUST NOT require Maven execution, but implementation changes MUST run the relevant scoped test and Checkstyle or Spotless gates.
- **MSH-FR-022**: Completion evidence MUST record command output, generated artifact paths, or reviewed contract snapshots; prose-only completion is insufficient.

### Key Entities

- **Tool Semantic Contract**: Tool name, title, annotations, input schema, output schema, execution metadata, and actual runtime side effects.
- **Distribution Evidence**: Proof that assembled scripts, Docker entrypoint, lib/plugin layout, and registry package metadata work as shipped.
- **E2E Running Mode**: A named group of tests that share the same transport, database, packaging shape, or external dependency, such as default H2 HTTP, STDIO, MySQL, packaged distribution, remote HTTP, Docker, or live LLM.
- **Structured Output Policy**: The rule that keeps JSON `structuredContent` authoritative and removes alternate output formats from the public tool contract.
- **MCP-Related Path Filter**: The PR path filter that decides whether MCP E2E jobs run for a given pull request.
- **Exit Gate**: The command, artifact, or contract snapshot needed to close a requirement.

## Success Criteria

### Measurable Outcomes

- **MSH-SC-001**: No planning tool descriptor falsely claims read-only or idempotent behavior while mutating workflow state.
- **MSH-SC-002**: Public tool naming uses the documented `database_gateway_` preferred prefix, with tests or golden contracts for the exposed names.
- **MSH-SC-003**: No production source file, descriptor YAML, test fixture, or model-contract snapshot keeps `execution.taskSupport`.
- **MSH-SC-004**: Every declared tool `outputSchema` is covered by representative success and error payload validation.
- **MSH-SC-005**: Packaged distribution E2E proves at least one script-level startup path and one Docker or equivalent registry package path.
- **MSH-SC-006**: Release validation rejects snapshot `mcp/server.json` metadata before registry publication.
- **MSH-SC-007**: The E2E matrix records PR jobs, path filters, commands, budgets, and artifacts for every MCP E2E running mode.
- **MSH-SC-008**: The requirements checklist has no failed items before implementation starts.
- **MSH-SC-009**: No source file, descriptor, test fixture, README, or Speckit task keeps `response_format=markdown` as supported or planned behavior.

## Assumptions

- MCP protocol compatibility targets the current repository choice rather than a moving `latest` protocol version.
- The current branch is `001-shardingsphere-mcp`.
- Existing MCP protocol field standardization work in `.specify/specs/013-mcp-protocol-field-standardization/` remains separate and may satisfy some descriptor-shape tasks.
- H2 remains the fast deterministic default running mode, while MySQL, STDIO, distribution, Docker, remote HTTP, and live LLM running modes run in PR only when MCP-related paths change.
