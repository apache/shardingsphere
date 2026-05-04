# Data Model: MCP AI-Friendly Lightweight Experience

## Current MCP Surface

Represents the model-visible public MCP contract.

Fields:

- `resources`: Fixed readable resources.
- `resourceTemplates`: Templated readable resources.
- `tools`: Public callable tools.
- `prompts`: Prompt descriptors and retrievable prompt templates.
- `completionTargets`: Descriptor-backed completion references.
- `resourceNavigation`: Public relationships between resources and tools.
- `protocolAvailability`: Which MCP protocol surfaces are available.
- `fingerprints`: Deterministic hashes for model-facing descriptor inputs.

Validation rules:

- Public README tool lists must match descriptor-backed tools.
- Current contract must not rely on historical PRD-only tool names.

## Implementation Analysis Note

Represents the pre-implementation evidence record required before a P0 slice starts.

Fields:

- `requirement_id`: Requirement or task identifier.
- `current_behavior`: Short summary of current code behavior.
- `evidence_paths`: Descriptor, handler, resource, workflow, completion, test, or README paths inspected.
- `affected_paths`: Minimal production, test, descriptor, or documentation paths expected to change.
- `verification_map`: Tests or document checks mapped to acceptance criteria.
- `non_goals`: Related capabilities intentionally excluded from the slice.
- `rollback_boundary`: Existing behavior restored by reverting the slice.

Validation rules:

- Must be recorded before implementation starts for each P0 slice.
- Must not introduce new product requirements by itself.
- Must be short enough to review with the implementation diff.

## Resource URI Hint

Represents a safe descriptor-backed URI that helps the model move from search or list results to detail resources.

Fields:

- `resource_uri`: Direct detail resource URI for the current hit.
- `parent_resource_uri`: Optional parent URI when it can be derived without guessing.
- `next_resource_uris`: Optional next-hop URIs for safe child resources.
- `derivation_status`: One of `derived` or `not_safe_to_derive`.
- `derivation_reason`: Short reason when no URI is returned.

Validation rules:

- URI patterns must come from the current descriptor catalog.
- URI values must be built from server-known or user-provided logical metadata only.
- Do not invent hidden physical object names, passwords, paths, or environment-specific values.

## Next Action

Represents one model-facing recommendation after a tool, resource, prompt, or recovery response.

Fields:

- `action_kind`: One of `call_tool`, `read_resource`, `ask_user`, or `stop`.
- `reason`: Short model-facing reason for this action.
- `requires_user_approval`: Whether visible user approval is required before the action.
- `target_tool`: Required when `action_kind=call_tool`.
- `target_resource`: Required when `action_kind=read_resource`.
- `required_arguments`: Arguments the model can safely reuse for a tool call.
- `required_inputs`: User inputs needed before continuing.

Validation rules:

- Suggested arguments must contain only server-known or user-provided values.
- Suggested arguments must not contain secrets, placeholders, guessed values, or hidden physical objects.
- Side-effecting next actions must preserve user approval semantics.

## Output Schema Contract

Represents the small contract between descriptor-visible output schema and real tool response payloads.

Fields:

- `tool_name`: Public tool identifier.
- `required_fields`: Fields that must appear for the documented scenario.
- `optional_fields`: Fields that may appear when enough context exists.
- `state_values`: Known state or status values such as `preview`, `clarifying`, `planned`, `completed`, `failed`, or `error`.
- `enum_values`: Enumerated argument or response values that must match real payload casing.
- `example_refs`: Optional compact examples used to explain non-trivial shapes.

Validation rules:

- The contract must cover `search_metadata`, `execute_query`, `execute_update`, `plan_encrypt_rule`, `plan_mask_rule`, `apply_workflow`, and `validate_workflow`.
- Do not lock large runtime payloads or environment-specific values.
- Schema fields should be specific enough that a model does not need to infer key nested structures from prose.

## Recovery Envelope

Represents structured retry guidance for recoverable model mistakes.

Fields:

