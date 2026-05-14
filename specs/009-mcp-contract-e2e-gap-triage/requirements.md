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

# Requirements: MCP Contract and E2E Gap Triage

## Source

This is the repo-visible handoff for `.specify/specs/016-mcp-contract-e2e-gap-triage/`.

Canonical Speckit files:

- `.specify/specs/016-mcp-contract-e2e-gap-triage/spec.md`
- `.specify/specs/016-mcp-contract-e2e-gap-triage/finding-ledger.md`
- `.specify/specs/016-mcp-contract-e2e-gap-triage/e2e-test-disposition.md`
- `.specify/specs/016-mcp-contract-e2e-gap-triage/source-path-evidence.md`
- `.specify/specs/016-mcp-contract-e2e-gap-triage/tasks.md`
- `.specify/specs/016-mcp-contract-e2e-gap-triage/checklists/requirements.md`

## Goal

Use Speckit to manage the latest mcp-builder findings for `shardingsphere-mcp` and `test/e2e/mcp` without switching branches.

The current pass starts the requirements document.
It records protocol, contract, functional, implementation, missing-test, and over-testing issues.
It intentionally does not implement fixes or mark the suite release-ready.
Protocol evidence is locked to MCP Specification `2025-11-25`.
Registry metadata evidence is tracked separately against the official `server.json` schema dated `2025-12-11`.

## Branch Constraint

- Current branch remains `001-shardingsphere-mcp`.
- Do not run `git switch`, `git checkout`, branch creation scripts, or Speckit commands that switch or create branches.
- Preserve unrelated dirty files in the working tree.

## Existing Owner Map

- `.specify/specs/012-mcp-scorecard-perfect-100/` owns the scorecard and complete OAuth validation gate.
- `.specify/specs/013-mcp-protocol-field-standardization/` owns descriptor field naming and canonical public enum casing.
- `.specify/specs/014-mcp-standard-and-e2e-hardening/` owns accepted descriptor semantics, output-schema validation, distribution, and PR E2E hardening work.
- `.specify/specs/015-mcp-protocol-api-generalization/` owns protocol/domain API boundaries, completion generalization, unsupported-target errors, ResourceLink ownership, and planner schema cleanup.
- `.specify/specs/016-mcp-contract-e2e-gap-triage/` is the new issue inventory and requirement index.

## Priority Requirements

### P0 - Protocol or Safety

- **MCE-P0-001 Secret-safe elicitation**: Secrets must not be requested through unsafe MCP form elicitation or exposed in model-visible structured payloads.
- **MCE-P0-002 Strict Streamable HTTP negotiation**: Missing or unsupported `Accept` behavior must follow the selected MCP baseline; tests must not bless invalid negotiation.
- **MCE-P0-003 Remote HTTP origin policy**: Remote HTTP requires an explicit allowlist or documented fail-closed default with negative tests.
- **MCE-P0-004 Authorization fail-closed gate**: OAuth and bearer-token validation must reject inactive, expired, wrong-issuer, wrong-audience/resource,
  insufficient-scope, and introspection-failure cases.

### P1 - Contract and Implementation

- **MCE-P1-001 Input-schema enforcement**: Tool arguments must be validated for required fields, types, enum values, and unknown fields before handlers use them.
- **MCE-P1-002 Output-schema strictness**: Declared `outputSchema` must match success `structuredContent`; non-conforming tools must remove the schema until fixed.
- **MCE-P1-003 Canonical enum casing**: `object_types` and similar enums need one public casing across descriptors, clients, scripts, recovery payloads, and tests.
- **MCE-P1-004 Lifecycle initialized evidence**: HTTP and STDIO helpers must prove the selected MCP initialize lifecycle, including initialized notification when required.
- **MCE-P1-005 Positive completion coverage**: Completion tests must cover successful metadata, algorithm, and workflow-plan suggestions.
- **MCE-P1-006 Resource URI encoding boundaries**: Resource templates already have unit-level encoding evidence;
  product E2E must cover encoded names, reserved characters, missing variables, and unsupported values.
- **MCE-P1-007 Session and transaction isolation**: Multi-session workflow, completion, transaction, and DELETE cleanup behavior needs deterministic tests.
- **MCE-P1-008 Registry manifest schema**: `mcp/server.json` validation must cover official registry schema shape,
  package transports, release versions, OCI identifiers, and publication-time rewrite behavior.

### P2 - Functional Scope

- **MCE-P2-001 Optional MCP capabilities**: Logging, progress, cancellation, roots, sampling, subscriptions, and resource `listChanged` must be implemented, absent, or documented as future work.
- **MCE-P2-002 ShardingSphere feature breadth**: Sharding, readwrite-splitting, shadow, traffic, DB discovery, mode governance, and observability must be scoped explicitly.
- **MCE-P2-003 Prompt/resource catalog clarity**: Catalog and prompt names must distinguish official MCP objects from ShardingSphere product guidance.
- **MCE-P2-004 Error recovery stability**: Stable error codes and structured recovery actions should be the test contract, not display prose.

## E2E Test Requirements

Necessary tests that must be added, confirmed, or assigned to an existing owner:

- Strict HTTP `Accept` negotiation and invalid media-type behavior.
- Remote HTTP origin allowlist and negative origin cases.
- Full initialize lifecycle for HTTP and STDIO.
- Input-schema rejection before handler execution.
- Resource URI encoding and missing-variable boundaries.
- Positive completion for metadata, algorithm, and workflow plan IDs.
- Session deletion, transaction cleanup, and cross-session isolation.
- `mcp/server.json` schema and release-publication validation.

Tests to reduce, preserve, or downscope after evidence mapping:

- Golden snapshots that duplicate unit-level descriptor checks without transport-visible value.
- Pure fixture, helper, path-construction, report-writing, or string-building tests living in E2E scope
  are preserved as support coverage until a narrower harness-support module exists.
- Exact human-message assertions where structured error codes or recovery fields are the stable contract.
- Duplicated per-transport scenarios where one canonical protocol contract is enough and the transport cannot diverge.

## Completion Rule

Before implementation starts:

- Every gap must have severity, owner package, affected path, expected behavior, and closure evidence.
- Every P0 gap must be owned by an active Speckit package.
- The E2E suite must have a class-by-class disposition matrix.
- Missing tests must be split into unit, integration, E2E, or release-gate targets.
- No quality, scorecard, or release-readiness claim may be closed by prose alone.

## Current Status

- Speckit package created as a draft requirements index.
- Finding ledger created with initial owner state.
- E2E class-by-class disposition matrix created.
- Source-path evidence mapping completed with official-source and local-path targets.
- Final owner assignment completed for packages 012, 013, 014, 015, and 016.
- Implementation task generation completed.
- First implementation slice completed for strict HTTP `Accept` negotiation.
- Second implementation slice completed for remote HTTP `Origin` allowlist configuration and validator wiring.
- Production and E2E test source changed for T021/T025 and T022/T026; CI workflow, generated files, and runtime configuration were not changed.
- Next step is executing the remaining P0 secret-safe elicitation slice from `tasks.md`, recommended as T020/T024.
