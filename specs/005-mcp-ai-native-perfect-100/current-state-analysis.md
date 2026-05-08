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

# Current State Analysis: MCP AI-Native Perfect 100

## Baseline

Date: 2026-05-08

Branch: `001-shardingsphere-mcp`

Scope: current MCP implementation under `mcp`, `distribution/mcp`, and `test/e2e/mcp`.

This analysis maps the current implementation to `requirements.md`.
It is not an implementation completion report.

## Current Score

Current gate result: not `100`.

Working score for prioritization: `82/100`.

Reason:

- The implementation already has a strong model-facing MCP surface.
- The remaining gaps are mostly contract consistency and proof gaps, not missing core runtime features.
- Under the strict gate model, any missing P0/P1/P2 item blocks the final `100` answer.

## Actual Module and Verification Paths

MCP modules:

- `mcp`
- `mcp/api`
- `mcp/support`
- `mcp/core`
- `mcp/bootstrap`
- `mcp/features/encrypt`
- `mcp/features/mask`
- `distribution/mcp`
- `test/e2e/mcp`

Recommended scoped commands after implementation:

```bash
./mvnw -pl mcp -am -DskipITs -Dspotless.skip=true test
./mvnw -pl mcp -am -Pcheck checkstyle:check
./mvnw -pl test/e2e/mcp -am -DskipITs -Dspotless.skip=true test
```

The earlier placeholder command `-pl mcp` is valid as an aggregate module path.

## P0 Analysis

### MCP100-P0-01: Universal Response Mode

Status: partial.

Evidence:

- Workflow planning, workflow validation, workflow apply, execute-update preview/executed, and recovery payloads use `response_mode`.
- `execute_query` does not always emit `response_mode`.
- `search_metadata` does not emit `response_mode`.
- metadata resources use `resource_kind`, not the universal `response_mode`.
- top-level error payloads do not expose `response_mode`; only nested recovery does.

Required work:

- Add one shared response-mode vocabulary.
- Emit or document `response_mode` for every public tool, resource, prompt, descriptor, and structured error response.
- Validate mode values in descriptor and contract tests.

### MCP100-P0-02: Deterministic Next Actions

Status: partial.

Evidence:

- `MCPNextActionUtils` centralizes action creation and supports `requires_user_approval`.
- Some call sites add `order`, for example metadata search and ordered recovery flows.
- Many call sites still return single `List.of(...)` actions without `order`.
- The active schema uses `action_kind`, `target_tool`, `target_resource`, and `required_arguments`.
- The `005` contract requires `type`, `title`, `tool_name`, `arguments`, `resource_uri`, and `question`.

Required work:

- Decide and enforce the final field vocabulary from `005`.
- Add `order` to every action path, including single-action responses.
- Add runtime or test validation for malformed next actions.

### MCP100-P0-03: Large Metadata Continuation Semantics

Status: partial.

Evidence:

- `search_metadata` has real pagination with `page_token`, `next_page_token`, and `has_more`.
- direct metadata list resources cap results at 100 and add `large_result_guidance`.
- direct list resources do not expose `continuation_mode`.
- capped direct lists set `has_more=false` because no `next_page_token` is passed to `MCPItemsResponse`.

Required work:

- Add `continuation_mode=pagination` for real paginated search results.
- Add `continuation_mode=search_metadata` for capped direct metadata lists.
- Make `has_more` and continuation semantics impossible to misread.

### MCP100-P0-04: Nearest-Resource Completion Recovery

Status: partial.

Evidence:

- completion metadata includes diagnostic values such as `missing_context`, `no_candidates`, and `prefix_filtered_all_candidates`.
- missing context currently returns a `complete_argument` action for the first missing argument.
- `no_candidates` falls back to `shardingsphere://capabilities`.
- completion does not consistently point to nearest metadata resources such as schemas, tables, columns, or indexes.

Required work:

- Route missing `database` to `shardingsphere://databases`.
- Route missing `schema`, `table`, `column`, and `index` to the nearest known parent resource.
- Distinguish `missing_context`, `empty_scope`, and prefix-filtered empty results.

### MCP100-P0-05: Deterministic Single-Schema Auto-Fill

Status: partial.

Evidence:

- workflow planning resolves schema when exactly one database schema exists.
- workflow planning also resolves schema when a table exists in exactly one schema.
- completion does not auto-fill schema.
- planning output does not expose this inferred schema with the final provenance vocabulary.

Required work:

- Add visible provenance for single-schema inference.
- Add completion behavior or metadata that lets clients reuse the deterministic single schema.
- Ask the user when more than one schema is visible.

### MCP100-P0-06: Not-Found and Empty-State Recovery

Status: partial.

Evidence:

- metadata detail resources return `found=false`, `empty_state`, parent resource hints, and `next_actions`.
- search empty results return `empty_state` and a broader search action.
- unsupported tool/resource and many invalid arguments have structured recovery.
- not-found responses do not consistently include `requested_token`, `retry_arguments`, or the final recovery category vocabulary.

Required work:

- Normalize categories to `not_found`, `ambiguous`, `empty_scope`, `missing_context`, `validation`, and `terminal`.
- Add retry-ready search arguments when an unresolved object token is known.
- Preserve the existing rule that unresolved child URIs are not invented.

### MCP100-P0-07: Public Contract Drift Guard

Status: partial.

Evidence:

- `MCPDescriptorCatalogLoader` validates descriptors, examples, no legacy recommendation fields, and basic `next_actions` schema.
- existing tests cover several recovery branches and descriptor contracts.
- no `005` contract test validates the final response modes, continuation modes, recovery categories, or next-action field vocabulary.

Required work:

- Add a deterministic `MCP100` contract test suite.
- Make the suite fail on every known `005` P0 regression.

