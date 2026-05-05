# Data Model: MCP AI-Native Polish

This document describes model-facing requirement entities, not new persistence classes.

## NextActionContract

Represents the capability-level schema for model continuation guidance.

- `action_kind`: Supported kind, such as `read_resource`, `call_tool`, `ask_user`, `retry_tool`, `complete_argument`, or `stop`.
- `required_fields`: Fields that must appear for this action kind.
- `optional_fields`: Fields that can appear when the server knows them.
- `approval_semantics`: Whether the action may require explicit user approval.

Validation rules:

- Every documented action kind must be produced by at least one existing response or recovery path, or marked reserved for compatibility with current responses.
- The contract must not instruct the model to execute side effects without preview and user approval.

## CommonFlow

Represents a short static model-facing recipe.

- `flow_id`: Stable identifier such as `metadata-inspection` or `side-effecting-sql-preview`.
- `intent`: User task category.
- `steps`: Ordered descriptor-backed resource or tool references.
- `stop_condition`: When the model should stop or ask the user.
- `safety_notes`: Preview, approval, or user-clarification constraints.

Validation rules:

- Every referenced tool or resource must be descriptor-backed.
- Flows should stay short enough for capabilities. Long explanations belong in README.

## SearchContext

Represents the search scope applied by `search_metadata`.

- `query`: Search string used by the server.
- `database`: Optional logical database scope.
- `schema`: Optional schema scope.
- `object_types`: Object type filters used by the server.
- `page_size`: Applied page size.
- `page_token`: Page token received by the server.
- `next_page_token`: Token returned for continuation when more items exist.

Validation rules:

- Context must reflect applied values, not only raw user input.
- Defaults should be visible when they affect result shape.

## MatchExplanation

Represents why a metadata search hit matched.

- `match_kind`: Cheap deterministic kind such as `exact`, `prefix`, or `contains`.
- `matched_fields`: Fields that matched, such as `name`, `table`, or `view`.
- `derivation_status`: Existing URI derivation status.
- `derivation_reason`: Existing explanation when URI derivation is unsafe.

Validation rules:

- Explanations must be deterministic and based on existing search comparisons.
- No semantic ranking, model calls, or vector scores are allowed.

## OutputParseHint

Represents explicit metadata that helps a model parse SQL or workflow outputs.

- `returned_row_count`: Number of rows in a result payload.
- `applied_max_rows`: Row limit used by the server when known.
- `applied_timeout_ms`: Timeout used by the server when known.
- `status_values`: Status vocabulary documented by schema or examples.
- `item_shapes`: Nested object shapes for arrays and packages.

Validation rules:

- Hints must be derived from existing payload values or validated request arguments.
- Hints must not expose secrets, passwords, tokens, or driver-specific private details.

## ApprovalSummary

Represents server-owned approval wording for preview responses.

- `summary`: Short text or structured fields that describe what would happen.
- `requires_user_approval`: Always true for side-effect execution follow-up.
- `side_effect_scope`: Existing or refined side-effect categories.
- `reuse_arguments`: Server-provided arguments to reuse only after approval.

Validation rules:

- Summaries must not weaken the preview-before-execute rule.
- Summaries must not become approval tokens or durable approval records.

## DeterministicGuard

Represents a focused regression check.

- `guard_name`: Test or lint identifier.
- `target_surface`: Capabilities, descriptor, search response, SQL output, workflow output, or recovery.
- `assertions`: Small shape or field-presence checks.
- `excluded_dependencies`: Live model services, external databases unless already part of existing scoped tests.

Validation rules:

- Guards should be stable in default local runs.
- Guards should avoid broad golden snapshots unless a later requirement justifies them.
