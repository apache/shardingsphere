# Breaking Cleanup Analysis: MCP AI-Native Polish

## Purpose

The implementation posture is conservative but not compatibility-preserving.
Conservative means small canonical contracts, descriptor-backed data, and no planner-like system.
It does not mean keeping old and new model-facing shapes alive together.

This analysis maps old model-facing contracts to one of three decisions:

- **Delete**: The field exists only as an old convenience, prose-only cue, or compatibility shim.
- **Replace**: The concept is still useful, but the shape must become canonical and machine-readable.
- **Keep**: The field carries correctness, protocol identity, or lossless data rather than compatibility.

## Cleanup Rules

- Do not keep parallel old/new payload fields after the canonical field lands.
- Delete replaced helpers, descriptor schema entries, README examples, and tests in the same review slice.
- Add negative assertions for removed fields, not only positive assertions for new fields.
- Keep human display text only as display text; models must not need it for field identity, allowed values, bounds, or recovery targets.
- Do not remove protocol-owned identity or lossless data fields merely because they contain strings.

## Deletion Matrix

### Workflow Missing-Input Questions

Current producers and references:

- `WorkflowPlanPayloadBuilder` emits `pending_questions`.
- `WorkflowGuidancePayloadBuilder` emits `clarification_questions`, `elicitation.fallback_fields`, and per-question `message`.
- `encrypt.yaml`, `mask.yaml`, `mcp/README.md`, `mcp/README_ZH.md`, and workflow tests still teach or assert `pending_questions`.

Decision:

- Delete `pending_questions` from canonical workflow plan payloads, descriptors, README examples, and tests.
- Delete `elicitation.fallback_fields` and the native-unsupported sentinel until native elicitation is actually implemented.
- Replace question `message` with `display_message`; it is display text, not the model's parsing source.
- Keep `missing_required_inputs`; it is a compact field index, not a prose compatibility shim.
- Keep `issues[].message` and error `message`; they describe incidents, not missing-input schema.

Canonical replacement:

- `clarification_questions[]` with `field`, `question_key`, `input_type`, `allowed_values`, `default_value`, `secret`, and `display_message`.
- Optional MCP-native elicitation may mirror these fields later, but must not create a second contract.

Verification:

- Positive tests assert structured question fields.
- Negative tests assert workflow plan payloads, descriptors, and README examples no longer expose `pending_questions` or `elicitation.fallback_fields`.

### Resource References and Navigation

Current producers and references:

- `WorkflowGuidancePayloadBuilder.createResourcesToRead` returns `List<String>`.
- `RuntimeStatusHandler` returns `resources_to_read` as URI strings.
- `MetadataResourceHandler` returns `next_resources` as URI strings.
- `MetadataSearchHit` exposes `resource_uri`, `parent_resource_uri`, and `next_resource_uris`.
- Capability payload contracts and tests list `next_resources` as an optional URI-list field.

Decision:

- Replace `resources_to_read` item shape with typed objects; keep the field name but remove string-list semantics.
- Replace `next_resources` item shape with typed objects; do not add a parallel `next_resource_hints`.
- Replace search-hit `resource_uri`, `parent_resource_uri`, and `next_resource_uris` with typed `resource`, `parent_resource`, and `next_resources`.
- Replace navigation `parent_uri` with typed `parent_resource` where it is used as a follow-up hint.
- Keep `self_uri`; it is the resource identity for the current payload.
- Keep URI strings inside typed objects; the URI value is still the actual MCP resource identifier.

Canonical replacement:

- Typed resource entries use `uri`, `resource_kind`, `purpose`, `reason`, and `source_field`.
- Add `required_arguments` only when a follow-up tool call needs argument carry-over.
- Allowed `purpose` values should be bounded, for example `read_first`, `inspect_parent`, `inspect_detail`, `continue_page`, and `validate_scope`.

Verification:

- Tests assert typed objects in workflow, runtime status, metadata resource navigation, and metadata search hits.
- Negative tests assert no URI-only arrays remain for `resources_to_read`, `next_resources`, or search-hit next-resource fields.
- Capability payload contracts must describe typed entries, not string lists.

### Completion Next Actions

Current producers and references:

- `MCPNextActionUtils.completeArgument` emits only `action_kind`, `argument_name`, `reason`, and `requires_user_approval`.
- `MCPCompletionSpecificationFactory` can identify missing context but calls the old helper.
- `MCPErrorConverter` uses the same helper for `plan_id`.
- Capabilities describe `complete_argument` as an action with only the old fields.

Decision:

- Delete the old `completeArgument(String, String)` helper or make it impossible to produce the old shape.
- Replace all call sites with an executable completion action payload.
- Keep `argument_name` and `reason`; they remain useful fields inside the richer action.
- Add context and resume fields only when known; omit unknown fields rather than guessing.

Canonical replacement:

- `complete_argument` action includes `reference_type`, `reference`, `argument_name`, `argument_prefix`,
  `context_arguments`, `missing_context_arguments`, `resume_target_type`, `resume_target`, and `resume_arguments` when available.

Verification:

- Completion and recovery tests assert the new fields for known tool/resource completion targets.
- Negative tests assert no `complete_argument` action is produced with only `argument_name`, `reason`, and `requires_user_approval`.
- `next_action_contract` must document the executable shape.

