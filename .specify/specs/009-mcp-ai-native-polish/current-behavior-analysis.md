# Current Behavior Analysis: MCP AI-Native Polish

## Scope and Constraints

- Current branch observed during analysis: `001-shardingsphere-mcp`.
- This analysis is documentation-only and does not switch, create, or check out branches.
- The quality lens follows `CODE_OF_CONDUCT.md`: readability, consistency, and simplicity are the relevant rules for this lightweight polish.
- The goal is to make `shardingsphere-mcp` more native, convenient, comfortable, and clear for direct LLM use.
- Do not add a planner, vector search, memory, approval-token, RBAC, or default live-model CI system.

## Summary

The current MCP surface already has strong foundations: descriptor-backed tools, a capabilities resource, structured `next_actions`,
workflow preview gates, recovery payloads, and opt-in LLM usability tests.
The remaining improvements are small clarity gaps.
A model can usually finish the task today, but it must infer too much from prose or missing fields in these areas:

- Capabilities do not yet document the `next_actions` vocabulary or compact common flows.
- Search results do not explain match quality, matched fields, applied scope, or unsafe URI derivation details.
- Numeric arguments silently fall back to defaults on malformed input.
- SQL result payloads lack applied limit and row-count hints.
- Preview payloads expose safety facts but do not provide a server-owned approval question.
- Workflow validation success returns no explicit stop action, while failure does.
- Some recovery payloads are structured, but invalid `object_types` and page tokens are less actionable.
- Deterministic tests exist, and opt-in LLM tests exist, but intent-style model tests can be more natural.

## Finding 1: Capabilities Contract

Current behavior:

- `MCPDescriptorCatalog.toPayload()` exposes supported resources, tools, statement classes, `model_contract`, `security_hints`,
  resources, templates, tools, prompts, completion targets, resource navigation, protocol availability, and fingerprints.
  Evidence: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java:88`.
- `model_contract` already tells models to start from capabilities, use databases for metadata, choose SQL tools by side effect,
  use workflow sessions, avoid legacy recommendation fields, follow detail resources, and respect recovery actions.
  Evidence: `MCPDescriptorCatalog.java:124`.
- There is no explicit `next_action_contract` or `common_flows` section yet.
- `ServerCapabilitiesHandlerTest` protects the current capability surface and legacy-field removal.
  Evidence: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandlerTest.java:36`.

Opportunity:

- Add a static `next_action_contract` section that defines `action_kind` values and required fields for `read_resource`, `call_tool`,
  `retry_tool`, `complete_argument`, `ask_user`, and `stop`.
- Add compact `common_flows` for metadata inspection, read-only query, side-effecting SQL preview, workflow planning, workflow validation, and recovery.
- Keep this in capabilities rather than adding a new planning tool.

Suggested affected paths:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandlerTest.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoader.java` if flow references should be linted.

## Finding 2: Next Actions Vocabulary

Current behavior:

- SQL execution success uses `stop` in `SQLExecutionResponse`.
  Evidence: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponse.java:139`.
- `execute_update` preview uses `ask_user` and `call_tool` with `requires_user_approval=true`.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java:109`.
- Error recovery creates `read_resource`, `complete_argument`, `retry_tool`, `call_tool`, and `ask_user` actions.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java:378`.
- Workflow planning and apply responses add `ask_user` and `call_tool` actions.
  Evidence: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowGuidancePayloadBuilder.java:56` and `:70`.
- Workflow apply preview builds a `call_tool` action requiring user approval.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java:178`.

Opportunity:

- Document the existing vocabulary rather than refactoring the producers first.
- Require each action kind to expose a minimal stable shape so models can parse it without guessing.
- Preserve existing producer-specific fields such as `required_arguments`, `target_resource`, `target_tool`, `argument_name`, and `required_inputs`.

Suggested affected paths:

- Capabilities contract tests first.
- Optional descriptor/catalog lint after the contract exists.

## Finding 3: `search_metadata` Context and Match Explanation

Current behavior:

- `SearchMetadataToolService.execute()` rejects a schema without database, otherwise uses either the specified database or all query databases.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java:64`.
- Missing `object_types` defaults to database, schema, table, view, column, index, and sequence (`SearchMetadataToolService.java:373`).
- `matchesQuery()` only checks case-insensitive containment against `name`, `table`, and `view` (`SearchMetadataToolService.java:302`).
- `MetadataSearchHit` returns object identity and URI derivation fields only. It does not return `match_kind`, `matched_fields`,
  or applied search context.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/response/MetadataSearchHit.java:32`.
- `MetadataSearchResult` contains only `items` and `nextPageToken` (`mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/response/MetadataSearchResult.java:27`).
- Pagination uses integer offsets; invalid tokens throw `InvalidPageTokenException`.
  Evidence: `SearchMetadataToolService.java:322` and
  `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/exception/InvalidPageTokenException.java:29`.
