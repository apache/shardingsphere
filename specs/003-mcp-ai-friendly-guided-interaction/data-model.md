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

# Data Model: AI-Friendly MCP Lightweight Requirements

This document defines the lightweight requirement objects used by the active Spec Kit package.
It is not an implementation class model and does not require new persistence, a planner, a graph service, or a benchmark framework.

## Public Surface Contract

Represents the current model-visible MCP surface.

Fields:

- `tools`: Public tool identifiers and descriptions exposed by descriptors.
- `resources`: Public resource identifiers exposed by descriptors and `resources/list`.
- `resource_templates`: Public resource template identifiers and URI patterns.
- `prompts`: Public prompt identifiers.
- `completion_targets`: Arguments that support MCP completion.
- `capabilities_uri`: The canonical `shardingsphere://capabilities` resource and sole current fact source.
- `source_paths`: README, descriptor, or documentation paths used to verify the contract.

Validation rules:

- README, descriptors, and capabilities must not describe conflicting public tools.
- Historical tool matrices must be labeled as non-current.
- Legacy tool names and compatibility shims must not remain in the active contract.
- The contract must not imply hidden physical resources or undocumented tools.

## Next Action

Represents a safe next step that a model can follow after a tool, resource, workflow, or recoverable error.

Fields:

- `action_kind`: Read resource, call tool, ask user, retry, stop, or equivalent stable kind.
- `reason`: Short explanation of why the action is suggested.
- `requires_user_approval`: Whether the next step can change state or depends on operator confirmation.
- `target_tool`: Optional public tool identifier.
- `target_resource`: Optional public resource URI.
- `required_arguments`: Optional schema-valid arguments known by the server or already supplied by the user.
- `required_inputs`: Optional user inputs that the server cannot infer safely.

Validation rules:

- Side-effecting next actions must preserve preview and user approval.
- Required arguments must not include guessed secrets, hidden physical objects, or placeholders.
- Compatibility fields such as `recommended_next_tool` and `suggested_next_tool` must be removed from the active contract.

## Resource URI Hint

Represents safe navigation returned by metadata search or resource responses.

Fields:

- `resource_uri`: Detail resource URI when safely derivable.
- `parent_resource_uri`: Parent resource URI when safely derivable.
- `next_resource_uris`: Next-hop resource URIs when safely derivable.
- `derivation_status`: Derived, unsupported, ambiguous, or not safe to derive.
- `derivation_reason`: Explanation when a URI is omitted.

Validation rules:

- URI hints must match public resource templates.
- Unsafe or ambiguous derivation must omit guessed URIs.
- Hints must not require a runtime graph traversal engine.

## Output Schema Contract

Represents the descriptor-declared response shape for a core MCP tool.

Fields:

- `tool_name`: One of `search_metadata`, `execute_query`, `execute_update`, `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow`, or `validate_workflow`.
- `status_values`: Status or state enum values returned by the tool.
- `required_fields`: Fields that are always present for the relevant response branch.
- `branch_shapes`: Preview, planned, clarifying, completed, failed, or error branch shapes.
- `examples`: Optional compact static examples.

Validation rules:

- Enum casing, field names, nested objects, and required fields must match actual payloads.
- Examples must be secret-free and environment-independent.
- Broad untyped `object` or `array` shapes are not enough for core fields.

## Recovery Envelope

Represents structured repair metadata for common model mistakes.

Fields:

- `recoverable`: Whether the caller can safely retry.
- `category`: Stable local error category.
- `missing_fields`: Arguments that block safe execution.
- `read_resources_first`: Public resources to read before retrying.
- `next_actions`: Structured next actions for repair.
- `ask_user_when_uncertain`: Whether the model must ask the user before continuing.

Validation rules:

- Missing `database` should point to `shardingsphere://databases`.
- Missing `execution_mode` should be rejected and then recover to preview.
- Wrong SQL tool recovery should point from `execute_query` to `execute_update` preview.
- Stale or unknown `plan_id` should recommend current-session completion or replanning.

## Descriptor Lint Rule

Represents a deterministic quality rule for model-visible descriptors.

Fields:

- `rule_id`: Stable lint rule identifier.
- `target_kind`: Tool, resource, prompt, completion target, navigation, or schema.
- `finding_category`: Missing description, placeholder description, missing enum value, missing approval wording, missing output schema, or invalid navigation reference.
- `severity`: Failure or warning.
- `message`: Maintainer-facing explanation.

Validation rules:

- P0 failures should fail a focused deterministic test.
- Rules must be small enough to review without a separate lint framework.
- Messages should identify the descriptor or schema that needs correction.