### Resource-Read Errors

Current producers and references:

- Tool errors use `CallToolResult.isError`.
- `createReadResourceResult` wraps any payload as `TextResourceContents`; resource reads have no tool-style `isError`.
- Resource controller tests currently assert simple `error_code` and `message` payloads for unsupported resources.

Decision:

- Resource-read failures must use `response_kind=error` inside the resource JSON payload.
- Do not convert empty or not-found domain states into resource errors.
- Keep `error_code` and secret-safe `message`; they are not compatibility fields.
- Add `original_uri`, `recoverable`, and `next_actions`.

Canonical replacement:

- Resource error payload: `response_kind`, `error_code`, `message`, `original_uri`, `recoverable`, and `next_actions`.
- Optional bounded `trace_id` may be added only if it does not create persistent state or expose secrets.

Verification:

- Resource transport tests assert resource-read error payloads are distinguishable by `response_kind=error`.
- Negative tests assert resource-read failures are not plain `error_code` + `message` documents.
- Empty list and not-found detail tests remain normal resource payload tests.

### Empty and Not-Found States

Current producers and references:

- `MetadataResourceHandler` emits `empty_reason` or `not_found_reason` plus `next_actions`.
- Detail resources keep `found`, `items`, and `count`.

Decision:

- Replace `empty_reason` and `not_found_reason` with canonical `empty_state`.
- Keep `found`, `items`, and `count`; they are stable resource state fields.
- Keep `next_actions`, but ensure actions use typed resource targets where applicable.

Canonical replacement:

- `empty_state` carries `state`, `reason`, `resource_kind`, and `safe_next_actions` or `next_actions`.
- Use bounded states such as `no_items`, `no_match`, `not_found`, `unsupported_scope`, and `missing_context`.

Verification:

- Tests assert `empty_state.state` and safe continuation actions.
- Negative tests assert `empty_reason` and `not_found_reason` are absent.

### Descriptor Schema Call Shape

Current producers and references:

- `MCPToolValueDefinition.toSchemaFragment` emits type, description, enum, object properties, required, and `additionalProperties`.
- Defaults and ranges for `page_size`, `max_rows`, and `timeout_ms` are encoded mainly in descriptions such as "Omit for default" and "Accepted range".
- Descriptor examples exist at tool level, but argument-level examples are not schema-visible.

Decision:

- Extend descriptor/value-definition metadata for `default`, `minimum`, `maximum`, `examples`, and `pattern` where the descriptor knows them.
- Delete prose-only default/range contracts from descriptions once schema fields exist.
- Keep human descriptions, but make them descriptive rather than the only source of machine constraints.

Canonical replacement:

- Argument schemas expose JSON Schema-compatible fields such as `default`, `minimum`, `maximum`, `examples`, and `pattern`.
- Validation and recovery use the same values as the schema, avoiding duplicated constants.

Verification:

- Descriptor schema tests assert machine-readable fields.
- Negative documentation/search checks assert old "Accepted range" and "Omit for default" descriptions are not the only contract.

### SQL Result Row Views

Decision:

- Keep positional `columns` and `rows`; they are lossless SQL result data, not compatibility shims.
- Add `row_objects` only when column labels are unique.
- Add `row_object_status` with unavailable reasons for duplicate or unnamed labels.

Verification:

- Tests assert `rows` remains present for result sets.
- Tests assert object rows are present only when safe and unavailable status is explicit otherwise.

### Resource Links

Decision:

- Resource links are optional transport affordances after canonical typed JSON resource references exist.
- Do not keep old URI-only fields solely because a client might ignore resource links.
- If SDK support is not stable, defer resource links without changing the canonical JSON shape.

Verification:

- If implemented, transport tests assert resource links and canonical `structuredContent` both exist.
- If deferred, tests still assert typed JSON references and no URI-only compatibility lists.

## Fields to Keep

Keep these fields unless a separate correctness analysis proves otherwise:

- `self_uri`, because it identifies the current resource payload.
- URI string values inside typed resource entries, because they are the actual MCP identifiers.
- `columns` and `rows`, because they preserve SQL result fidelity.
- `found`, `items`, `count`, `has_more`, and `next_page_token`, because they represent resource/list state.
- `error_code` and `message` in tool errors and resource error payloads.
- `issues[].message`, because issue text is user-facing diagnostic detail.
- `next_actions[].reason`, because it is display/explanation text paired with structured action fields.
- Preview and approval fields that enforce user control before side effects.

## Implementation Slices

1. Replace workflow missing-input payloads and delete `pending_questions` references.
2. Replace resource reference fields with typed resource entries across workflow, runtime status, metadata resources, and search hits.
3. Replace `complete_argument` helper and all call sites with executable completion actions.
4. Add resource-read error payloads and replace `empty_reason` / `not_found_reason` with `empty_state`.
5. Extend descriptor schema metadata for defaults, bounds, examples, and patterns; remove prose-only constraint contracts.
6. Add SQL row object convenience while retaining positional rows.
7. Add resource links only if SDK support is stable after typed JSON references are complete.

Each slice must update production code, descriptor contracts, tests, README examples, and negative shape assertions together.
