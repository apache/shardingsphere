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

## Recovery Envelope

Represents structured retry guidance for recoverable model mistakes.

Fields:

- `recoverable`: Boolean.
- `category`: Stable category, limited to common mistakes in this increment.
- `model_action`: Human-readable safe next action.
- `missing_fields`: Missing argument names when applicable.
- `suggested_next_tool`: Optional tool for retry.
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