- URI derivation only rejects blank names or names containing `/`; names containing spaces, `?`, or `#` are still URI-usable.
  Evidence: `SearchMetadataToolService.java:391`.

Opportunity:

- Add low-cost fields: `search_context`, `match_kind`, `matched_fields`, and possibly `matched_value`.
- Tighten URI derivation conservatively and return `derivation_status=not_safe` with a clear `derivation_reason` for unsafe names.
- Add page metadata such as applied `page_size` and result count only if it stays lightweight.
- Add recovery for invalid page token if it becomes a common model failure.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/response/MetadataSearchHit.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/response/MetadataSearchResult.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolServiceTest.java`

## Finding 4: Numeric Argument Defaults

Current behavior:

- `MCPToolArguments.getIntegerArgument()` returns the default for missing, blank, and invalid numeric strings.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArguments.java:105`.
- Tests codify the silent default behavior for invalid integers (`mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArgumentsTest.java:106`).
- SQL handlers use `max_rows` default `0` and `timeout_ms` default `0`.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/SQLExecutionToolHandlerSupport.java:38`.
- JDBC query timeout is applied only when `timeout_ms > 0`, converted upward to seconds.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/MCPJdbcStatementExecutor.java:179`.

Opportunity:

- Do not reject omitted numeric arguments.
- Either reject malformed provided numeric values with structured recovery, or keep defaulting but expose `applied_max_rows` and `applied_timeout_ms` in responses.
- Prefer explicit applied hints first because it is less disruptive.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/request/MCPToolArguments.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponse.java`
- `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponseTest.java`

## Finding 5: SQL Output Parse Hints

Current behavior:

- `SQLExecutionResponse.toPayload()` returns `result_kind`, `statement_class`, `statement_type`, `status`, result-specific fields,
  `truncated`, and `next_actions`.
  Evidence: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponse.java:121`.
- Result-set truncation is computed with effective max rows. The payload only exposes `truncated`, not the limit or returned count.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/MCPJdbcStatementExecutor.java:135`.
- Existing tests assert exact payload shapes without row-count or applied-default hints.
  Evidence: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponseTest.java:94`.

Opportunity:

- Add `returned_row_count`, `applied_max_rows`, and `applied_timeout_ms` where values are already known.
- Keep this as parse hints, not a new SQL introspection tool.
- Update descriptor output schemas alongside payload tests.

Suggested affected paths:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponse.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/MCPJdbcStatementExecutor.java`
- `mcp/core/src/main/resources/META-INF/shardingsphere-mcp/descriptors/core.yaml`

## Finding 6: Approval Summary and Workflow Payloads

Current behavior:

- `execute_update` preview returns `execution_mode=preview`, `status=AWAITING_APPROVAL`, `would_execute=false`, normalized SQL,
  statement class/type, side-effect scope, target object, approval guidance, suggested arguments, read resources, and next actions.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java:90`.
- It does not expose a server-owned `approval_summary` or `approval_question`.
- `apply_workflow` preview returns `would_apply=false`, `preview_artifacts`, `requires_user_approval=true`, and a `call_tool` next action.
  Evidence: `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java:155`.
- Workflow apply common responses include `issues`, `step_results`, executed/skipped/manual artifacts, and guidance (`WorkflowExecutionService.java:285`).
- Workflow validation failure includes `recommended_recovery` and `next_actions`; validation success leaves `next_actions` empty.
  Evidence: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowGuidancePayloadBuilder.java:93`.
- Descriptor schemas expose top-level workflow fields, but nested item shapes and status enums are relatively loose.
  Evidence: `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml:174` and `:260`.

Opportunity:

- Add concise `approval_summary` or `approval_question` to SQL and workflow preview responses.
  This lets models avoid paraphrasing risk wording from scattered fields.
- Add status vocabulary and nested item schemas for `preview_artifacts`, `step_results`, `issues`, and `mismatches`.
- Consider explicit validation success `stop` action for consistency with SQL execution success.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowGuidancePayloadBuilder.java`
- `mcp/support/src/main/resources/META-INF/shardingsphere-mcp/descriptors/support.yaml`
- `mcp/features/encrypt/src/main/resources/META-INF/shardingsphere-mcp/descriptors/encrypt.yaml`
- `mcp/features/mask/src/main/resources/META-INF/shardingsphere-mcp/descriptors/mask.yaml`

## Finding 7: Recovery Paths

Current behavior:

- Unsupported tools/resources direct the model to read capabilities (`MCPErrorConverter.java:182` and `:192`).
- SQL tool mismatch returns normalized SQL, suggested arguments, and a target tool action (`MCPErrorConverter.java:202`).
- Missing database recovery directs the model to `shardingsphere://databases`; generic missing arguments use `ask_user` (`MCPErrorConverter.java:293`).
- Workflow state recovery suggests plan-id completion and capabilities (`MCPErrorConverter.java:351`).
- Invalid `object_types` returns allowed values and `ask_user_when_uncertain=false`, but no `next_actions` or `suggested_arguments`.
  Evidence: `MCPErrorConverter.java:317`.
