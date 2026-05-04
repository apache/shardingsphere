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

# Feature Specification: AI-Friendly MCP Tool and Resource Surface

**Feature Branch**: `001-shardingsphere-mcp`  
**Created**: 2026-05-03  
**Status**: Draft  
**Input**: User request to use Spec Kit style requirements and catalog the gaps that prevent MCP tools and resources from being natural for large language models, without switching branches.

## Process Constraints

- Stay on the current branch. Do not run `git switch`, `git checkout`, branch-creating Spec Kit commands, or any other branch-changing command.
- This specification follows the existing repository Spec Kit format manually because the repository does not currently contain `.specify` command scaffolding.
- This document is requirement clarification only. Implementation, deletion, API migration, test rewrites, and documentation updates are separate follow-up work.
- Do not edit generated paths such as `target/`.
- Any later operation that deletes files, performs wide API migration, commits, pushes, or changes dependency versions requires explicit confirmation.

## Summary

The current ShardingSphere MCP surface is protocol-valid, but it is not yet fully AI-friendly.
Tools expose descriptors and input schemas, but several descriptions are too short.
Object parameters are under-specified, output schemas are missing, workflow sequencing is implicit, and side effects are not strongly signaled.
Resources expose URI patterns and generic generated descriptions, but they do not expose business-level descriptors that tell a model when and why to read each resource.

The target is to add a model-facing semantic contract across tools, resources, capabilities, workflow responses, and recoverable errors.
A large language model should be able to discover the MCP surface, understand which resource or tool fits a user intent,
fill parameters with fewer guesses, read relevant context before acting, and continue multi-step workflows through explicit next-action hints.

## Problem Statement

Observed gaps in the current MCP surface:

- Resource handlers expose URI patterns and handlers, but not business descriptors.
- Runtime resource descriptions are generated from URIs, so they are valid but not semantically useful.
- Resource template variables such as `database`, `schema`, `table`, and `column` do not have protocol-visible meaning.
- `shardingsphere://capabilities` returns supported resource and tool identifiers, but not rich descriptors or workflow relationships.
- Tool descriptors exist, but some descriptions do not explain when to call the tool, what not to expect, prerequisite context, follow-up tools, or side effects.
- Critical object parameters use open-ended schemas, making hidden keys discoverable only by reading implementation code.
- Core tools and workflow tools do not publish output schemas, so models must infer response structure from examples or previous calls.
- Workflow planning responses do not provide a fully stable model-facing contract for recommended next calls, required user approval, related resources, or missing inputs.
- Error responses are not consistently structured for model recovery.
- Tool/resource relationships are documented in README prose, but not consistently exposed through the MCP protocol surface.

## Goals

- Make every public MCP resource and resource template self-describing through protocol-visible metadata.
- Make every public MCP tool self-describing enough for model-driven selection and parameter filling.
- Expose resource and tool relationships so a model can read relevant context before calling action tools.
- Add stable input and output schema contracts for model-facing tools.
- Add workflow guidance fields so planning, apply, and validation steps can be chained naturally.
- Add recoverable error details that help a model repair the next call.
- Keep ShardingSphere logical database, schema, table, column, rule, and algorithm semantics explicit.
- Keep side-effect risk clear for SQL execution and workflow application.

## Non-Goals

- Do not change encrypt, mask, metadata, SQL execution, or workflow business behavior in this requirement phase.
- Do not force all MCP clients to auto-read resources. The server must expose metadata; host applications decide how to surface it to models.
- Do not replace URI-based resources with natural-language-only resources.
- Do not make `apply_workflow` or SQL execution run without user-visible approval policy.
- Do not implement the requirements in this document until the implementation scope is explicitly approved.
- Do not implement MCP prompts or completion endpoints in the first implementation phase.
  They remain recorded requirements for a later phase.

## Confirmed Design Direction

- Use URIs as stable machine handles for resources and natural-language metadata as model-facing semantics.
- Treat resources as context surfaces and tools as action surfaces.
- Treat MCP protocol descriptors as the primary source of truth for model discovery, not README prose.
- Keep README examples aligned with descriptors, but do not rely on README as the model's only semantic input.
- Prefer explicit structured metadata over relying on models to infer behavior from names.

