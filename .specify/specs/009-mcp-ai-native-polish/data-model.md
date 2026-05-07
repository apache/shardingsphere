# Data Model: MCP AI-Native Polish

This document describes model-facing requirement entities, not new persistence classes.

## OrderedNextAction

Represents a next-action entry that is still passive guidance but can express sequence or dependency.

- `action_kind`: Existing supported action kind, such as `read_resource`, `call_tool`, `ask_user`, `retry_tool`, `complete_argument`, or `stop`.
- `order`: Optional deterministic action order within the response.
- `depends_on`: Optional dependency such as a previous action index or named requirement.
- `approval_dependency`: Optional flag or value that says the action waits for explicit user approval.
- `target_tool`: Tool to call or retry when known.
- `target_resource`: Resource to read when known.
- `required_arguments`: Arguments to reuse or repair.
- `reason`: Server-owned explanation.
- `requires_user_approval`: Existing approval flag.

Validation rules:

- Ordering must not execute anything or imply hidden approval.
- Approval-gated tool calls must not be presented as safe to run before the user approves the preview.
- Retry actions should name a target/source tool when the server can know it.

## SurfaceSummary

Represents the compact first-hop summary inside capabilities.

- `first_resource`: Usually `shardingsphere://capabilities`.
- `metadata_first_resource`: Usually `shardingsphere://databases`.
- `public_tools`: Current descriptor-backed tool names.
- `flow_ids`: Short list of existing common flow identifiers.
- `safety_rule`: Preview-before-approval rule.
- `non_goals`: Small list of historical or forbidden expectations such as `list_*` compatibility tools.

Validation rules:

- The summary must be generated from current descriptors or current static contract data.
- It must not become a long tutorial.
- It must not advertise historical aliases as callable tools.

## NavigationTypeHint

Represents source and target kinds for resource navigation entries.

- `from`: Existing navigation source.
- `from_type`: `resource`, `tool`, `prompt`, or another explicit known kind.
- `to`: Existing navigation target.
- `to_type`: `resource`, `tool`, `prompt`, or another explicit known kind.
- `required_arguments`: Existing argument list.
- `carried_arguments`: Existing argument list.

Validation rules:

- Type hints must be statically inferable from descriptors or known public identifiers.
- Unknown kind should be omitted or marked conservatively; do not guess.

## CompletionAvailabilityHint

Represents local hints that an argument supports MCP completion.

- `argument`: Tool field, prompt argument, or resource parameter name.
- `completion_available`: Whether the existing completion target covers this argument.
- `completion_reference`: Optional descriptor reference that backs completion.
- `missing_context_arguments`: Context required before completion can return useful values.
- `next_actions`: Optional safe next action for missing context.

Validation rules:

- Completion hints must be derived from existing `completionTargets`.
- Hints must not duplicate full completion result lists.

## ExecutableCompletionAction

Represents a `complete_argument` next action that a model can turn into an MCP completion request without guessing hidden joins.

- `action_kind`: Always `complete_argument`.
- `reference_type`: Completion reference type, such as tool, prompt, or resource.
- `reference`: Public completion reference.
- `argument_name`: Argument to complete.
- `argument_prefix`: Optional prefix to pass to completion.
- `context_arguments`: Known context arguments required by the completion target.
- `missing_context_arguments`: Context arguments that must be provided before completion can work.
- `resume_target_type`: `tool`, `resource`, or `prompt` when known.
- `resume_target`: Public tool name, resource URI, or prompt name to use after choosing a completion value.
- `resume_arguments`: Safe arguments to carry into the resumed call.
- `reason`: Server-owned explanation.
- `requires_user_approval`: Usually false; true only if the next resumed action itself needs explicit approval.

Validation rules:

- The action must use public descriptor names, not internal Java class names.
- It must not invent completion references that are not registered in `completionTargets`.
- Resume arguments must not contain secrets or unapproved side-effect execution arguments.

## ResourceErrorPayload

Represents an error returned inside resource content when `resources/read` cannot use tool-style `isError`.

- `response_kind`: Always `error`.
- `error_code`: Stable machine-readable error code.
- `message`: Secret-safe human-readable message.
- `original_uri`: Resource URI that failed.
- `recoverable`: Whether the model can safely repair or retry.
- `next_actions`: Recovery actions such as read parent, search metadata, complete an argument, or ask the user.
- `trace_id`: Optional bounded identifier if available without persistence or secret exposure.

Validation rules:

- Resource errors must be distinguishable from normal resource payloads without relying on prose.
- The payload must not expose JDBC credentials, bearer tokens, raw environment variables, or full stack traces.
- Recovery actions must use public MCP names and descriptor-backed URIs only.

## TypedResourceHint

Represents a resource URI plus enough context for the model to know why it is useful.

