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

# Implementation Plan: MCP Natural Language Markdown Resources

## Constraints

- Do not switch branches or run branch-changing Speckit commands.
- Do not edit production code in this design-only round.
- Preserve unrelated worktree changes.
- Follow `CODE_OF_CONDUCT.md`: keep source files readable, simple, consistent, and line length at or below 200 characters.
- Treat MCP server instructions as model-facing guidance, not as deterministic security or validation logic.
- Keep descriptor metadata, diagnostics, protocol identifiers, and structured payload contracts outside Markdown.
- Treat Markdown resource files as classpath/source resources unless explicitly designed as MCP protocol resources.

## Phase 0: Evidence and Inventory

Goal: make the migration source-driven before implementation.

Actions:

1. Record official MCP evidence for `InitializeResult.instructions`, prompts, discovery, and server-instruction boundaries.
2. Record MCP-builder checks for tool discoverability, structured responses, annotations, and actionable errors.
3. Inventory current hardcoded server instructions and current Markdown/YAML MCP resources.
4. Inventory candidate natural-language categories and assign each category one owner.

Exit criteria:

- `source-map.md` covers official sources, current code locations, current resources, and ownership decisions.
- No source category remains ambiguous before implementation starts.

## Phase 1: Markdown Resource Loading Foundation

Goal: establish a safe loader for model-facing Markdown resources.

Preferred design:

1. Prefer a small shared Markdown resource loader in MCP support because both prompts and server instructions are model-facing Markdown.
   A bootstrap-local loader is acceptable only for a deliberately server-only implementation slice with prompt header stripping recorded as a follow-up.
2. Load UTF-8 classpath resources through the thread context class loader, matching existing prompt loading style.
   If the thread context class loader is unavailable, fall back to the loader class's defining class loader.
3. Strip only a leading ASF HTML comment block from Markdown resources before returning model-facing content.
4. Trim only outer whitespace that is not meaningful model guidance.
5. Fail with a clear exception if the resource is missing, unreadable, or blank after header stripping.

Notes:

- Existing prompt Markdown appears to be loaded as raw text today.
  If implementation touches Markdown loading, prompt templates should use the same header-stripping path unless a compatibility reason blocks it.
- If the loader is local to bootstrap first, a later phase can reuse or promote it only after tests show the need.
- The loader must not parse prompt placeholders; placeholder rendering remains prompt-template behavior.
- Do not strip arbitrary Markdown comments after the leading ASF header.
  Authored comments elsewhere in a Markdown resource must remain content unless a separate syntax is designed.
- The header matcher should require ASF license markers such as `Licensed to the Apache Software Foundation` and `Apache License, Version 2.0`.
  A non-license leading HTML comment is authored content and should remain.
- Do not add YAML front matter or Markdown metadata parsing in this package.
  Descriptor metadata remains in descriptor YAML; Markdown resources contain authored guidance text only.
- Avoid generic path-only global caching in the shared loader.
  Resource lookup can depend on class loader context, especially in tests and plugin-oriented runtime paths.
  If caching is needed later, key it by both class loader and resource path, and document why.

Exit criteria:

- Loader behavior has dedicated tests for normal content, missing resource, blank resource, and source-header stripping.
- Header stripping tests cover both an ASF header and a non-license leading HTML comment that must remain.
- Loader tests cover a missing thread context class loader fallback.
- Prompt template validation still extracts placeholders after header stripping and before rendering.
- The design avoids silent fallback to Java prompt prose.

## Phase 2: Server Instructions Migration

Goal: move server-level MCP instructions from Java constant text to an authored Markdown resource.

Actions:

1. Add `mcp/bootstrap/src/main/resources/META-INF/shardingsphere-mcp/instructions/server-instructions.md`.
   This creates the bootstrap module's main resources tree if it does not already exist.
2. Preserve the existing semantics:
   - use official MCP list discovery methods;
   - use `completion/complete` for supported argument values;
   - treat `shardingsphere://capabilities` as optional domain catalog guidance;
   - use `database_gateway_execute_query` only for read-only `SELECT` or `EXPLAIN ANALYZE`;
   - use `database_gateway_execute_update` with `execution_mode=preview` before side effects;
   - continue from `next_actions` or `recovery.next_actions`.
