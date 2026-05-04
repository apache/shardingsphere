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

# Data Model: AI-Friendly MCP Experience Hardening

## Normalized Transcript Fixture

Represents a reviewable golden payload generated from public MCP protocol calls.

Fields:

- `name`: Stable fixture name such as `tools-list` or `capabilities-resource`.
- `method`: Protocol method or resource URI.
- `payload`: Normalized JSON payload.
- `normalizers`: Dynamic fields and ordering rules applied before comparison.
- `assertion_scope`: The model-facing contract fields protected by the fixture.
- `fingerprints`: Descriptor, prompt, navigation, and schema fingerprints captured with the payload.

Validation rules:

- Dynamic values must be masked or removed.
- Collections whose order is not meaningful must be sorted by stable identifiers.
- Fixtures must focus on model-facing metadata, not large runtime data.
- Fingerprints must be deterministic for the same descriptor inputs.

## Real-Model Scenario

Represents an opt-in E2E scenario executed against an actual model.

Fields:

- `scenario_id`: Stable scenario identifier.
- `runtime_kind`: Fixture runtime such as H2 or MySQL.
- `model_provider`: Model provider name.
- `model_identifier`: Model identifier.
- `allowed_actions`: MCP bridge actions and tools available to the model.
- `descriptor_fingerprint`: Descriptor catalog fingerprint used by the scenario.
- `prompt_fingerprint`: Prompt set fingerprint used by the scenario.
- `navigation_fingerprint`: Resource navigation fingerprint used by the scenario.
- `required_trace`: Required ordered or partially ordered MCP interactions.
- `approval_boundary`: Expected preview and approval behavior before side effects.
- `final_assertions`: Final validation result and result-shape checks.
- `redaction_policy`: Credential fields that must be removed from artifacts.
- `failure_classification`: Stable category for unavailable model, assertion failure, invalid call order, unsafe execution attempt, or infrastructure failure.

Validation rules:

- Default CI must not require real-model credentials.
- Assertions must target protocol-visible interactions, not exact model prose.
- Credentials, API keys, and tokens must not appear in artifacts.
- Reports must include model and capability fingerprints so regressions are traceable.

## Completion Ranking Rule

Represents deterministic ordering applied to completion candidates.

Fields:

- `argument`: Completed argument name.
- `reference_type`: Prompt or resource reference type.
- `reference`: Prompt name or resource URI.
- `prefix_score`: Exact and prefix matching score.
- `context_score`: Match strength for supplied context arguments.
- `lifecycle_score`: Workflow plan eligibility and update recency score.
- `feature_score`: Encrypt or mask feature match score for algorithms.
- `fallback_order`: Stable lexical order used after stronger scores tie.

Validation rules:

- Returned values must remain exact argument strings.
- Ranking must be deterministic for the same request and runtime state.
- Ranking must not use external models, vector search, cross-session history, or user behavior learning.

## Completion Diagnostic Metadata

Represents optional metadata returned with completion values.

Fields:

- `value_sources`: Stable source labels for completion candidates.
- `ranking_reasons`: Static reasons such as exact prefix, context match, plan recency, or feature match.
- `missing_context`: Context arguments required before stronger candidates can be returned.
- `read_resources_first`: Public resources that can provide missing context.
- `ask_user_when_uncertain`: Whether missing context must come from the user.

Validation rules:

- Completion `values` remain plain strings.
- Diagnostics must not include secrets, hidden physical objects, guessed values, or environment-specific values.
- Empty completions must distinguish no candidates from missing prerequisite context.

## Recovery Envelope

Represents structured metadata returned with recoverable errors.

Fields:

- `recoverable`: Whether a model can safely attempt recovery.
- `category`: Stable recovery category.
- `model_action`: Natural-language action summary for the model.
- `suggested_next_tool`: Optional public tool name to call next.
- `suggested_arguments`: Optional schema-valid arguments known by the server or already supplied by the user.
- `read_resources_first`: Optional resource URIs to inspect before retrying.
- `ask_user_when_uncertain`: Whether the model must ask the user for missing values.
- `pending_questions`: Optional concise user-facing questions when native elicitation is unavailable.
- `missing_arguments`: Optional argument names that block safe retry.

Validation rules:

- Suggested arguments must not include secrets, guessed values, placeholders, or hidden physical objects.
- Wrong-tool SQL recovery must preserve `execution_mode=preview` and approval guidance.
- Unavailable workflow plan recovery must recommend replanning in the current MCP session.
- Recovery categories must be stable enough for model-confusion tests.