- `recoverable`: Boolean.
- `category`: Stable category, limited to common mistakes in this increment.
- `model_action`: Human-readable safe next action.
- `missing_fields`: Missing argument names when applicable.
- `suggested_next_tool`: Optional compatibility hint for retry; `next_actions` remains the primary guidance surface.
- `suggested_arguments`: Optional safe arguments for retry.
- `read_resources_first`: Optional resource URIs to inspect before retry.
- `requires_user_approval`: Whether user approval is required before the recommended retry.
- `ask_user_when_uncertain`: Whether the model should ask the user before continuing.

Initial categories:

- `missing_database`
- `missing_execution_mode`
- `wrong_sql_tool`
- `unsupported_tool`
- `unsupported_resource_uri`
- `unavailable_plan_id`

## Compact Example

Represents a small static example for complex tool output.

Fields:

- `target`: Tool or response type the example describes.
- `scenario`: Short label such as `preview`, `planned`, `clarifying`, `validated`, or `failed`.
- `payload`: Small JSON-compatible shape.

Validation rules:

- Must not include real secrets.
- Must not include production database names.
- Must not include environment-specific paths.
- Should fit in documentation or descriptor metadata without overwhelming model context.

## Capabilities Core Contract

Represents the deterministic, lightweight assertions for `shardingsphere://capabilities`.

Fields:

- `required_sections`: Expected top-level sections.
- `required_tool_fields`: Fields every public tool entry must expose.
- `required_resource_fields`: Fields every public resource or template entry must expose.
- `required_fingerprint_fields`: Fingerprint keys needed for traceability.

Validation rules:

- Assert section presence and basic shape only.
- Avoid large runtime payload snapshots.
- Avoid dependency on real model services.

## Descriptor Lint Rule

Represents a deterministic rule that protects model-facing descriptor quality.

Fields:

- `rule_id`: Stable identifier.
- `target_kind`: One of `tool`, `resource`, `resource_template`, `prompt`, `completion`, or `navigation`.
- `severity`: `error` for P0 blocking regressions in this increment.
- `message`: Short actionable failure message.
- `target_id`: Descriptor identifier that failed the rule.

Initial rules:

- Descriptions are not empty.
- Descriptions are not placeholders.
- Side-effecting tools expose side-effect and approval semantics.
- Enum-like fields list known values.
- Core output schemas include key fields.
- Navigation references resolve to public identifiers.

## Workflow Plan Summary

Optional P1 model for current-session workflow recovery.

Fields:

- `plan_id`: Current-session plan identifier.
- `workflow_kind`: Workflow kind such as `encrypt.rule` or `mask.rule`.
- `status`: Current lifecycle status.
- `updated_at`: Last update time if available.
- `artifact_summary`: Small summary of generated DDL, DistSQL, or manual artifacts.
- `next_actions`: Structured next actions.

Validation rules:

- Must be session-scoped.
- Must not expose plans from other sessions.
- Must not imply durability across server restarts.

## Metadata Freshness Hint

Optional P1 field for metadata responses.

Fields:

- `metadata_fingerprint`: Deterministic identifier for the visible metadata snapshot, or
- `loaded_at`: Load time for the metadata snapshot.

Validation rules:

- Should help compare whether multiple metadata responses came from the same snapshot.
- Must not require a new active refresh mechanism in this increment.

## Startup Runtime Hint

Optional P2 model for startup or first-run diagnostics.

Fields:

- `transport`: `http` or `stdio`.
- `endpoint`: HTTP endpoint when applicable.
- `config_path`: Config file path.
- `log_path`: Log path when known.
- `runtime_database_count`: Number of configured runtime databases when available.
- `access_token_required`: Whether HTTP bearer token is expected.
- `stdio_logging_rule`: Reminder that STDIO protocol output must not be polluted by logs.

Validation rules:

- Must not print secrets.
- STDIO mode must keep protocol stdout clean.
- This is diagnostic guidance, not a new management API.

## Environment Variable Reference

Optional P2 model for sensitive configuration values sourced from environment variables.

Fields:

- `config_key`: Configuration key, such as `transport.http.accessToken` or runtime database password.
- `env_name`: Environment variable name.
- `required`: Whether startup should fail if the variable is missing.
- `failure_message`: Clear validation message when the reference cannot be resolved.

Validation rules:

- Do not introduce a full secret management system in this increment.
- Documentation must explain missing-variable behavior.