## Confirmed Decisions

- Include all items that materially affect large-language-model usability in this specification.
  This includes descriptors, schemas, outputs, workflow guidance, recoverable errors, capability metadata, side-effect semantics, prompts, and completions.
- Use an API-breaking implementation for the MCP feature branch.
  Existing MCP API compatibility does not need to be preserved when it conflicts with the model-friendly surface.
- Add the resource descriptor contract directly to `MCPResourceHandler`.
  Do not use an adapter-only or default-method migration path.
- Split SQL execution into separate read-only query and update-capable SQL tools.
  Their descriptions must clearly state read-only versus mutating behavior.
- Store descriptor metadata in external YAML descriptor files as the canonical metadata source.
  Runtime Java descriptors and documentation should be generated from or validated against that source.
- Record MCP prompts and completions as requirements, but defer their implementation beyond the first implementation phase.
- Use Option A for `shardingsphere://capabilities`.
  The resource must expose the full descriptor catalog as a generated aggregate view from the same external descriptor source.

## User Scenarios and Acceptance Checks

### Scenario 1: Model discovers encrypt resources before planning

Given a user asks to create or alter encryption on a logical table column, when the MCP client exposes resource templates to the model,
then the model can identify the encrypt algorithms resource and the encrypt table rules resource before calling the planning tool.

Acceptance checks:

- The encrypt algorithms resource has a description that says it lists algorithm plugins visible from the current ShardingSphere-Proxy runtime.
- The encrypt table rules resource template has a description that says it reads rules for one logical table.
- Template variable metadata explains that `database` and `table` are logical names exposed by ShardingSphere-Proxy.
- The planning tool metadata references these related resources.

### Scenario 2: Model selects the right planning tool

Given a user asks for encryption or masking changes, when the model sees `tools/list`, then it can distinguish `plan_encrypt_rule` from `plan_mask_rule` without relying on README context.

Acceptance checks:

- Each planning tool description states the target rule family, supported lifecycle operations, logical object scope, and that it plans but does not execute.
- Each planning tool description states that follow-up execution uses `apply_workflow` and final checking uses `validate_workflow`.
- Each planning tool input schema exposes the minimum useful first-call fields and the supported continuation field `plan_id`.

### Scenario 3: Model fills structured intent evidence

Given a user says that a column must support decryption, equality filtering, LIKE queries, or domain-specific field semantics,
when the model fills the planning tool arguments, then it can use documented structured fields instead of hidden object keys.

Acceptance checks:

- `structured_intent_evidence` has declared properties for decrypt, equality, LIKE, and field semantics requirements.
- Field descriptions explain how structured evidence interacts with `natural_language_intent`.
- Unknown or unsupported structured evidence is either rejected with recoverable details or safely ignored with an explicit warning.

### Scenario 4: Model continues a workflow after a planning response

Given a planning tool returns a response, when the model reads the structured result, then it knows whether to ask the user, call planning again, call `apply_workflow`, or call `validate_workflow`.

Acceptance checks:

- Planning responses include stable `plan_id`, `status`, `missing_required_inputs`, `issues`, `next_actions`, and `recommended_next_tool` fields.
- Planning responses include `related_resources` or `resources_to_read` when more context should be loaded.
- Planning responses include `requires_user_approval` before any side-effecting follow-up.
- If a response needs more information, missing inputs include field names, reasons, and user-facing questions.

### Scenario 5: Model handles side-effecting tools safely

Given a user or model is about to call a tool that may execute SQL, DDL, or DistSQL, when the tool is discovered or called, then side effects are visible before execution.

Acceptance checks:

- Side-effecting tools declare whether they may mutate data, metadata, rules, or physical structures.
- `apply_workflow` explains the difference between `auto-execute`, `review-then-execute`, and `manual-only`.
- `execute_query` is either constrained to read-only behavior or its name, description, annotations, and validation explain the actual supported statement classes.
- Responses from side-effecting tools include execution mode, executed artifacts, skipped artifacts, and validation recommendations.

### Scenario 6: Model recovers from invalid calls

Given the model sends a request with missing or invalid arguments, when the MCP server returns an error, then the model can repair the next request without guessing from free text.

