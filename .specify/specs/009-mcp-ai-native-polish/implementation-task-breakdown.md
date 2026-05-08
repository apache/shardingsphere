# Implementation Task Breakdown: MCP AI-Native Polish

**Date**: 2026-05-06
**Branch observed**: `001-shardingsphere-mcp`
**Branch rule**: Do not switch, create, or check out branches.
**Source**: `final-requirement-inventory.md`

## Accepted Defaults

- Start with P0 slices: URI encoding, continuation, ambiguity, recovery target, workflow response mode, and next-action sequencing.
- Omit optional bounded `request_id` for now.
- Keep `ResourceLink` additive; JSON `structuredContent` stays canonical.
- Gate MCP-native elicitation on SDK and client capability; structured JSON clarification stays canonical fallback.
- Remove URI-only, prose-only, and duplicate old/new payload fields when a canonical replacement lands.
- Preserve correctness fields such as `self_uri`, positional SQL rows, resource counts, and approval fields.

## Slice Order

1. **P0-A URI encoding**: smallest foundational slice because later resource hints and continuations depend on stable URIs.
2. **P0-B Continuation and ambiguity**: metadata and SQL follow-up comfort, grouped because both guide the model away from guessing.
3. **P0-C Recovery target completeness**: exact repair target and public argument paths for model retries.
4. **P0-D Workflow response mode**: align workflow status markers with SQL response-mode clarity.
5. **P0-E Next-action sequencing contract**: cross-cutting negative and consistency tests after producers settle.
6. **P1 queue**: runtime, EXPLAIN, completion, topology, and empty/not-found comfort.
7. **P2 queue**: opt-in metrics and docs-only/package metadata polish.

## P0-A: URI Encoding

### Tasks

- [x] TB001 [P0-A] Inspect URI producers in `mcp/core`, `mcp/support`, `mcp/features/encrypt`, and `mcp/features/mask`.
- [x] TB002 [P0-A] Add tests for non-ASCII, spaces, slash-like reserved characters, question marks, percent signs, and round-trip resource reads.
- [x] TB003 [P0-A] Centralize path-segment encoding in one MCP URI utility; keep decode behavior in `MCPUriPattern`.
- [x] TB004 [P0-A] Replace local encoding helpers in search result URI generation, resource navigation, runtime hints, execute-update resources, and workflow resource hints.
- [x] TB005 [P0-A] Add a cleanup assertion that `rg "URLEncoder.encode" mcp` only finds the central helper or justified tests.

### Verification

- `./mvnw -pl mcp/api,mcp/core,mcp/support -DskipITs -Dspotless.skip=true -Dtest=*Uri*,*MetadataResource*,*SearchMetadata* test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/api,mcp/core,mcp/support -am -Pcheck -DskipTests checkstyle:check`
- `rg "URLEncoder.encode" mcp`

## P0-B: Continuation and Ambiguity

### Tasks

- [x] TB006 [P0-B] Add SQL truncation tests that assert `next_actions` asks for a narrower query when continuation is not safely possible.
- [x] TB007 [P0-B] Add metadata pagination tests that assert `has_more=true` and `next_page_token` produce a safe retry action with copied scope.
- [x] TB008 [P0-B] Add duplicate-name metadata tests across database, schema, and object type dimensions.
- [x] TB009 [P0-B] Implement continuation action builders using existing `MCPNextActionUtils`; include `target_tool`, `required_arguments`, `order`, and safe reason text.
- [x] TB010 [P0-B] Implement `ambiguity` payload fields only when candidates are already available or cheap to count.
- [x] TB011 [P0-B] Update descriptor/output schema fragments for continuation and ambiguity fields where model-visible.

### Verification

- `./mvnw -pl mcp/core,mcp/support -DskipITs -Dspotless.skip=true -Dtest=SQLExecutionResponseTest,SearchMetadataToolServiceTest test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/core,mcp/support -am -Pcheck -DskipTests checkstyle:check`

## P0-C: Recovery Target Completeness

### Tasks