- Invalid page token currently produces the generic invalid request path unless callers add special handling (`InvalidPageTokenException.java:29`).

Opportunity:

- Add `next_actions` to invalid `object_types` recovery, likely a `retry_tool` with allowed values or a `read_resource` capabilities action.
- Add invalid page token recovery that tells the model to retry without `page_token` or use the previous `next_page_token`.
- Keep generic missing argument recovery as-is unless model tests show confusion.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/MCPErrorConverterTest.java`

## Finding 8: Descriptor Lint and Deterministic Tests

Current behavior:

- `MCPDescriptorCatalogLoader` validates known enum fields, output schemas, legacy recommendation fields, required core output fields,
  `search_metadata` item fields, prompt guidance, completion references, resource navigation, and destructive tool metadata.
  Evidence: `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoader.java:263`.
- `MCPDescriptorCatalogLoaderTest` asserts workflow tools and no legacy recommendation fields.
  Evidence: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoaderTest.java:36`.
- `ServerCapabilitiesHandlerTest` asserts capabilities shape, core tools, payload contracts, security hints, and no legacy recommendation fields.
  Evidence: `ServerCapabilitiesHandlerTest.java:36`.

Opportunity:

- Extend deterministic contract tests for `next_action_contract`, `common_flows`, search match fields, SQL parse hints, and approval summaries.
- Add lint for common-flow references only if the new flows are descriptor-backed enough to validate cheaply.
- Avoid broad golden transcripts for this increment.

Suggested affected paths:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoader.java`
- `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoaderTest.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandlerTest.java`

## Finding 9: Opt-In LLM E2E

Current behavior:

- LLM E2E is opt-in via `mcp.e2e.llm.enabled=false` by default (`test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/env/MCPE2ETestConfiguration.java:58`).
- LLM configuration defaults to an OpenAI-compatible local endpoint and model (`test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/config/LLME2EConfiguration.java:64`).
- The smoke prompt forces an exact sequence: `search_metadata`, `mcp_read_resource`, and `execute_query` (`test/e2e/mcp/src/test/resources/llm/suite/smoke/minimal-smoke-user-prompt.md:3`).
- The usability suite already includes capabilities, table resources, search detail URI, preview update, workflow preview/manual/validate,
  prompt completion, resource discovery, database disambiguation, and recovery scenarios.
  Evidence: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/scenario/LLMUsabilityScenarioCatalog.java:49`.
- The runner requires tool coverage, blocks unsafe SQL/update/workflow execution, records capability fingerprints, and validates final JSON.
  Evidence: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/conversation/LLMMCPConversationRunner.java:153` and `:216`.

Opportunity:

- Keep live-model tests opt-in.
- Add one or two intent-style scenarios after the deterministic contract work, where the prompt gives the goal but does not prescribe exact next actions.
- Use those scenarios as usability signal, not default CI blockers.

Suggested affected paths:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/scenario/LLMUsabilityScenarioCatalog.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/scenario/LLMUsabilityScenarioCatalogTest.java`

## Proposed Implementation Order

1. Add capabilities `next_action_contract` and compact `common_flows`.
2. Add deterministic tests for the new capabilities fields and reference integrity.
3. Add search context and match explanation fields with unsafe URI derivation behavior.
4. Add SQL parse hints where values are already known.
5. Add approval summaries to SQL preview and workflow preview.
6. Tighten workflow descriptor schemas and add validation success stop action only if it keeps payload semantics simple.
7. Add small recovery improvements for invalid `object_types` and page tokens.
8. Add opt-in intent-style LLM scenarios after deterministic tests are stable.

## Non-Goals Reconfirmed

- No planner or model-orchestration tool.
- No semantic/vector search.
- No memory or conversation state beyond existing workflow sessions.
- No approval token, approval ledger, RBAC, or policy engine.
- No default CI dependency on live model providers.
- No broad response golden snapshots unless focused shape tests become insufficient.

## Verification Map

- Capabilities: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandlerTest.java`
- Descriptor quality: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalogLoaderTest.java`
- Search behavior: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolServiceTest.java`
- Error recovery: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/protocol/MCPErrorConverterTest.java`
- SQL output: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponseTest.java`
- SQL/update preview: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandlerTest.java`
- Workflow apply: `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionServiceTest.java`
- Workflow validation: `mcp/support/src/test/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowValidationSupportTest.java`
- Opt-in LLM usability: `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/LLMUsabilitySuiteE2ETest.java`

## Rollback Boundary

All proposed changes can be rolled back by removing the added model-facing fields and their tests.
No persisted data format, public SQL behavior, transport protocol, branch topology, or workflow lifecycle state needs to change.