Acceptance checks:

- Error payloads include stable `error_code`, human-readable `message`, and machine-readable recovery fields.
- Missing required argument errors include `missing_fields`.
- Invalid argument errors include `invalid_fields`, expected values or formats, and a recommended next call shape when practical.
- Workflow state errors identify whether the model should re-plan, re-use `plan_id`, or ask the user for confirmation.

### Scenario 7: Feature author adds a new MCP feature

Given a ShardingSphere feature author adds a new MCP tool or resource, when they implement and register handlers, then they must provide model-facing descriptors, schemas, examples, and tests.

Acceptance checks:

- New resource handlers cannot rely only on URI patterns for protocol descriptions.
- New tool handlers cannot use open-ended object parameters for known structured inputs.
- Tests fail if a public tool or resource has placeholder, URI-only, or blank model-facing metadata.
- README content must not be the only place where tool/resource semantics are defined.

## Functional Requirements

### Resource Discovery Requirements

- **FR-001**: Introduce a model-facing resource descriptor contract for every public `MCPResourceHandler`.
- **FR-002**: Each resource descriptor must expose URI or URI template, stable `name`, human-readable `title`, business-level `description`, and `mimeType`.
- **FR-003**: Resource descriptions must explain purpose, use timing, logical versus physical object scope, and returned payload kind.
- **FR-004**: The runtime must not publish placeholder descriptions such as `ShardingSphere MCP resource: <uri>` for public resources after descriptor migration.
- **FR-005**: Resource templates must expose variable semantics for every template variable, including `database`, `schema`, `table`, `column`, `index`, and `sequence` where applicable.
- **FR-006**: Variable semantics must state whether the value is a ShardingSphere logical object, database-native schema object, physical storage object, or protocol-specific object.
- **FR-007**: Resource descriptors must identify resources that depend on ShardingSphere-Proxy runtime visibility, such as encrypt and mask algorithm resources.
- **FR-008**: Resource descriptors must distinguish list resources from detail resources.
- **FR-009**: Resource descriptors must support optional model-facing examples for complex URI templates.
- **FR-010**: Resource discovery tests must cover fixed resources and templated resources separately.

### Tool Discovery Requirements

- **FR-011**: Every public `MCPToolDescriptor` description must explain when to call the tool, what it does, what it does not do, and its expected follow-up.
- **FR-012**: Tool descriptions for planning tools must state that they plan only and do not execute DDL, DistSQL, or data changes.
- **FR-013**: Tool descriptions for side-effecting tools must declare side-effect scope before execution.
- **FR-014**: Tool descriptors must expose related resources when the model should read context before calling the tool.
- **FR-015**: Tool descriptors must expose related follow-up tools when a tool participates in a multi-step workflow.
- **FR-016**: Tool input schemas must keep enum values explicit for lifecycle operations, delivery modes, execution modes, metadata object types, and any future closed value sets.
- **FR-017**: Tool input field descriptions must define whether values refer to logical ShardingSphere objects or physical database objects.
- **FR-018**: Required fields must be used for fields that are always required for a successful first call.
- **FR-019**: Optional fields must explain when omitting them is acceptable and what default or clarification behavior follows.
- **FR-020**: Tool descriptor tests must fail on blank, placeholder, or purely restated descriptions.

### Structured Input Schema Requirements

- **FR-021**: Extend the MCP tool value schema model so known object parameters can declare named properties instead of only `additionalProperties: true`.
- **FR-022**: `structured_intent_evidence` must declare properties for `requires_decrypt`, `requires_equality_filter`, `requires_like_query`, and `field_semantics`.
- **FR-023**: `user_overrides` must declare supported override keys for each planning tool.
- **FR-024**: Encrypt algorithm property objects must distinguish primary, assisted-query, and LIKE-query algorithm property scopes.
- **FR-025**: Mask algorithm property objects must distinguish primary mask algorithm properties from user overrides.
- **FR-026**: Known nested object fields must include descriptions, types, and required flags where the domain can define them.
- **FR-027**: Open-ended object support may remain available only for intentionally plugin-defined property maps, and descriptions must state that the accepted keys depend on algorithm descriptors.
- **FR-028**: When accepted object keys depend on visible algorithm plugins, planning responses must include property requirements for the selected algorithms.