- [x] TB012 [P0-C] Add invalid `max_rows`, `timeout_ms`, `page_size`, and `page_token` recovery tests with public `argument_path` and bounds.
- [x] TB013 [P0-C] Add `apply_workflow.execution_mode` recovery tests for missing, invalid, and unsafe execute-without-approval cases.
- [x] TB014 [P0-C] Add workflow missing-property tests for `primary_algorithm_properties`, `assisted_query_algorithm_properties`, and `like_query_algorithm_properties`.
- [x] TB015 [P0-C] Update recovery conversion so retry actions include `source_tool`, `target_tool`, `argument_path`, `suggested_value`, and reusable safe arguments when known.
- [x] TB016 [P0-C] Ensure recovery never points `apply_workflow` argument repair at `execute_update` unless the actual action is changing tools.

### Verification

- `./mvnw -pl mcp/core,mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true -Dtest=MCPErrorConverterTest,*Workflow*Test test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/core,mcp/features/encrypt,mcp/features/mask -am -Pcheck -DskipTests checkstyle:check`

## P0-D: Workflow Response Mode

### Tasks

- [x] TB017 [P0-D] Add workflow plan/apply/validate tests for `response_mode` values: `preview`, `executed`, `manual_only`, `validation`, `recovery`, and `terminal`.
- [x] TB018 [P0-D] Add manual-only tests that assert the user must confirm external execution before validation.
- [x] TB019 [P0-D] Add preview tests that assert no affected-row estimate or hidden execution is implied.
- [x] TB020 [P0-D] Implement workflow response-mode fields in shared workflow payload builders instead of duplicating per feature.
- [x] TB021 [P0-D] Update encrypt and mask descriptor schemas only for fields that are actually emitted.

### Verification

- `./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask -DskipITs -Dspotless.skip=true -Dtest=*Workflow*Test test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/support,mcp/features/encrypt,mcp/features/mask -am -Pcheck -DskipTests checkstyle:check`

## P0-E: Next-Action Sequencing Contract

### Tasks

- [x] TB022 [P0-E] Add contract tests that every multi-action response with approval dependency exposes `order` or `depends_on`.
- [x] TB023 [P0-E] Add contract tests that `retry_tool`, `call_tool`, `read_resource`, and `complete_argument` include source or target metadata when known.
- [x] TB024 [P0-E] Add negative checks that next actions do not contain approval tokens, hidden execution flags, or planner-like orchestration fields.
- [x] TB025 [P0-E] Update `next_action_contract` in capabilities after producer behavior is stable.
- [x] TB026 [P0-E] Run documentation searches for removed URI-only, prose-only, and duplicate old/new payload fields.

### Verification

- `./mvnw -pl mcp/core,mcp/support,mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest=*Capability*,*Transport*,*Error*,*Workflow* test -Dsurefire.failIfNoSpecifiedTests=false`
- `./mvnw -pl mcp/core,mcp/support,mcp/bootstrap -am -Pcheck -DskipTests checkstyle:check`
- `rg "pending_questions|resource_uri|parent_uri|next_resource_uris|read_resources_first|empty_reason|not_found_reason" mcp`

## P1 Queue

- [x] TB027 [P1] Add EXPLAIN ANALYZE risk wording to database capability payloads or docs where engine semantics are known.
- [x] TB028 [P1] Add single-schema completion auto-resolution or schema-first next action tests and implementation.
- [x] TB029 [P1] Enrich runtime status with active transport, feature availability, and safe first checks without exposing secrets.
- [x] TB030 [P1] Add Proxy topology guidance for encrypt/mask workflows as recovery or documentation only.
- [x] TB031 [P1] Normalize empty, zero-hit, and not-found follow-up shapes where gaps remain.

## P2 Queue

- [x] TB032 [P2] Keep bounded `request_id` omitted; document this decision in implementation notes if runtime recovery is touched.
- [x] TB033 [P2] Improve opt-in approval-violation metric to inspect trace behavior, not only error codes.
- [x] TB034 [P2] Keep token-safe health-check docs aligned with runtime/capabilities resources.
- [x] TB035 [P2] Add HTTP package metadata shape only if public packaging becomes part of the release slice.
- [x] TB036 [P2] Keep MCP-native elicitation capability-gated at the transport boundary.

TB035 is complete as a bounded no-op for this slice: public HTTP package metadata is not part of the accepted release surface.

## 2026-05-08 Continuous Optimization 100 Queue

This queue implements `continuous-optimization-100.md`. It is the stricter sustained-improvement closure after the accepted 009 baseline, not a reset of the completed P0/P1/P2 slices.