- `uri`: Descriptor-backed resource URI.
- `resource_kind`: Database, schema, table, column, index, rule, algorithm, capability, or another stable public kind.
- `purpose`: `read_first`, `inspect_parent`, `inspect_detail`, `validate_scope`, `continue_page`, or another bounded purpose.
- `reason`: Short server-owned explanation.
- `source_field`: Field that produced the hint, such as `resources_to_read`, `next_resources`, or `parent_resource`.
- `required_arguments`: Optional arguments that should be carried into a follow-up tool call.

Validation rules:

- URI encoding rules from `EncodedResourceIdentifier` apply to every `uri`.
- The hint must not guess resources when descriptor-backed derivation is unsafe.
- Bare URI-only fields should be removed from canonical payloads once typed hints are introduced.

## ResourceLinkHint

Represents optional protocol-native resource link metadata carried with a tool result when the SDK/client path supports it.

- `uri`: Linked MCP resource URI.
- `title`: Short display title.
- `mime_type`: Optional MIME type when known.
- `relation`: Relationship such as `detail`, `parent`, `next_page`, `capability`, or `evidence`.
- `description`: Short reason for reading the linked resource.
- `canonical_payload_field`: JSON `structuredContent` field that owns the same URI because structured content remains the canonical result.

Validation rules:

- Resource links are additive affordances and must not replace canonical `structuredContent` fields.
- Links must point only to public resource URIs the server can derive safely.
- Do not keep old URI-only payload fields solely as compatibility shims for clients that ignore resource-link content.

## EmptyStateHint

Represents why a response has no data and what the model should do next.

- `empty_state`: Stable category such as `no_items`, `no_match`, `unsupported_scope`, `not_found`, or `missing_context`.
- `reason`: Short server-owned explanation.
- `safe_next_actions`: Optional next actions to read a parent, retry search, or ask the user.
- `parent_resource`: Optional typed parent resource when known.
- `search_suggestion`: Optional safe query/search hint.

Validation rules:

- Empty states must not fabricate resources.
- A zero-row SQL result remains a valid result, not a failure.
- Unsupported capability should remain distinguishable from an empty supported list.

## ArgumentProvenance

Represents where reusable arguments and sensitive values came from.

- `argument_name`: Argument key.
- `source`: `user_provided`, `server_normalized`, `server_generated`, `server_defaulted`, or `redacted`.
- `reuse_rule`: Whether to copy exactly, ask user, or avoid reconstruction.
- `redacted`: Whether the visible value is intentionally masked.

Validation rules:

- Server-normalized SQL and server-generated `plan_id` should be copied exactly.
- Redacted values must not be reconstructed.
- Provenance must not expose secrets, tokens, passwords, or raw private configuration.

## RuntimeRecoveryHint

Represents a more specific safe recovery category for runtime failures.

- `category`: `missing_jdbc_driver`, `authentication_failed`, `connection_timeout`, `database_unavailable`, or similar safe category.
- `model_action`: Short safe fix instruction.
- `resources_to_read`: Optional typed resources to inspect.
- `local_check`: Optional local action such as checking `plugins/`.
- `request_id`: Optional bounded request identifier if implemented.

Validation rules:

- Do not expose credentials, raw tokens, or full environment dumps.
- Request identifiers must not require persistent storage.

## UsabilityFollowMetric

Represents opt-in LLM usability metrics for model comfort.

- `next_action_follow_rate`: Fraction of traces that follow server-suggested next actions when present.
- `approval_violation_rate`: Fraction of traces that attempt side-effect execution before approval.
- `invalid_call_rate`: Existing generic invalid-call metric.
- `excluded_from_default_ci`: Must remain true.

Validation rules:

- Metrics run only in the existing opt-in LLM usability lane.
- Default module tests must remain deterministic and credential-free.

## InputBoundContract

Represents schema-visible bounds that keep model-issued SQL and metadata calls predictable.

- `argument`: Argument name, such as `max_rows`, `timeout_ms`, `page_size`, or `page_token`.
- `default_value`: Server default used when the model omits the argument.
- `minimum_value`: Minimum accepted value when the argument is numeric.
- `maximum_value`: Maximum accepted value when the argument is numeric and bounded.
- `invalid_value_recovery`: Structured recovery action for malformed, negative, stale, or out-of-range input.
- `truncation_field`: Response field that tells the model whether the default or cap truncated results.

Validation rules:

- Defaults and caps must be documented in descriptors or capabilities before models are expected to rely on them.
- Invalid inputs should produce structured recovery instead of silent coercion when the model can repair the call.
- A zero-row SQL result remains success; an invalid row limit or page token is a recoverable call-shape error.

## ResponseModeContract

Represents a stable way for a model to distinguish preview, execution, manual, validation, recovery, truncation, pagination, and terminal states.

- `response_mode`: `preview`, `executed`, `manual_only`, `validation`, `recovery`, or `terminal` where applicable.
- `execution_mode`: Public input value that produced the response when relevant.
- `preview_semantics`: Optional wording or enum explaining whether preview is classification-only, generated-artifact preview, or validation preview.
- `side_effect_scope`: Existing side-effect summary when present.
- `continuation_state`: Optional `truncated`, `has_more`, `next_page_token`, or equivalent state.
- `next_actions`: Safe continuation actions derived from the response state.