### Output Schema and Structured Result Requirements

- **FR-029**: Public tools must publish output schemas where the MCP SDK and protocol version support them.
- **FR-030**: If output schemas are not supported by the current SDK, the same structures must be documented through a model-facing descriptor extension and covered by tests.
- **FR-031**: `search_metadata` output must declare item structure, object type, logical path fields, and pagination fields.
- **FR-032**: `execute_query` output must declare result kind, statement class, rows, update counts, warnings, and truncation or timeout indicators.
- **FR-033**: `plan_encrypt_rule` and `plan_mask_rule` outputs must declare plan identity, planning status, missing inputs, issues, artifacts,
  property requirements, validation strategy, and next actions.
- **FR-034**: `apply_workflow` output must declare execution mode, applied artifacts, skipped artifacts, manual artifacts, errors, and recommended validation action.
- **FR-035**: `validate_workflow` output must declare validation status, validation layers, mismatches, warnings, and recommended recovery action.
- **FR-036**: Tool results must continue to provide structured content, not only text content.

### Workflow Guidance Requirements

- **FR-037**: Workflow planning responses must include stable `status` values that distinguish needs-input, ready-for-review, ready-to-apply, applied, validation-failed, and failed states.
- **FR-038**: Workflow planning responses must include `next_actions` as structured, ordered recommendations.
- **FR-039**: A recommended next action must include action kind, target tool or resource when applicable, reason, required arguments, and whether user approval is required.
- **FR-040**: Planning responses must include `recommended_next_tool` only when one next tool is preferred.
- **FR-041**: Planning responses must include `resources_to_read` when more context should be loaded before the next tool call.
- **FR-042**: Workflow responses must preserve `plan_id` across all follow-up calls.
- **FR-043**: Continuation calls with `plan_id` must have clear schema and description guidance.
- **FR-044**: Workflow guidance must never encourage automatic execution of side-effecting artifacts without user-visible approval policy.

### Capability Catalog Requirements

- **FR-045**: Capability discovery must expose resource descriptors, resource template descriptors, tool descriptors, and workflow relationships.
- **FR-046**: Capability discovery must indicate protocol-level availability for resources, resource templates, tools, prompts, completions, and annotations when implemented.
- **FR-047**: Capability discovery must identify the runtime topology relevant to MCP workflows, including ShardingSphere-Proxy-only workflow limitations where applicable.
- **FR-048**: `shardingsphere://capabilities` must publish the full descriptor catalog as a generated aggregate view.

### Relationship and Navigation Requirements

- **FR-049**: Tool descriptors must support related resource metadata.
- **FR-050**: Resource descriptors must support related tool metadata when a resource is usually consumed before an action.
- **FR-051**: Workflow descriptors must describe the normal path from planning to apply to validation.
- **FR-052**: Relationships must be exposed in a machine-readable structure, not only prose.
- **FR-053**: README examples must be updated to mirror the descriptor-driven workflow, but README must remain secondary to protocol metadata.

### Safety and Side-Effect Requirements

- **FR-054**: Side-effecting tools must declare whether they are read-only, destructive, idempotent, or require approval where the MCP SDK supports annotations.
- **FR-055**: If the SDK does not support standard annotations, side-effect semantics must be exposed through descriptor metadata and descriptions.
- **FR-056**: `apply_workflow` must expose that it may execute generated DDL or DistSQL when execution mode permits.
- **FR-057**: `apply_workflow` must expose `manual-only` as the safe export mode that does not execute artifacts.
- **FR-058**: SQL execution must be split into a read-only query tool and an update-capable SQL tool, with descriptions and side-effect metadata for both.
- **FR-059**: Side-effecting tool outputs must include a summary of what actually ran and what did not run.

### Recoverable Error Requirements