### Accepted Defaults

- Keep JSON `structuredContent` canonical.
- Add or cap protocol-native content only at the transport boundary.
- Prefer fixed in-code limits over new configuration knobs unless a test proves the limit must be user-controlled.
- Preserve scoped blank-query metadata discovery; only guard the blank query across all databases and all object types.
- Treat Qwen live scoring as opt-in local Ollama evidence; an unavailable service or missing `qwen3:1.7b` model is a validation blocker, not a product feature failure.

### Slice CO-A: Context Size And Broad Discovery

- [x] TB037 [CO-A] Add tests for broad direct metadata list resources that assert capped `items`, `total_count`, `returned_count`, `truncated`, and narrowing `next_actions`.
- [x] TB038 [CO-A] Implement direct non-detail metadata list capping in `MetadataResourceHandler`; leave detail resources unchanged.
- [x] TB039 [CO-A] Add tests for blank all-database `search_metadata` calls that assert compact database or scope guidance instead of all-object expansion.
- [x] TB040 [CO-A] Implement the blank all-database search guard without changing non-empty cross-database searches or database-scoped blank searches.
- [x] TB041 [CO-A] Update descriptor payload contracts and README snippets only for the new emitted fields.

### Slice CO-B: ResourceLink And Ambiguity

- [x] TB042 [CO-B] Add transport tests for ResourceLink priority, maximum emitted link count, and omitted-link metadata.
- [x] TB043 [CO-B] Implement bounded ResourceLink collection in `MCPTransportPayloadUtils` and put count metadata in `CallToolResult.meta`.
- [x] TB044 [CO-B] Add search tests for duplicate names split across pages and `total_match_count`.
- [x] TB045 [CO-B] Extend `MetadataSearchResult` and search payload metadata with pre-pagination match count and global ambiguity details.
- [x] TB046 [CO-B] Update search output schema for `total_match_count` and any global ambiguity fields.

### Slice CO-C: Recovery Durability And Manual Boundaries

- [x] TB047 [CO-C] Add runtime-status tests for a compact capability or descriptor-catalog fingerprint reference plus typed capabilities resource hint.
- [x] TB048 [CO-C] Implement the runtime fingerprint reference without exposing secrets or duplicating the full capability catalog.
- [x] TB049 [CO-C] Add manual-only workflow tests for a compact manual artifact summary.
- [x] TB050 [CO-C] Implement manual artifact summary with counts, external-execution requirement, approval requirement, and validation next action.
- [x] TB051 [CO-C] Add descriptor anti-regression tests for model-critical metadata on new tools, resources, prompts, completion targets, annotations, examples, and side-effect metadata.

### Slice CO-D: Live And Long-Context Evidence

- [x] TB052 [CO-D] Add or extend an opt-in LLM E2E scenario that simulates context loss and recovers through `plan_id`, `shardingsphere://workflows/{plan_id}`, runtime status, or typed resource hints.
- [x] TB053 [CO-D] Run the deterministic non-LLM MCP smoke for touched HTTP/STDIO transport paths when transport behavior changes.
- [x] TB054 [CO-D] Run the Qwen LLM usability suite against the local Ollama `qwen3:1.7b` E2E stack and require `overallScore=100.0`; cloud-hosted Qwen credentials are not part of this gate.
- [x] TB055 [CO-D] Record the deterministic score, local Ollama live score or readiness blocker, capability fingerprints, and final repeated-question review result.

## Cross-Slice Guardrails

- Add deterministic tests before changing each model-facing payload.
- Keep tests dedicated by public method and scenario.
- Default to Mockito for heavy runtime, database, cache, registry, network, or SDK boundary dependencies.
- Use SPI loaders for SPI components instead of direct construction when applicable.
- Keep single-use locals inline unless reused or materially clearer.
- Delete replaced compatibility code, tests, and docs in the same slice.
- Do not edit generated `target/` paths.
- Do not run branch-changing commands.

## Final Verification For Any Code Slice

- Run the scoped module tests listed in that slice.
- Run scoped Checkstyle with `-Pcheck` for touched Java modules.
- Run `git diff --check`.
- Confirm `git branch --show-current` returns `001-shardingsphere-mcp`.