3. Replace the long `SERVER_INSTRUCTIONS` string with a resource path constant or a loader call.
4. Keep `PROTOCOL_VERSION`, `SUPPORTED_PROTOCOL_VERSIONS`, and `SERVER_NAME` in Java constants.
5. Treat future `server/discover` as a compatibility watch point.
   If implemented later, it should reuse the same instruction resource rather than introduce a second copy.
6. Keep the instruction text static and concise.
   Do not embed runtime database metadata, descriptor inventories, prompt bodies, or generated capability payloads.
7. Do not expose `server-instructions.md` through `resources/list` or create a new `shardingsphere://` URI for it in this package.
8. Do not implement hot reload for server instructions.
   Changes to instruction Markdown take effect on server restart or new server construction.

Exit criteria:

- No long prompt prose remains in `MCPTransportConstants`.
- Public initialization behavior exposes the Markdown-derived instruction text.
- Server instructions do not duplicate tool, prompt, or resource descriptor bodies.
- The bootstrap artifact includes the instruction Markdown resource.
- The MCP distribution includes a bootstrap jar in `lib` that contains the instruction Markdown resource.
- Dynamic runtime state remains discoverable through resources, tools, prompts, and structured payloads rather than server instructions.
- The instruction Markdown resource remains internal classpath content and does not expand the MCP resource surface.
- Instruction text is stable for the lifetime of a created server instance.

## Phase 3: Full Natural-Language Classification

Goal: cover all phases beyond server instructions without bulk-moving unsuitable strings.

Actions:

1. Search MCP modules for string literals and resources that are model-facing, client-facing, descriptor-facing, diagnostic, or contract-facing.
2. Classify each hit using the ownership map in `source-map.md`.
3. Move only authored prose that passes all Markdown criteria:
   - paragraph or list style guidance;
   - not field-owned descriptor metadata;
   - not a runtime diagnostic;
   - not a JSON key, enum, URI, tool name, prompt name, or status;
   - not consumed as structured data by code.
4. Keep existing prompt Markdown resources as canonical prompt templates.
5. Record every non-moved natural-language category as an intentional exclusion.

Candidate handling:

- Existing prompt Markdown: keep and route through the safer loader if implementation changes model-facing Markdown loading.
- Descriptor YAML descriptions: keep in YAML.
- Elicitation copy: classify separately; keep in code or a message catalog unless it becomes reusable authored guidance.
- Workflow payload guidance: keep structured unless it is pure explanatory prose outside the payload contract.
- Algorithm/property metadata: keep structured.
- Intent aliases and keywords: keep code or structured dictionary data.

Exit criteria:

- A future implementation source map lists each moved and non-moved category.
- No free-form Markdown replaces structured client contracts.

## Phase 4: Exclusion Protection

Goal: prevent accidental migration of natural language that belongs in structured or code-owned locations.

Actions:

1. Keep descriptor text in descriptor YAML and schema resources.
2. Keep diagnostics in code paths unless a separate message-catalog design is approved.
3. Keep machine contract text in code, descriptors, or structured payload schemas.
4. Keep workflow response keys and next-action structures as JSON-compatible structured data.
5. Keep algorithm recommendations and property templates in structured resources.

Recommended searches before implementation handoff:

```bash
rg -n "SERVER_INSTRUCTIONS|instructions\\(" mcp
rg -n "description:|title:" mcp/**/src/main/resources/META-INF/shardingsphere-mcp/mcp-descriptors
rg -n "next_actions|recovery\\.next_actions|operation_id|tool_name|uriTemplate" mcp
rg -n "throw new|IllegalArgumentException|IllegalStateException|log\\." mcp
```

Exit criteria:

- Review evidence proves descriptor metadata, diagnostics, and machine contracts were not moved to Markdown by mistake.

## Phase 5: Verification and Delivery

Goal: prove the migration works and stays within scope.