## Capabilities Shape Check

Represents a focused contract assertion for `shardingsphere://capabilities`.

Fields:

- `resources_section`: Presence and basic shape of public resources.
- `resource_templates_section`: Presence and basic shape of resource templates.
- `tools_section`: Presence and basic shape of public tools.
- `prompts_section`: Presence and basic shape of prompts.
- `completion_targets_section`: Presence and basic shape of completion targets.
- `resource_navigation_section`: Presence and basic shape of navigation metadata when exposed.
- `protocol_availability_section`: Transport/protocol availability metadata.
- `fingerprints_section`: Deterministic fingerprints if already exposed.

Validation rules:

- Checks should assert sections and shape, not large snapshots.
- Checks must not require a live model service.
- Dynamic runtime values must not make the check flaky.

## Completion Candidate

Represents a deterministic completion value.

Fields:

- `argument`: Completed argument name.
- `value`: Plain reusable argument string.
- `context`: Supplied database, schema, table, feature, or workflow context.
- `rank_reason`: Prefix match, contains fallback, context match, plan recency, or stable fallback order.

Validation rules:

- Values remain plain strings.
- Ranking must be deterministic for the same request and runtime state.
- Completion must not use vector search, model calls, cross-session history, or user behavior learning.

## Algorithm Property Template

Represents model-visible requirements for encrypt and mask algorithms.

Fields:

- `algorithm_type`: Encrypt or mask algorithm identifier.
- `required_properties`: Required property names.
- `optional_properties`: Optional property names.
- `defaults`: Default values when known.
- `secret_flags`: Properties that must be treated as secret.
- `capability_hints`: Known capability hints such as decrypt support, equality filter support, or like query support.

Validation rules:

- Secret values must not be emitted.
- Unknown properties should be omitted rather than guessed.
- The template should be available from existing algorithm resources.

## Approval Step Contract

Represents approval values that a model may reuse from preview to execute or apply.

Fields:

- `argument_name`: Approval argument such as `approved_steps`.
- `accepted_values`: Step identifiers, artifact identifiers, or a documented all-step value when supported.
- `preview_source`: Preview response field that produced the value.
- `requires_user_approval`: Whether visible user approval is required before reuse.

Validation rules:

- Values must come from preview output or descriptor contract.
- Values must not be guessed.
- Side-effecting execute/apply paths must preserve visible user approval.

## Workflow Side-Effect Scope

Represents the category of changes that workflow apply preview may perform.

Fields:

- `scope`: Rule metadata, physical structure, physical data, or equivalent stable category.
- `affected_objects`: Logical database, table, column, rule, or generated artifact identifiers when safely known.
- `approval_message`: Short statement of what execution can change.

Validation rules:

- The scope should be specific when the server can know it safely.
- The scope must not understate physical or rule changes.
- Physical data scope should appear only when data can actually be changed.

## Startup Troubleshooting Hint

Represents first-use connection guidance.

Fields:

- `transport`: HTTP or STDIO.
- `endpoint`: HTTP endpoint when applicable.
- `config_path`: Runtime configuration path when known.
- `log_path`: Diagnostic log path when known.
- `token_required`: Whether bearer token is required.
- `database_count`: Runtime logical database count when safely available.
- `common_failures`: Java version, missing JDBC driver, token error, STDIO log pollution, empty public surface, or workflow topology mistake.

Validation rules:

- STDIO mode must keep stdout reserved for MCP protocol.
- Hints must avoid secrets and environment-specific credentials.
- Troubleshooting should remain concise and documentation-oriented.

## Client Configuration Hint

Represents first-use client setup guidance.

Fields:

- `transport`: HTTP or STDIO.
- `command_or_url`: Local command, OCI package command, or HTTP endpoint shape.
- `headers`: Non-secret header names such as `Authorization` when token mode is enabled.
- `first_resource`: Expected first resource, normally `shardingsphere://capabilities`.

Validation rules:

- Examples must not include secrets or machine-specific paths.
- The hint should be short enough to copy into common MCP client configuration.
- Registry/package metadata should stay concise and not include long tutorials.

## Opt-In Usability Scenario

Represents a small live-model or model-like scenario outside default CI.

Fields:

- `scenario_id`: Stable scenario name.
- `purpose`: Preview-first SQL, search-to-resource, or workflow order.
- `required_trace`: Minimal ordered MCP interactions.
- `approval_boundary`: Expected user approval requirement.
- `enabled_by_default`: Always false for live-model execution.

Validation rules:

- Default CI must not require live model credentials.
- Assertions should target MCP calls and structured fields, not exact model prose.
- The scenario set must stay small and focused.