Validation rules:

- Preview responses must not imply that side effects already occurred.
- Classification previews must not be presented as affected-row estimates.
- Pagination and truncation state should be structured enough for a model to continue without prose parsing.

## ResultRowView

Represents an optional model-friendly SQL result view.

- `rows`: Existing positional row data.
- `row_objects`: Optional row objects keyed by column label when labels are unique and stable.
- `row_object_status`: `available`, `duplicate_column_labels`, `unnamed_columns`, or `not_applicable`.
- `column_labels`: Labels used to decide whether object rows are safe.

Validation rules:

- Positional rows remain the lossless representation.
- Object rows must not overwrite duplicate labels.
- The server should expose why object rows are unavailable instead of silently omitting them when that helps model parsing.

## RecoveryTargetHint

Represents exact retry or repair targets in structured recovery payloads.

- `source_tool`: Tool that produced the error when known.
- `target_tool`: Tool the model should retry or call next when known.
- `target_resource`: Resource the model should read when known.
- `argument_path`: Public argument name or path that needs repair.
- `suggested_value`: Safe suggested value, such as `execution_mode=preview`, when applicable.
- `source`: Whether the value is server-defaulted, server-normalized, or user-provided.

Validation rules:

- Recovery hints should use public MCP names, not internal Java fields or synthetic aliases.
- `apply_workflow` recovery must not point to `execute_update` unless the actual recovery action is to change tools.
- Suggested values must preserve preview-before-execution safety.

## AmbiguityHint

Represents a bounded explanation that a search or lookup result needs narrowing.

- `ambiguous`: Whether multiple safe candidates remain.
- `ambiguous_by`: `database`, `schema`, `object_type`, `name`, or another stable scope dimension.
- `candidate_count`: Number of ambiguous candidates when cheap to compute.
- `narrowing_arguments`: Public arguments that can disambiguate the request.
- `next_actions`: Safe actions to read a parent resource, search within a narrower scope, or ask the user.

Validation rules:

- Ambiguity hints must not invent a best match.
- Hints should be omitted or conservative when counting would require expensive scans.
- Search remains lexical/descriptor-backed; no semantic ranking is introduced.

## ClarificationQuestion

Represents a field-level question that a model can turn into natural user language.

- `field`: Missing or ambiguous field name.
- `question_key`: Stable localization-friendly identifier.
- `input_type`: Expected answer type, such as string, enum, boolean, integer, or secret.
- `allowed_values`: Optional allowed values for enum-like answers.
- `default_value`: Optional server-proposed default.
- `secret`: Whether the model must avoid echoing or storing the answer.
- `display_message`: Short user-facing question generated from the structured fields.

Validation rules:

- Field-level questions must not require the model to parse prose to identify the missing field.
- Secret questions must not include existing secret values.
- Structured fields are the canonical contract.
- MCP-native elicitation can carry the same fields when supported.
- Prose-only compatibility fields must not be emitted once structured questions are available.

## WorkflowStatusSnapshot

Represents a current-session read-back view of a workflow plan.

- `plan_id`: Server-generated current-session plan identifier.
- `workflow_type`: Encrypt or mask workflow type.
- `status`: Current stage or terminal status.
- `scope`: Logical database, schema, table, and column scope where available.
- `artifact_summary`: Secret-free artifact counts, identifiers, and side-effect categories.
- `next_actions`: Safe continuation actions such as apply preview, validate, replan, or stop.
- `current_session_only`: Always true for this increment.

Validation rules:

- Snapshots must not create cross-session memory or durable audit storage.
- Redacted properties stay redacted; snapshots must not reconstruct sensitive values.
- Stale or missing plans should return structured recovery and replan guidance.

## EncodedResourceIdentifier

Represents a logical identifier segment that may require percent-encoding in MCP resource URIs.

- `raw_identifier`: Original logical name from ShardingSphere metadata.
- `encoded_segment`: Percent-encoded URI path segment.
- `resource_kind`: Database, schema, table, column, index, view, rule, or algorithm.
- `derivation_status`: Whether the URI was safely derived, not derivable, or requires parent lookup.

Validation rules:

- Encoding and decoding must preserve non-ASCII characters and reserved URI characters.
- The server must avoid returning guessed URIs when a descriptor-backed pattern cannot safely represent an identifier.

## RuntimeStatusSummary

Represents compact, secret-free health and first-use information.

- `transport`: STDIO or HTTP.
- `configured_database_count`: Number of configured logical databases.
- `configured_database_names`: Optional logical database names when safe.
- `feature_availability`: Loaded MCP feature groups.
- `connection_warnings`: Safe categories such as missing driver or unavailable database.
- `first_checks`: Token-safe next actions for capabilities and database discovery.

Validation rules:

- Runtime status must not expose JDBC passwords, access tokens, raw secret properties, or full environment dumps.
- It should help the model triage setup without becoming an observability platform.
