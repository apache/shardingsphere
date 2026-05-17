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

# Requirements Checklist: MCP Natural Language Markdown Resources

**Purpose**: Validate the Speckit package before any MCP resource-loading implementation starts.
**Created**: 2026-05-17
**Package**: `.specify/specs/025-mcp-natural-language-md-resources/`

## Governance

- [x] Active branch confirmed as `001-shardingsphere-mcp` without switching.
- [x] Package created manually without branch-changing Speckit commands.
- [x] Documentation-only work performed before implementation authorization.
- [x] Existing worktree changes preserved and not reverted, reformatted, or staged.
- [x] Repository rules from `AGENTS.md` and `CODE_OF_CONDUCT.md` considered.
- [x] New source-controlled Markdown files include Apache license headers.
- [x] Generated directories such as `target/` are untouched.

## Requirement Quality

- [x] The target problem is stated as natural-language resource ownership, not MCP behavior redesign.
- [x] Server-level instructions are identified as the first Markdown migration target.
- [x] Full-phase design is required for inventory, resource loading, classification, migration, exclusion protection, and verification.
- [x] Server instruction resource path is confirmed as `mcp/bootstrap/src/main/resources/META-INF/shardingsphere-mcp/instructions/server-instructions.md`.
- [x] The reason for moving server instructions is captured: long model-facing prompt prose does not belong in transport constants.
- [x] The target preserves official MCP discovery guidance.
- [x] The target preserves completion guidance for operation IDs and known argument names.
- [x] The target preserves optional resource catalog guidance.
- [x] The target preserves SQL read-only/update separation.
- [x] The target preserves preview or approval guidance before side-effecting operations.
- [x] The target preserves structured `next_actions` and `recovery.next_actions` guidance.
- [x] Java constants are limited to behavior constants, protocol names, resource paths, or loader identifiers after implementation.
- [x] Missing or blank Markdown instruction resource behavior is required to fail visibly instead of silently falling back.
- [x] Model-facing Markdown loading must strip source license headers before exposing text to MCP clients.
- [x] Header stripping is narrow and limited to leading ASF HTML comment blocks in Markdown resources.
- [x] Header stripping requires ASF license markers and must preserve ordinary leading HTML comments.
- [x] Existing prompt Markdown templates are included in the header-exposure analysis.
- [x] Bootstrap and MCP distribution packaging must include the server instruction Markdown resource after implementation.
- [x] Shared Markdown loading avoids path-only global caching across class loader contexts.
- [x] Prompt placeholder extraction and rendering semantics must remain unchanged.
- [x] Markdown front matter is excluded; descriptor metadata remains in YAML.
- [x] Server instructions remain static and concise; runtime metadata and descriptor inventories stay structured.
- [x] Server instruction Markdown is an internal classpath resource, not a new MCP `resources/list` entry.
- [x] Server instructions do not add hot reload behavior in this package.
- [x] Versioned MCP documentation URLs are preferred; `latest` is only a drift check.
- [x] Existing prompt Markdown templates remain canonical and are not duplicated.

## Exclusion Quality

- [x] Descriptor YAML titles and descriptions are excluded from Markdown migration.
- [x] Tool, resource, prompt, argument, and JSON Schema descriptions remain structured descriptor data.
- [x] Exception, validation, HTTP error, and log messages are excluded from Markdown migration.
- [x] JSON keys, status labels, operation IDs, tool names, prompt names, resource URI templates, and enum-like values are excluded from Markdown migration.
- [x] Structured workflow response payloads remain deterministic client contracts.
- [x] Algorithm property templates, recommendation metadata, and capability maps remain structured data.
- [x] Intent keywords and aliases remain code or structured dictionary data, not Markdown prose.
- [x] Future localization or diagnostic-message catalogs are identified as separate designs outside this package.

## Testability

- [x] Future verification must prove exposed server instructions match the authored Markdown resource.
- [x] Future verification must preserve instruction semantics after migration.
- [x] Future verification must cover missing or blank instruction resource behavior.
- [x] Future source searches or review evidence must confirm excluded categories were not moved to Markdown by mistake.
- [x] Future implementation must report scoped verification commands and exit codes.
- [x] Style and resource-loading checks are required for the touched modules after implementation.

## Confirmed Decisions Before Code

- [x] Do not switch branches.
- [x] Do not run the Speckit feature creation script because it creates and checks out a branch.
- [x] Do not edit production code, tests, descriptors, distribution files, or generated files in this round.
- [x] Move `MCPTransportConstants.SERVER_INSTRUCTIONS` prose to Markdown in a later implementation slice.
- [x] Keep descriptor-owned natural language in YAML or schema resources.
- [x] Keep runtime diagnostics outside Markdown.
- [x] Keep machine-readable protocol and payload contract text outside Markdown.
- [x] Treat broader workflow guidance migration as part of the phased design and classify each candidate before implementation.