## Resource Navigation Entry

Represents a lightweight descriptor-owned relationship between public MCP resources, prompts, or tools.

Fields:

- `from`: Source public identifier.
- `to`: Target public identifier.
- `required_arguments`: Arguments required to follow the relationship.
- `carried_arguments`: Arguments that can be reused from the source context.
- `description`: Model-facing explanation of when to follow the relationship.
- `source_descriptor`: Descriptor file or logical descriptor owner that declared the relationship.

Validation rules:

- Both endpoints must resolve to public MCP resources, prompts, or tools.
- Navigation must be loaded from descriptors or descriptor-owned metadata.
- Navigation must not expose hidden physical metadata or create an automatic traversal service.

## Descriptor Lint Finding

Represents a descriptor quality defect that would make the MCP surface less natural for models.

Fields:

- `descriptor_id`: Tool, resource, prompt, completion target, or navigation identifier.
- `finding_category`: Missing description, weak description, missing enum meaning, missing output schema, missing safety annotation, missing completion target, missing navigation, or invalid example.
- `message`: Maintainer-facing explanation of the failed clarity rule.
- `severity`: Failure or warning level.

Validation rules:

- Findings for required P0/P1/P2 rules fail deterministic tests.
- Messages must point to the descriptor identifier and the missing model-facing contract.

## Capability Fingerprint Set

Represents deterministic identifiers for model-facing contract versions.

Fields:

- `descriptor_catalog`: Fingerprint for tool, resource, schema, and annotation descriptors.
- `prompt_set`: Fingerprint for prompt descriptors and prompt template content.
- `resource_navigation`: Fingerprint for descriptor-owned navigation metadata.
- `model_schema_set`: Fingerprint for output schemas and reusable model-facing schema fragments.

Validation rules:

- Fingerprints must ignore session IDs, timestamps, runtime database versions, and result data.
- Golden transcripts and real-model E2E reports must record the fingerprints they used.

## Next-Action Metadata

Represents shared guidance returned from successful outputs, resource outputs, prompt instructions, or errors.

Fields:

- `suggested_next_tool`: Optional public tool to call next.
- `suggested_arguments`: Optional safe arguments for the next call.
- `read_resources_first`: Optional public resources to inspect before retrying.
- `requires_user_approval`: Whether the next step can change state and needs approval.
- `ask_user_when_uncertain`: Whether missing intent must be clarified with the user.
- `stop_condition`: Optional condition telling the model no more MCP calls are needed for the current workflow.

Validation rules:

- Suggested arguments must use server-known or user-supplied values only.
- Side-effecting next actions must preserve preview and approval boundaries.

## Prompt Argument Coverage

Represents how each prompt argument can be filled.

Fields:

- `prompt_name`: Prompt descriptor name.
- `argument_name`: Prompt argument.
- `coverage_kind`: Completion-backed, resource-backed, user-provided-only, or optional.
- `reference`: Completion target, resource URI, or explanation for user-provided-only arguments.

Validation rules:

- Every prompt argument must have exactly one coverage kind.
- User-provided-only arguments must include ask-user guidance.

## Model Ergonomics Surface

Represents second-order fields that make model calls clear and bounded.

Fields:

- `name_intent`: Read-only, preview, side-effecting, planning, validation, or lookup.
- `pagination`: `has_more`, `next_page_token`, and continuation arguments when applicable.
- `progress`: Native progress handle or structured progress fields when supported.
- `sampling`: Native sampling metadata only when a concrete workflow needs model-side generation.
- `logging`: Native logging or structured diagnostic fields for long-running workflows.
- `roots`: Native roots or permission-boundary metadata for future file or configuration resources.
- `example_shape`: Compact static example for complex outputs.

Validation rules:

- Large result surfaces must expose continuation metadata.
- Sampling and logging must not become hidden planning, hidden execution, or completion ranking dependencies.
- Examples must be secret-free and environment-independent.
- File or configuration resource boundaries must be explicit before access.

## Model-Confusion Scenario

Represents a deterministic negative-path test for common model mistakes.

Fields:

- `scenario_id`: Stable scenario identifier.
- `initial_call`: The intentionally wrong or ambiguous MCP call.
- `expected_recovery`: Required recovery or next-action metadata.
- `forbidden_behavior`: Behavior that must not happen, such as execution without preview.

Validation rules:

- Scenarios must cover wrong call order, missing execution mode, stale `plan_id`, unknown database, ambiguous metadata, invalid enum, and wrong SQL tool.
- Assertions target structured fields rather than exact prose.
