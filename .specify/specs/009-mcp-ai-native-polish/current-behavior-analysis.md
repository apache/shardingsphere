# Current Behavior Analysis: MCP AI-Native Polish

## Scope and Constraints

- Current branch observed during analysis: `001-shardingsphere-mcp`.
- This analysis is documentation-only and does not switch, create, or check out branches.
- The quality lens follows `CODE_OF_CONDUCT.md`: readability, consistency, and simplicity are the relevant rules for this lightweight polish.
- The goal is to make `shardingsphere-mcp` more native, convenient, comfortable, and clear for direct LLM use.
- Do not add a planner, vector search, memory, approval-token, RBAC, or default live-model CI system.

## Summary

The current MCP surface already has strong foundations and has advanced beyond the older 009 draft:

- Capabilities already expose `model_contract`, `next_action_contract`, `common_flows`, `security_hints`, payload contracts, protocol availability, and fingerprints.
- Search already exposes `searchContext`, `match_kind`, `matched_fields`, `matched_value`, and descriptor-backed typed resource hints.
- SQL outputs already expose `returned_row_count`, `applied_max_rows`, `applied_timeout_ms`, `truncated`, and `next_actions`.
- Side-effect previews already expose `approval_summary`, `approval_question`, side-effect scope, reusable arguments, and approval requirements.
- Recovery already covers invalid `object_types`, invalid page token, stale plan IDs, wrong SQL tool, unsupported tool/resource, and missing execution mode.
- Completion already exposes diagnostics, missing context, ranking policy, candidate counts, prefix-first matching, contains fallback, and value details.
- Resource responses already expose `self_uri`, typed `parent_resource` where applicable, typed `next_resources`, counts, and payload contracts.
- Opt-in LLM usability already covers capability discovery, search-to-detail URI, preview SQL, workflow preview/manual/validate, prompt completion, and recovery paths.

The remaining improvements are narrower:

- Multi-action `next_actions` still rely on list order and prose for dependencies.
- `retry_tool` can be clearer when context is compacted.
- Capabilities are rich; a tiny `surface_summary` would make first-hop use more comfortable.
- Resource navigation should keep source/target type hints centralized and consistent.
- Tool fields do not locally say whether completion is available.
- Empty, zero-hit, and not-found states can explain what happened more directly.
- Reusable arguments do not consistently expose provenance.
- Redacted workflow values can use a more uniform marker.
- EXPLAIN ANALYZE risk semantics can be clearer by database capability.
- Runtime failures can distinguish missing driver, authentication, timeout, and database-unavailable cases more safely.
- Opt-in usability reports can measure next-action following and approval violations directly.
- SQL and metadata calls need explicit defaults, caps, and invalid-argument recovery to avoid accidental unbounded work.
- Blank metadata search, broad all-database search, and percent-encoded identifiers need clearer contracts.
- Missing-input prompts need canonical field-level structure; MCP-native elicitation remains optional only when supported.
- Common Chinese encrypt/mask intents need deterministic synonym coverage or structured intent evidence.
- Workflow plans need a current-session read-back surface so models can resume after context compaction.
- Runtime setup needs secret-safe status, environment placeholders, clearer bearer-token hints, and minimal client examples.
- `complete_argument` next actions need enough data to call native completion and resume safely.
- Resource-read errors need a machine-readable error marker inside resource content.
- Resource references need typed purpose/kind hints so models do not infer why to read them.
- Descriptor schemas need machine-readable defaults, bounds, examples, and patterns instead of prose-only contracts.
- Replaced URI-only, prose-only, or duplicate old/new payload contracts should be deleted rather than kept as compatibility shims.
- `breaking-cleanup-analysis.md` records the field-level delete/replace/keep matrix for implementation.

## Finding 1: Capability Surface

Current behavior:

- `MCPDescriptorCatalog.toPayload()` includes `model_contract`, `next_action_contract`, `common_flows`, `security_hints`, resources, templates, tools, prompts,
  completion targets, resource navigation, protocol availability, and fingerprints.
- `ServerCapabilitiesHandlerTest` asserts `next_action_contract`, `common_flows`, payload contracts, security hints, core schemas, and legacy-field removal.

Opportunity:

- Add `surface_summary` above the rich catalog.
- Keep the summary static and short.
- Do not create a second discovery resource.

