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

# Requirements: MCP AI-Native Perfect 100

## Requirement Contract

This document is the canonical requirement inventory for the strict `100/100` target.

The MCP can be called `100/100` only when every `MCP100-*` requirement below is implemented, verified, and reflected in the scorecard.

## P0 Requirements

### MCP100-P0-01: Universal Response Mode

Every public tool, resource, prompt, descriptor, and structured error response MUST expose or document a `response_mode`.

Allowed modes:

- `catalog`
- `runtime`
- `list`
- `detail`
- `search`
- `query`
- `preview`
- `executed`
- `planning`
- `manual_only`
- `validation`
- `recovery`
- `terminal`

Acceptance:

- Unknown response modes fail descriptor linting.
- Public examples and tests use the same mode names as runtime payloads.
- Error payloads use `recovery`, `validation`, or `terminal` instead of free-form strings.

### MCP100-P0-02: Deterministic Next Actions

Every `next_actions` entry MUST be machine-readable without relying on prose.

Required fields:

- `order`
- `type`
- `title`
- `requires_user_approval`

Conditional fields:

- `depends_on`, when the action is not valid until a prior action completes.
- `tool_name` and `arguments`, when the follow-up is executable by MCP.
- `resource_uri`, when the follow-up is a resource read.
- `question`, when the next action is to ask the user.
- completion reference fields, when the follow-up is an MCP completion request.

Compatibility:

- Existing legacy aliases such as `action_kind`, `target_tool`, `target_resource`, and `required_arguments` MAY remain, but they cannot replace the canonical fields.

Acceptance:

- `next_actions` without `order` fail tests.
- Actions that can mutate state MUST set `requires_user_approval=true`.
- Pure reads MUST set `requires_user_approval=false`.

### MCP100-P0-03: Large Metadata Continuation Semantics

Direct metadata list results MUST not leave capped data ambiguous.

Acceptance:

- If real pagination exists, responses expose page state and next page arguments.
- If real pagination does not exist, responses expose `continuation_mode=search_metadata`.
- `has_more` MUST describe the actual continuation model.
- Capped lists include a narrowed search action with prefilled arguments.

### MCP100-P0-04: Nearest-Resource Completion Recovery

Argument completion MUST guide the model to the nearest known resource.

Acceptance:

- Missing `database` points to database list/catalog.
- Missing `schema` points to schema list when `database` is known.
- Missing `table` points to table list when `database` and `schema` are known.
- Missing `column` points to table-column detail when table context is known.
- Missing `index` points to table-index detail when table context is known.
- Empty candidates distinguish `empty_scope` from `missing_context`.

### MCP100-P0-05: Deterministic Single-Schema Auto-Fill

When exactly one visible schema exists for a database, completion and workflow planning MUST be able to fill it deterministically.

Acceptance:

- The filled value is marked as inferred or server-defaulted.
- More than one visible schema returns a structured user question.
- Zero visible schemas returns a scoped empty-state recovery response.

### MCP100-P0-06: Not-Found and Empty-State Recovery

Not-found and empty-state responses MUST provide a next safe move.

Acceptance:

- Responses include a parent resource if enough context is known.
- Search retry arguments include the requested object token as `query`.
- Responses never fabricate child URIs for unresolved objects.
- The recovery response states whether the issue is `not_found`, `ambiguous`, `empty_scope`, or `missing_context` through `recovery_category`.

### MCP100-P0-07: Public Contract Drift Guard

A deterministic test or lint gate MUST encode all P0 contracts.

Acceptance:

- The gate runs without live databases or live LLMs.
- The gate fails on missing response modes, malformed next actions, unknown continuation modes, and missing recovery categories.
- The gate is documented in `quickstart.md`.

## P1 Requirements

### MCP100-P1-01: Secret-Free Readiness

MCP MUST expose a readiness or health path that is useful to models and operators without leaking credentials.

Acceptance:

- Credentials, tokens, JDBC URLs with secrets, and raw properties are redacted.
- Readiness distinguishes server availability from runtime metadata readiness.
- The response is usable before executing deeper workflow tools.