- **FR-060**: Error responses must include machine-readable recovery metadata for missing fields, invalid values, unsupported resources, unsupported tools, and workflow state errors.
- **FR-061**: Missing field errors must include field names, expected purpose, and whether the model should ask the user or infer from available resources.
- **FR-062**: Invalid enum errors must include allowed values.
- **FR-063**: Invalid URI errors must include matching resource templates when possible.
- **FR-064**: Workflow plan errors must state whether the plan can be continued, must be re-planned, or is no longer available in the session.
- **FR-065**: Recoverable errors must remain safe to show to users and must not leak secret algorithm properties.

### Completion and Prompt Requirements

- **FR-066**: Resource template completion requirements must be recorded for logical database, schema, table, column, index, and sequence variables.
- **FR-067**: Tool argument completion requirements must be recorded for enum-like fields and runtime-visible algorithms.
- **FR-068**: MCP prompt requirements must be recorded for common multi-step workflows such as inspect metadata, plan encrypt rule, plan mask rule, apply workflow, and validate workflow.
- **FR-069**: Prompt templates must reference protocol-visible tools and resources instead of duplicating private implementation details when a later phase implements them.

### Documentation and Test Requirements

- **FR-070**: Add protocol golden tests for `tools/list`, `resources/list`, and `resources/templates/list` metadata.
- **FR-071**: Add descriptor completeness tests for all built-in core, encrypt, and mask handlers.
- **FR-072**: Add schema tests that detect known object parameters published as unstructured `additionalProperties: true` without declared properties.
- **FR-073**: Add workflow response contract tests for `next_actions`, `recommended_next_tool`, `resources_to_read`, and `requires_user_approval`.
- **FR-074**: Add recoverable error contract tests for missing argument, invalid enum, unsupported resource URI, unsupported tool, and missing workflow plan cases.
- **FR-075**: Add documentation consistency checks or review requirements so README examples do not drift from descriptor metadata.
- **FR-076**: Public API Javadocs for new descriptor and schema contracts must explain model-facing semantics clearly.

## Key Entities

- **MCPResourceDescriptor**: Model-facing metadata for a fixed resource or resource template, including identity, description, content type, variable semantics, and related tools.
- **MCPResourceParameterDescriptor**: Metadata for one URI template variable, including name, meaning, object scope, examples, and completion hints.
- **MCPToolDescriptor**: Existing model-facing metadata for one tool, extended with richer descriptions, relationships, side-effect metadata, and output schema support.
- **MCPToolValueDefinition**: Input schema definition that must support structured object properties for known nested inputs.
- **MCPToolOutputDefinition**: Model-facing description of stable structured tool output.
- **MCPWorkflowGuidance**: Structured next-step metadata returned by planning, apply, and validation tools.
- **MCPRecoverableError**: Structured error payload that tells a model how to repair the next call.
- **MCPCapabilityCatalog**: Protocol-visible catalog that combines resources, resource templates, tools, prompts, completions, annotations, and workflow relationships.

## Current Surface Gap Inventory

### Core Resources

- `shardingsphere://capabilities` lacks descriptor-rich capability payloads.
- `shardingsphere://databases` and nested metadata resources need business descriptions and variable semantics.
- Database capability resources should explain database type, default schema semantics, statement support, and cross-schema behavior.

### Encrypt Resources

- `shardingsphere://features/encrypt/algorithms` needs a description for runtime-visible encrypt algorithms and recommendation use.
- `shardingsphere://features/encrypt/databases/{database}/rules` needs logical database scope and list semantics.
- `shardingsphere://features/encrypt/databases/{database}/tables/{table}/rules` needs logical table scope and pre-planning use timing.

### Mask Resources

- `shardingsphere://features/mask/algorithms` needs a description for runtime-visible mask algorithms and recommendation use.
- `shardingsphere://features/mask/databases/{database}/rules` needs logical database scope and list semantics.
- `shardingsphere://features/mask/databases/{database}/tables/{table}/rules` needs logical table scope and pre-planning use timing.

### Core Tools

- `search_metadata` needs output schema and relationship guidance to metadata resources.
- SQL execution needs a read-only query tool, an update-capable SQL tool, output schemas, and clear side-effect metadata.

### Workflow Tools

- `plan_encrypt_rule` needs richer description, related resources, structured object schemas, output schema, next actions, and side-effect-safe follow-up guidance.
- `plan_mask_rule` needs richer description, related resources, structured object schemas, output schema, next actions, and side-effect-safe follow-up guidance.
- `apply_workflow` needs side-effect annotation, artifact execution output schema, approval semantics, and validation next action.
- `validate_workflow` needs validation output schema and recovery guidance.