## P1 Analysis

### MCP100-P1-01: Secret-Free Readiness

Status: partial.

Evidence:

- `shardingsphere://runtime` is secret-free and exposes runtime status.
- README documents startup hints and token state.
- there is no short token-safe health or readiness path outside normal MCP resource flow.

Required work:

- Define whether readiness is the `shardingsphere://runtime` resource, an HTTP path, or both.
- Document the token-safe check path and test that it does not leak credentials.

### MCP100-P1-02: Runtime Visibility Resource

Status: partial.

Evidence:

- `RuntimeStatusHandler` exposes status, active transport, database count, per-database type, schema count, capabilities, and resource links.
- it does not expose the final `metadata_visibility`, `capability_visibility`, `feature_visibility`, and `driver_category` fields.
- unavailable slices are not categorized as unsupported, unconfigured, or unavailable.

Required work:

- Extend runtime status to the final visibility vocabulary.
- Keep the existing secret-free rule.

### MCP100-P1-03: Workflow Argument Provenance

Status: partial.

Evidence:

- `execute_update` and workflow apply expose `argument_provenance`.
- current values include `server_normalized`, `server_suggested`, and `server_generated_current_session`.
- workflow planning does not expose per-argument provenance for the planning request.
- the final `005` vocabulary is different.

Required work:

- Normalize provenance to `user_provided`, `inferred_from_intent`, `server_defaulted`, `server_generated`, and `redacted`.
- Add provenance to workflow planning, not only apply or SQL preview.

### MCP100-P1-04: Redaction Consistency

Status: partial.

Evidence:

- workflow artifacts use `WorkflowArtifactMaskUtils` with marker `******`.
- redaction payload lists `redacted_properties`.
- broader runtime/config/connection redaction categories are not summarized with a shared contract.

Required work:

- Centralize redaction markers and summaries.
- Cover representative workflow secrets and runtime connection secret categories.

### MCP100-P1-05: Chinese Data-Governance Intent Lexicon

Status: partial.

Evidence:

- intent resolution recognizes Chinese terms for phone, ID card, email, encryption reversibility, equality, and fuzzy query.
- it does not cover the required compact lexicon for bank card, name, address, birthday, passport, and license plate.

Required work:

- Add the compact lexicon in shared workflow intent support.
- Keep it as planning/search assistance only.

### MCP100-P1-06: Terminology Alignment

Status: partial.

Evidence:

- capability payload documents field naming and common flows.
- active fields still mix protocol camelCase and ShardingSphere snake_case, which is expected but needs clearer final separation.
- next-action terminology conflicts with the `005` data model.

Required work:

- Choose final action field names and document them once.
- Align descriptors, runtime responses, tests, and docs to the chosen vocabulary.

### MCP100-P1-07: Deterministic Local MCP Client Smoke

Status: partial.

Evidence:

- `ProductionH2RuntimeSmokeE2ETest` has a deterministic AI-native interaction loop over HTTP and STDIO.
- programmatic e2e tests cover metadata search, resources, pagination, and some errors.
- live LLM smoke/usability suites exist but are gated by configuration.
- the deterministic smoke does not yet prove all `005` discovery, completion, and recovery gates together.

Required work:

- Add or extend a deterministic smoke flow for prompts, tools, resources, completion, and one recovery path.
- Keep live LLM tests optional evidence only.

## P2 Analysis

### MCP100-P2-01: Optional Correlation Id

Status: not implemented.

Evidence:

- no runtime response or recovery payload exposes `request_id` or `correlation_id`.
- e2e trace records exist for tests, but they are not response fields.

Required work:

- Add bounded, secret-free request/correlation id where runtime context exists.
- Keep the field optional for clients.

### MCP100-P2-02: MCP Packaging Metadata Hints

Status: partial.

Evidence:

- `mcp/server.json` exists and describes the server, repository, OCI package, and stdio package transport.
- README documents registry and OCI publication.
- HTTP package metadata is not represented as clearly as STDIO metadata.

Required work:

- Add or clarify HTTP metadata hints when supported by registry/package format.
- Ensure distribution packaging includes the metadata where expected.

### MCP100-P2-03: Descriptor Authoring Lint

Status: partial.

Evidence:

- descriptor loading validates many authoring errors.
- current validation does not enforce the `005` response-mode vocabulary, continuation mode vocabulary, recovery categories, or final next-action schema.

Required work:

- Extend descriptor validation or add a dedicated contract test for the final `005` vocabulary.

### MCP100-P2-04: Maintained 100 Scorecard

Status: not implemented.

Evidence:

- live LLM usability scorecard classes exist under `test/e2e/mcp`.
- no maintained `005` scorecard artifact maps every `MCP100-*` gate to deterministic evidence.

Required work:

- Add a maintained scorecard artifact for this feature.
- Make any non-passing gate block the final `100` answer.

## Priority Conclusion

P0 first:

1. final response-mode vocabulary and coverage.
2. final next-action schema and order coverage.
3. continuation semantics for direct metadata lists.
4. nearest-resource completion recovery.
5. normalized empty/not-found recovery categories.
6. deterministic P0 contract tests.

P1 second:

1. runtime visibility vocabulary.
2. provenance vocabulary and planning provenance.
3. redaction summary contract.
4. compact Chinese data-governance lexicon.
5. deterministic smoke coverage for one complete model-native loop.

P2 last:

1. request/correlation id.
2. packaging metadata completion.
3. descriptor lint completion.
4. maintained `100` scorecard.

## Final Assessment

The current implementation is close enough that this is a contract-hardening project, not a rewrite.

It cannot honestly answer `100/100` yet because several `005` gates are only partial or absent.

The fastest path to the target is to implement P0 as a single shared contract layer, then apply it across resources, tools, completion, workflow, and recovery.