Suggested affected paths:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`
- `mcp/core/src/test/java/org/apache/shardingsphere/mcp/core/resource/handler/capability/ServerCapabilitiesHandlerTest.java`

## Finding 2: Next Action Dependencies

Current behavior:

- `execute_update` preview returns `ask_user` and `call_tool` actions.
- Workflow apply preview returns approval-gated follow-up actions.
- Recovery can return `retry_tool`, `call_tool`, `read_resource`, `complete_argument`, `ask_user`, and `stop`.
- Existing actions expose required arguments and approval flags, but not explicit order/dependency metadata.

Opportunity:

- Add optional `order`, `depends_on`, or `approval_dependency` fields for multi-action responses.
- Add `target_tool` or `source_tool` to `retry_tool` when known.
- Keep the action contract descriptive; no planner and no hidden execution.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowGuidancePayloadBuilder.java`

## Finding 3: Navigation and Completion Locality

Current behavior:

- `resourceNavigation` exposes `from`, `to`, required arguments, carried arguments, and description.
- `completionTargets` exposes reference type, reference, argument names, max values, and metadata.
- Completion responses include missing context diagnostics and ranking policy.

Opportunity:

- Add `from_type` and `to_type` to navigation payloads where the catalog can infer tool/resource/prompt kinds.
- Add local completion availability hints to tool fields, prompt arguments, or resource parameters where current completion targets already support them.
- Add next actions to completion diagnostics for missing context, such as reading databases before schema/table completion.

Suggested affected paths:

- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/descriptor/MCPDescriptorCatalog.java`
- `mcp/bootstrap/src/main/java/org/apache/shardingsphere/mcp/bootstrap/transport/completion/MCPCompletionSpecificationFactory.java`
- Descriptor catalog tests in `mcp/support` and completion tests in `mcp/bootstrap`

## Finding 4: Empty and Not-Found States

Current behavior:

- List resources expose `items`, `count`, `has_more`, optional pagination, and navigation.
- Detail resources expose `found`, `items`, `count`, optional `item`, and navigation.
- Search exposes context and match details, but a zero-hit result can still feel underspecified.

Opportunity:

- Add compact empty-state categories where safe.
- Include parent/list/search follow-up hints for not-found details.
- Keep zero SQL rows as a valid SQL result, not an error.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/resource/handler/metadata/MetadataResourceHandler.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/metadata/SearchMetadataToolService.java`
- `mcp/api/src/main/java/org/apache/shardingsphere/mcp/api/protocol/response/MCPItemsResponse.java`

## Finding 5: Argument Provenance and Redaction

Current behavior:

- `execute_update` preview exposes normalized SQL and `suggested_arguments`.
- Workflow planning/apply returns `plan_id`, artifacts, redacted sensitive properties, and reusable apply/validation guidance.
- Redaction is present, but provenance is not consistently explicit.

Opportunity:

- Add provenance for reusable arguments: user-provided, server-normalized, server-generated, server-defaulted, or redacted.
- Add standardized redaction markers for algorithm/workflow properties.
- Add manual-only follow-up guidance that asks the user to confirm external execution before validation.
- Add normalized SQL to success payloads when already classified and safe.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/tool/handler/execute/ExecuteUpdateToolHandler.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/tool/response/SQLExecutionResponse.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/workflow/service/WorkflowArtifactMaskUtils.java`
- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/workflow/WorkflowExecutionService.java`
- Encrypt and mask workflow services/descriptors

## Finding 6: Runtime Diagnostics

Current behavior:

- Error conversion returns structured recovery for many model mistakes.
- Startup hints and README cover Java version, token, driver path, STDIO stdout, empty surface, and workflow topology.
- JDBC/config exceptions can still collapse into broader unavailable/query-failed categories.

Opportunity:

- Add safe categories for missing JDBC driver, authentication failure, connection timeout, and database unavailable where exception type/message supports it.
- Optionally add bounded request/trace IDs if they do not require persistence or expose secrets.
- Add a short health-check shape to docs if needed.

Suggested affected paths:

- `mcp/core/src/main/java/org/apache/shardingsphere/mcp/core/protocol/error/MCPErrorConverter.java`
- `mcp/support/src/main/java/org/apache/shardingsphere/mcp/support/database/metadata/jdbc/RuntimeDatabaseConfiguration.java`
- `mcp/README.md`
- `mcp/README_ZH.md`

## Finding 7: Opt-In Usability Metrics

Current behavior:

- LLM usability tests are opt-in and outside default CI.
- Existing metrics cover task success, first correct action, invalid calls, round trips, resource hits, recovery, and query answer fidelity.

Opportunity:

- Add next-action-follow rate.
- Add approval-violation rate.
- Keep both metrics in the existing opt-in lane.

Suggested affected paths:

- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/assessment/LLMUsabilityMetricCalculator.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/assessment/LLMUsabilityScorecard.java`
- `test/e2e/mcp/src/test/java/org/apache/shardingsphere/test/e2e/mcp/llm/suite/usability/assessment/LLMUsabilityScenarioResult.java`

## Recommended Sequence

1. Add P0 action ordering and compact surface summary.
2. Add navigation/completion locality hints.
3. Add SQL/search input bounds, strict recovery, and URI encoding.
4. Add empty-state and not-found diagnostics.
5. Add provenance/redaction/manual-only follow-up refinements.
6. Add structured clarification, Chinese intent hints, and workflow read-back.
7. Add safe runtime and configuration diagnostics.
8. Add opt-in usability metrics.

## Implementation Readiness Recheck (2026-05-05)

This recheck narrows implementation to additive, model-facing fields. It avoids replacing existing response shapes because the current MCP surface is already richer than the
original gap analysis assumed.

### Next Actions

Confirmed producers:

- `ExecuteUpdateToolHandler` emits two preview actions: `ask_user` and `call_tool`.
- `MCPErrorConverter` emits `read_resource`, `complete_argument`, `retry_tool`, `call_tool`, and `ask_user` recovery actions.
- `WorkflowExecutionService` emits preview apply actions.
- `WorkflowGuidancePayloadBuilder` emits planning, apply, validation, and stop actions.
- `SQLExecutionResponse` emits a terminal `stop` action.

Remaining gap:

- Multi-action flows still rely on array order and prose for dependencies.
- `retry_tool` recovery actions do not carry a `target_tool` or `source_tool`; this is acceptable for "same tool" semantics, but less clear for models after error recovery.
- Add optional `order` and optional dependency fields such as `depends_on` or `approval_dependency` to multi-action producers only.
- Update `next_action_contract` to document optional sequencing fields without making every action verbose.

### Completion Locality

Confirmed behavior:

- `MCPCompletionSpecificationFactory.createMeta` already returns `missingContextArguments`, `diagnostic`, `rankingPolicy`, candidate counts, and `valueDetails`.
- Completion candidates support prefix matching, contains fallback, and recent-plan-first ordering for `plan_id`.
- `MCPDescriptorCatalog.toCompletionTargetPayload` exposes completion targets, but `toFieldPayload` only emits `name`, `required`, and `schema`.

Remaining gap:

- Completion results do not yet provide `next_actions` for `missing_context`, `no_candidates`, or `prefix_filtered_all_candidates`.
- Tool fields, prompt arguments, and resource parameters do not locally tell the model whether a value is completable.
- Add local hints derived from existing completion target descriptors instead of adding new descriptor files or a new completion registry.

### Empty and Not-Found States

Confirmed behavior:

- `MetadataResourceHandler` already distinguishes list and detail resources.
- Detail resources already return `found`, `items`, `count`, optional `item`, `self_uri`, typed `parent_resource`, and typed `next_resources`.
- `MCPItemsResponse` already provides `items`, `count`, `has_more`, optional `next_page_token`, and navigation.
- `SearchMetadataToolService` and `SearchMetadataToolHandler` already provide `search_context`, paging, match kind, matched fields, and derived resource URIs.

Remaining gap:

- A zero-hit search needs the canonical compact diagnostic shape `empty_state`.
- Detail not-found responses have `found=false`, but do not explain whether to read the parent/list resource, broaden search, or correct path arguments.
- Prefer adding empty diagnostics in `MetadataResourceHandler` and search-specific diagnostics in `SearchMetadataToolService` or `MetadataSearchResult`.
- Avoid widening `MCPItemsResponse` unless the hint is truly generic for every list response.

### Provenance, Redaction, and Manual Follow-Up

Confirmed behavior:

- `execute_update` preview already exposes `normalized_sql`, `approval_summary`, `approval_question`, and `suggested_arguments`.
- `SQLExecutionResponse` exposes execution status, row/update counts, applied limits, truncation, and terminal next actions.
- `WorkflowArtifactMaskUtils` masks sensitive workflow property values inside rule DistSQL artifacts.
- `WorkflowArtifactBundle` and `WorkflowArtifactPayloadUtils` are the central artifact payload construction points.

Remaining gap:

- `suggested_arguments` do not say which values came from the user, the server normalizer, server defaults, or generated workflow state.
- `SQLExecutionResponse` cannot expose `normalized_sql` without carrying classification metadata into the response.
- Masked workflow artifacts do not expose a compact redaction marker such as redacted property keys or redacted count.
- Manual-only workflow guidance already asks for external execution before validation, but artifact payloads can make the manual confirmation contract more explicit.

### Runtime Diagnostics

Confirmed behavior:

- `RuntimeDatabaseConfiguration.openConnection` loads the configured driver and opens via `DriverManager`.
- Missing configured JDBC driver throws `IllegalStateException`.
- `MCPJdbcStatementExecutor` catches `IllegalStateException` during transaction/open-connection setup and converts it to `MCPTransactionStateException`.
- Metadata/profile loading wraps `SQLException` as `IllegalStateException`.
- `MCPErrorConverter` maps generic `IllegalStateException` to `transaction_state_error`.

Remaining gap:

- Missing driver, authentication failure, connection timeout, and database unavailable can still be reported under broad or misleading categories.
- Keep diagnostics safe: include database name, category, retry/read-resource action, and never expose JDBC URL credentials or password values.
- The cleanest implementation point is a small runtime-connection diagnostic mapper used before generic exception wrapping, plus focused tests around driver missing, timeout,
  authentication/database unavailable messages, and metadata/profile loading.

## Final Requirement Sweep (2026-05-05)

This sweep re-asks whether a large model can use the MCP natively, conveniently, comfortably, and clearly without over-design.

### SQL and Metadata Bounds

Remaining gap:

- `execute_query` should have documented default and maximum row limits instead of leaving omitted limits effectively unbounded.
- Timeout and row-limit arguments should be preserved when preview responses provide suggested follow-up arguments.
- `search_metadata` should document whether blank query means "list within scope" and should require narrowing for expensive all-database scans.
- `page_size` and `page_token` should expose schema-visible min/max/defaults and structured recovery for invalid values.

Non-goals:

- Do not add a semantic search engine or model-ranked metadata explorer.

### URI and Identifier Handling

Remaining gap:

- Resource URI derivation and matching should support percent-encoded non-ASCII and reserved identifier characters.
- When identifiers cannot be safely represented by descriptor-backed patterns, responses should keep the existing "not safe to derive" fallback.

Non-goals:

- Do not invent guessed URIs or duplicate parser logic.

### Clarification, Localization, and Context Recovery

Remaining gap:

- Missing workflow inputs should be represented as structured questions, not only prose.
- Structured clarification fields should become canonical, with MCP-native elicitation mirroring them where supported.
- Common Chinese terms for encryption, masking, reversibility, hashing, equality query, fuzzy query, phone, identity card,
  and email should be handled through deterministic synonyms or structured intent evidence.
- A current-session workflow plan should be readable by `plan_id` so a model can recover after context compaction.

Non-goals:

- Do not add a full natural-language parser or cross-session memory.

### Runtime and Packaging Comfort

Remaining gap:

- Runtime status should expose secret-free setup and health information such as configured logical databases, loaded features, transport, and safe first checks.
- YAML configuration should support environment-variable placeholders for tokens and JDBC credentials.
- HTTP auth errors should clearly point to bearer-token requirements without leaking the token.
- Server identity should expose a machine-friendly name where protocol clients expect one.
- README examples should include minimal STDIO and HTTP client configuration shapes.

Non-goals:

- Do not add OAuth, RBAC, tenant policy, or an observability platform in this increment.

## Final Requirement Sweep (2026-05-06)

This pass asks the same question again from the model's point of view: "Where would I still have to guess?"
The answer is a set of concrete repair targets, not a larger architecture.

### Recovery Target Accuracy

Remaining gap:

- Missing or invalid `execution_mode` in `apply_workflow` should retry `apply_workflow`, not point the model toward `execute_update` by generic recovery wording.
- Missing workflow algorithm properties should identify public argument names such as `primary_algorithm_properties`, `assisted_query_algorithm_properties`, and `like_query_algorithm_properties`.
- Recovery actions should include `source_tool`, `target_tool`, `argument_path`, or `suggested_value` when these are safely known.

Non-goals:

- Do not add durable approval tokens, cross-tool orchestration, or an automatic retry executor.

### Response Mode and Preview Semantics

Remaining gap:

- `execute_update` schema and payload should make preview-vs-execute state obvious from stable fields.
- Preview output should state that it summarizes classification and side-effect scope, not real affected rows.
- Manual-only workflow output should make the "ask the user to confirm external execution before validation" contract structurally visible.

Non-goals:

- Do not estimate affected rows by running extra SQL.
- Do not make manual-only artifacts execute through MCP.

### Result Parsing Comfort

Remaining gap:

- SQL result payloads can add object-shaped rows when column labels are unique.
- If duplicate or unnamed columns prevent object rows, the response should say so with a small status field.
- Truncated or paginated responses should tell the model how to continue safely through `next_actions`.

Non-goals:

- Do not replace the lossless positional row representation.
- Do not invent column names or collapse duplicate labels.

### Metadata Intent and Ambiguity

Remaining gap:

- If a model uses console-style metadata SQL such as `SHOW TABLES` or `DESCRIBE`, recovery should steer it toward logical metadata resources or `search_metadata`.
- Duplicate table/column/rule names across databases, schemas, or object types should produce an ambiguity hint and narrowing arguments.
- URI encoding should be centralized for search result URIs, resource navigation, and workflow `resources_to_read` so non-ASCII and reserved characters behave consistently.

Non-goals:

- Do not add semantic metadata search, model-ranked results, or guessed URIs.

### Runtime Setup Comfort

Remaining gap:

- Docker and HTTP examples should show bind host, bearer token, and environment-placeholder patterns without embedding secrets.
- Runtime preflight or status hints should stay secret-free and limited to configured logical databases, feature loading, transport, and safe first checks.
- Proxy-topology errors in encrypt/mask flows should explain that the workflow must connect to ShardingSphere-Proxy's logical view,
  not directly to a physical database, when the server can identify that mismatch.

Non-goals:

- Do not add OAuth, RBAC, tenant policy, or a generalized health monitoring platform.

## Last Guess-Reduction Sweep (2026-05-06)

This sweep asks again where the model still has to join catalog sections, infer protocol semantics, or parse prose.
The items below are concrete field-level gaps and should stay additive.

### Completion Continuation

Confirmed behavior:

- `MCPNextActionUtils.completeArgument` currently emits `action_kind`, `argument_name`, `reason`, and `requires_user_approval`.
- `MCPCompletionSpecificationFactory` can identify missing context and returns `complete_argument` actions.
- `MCPDescriptorCatalog` already exposes `completionTargets`, so completion references exist elsewhere in the catalog.

Remaining gap:

- A model must infer the completion reference, known context arguments, prefix, and resume call from other sections.
- Add `reference_type`, `reference`, `context_arguments`, `missing_context_arguments`, `argument_prefix`,
  `resume_target_type`, `resume_target`, and `resume_arguments` when the server can know them.
- Keep native MCP completion as the mechanism; do not add another helper tool.

### Resource Error Recognition

Confirmed behavior:

- `MCPTransportPayloadUtils.createCallToolResult` marks tool errors with `isError`.
- `MCPTransportPayloadUtils.createReadResourceResult` wraps JSON as `TextResourceContents`.
- Resource reads therefore do not expose the same tool-style `isError` marker.

Remaining gap:

- If a resource read fails, a model needs `response_kind=error`, `error_code`, `original_uri`, and recovery `next_actions`
  inside the JSON payload.
- Keep the message secret-safe and avoid raw stack traces, credentials, tokens, or environment dumps.

### Typed Resource Hints

Confirmed behavior:

- `WorkflowGuidancePayloadBuilder.createResourcesToRead` returns a list of URI strings.
- Metadata resources return `next_resources` as URI strings.
- Runtime status already uses `resources_to_read` as bare URIs for first checks.

Remaining gap:

- Bare URIs force the model to guess whether a resource is a parent, detail, capability, algorithm catalog, validation evidence, or next page.
- Add typed entries with `uri`, `resource_kind`, `purpose`, `reason`, and `source_field`.
- Delete bare URI-only list fields from canonical payloads once typed resource hints replace them, unless a protocol-owned field requires a URI string.

### Schema-Visible Call Shape

Confirmed behavior:

- `MCPToolValueDefinition.toSchemaFragment` emits type, description, enum, object properties, required fields, and `additionalProperties`.
- Current descriptors often mention defaults and ranges in descriptions, such as `page_size` default/range and `max_rows` default/range.

Remaining gap:

- Models must parse descriptions to learn defaults, minimums, maximums, examples, and patterns.
- Add schema-visible fields when descriptor metadata already knows them, and keep descriptions only as human-readable display text.
- Do not introduce a separate validation language; stay within JSON Schema fields the SDK can carry.

### Resource Links

Confirmed behavior:

- Tool results currently carry `structuredContent` plus text JSON.
- Tool payloads already include structured resource hints and `next_actions` in JSON `structuredContent`.

Remaining gap:

- Clients that understand MCP resource links could expose direct navigation, while JSON `structuredContent` remains the canonical payload path.
- Add resource links only as optional transport content when SDK/client support is available.
- Do not keep old URI-only payload fields solely for clients that ignore resource links.