## API-Breaking Migration Requirements

- Existing MCP resource URI patterns, tool names, and descriptor APIs may be changed when needed for model-friendly behavior.
- No deprecated aliases, compatibility shims, or default-method migration path is required for the MCP API in this feature branch.
- Existing `structuredContent` in tool results should remain available unless output-schema migration proves a cleaner replacement.
- Descriptor additions must not require feature handlers to depend on bootstrap transport classes.
- New descriptor APIs must stay in MCP API or support layers according to existing module boundaries.
- External YAML descriptor files must be treated as source files and must include ASF license headers.
- Generated `target/` files must not be edited during migration.

## Verification Requirements

- Run targeted tests for touched MCP modules when implementation begins.
- Run module-scoped Checkstyle and Spotless for touched MCP modules.
- Search verification must confirm no public resource description is still only the generated URI placeholder after migration.
- Search verification must confirm known object parameters expose structured properties where required.
- Protocol golden tests must verify resource, resource template, tool, and capability metadata.
- If output schema support depends on MCP Java SDK availability, verification must document the supported SDK behavior and fallback metadata path.

## Capability Catalog Decision

### Selected: Full `shardingsphere://capabilities` catalog

Make `shardingsphere://capabilities` return the complete descriptor catalog for resources, resource templates, tools, workflows, side-effect metadata, prompts, and completions.
The catalog is selected because model usability is the primary goal and one aggregate resource gives models and lightweight clients a direct operating map.
The catalog must be generated from the same external descriptor source used by standard discovery responses, so duplicated output does not become duplicated truth.

Advantages:

- Gives models and lightweight clients one stable resource to read before planning.
- Makes ShardingSphere-specific workflow relationships easy to discover without requiring clients to merge multiple list endpoints.
- Provides one convenient diagnostics payload for support, tests, and examples.
- Works well for clients that expose resources more naturally than all MCP discovery endpoints.

Disadvantages:

- Duplicates information already available through standard `tools/list`, `resources/list`, and `resources/templates/list`.
- Can become large as features grow, increasing context size and payload cost.
- Creates a second catalog that must stay synchronized with protocol-native discovery responses.
- May tempt clients to depend on ShardingSphere-specific catalog shape instead of standard MCP discovery.

### Rejected: Lightweight `shardingsphere://capabilities` index plus standard discovery

Keep `shardingsphere://capabilities` compact and use standard MCP discovery endpoints as the canonical source for full descriptors.
The capabilities resource would expose runtime topology, enabled feature families, and links to the standard discovery surfaces.

Advantages:

- Avoids duplicating the full tool and resource descriptor catalog.
- Keeps payload small and lowers context cost for routine capability checks.
- Preserves stronger alignment with MCP standard discovery endpoints.
- Reduces synchronization risk between the capability resource and protocol-native list responses.

Disadvantages:

- Less convenient for clients that want one ShardingSphere-specific resource with the complete model-facing surface.
- Requires host applications or models to merge several discovery responses.
- Makes workflow relationships harder to inspect if the client does not expose the full standard discovery results to the model.
- Gives fewer benefits to simple debugging and golden-file inspection workflows.

## Open Questions

- Choose concrete tool names for the read-only query tool and the update-capable SQL tool.

## Success Criteria

- A model can inspect MCP discovery metadata and choose between metadata search, SQL execution, encrypt planning, mask planning, apply, validation, and resource reads without README context.
- A model can identify and read relevant encrypt or mask resources before planning changes.
- A model can fill first-call and continuation arguments with fewer guesses because structured object schemas and field descriptions are explicit.
- A model can continue a workflow from planning to apply to validation using stable next-action fields.
- A model can recover from missing fields, invalid values, unsupported URI templates, and missing plans using structured error metadata.
- Side-effecting tools are visible as side-effecting before execution.
- Public resource descriptions are business-level descriptions, not URI restatements.
- Descriptor completeness and protocol golden tests prevent regressions in model-facing metadata quality.