Implementation verification:

1. Unit-test the Markdown resource loader.
2. Assert the server exposes Markdown-derived `instructions`.
   Prefer HTTP and STDIO initialize-response assertions through `AbstractMCPWireBehaviorTest` or a small test-helper hook.
   Use `Plugins.getMemberAccessor()` only if no public wire-level or SDK-supported path exists.
3. Assert source headers are stripped from delivered model-facing text.
4. Assert prompt template text also strips source headers if prompt loading uses the shared Markdown path.
5. Assert prompt placeholder extraction still validates rendered prompt arguments after header stripping.
6. Assert prompt input and output validation expectations remain unchanged after loader changes.
7. Assert missing and blank resources fail visibly.
8. Assert packaged bootstrap resources include `META-INF/shardingsphere-mcp/instructions/server-instructions.md`.
   For distribution checks, inspect the bootstrap jar copied under the generated MCP distribution `lib` directory.
9. Assert `resources/list` does not gain a server-instructions resource.
10. Run source searches for excluded categories.
11. Run the narrowest module tests and style checks for touched MCP modules.

Likely commands after code changes:

```bash
./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest=MCPSyncServerFactoryTest test
./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest='*MCPWire*Test' -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl mcp/bootstrap -DskipITs -Dspotless.skip=true -Dtest='*MCP*Instruction*Test' -Dsurefire.failIfNoSpecifiedTests=false test
./mvnw -pl distribution/mcp -am -DskipTests package
./mvnw -pl mcp/bootstrap -am -Pcheck -DskipTests checkstyle:check
./mvnw -pl mcp/bootstrap -am -Pcheck spotless:check
```

Delivery evidence:

- List changed files.
- State why each moved text is Markdown-owned.
- State why each natural-language exclusion remains outside Markdown.
- Report every command with exit code.
- Call out any skipped E2E or coverage command with a reason and exact follow-up command.

## MCP-Builder Reasonableness Check

- Server instructions should explain cross-feature relationships and operational patterns.
- Instructions should not restate full tool, resource, prompt, or schema descriptions already exposed through discovery.
- Instructions should stay concise to avoid wasting client context.
- Tool safety must remain deterministic through handlers, schemas, annotations, and preview/approval logic.
- Markdown is appropriate for model-facing prose; JSON/YAML remains appropriate for programmatic processing.
- Error messages should remain actionable diagnostics rather than model instructions.
- Structured tool results and `outputSchema` remain authoritative for machine-readable outputs.
- Prompt templates remain user-controlled MCP prompts; server instructions remain session-level server guidance.
- Classpath Markdown resources are not automatically MCP resources.
- Versioned MCP documentation URLs are authoritative for this package; `latest` is a drift check only.

## Reanalysis Results

- **Latest prompt source**: use MCP `2025-11-25` prompt documentation in source mapping.
- **Discovery draft**: keep `server/discover` as future compatibility evidence only.
- **Header stripping**: narrow the stripping rule to leading ASF HTML comments in Markdown resources.
- **Existing prompts**: include prompt-template header stripping in the design because prompts are also model-facing Markdown.
- **Verification route**: prefer initialize-response wire tests over SDK-private reflection for exposed instructions.
- **Packaging**: include a package-level resource check so the authored instruction file survives bootstrap and distribution packaging.
- **Loader state**: avoid path-only global caching because classpath resources may depend on the active class loader.
- **Metadata boundary**: do not introduce Markdown front matter; keep descriptor metadata in YAML.
- **Resource surface**: do not expose `server-instructions.md` through `resources/list`.
- **Reload boundary**: no hot reload; instruction changes require server reconstruction or restart.
- **Version drift**: `latest` currently maps to `2025-11-25`; keep explicit versioned citations in implementation evidence.

## Open Decisions

- The server instruction path is confirmed:
  `mcp/bootstrap/src/main/resources/META-INF/shardingsphere-mcp/instructions/server-instructions.md`.
- The implementation breadth still needs a later user command before code changes begin.
- No additional scope decision is open at the design level after this plan.