### MCP100-P1-02: Runtime Visibility Resource

MCP MUST expose runtime visibility for each known database.

Acceptance:

- Runtime data includes metadata visibility, capability visibility, feature visibility, and driver category.
- Unavailable slices explain whether they are unsupported, unconfigured, or temporarily unavailable.
- The resource remains secret-free.

### MCP100-P1-03: Workflow Argument Provenance

Workflow planning MUST show where each argument came from.

Allowed provenance:

- `user_provided`
- `inferred_from_intent`
- `server_defaulted`
- `server_generated`
- `redacted`

Acceptance:

- Every planned argument includes provenance.
- Redacted values keep type and purpose but hide sensitive content.
- Ambiguous inferred values result in questions, not silent selection.

### MCP100-P1-04: Redaction Consistency

All sensitive output MUST use consistent redaction markers and summaries.

Acceptance:

- The same sensitive category receives the same marker shape.
- Redaction summaries list categories, not raw values.
- Tests cover representative credential and connection-property cases.

### MCP100-P1-05: Chinese Data-Governance Intent Lexicon

The MCP planning surface MUST recognize a compact Chinese domain lexicon for common metadata and rule-discovery intents.

Initial terms:

- Bank card: `银行卡`, `卡号`, `bank card`, `card number`.
- Name: `姓名`, `名字`, `name`.
- Address: `地址`, `住址`, `address`.
- Birthday: `生日`, `出生日期`, `birth date`.
- Passport: `护照`, `passport`.
- License plate: `车牌`, `license plate`.

Acceptance:

- Lexicon use only improves planning hints and search terms.
- It does not create new rule semantics by itself.
- Unknown terms degrade to generic search guidance.

### MCP100-P1-06: Terminology Alignment

Prompts, tool descriptions, resources, and capability descriptors MUST use aligned terms for the same concept.

Acceptance:

- The same operation is not described with conflicting verbs across prompt and tool surfaces.
- Descriptor examples and runtime responses use the same field names.
- Review docs list the canonical terms.

### MCP100-P1-07: Deterministic Local MCP Client Smoke

MCP MUST have a deterministic local smoke flow that exercises discovery and safe read-only calls without a live LLM.

Acceptance:

- The smoke verifies prompts, tools, resources, argument completion, and one recovery path.
- It runs locally with bounded runtime.
- It is optional for full ShardingSphere builds if module prerequisites are unavailable, but documented as the scorecard evidence path.

## P2 Requirements

### MCP100-P2-01: Optional Correlation Id

Recovery-safe responses MUST include or propagate a request/correlation id where the runtime already supports it.
The field remains optional for clients, but the server contract must be explicit for final `100`.

Acceptance:

- The id never includes sensitive user input.
- Absence of an id does not break clients.
- Logs and payloads can be correlated during triage when enabled.

### MCP100-P2-02: MCP Packaging Metadata Hints

Distribution metadata MUST make server startup and client discovery clearer where the current packaging path already supports metadata files.

Acceptance:

- Metadata does not add a new packaging system.
- It points to the existing STDIO and Streamable HTTP entry points.
- It documents required Java/toolchain expectations.

### MCP100-P2-03: Descriptor Authoring Lint

Descriptor linting MUST catch drift before runtime tests.

Acceptance:

- Unknown navigation or action types fail linting.
- Missing examples for public response modes fail linting.
- The lint rule is local and deterministic.

### MCP100-P2-04: Maintained 100 Scorecard

The repository MUST contain a maintained `100` scorecard for this feature.

Acceptance:

- The scorecard lists every gate, evidence command, and status.
- Non-goals are repeated so future review does not count them as defects.
- If any in-scope status is not passing, the score is below `100`.

## Explicit 100 Rule

After all P0, P1, and P2 acceptance criteria pass, a future answer to the same AI-native usability review prompt MUST be:

`100/100. 在当前定义的 MCP 原生易用性范围内，没有已知可优化空间。`

Before all acceptance criteria pass, the answer MUST be below `100`.
